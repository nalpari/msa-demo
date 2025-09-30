package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,

    @Column("bp_name")
    val bpName: String,

    @Column("bp_type")
    val bpType: String,

    @Column("business_reg_no")
    val businessRegNo: String? = null,

    @Column("representative_name")
    val representativeName: String? = null,

    @Column("address")
    val address: String? = null,

    @Column("phone_number")
    val phoneNumber: String? = null,

    @Column("email")
    val email: String? = null,

    @Column("primary_pf_code")
    val primaryPfCode: String? = null,

    @Column("status")
    val status: String? = "ACTIVE",

    @Column("erp_usage_fee")
    val erpUsageFee: BigDecimal? = null,

    @Column("commission_rate")
    val commissionRate: BigDecimal? = null,

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