package com.sandy.text.tosql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    private String question;

    private String content;

    private TrainingPolicy policy;




}
