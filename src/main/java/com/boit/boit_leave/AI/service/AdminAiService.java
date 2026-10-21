package com.boit.boit_leave.AI.service;

import com.boit.boit_leave.AI.dto.AdminAiItem;
import com.boit.boit_leave.AI.dto.AdminAiResponse;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.dto.AdminDashboardDto;
import com.boit.boit_leave.dto.CountryEmployeeCountDto;
import com.boit.boit_leave.dto.EmployeeResponseDto;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.service.LeaveService;
import com.boit.boit_leave.service.UserService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.boit.boit_leave.entity.SlaStatus;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.entity.AppraisalCycle;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.repository.AppraisalCycleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAiService {

	private final LeaveService leaveService;

	private final UserService userService;

	private final UserRepository userRepository;
	
	private final EmployeeAppraisalRepository employeeAppraisalRepository;

	private final AppraisalCycleRepository appraisalCycleRepository;
	
    public AdminAiResponse processQuestion(String prompt) {

        if (prompt == null || prompt.trim().isEmpty()) {

            return AdminAiResponse.builder()
                    .intent("UNKNOWN")
                    .answer("Please enter a question.")
                    .count(0)
                    .items(List.of())
                    .build();
        }

        String question =
                prompt.trim().toLowerCase(Locale.ROOT);
        
        /*
         * =====================================================
         * APPRAISAL TRACKING
         * =====================================================
         */
        if (question.contains("appraisal") || question.contains("appraisals")) {

        	List<EmployeeAppraisal> allAppraisals =
        	        employeeAppraisalRepository.findAllWithCycle();

            List<User> allUsers = userRepository.findAll();

            Map<Long, User> usersById = allUsers.stream()
                    .filter(user -> user.getId() != null)
                    .collect(Collectors.toMap(
                            User::getId,
                            user -> user,
                            (first, second) -> first
                    ));

            /*
             * UPCOMING APPRAISAL CYCLES
             */
            if (question.contains("upcoming")
                    || question.contains("next appraisal cycle")) {

                LocalDate today = LocalDate.now();

                List<AppraisalCycle> cycles =
                        appraisalCycleRepository.findAllByOrderByAppraisalStartDateDesc()
                                .stream()
                                .filter(cycle ->
                                        cycle.getAppraisalStartDate() != null
                                        && !cycle.getAppraisalStartDate().isBefore(today)
                                )
                                .filter(cycle ->
                                        cycle.getStatus() != AppraisalCycleStatus.CLOSED
                                        && cycle.getStatus() != AppraisalCycleStatus.COMPLETED
                                )
                                .sorted(Comparator.comparing(
                                        AppraisalCycle::getAppraisalStartDate
                                ))
                                .toList();

                List<AdminAiItem> items = cycles.stream()
                        .map(cycle -> AdminAiItem.builder()
                                .employeeName(cycle.getName())
                                .status(cycle.getStatus().name())
                                .holidayDate(
                                        cycle.getAppraisalStartDate().toString()
                                )
                                .endDate(
                                        cycle.getAppraisalEndDate() != null
                                                ? cycle.getAppraisalEndDate().toString()
                                                : null
                                )
                                .reason(cycle.getDescription())
                                .build())
                        .toList();

                return build(
                        "UPCOMING_APPRAISAL_CYCLES",
                        cycles.isEmpty()
                                ? "No upcoming appraisal cycles were found."
                                : cycles.size()
                                        + " upcoming appraisal cycle(s) found.",
                        items.size(),
                        items
                );
            }

            /*
             * FIND APPRAISALS FOR A SPECIFIC EMPLOYEE
             */
            User matchedEmployee = allUsers.stream()
                    .filter(user -> user.getFullName() != null)
                    .filter(user -> question.contains(
                            user.getFullName().toLowerCase(Locale.ROOT)
                    ))
                    .findFirst()
                    .orElse(null);

            List<EmployeeAppraisal> matchingAppraisals = allAppraisals.stream()
                    .filter(appraisal -> {
                        if (matchedEmployee == null) {
                            return true;
                        }

                        return matchedEmployee.getId().equals(
                                appraisal.getEmployeeId()
                        );
                    })
                    .toList();

            /*
             * FILTER COMPLETED APPRAISALS
             */
            if (question.contains("completed")
                    || question.contains("finalized")
                    || question.contains("finalised")) {

                matchingAppraisals = matchingAppraisals.stream()
                        .filter(appraisal ->
                                appraisal.getStatus() == AppraisalStatus.FINALIZED
                        )
                        .toList();
            }

            /*
             * FILTER PENDING / INCOMPLETE APPRAISALS
             */
            else if (question.contains("pending")
                    || question.contains("outstanding")
                    || question.contains("incomplete")) {

                matchingAppraisals = matchingAppraisals.stream()
                        .filter(appraisal ->
                                appraisal.getStatus() != AppraisalStatus.FINALIZED
                                && appraisal.getStatus() != AppraisalStatus.CANCELLED
                        )
                        .toList();
            }

            /*
             * BUILD APPRAISAL RESULTS
             */
            List<AdminAiItem> appraisalItems = matchingAppraisals.stream()
                    .map(appraisal -> {
                        User employee = usersById.get(appraisal.getEmployeeId());

                        return AdminAiItem.builder()
                                .id(appraisal.getId())
                                .employeeName(
                                        employee != null
                                                ? employee.getFullName()
                                                : "Employee ID " + appraisal.getEmployeeId()
                                )
                                .employeeCode(
                                        employee != null
                                                ? employee.getEmployeeCode()
                                                : null
                                )
                                .requestId(appraisal.getId())
                                .status(appraisal.getStatus().name())
                                .reason(
                                        appraisal.getCycle() != null
                                                ? appraisal.getCycle().getName()
                                                : null
                                )
                                .department(
                                        appraisal.getFinalPerformanceCategory()
                                )
                                .totalDays(
                                        appraisal.getFinalRating() != null
                                                ? appraisal.getFinalRating().intValue()
                                                : null
                                )
                                .startDate(
                                        appraisal.getCreatedAt() != null
                                                ? appraisal.getCreatedAt().toLocalDate().toString()
                                                : null
                                )
                                .endDate(
                                        appraisal.getUpdatedAt() != null
                                                ? appraisal.getUpdatedAt().toLocalDate().toString()
                                                : null
                                )
                                .build();
                    })
                    .toList();

            String employeeLabel = matchedEmployee != null
                    ? matchedEmployee.getFullName() + " has "
                    : "There are ";

            String answer;

            if (question.contains("completed")
                    || question.contains("finalized")
                    || question.contains("finalised")) {

                answer = matchingAppraisals.isEmpty()
                        ? "No finalized appraisals were found."
                        : employeeLabel
                                + matchingAppraisals.size()
                                + " finalized appraisal(s).";
            } else if (question.contains("pending")
                    || question.contains("outstanding")
                    || question.contains("incomplete")) {

                answer = matchingAppraisals.isEmpty()
                        ? "No incomplete appraisals were found."
                        : employeeLabel
                                + matchingAppraisals.size()
                                + " incomplete appraisal(s).";
            } else {
                answer = matchingAppraisals.isEmpty()
                        ? "No appraisal records were found."
                        : employeeLabel
                                + matchingAppraisals.size()
                                + " appraisal record(s).";
            }

            return build(
                    "APPRAISAL_TRACKING",
                    answer,
                    appraisalItems.size(),
                    appraisalItems
            );
        }
        
        

/*
 * =====================================================
 * SLA STATUS FOR A SPECIFIC REQUEST
 * =====================================================
 */
if (question.contains("sla")
        && question.matches(".*\\b\\d+\\b.*")
        && (
            question.contains("request")
            || question.contains("leave")
            || question.contains("due")
            || question.contains("breach")
            || question.contains("deadline")
        )) {

    java.util.regex.Matcher matcher =
            java.util.regex.Pattern
                    .compile("\\b\\d+\\b")
                    .matcher(question);

    if (matcher.find()) {
        Long requestId = Long.parseLong(matcher.group());

        List<LeaveResponseDto> requests =
                leaveService.getAllRequestResponses();

        LeaveResponseDto request = requests.stream()
                .filter(r -> r.getId() != null
                        && r.getId().equals(requestId))
                .findFirst()
                .orElse(null);

        if (request == null) {
            return build(
                    "SLA_REQUEST_NOT_FOUND",
                    "I couldn't find leave request "
                            + requestId + ".",
                    0,
                    List.of()
            );
        }

        String status = request.getSlaStatus() != null
                ? request.getSlaStatus().toUpperCase(Locale.ROOT)
                : "UNKNOWN";

        String dueAt = request.getSlaDueAt() != null
                ? request.getSlaDueAt().format(
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm a"
                    )
                )
                : "Not available";

        String answer = switch (status) {
            case "BREACHED" ->
                    "Leave request " + requestId
                    + " has breached its SLA. SLA deadline: "
                    + dueAt + ".";

            case "WARNING" ->
                    "Leave request " + requestId
                    + " is in the SLA warning period. SLA deadline: "
                    + dueAt + ".";

            case "WITHIN_SLA" ->
                    "Leave request " + requestId
                    + " is currently within SLA. SLA deadline: "
                    + dueAt + ".";

            default ->
                    "The SLA status is unavailable for request "
                    + requestId + ". Deadline: " + dueAt + ".";
        };

        return build(
                "SLA_REQUEST_STATUS",
                answer,
                1,
                List.of(convert(request))
        );
    }
}

/*
 * =====================================================
 * REQUESTS FILTERED BY SLA STATUS
 * =====================================================
 */
if (question.contains("sla")
        && (
            question.contains("breached")
            || question.contains("overdue")
            || question.contains("warning")
            || question.contains("approaching")
            || question.contains("within sla")
        )) {

    String requestedStatus =
            question.contains("breached")
                    || question.contains("overdue")
                    ? "BREACHED"
                    : question.contains("warning")
                            || question.contains("approaching")
                            ? "WARNING"
                            : "WITHIN_SLA";

    List<LeaveResponseDto> matchingRequests =
            leaveService.getAllRequestResponses()
                    .stream()
                    .filter(r -> "PENDING".equalsIgnoreCase(
                            r.getStatus()))
                    .filter(r -> r.getSlaStatus() != null
                            && requestedStatus.equalsIgnoreCase(
                                    r.getSlaStatus()))
                    .toList();

    List<AdminAiItem> items = matchingRequests.stream()
            .map(this::convert)
            .toList();

    String answer = matchingRequests.isEmpty()
            ? "There are no pending leave requests with SLA status "
                    + requestedStatus + "."
            : matchingRequests.size()
                    + " pending leave request(s) have SLA status "
                    + requestedStatus + ".";

    return build(
            "SLA_REQUESTS_BY_STATUS",
            answer,
            items.size(),
            items
    );
}


        /*
         * =====================================================
         * FIND EMPLOYEE
         * =====================================================
         */

        if (
                question.contains("find employee")
                ||
                question.contains("find ")
                ||
                question.contains("search employee")
                ||
                question.contains("show employee")
        ) {

            String name = prompt
                    .trim()
                    .replaceFirst(
                            "(?i)find employee\\s*",
                            ""
                    )
                    .replaceFirst(
                            "(?i)search employee\\s*",
                            ""
                    )
                    .replaceFirst(
                            "(?i)show employee\\s*",
                            ""
                    )
                    .trim();

            if (name.isEmpty()) {

                return build(
                        "EMPLOYEE_SEARCH",
                        "Please provide an employee name.",
                        0,
                        List.of()
                );
            }

            User employee =
                    userRepository.findAll()
                            .stream()
                            .filter(user ->
                                    user.getFullName() != null
                                            &&
                                    user.getFullName()
                                            .equalsIgnoreCase(name)
                            )
                            .findFirst()
                            .orElse(null);

            if (employee == null) {

                return build(
                        "EMPLOYEE_NOT_FOUND",
                        "I couldn't find an employee named "
                                + name
                                + ".",
                        0,
                        List.of()
                );
            }

            List<AdminAiItem> items =
                    List.of(
                            convertUser(employee)
                    );

            String answer =
                    "Employee found: "
                            + employee.getFullName()
                            + ".";

            return build(
                    "EMPLOYEE_SEARCH",
                    answer,
                    1,
                    items
            );
        }
        
        /*
         * =====================================================
         * EMPLOYEE BIRTHDAYS
         * =====================================================
         */
        if (
                question.contains("birthday")
                || question.contains("birthdays")
                || question.contains("date of birth")
        ) {
            LocalDate today = LocalDate.now();
            boolean todayOnly = question.contains("today");
            boolean thisMonth = question.contains("this month");
            boolean upcoming = question.contains("upcoming")
                    || question.contains("next");

            List<User> birthdayEmployees = userRepository.findAll()
                    .stream()
                    .filter(user -> Boolean.TRUE.equals(user.getActive()))
                    .filter(user -> user.getFullName() != null)
                    .filter(user -> user.getDateOfBirth() != null)
                    .filter(user -> {
                        LocalDate dob = user.getDateOfBirth();

                        if (todayOnly) {
                            return dob.getMonthValue() == today.getMonthValue()
                                    && dob.getDayOfMonth() == today.getDayOfMonth();
                        }

                        if (thisMonth) {
                            return dob.getMonthValue() == today.getMonthValue();
                        }

                        if (upcoming) {
                            LocalDate nextBirthday = dob.withYear(today.getYear());

                            if (nextBirthday.isBefore(today)) {
                                nextBirthday = nextBirthday.plusYears(1);
                            }

                            return !nextBirthday.isAfter(today.plusDays(30));
                        }

                        // For a general birthday question, show birthdays
                        // in the current month.
                        return dob.getMonthValue() == today.getMonthValue();
                    })
                    .sorted(Comparator.comparing(user -> {
                        LocalDate dob = user.getDateOfBirth();
                        LocalDate nextBirthday = dob.withYear(today.getYear());

                        if (nextBirthday.isBefore(today)) {
                            nextBirthday = nextBirthday.plusYears(1);
                        }

                        return nextBirthday;
                    }))
                    .toList();

            List<AdminAiItem> items = birthdayEmployees.stream()
                    .map(user -> AdminAiItem.builder()
                            .employeeName(user.getFullName())
                            .holidayDate(
                                    user.getDateOfBirth()
                                            .format(DateTimeFormatter.ofPattern(
                                                    "dd MMM"
                                            ))
                            )
                            .status("BIRTHDAY")
                            .build())
                    .toList();

            String period = todayOnly
                    ? "today"
                    : thisMonth
                            ? "this month"
                            : upcoming
                                    ? "in the next 30 days"
                                    : "this month";

            String answer = birthdayEmployees.isEmpty()
                    ? "No employee birthdays were found " + period + "."
                    : birthdayEmployees.size()
                            + " active employee birthday(s) found "
                            + period + ".";

            return build(
                    "EMPLOYEE_BIRTHDAYS",
                    answer,
                    items.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * ACTIVE EMPLOYEES
         * =====================================================
         */
        if (
                question.contains("active employees")
                ||
                question.contains("active employee")
        ) {

            AdminDashboardDto dashboard =
                    userService.getAdminDashboard();

            Long count =
                    dashboard.getActiveEmployees();

            return build(
                    "ACTIVE_EMPLOYEES",
                    "There are currently "
                            + count
                            + " active employees.",
                    count.intValue(),
                    List.of()
            );
        }
        
        /*
         * =====================================================
         * HR SUMMARY
         * =====================================================
         */
        if (
                question.contains("hr summary")
                || question.contains("human resources summary")
                || question.contains("hr overview")
                || question.contains("hr report")
        ) {
            AdminDashboardDto dashboard =
                    userService.getAdminDashboard();

            List<LeaveResponseDto> requests =
                    leaveService.getAllRequestResponses();

            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(
                    today.lengthOfMonth()
            );

            List<LeaveResponseDto> monthlyRequests = requests.stream()
                    .filter(r -> r.getStartDate() != null
                            && r.getEndDate() != null)
                    .filter(r -> !r.getEndDate().isBefore(monthStart)
                            && !r.getStartDate().isAfter(monthEnd))
                    .toList();

            long pending = requests.stream()
                    .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                    .count();

            long approvedThisMonth = monthlyRequests.stream()
                    .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                    .count();

            long rejectedThisMonth = monthlyRequests.stream()
                    .filter(r -> "REJECTED".equalsIgnoreCase(r.getStatus()))
                    .count();

            BigDecimal approvedDays = monthlyRequests.stream()
                    .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                    .map(LeaveResponseDto::getTotalDays)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String answer =
                    "HR summary: "
                    + dashboard.getTotalEmployees()
                    + " total employees, "
                    + dashboard.getActiveEmployees()
                    + " active employees, "
                    + dashboard.getEmployeesOnLeaveToday()
                    + " employees on leave today, "
                    + pending
                    + " pending leave requests, "
                    + approvedThisMonth
                    + " leave requests approved this month, "
                    + rejectedThisMonth
                    + " rejected this month, and "
                    + approvedDays.stripTrailingZeros().toPlainString()
                    + " approved leave days scheduled in the current month.";

            return build(
                    "HR_SUMMARY",
                    answer,
                    requests.size(),
                    List.of()
            );
        }
        
        /*
         * =====================================================
         * ADMIN DASHBOARD SUMMARY
         * =====================================================
         */
        if (
                question.contains("dashboard summary")
                ||
                question.contains("dashboard overview")
                ||
                question.contains("dashboard")
                ||
                question.contains("overall dashboard")
        ) {

            AdminDashboardDto dashboard =
                    userService.getAdminDashboard();

            String answer =
                    "Here is the current dashboard summary: "
                    + dashboard.getTotalEmployees()
                    + " total employees, "
                    + dashboard.getActiveEmployees()
                    + " active employees, "
                    + dashboard.getEmployeesOnLeaveToday()
                    + " employees on leave today, "
                    + dashboard.getPendingApprovals()
                    + " pending approvals, "
                    + dashboard.getTotalCountries()
                    + " countries, "
                    + dashboard.getTotalPolicies()
                    + " leave policies and "
                    + dashboard.getUpcomingHolidays()
                    + " upcoming holidays.";

            return build(
                    "DASHBOARD_SUMMARY",
                    answer,
                    dashboard.getTotalEmployees().intValue(),
                    List.of()
            );
        }
        
        /*
         * =====================================================
         * EMPLOYEES BY COUNTRY
         * =====================================================
         */
        if (
                question.contains("each country")
                ||
                question.contains("by country")
                ||
                question.contains("country statistics")
                ||
                question.contains("country stats")
        ) {

            List<CountryEmployeeCountDto> countries =
                    userService.getEmployeeCountByCountry();

            List<AdminAiItem> items =
                    countries.stream()
                            .map(this::convertCountry)
                            .toList();

            return build(
                    "EMPLOYEES_BY_COUNTRY",
                    "There are employees across "
                            + countries.size()
                            + " countries.",
                    items.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * EMPLOYEES BY SPECIFIC COUNTRY
         * =====================================================
         */
        if (
                question.contains("employees in ")
                ||
                question.contains("working in ")
                ||
                question.contains("employees working in ")
        ) {

            String countryName =
                    extractCountryName(prompt);

            if (!countryName.isEmpty()) {

                List<CountryEmployeeCountDto> countries =
                        userService.getEmployeeCountByCountry();

                CountryEmployeeCountDto match =
                        countries.stream()
                                .filter(c ->
                                        c.getCountryName()
                                                != null
                                        &&
                                        c.getCountryName()
                                                .equalsIgnoreCase(
                                                        countryName
                                                )
                                )
                                .findFirst()
                                .orElse(null);

                if (match == null) {

                    return build(
                            "COUNTRY_NOT_FOUND",
                            "I couldn't find country information for "
                                    + countryName
                                    + ".",
                            0,
                            List.of()
                    );
                }

                return build(
                        "EMPLOYEES_BY_COUNTRY",
                        "There are "
                                + match.getEmployeeCount()
                                + " employees working in "
                                + match.getCountryName()
                                + ".",
                        match.getEmployeeCount().intValue(),
                        List.of(
                                convertCountry(match)
                        )
                );
            }
        }
        
        /*
         * =====================================================
         * EMPLOYEES ON LEAVE TODAY
         * =====================================================
         */
        if (
                question.contains("who is on leave today")
                ||
                question.contains("who are on leave today")
                ||
                question.contains("employees on leave today")
                ||
                question.contains("how many employees are on leave today")
                ||
                question.contains("how many people are on leave today")
        ) {

            LocalDate today =
                    LocalDate.now();

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(request ->
                                    "APPROVED".equalsIgnoreCase(
                                            request.getStatus()
                                    )
                            )
                            .filter(request ->
                                    request.getStartDate() != null
                                    &&
                                    request.getEndDate() != null
                            )
                            .filter(request ->
                                    !today.isBefore(
                                            request.getStartDate()
                                    )
                                    &&
                                    !today.isAfter(
                                            request.getEndDate()
                                    )
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer;

            if (items.isEmpty()) {

                answer =
                        "No employees are on approved leave today.";

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

            return build(
                    "EMPLOYEES_ON_LEAVE_TODAY",
                    answer,
                    items.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * TOMORROW'S LEAVE
         * =====================================================
         */
        if (
                question.contains("tomorrow")
                &&
                (
                        question.contains("leave")
                        ||
                        question.contains("going")
                        ||
                        question.contains("on leave")
                )
        ) {

            LocalDate tomorrow =
                    LocalDate.now().plusDays(1);

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
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
                            )
                            .filter(r ->
                                    !tomorrow.isBefore(
                                            r.getStartDate()
                                    )
                                    &&
                                    !tomorrow.isAfter(
                                            r.getEndDate()
                                    )
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    items.isEmpty()
                            ?
                            "No employees are on approved leave tomorrow."
                            :
                            items.size()
                            + " employee"
                            + (
                                items.size() == 1
                                ? " is"
                                : "s are"
                            )
                            + " on approved leave tomorrow.";

            return build(
                    "TEAM_LEAVE_TOMORROW",
                    answer,
                    items.size(),
                    items
            );
        }
        

/*
 * =====================================================
 * EMPLOYEE LEAVE DAYS TAKEN THIS YEAR
 * =====================================================
 */
if (
        question.contains("this year")
        && (question.contains("leave") || question.contains("leaves"))
        && (
                question.contains("taken")
                || question.contains("used")
                || question.contains("days")
        )
) {
    User employee = userRepository.findAll()
            .stream()
            .filter(user -> user.getFullName() != null)
            .filter(user -> question.contains(
                    user.getFullName().toLowerCase(Locale.ROOT)))
            .findFirst()
            .orElse(null);

    if (employee != null) {
        int currentYear = LocalDate.now().getYear();

        List<LeaveResponseDto> approvedLeaves =
                leaveService.getAllRequestResponses()
                        .stream()
                        .filter(r -> employee.getId().equals(
                                r.getEmployeeId()))
                        .filter(r -> "APPROVED".equalsIgnoreCase(
                                r.getStatus()))
                        .filter(r -> r.getStartDate() != null
                                && r.getStartDate().getYear() == currentYear)
                        .toList();

        BigDecimal totalDays = approvedLeaves.stream()
                .map(LeaveResponseDto::getTotalDays)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String days = totalDays.stripTrailingZeros().toPlainString();

        List<AdminAiItem> items = approvedLeaves.stream()
                .map(this::convert)
                .toList();

        return build(
                "EMPLOYEE_LEAVE_DAYS_THIS_YEAR",
                employee.getFullName() + " has taken "
                        + days + " approved leave days in "
                        + currentYear + ".",
                items.size(),
                items
        );
    }
}

/*
 * =====================================================
 * EMPLOYEE PENDING LEAVE REQUESTS
 * =====================================================
 */
if (
        question.contains("pending")
        && question.contains("leave")
        && (
                question.contains("request")
                || question.contains("how many")
                || question.contains("count")
        )
) {
    User employee = userRepository.findAll()
            .stream()
            .filter(user -> user.getFullName() != null)
            .filter(user -> question.contains(
                    user.getFullName().toLowerCase(Locale.ROOT)))
            .findFirst()
            .orElse(null);

    if (employee != null) {
        List<LeaveResponseDto> pendingLeaves =
                leaveService.getAllRequestResponses()
                        .stream()
                        .filter(r -> employee.getId().equals(
                                r.getEmployeeId()))
                        .filter(r -> "PENDING".equalsIgnoreCase(
                                r.getStatus()))
                        .toList();

        BigDecimal pendingDays = pendingLeaves.stream()
                .map(LeaveResponseDto::getTotalDays)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String days = pendingDays.stripTrailingZeros().toPlainString();

        List<AdminAiItem> items = pendingLeaves.stream()
                .map(this::convert)
                .toList();

        return build(
                "EMPLOYEE_PENDING_LEAVE_REQUESTS",
                employee.getFullName() + " has "
                        + pendingLeaves.size()
                        + " pending leave request(s), totalling "
                        + days + " requested leave days.",
                pendingLeaves.size(),
                items
        );
    }
}

        
        /*
         * =====================================================
         * ALL PENDING LEAVE REQUESTS
         * =====================================================
         */
        if (
                question.contains("pending leave")
                ||
                question.contains("pending requests")
                ||
                question.contains("pending request")
                ||
                question.contains("show all pending")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(r ->
                                    "PENDING".equals(
                                            r.getStatus()
                                    )
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    items.isEmpty()
                            ?
                            "There are currently no pending leave requests."
                            :
                            "There are "
                            + items.size()
                            + " pending leave request"
                            + (
                                items.size() == 1
                                ? "."
                                : "s."
                            );

            return build(
                    "PENDING_LEAVE_REQUESTS",
                    answer,
                    items.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * OVERALL LEAVE OVERVIEW
         * =====================================================
         */
        if (
                question.contains("overall leave")
                ||
                question.contains("leave overview")
                ||
                question.contains("overall leave overview")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses();

            long pending =
                    requests.stream()
                            .filter(r ->
                                    "PENDING".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            long approved =
                    requests.stream()
                            .filter(r ->
                                    "APPROVED".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            long rejected =
                    requests.stream()
                            .filter(r ->
                                    "REJECTED".equals(
                                            r.getStatus()
                                    )
                            )
                            .count();

            String answer =
                    "Overall leave overview: "
                    + requests.size()
                    + " total requests, "
                    + approved
                    + " approved, "
                    + pending
                    + " pending and "
                    + rejected
                    + " rejected.";

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            return build(
                    "OVERALL_LEAVE_OVERVIEW",
                    answer,
                    requests.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * EMPLOYEE LEAVE HISTORY
         * =====================================================
         */

        if (
                question.contains("leave history")
                ||
                question.contains("history of")
                ||
                question.contains("leave records")
        ) {

            String name =
                    extractHistoryEmployeeName(prompt);

            if (!name.isEmpty()) {

                User employee =
                        userRepository.findAll()
                                .stream()
                                .filter(user ->
                                        user.getFullName() != null
                                                &&
                                        user.getFullName()
                                                .equalsIgnoreCase(name)
                                )
                                .findFirst()
                                .orElse(null);

                if (employee == null) {

                    return build(
                            "EMPLOYEE_NOT_FOUND",
                            "I couldn't find an employee named "
                                    + name
                                    + ".",
                            0,
                            List.of()
                    );
                }

                Long employeeId =
                        employee.getId();

                List<LeaveResponseDto> history =
                        leaveService
                                .getAllRequestResponses()
                                .stream()
                                .filter(r ->
                                        r.getEmployeeId() != null
                                                &&
                                        r.getEmployeeId()
                                                .equals(employeeId)
                                )
                                .toList();

                List<AdminAiItem> items =
                        history.stream()
                                .map(this::convert)
                                .toList();

                String answer =
                        history.isEmpty()
                                ?
                                name
                                        + " has no leave history."
                                :
                                name
                                        + " has "
                                        + history.size()
                                        + " leave request"
                                        + (
                                            history.size() == 1
                                                    ? "."
                                                    : "s."
                                        );

                return build(
                        "EMPLOYEE_LEAVE_HISTORY",
                        answer,
                        history.size(),
                        items
                );
            }
        }
        

/*
 * =====================================================
 * EMPLOYEE LEAVE BALANCE
 * =====================================================
 */
if (
        question.contains("leave balance")
        || question.contains("remaining leave")
        || question.contains("leave entitlement")
) {
    User employee = userRepository.findAll()
            .stream()
            .filter(user -> user.getFullName() != null)
            .filter(user -> question.contains(
                    user.getFullName().toLowerCase(Locale.ROOT)))
            .findFirst()
            .orElse(null);

    if (employee == null) {
        return build(
                "EMPLOYEE_NOT_FOUND",
                "Please include the employee's full name "
                        + "in your question so I can find the correct person.",
                0,
                List.of()
        );
    }

    var balance = leaveService.getEmployeeBalance(employee.getId());

    if (balance == null) {
        return build(
                "EMPLOYEE_LEAVE_BALANCE",
                "No leave balance was found for "
                        + employee.getFullName() + ".",
                0,
                List.of()
        );
    }

    BigDecimal annual = balance.getAnnualBalance() != null
            ? balance.getAnnualBalance()
            : BigDecimal.ZERO;

    BigDecimal used = balance.getUsedLeave() != null
            ? balance.getUsedLeave()
            : BigDecimal.ZERO;

    BigDecimal remaining = balance.getRemainingLeave() != null
            ? balance.getRemainingLeave()
            : BigDecimal.ZERO;

    String answer = employee.getFullName()
            + "'s leave balance: annual entitlement "
            + annual.stripTrailingZeros().toPlainString()
            + " days, used "
            + used.stripTrailingZeros().toPlainString()
            + " days, remaining "
            + remaining.stripTrailingZeros().toPlainString()
            + " days.";

    return build(
            "EMPLOYEE_LEAVE_BALANCE",
            answer,
            1,
            List.of(convertUser(employee))
    );
}


        /*
         * =====================================================
         * 1. MOST LEAVE THIS MONTH
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

            LocalDate now = LocalDate.now();

            LocalDate monthStart =
                    now.withDayOfMonth(1);

            LocalDate monthEnd =
                    now.withDayOfMonth(
                            now.lengthOfMonth()
                    );

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
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
                            )
                            .filter(r ->
                                    !r.getEndDate()
                                            .isBefore(monthStart)
                                    &&
                                    !r.getStartDate()
                                            .isAfter(monthEnd)
                            )
                            .toList();

            Map<String, BigDecimal> employeeDays =
                    requests.stream()
                            .collect(
                                    Collectors.groupingBy(
                                            LeaveResponseDto::getEmployeeName,
                                            Collectors.reducing(
                                                    BigDecimal.ZERO,
                                                    request ->
                                                            request.getTotalDays() != null
                                                                    ? request.getTotalDays()
                                                                    : BigDecimal.ZERO,
                                                    BigDecimal::add
                                            )
                                    )
                            );

            List<Map.Entry<String, BigDecimal>> sorted =
                    employeeDays.entrySet()
                            .stream()
                            .sorted(
                                    Map.Entry
                                            .<String, BigDecimal>
                                            comparingByValue(
                                                    Comparator.reverseOrder()
                                            )
                            )
                            .toList();

            if (sorted.isEmpty()) {

                return build(
                        "MOST_LEAVE_MONTH",
                        "No approved leave was found for this month.",
                        0,
                        List.of()
                );
            }

            BigDecimal highest =
                    sorted.get(0).getValue();

            List<LeaveResponseDto> topRequests =
                    requests.stream()
                    .filter(r ->
                    employeeDays
                            .get(r.getEmployeeName())
                            .compareTo(highest) == 0
            )
                            .toList();

            List<AdminAiItem> items =
                    topRequests.stream()
                            .map(this::convert)
                            .toList();

            String names =
                    sorted.stream()
                    .filter(e ->
                    e.getValue().compareTo(highest) == 0
            )
                            .map(Map.Entry::getKey)
                            .collect(
                                Collectors.joining(", ")
                            );

            return build(
                    "MOST_LEAVE_MONTH",
                    names
                        + " has the most approved leave this month "
                        + "("
                        + highest
                        + " days).",
                    items.size(),
                    items
            );
        }
        
        /*
         * =====================================================
         * EMPLOYEE ANNUAL LEAVE USAGE PERCENTAGE
         * =====================================================
         */
        if (
                question.contains("percentage")
                || question.contains("percent")
                || question.contains("usage")
                || question.contains("entitlement used")
        ) {
            User employee = userRepository.findAll()
                    .stream()
                    .filter(user -> user.getFullName() != null)
                    .filter(user -> question.contains(
                            user.getFullName().toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .orElse(null);

            if (employee != null) {
                var balance =
                        leaveService.getEmployeeBalance(employee.getId());

                if (balance == null) {
                    return build(
                            "EMPLOYEE_LEAVE_USAGE",
                            "No leave balance was found for "
                                    + employee.getFullName() + ".",
                            0,
                            List.of()
                    );
                }

                BigDecimal annual = balance.getAnnualBalance() != null
                        ? balance.getAnnualBalance()
                        : BigDecimal.ZERO;

                BigDecimal used = balance.getUsedLeave() != null
                        ? balance.getUsedLeave()
                        : BigDecimal.ZERO;

                BigDecimal remaining = balance.getRemainingLeave() != null
                        ? balance.getRemainingLeave()
                        : BigDecimal.ZERO;

                String percentage;

                if (annual.compareTo(BigDecimal.ZERO) <= 0) {
                    percentage = "N/A";
                } else {
                    percentage = used.multiply(BigDecimal.valueOf(100))
                            .divide(
                                    annual,
                                    2,
                                    java.math.RoundingMode.HALF_UP
                            )
                            .stripTrailingZeros()
                            .toPlainString() + "%";
                }

                String answer = employee.getFullName()
                        + " has used "
                        + used.stripTrailingZeros().toPlainString()
                        + " of "
                        + annual.stripTrailingZeros().toPlainString()
                        + " annual leave days ("
                        + percentage
                        + "). Remaining balance: "
                        + remaining.stripTrailingZeros().toPlainString()
                        + " days.";

                return build(
                        "EMPLOYEE_LEAVE_USAGE",
                        answer,
                        1,
                        List.of(convertUser(employee))
                );
            }
        }

        /*
         * =====================================================
         * EMPLOYEES WITH HIGHEST ANNUAL LEAVE USAGE
         * =====================================================
         */
        if (
                question.contains("highest leave usage")
                || question.contains("highest leave utilization")
                || question.contains("most annual leave used")
                || question.contains("employees with most leave usage")
                || question.contains("highest leave percentage")
        ) {
            List<User> employees = userRepository.findAll()
                    .stream()
                    .filter(user -> user.getFullName() != null)
                    .filter(user -> Boolean.TRUE.equals(user.getActive()))
                    .toList();

            List<String> usageResults = new java.util.ArrayList<>();

            for (User employee : employees) {
                var balance =
                        leaveService.getEmployeeBalance(employee.getId());

                if (balance == null
                        || balance.getAnnualBalance() == null
                        || balance.getUsedLeave() == null
                        || balance.getAnnualBalance()
                                .compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal annual = balance.getAnnualBalance();
                BigDecimal used = balance.getUsedLeave();

                BigDecimal percentage = used.multiply(BigDecimal.valueOf(100))
                        .divide(
                                annual,
                                2,
                                java.math.RoundingMode.HALF_UP
                        );

                usageResults.add(
                        employee.getFullName()
                                + " — "
                                + percentage.stripTrailingZeros().toPlainString()
                                + "% used ("
                                + used.stripTrailingZeros().toPlainString()
                                + "/"
                                + annual.stripTrailingZeros().toPlainString()
                                + " days)"
                );
            }

            if (usageResults.isEmpty()) {
                return build(
                        "HIGHEST_LEAVE_USAGE",
                        "No usable annual leave balance data was found.",
                        0,
                        List.of()
                );
            }

            return build(
                    "HIGHEST_LEAVE_USAGE",
                    "Annual leave usage by active employees: "
                            + String.join("; ", usageResults),
                    usageResults.size(),
                    List.of()
            );
        }


        /*
         * =====================================================
         * 2. OLD PENDING REQUESTS
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

            LocalDateTime threshold =
                    LocalDateTime.now()
                            .minusDays(2);

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(r ->
                                "PENDING".equals(
                                    r.getStatus()
                                )
                            )
                            .filter(r ->
                                r.getCreatedAt() != null
                            )
                            .filter(r ->
                                r.getCreatedAt()
                                        .isBefore(threshold)
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    items.isEmpty()
                    ?
                    "There are no pending requests waiting for more than 2 days."
                    :
                    items.size()
                    + " pending request"
                    + (items.size() == 1 ? "" : "s")
                    + " have been waiting for more than 2 days.";

            return build(
                    "OLD_PENDING_REQUESTS",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * 3. MEDICAL LEAVE
         * =====================================================
         */

        if (
                question.contains("medical leave")
                ||
                question.contains("medical leaves")
                ||
                question.contains("sick leave")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(r ->
                                r.getLeaveType() != null
                            )
                            .filter(r ->
                                "MEDICAL".equalsIgnoreCase(
                                    r.getLeaveType()
                                )
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    items.isEmpty()
                    ?
                    "No medical leave requests were found."
                    :
                    items.size()
                    + " medical leave request"
                    + (items.size() == 1 ? " was" : "s were")
                    + " found.";

            return build(
                    "MEDICAL_LEAVE",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * 4. OVERLAPPING LEAVES
         * =====================================================
         */

        if (
                question.contains("overlapping")
                ||
                question.contains("overlap")
                ||
                question.contains("conflicting leave")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
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
                            )
                            .toList();

            List<AdminAiItem> overlapping =
                    requests.stream()
                            .filter(current ->

                                requests.stream()
                                    .anyMatch(other ->

                                        !current
                                            .getId()
                                            .equals(
                                                other.getId()
                                            )

                                        &&

                                        current
                                            .getStartDate()
                                            .isBefore(
                                                other.getEndDate()
                                                    .plusDays(1)
                                            )

                                        &&

                                        current
                                            .getEndDate()
                                            .isAfter(
                                                other.getStartDate()
                                                    .minusDays(1)
                                            )
                                    )
                            )
                            .map(this::convert)
                            .distinct()
                            .toList();

            String answer =
                    overlapping.isEmpty()
                    ?
                    "There are no overlapping approved leave requests."
                    :
                    overlapping.size()
                    + " approved leave request"
                    + (
                        overlapping.size() == 1
                        ? " overlaps"
                        : "s overlap"
                      )
                    + " with another leave request.";

            return build(
                    "OVERLAPPING_LEAVES",
                    answer,
                    overlapping.size(),
                    overlapping
            );
        }


        /*
         * =====================================================
         * 5. PENDING DOCUMENTS
         * =====================================================
         */

        if (
                question.contains("pending document")
                ||
                question.contains("pending documents")
                ||
                question.contains("documents pending")
                ||
                question.contains("missing document")
        ) {

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(r ->
                                Boolean.TRUE.equals(
                                    r.getDocumentRequested()
                                )
                            )
                            .filter(r ->
                                r.getDocumentStatus() != null
                            )
                            .filter(r ->
                                !isDocumentCompleted(
                                    r.getDocumentStatus()
                                )
                            )
                            .toList();

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            String answer =
                    items.isEmpty()
                    ?
                    "There are no employees with pending documents."
                    :
                    items.size()
                    + " leave request"
                    + (items.size() == 1 ? " has" : "s have")
                    + " pending documents.";

            return build(
                    "PENDING_DOCUMENTS",
                    answer,
                    items.size(),
                    items
            );
        }


        /*
         * =====================================================
         * 6. MONTHLY LEAVE SUMMARY
         * =====================================================
         */

        if (
                question.contains("monthly leave summary")
                ||
                question.contains("this month's leave")
                ||
                question.contains("this month leave")
                ||
                question.contains("leave summary")
        ) {

            LocalDate now = LocalDate.now();

            LocalDate monthStart =
                    now.withDayOfMonth(1);

            LocalDate monthEnd =
                    now.withDayOfMonth(
                            now.lengthOfMonth()
                    );

            List<LeaveResponseDto> requests =
                    leaveService
                            .getAllRequestResponses()
                            .stream()
                            .filter(r ->
                                r.getStartDate() != null
                                &&
                                r.getEndDate() != null
                            )
                            .filter(r ->
                                !r.getEndDate()
                                        .isBefore(monthStart)
                                &&
                                !r.getStartDate()
                                        .isAfter(monthEnd)
                            )
                            .toList();

            long pending =
                    requests.stream()
                            .filter(r ->
                                "PENDING".equals(
                                    r.getStatus()
                                )
                            )
                            .count();

            long approved =
                    requests.stream()
                            .filter(r ->
                                "APPROVED".equals(
                                    r.getStatus()
                                )
                            )
                            .count();

            long rejected =
                    requests.stream()
                            .filter(r ->
                                "REJECTED".equals(
                                    r.getStatus()
                                )
                            )
                            .count();

            BigDecimal totalDays = requests.stream()
                            .filter(r ->
                                "APPROVED".equals(
                                    r.getStatus()
                                )
                            )
                            .map(LeaveResponseDto::getTotalDays)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            String answer =
                    "This month there are "
                    + requests.size()
                    + " leave requests: "
                    + approved
                    + " approved, "
                    + pending
                    + " pending and "
                    + rejected
                    + " rejected. "
                    + "Approved leave accounts for "
                    + totalDays
                    + " leave days.";

            List<AdminAiItem> items =
                    requests.stream()
                            .map(this::convert)
                            .toList();

            return build(
                    "MONTHLY_LEAVE_SUMMARY",
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
                "I can help with employee information, leave balances, "
                        + "annual leave usage, employee birthdays, "
                        + "leave history, pending requests, SLA monitoring, "
                        + "employees on leave, country statistics, "
                        + "medical leave, overlapping leaves, pending "
                        + "documents, monthly leave summaries and HR summaries. "
                        + "Project allocation and appraisal questions require "
                        + "their corresponding data to be integrated.",
                0,
                List.of()
        );
    }


    private boolean isDocumentCompleted(
            Object status) {

        String value =
                status.toString()
                        .toUpperCase(Locale.ROOT);

        return value.equals("VERIFIED")
                || value.equals("APPROVED")
                || value.equals("COMPLETED");
    }


    private AdminAiResponse build(
            String intent,
            String answer,
            int count,
            List<AdminAiItem> items) {

        return AdminAiResponse.builder()
                .intent(intent)
                .answer(answer)
                .count(count)
                .items(items)
                .build();
    }


    private AdminAiItem convert(
            LeaveResponseDto dto) {

        return AdminAiItem.builder()

                .requestId(dto.getId())

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
                        dto.getTotalDays() != null
                                ? dto.getTotalDays().intValue()
                                : null
                )

                .status(
                        dto.getStatus()
                )

                .reason(
                        dto.getReason()
                )
                
                .slaStatus(dto.getSlaStatus())

                .slaDueAt(
                        dto.getSlaDueAt() != null
                                ? dto.getSlaDueAt().toString()
                                : null
                )

                .build();
    }
    
    private AdminAiItem convertEmployee(
            EmployeeResponseDto dto) {

        return AdminAiItem.builder()
                .employeeName(dto.getFullName())
                .status(
                        dto.getActive()
                                ? "ACTIVE"
                                : "INACTIVE"
                )
                .reason(dto.getDepartment())
                .build();
    }
    
    private AdminAiItem convertCountry(
            CountryEmployeeCountDto dto) {

        return AdminAiItem.builder()
                .employeeName(dto.getCountryName())
                .totalDays(
                        dto.getEmployeeCount() != null
                                ? dto.getEmployeeCount().intValue()
                                : null
                )
                .build();
    }
    
    private String extractCountryName(
            String prompt) {

        String text =
                prompt.trim();

        String lower =
                text.toLowerCase(Locale.ROOT);

        String country = "";

        if (lower.contains("working in ")) {

            int index =
                    lower.indexOf("working in ")
                            + "working in ".length();

            country =
                    text.substring(index).trim();

        } else if (lower.contains("employees in ")) {

            int index =
                    lower.indexOf("employees in ")
                            + "employees in ".length();

            country =
                    text.substring(index).trim();
        }

        return country
                .replace("?", "")
                .replace(".", "")
                .trim();
    }
    
    private String extractHistoryEmployeeName(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            return "";
        }

        String text = prompt.trim();

        String lower =
                text.toLowerCase(Locale.ROOT);

        String name = "";

        if (lower.contains("leave history of ")) {

            int index =
                    lower.indexOf("leave history of ")
                            + "leave history of ".length();

            name = text.substring(index).trim();

        } else if (lower.contains("leave history for ")) {

            int index =
                    lower.indexOf("leave history for ")
                            + "leave history for ".length();

            name = text.substring(index).trim();

        } else if (lower.contains("history of ")) {

            int index =
                    lower.indexOf("history of ")
                            + "history of ".length();

            name = text.substring(index).trim();

        } else if (lower.contains("history for ")) {

            int index =
                    lower.indexOf("history for ")
                            + "history for ".length();

            name = text.substring(index).trim();
        }

        return name
                .replace("?", "")
                .replace(".", "")
                .trim();
    }
    
    private AdminAiItem convertUser(User user) {

        return AdminAiItem.builder()
                .employeeName(user.getFullName())
                .status(
                        Boolean.TRUE.equals(user.getActive())
                                ? "ACTIVE"
                                : "INACTIVE"
                )
                .reason(user.getDepartment())
                .build();
    }
}