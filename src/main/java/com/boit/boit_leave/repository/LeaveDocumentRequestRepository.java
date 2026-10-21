package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.DocumentRequestStatus;
import com.boit.boit_leave.entity.DocumentStatus;
import com.boit.boit_leave.entity.LeaveDocumentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import com.boit.boit_leave.entity.DocumentType;

import java.util.List;
import java.util.Optional;

public interface LeaveDocumentRequestRepository
        extends JpaRepository<LeaveDocumentRequest, Long> {

	Optional<LeaveDocumentRequest>
	findFirstByLeaveRequestIdOrderByRequestedAtDesc(
	        Long leaveRequestId
	);
    /*
     * All requests for a Leave Request
     */
    List<LeaveDocumentRequest>
    findByLeaveRequestIdOrderByRequestedAtDesc(
            Long leaveRequestId
    );

    /*
     * Pending requests for a Leave Request
     */
    List<LeaveDocumentRequest>
    findByLeaveRequestIdAndStatusOrderByRequestedAtDesc(
            Long leaveRequestId,
            DocumentRequestStatus status
    );

    /*
     * Latest request
     */
    Optional<LeaveDocumentRequest>
    findTopByLeaveRequestIdOrderByRequestedAtDesc(
            Long leaveRequestId
    );

    /*
     * Check if any pending request exists
     */
//    boolean existsByLeaveRequestIdAndStatus(
//            Long leaveRequestId,
//            DocumentRequestStatus status
//    );
    
    boolean existsByLeaveRequestIdAndDocumentTypeAndStatus(
            Long leaveRequestId,
            DocumentType documentType,
            DocumentRequestStatus status
    );
    
    Optional<LeaveDocumentRequest>
    findFirstByLeaveRequestIdAndDocumentTypeAndStatusOrderByRequestedAtDesc(
            Long leaveRequestId,
            DocumentType documentType,
            DocumentRequestStatus status
    );
    
    Optional<LeaveDocumentRequest>
    findFirstByLeaveRequestIdAndDocumentTypeOrderByRequestedAtDesc(
            Long leaveRequestId,
            DocumentType documentType
    );
    
    List<LeaveDocumentRequest> findByLeaveRequestUserIdAndStatusOrderByRequestedAtDesc(
            Long userId,
            DocumentRequestStatus status
    );
    
    List<LeaveDocumentRequest>

    findByRequestedByIdAndDocumentStatusOrderByCompletedAtDesc(

            Long requestedBy,

            DocumentStatus documentStatus

    );

}