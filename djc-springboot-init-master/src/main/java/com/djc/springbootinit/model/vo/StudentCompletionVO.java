package com.djc.springbootinit.model.vo;


import lombok.Data;

@Data
public class StudentCompletionVO {

    private String studentName;

    private Long studentId;

    private Boolean isCompletion;

    private static final long serialVersionUID = 1L;
}
