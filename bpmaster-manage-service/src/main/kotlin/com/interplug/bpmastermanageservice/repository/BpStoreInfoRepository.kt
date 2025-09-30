package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.BpStoreInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface BpStoreInfoRepository : ReactiveCrudRepository<BpStoreInfo, Long> {

    fun findByStoreCode(storeCode: String): Mono<BpStoreInfo>

    fun findByBpId(bpId: Long): Flux<BpStoreInfo>

    fun findByStoreType(storeType: String): Flux<BpStoreInfo>

    fun findByStatus(status: String): Flux<BpStoreInfo>

    fun findByBpIdAndStatus(bpId: Long, status: String): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE bp_id = :bpId
        AND status = 'ACTIVE'
    """)
    fun findActiveStoresByBpId(bpId: Long): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE store_name ILIKE CONCAT('%', :name, '%')
    """)
    fun searchByStoreName(name: String): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE address ILIKE CONCAT('%', :address, '%')
    """)
    fun searchByAddress(address: String): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE manager_name ILIKE CONCAT('%', :managerName, '%')
    """)
    fun searchByManagerName(managerName: String): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE opening_date <= :date
        AND (closing_date IS NULL OR closing_date > :date)
        AND status = 'ACTIVE'
    """)
    fun findOperatingStoresAsOfDate(date: LocalDate): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE opening_date BETWEEN :startDate AND :endDate
    """)
    fun findStoresOpenedBetween(startDate: LocalDate, endDate: LocalDate): Flux<BpStoreInfo>

    @Query("""
        SELECT * FROM bp_store_info
        WHERE closing_date BETWEEN :startDate AND :endDate
    """)
    fun findStoresClosedBetween(startDate: LocalDate, endDate: LocalDate): Flux<BpStoreInfo>

    @Query("""
        SELECT COUNT(*) FROM bp_store_info
        WHERE bp_id = :bpId
        AND status = 'ACTIVE'
    """)
    fun countActiveStoresByBpId(bpId: Long): Mono<Long>

    fun existsByStoreCode(storeCode: String): Mono<Boolean>

    fun existsByBpIdAndStoreCode(bpId: Long, storeCode: String): Mono<Boolean>
}