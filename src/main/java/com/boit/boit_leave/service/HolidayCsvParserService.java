package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.HolidayCsvRowDto;

import lombok.RequiredArgsConstructor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayCsvParserService {

    public List<HolidayCsvRowDto> parse(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "CSV file is empty."
            );
        }

        List<HolidayCsvRowDto> rows =
                new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        );

                CSVParser parser =
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreEmptyLines(true)
                                .setTrim(true)
                                .build()
                                .parse(reader)
        ) {

            validateHeaders(parser);

            for (CSVRecord record : parser) {

                rows.add(
                        HolidayCsvRowDto.builder()

                                .holidayName(
                                        record.get(
                                                "holidayName"
                                        )
                                )

                                .holidayDate(
                                        record.get(
                                                "holidayDate"
                                        )
                                )

                                .holidayType(
                                        record.get(
                                                "holidayType"
                                        )
                                )

                                .description(
                                        record.get(
                                                "description"
                                        )
                                )

                                .build()
                );
            }
        }

        return rows;
    }

    private void validateHeaders(
            CSVParser parser
    ) {

        List<String> headers =
                parser.getHeaderNames();

        String[] requiredHeaders = {
                "holidayName",
                "holidayDate",
                "holidayType",
                "description"
        };

        for (String requiredHeader :
                requiredHeaders) {

            if (!headers.contains(
                    requiredHeader
            )) {

                throw new IllegalArgumentException(
                        "Missing required CSV column: "
                                + requiredHeader
                );
            }
        }
    }
}