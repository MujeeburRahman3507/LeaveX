package com.boit.boit_leave.AI.service;

import com.boit.boit_leave.AI.dto.DocumentAnalysisResponse;
import com.boit.boit_leave.AI.dto.ExtractedField;
import com.boit.boit_leave.AI.dto.RiskIndicator;
import com.boit.boit_leave.entity.LeaveDocument;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

@Service
public class DocumentIntelligenceService {

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;
    


    public DocumentIntelligenceService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper
    ) {

        this.chatClient =
                chatClientBuilder.build();

        this.objectMapper =
                objectMapper;
    }


    // =========================================================
    // EXISTING MULTIPART DOCUMENT ANALYSIS
    // =========================================================

    public DocumentAnalysisResponse analyze(
            MultipartFile file
    ) {

        try {

            if (file == null || file.isEmpty()) {

                throw new IllegalArgumentException(
                        "Document file is empty."
                );
            }


            String contentType =
                    file.getContentType();


            if (contentType == null) {

                throw new IllegalArgumentException(
                        "Unable to determine document type."
                );
            }


            String prompt =
                    buildAnalysisPrompt();


            String aiResponse;


            // -------------------------------------------------
            // PDF
            // -------------------------------------------------

            if (
                    contentType.equalsIgnoreCase(
                            "application/pdf"
                    )
            ) {

                aiResponse =
                        analyzePdf(
                                file,
                                prompt
                        );

            }


            // -------------------------------------------------
            // IMAGE
            // -------------------------------------------------

            else if (
                    contentType.equalsIgnoreCase("image/png")
                    || contentType.equalsIgnoreCase("image/jpeg")
                    || contentType.equalsIgnoreCase("image/jpg")
                    || contentType.equalsIgnoreCase("image/webp")
            ) {

                aiResponse =
                        analyzeImage(
                                file.getBytes(),
                                contentType,
                                prompt
                        );

            }


            // -------------------------------------------------
            // UNSUPPORTED
            // -------------------------------------------------

            else {

                throw new IllegalArgumentException(
                        "Unsupported document type: "
                                + contentType
                );
            }


            return parseResponse(
                    aiResponse
            );


        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to analyze document.",
                    e
            );
        }
    }


    // =========================================================
    // MANAGER - ANALYZE EXISTING UPLOADED DOCUMENT
    // =========================================================

    public DocumentAnalysisResponse analyzeDocument(
            LeaveDocument document
    ) {

        try {

            if (document == null) {

                throw new IllegalArgumentException(
                        "Document is required."
                );
            }
            


            // -------------------------------------------------
            // Get physical file path
            // -------------------------------------------------

            Path path =
                    Paths.get(
                            document.getFilePath()
                    );


            // -------------------------------------------------
            // Check file exists
            // -------------------------------------------------

            if (!Files.exists(path)) {

                throw new IllegalArgumentException(
                        "Document file not found: "
                                + document.getOriginalFileName()
                );
            }


            // -------------------------------------------------
            // Read file
            // -------------------------------------------------

            byte[] fileBytes =
                    Files.readAllBytes(path);


            if (fileBytes.length == 0) {

                throw new IllegalArgumentException(
                        "Document file is empty."
                );
            }


            // -------------------------------------------------
            // Determine content type
            // -------------------------------------------------

            String contentType =
                    document.getContentType();


            if (
                    contentType == null
                    || contentType.isBlank()
            ) {

                contentType =
                        Files.probeContentType(path);
            }


            if (contentType == null) {

                throw new IllegalArgumentException(
                        "Unable to determine document type."
                );
            }


            // -------------------------------------------------
            // Build prompt
            // -------------------------------------------------

            String prompt =
                    buildAnalysisPrompt();


            String aiResponse;


            // -------------------------------------------------
            // PDF
            // -------------------------------------------------

            if (
                    contentType.equalsIgnoreCase(
                            "application/pdf"
                    )
            ) {

                aiResponse =
                        analyzePdfBytes(
                                fileBytes,
                                prompt
                        );

            }


            // -------------------------------------------------
            // IMAGE
            // -------------------------------------------------

            else if (
                    contentType.equalsIgnoreCase("image/png")
                    || contentType.equalsIgnoreCase("image/jpeg")
                    || contentType.equalsIgnoreCase("image/jpg")
                    || contentType.equalsIgnoreCase("image/webp")
            ) {

                aiResponse =
                        analyzeImage(
                                fileBytes,
                                contentType,
                                prompt
                        );

            }


            // -------------------------------------------------
            // UNSUPPORTED
            // -------------------------------------------------

            else {

                throw new IllegalArgumentException(
                        "Unsupported document type: "
                                + contentType
                );
            }


            return parseResponse(
                    aiResponse
            );


        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to analyze document.",
                    e
            );
        }
    }
    


    // =========================================================
    // IMAGE ANALYSIS
    // =========================================================

    private String analyzeImage(
            byte[] imageBytes,
            String contentType,
            String prompt
    ) {

        Media media =
                new Media(
                        MimeTypeUtils.parseMimeType(
                                contentType
                        ),
                        new ByteArrayResource(
                                imageBytes
                        )
                );


        /*
         * Spring AI version used in this project
         * requires the 3-argument UserMessage constructor.
         */
        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(List.of(media))
                .build();

        return chatClient
                .prompt(
                        new Prompt(
                                userMessage,
                                OpenAiChatOptions
                                        .builder()
                                        .model("gpt-4o-mini")
                                        .temperature(0.1)
                                        .build()
                        )
                )
                .call()
                .content();
    }


    // =========================================================
    // EXISTING PDF ANALYSIS
    // =========================================================

    private String analyzePdf(
            MultipartFile file,
            String prompt
    ) throws Exception {

        List<Media> mediaList =
                new ArrayList<>();


        try (
                PDDocument document =
                        PDDocument.load(
                                file.getInputStream()
                        )
        ) {

            PDFRenderer renderer =
                    new PDFRenderer(
                            document
                    );


            /*
             * Analyze maximum 5 pages.
             */
            int pagesToAnalyze =
                    Math.min(
                            document.getNumberOfPages(),
                            5
                    );


            for (
                    int i = 0;
                    i < pagesToAnalyze;
                    i++
            ) {

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                i,
                                150
                        );


                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();


                ImageIO.write(
                        image,
                        "png",
                        output
                );


                mediaList.add(
                        new Media(
                                MimeTypeUtils.IMAGE_PNG,
                                new ByteArrayResource(
                                        output.toByteArray()
                                )
                        )
                );
            }
        }


        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(mediaList)
                .build();


        return chatClient
                .prompt(
                        new Prompt(
                                userMessage,
                                OpenAiChatOptions
                                        .builder()
                                        .model("gpt-4o-mini")
                                        .temperature(0.1)
                                        .build()
                        )
                )
                .call()
                .content();
    }


    // =========================================================
    // MANAGER PDF ANALYSIS USING BYTE[]
    // =========================================================

    private String analyzePdfBytes(
            byte[] pdfBytes,
            String prompt
    ) throws Exception {

        List<Media> mediaList =
                new ArrayList<>();


        try (
                PDDocument document =
                        PDDocument.load(
                                pdfBytes
                        )
        ) {

            PDFRenderer renderer =
                    new PDFRenderer(
                            document
                    );


            /*
             * Analyze maximum 5 pages.
             */
            int pagesToAnalyze =
                    Math.min(
                            document.getNumberOfPages(),
                            5
                    );


            for (
                    int i = 0;
                    i < pagesToAnalyze;
                    i++
            ) {

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                i,
                                150
                        );


                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();


                ImageIO.write(
                        image,
                        "png",
                        output
                );


                mediaList.add(
                        new Media(
                                MimeTypeUtils.IMAGE_PNG,
                                new ByteArrayResource(
                                        output.toByteArray()
                                )
                        )
                );
            }
        }


        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(mediaList)
                .build();


        return chatClient
                .prompt(
                        new Prompt(
                                userMessage,
                                OpenAiChatOptions
                                        .builder()
                                        .model("gpt-4o-mini")
                                        .temperature(0.1)
                                        .build()
                        )
                )
                .call()
                .content();
    }


    // =========================================================
    // AI PROMPT
    // =========================================================

    private String buildAnalysisPrompt() {

        return """
            You are an AI document analysis assistant
            for a Leave Management System.

            Analyze the uploaded employee supporting document
            carefully.

            Your job is to assist the MANAGER during
            leave-document review.

            DO NOT approve or reject the leave automatically.

            Determine:

            1. Document type

            2. Short document summary

            3. Confidence score from 0 to 100

            4. Whether the document is readable

            5. Approximate blur level:
               NONE, LOW, MEDIUM or HIGH

            6. Whether a signature appears visible

            7. Whether an official stamp/seal appears visible

            8. Whether a QR code appears visible

            9. Important information visible in the document

            10. Potential risks, inconsistencies or
                missing information

            11. A recommendation for the manager


            IMPORTANT RULES:

            - Never claim that a signature is valid or authentic.

            - Never claim that a stamp is valid or authentic.

            - Never claim that a QR code is valid or authentic.

            - Only report whether these elements
              appear visible.

            - Do not invent information.

            - Only extract information that is visible.

            - If information cannot be determined,
              use "Unknown".

            - The AI recommendation is advisory only.

            - The manager makes the final decision.

            - Do not approve or reject the leave.


            Return ONLY valid JSON.

            Use exactly this structure:

            {
              "documentType": "Medical Certificate",

              "summary": "Short summary of the document.",

              "confidenceScore": 95,

              "readable": true,

              "signatureFound": true,

              "stampFound": true,

              "qrCodeFound": false,

              "blurLevel": "LOW",

              "recommendation":
                "Document appears readable and suitable for manual manager review.",

              "extractedFields": [
                {
                  "fieldName": "Employee Name",
                  "value": "John Smith",
                  "confidence": 98
                },
                {
                  "fieldName": "Doctor Name",
                  "value": "Dr. David Lee",
                  "confidence": 94
                },
                {
                  "fieldName": "Document Date",
                  "value": "14 September 2026",
                  "confidence": 96
                }
              ],

              "risks": [
                {
                  "severity": "LOW",
                  "message": "QR code was not detected."
                }
              ]
            }


            CONFIDENCE RULES:

            - confidenceScore must be between 0 and 100.

            - Each extracted field confidence must
              be between 0 and 100.


            RISK SEVERITY:

            Use only:

            LOW
            MEDIUM
            HIGH


            If no risks are detected, return:

            "risks": []


            If no useful fields can be extracted, return:

            "extractedFields": []

            """;
    }


    // =========================================================
    // PARSE AI RESPONSE
    // =========================================================

    private DocumentAnalysisResponse parseResponse(
            String aiResponse
    ) throws Exception {

        if (
                aiResponse == null
                || aiResponse.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "AI returned an empty response."
            );
        }


        /*
         * Remove Markdown JSON code fences if the model
         * returns them despite the prompt.
         */
        String cleaned =
                aiResponse
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();


        JsonNode root =
                objectMapper.readTree(
                        cleaned
                );


        DocumentAnalysisResponse response =
                new DocumentAnalysisResponse();


        /*
         * Safe defaults
         */
        response.setExtractedFields(
                new ArrayList<>()
        );

        response.setRisks(
                new ArrayList<>()
        );


        // -----------------------------------------------------
        // Basic fields
        // -----------------------------------------------------

        response.setDocumentType(
                root.path("documentType")
                        .asText("Unknown")
        );


        response.setSummary(
                root.path("summary")
                        .asText("")
        );


        response.setConfidenceScore(
                root.path("confidenceScore")
                        .asInt(0)
        );


        response.setReadable(
                root.path("readable")
                        .asBoolean(false)
        );


        response.setSignatureFound(
                root.path("signatureFound")
                        .asBoolean(false)
        );


        response.setStampFound(
                root.path("stampFound")
                        .asBoolean(false)
        );


        response.setQrCodeFound(
                root.path("qrCodeFound")
                        .asBoolean(false)
        );


        response.setBlurLevel(
                root.path("blurLevel")
                        .asText("UNKNOWN")
        );


        response.setRecommendation(
                root.path("recommendation")
                        .asText("")
        );


        // -----------------------------------------------------
        // Extracted Fields
        // -----------------------------------------------------

        if (
                root.has("extractedFields")
                && root.get("extractedFields").isArray()
        ) {

            response.setExtractedFields(
                    objectMapper.convertValue(
                            root.get("extractedFields"),
                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(
                                            List.class,
                                            ExtractedField.class
                                    )
                    )
            );
        }


        // -----------------------------------------------------
        // Risk Indicators
        // -----------------------------------------------------

        if (
                root.has("risks")
                && root.get("risks").isArray()
        ) {

            response.setRisks(
                    objectMapper.convertValue(
                            root.get("risks"),
                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(
                                            List.class,
                                            RiskIndicator.class
                                    )
                    )
            );
        }


        return response;
    }
}