package com.interplug.bpmastermanageservice.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Table("bp_contract_info")
data class BpContractInfo(
    @Id
    @Column("contract_id")
    val contractId: Long? = null,

    @Column("contract_code")
    val contractCode: String,

    @Column("contractor_bp_id")
    val contractorBpId: Long,

    @Column("contractee_bp_id")
    val contracteeBpId: Long,

    @Column("contract_type")
    val contractType: String,

    @Column("pf_id")
    val pfId: Long,

    @Column("contract_start_date")
    val contractStartDate: LocalDate,

    @Column("contract_end_date")
    val contractEndDate: LocalDate? = null,

    @Column("contract_terms")
    val contractTerms: String? = null,

    @Column("fee_rate")
    val feeRate: BigDecimal? = null,

    @Column("status")
    val status: String = "ACTIVE",

    @Column("created_date")
    val createdDate: LocalDateTime? = LocalDateTime.now()
)