package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.PfCodeMaster
import com.interplug.bpmastermanageservice.service.PfCodeMasterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Tag(name = "PF Code Master", description = "Platform Code Master Data Management APIs")
@RestController
@RequestMapping("/api/v1/pf-code-master")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class PfCodeMasterController(
    private val pfCodeMasterService: PfCodeMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Get all Platform codes",
        description = "Retrieve all Platform codes with optional filtering by status and attributes"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved Platform codes"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping
    fun getAllPfCodes(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) hasMasterData: String?,
        @RequestParam(required = false) canOwnStores: String?,
        @RequestParam(required = false) canFranchise: String?,
        @RequestParam(required = false) billingCapable: String?
    ): Flux<PfCodeMaster> {
        logger.info("GET /api/v1/pf-code-master - status: $status, hasMasterData: $hasMasterData, canOwnStores: $canOwnStores, canFranchise: $canFranchise, billingCapable: $billingCapable")

        return when {
            status == "ACTIVE" && (hasMasterData != null || canOwnStores != null || canFranchise != null || billingCapable != null) ->
                pfCodeMasterService.findActiveWithAttributes(hasMasterData, canOwnStores, canFranchise, billingCapable)
            status == "ACTIVE" -> pfCodeMasterService.findAllActive()
            status != null -> pfCodeMasterService.findByStatus(status)
            hasMasterData == "Y" -> pfCodeMasterService.findWithMasterData()
            canOwnStores == "Y" -> pfCodeMasterService.findCanOwnStores()
            canFranchise == "Y" -> pfCodeMasterService.findCanFranchise()
            billingCapable == "Y" -> pfCodeMasterService.findBillingCapable()
            else -> pfCodeMasterService.findAll()
        }
    }

    // ID로 PF 코드 조회
    @GetMapping("/{id}")
    fun getPfCodeById(@PathVariable id: Long): Mono<ResponseEntity<PfCodeMaster>> {
        logger.info("GET /api/v1/pf-code-master/$id")

        return pfCodeMasterService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // PF 코드로 조회
    @GetMapping("/code/{pfCode}")
    fun getPfCodeByCode(@PathVariable pfCode: String): Mono<ResponseEntity<PfCodeMaster>> {
        logger.info("GET /api/v1/pf-code-master/code/$pfCode")

        return pfCodeMasterService.findByCode(pfCode)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // PF 이름으로 검색
    @GetMapping("/search")
    fun searchPfCodes(@RequestParam name: String): Flux<PfCodeMaster> {
        logger.info("GET /api/v1/pf-code-master/search - name: $name")
        return pfCodeMasterService.searchByName(name)
    }

    // 특정 속성을 가진 활성 PF 조회
    @GetMapping("/active/with-attributes")
    fun getActiveWithAttributes(
        @RequestParam(required = false) hasMasterData: String?,
        @RequestParam(required = false) canOwnStores: String?,
        @RequestParam(required = false) canFranchise: String?,
        @RequestParam(required = false) billingCapable: String?
    ): Flux<PfCodeMaster> {
        logger.info("GET /api/v1/pf-code-master/active/with-attributes - hasMasterData: $hasMasterData, canOwnStores: $canOwnStores, canFranchise: $canFranchise, billingCapable: $billingCapable")
        return pfCodeMasterService.findActiveWithAttributes(hasMasterData, canOwnStores, canFranchise, billingCapable)
    }

    // PF 코드 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPfCode(@RequestBody pfCodeMaster: PfCodeMaster): Mono<PfCodeMaster> {
        logger.info("POST /api/v1/pf-code-master - Creating PF code: ${pfCodeMaster.pfCode}")
        return pfCodeMasterService.create(pfCodeMaster)
    }

    // PF 코드 수정
    @PutMapping("/{id}")
    fun updatePfCode(
        @PathVariable id: Long,
        @RequestBody pfCodeMaster: PfCodeMaster
    ): Mono<ResponseEntity<PfCodeMaster>> {
        logger.info("PUT /api/v1/pf-code-master/$id - Updating PF code")

        return pfCodeMasterService.update(id, pfCodeMaster)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // PF 코드 상태 변경
    @PatchMapping("/{id}/status")
    fun updatePfCodeStatus(
        @PathVariable id: Long,
        @RequestParam status: String
    ): Mono<ResponseEntity<PfCodeMaster>> {
        logger.info("PATCH /api/v1/pf-code-master/$id/status - Updating status to: $status")

        return pfCodeMasterService.updateStatus(id, status)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // PF 코드 속성 업데이트
    @PatchMapping("/{id}/attributes")
    fun updatePfCodeAttributes(
        @PathVariable id: Long,
        @RequestParam(required = false) hasMasterData: String?,
        @RequestParam(required = false) canOwnStores: String?,
        @RequestParam(required = false) canFranchise: String?,
        @RequestParam(required = false) billingCapable: String?
    ): Mono<ResponseEntity<PfCodeMaster>> {
        logger.info("PATCH /api/v1/pf-code-master/$id/attributes - Updating attributes")

        return pfCodeMasterService.updateAttributes(id, hasMasterData, canOwnStores, canFranchise, billingCapable)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // PF 코드 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePfCode(@PathVariable id: Long): Mono<Void> {
        logger.info("DELETE /api/v1/pf-code-master/$id")
        return pfCodeMasterService.delete(id)
    }

    // PF 코드 중복 체크
    @GetMapping("/check/code/{pfCode}")
    fun checkPfCode(@PathVariable pfCode: String): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/pf-code-master/check/code/$pfCode")

        return pfCodeMasterService.existsByCode(pfCode)
            .map { exists -> mapOf("exists" to exists) }
    }
}