package com.boit.boit_leave.dto;

import com.boit.boit_leave.entity.DocumentPriority;
import com.boit.boit_leave.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequestDto {

    @NotNull
    private Long leaveRequestId;

    @NotNull
    private DocumentType documentType;

    @NotBlank
    private String comments;
    
    private DocumentPriority priority;

	public Long getLeaveRequestId() {
		return leaveRequestId;
	}

	public void setLeaveRequestId(Long leaveRequestId) {
		this.leaveRequestId = leaveRequestId;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public DocumentPriority getPriority() {
		return priority;
	}

	public void setPriority(DocumentPriority priority) {
		this.priority = priority;
	}
	
	
    
    

}