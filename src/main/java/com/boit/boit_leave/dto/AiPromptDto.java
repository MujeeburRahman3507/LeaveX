package com.boit.boit_leave.dto;

import lombok.Data;

@Data
public class AiPromptDto {
	
	private String prompt;

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

}
