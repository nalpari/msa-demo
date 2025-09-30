package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.BpContractInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface BpContractInfoRepository : ReactiveCrudRepository<BpContractInfo, Long> {

    fun findByContractCode(contractCode: String): Mono<BpContractInfo>

    fun findByContractorBpId(contractorBpId: Long): Flux<BpContractInfo>

    fun findByContracteeBpId(contracteeBpId: Long): Flux<BpContractInfo>

    fun findByPfId(pfId: Long): Flux<BpContractInfo>

    fun findByContractType(contractType: String): Flux<BpContractInfo>

    fun findByStatus(status: String): Flux<BpContractInfo>

    fun findByContractorBpIdAndStatus(contractorBpId: Long, status: String): Flux<BpContractInfo>

    fun findByContracteeBpIdAndStatus(contracteeBpId: Long, status: String): Flux<BpContractInfo>

    @Query("""
        SELECT * FROM bp_contract_info
        WHERE (contractor_bp_id = :bpId OR contractee_bp_id = :bpId)
        AND status = 'ACTIVE'
    """)
    fun findActiveContractsByBpId(bpId: Long): Flux<BpContractInfo>

    @Query("""
        SELECT * FROM bp_contract_info
        WHERE :date BETWEEN contract_start_date AND COALESCE(contract_end_date, '9999-12-31')
        AND status = 'ACTIVE'
    """)
    fun findEffectiveContracts(date: LocalDate): Flux<BpContractInfo>

    @Query("""
        SELECT * FROM bp_contract_info
        WHERE contractor_bp_id = :contractorBpId
        AND contractee_bp_id = :contracteeBpId
        AND pf_id = :pfId
        AND :date BETWEEN contract_start_date AND COALESCE(contract_end_date, '9999-12-31')
        AND status = 'ACTIVE'
    """)
    fun findActiveContractBetweenBps(
        contractorBpId: Long,
        contracteeBpId: Long,
        pfId: Long,
        date: LocalDate
    ): Mono<BpContractInfo>

    fun existsByContractCode(contractCode: String): Mono<Boolean>

    @Query("""
        SELECT COUNT(*) > 0 FROM bp_contract_info
        WHERE contractor_bp_id = :contractorBpId
        AND contractee_bp_id = :contracteeBpId
        AND pf_id = :pfId
        AND contract_start_date <= :endDate
        AND COALESCE(contract_end_date, '9999-12-31') >= :startDate
        AND status = 'ACTIVE'
    """)
    fun existsOverlappingContract(
        contractorBpId: Long,
        contracteeBpId: Long,
        pfId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Mono<Boolean>
}