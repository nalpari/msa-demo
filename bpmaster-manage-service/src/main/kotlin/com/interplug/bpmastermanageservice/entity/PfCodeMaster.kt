package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

@Table("pf_code_master")
data class PfCodeMaster(
    @Id
    @Column("pf_id")
    val pfId: Long? = null,

    @Column("pf_code")
    val pfCode: String,

    @Column("pf_name")
    val pfName: String,

    @Column("pf_description")
    val pfDescription: String? = null,

    @Column("has_master_data")
    val hasMasterData: String? = "N",

    @Column("can_own_stores")
    val canOwnStores: String? = "N",

    @Column("can_franchise")
    val canFranchise: String? = "N",

    @Column("billing_capable")
    val billingCapable: String? = "N",

    @Column("status")
    val status: String? = "ACTIVE",

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null
)