package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.DocumentStatus;
import com.boit.boit_leave.entity.LeaveDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveDocumentRepository
        extends JpaRepository<LeaveDocument, Long> {

    /**
     * Documents of one leave request
     */
    List<LeaveDocument>
    findByLeaveRequestIdOrderByUploadedAtDesc(
            Long leaveRequestId
    );

    /**
     * Documents by status
     */
    List<LeaveDocument>
    findByStatus(
            DocumentStatus status
    );

    /**
     * Count uploaded documents
     */
    long countByLeaveRequestId(
            Long leaveRequestId
    );

    /**
     * Delete all documents
     * when leave request is deleted
     */
    void deleteByLeaveRequestId(
            Long leaveRequestId
    );

}