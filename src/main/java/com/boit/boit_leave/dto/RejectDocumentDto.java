package com.boit.boit_leave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectDocumentDto {

    @NotBlank
    private String rejectionComments;

	public String getRejectionComments() {
		return rejectionComments;
	}

	public void setRejectionComments(String rejectionComments) {
		this.rejectionComments = rejectionComments;
	}
    
    

}