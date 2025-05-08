package edu.ntu.ai.exam.model;

import lombok.Data;

@Data
public class Question {
    private Long id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String question_type;    // 题型
    private String knowledge_point; // 知识点
    private String difficulty;     // 难度

}