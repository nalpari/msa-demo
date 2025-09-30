package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.BpPfMapping
import com.interplug.bpmastermanageservice.service.BpPfMappingService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/bp-pf-mapping")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpPfMappingController(
    private val bpPfMappingService: BpPfMappingService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 매핑 조회
    @GetMapping
    fun getAllMappings(
        @RequestParam(required = false) bpId: Long?,
        @RequestParam(required = false) pfId: Long?,
        @RequestParam(required = false) status: String?
    ): Flux<BpPfMapping> {
        logger.info("GET /api/v1/bp-pf-mapping - bpId: $bpId, pfId: $pfId, status: $status")

        return when {
            bpId != null && status != null -> bpPfMappingService.findByBpIdAndStatus(bpId, status)
            bpId != null -> bpPfMappingService.findByBpId(bpId)
            pfId != null -> bpPfMappingService.findByPfId(pfId)
            else -> bpPfMappingService.findAll()
        }
    }

    // ID로 매핑 조회
    @GetMapping("/{id}")
    fun getMappingById(@PathVariable id: Long): Mono<ResponseEntity<BpPfMapping>> {
        logger.info("GET /api/v1/bp-pf-mapping/$id")

        return bpPfMappingService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // BP와 PF 조합으로 매핑 조회
    @GetMapping("/by-bp-pf")
    fun getMappingsByBpAndPf(
        @RequestParam bpId: Long,
        @RequestParam pfId: Long
    ): Flux<BpPfMapping> {
        logger.info("GET /api/v1/bp-pf-mapping/by-bp-pf - bpId: $bpId, pfId: $pfId")
        return bpPfMappingService.findByBpIdAndPfId(bpId, pfId)
    }

    // Target BP ID로 매핑 조회
    @GetMapping("/by-target/{targetBpId}")
    fun getMappingsByTargetBp(@PathVariable targetBpId: Long): Flux<BpPfMapping> {
        logger.info("GET /api/v1/bp-pf-mapping/by-target/$targetBpId")
        return bpPfMappingService.findByTargetBpId(targetBpId)
    }

    // Parent BP ID로 매핑 조회
    @GetMapping("/by-parent/{parentBpId}")
    fun getMappingsByParentBp(@PathVariable parentBpId: Long): Flux<BpPfMapping> {
        logger.info("GET /api/v1/bp-pf-mapping/by-parent/$parentBpId")
        return bpPfMappingService.findByParentBpId(parentBpId)
    }

    // 특정 날짜에 유효한 매핑 조회
    @GetMapping("/effective")
    fun getEffectiveMappings(
        @RequestParam bpId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Flux<BpPfMapping> {
        val effectiveDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-pf-mapping/effective - bpId: $bpId, date: $effectiveDate")
        return bpPfMappingService.findEffectiveMappings(bpId, effectiveDate)
    }

    // 매핑 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMapping(@RequestBody bpPfMapping: BpPfMapping): Mono<BpPfMapping> {
        logger.info("POST /api/v1/bp-pf-mapping - Creating mapping for BP: ${bpPfMapping.bpId}, PF: ${bpPfMapping.pfId}")
        return bpPfMappingService.create(bpPfMapping)
    }

    // 매핑 수정
    @PutMapping("/{id}")
    fun updateMapping(
        @PathVariable id: Long,
        @RequestBody bpPfMapping: BpPfMapping
    ): Mono<ResponseEntity<BpPfMapping>> {
        logger.info("PUT /api/v1/bp-pf-mapping/$id - Updating mapping")

        return bpPfMappingService.update(id, bpPfMapping)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매핑 상태 변경
    @PatchMapping("/{id}/status")
    fun updateMappingStatus(
        @PathVariable id: Long,
        @RequestParam status: String,
        @RequestParam(required = false) updatedBy: String?
    ): Mono<ResponseEntity<BpPfMapping>> {
        logger.info("PATCH /api/v1/bp-pf-mapping/$id/status - Updating status to: $status")

        return bpPfMappingService.updateStatus(id, status, updatedBy)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매핑 만료 처리
    @PatchMapping("/{id}/expire")
    fun expireMapping(
        @PathVariable id: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) expiryDate: LocalDate?,
        @RequestParam(required = false) updatedBy: String?
    ): Mono<ResponseEntity<BpPfMapping>> {
        val expDate = expiryDate ?: LocalDate.now()
        logger.info("PATCH /api/v1/bp-pf-mapping/$id/expire - Expiring on: $expDate")

        return bpPfMappingService.expire(id, expDate, updatedBy)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매핑 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMapping(@PathVariable id: Long): Mono<Void> {
        logger.info("DELETE /api/v1/bp-pf-mapping/$id")
        return bpPfMappingService.delete(id)
    }

    // 중복 체크
    @GetMapping("/check/duplicate")
    fun checkDuplicate(
        @RequestParam bpId: Long,
        @RequestParam pfId: Long,
        @RequestParam(required = false) targetBpId: Long?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) effectiveDate: LocalDate
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-pf-mapping/check/duplicate - bpId: $bpId, pfId: $pfId, targetBpId: $targetBpId, effectiveDate: $effectiveDate")

        return bpPfMappingService.existsByBpPfTargetAndDate(bpId, pfId, targetBpId, effectiveDate)
            .map { exists -> mapOf("exists" to exists) }
    }
}