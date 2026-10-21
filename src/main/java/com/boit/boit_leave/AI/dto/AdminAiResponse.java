package com.boit.boit_leave.AI.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminAiResponse {

    private String intent;

    private String answer;

    private int count;

    private List<AdminAiItem> items;
}