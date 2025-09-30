package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.BpMasterDataPermission
import com.interplug.bpmastermanageservice.repository.BpMasterDataPermissionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BpMasterDataPermissionService(
    private val permissionRepository: BpMasterDataPermissionRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 권한 조회
    fun findAll(): Flux<BpMasterDataPermission> {
        logger.debug("Finding all permissions")
        return permissionRepository.findAll()
    }

    // ID로 권한 조회
    fun findById(id: Long): Mono<BpMasterDataPermission> {
        logger.debug("Finding permission by id: $id")
        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Permission not found with id: $id")))
    }

    // Owner BP ID로 권한 조회
    fun findByOwnerBpId(ownerBpId: Long): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions by owner BP id: $ownerBpId")
        return permissionRepository.findByOwnerBpId(ownerBpId)
    }

    // User BP ID로 권한 조회
    fun findByUserBpId(userBpId: Long): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions by user BP id: $userBpId")
        return permissionRepository.findByUserBpId(userBpId)
    }

    // 데이터 타입별 권한 조회
    fun findByDataType(dataType: String): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions by data type: $dataType")
        return permissionRepository.findByDataType(dataType)
    }

    // 권한 타입별 조회
    fun findByPermissionType(permissionType: String): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions by permission type: $permissionType")
        return permissionRepository.findByPermissionType(permissionType)
    }

    // 상태별 조회
    fun findByStatus(status: String): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions by status: $status")
        return permissionRepository.findByStatus(status)
    }

    // Owner와 User BP 간 권한 조회
    fun findByOwnerAndUser(ownerBpId: Long, userBpId: Long): Flux<BpMasterDataPermission> {
        logger.debug("Finding permissions between owner BP $ownerBpId and user BP $userBpId")
        return permissionRepository.findByOwnerBpIdAndUserBpId(ownerBpId, userBpId)
    }

    // 활성 권한 조회
    fun findActivePermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String
    ): Flux<BpMasterDataPermission> {
        logger.debug("Finding active permission for owner: $ownerBpId, user: $userBpId, dataType: $dataType")
        return permissionRepository.findActivePermission(ownerBpId, userBpId, dataType)
    }

    // 특정 날짜에 유효한 권한 조회
    fun findEffectivePermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        date: LocalDate = LocalDate.now()
    ): Mono<BpMasterDataPermission> {
        logger.debug("Finding effective permission on date: $date")
        return permissionRepository.findEffectivePermission(ownerBpId, userBpId, dataType, date)
    }

    // 특정 날짜에 유효한 모든 권한 조회
    fun findEffectivePermissions(date: LocalDate = LocalDate.now()): Flux<BpMasterDataPermission> {
        logger.debug("Finding all effective permissions on date: $date")
        return permissionRepository.findEffectivePermissions(date)
    }

    // 권한 체크
    fun hasPermission(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        permissionType: String,
        date: LocalDate = LocalDate.now()
    ): Mono<Boolean> {
        logger.debug("Checking permission for owner: $ownerBpId, user: $userBpId, dataType: $dataType, permissionType: $permissionType")
        return permissionRepository.hasPermission(ownerBpId, userBpId, dataType, permissionType, date)
    }

    // 권한 생성
    @Transactional
    fun create(permission: BpMasterDataPermission): Mono<BpMasterDataPermission> {
        logger.debug("Creating new permission for owner: ${permission.ownerBpId}, user: ${permission.userBpId}")

        return permissionRepository.existsByOwnerBpIdAndUserBpIdAndDataTypeAndEffectiveDate(
            permission.ownerBpId,
            permission.userBpId,
            permission.dataType,
            permission.effectiveDate
        ).flatMap { exists ->
            if (exists) {
                Mono.error(IllegalArgumentException("Permission already exists for the given combination"))
            } else {
                val newPermission = permission.copy(
                    createdDate = LocalDateTime.now()
                )
                permissionRepository.save(newPermission)
            }
        }
        .doOnSuccess { logger.info("Successfully created permission: ${it.permissionId}") }
        .doOnError { logger.error("Failed to create permission", it) }
    }

    // 권한 수정
    @Transactional
    fun update(id: Long, updateRequest: BpMasterDataPermission): Mono<BpMasterDataPermission> {
        logger.debug("Updating permission id: $id")

        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Permission not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    ownerBpId = updateRequest.ownerBpId,
                    userBpId = updateRequest.userBpId,
                    dataType = updateRequest.dataType,
                    permissionType = updateRequest.permissionType,
                    effectiveDate = updateRequest.effectiveDate,
                    expiryDate = updateRequest.expiryDate,
                    status = updateRequest.status
                )
                permissionRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated permission id: $id") }
            .doOnError { logger.error("Failed to update permission id: $id", it) }
    }

    // 권한 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String): Mono<BpMasterDataPermission> {
        logger.debug("Updating status for permission id: $id to $status")

        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Permission not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status
                )
                permissionRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for permission id: $id to $status") }
    }

    // 권한 만료 처리
    @Transactional
    fun expirePermission(id: Long, expiryDate: LocalDate = LocalDate.now()): Mono<BpMasterDataPermission> {
        logger.debug("Expiring permission id: $id on date: $expiryDate")

        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Permission not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    expiryDate = expiryDate,
                    status = "EXPIRED"
                )
                permissionRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully expired permission id: $id") }
    }

    // 권한 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting permission id: $id")

        return permissionRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Permission not found with id: $id")))
            .flatMap { permissionRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted permission id: $id") }
            .doOnError { logger.error("Failed to delete permission id: $id", it) }
    }

    // 권한 중복 체크
    fun existsByOwnerUserDataTypeAndDate(
        ownerBpId: Long,
        userBpId: Long,
        dataType: String,
        effectiveDate: LocalDate
    ): Mono<Boolean> {
        logger.debug("Checking if permission exists for owner: $ownerBpId, user: $userBpId, dataType: $dataType, date: $effectiveDate")
        return permissionRepository.existsByOwnerBpIdAndUserBpIdAndDataTypeAndEffectiveDate(
            ownerBpId,
            userBpId,
            dataType,
            effectiveDate
        )
    }
}