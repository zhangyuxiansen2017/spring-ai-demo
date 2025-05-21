const eventSource = new EventSource('/ai/generateStream?message=你好');
const outputDiv = document.getElementById('output');
let buffer = '';

eventSource.onmessage = function(event) {
  // 添加到缓冲区
  buffer += event.data;
  
  // 尝试按句子或段落更新显示
  if (buffer.endsWith('。') || buffer.endsWith('！') || buffer.endsWith('？') || 
      buffer.endsWith('.') || buffer.endsWith('!') || buffer.endsWith('?') ||
      buffer.includes('\n')) {
    outputDiv.textContent += buffer;
    buffer = '';
  }
};

eventSource.onerror = function() {
  // 确保最后的缓冲区内容也被显示
  if (buffer.length > 0) {
    outputDiv.textContent += buffer;
  }
  eventSource.close();
};