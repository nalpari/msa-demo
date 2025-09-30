package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("bp_master_data_permission")
data class BpMasterDataPermission(
    @Id
    @Column("permission_id")
    val permissionId: Long? = null,

    @Column("owner_bp_id")
    val ownerBpId: Long,

    @Column("user_bp_id")
    val userBpId: Long,

    @Column("data_type")
    val dataType: String,

    @Column("permission_type")
    val permissionType: String,

    @Column("effective_date")
    val effectiveDate: LocalDate,

    @Column("expiry_date")
    val expiryDate: LocalDate? = null,

    @Column("status")
    val status: String = "ACTIVE",

    @Column("created_date")
    val createdDate: LocalDateTime? = LocalDateTime.now()
)