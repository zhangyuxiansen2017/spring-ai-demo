package com.octopus.ai.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/document")
public class DocumentUploadController {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadController.class);
    private final MilvusVectorStore vectorStore;

    @Autowired
    public DocumentUploadController(MilvusVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 文档上传支持 csv、md、txt、docx、pdf
     * @param file
     * @param metadata
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "metadata", required = false, defaultValue = "未知来源") String metadata) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "请选择文件"));
        }

        try {
            String content = extractTextFromFile(file);
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "无法从文件中提取文本"));
            }

            // 将文本分割成较小的块，每块最多2000个字符
            List<String> chunks = splitTextIntoChunks(content, 2000);
            
            for (String chunk : chunks) {
                Document document = new Document(chunk, new HashMap<>());
                document.getMetadata().put("source", metadata);
                document.getMetadata().put("filename", file.getOriginalFilename());
                document.getMetadata().put("id", UUID.randomUUID().toString());
                
                vectorStore.add(List.of(document));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "message", "文件已成功添加到知识库",
                "filename", file.getOriginalFilename(),
                "chunks", String.valueOf(chunks.size())
            ));
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", 
                "message", "处理文件时出错: " + e.getMessage()
            ));
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return null;
        
        // 根据文件扩展名选择不同的提取方法
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            return extractTextFromDocx(file);
        } else if (fileName.toLowerCase().endsWith(".txt") || 
                  fileName.toLowerCase().endsWith(".md") ||
                  fileName.toLowerCase().endsWith(".csv")) {
            return extractTextFromTextFile(file);
        } else {
            throw new IOException("不支持的文件类型: " + fileName);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private String extractTextFromTextFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);

            // 如果不是在文本末尾，尝试在句子边界处分割
            if (end < length) {
                // 向后查找最近的句子结束标记
                int sentenceEnd = text.indexOf(". ", end - 30);
                if (sentenceEnd > i && sentenceEnd < end + 100) { // 允许一定的灵活性
                    end = sentenceEnd + 2; // 包含句号和空格
                }
            }

            chunks.add(text.substring(i, end));
        }
        return chunks;
    }
}