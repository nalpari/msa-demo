package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate
import java.time.LocalDateTime

@Table("bp_pf_mapping")
data class BpPfMapping(
    @Id
    @Column("mapping_id")
    val mappingId: Long? = null,

    @Column("bp_id")
    val bpId: Long,

    @Column("pf_id")
    val pfId: Long,

    @Column("target_bp_id")
    val targetBpId: Long? = null,

    @Column("parent_bp_id")
    val parentBpId: Long? = null,

    @Column("mapping_type")
    val mappingType: String,

    @Column("effective_date")
    val effectiveDate: LocalDate,

    @Column("expiry_date")
    val expiryDate: LocalDate? = null,

    @Column("status")
    val status: String? = "ACTIVE",

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_date")
    val updatedDate: LocalDateTime? = null,

    @Column("created_by")
    val createdBy: String? = null,

    @Column("updated_by")
    val updatedBy: String? = null
)