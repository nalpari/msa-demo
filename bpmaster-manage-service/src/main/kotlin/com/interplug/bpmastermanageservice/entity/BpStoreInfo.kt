package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("bp_store_info")
data class BpStoreInfo(
    @Id
    @Column("store_id")
    val storeId: Long? = null,

    @Column("store_code")
    val storeCode: String,

    @Column("bp_id")
    val bpId: Long,

    @Column("store_name")
    val storeName: String,

    @Column("store_type")
    val storeType: String? = null,

    @Column("address")
    val address: String? = null,

    @Column("phone_number")
    val phoneNumber: String? = null,

    @Column("manager_name")
    val managerName: String? = null,

    @Column("opening_date")
    val openingDate: LocalDate? = null,

    @Column("closing_date")
    val closingDate: LocalDate? = null,

    @Column("status")
    val status: String = "ACTIVE",

    @Column("created_date")
    val createdDate: LocalDateTime? = LocalDateTime.now()
)