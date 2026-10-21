package com.boit.boit_leave.AI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAiResponse {

    private String intent;

    private String answer;

    private int count;

    private List<EmployeeAiItem> items;

}