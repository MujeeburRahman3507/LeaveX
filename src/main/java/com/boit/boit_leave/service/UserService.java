package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.AdminDashboardDto;
import com.boit.boit_leave.dto.AdminStatisticsDto;
import com.boit.boit_leave.dto.BirthdayDto;
import com.boit.boit_leave.dto.BirthdayGreetingDto;
import com.boit.boit_leave.dto.CountryEmployeeCountDto;
import com.boit.boit_leave.dto.DashboardMetricsDto;
import com.boit.boit_leave.dto.EmployeeBalanceDto;
import com.boit.boit_leave.dto.EmployeeCreateDto;
import com.boit.boit_leave.dto.EmployeeResponseDto;
import com.boit.boit_leave.dto.EmployeeUpdateDto;
import com.boit.boit_leave.dto.LeaveBalanceUpdateDto;
import com.boit.boit_leave.dto.ProfileDto;
import com.boit.boit_leave.dto.UpdateProfileDto;
import com.boit.boit_leave.entity.Country;
import com.boit.boit_leave.entity.LeaveBalance;
import com.boit.boit_leave.entity.LeavePolicy;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.entity.Role;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.entity.UserCountryMapping;
import com.boit.boit_leave.exception.ResourceNotFoundException;
import com.boit.boit_leave.repository.CountryRepository;
import com.boit.boit_leave.repository.HolidayRepository;
import com.boit.boit_leave.repository.LeaveBalanceRepository;
import com.boit.boit_leave.repository.LeavePolicyRepository;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.repository.UserCountryMappingRepository;
import com.boit.boit_leave.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	@Value("${file.upload-dir}")
	private String uploadDir;
	
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCountryMappingRepository userCountryMappingRepository;
    private final CountryRepository countryRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final HolidayRepository holidayRepository;

    public EmployeeResponseDto createEmployee(
            EmployeeCreateDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        if (userRepository.existsByEmployeeCode(
                dto.getEmployeeCode())) {

            throw new IllegalArgumentException(
                    "Employee code already exists");
        }

        User manager = null;

        if (dto.getManagerId() != null) {

            manager = userRepository
                    .findById(dto.getManagerId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Manager not found"));
        }

        User user = User.builder()
                .employeeCode(dto.getEmployeeCode())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(
                        passwordEncoder.encode(
                                dto.getPassword()))
                .role(dto.getRole())
                .manager(manager)
                .active(dto.getActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .department(dto.getDepartment())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
        
        LeavePolicy policy =
                leavePolicyRepository
                        .findById(
                                dto.getLeavePolicyId()
                        )
                        .orElseThrow();

        user.setLeavePolicy(policy);
        
        //2 APPROVAL FLOW//
        configureApprovalFlow(
                user,
                dto.getApprovalLevels(),
                dto.getLevel1ApproverId(),
                dto.getLevel2ApproverId()
        );

        User savedUser =
                userRepository.save(user);

        if(dto.getCountryIds() != null) {

            for(Long countryId :
                    dto.getCountryIds()) {

                Country country =
                        countryRepository
                                .findById(countryId)
                                .orElseThrow();

                UserCountryMapping mapping =
                        UserCountryMapping
                                .builder()
                                .user(savedUser)
                                .country(country)
                                .createdAt(
                                        LocalDateTime.now())
                                .build();

                userCountryMappingRepository
                        .save(mapping);
            }
        }
//        LeaveBalance leaveBalance =
//                LeaveBalance.builder()
//                        .user(savedUser)
//                        .annualBalance(24)
//                        .usedLeave(0)
//                        .remainingLeave(24)
//      .createdAt(LocalDateTime.now())
//      .updatedAt(LocalDateTime.now())
//      .build();
//                        int totalLeave =
//                        policy.getCasualLeave()
//                        + policy.getMedicalLeave()
//                        + policy.getEarnedLeave();
        
        int totalLeave =
                policy.getAnnualLeave();

//                LeaveBalance leaveBalance =
//                        LeaveBalance.builder()
//                                .user(savedUser)
//                                .annualBalance(totalLeave)
//                                .usedLeave(0)
//                                .remainingLeave(totalLeave)
//                                .createdAt(LocalDateTime.now())
//                                .updatedAt(LocalDateTime.now())
//                                .build();
        
        LeaveBalance leaveBalance =
//                LeaveBalance.builder()
//                        .user(savedUser)
//                        .annualBalance(
//                                policy.getAnnualLeave()
//                        )
//                        .usedLeave(0)
//                        .remainingLeave(
//                                policy.getAnnualLeave()
//                        )
//                        .createdAt(LocalDateTime.now())
//                        .updatedAt(LocalDateTime.now())
//                        .build();
        
        LeaveBalance.builder()
        .user(savedUser)
        .annualBalance(BigDecimal.valueOf(totalLeave))
        .usedLeave(BigDecimal.ZERO)
        .remainingLeave(BigDecimal.valueOf(totalLeave))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();


        leaveBalanceRepository.save(
                leaveBalance
        );
        
  

        return mapToEmployeeResponse(
                savedUser
        );
    }

    public EmployeeResponseDto updateEmployee(
            Long employeeId,
            EmployeeUpdateDto dto) {

        User user = userRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"));

        user.setFullName(
                dto.getFullName());

        user.setEmail(
                dto.getEmail());

        user.setDepartment(
                dto.getDepartment());

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        if (dto.getManagerId() != null) {

            User manager =
                    userRepository
                            .findById(
                                    dto.getManagerId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Manager not found"));

            user.setManager(manager);
        } else {

            user.setManager(null);
        }

        if (dto.getActive() != null) {

            user.setActive(
                    dto.getActive());
        }

        user.setUpdatedAt(
                LocalDateTime.now());
        
        LeavePolicy policy =
                leavePolicyRepository
                        .findById(
                                dto.getLeavePolicyId()
                        )
                        .orElseThrow();

        user.setLeavePolicy(policy);
        
        user.setDateOfBirth(dto.getDateOfBirth());
        
        LeaveBalance balance =
                leaveBalanceRepository
                        .findByUserId(
                                user.getId()
                        )
                        .orElseThrow();

//        int totalLeave =
//                policy.getCasualLeave()
//                + policy.getMedicalLeave()
//                + policy.getEarnedLeave();
        
        int totalLeave =
                policy.getAnnualLeave();
//
//        balance.setAnnualBalance(
//                totalLeave
//        );
        balance.setAnnualBalance(
                BigDecimal.valueOf(policy.getAnnualLeave())
        );

//        balance.setRemainingLeave(
//                totalLeave
//                - balance.getUsedLeave()
//        );
        
        balance.setRemainingLeave(
                BigDecimal.valueOf(policy.getAnnualLeave())
                        .subtract(balance.getUsedLeave())
        );
        balance.setUpdatedAt(
                LocalDateTime.now()
        );

        leaveBalanceRepository.save(
                balance
        );
        
        //2APPROVAL FLOW//
        configureApprovalFlow(
                user,
                dto.getApprovalLevels(),
                dto.getLevel1ApproverId(),
                dto.getLevel2ApproverId()
        );

        User savedUser =
                userRepository.save(user);

        userCountryMappingRepository
                .deleteByUser(savedUser);

        if(dto.getCountryIds() != null) {

            for(Long countryId :
                    dto.getCountryIds()) {

                Country country =
                        countryRepository
                                .findById(countryId)
                                .orElseThrow();

                UserCountryMapping mapping =
                        UserCountryMapping
                                .builder()
                                .user(savedUser)
                                .country(country)
                                .createdAt(
                                        LocalDateTime.now())
                                .build();

                userCountryMappingRepository
                        .save(mapping);
            }
        }
        
  

        return mapToEmployeeResponse(
                savedUser);
    }

    public void deleteEmployee(
            Long employeeId) {

        User user = userRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(
            Long employeeId) {

        User user = userRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"));

        return mapToEmployeeResponse(user);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    public EmployeeBalanceDto updateLeaveBalance(
            Long userId,
            LeaveBalanceUpdateDto dto) {

        LeaveBalance balance =
                leaveBalanceRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave balance not found"));

        balance.setAnnualBalance(
                dto.getAnnualBalance());

        balance.setRemainingLeave(
        		dto.getAnnualBalance()
                        .subtract(balance.getUsedLeave())
        );

        balance.setUpdatedAt(
                LocalDateTime.now());

        LeaveBalance saved =
                leaveBalanceRepository
                        .save(balance);

        return EmployeeBalanceDto.builder()
                .userId(saved.getUser().getId())
                .employeeName(
                        saved.getUser().getFullName())
                .annualBalance(
                        saved.getAnnualBalance())
                .usedLeave(
                        saved.getUsedLeave())
                .remainingLeave(
                        saved.getRemainingLeave())
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeBalanceDto getEmployeeBalance(
            Long userId) {

        LeaveBalance balance =
                leaveBalanceRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave balance not found"));

        return EmployeeBalanceDto.builder()
                .userId(userId)
                .employeeName(
                        balance.getUser()
                                .getFullName())
                .annualBalance(
                        balance.getAnnualBalance())
                .usedLeave(
                        balance.getUsedLeave())
                .remainingLeave(
                        balance.getRemainingLeave())
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDto getDashboardMetrics(
            Long userId) {

        LeaveBalance balance =
                leaveBalanceRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave balance not found"));
        
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        LeavePolicy policy =
                user.getLeavePolicy();

        Long approved =
                leaveRequestRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId)
                        .stream()
                        .filter(x ->
                                x.getStatus()
                                        == LeaveStatus.APPROVED)
                        .count();

        Long rejected =
                leaveRequestRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId)
                        .stream()
                        .filter(x ->
                                x.getStatus()
                                        == LeaveStatus.REJECTED)
                        .count();

        Long pending =
                leaveRequestRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId)
                        .stream()
                        .filter(x ->
                                x.getStatus()
                                        == LeaveStatus.PENDING)
                        .count();
        
        
        return DashboardMetricsDto.builder()

                .annualBalance(
                        balance.getAnnualBalance())

                .usedLeave(
                        balance.getUsedLeave())

                .remainingLeave(
                        balance.getRemainingLeave())

                .approvedLeaves(
                        approved)

                .rejectedLeaves(
                        rejected)

                .pendingLeaves(
                        pending)

                // NEW

                .casualLeave(
                        BigDecimal.valueOf(policy.getCasualLeave()))

                .medicalLeave(
                        BigDecimal.valueOf(policy.getMedicalLeave()))

                .earnedLeave(
                        BigDecimal.valueOf(policy.getEarnedLeave()))

                .build();
    }

    @Transactional(readOnly = true)
    public AdminStatisticsDto getAdminStatistics() {

        return AdminStatisticsDto.builder()
                .totalEmployees(
                        userRepository.countByRole(
                                Role.EMPLOYEE))
                .totalManagers(
                        userRepository.countByRole(
                                Role.MANAGER))
                .totalAdmins(
                        userRepository.countByRole(
                                Role.ADMIN))
                .totalRequests(
                        leaveRequestRepository.count())
                .approvedRequests(
                        leaveRequestRepository
                                .countByStatus(
                                        LeaveStatus.APPROVED))
                .rejectedRequests(
                        leaveRequestRepository
                                .countByStatus(
                                        LeaveStatus.REJECTED))
                .pendingRequests(
                        leaveRequestRepository
                                .countByStatus(
                                        LeaveStatus.PENDING))
                .build();
    }

    private EmployeeResponseDto mapToEmployeeResponse(
            User user) {

        List<UserCountryMapping> mappings =
                userCountryMappingRepository
                        .findByUser(user);

        return EmployeeResponseDto.builder()
                .id(user.getId())
                .employeeCode(
                        user.getEmployeeCode())
                .fullName(
                        user.getFullName())
                .email(
                        user.getEmail())
                .role(
                        user.getRole())
                .active(
                        user.getActive())
                .department(
                        user.getDepartment())
                .managerId(
                        user.getManager() != null
                                ? user.getManager().getId()
                                : null)
                .managerName(
                        user.getManager() != null
                                ? user.getManager().getFullName()
                                : null)

                .countryIds(
                        mappings.stream()
                                .map(m ->
                                        m.getCountry().getId())
                                .toList()
                )

                .countryNames(
                        mappings.stream()
                                .map(m ->
                                        m.getCountry()
                                         .getCountryName())
                                .toList()
                )
                
                .leavePolicyId(
                	    user.getLeavePolicy() != null
                	        ? user.getLeavePolicy().getId()
                	        : null
                	)

                	.leavePolicyName(
                	    user.getLeavePolicy() != null
                	        ? user.getLeavePolicy().getPolicyName()
                	        : null
                	)
                	
                	.dateOfBirth(
                		    user.getDateOfBirth()
                		)
                	//2 APPROVAL FLOW//

.approvalLevels(user.getApprovalLevels())

.level1ApproverId(
    user.getLevel1Approver() != null
        ? user.getLevel1Approver().getId()
        : null
)
.level1ApproverName(
    user.getLevel1Approver() != null
        ? user.getLevel1Approver().getFullName()
        : null
)

.level2ApproverId(
    user.getLevel2Approver() != null
        ? user.getLevel2Approver().getId()
        : null
)
.level2ApproverName(
    user.getLevel2Approver() != null
        ? user.getLevel2Approver().getFullName()
        : null
)


                .build();
    }
    
    public List<EmployeeResponseDto> getManagers() {

        return userRepository
                .findByRole(Role.MANAGER)
                .stream()
                .map(this::mapToEmployeeResponse)
                .toList();
    }

    public List<EmployeeResponseDto> getAdmins() {

        return userRepository
                .findByRole(Role.ADMIN)
                .stream()
                .map(this::mapToEmployeeResponse)
                .toList();
    }
    
    public ProfileDto getProfile(
            Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        return ProfileDto.builder()
                .id(user.getId())
                .employeeCode(
                        user.getEmployeeCode())
                .fullName(
                        user.getFullName())
                .email(
                        user.getEmail())
                .role(
                        user.getRole().name())
                .department(
                        user.getDepartment())
                .managerName(
                        user.getManager() != null
                                ? user.getManager()
                                .getFullName()
                                : "N/A")
                .phone(
                        user.getPhone())
                .address(
                        user.getAddress())
                .profileImage(
                        user.getProfileImage())
                .build();
    }
    
    public ProfileDto updateProfile(
            Long userId,
            UpdateProfileDto request) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        user.setFullName(
                request.getFullName());

        user.setPhone(
                request.getPhone());

        user.setAddress(
                request.getAddress());

        user.setDepartment(
                request.getDepartment());

        userRepository.save(user);

        return getProfile(userId);
    }
    
    public String uploadPhoto(
            Long userId,
            MultipartFile file) {

        try {

            User user =
                    userRepository
                            .findById(userId)
                            .orElseThrow();

            Files.createDirectories(
                    Paths.get(uploadDir)
            );

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + file.getOriginalFilename();

            Path path =
                    Paths.get(
                            uploadDir,
                            fileName
                    );

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String imageUrl =
                    "/uploads/profile-images/"
                            + fileName;

            user.setProfileImage(
                    imageUrl
            );

            userRepository.save(user);

            return imageUrl;

        } catch (Exception e) {

            throw new RuntimeException(
                    e.getMessage()
            );
        }
    }
    
    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {

        LocalDate today = LocalDate.now();

        return AdminDashboardDto.builder()

                .totalEmployees(
                        userRepository.count()
                )

                .activeEmployees(
                        userRepository
                                .countByActiveTrue()
                )

                .employeesOnLeaveToday(
                        leaveRequestRepository
                                .countEmployeesOnLeaveToday(
                                        today
                                )
                )

                .pendingApprovals(
                        leaveRequestRepository
                                .countByStatus(
                                        LeaveStatus.PENDING
                                )
                )

                .totalCountries(
                        countryRepository.count()
                )

                .totalPolicies(
                        leavePolicyRepository.count()
                )

                .upcomingHolidays(
                        holidayRepository
                                .countByHolidayDateGreaterThanEqual(
                                        today
                                )
                )

                .build();
    }
    
    public List<CountryEmployeeCountDto>
    getEmployeeCountByCountry() {

        return userCountryMappingRepository
                .getEmployeeCountByCountry();
    }
    
    @Transactional(readOnly = true)
    public List<BirthdayDto> getTodaysBirthdays() {

        return userRepository
                .findTodaysBirthdays()
                .stream()
                .map(user ->

                        BirthdayDto.builder()

                                .id(user.getId())

                                .employeeCode(
                                        user.getEmployeeCode()
                                )

                                .fullName(
                                        user.getFullName()
                                )

                                .department(
                                        user.getDepartment()
                                )

                                .email(
                                        user.getEmail()
                                )

                                .profileImage(
                                        user.getProfileImage()
                                )

                                .build()

                )
                .toList();
    }
    
    @Transactional(readOnly = true)
    public BirthdayGreetingDto getBirthdayGreeting(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        if (user.getDateOfBirth() == null) {

            return BirthdayGreetingDto.builder()
                    .birthday(false)
                    .employeeName(user.getFullName())
                    .message("")
                    .build();

        }

        LocalDate today =
                LocalDate.now();

        LocalDate dob =
                user.getDateOfBirth();

        boolean birthday =
                today.getMonth() == dob.getMonth()
                        &&
                today.getDayOfMonth() == dob.getDayOfMonth();

        return BirthdayGreetingDto.builder()

                .birthday(
                        birthday
                )

                .employeeName(
                        user.getFullName()
                )

                .message(

                        birthday

                        ?

                        "🎉 Happy Birthday! BOIT GLOBAL wishes you a wonderful year filled with happiness, success and good health."

                        :

                        ""

                )

                .build();

    }
    
    public List<User> searchEmployeesByName(
            String name) {

        return userRepository
                .findByFullNameContainingIgnoreCase(name);
    }
    
    //2 APPROVAL FLOW//

private User getValidApprover(Long approverId, String levelName, Long employeeId) {
    if (approverId == null) {
        throw new IllegalArgumentException(
                levelName + " approver is required"
        );
    }

    if (employeeId != null && employeeId.equals(approverId)) {
        throw new IllegalArgumentException(
                "An employee cannot approve their own leave"
        );
    }

    User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    levelName + " approver not found"
            ));

    if (!Boolean.TRUE.equals(approver.getActive())) {
        throw new IllegalArgumentException(
                levelName + " approver must be active"
        );
    }

    if (approver.getRole() != Role.MANAGER
            && approver.getRole() != Role.ADMIN) {
        throw new IllegalArgumentException(
                levelName + " approver must be a Manager or Admin"
        );
    }

    return approver;
}


private void configureApprovalFlow(
        User user,
        Integer approvalLevels,
        Long level1ApproverId,
        Long level2ApproverId) {
	
	// Admin accounts do not require leave approvers.
	if (user.getRole() == Role.ADMIN) {
	    user.setApprovalLevels(1);
	    user.setLevel1Approver(null);
	    user.setLevel2Approver(null);
	    return;
	}


    int levels = approvalLevels == null ? 1 : approvalLevels;

    if (levels != 1 && levels != 2) {
        throw new IllegalArgumentException(
                "Approval levels must be either 1 or 2"
        );
    }

    User level1 = getValidApprover(
            level1ApproverId,
            "Level 1",
            user.getId()
    );

    User level2 = null;

    if (levels == 2) {
        level2 = getValidApprover(
                level2ApproverId,
                "Level 2",
                user.getId()
        );

        if (level1.getId().equals(level2.getId())) {
            throw new IllegalArgumentException(
                    "Level 1 and Level 2 approvers must be different"
            );
        }
    }

    user.setApprovalLevels(levels);
    user.setLevel1Approver(level1);
    user.setLevel2Approver(level2);
}

}