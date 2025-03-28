package edu.ntu.ai.exam.model;

import lombok.Data;

import java.util.Date;

@Data
public class Exam {
    private Long id;
    private String examName;
    private Integer questionCount;
    private Date createTime;
    private Long creatorId;

}