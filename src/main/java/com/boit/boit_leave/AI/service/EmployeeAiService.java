package com.boit.boit_leave.AI.service;

import com.boit.boit_leave.AI.dto.EmployeeAiItem;
import com.boit.boit_leave.AI.dto.EmployeeAiResponse;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.entity.LeaveBalance;
import com.boit.boit_leave.service.LeaveService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmployeeAiService {

    private final LeaveService leaveService;


    public EmployeeAiResponse processQuestion(
            Long employeeId,
            String prompt) {

        if (prompt == null ||
                prompt.trim().isEmpty()) {

            return build(
                    "UNKNOWN",
                    "Please enter a question.",
                    0,
                    List.of()
            );
        }


        String question =
                prompt
                        .trim()
                        .toLowerCase(Locale.ROOT);


        /*
         * =====================================================
         * LEAVE BALANCE
         * =====================================================
         */

        if (
                question.contains("leave balance")
                ||
                question.contains("remaining leave")
                ||
                question.contains("leaves remaining")
                ||
                question.contains("how many leaves do i have")
                ||
                question.contains("how much leave")
        ) {

            LeaveBalance balance =
                    leaveService.getEmployeeBalance(
                            employeeId
                    );

//            int annual =
//                    balance.getAnnualBalance();
//
//            int used =
//                    balance.getUsedLeave();
//
//            int remaining =
//                    balance.getRemainingLeave();
            
            BigDecimal annual = balance.getAnnualBalance();
            BigDecimal used = balance.getUsedLeave();
            BigDecimal remaining = balance.getRemainingLeave();

            String answer =
                    "Your leave balance is "
                    + remaining
                    + " days remaining out of "
                    + annual
                    + " annual leave days. "
                    + "You have used "
                    + used
                    + " days.";

            return build(
                    "LEAVE_BALANCE",
                    answer,
                    1,
                    List.of(
                            EmployeeAiItem.builder()
                                    .totalDays(remaining)
                                    .status("REMAINING")
                                    .reason(
                                            "Annual="
                                            + annual
                                            + ", Used="
                                            + used
                                    )
                                    .build()
                    )
            );
        }
        
        /*
         * =====================================================
         * LEAVE USAGE PERCENTAGE
         * =====================================================
         */

        if (
                question.contains("leave usage percentage")
                ||
                question.contains("leave utilization")
                ||
                question.contains("percentage of leave used")
                ||
                question.contains("how much leave have i used")
                ||
                question.contains("leave used percentage")
        ) {

            LeaveBalance balance =
                    leaveService.getEmployeeBalance(employeeId);

            BigDecimal annual =
                    balance.getAnnualBalance() != null
                            ? balance.getAnnualBalance()
                            : BigDecimal.ZERO;

            BigDecimal used =
                    balance.getUsedLeave() != null
                            ? balance.getUsedLeave()
                            : BigDecimal.ZERO;

            BigDecimal percentage = BigDecimal.ZERO;

            if (annual.compareTo(BigDecimal.ZERO) > 0) {
                percentage = used
                        .multiply(BigDecimal.valueOf(100))
                        .divide(annual, 2, RoundingMode.HALF_UP);
            }

            String answer =
                    "You have used "
                            + used.stripTrailingZeros().toPlainString()
                            + " out of "
                            + annual.stripTrailingZeros().toPlainString()
                            + " annual leave days ("
                            + percentage.stripTrailingZeros().toPlainString()
                            + "% used).";

            return build(
                    "LEAVE_USAGE_PERCENTAGE",
                    answer,
                    1,
                    List.of(
                            EmployeeAiItem.builder()
                                    .totalDays(used)
                                    .status("USED")
                                    .reason(
                                            "Annual entitlement: "
                                                    + annual
                                                    + ", Usage: "
                                                    + percentage
                                                    + "%"
                                    )
                                    .build()
                    )
            );
        }
        
        /*
         * =====================================================
         * LAST LEAVE TAKEN
         * =====================================================
         */

        if (
                question.contains("last leave taken")
                ||
                question.contains("last leave i took")
                ||
                question.contains("when did i last take leave")
                ||
                question.contains("most recent leave")
        ) {

            LocalDate today = LocalDate.now();

            List<LeaveResponseDto> requests =
                    leaveService.getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(r.getStatus())
                            )
                            .filter(r ->
                                    r.getEndDate() != null
                                            && !r.getEndDate().isAfter(today)
                            )
                            .sorted(
                                    (a, b) ->
                                            b.getEndDate().compareTo(a.getEndDate())
                            )
                            .toList();

            if (requests.isEmpty()) {
                return build(
                        "LAST_LEAVE_TAKEN",
                        "You don't have any completed approved leave recorded yet.",
                        0,
                        List.of()
                );
            }

            LeaveResponseDto lastLeave = requests.get(0);

            String answer =
                    "Your most recent completed approved leave was "
                            + lastLeave.getLeaveType()
                            + ", from "
                            + lastLeave.getStartDate()
                            + " to "
                            + lastLeave.getEndDate()
                            + " ("
                            + lastLeave.getTotalDays()
                            + " days).";

            return build(
                    "LAST_LEAVE_TAKEN",
                    answer,
                    1,
                    List.of(convert(lastLeave))
            );
        }
        
        /*
         * =====================================================
         * PERSONAL LEAVE SUGGESTION
         * =====================================================
         */

        if (
                question.contains("leave suggestion")
                ||
                question.contains("leave advice")
                ||
                question.contains("leave recommendation")
                ||
                question.contains("manage my leave")
        ) {

            LeaveBalance balance =
                    leaveService.getEmployeeBalance(employeeId);

            BigDecimal annual =
                    balance.getAnnualBalance() != null
                            ? balance.getAnnualBalance()
                            : BigDecimal.ZERO;

            BigDecimal used =
                    balance.getUsedLeave() != null
                            ? balance.getUsedLeave()
                            : BigDecimal.ZERO;

            BigDecimal remaining =
                    balance.getRemainingLeave() != null
                            ? balance.getRemainingLeave()
                            : BigDecimal.ZERO;

            String answer;

            if (annual.compareTo(BigDecimal.ZERO) <= 0) {
                answer =
                        "Your annual leave entitlement is currently zero or unavailable. "
                                + "Please check your leave policy with HR or your administrator.";
            } else {
                BigDecimal usagePercentage =
                        used.multiply(BigDecimal.valueOf(100))
                                .divide(annual, 2, RoundingMode.HALF_UP);

                if (usagePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                    answer =
                            "You have used "
                                    + usagePercentage.stripTrailingZeros().toPlainString()
                                    + "% of your annual leave entitlement. "
                                    + "Review your remaining balance of "
                                    + remaining.stripTrailingZeros().toPlainString()
                                    + " days before planning additional leave.";
                } else if (usagePercentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
                    answer =
                            "You have used "
                                    + usagePercentage.stripTrailingZeros().toPlainString()
                                    + "% of your annual leave entitlement and have "
                                    + remaining.stripTrailingZeros().toPlainString()
                                    + " days remaining. "
                                    + "Consider planning future leave around your work commitments.";
                } else {
                    answer =
                            "You have used "
                                    + usagePercentage.stripTrailingZeros().toPlainString()
                                    + "% of your annual leave entitlement and have "
                                    + remaining.stripTrailingZeros().toPlainString()
                                    + " days remaining. "
                                    + "You may want to plan breaks in advance while considering your work schedule.";
                }
            }

            return build(
                    "PERSONAL_LEAVE_SUGGESTION",
                    answer,
                    1,
                    List.of(
                            EmployeeAiItem.builder()
                                    .totalDays(remaining)
                                    .status("REMAINING")
                                    .reason("Annual entitlement: " + annual
                                            + ", Used: " + used)
                                    .build()
                    )
            );
        }


        /*
         * =====================================================
         * PENDING LEAVES
         * =====================================================
         */

        if (
                question.contains("pending leave")
                ||
                question.contains("pending leaves")
                ||
                question.contains("pending request")
                ||
                question.contains("pending requests")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "PENDING".equals(
                                            r.getStatus()
                                    )
                            )
                            .toList();

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();
            
            BigDecimal pendingDays =
                    requests.stream()
                            .map(r ->
                                    r.getTotalDays() != null
                                            ? r.getTotalDays()
                                            : BigDecimal.ZERO
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            String answer =
                    requests.isEmpty()
                            ?
                            "You currently have no pending leave requests."
                            :
                            "You have "
                            + requests.size()
                            + " pending leave request"
                            + (requests.size() == 1 ? "" : "s")
                            + ", totalling "
                            + pendingDays.stripTrailingZeros().toPlainString()
                            + " requested leave days.";

            return build(
                    "MY_PENDING_LEAVES",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * APPROVED LEAVES
         * =====================================================
         */

        if (
                question.contains("approved leave")
                ||
                question.contains("approved leaves")
                ||
                question.contains("approved requests")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .toList();

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    requests.isEmpty()
                            ?
                            "You don't have any approved leave requests."
                            :
                            "You have "
                            + requests.size()
                            + " approved leave request"
                            + (
                                requests.size() == 1
                                ? "."
                                : "s."
                            );

            return build(
                    "MY_APPROVED_LEAVES",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * REJECTED LEAVES
         * =====================================================
         */

        if (
                question.contains("rejected leave")
                ||
                question.contains("rejected leaves")
                ||
                question.contains("rejected requests")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "REJECTED".equals(
                                            r.getStatus()
                                    )
                            )
                            .toList();

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    requests.isEmpty()
                            ?
                            "You don't have any rejected leave requests."
                            :
                            "You have "
                            + requests.size()
                            + " rejected leave request"
                            + (
                                requests.size() == 1
                                ? "."
                                : "s."
                            );

            return build(
                    "MY_REJECTED_LEAVES",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * LEAVE HISTORY
         * =====================================================
         */

        if (
                question.contains("leave history")
                ||
                question.contains("leave records")
                ||
                question.contains("my leaves")
                ||
                question.contains("my leave requests")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId);

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    requests.isEmpty()
                            ?
                            "You don't have any leave history."
                            :
                            "You have "
                            + requests.size()
                            + " leave request"
                            + (
                                requests.size() == 1
                                ? "."
                                : "s."
                            )
                            + " in your leave history.";

            return build(
                    "MY_LEAVE_HISTORY",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * UPCOMING LEAVE
         * =====================================================
         */

        if (
                question.contains("upcoming leave")
                ||
                question.contains("next leave")
                ||
                question.contains("future leave")
        ) {

            LocalDate today =
                    LocalDate.now();

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .filter(r ->
                                    r.getStartDate() != null
                                    &&
                                    !r.getStartDate()
                                            .isBefore(today)
                            )
                            .sorted(
                                (a, b) ->
                                    a.getStartDate()
                                     .compareTo(
                                        b.getStartDate()
                                     )
                            )
                            .toList();

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer;

            if (requests.isEmpty()) {

                answer =
                        "You don't have any upcoming approved leave.";

            } else {

                LeaveResponseDto next =
                        requests.get(0);

                answer =
                        "Your next approved leave is from "
                        + next.getStartDate()
                        + " to "
                        + next.getEndDate()
                        + " ("
                        + next.getTotalDays()
                        + " days).";
            }

            return build(
                    "UPCOMING_LEAVE",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * LEAVE TOMORROW
         * =====================================================
         */

        if (
                question.contains("tomorrow")
                &&
                (
                    question.contains("leave")
                    ||
                    question.contains("on leave")
                )
        ) {

            LocalDate tomorrow =
                    LocalDate.now().plusDays(1);

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .filter(r ->
                                    r.getStartDate() != null
                                    &&
                                    r.getEndDate() != null
                                    &&
                                    !tomorrow.isBefore(
                                            r.getStartDate()
                                    )
                                    &&
                                    !tomorrow.isAfter(
                                            r.getEndDate()
                                    )
                            )
                            .toList();

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    requests.isEmpty()
                            ?
                            "You are not on approved leave tomorrow."
                            :
                            "Yes, you are on approved leave tomorrow.";

            return build(
                    "MY_LEAVE_TOMORROW",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * THIS YEAR LEAVE DAYS
         * =====================================================
         */

        if (
                question.contains("how many leaves did i take")
                ||
                question.contains("leave days this year")
                ||
                question.contains("leaves this year")
                ||
                question.contains("used leave this year")
        ) {

            int year =
                    LocalDate.now().getYear();

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId)
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .filter(r ->
                                    r.getStartDate() != null
                                    &&
                                    r.getStartDate()
                                            .getYear() == year
                            )
                            .toList();

            BigDecimal totalDays =
                    requests
                            .stream()
                            .map(r -> r.getTotalDays() != null
                                    ? r.getTotalDays()
                                    : BigDecimal.ZERO)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    "You have taken "
                    + totalDays
                    + " approved leave day"
                    + (
                        totalDays.compareTo(BigDecimal.ONE) == 0
                        ? ""
                        : "s"
                    )
                    + " this year.";

            return build(
                    "LEAVE_DAYS_THIS_YEAR",
                    answer,
                    requests.size(),
                    items
            );
        }


        /*
         * =====================================================
         * LEAVE SUMMARY
         * =====================================================
         */

        if (
                question.contains("leave summary")
                ||
                question.contains("my summary")
                ||
                question.contains("overall leave")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getMyRequests(employeeId);

            long pending =
                    requests
                            .stream()
                            .filter(r ->
                                    "PENDING".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            long approved =
                    requests
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            long rejected =
                    requests
                            .stream()
                            .filter(r ->
                                    "REJECTED".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            BigDecimal approvedDays =
                    requests
                            .stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .map(r ->
                                    r.getTotalDays() != null
                                            ? r.getTotalDays()
                                            : BigDecimal.ZERO
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            String answer =
                    "Your leave summary: "
                    + requests.size()
                    + " total requests, "
                    + approved
                    + " approved, "
                    + pending
                    + " pending and "
                    + rejected
                    + " rejected. "
                    + approvedDays
                    + " approved leave days.";

            List<EmployeeAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();

            return build(
                    "MY_LEAVE_SUMMARY",
                    answer,
                    requests.size(),
                    items
            );
        }


        /*
         * =====================================================
         * DEFAULT
         * =====================================================
         */

        return build(
                "UNKNOWN",
                "I can help you with your leave balance, "
                + "pending leaves, approved leaves, rejected leaves, "
                + "leave history, upcoming leave and leave summary.",
                0,
                List.of()
        );
    }


    /*
     * =========================================================
     * CONVERT LEAVE RESPONSE
     * =========================================================
     */

    private EmployeeAiItem convert(
            LeaveResponseDto dto) {

        return EmployeeAiItem.builder()

                .requestId(
                        dto.getId()
                )

                .leaveType(
                        dto.getLeaveType()
                )

                .startDate(
                        dto.getStartDate() != null
                                ? dto.getStartDate().toString()
                                : null
                )

                .endDate(
                        dto.getEndDate() != null
                                ? dto.getEndDate().toString()
                                : null
                )

                .totalDays(
                        dto.getTotalDays()
                )

                .status(
                        dto.getStatus()
                )

                .reason(
                        dto.getReason()
                )

                .documentStatus(
                        dto.getDocumentStatus() != null
                                ? dto.getDocumentStatus().name()
                                : null
                )

                .documentRequested(
                        dto.getDocumentRequested()
                )

                .build();
    }


    /*
     * =========================================================
     * RESPONSE BUILDER
     * =========================================================
     */

    private EmployeeAiResponse build(
            String intent,
            String answer,
            int count,
            List<EmployeeAiItem> items) {

        return EmployeeAiResponse.builder()

                .intent(intent)

                .answer(answer)

                .count(count)

                .items(items)

                .build();
    }

}