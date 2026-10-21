package com.boit.boit_leave.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileValidationUtil {

    /*
     * Maximum File Size
     * 10 MB
     */
    private static final long MAX_FILE_SIZE =
            10 * 1024 * 1024;

    /*
     * Allowed Content Types
     */
    private static final List<String> ALLOWED_TYPES =
            List.of(

                    "application/pdf",

                    "image/jpeg",

                    "image/jpg",

                    "image/png"

            );

    public void validate(
            MultipartFile file) {

        if(file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "Please select a file.");
        }

        if(file.getSize() >
                MAX_FILE_SIZE) {

            throw new RuntimeException(
                    "Maximum allowed file size is 10 MB.");
        }

        if(!ALLOWED_TYPES.contains(
                file.getContentType())) {

            throw new RuntimeException(
                    "Only PDF, JPG, JPEG and PNG files are allowed.");
        }

    }

}