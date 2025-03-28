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

}