package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.BpPfMapping
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Repository
interface BpPfMappingRepository : ReactiveCrudRepository<BpPfMapping, Long> {

    // BP ID로 매핑 조회
    fun findByBpId(bpId: Long): Flux<BpPfMapping>

    // PF ID로 매핑 조회
    fun findByPfId(pfId: Long): Flux<BpPfMapping>

    // Target BP ID로 매핑 조회
    fun findByTargetBpId(targetBpId: Long): Flux<BpPfMapping>

    // Parent BP ID로 매핑 조회
    fun findByParentBpId(parentBpId: Long): Flux<BpPfMapping>

    // 매핑 타입으로 조회
    fun findByMappingType(mappingType: String): Flux<BpPfMapping>

    // 상태별 조회
    fun findByStatus(status: String): Flux<BpPfMapping>

    // BP ID와 상태로 조회
    fun findByBpIdAndStatus(bpId: Long, status: String): Flux<BpPfMapping>

    // 특정 날짜에 유효한 매핑 조회
    @Query("""
        SELECT * FROM bp_pf_mapping
        WHERE bp_id = :bpId
        AND effective_date <= :date
        AND (expiry_date IS NULL OR expiry_date >= :date)
        AND status = 'ACTIVE'
    """)
    fun findEffectiveMappings(bpId: Long, date: LocalDate): Flux<BpPfMapping>

    // BP와 PF 조합으로 조회
    fun findByBpIdAndPfId(bpId: Long, pfId: Long): Flux<BpPfMapping>

    // 중복 체크
    fun existsByBpIdAndPfIdAndTargetBpIdAndEffectiveDate(
        bpId: Long,
        pfId: Long,
        targetBpId: Long?,
        effectiveDate: LocalDate
    ): Mono<Boolean>
}