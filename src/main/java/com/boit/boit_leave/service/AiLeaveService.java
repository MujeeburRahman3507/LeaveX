package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.NaturalLeaveRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiLeaveService {

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;


    // ============================================================
    // MAIN AI METHOD
    // ============================================================

    public NaturalLeaveRequestDto parseLeave(String text) {

        try {

            String prompt =
                    """
                    You are an HR Leave Assistant.

                    Extract leave information from the employee request.

                    Return ONLY valid JSON.

                    Required format:

                    {
                      "leaveType":"MEDICAL",
                      "startDate":"2026-09-10",
                      "endDate":"2026-09-12",
                      "reason":"I am sick"
                    }

                    Leave type rules:

                    Fever -> MEDICAL
                    Sick -> MEDICAL
                    Ill -> MEDICAL
                    Hospital -> MEDICAL
                    Medical -> MEDICAL

                    Vacation -> EARNED
                    Travel -> EARNED
                    Trip -> EARNED
                    Holiday -> EARNED

                    Family function -> CASUAL
                    Personal -> CASUAL
                    Casual -> CASUAL

                    Date rules:

                    Convert natural dates such as:
                    tomorrow
                    next Monday
                    Monday to Wednesday
                    this Friday

                    into actual dates using today's date.

                    User Input:
                    %s
                    """
                    .formatted(text);


            String response =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();


            return objectMapper.readValue(
                    response,
                    NaturalLeaveRequestDto.class
            );


        } catch (Exception ex) {

            System.out.println(
                    "AI parsing failed. Falling back to rule-based parser."
            );

            return parseWithRules(text);
        }
    }


    // ============================================================
    // RULE BASED FALLBACK
    // ============================================================

    private NaturalLeaveRequestDto parseWithRules(String text) {

        text = URLDecoder.decode(
                text,
                StandardCharsets.UTF_8
        );

        String originalText = text;

        String lower =
                text.toLowerCase(Locale.ROOT);


        // ========================================================
        // LEAVE TYPE
        // ========================================================

        String leaveType = "CASUAL";


        if (
                lower.contains("fever")
                || lower.contains("medical")
                || lower.contains("hospital")
                || lower.contains("sick")
                || lower.contains("ill")
                || lower.contains("doctor")
        ) {

            leaveType = "MEDICAL";

        } else if (
                lower.contains("vacation")
                || lower.contains("trip")
                || lower.contains("travel")
                || lower.contains("holiday")
        ) {

            leaveType = "EARNED";

        }


        // ========================================================
        // DATE EXTRACTION
        // ========================================================

        LocalDate startDate = null;

        LocalDate endDate = null;


        // --------------------------------------------------------
        // 1. YYYY-MM-DD
        // --------------------------------------------------------

        Pattern isoPattern =
                Pattern.compile(
                        "\\b(\\d{4}-\\d{2}-\\d{2})\\b"
                );

        Matcher isoMatcher =
                isoPattern.matcher(text);


        if (isoMatcher.find()) {

            startDate =
                    parseDateSafely(
                            isoMatcher.group(1)
                    );

            if (isoMatcher.find()) {

                endDate =
                        parseDateSafely(
                                isoMatcher.group(1)
                        );
            }
        }


        // --------------------------------------------------------
        // 2. Natural date: tomorrow
        // --------------------------------------------------------

        if (startDate == null
                && lower.contains("tomorrow")) {

            startDate =
                    LocalDate.now()
                            .plusDays(1);

            endDate = startDate;
        }


        // --------------------------------------------------------
        // 3. Natural date: today
        // --------------------------------------------------------

        if (startDate == null
                && lower.contains("today")) {

            startDate =
                    LocalDate.now();

            endDate = startDate;
        }


        // --------------------------------------------------------
        // 4. "Monday to Wednesday"
        // --------------------------------------------------------

        if (startDate == null) {

            LocalDate[] dates =
                    extractWeekdayRange(lower);

            if (dates != null) {

                startDate = dates[0];

                endDate = dates[1];
            }
        }


        // --------------------------------------------------------
        // 5. "next Monday"
        // --------------------------------------------------------

        if (startDate == null) {

            LocalDate nextDay =
                    extractNextWeekday(lower);

            if (nextDay != null) {

                startDate = nextDay;

                endDate = nextDay;
            }
        }


        // --------------------------------------------------------
        // 6. "this Friday"
        // --------------------------------------------------------

        if (startDate == null) {

            LocalDate thisDay =
                    extractThisWeekday(lower);

            if (thisDay != null) {

                startDate = thisDay;

                endDate = thisDay;
            }
        }


        // ========================================================
        // REASON
        // ========================================================

        String reason =
                extractReason(originalText);


        if (
                reason == null
                || reason.isBlank()
        ) {

            if ("MEDICAL".equals(leaveType)) {

                reason = "Medical leave";

            } else if ("EARNED".equals(leaveType)) {

                reason = "Vacation";

            } else {

                reason = "General Leave";
            }
        }


        // ========================================================
        // BUILD RESPONSE
        // ========================================================

        return NaturalLeaveRequestDto
                .builder()

                .leaveType(
                        leaveType
                )

                .startDate(
                        startDate != null
                                ? startDate.toString()
                                : null
                )

                .endDate(
                        endDate != null
                                ? endDate.toString()
                                : null
                )

                .reason(
                        reason
                )

                .build();
    }


    // ============================================================
    // WEEKDAY RANGE
    // ============================================================

    private LocalDate[] extractWeekdayRange(
            String text) {

        String[] days = {

                "monday",
                "tuesday",
                "wednesday",
                "thursday",
                "friday",
                "saturday",
                "sunday"
        };


        for (String startDay : days) {

            for (String endDay : days) {

                String pattern =
                        "\\b"
                        + startDay
                        + "\\b\\s*(to|through|until|-|and)\\s*\\b"
                        + endDay
                        + "\\b";


                if (
                        Pattern
                                .compile(pattern)
                                .matcher(text)
                                .find()
                ) {

                    LocalDate start =
                            getNextOrCurrentDay(
                                    convertDay(startDay)
                            );

                    LocalDate end =
                            getNextOrCurrentDay(
                                    convertDay(endDay)
                            );


                    /*
                     * If the end day is before the start day,
                     * move the end date to the following week.
                     */

                    if (end.isBefore(start)) {

                        end = end.plusWeeks(1);
                    }


                    return new LocalDate[] {
                            start,
                            end
                    };
                }
            }
        }

        return null;
    }


    // ============================================================
    // NEXT WEEKDAY
    // ============================================================

    private LocalDate extractNextWeekday(
            String text) {

        Pattern pattern =
                Pattern.compile(
                        "next\\s+"
                        + "(monday|tuesday|wednesday|thursday|friday|saturday|sunday)"
                );


        Matcher matcher =
                pattern.matcher(text);


        if (!matcher.find()) {

            return null;
        }


        DayOfWeek day =
                convertDay(
                        matcher.group(1)
                );


        return LocalDate.now()
                .with(
                        TemporalAdjusters.next(day)
                );
    }


    // ============================================================
    // THIS WEEKDAY
    // ============================================================

    private LocalDate extractThisWeekday(
            String text) {

        Pattern pattern =
                Pattern.compile(
                        "this\\s+"
                        + "(monday|tuesday|wednesday|thursday|friday|saturday|sunday)"
                );


        Matcher matcher =
                pattern.matcher(text);


        if (!matcher.find()) {

            return null;
        }


        DayOfWeek day =
                convertDay(
                        matcher.group(1)
                );


        LocalDate today =
                LocalDate.now();


        /*
         * Find the weekday in the current week.
         */

        return today.with(
                TemporalAdjusters.previousOrSame(day)
        );
    }


    // ============================================================
    // CURRENT / NEXT DAY
    // ============================================================

    private LocalDate getNextOrCurrentDay(
            DayOfWeek targetDay) {

        LocalDate today =
                LocalDate.now();


        return today.with(
                TemporalAdjusters.nextOrSame(
                        targetDay
                )
        );
    }


    // ============================================================
    // DAY CONVERTER
    // ============================================================

    private DayOfWeek convertDay(
            String day) {

        return switch (
                day.toLowerCase(Locale.ROOT)
        ) {

            case "monday" ->
                    DayOfWeek.MONDAY;

            case "tuesday" ->
                    DayOfWeek.TUESDAY;

            case "wednesday" ->
                    DayOfWeek.WEDNESDAY;

            case "thursday" ->
                    DayOfWeek.THURSDAY;

            case "friday" ->
                    DayOfWeek.FRIDAY;

            case "saturday" ->
                    DayOfWeek.SATURDAY;

            case "sunday" ->
                    DayOfWeek.SUNDAY;

            default ->
                    throw new IllegalArgumentException(
                            "Invalid weekday: " + day
                    );
        };
    }


    // ============================================================
    // SAFE DATE PARSER
    // ============================================================

    private LocalDate parseDateSafely(
            String value) {

        try {

            return LocalDate.parse(
                    value,
                    DateTimeFormatter.ISO_LOCAL_DATE
            );

        } catch (DateTimeParseException ex) {

            return null;
        }
    }


    // ============================================================
    // REASON EXTRACTION
    // ============================================================

    private String extractReason(
            String text) {

        String lower =
                text.toLowerCase(Locale.ROOT);


        /*
         * Example:
         *
         * I need medical leave tomorrow
         * because I have fever
         */

        int becauseIndex =
                lower.indexOf("because");


        if (becauseIndex >= 0) {

            return text
                    .substring(
                            becauseIndex + "because".length()
                    )
                    .trim()
                    .replaceAll("[.!?]+$", "");
        }


        /*
         * Example:
         *
         * I need leave for family function
         */

        int forIndex =
                lower.indexOf(" for ");


        if (forIndex >= 0) {

            String reason =
                    text.substring(
                            forIndex + 5
                    )
                    .trim()
                    .replaceAll("[.!?]+$", "");


            if (!reason.isBlank()) {

                return reason;
            }
        }


        /*
         * Medical keyword fallback.
         */

        if (
                lower.contains("fever")
                || lower.contains("sick")
                || lower.contains("ill")
        ) {

            return "Medical leave due to sickness";
        }


        return null;
    }
}