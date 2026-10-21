package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.AppraisalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppraisalDocumentRepository extends JpaRepository<AppraisalDocument, Long> {

    List<AppraisalDocument> findByAppraisalIdOrderByCreatedAtDesc(
            Long appraisalId
    );

    List<AppraisalDocument> findByGoalIdOrderByCreatedAtDesc(
            Long goalId
    );
}