package edu.ntu.ai.exam.model;

import lombok.Data;

import java.util.Date;

@Data
public class Score {
    private Long id;
    private Long userId;
    private Long examId;
    private Integer score;
    private Date submitTime;
    private String userName;
    private String examName;
    private Integer actualScore;
    private Integer totalQuestions;
}