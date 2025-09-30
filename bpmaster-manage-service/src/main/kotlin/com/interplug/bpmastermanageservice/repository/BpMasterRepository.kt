package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.BpMaster
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {

    // 비즈니스 파트너 코드로 조회
    fun findByBpCode(bpCode: String): Mono<BpMaster>

    // 비즈니스 파트너 타입으로 조회
    fun findByBpType(bpType: String): Flux<BpMaster>

    // 상태별 조회
    fun findByStatus(status: String): Flux<BpMaster>

    // 비즈니스 파트너 타입과 상태로 조회
    fun findByBpTypeAndStatus(bpType: String, status: String): Flux<BpMaster>

    // 이름으로 검색 (LIKE 검색)
    @Query("SELECT * FROM bp_master WHERE bp_name LIKE CONCAT('%', :bpName, '%')")
    fun searchByBpName(bpName: String): Flux<BpMaster>

    // 사업자등록번호로 조회
    fun findByBusinessRegNo(businessRegNo: String): Mono<BpMaster>

    // 비즈니스 파트너 코드 중복 체크
    fun existsByBpCode(bpCode: String): Mono<Boolean>

    // 사업자등록번호 중복 체크
    fun existsByBusinessRegNo(businessRegNo: String): Mono<Boolean>

    // 활성화된 비즈니스 파트너만 조회
    @Query("SELECT * FROM bp_master WHERE status = 'ACTIVE' ORDER BY bp_name")
    fun findAllActive(): Flux<BpMaster>

    // 타입별 활성화된 비즈니스 파트너 조회
    @Query("SELECT * FROM bp_master WHERE bp_type = :bpType AND status = 'ACTIVE' ORDER BY bp_name")
    fun findActiveByType(bpType: String): Flux<BpMaster>

    // Primary PF Code로 조회
    fun findByPrimaryPfCode(primaryPfCode: String): Flux<BpMaster>
}