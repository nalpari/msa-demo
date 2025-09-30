package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.BpMasterDataPermission
import com.interplug.bpmastermanageservice.service.BpMasterDataPermissionService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/bp-master-data-permission")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpMasterDataPermissionController(
    private val permissionService: BpMasterDataPermissionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 권한 조회
    @GetMapping
    fun getAllPermissions(
        @RequestParam(required = false) ownerBpId: Long?,
        @RequestParam(required = false) userBpId: Long?,
        @RequestParam(required = false) dataType: String?,
        @RequestParam(required = false) permissionType: String?,
        @RequestParam(required = false) status: String?
    ): Flux<BpMasterDataPermission> {
        logger.info("GET /api/v1/bp-master-data-permission - owner: $ownerBpId, user: $userBpId, dataType: $dataType, permissionType: $permissionType, status: $status")

        return when {
            ownerBpId != null && userBpId != null -> permissionService.findByOwnerAndUser(ownerBpId, userBpId)
            ownerBpId != null && status != null -> permissionService.findByOwnerBpId(ownerBpId)
                .filter { it.status == status }
            ownerBpId != null -> permissionService.findByOwnerBpId(ownerBpId)
            userBpId != null && status != null -> permissionService.findByUserBpId(userBpId)
                .filter { it.status == status }
            userBpId != null -> permissionService.findByUserBpId(userBpId)
            dataType != null -> permissionService.findByDataType(dataType)
            permissionType != null -> permissionService.findByPermissionType(permissionType)
            status != null -> permissionService.findByStatus(status)
            else -> permissionService.findAll()
        }
    }

    // ID로 권한 조회
    @GetMapping("/{id}")
    fun getPermissionById(@PathVariable id: Long): Mono<ResponseEntity<BpMasterDataPermission>> {
        logger.info("GET /api/v1/bp-master-data-permission/$id")

        return permissionService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 활성 권한 조회
    @GetMapping("/active")
    fun getActivePermission(
        @RequestParam ownerBpId: Long,
        @RequestParam userBpId: Long,
        @RequestParam dataType: String
    ): Flux<BpMasterDataPermission> {
        logger.info("GET /api/v1/bp-master-data-permission/active - owner: $ownerBpId, user: $userBpId, dataType: $dataType")
        return permissionService.findActivePermission(ownerBpId, userBpId, dataType)
    }

    // 특정 날짜에 유효한 권한 조회
    @GetMapping("/effective")
    fun getEffectivePermission(
        @RequestParam ownerBpId: Long,
        @RequestParam userBpId: Long,
        @RequestParam dataType: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Mono<ResponseEntity<BpMasterDataPermission>> {
        val effectiveDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-master-data-permission/effective - owner: $ownerBpId, user: $userBpId, dataType: $dataType, date: $effectiveDate")

        return permissionService.findEffectivePermission(ownerBpId, userBpId, dataType, effectiveDate)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 특정 날짜에 유효한 모든 권한 조회
    @GetMapping("/effective/all")
    fun getAllEffectivePermissions(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Flux<BpMasterDataPermission> {
        val effectiveDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-master-data-permission/effective/all - date: $effectiveDate")
        return permissionService.findEffectivePermissions(effectiveDate)
    }

    // 권한 체크
    @GetMapping("/check")
    fun checkPermission(
        @RequestParam ownerBpId: Long,
        @RequestParam userBpId: Long,
        @RequestParam dataType: String,
        @RequestParam permissionType: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Mono<Map<String, Boolean>> {
        val checkDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-master-data-permission/check - owner: $ownerBpId, user: $userBpId, dataType: $dataType, permissionType: $permissionType, date: $checkDate")

        return permissionService.hasPermission(ownerBpId, userBpId, dataType, permissionType, checkDate)
            .map { hasPermission -> mapOf("hasPermission" to hasPermission) }
    }

    // 권한 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPermission(@RequestBody permission: BpMasterDataPermission): Mono<BpMasterDataPermission> {
        logger.info("POST /api/v1/bp-master-data-permission - Creating permission for owner: ${permission.ownerBpId}, user: ${permission.userBpId}")
        return permissionService.create(permission)
    }

    // 권한 수정
    @PutMapping("/{id}")
    fun updatePermission(
        @PathVariable id: Long,
        @RequestBody permission: BpMasterDataPermission
    ): Mono<ResponseEntity<BpMasterDataPermission>> {
        logger.info("PUT /api/v1/bp-master-data-permission/$id - Updating permission")

        return permissionService.update(id, permission)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 권한 상태 변경
    @PatchMapping("/{id}/status")
    fun updatePermissionStatus(
        @PathVariable id: Long,
        @RequestParam status: String
    ): Mono<ResponseEntity<BpMasterDataPermission>> {
        logger.info("PATCH /api/v1/bp-master-data-permission/$id/status - Updating status to: $status")

        return permissionService.updateStatus(id, status)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 권한 만료 처리
    @PatchMapping("/{id}/expire")
    fun expirePermission(
        @PathVariable id: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) expiryDate: LocalDate?
    ): Mono<ResponseEntity<BpMasterDataPermission>> {
        val expDate = expiryDate ?: LocalDate.now()
        logger.info("PATCH /api/v1/bp-master-data-permission/$id/expire - Expiring on: $expDate")

        return permissionService.expirePermission(id, expDate)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 권한 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePermission(@PathVariable id: Long): Mono<Void> {
        logger.info("DELETE /api/v1/bp-master-data-permission/$id")
        return permissionService.delete(id)
    }

    // 권한 중복 체크
    @GetMapping("/check/duplicate")
    fun checkDuplicate(
        @RequestParam ownerBpId: Long,
        @RequestParam userBpId: Long,
        @RequestParam dataType: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) effectiveDate: LocalDate
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-master-data-permission/check/duplicate - owner: $ownerBpId, user: $userBpId, dataType: $dataType, date: $effectiveDate")

        return permissionService.existsByOwnerUserDataTypeAndDate(ownerBpId, userBpId, dataType, effectiveDate)
            .map { exists -> mapOf("exists" to exists) }
    }
}