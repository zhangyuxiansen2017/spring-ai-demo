package com.octopus.ai.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("dog")
public class Dog {
    @Id
    private Integer id;
    private String name;
    private int age;
}
