package com.boit.boit_leave.AI.service;

import com.boit.boit_leave.AI.dto.ManagerAiItem;
import com.boit.boit_leave.AI.dto.ManagerAiResponse;
import com.boit.boit_leave.dto.DocumentRequestDto;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.service.LeaveDocumentRequestService;
import com.boit.boit_leave.service.LeaveService;
import java.math.BigDecimal;
import com.boit.boit_leave.entity.LeaveBalance;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ManagerAiService {

    private final LeaveService leaveService;

    private final LeaveDocumentRequestService
            leaveDocumentRequestService;


    public ManagerAiResponse processQuestion(
            Long managerId,
            String prompt) {

        if (prompt == null ||
                prompt.trim().isEmpty()) {

            return ManagerAiResponse.builder()

                    .intent("UNKNOWN")

                    .answer(
                        "Please enter a question."
                    )

                    .count(0)

                    .items(List.of())

                    .build();
        }


        String question =
                prompt
                        .trim()
                        .toLowerCase(Locale.ROOT);


/*
 * =====================================================
 * PERSONAL LEAVE BALANCE AND USAGE
 * =====================================================
 */

if (
        question.contains("my leave balance")
        || question.contains("my remaining leave")
        || question.contains("my leave usage percentage")
        || question.contains("my leave utilization")
        || question.contains("percentage of leave i used")
        || question.contains("how much leave have i used")
) {

    LeaveBalance balance =
            leaveService.getEmployeeBalance(managerId);

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

    BigDecimal percentage = BigDecimal.ZERO;

    if (annual.compareTo(BigDecimal.ZERO) > 0) {
        percentage = used
                .multiply(BigDecimal.valueOf(100))
                .divide(annual, 2, RoundingMode.HALF_UP);
    }

    String answer =
            "Your personal leave balance is "
                    + remaining.stripTrailingZeros().toPlainString()
                    + " days remaining out of "
                    + annual.stripTrailingZeros().toPlainString()
                    + " annual leave days. You have used "
                    + used.stripTrailingZeros().toPlainString()
                    + " days ("
                    + percentage.stripTrailingZeros().toPlainString()
                    + "% used).";

    return ManagerAiResponse.builder()
            .intent("MY_LEAVE_BALANCE")
            .answer(answer)
            .count(1)
            .items(List.of())
            .build();
}



/*
 * =====================================================
 * PERSONAL LAST LEAVE TAKEN
 * =====================================================
 */

if (
        question.contains("my last leave")
        || question.contains("last leave i took")
        || question.contains("when did i last take leave")
        || question.contains("my most recent leave")
) {

    LocalDate today = LocalDate.now();

    List<LeaveResponseDto> requests =
            leaveService.getMyRequests(managerId)
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
        return ManagerAiResponse.builder()
                .intent("MY_LAST_LEAVE")
                .answer(
                        "You don't have any completed approved leave recorded yet."
                )
                .count(0)
                .items(List.of())
                .build();
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

    return ManagerAiResponse.builder()
            .intent("MY_LAST_LEAVE")
            .answer(answer)
            .count(1)
            .items(List.of(convert(lastLeave)))
            .build();
}


/*
 * =====================================================
 * PERSONAL UPCOMING APPROVED LEAVE
 * =====================================================
 */

if (
        question.contains("my upcoming leave")
        || question.contains("my next leave")
        || question.contains("my future leave")
        || question.contains("when is my next leave")
) {

    LocalDate today = LocalDate.now();

    List<LeaveResponseDto> requests =
            leaveService.getMyRequests(managerId)
                    .stream()
                    .filter(r ->
                            "APPROVED".equals(r.getStatus())
                    )
                    .filter(r ->
                            r.getStartDate() != null
                                    && !r.getStartDate().isBefore(today)
                    )
                    .sorted(
                            Comparator.comparing(
                                    LeaveResponseDto::getStartDate
                            )
                    )
                    .toList();

    if (requests.isEmpty()) {
        return ManagerAiResponse.builder()
                .intent("MY_UPCOMING_LEAVE")
                .answer(
                        "You don't have any upcoming approved leave."
                )
                .count(0)
                .items(List.of())
                .build();
    }

    LeaveResponseDto nextLeave = requests.get(0);

    String answer =
            "Your next approved leave is from "
                    + nextLeave.getStartDate()
                    + " to "
                    + nextLeave.getEndDate()
                    + " ("
                    + nextLeave.getTotalDays()
                    + " days).";

    return ManagerAiResponse.builder()
            .intent("MY_UPCOMING_LEAVE")
            .answer(answer)
            .count(1)
            .items(List.of(convert(nextLeave)))
            .build();
}



/*
 * =====================================================
 * PERSONAL LEAVE TAKEN THIS YEAR
 * =====================================================
 */

if (
        question.contains("my leave this year")
        || question.contains("my leaves this year")
        || question.contains("my leave days this year")
        || question.contains("how much leave did i take this year")
        || question.contains("my used leave this year")
) {

    int year = LocalDate.now().getYear();

    List<LeaveResponseDto> requests =
            leaveService.getMyRequests(managerId)
                    .stream()
                    .filter(r ->
                            "APPROVED".equals(r.getStatus())
                    )
                    .filter(r ->
                            r.getStartDate() != null
                                    && r.getEndDate() != null
                                    && r.getStartDate().getYear() == year
                    )
                    .toList();

    BigDecimal totalDays =
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

    List<ManagerAiItem> items =
            requests.stream()
                    .map(this::convert)
                    .toList();

    String answer =
            "You have taken "
                    + totalDays.stripTrailingZeros().toPlainString()
                    + " approved leave days this year.";

    return ManagerAiResponse.builder()
            .intent("MY_LEAVE_DAYS_THIS_YEAR")
            .answer(answer)
            .count(requests.size())
            .items(items)
            .build();
}



/*
 * =====================================================
 * PERSONAL PENDING LEAVE REQUESTS
 * =====================================================
 */

if (
        question.contains("my pending leave")
        || question.contains("my pending leaves")
        || question.contains("my pending request")
        || question.contains("my pending requests")
        || question.contains("how many leave requests am i waiting for")
) {

    List<LeaveResponseDto> requests =
            leaveService.getMyRequests(managerId)
                    .stream()
                    .filter(r ->
                            "PENDING".equals(r.getStatus())
                    )
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

    List<ManagerAiItem> items =
            requests.stream()
                    .map(this::convert)
                    .toList();

    String answer =
            requests.isEmpty()
                    ? "You currently have no pending personal leave requests."
                    : "You have "
                            + requests.size()
                            + " pending personal leave request"
                            + (requests.size() == 1 ? "" : "s")
                            + ", totalling "
                            + pendingDays.stripTrailingZeros().toPlainString()
                            + " requested leave days.";

    return ManagerAiResponse.builder()
            .intent("MY_PENDING_LEAVE_REQUESTS")
            .answer(answer)
            .count(requests.size())
            .items(items)
            .build();
}


        /*
         * =====================================================
         * 1. PENDING APPROVALS
         * =====================================================
         */

        if (
                question.contains("pending")
                ||
                question.contains("approval")
                ||
                question.contains("approvals")
                ||
                question.contains("waiting")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getManagerPendingRequests(
                                    managerId
                            );


            List<ManagerAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();


            String answer;

            if (items.isEmpty()) {

                answer =
                        "You currently have no pending leave approvals.";

            } else {

                answer =
                        "You have "
                        + items.size()
                        + " pending leave approval"
                        + (
                            items.size() == 1
                            ? "."
                            : "s."
                          );
            }


            return ManagerAiResponse.builder()

                    .intent("PENDING_APPROVALS")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 2. TODAY'S LEAVE
         * =====================================================
         */

        if (
                question.contains("today")
                &&
                question.contains("leave")
        ) {

            LocalDate today =
                    LocalDate.now();


            List<LeaveResponseDto> requests =
                    getTeamRequests(managerId)
                            .stream()

                            .filter(request ->
                                    "APPROVED".equals(
                                            request.getStatus()
                                    )
                            )

                            .filter(request ->
                                    isDateInsideLeave(
                                            today,
                                            request
                                    )
                            )

                            .toList();


            List<ManagerAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();


            String answer;

            if (items.isEmpty()) {

                answer =
                        "No employees from your team are on approved leave today.";

            } else {

                answer =
                        items.size()
                        + " employee"
                        + (
                            items.size() == 1
                            ? " is"
                            : "s are"
                          )
                        + " on approved leave today.";
            }


            return ManagerAiResponse.builder()

                    .intent("TEAM_LEAVE_TODAY")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 3. MOST LEAVE THIS MONTH
         *
         * Example:
         * Who has the most leave this month?
         * =====================================================
         */

        if (
                question.contains("most leave")
                ||
                question.contains("maximum leave")
                ||
                question.contains("highest leave")
                ||
                question.contains("most leaves")
        ) {

            YearMonth currentMonth =
                    YearMonth.now();


            List<LeaveResponseDto> requests =
                    getTeamRequests(managerId)
                            .stream()

                            .filter(request ->
                                    "APPROVED".equals(
                                            request.getStatus()
                                    )
                            )

                            .filter(request ->
                                    overlapsMonth(
                                            request,
                                            currentMonth
                                    )
                            )

                            .toList();


            /*
             * Calculate leave days per employee.
             */

            Map<String, BigDecimal> employeeDays =
                    new HashMap<>();

            Map<String, LeaveResponseDto> employeeRequest =
                    new HashMap<>();


            for (LeaveResponseDto request : requests) {

            	BigDecimal days =
            	        BigDecimal.valueOf(
            	                calculateDaysInMonth(
            	                        request,
            	                        currentMonth
            	                )
            	        );


                String employee =
                        request.getEmployeeName();


                employeeDays.merge(
                        employee,
                        days,
                        BigDecimal::add
                );


                employeeRequest.put(
                        employee,
                        request
                );
            }


            if (employeeDays.isEmpty()) {

                return ManagerAiResponse.builder()

                        .intent("MOST_LEAVE_MONTH")

                        .answer(
                            "No approved leave has been recorded for your team this month."
                        )

                        .count(0)

                        .items(List.of())

                        .build();
            }


            BigDecimal maximum =
                    employeeDays.values()
                            .stream()
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);


            List<ManagerAiItem> items =
                    employeeDays.entrySet()
                            .stream()

                            .filter(entry ->
                            entry.getValue().compareTo(maximum) == 0
                    )
                            .map(entry -> {

                                LeaveResponseDto source =
                                        employeeRequest
                                                .get(entry.getKey());

                                return ManagerAiItem.builder()

                                        .requestId(
                                                source.getId()
                                        )

                                        .employeeName(
                                                entry.getKey()
                                        )

                                        .leaveType(
                                                source.getLeaveType()
                                        )

                                        .startDate(
                                                source.getStartDate()
                                                        != null
                                                ? source.getStartDate()
                                                        .toString()
                                                : null
                                        )

                                        .endDate(
                                                source.getEndDate()
                                                        != null
                                                ? source.getEndDate()
                                                        .toString()
                                                : null
                                        )

                                        .totalDays(
                                                entry.getValue()
                                        )

                                        .status(
                                                "APPROVED"
                                        )

                                        .reason(
                                                "Total approved leave this month: "
                                                + entry.getValue()
                                                + " days"
                                        )

                                        .build();

                            })

                            .toList();


            String answer =
                    items.size() == 1

                    ?

                    items.get(0).getEmployeeName()
                    + " has the most approved leave this month with "
                    + maximum
                    + " days."

                    :

                    "The employees with the most approved leave this month have "
                    + maximum
                    + " days each.";


            return ManagerAiResponse.builder()

                    .intent("MOST_LEAVE_MONTH")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 4. OLD PENDING REQUESTS
         *
         * Example:
         * Which requests are waiting for more than 2 days?
         * =====================================================
         */

        if (
                question.contains("more than 2 days")
                ||
                question.contains("older than 2 days")
                ||
                question.contains("waiting for more")
                ||
                question.contains("old pending")
        ) {

            LocalDate today =
                    LocalDate.now();


            List<LeaveResponseDto> requests =
                    leaveService
                            .getManagerPendingRequests(
                                    managerId
                            )
                            .stream()

                            .filter(request ->
                                    request.getCreatedAt()
                                            != null
                            )

                            .filter(request -> {

                                LocalDate createdDate =
                                        request
                                            .getCreatedAt()
                                            .toLocalDate();

                                long days =
                                        ChronoUnit.DAYS.between(
                                                createdDate,
                                                today
                                        );

                                return days > 2;

                            })

                            .toList();


            List<ManagerAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();


            String answer;

            if (items.isEmpty()) {

                answer =
                        "There are no pending leave requests waiting for more than 2 days.";

            } else {

                answer =
                        items.size()
                        + " pending request"
                        + (items.size() == 1 ? " has" : "s have")
                        + " been waiting for more than 2 days.";
            }


            return ManagerAiResponse.builder()

                    .intent("OLD_PENDING_REQUESTS")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 5. MEDICAL LEAVE
         *
         * Example:
         * Show medical leave requests
         * =====================================================
         */

        if (
                question.contains("medical leave")
                ||
                question.contains("medical")
                ||
                question.contains("sick leave")
        ) {

            List<LeaveResponseDto> requests =
                    getTeamRequests(managerId)
                            .stream()

                            .filter(request ->
                                    request.getLeaveType()
                                            != null
                            )

                            .filter(request ->
                                    request.getLeaveType()
                                            .toString()
                                            .equalsIgnoreCase(
                                                    "MEDICAL"
                                            )
                            )

                            .toList();


            List<ManagerAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();


            String answer;

            if (items.isEmpty()) {

                answer =
                        "There are no medical leave requests from your team.";

            } else {

                answer =
                        "There are "
                        + items.size()
                        + " medical leave request"
                        + (items.size() == 1 ? "." : "s.");
            }


            return ManagerAiResponse.builder()

                    .intent("MEDICAL_LEAVE")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 6. OVERLAPPING LEAVES
         *
         * Example:
         * Are there any overlapping leaves?
         * =====================================================
         */

        if (
                question.contains("overlapping")
                ||
                question.contains("overlap")
        ) {

            List<LeaveResponseDto> approved =
                    getTeamRequests(managerId)
                            .stream()

                            .filter(request ->
                                    "APPROVED".equals(
                                            request.getStatus()
                                    )
                            )

                            .filter(request ->
                                    request.getStartDate()
                                            != null
                                    &&
                                    request.getEndDate()
                                            != null
                            )

                            .toList();


            List<ManagerAiItem> overlapping =
                    new ArrayList<>();


            Set<String> alreadyAdded =
                    new HashSet<>();


            for (int i = 0;
                 i < approved.size();
                 i++) {

                LeaveResponseDto first =
                        approved.get(i);


                for (int j = i + 1;
                     j < approved.size();
                     j++) {

                    LeaveResponseDto second =
                            approved.get(j);


                    if (
                            first.getEmployeeName()
                                    .equalsIgnoreCase(
                                            second.getEmployeeName()
                                    )
                    ) {
                        continue;
                    }


                    boolean overlaps =
                            !first.getEndDate()
                                    .isBefore(
                                            second.getStartDate()
                                    )
                            &&
                            !second.getEndDate()
                                    .isBefore(
                                            first.getStartDate()
                                    );


                    if (overlaps) {

                        String key =
                                first.getId()
                                + "-"
                                + second.getId();


                        if (
                                alreadyAdded.add(key)
                        ) {

                            overlapping.add(
                                    convert(first)
                            );

                            overlapping.add(
                                    convert(second)
                            );
                        }
                    }
                }
            }


            String answer;

            if (overlapping.isEmpty()) {

                answer =
                        "There are no overlapping approved leaves in your team.";

            } else {

                answer =
                        "I found "
                        + overlapping.size()
                        + " leave requests involved in overlapping leave periods.";
            }


            return ManagerAiResponse.builder()

                    .intent("OVERLAPPING_LEAVES")

                    .answer(answer)

                    .count(overlapping.size())

                    .items(overlapping)

                    .build();
        }


        /*
         * =====================================================
         * 7. PENDING DOCUMENTS
         *
         * Example:
         * Which employees have pending documents?
         * =====================================================
         */

        if (
                question.contains("pending document")
                ||
                question.contains("pending documents")
                ||
                question.contains("documents pending")
        ) {

            List<LeaveResponseDto> teamRequests =
                    getTeamRequests(managerId);


            List<ManagerAiItem> items =
                    new ArrayList<>();


            Set<Long> addedLeaveRequests =
                    new HashSet<>();


            for (LeaveResponseDto leave :
                    teamRequests) {

                if (leave.getId() == null) {
                    continue;
                }


                List<DocumentRequestDto>
                        documentRequests =
                        leaveDocumentRequestService
                                .getPendingRequests(
                                        leave.getId()
                                );


                if (
                        !documentRequests.isEmpty()
                        &&
                        addedLeaveRequests.add(
                                leave.getId()
                        )
                ) {

                    ManagerAiItem item =
                            convert(leave);


                    String documentNames =
                            documentRequests
                                    .stream()

                                    .map(request ->
                                            request.getDocumentType()
                                                    != null
                                            ?
                                            request.getDocumentType()
                                                    .toString()
                                                    .replace(
                                                        "_",
                                                        " "
                                                    )
                                            :
                                            "Document"
                                    )

                                    .distinct()

                                    .reduce(
                                        (a, b) ->
                                            a + ", " + b
                                    )

                                    .orElse(
                                        "Document"
                                    );


                    item.setReason(
                            "Pending documents: "
                            + documentNames
                    );


                    items.add(item);
                }
            }


            String answer;

            if (items.isEmpty()) {

                answer =
                        "There are no employees with pending document requests.";

            } else {

                answer =
                        items.size()
                        + " employee"
                        + (items.size() == 1
                           ? " has"
                           : "s have")
                        + " pending documents.";
            }


            return ManagerAiResponse.builder()

                    .intent("PENDING_DOCUMENTS")

                    .answer(answer)

                    .count(items.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * 8. MONTHLY LEAVE SUMMARY
         *
         * Example:
         * Give me this month's leave summary
         * =====================================================
         */

        if (
                question.contains("this month")
                ||
                question.contains("monthly leave summary")
                ||
                question.contains("month summary")
        ) {

            YearMonth currentMonth =
                    YearMonth.now();


            List<LeaveResponseDto> requests =
                    getTeamRequests(managerId)
                            .stream()

                            .filter(request ->
                                    overlapsMonth(
                                            request,
                                            currentMonth
                                    )
                            )

                            .toList();


            long pending =
                    requests
                            .stream()
                            .filter(request ->
                                    "PENDING".equals(
                                            request.getStatus()
                                    )
                            )
                            .count();


            long approved =
                    requests
                            .stream()
                            .filter(request ->
                                    "APPROVED".equals(
                                            request.getStatus()
                                    )
                            )
                            .count();


            long rejected =
                    requests
                            .stream()
                            .filter(request ->
                                    "REJECTED".equals(
                                            request.getStatus()
                                    )
                            )
                            .count();


            List<ManagerAiItem> items =
                    requests
                            .stream()
                            .map(this::convert)
                            .toList();


            String answer =
                    "This month your team has "
                    + requests.size()
                    + " leave request"
                    + (requests.size() == 1 ? "" : "s")
                    + ": "
                    + pending
                    + " pending, "
                    + approved
                    + " approved and "
                    + rejected
                    + " rejected.";


            return ManagerAiResponse.builder()

                    .intent("MONTHLY_LEAVE_SUMMARY")

                    .answer(answer)

                    .count(requests.size())

                    .items(items)

                    .build();
        }


        /*
         * =====================================================
         * DEFAULT
         * =====================================================
         */

        return ManagerAiResponse.builder()

                .intent("UNKNOWN")

                .answer(
                    "I can help you with pending approvals, "
                    + "employees on leave today, "
                    + "most leave this month, "
                    + "old pending requests, "
                    + "medical leave, "
                    + "overlapping leaves, "
                    + "pending documents, "
                    + "and this month's leave summary."
                )

                .count(0)

                .items(List.of())

                .build();
    }


    /*
     * =========================================================
     * HELPER: GET TEAM REQUESTS
     * =========================================================
     */

    private List<LeaveResponseDto> getTeamRequests(
            Long managerId) {

        return leaveService
                .getManagerTeamRequests(
                        managerId
                );
    }


    /*
     * =========================================================
     * HELPER: DATE INSIDE LEAVE
     * =========================================================
     */

    private boolean isDateInsideLeave(
            LocalDate date,
            LeaveResponseDto request) {

        return
                request.getStartDate() != null
                &&
                request.getEndDate() != null
                &&
                !date.isBefore(
                        request.getStartDate()
                )
                &&
                !date.isAfter(
                        request.getEndDate()
                );
    }


    /*
     * =========================================================
     * HELPER: MONTH OVERLAP
     * =========================================================
     */

    private boolean overlapsMonth(
            LeaveResponseDto request,
            YearMonth month) {

        if (
                request.getStartDate() == null
                ||
                request.getEndDate() == null
        ) {
            return false;
        }


        LocalDate monthStart =
                month.atDay(1);

        LocalDate monthEnd =
                month.atEndOfMonth();


        return
                !request.getEndDate()
                        .isBefore(monthStart)

                &&

                !request.getStartDate()
                        .isAfter(monthEnd);
    }


    /*
     * =========================================================
     * HELPER: CALCULATE DAYS WITHIN MONTH
     * =========================================================
     */

    private int calculateDaysInMonth(
            LeaveResponseDto request,
            YearMonth month) {

        if (
                request.getStartDate() == null
                ||
                request.getEndDate() == null
        ) {
            return 0;
        }


        LocalDate start =
                request.getStartDate()
                        .isBefore(
                                month.atDay(1)
                        )
                ?
                month.atDay(1)
                :
                request.getStartDate();


        LocalDate end =
                request.getEndDate()
                        .isAfter(
                                month.atEndOfMonth()
                        )
                ?
                month.atEndOfMonth()
                :
                request.getEndDate();


        if (start.isAfter(end)) {
            return 0;
        }


        return (int)
                ChronoUnit.DAYS.between(
                        start,
                        end
                ) + 1;
    }


    /*
     * =========================================================
     * CONVERT LEAVE DTO -> AI ITEM
     * =========================================================
     */

    private ManagerAiItem convert(
            LeaveResponseDto dto) {

        return ManagerAiItem.builder()

                .requestId(
                        dto.getId()
                )

                .employeeName(
                        dto.getEmployeeName()
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

                .build();
    }
}