package com.boit.boit_leave.AI.dto;

import java.util.List;

public class DocumentAnalysisResponse {

    private String documentType;

    private String summary;

    private int confidenceScore;

    private boolean readable;

    private boolean signatureFound;

    private boolean stampFound;

    private boolean qrCodeFound;

    private String blurLevel;

    private String recommendation;

    private List<ExtractedField> extractedFields;

    private List<RiskIndicator> risks;

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(int confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isSignatureFound() {
		return signatureFound;
	}

	public void setSignatureFound(boolean signatureFound) {
		this.signatureFound = signatureFound;
	}

	public boolean isStampFound() {
		return stampFound;
	}

	public void setStampFound(boolean stampFound) {
		this.stampFound = stampFound;
	}

	public boolean isQrCodeFound() {
		return qrCodeFound;
	}

	public void setQrCodeFound(boolean qrCodeFound) {
		this.qrCodeFound = qrCodeFound;
	}

	public String getBlurLevel() {
		return blurLevel;
	}

	public void setBlurLevel(String blurLevel) {
		this.blurLevel = blurLevel;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	public List<ExtractedField> getExtractedFields() {
		return extractedFields;
	}

	public void setExtractedFields(List<ExtractedField> extractedFields) {
		this.extractedFields = extractedFields;
	}

	public List<RiskIndicator> getRisks() {
		return risks;
	}

	public void setRisks(List<RiskIndicator> risks) {
		this.risks = risks;
	}
    
    

}