package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.BpMasterDataPermission
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface BpMasterDataPermissionRepository : ReactiveCrudRepository<BpMasterDataPermission, Long> {

    fun findByOwnerBpId(ownerBpId: Long): Flux<BpMasterDataPermission>

    fun findByUserBpId(userBpId: Long): Flux<BpMasterDataPermission>

    fun findByDataType(dataType: String): Flux<BpMasterDataPermission>

    fun findByPermissionType(permissionType: String): Flux<BpMasterDataPermission>

    fun findByStatus(status: String): Flux<BpMasterDataPermission>

    fun findByOwnerBpIdAndStatus(ownerBpId: Long, status: String): Flux<BpMasterDataPermission>

    fun findByUserBpIdAndStatus(userBpId: Long, status: String): Flux<BpMasterDataPermission>

    fun findByOwnerBpIdAndUserBpId(ownerBpId: Long, userBpId: Long): Flux<BpMasterDataPermission>

    @Query("""
        SELECT * FROM bp_master_data_permission
        WHERE owner_bp_id = :ownerBpId
        AND user_bp_id = :userBpId
        AND data_type = :dataType
        AND status = 'ACTIVE'
    """)
    fun findActivePermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String
    ): Flux<BpMasterDataPermission>

    @Query("""
        SELECT * FROM bp_master_data_permission
        WHERE owner_bp_id = :ownerBpId
        AND user_bp_id = :userBpId
        AND data_type = :dataType
        AND :date BETWEEN effective_date AND COALESCE(expiry_date, '9999-12-31')
        AND status = 'ACTIVE'
    """)
    fun findEffectivePermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        date: LocalDate
    ): Mono<BpMasterDataPermission>

    @Query("""
        SELECT * FROM bp_master_data_permission
        WHERE :date BETWEEN effective_date AND COALESCE(expiry_date, '9999-12-31')
        AND status = 'ACTIVE'
    """)
    fun findEffectivePermissions(date: LocalDate): Flux<BpMasterDataPermission>

    @Query("""
        SELECT COUNT(*) > 0 FROM bp_master_data_permission
        WHERE owner_bp_id = :ownerBpId
        AND user_bp_id = :userBpId
        AND data_type = :dataType
        AND permission_type = :permissionType
        AND :date BETWEEN effective_date AND COALESCE(expiry_date, '9999-12-31')
        AND status = 'ACTIVE'
    """)
    fun hasPermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        permissionType: String,
        date: LocalDate
    ): Mono<Boolean>

    fun existsByOwnerBpIdAndUserBpIdAndDataTypeAndEffectiveDate(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        effectiveDate: LocalDate
    ): Mono<Boolean>
}