package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.BpStoreInfo
import com.interplug.bpmastermanageservice.service.BpStoreInfoService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/bp-store-info")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpStoreInfoController(
    private val storeService: BpStoreInfoService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 매장 조회
    @GetMapping
    fun getAllStores(
        @RequestParam(required = false) bpId: Long?,
        @RequestParam(required = false) storeType: String?,
        @RequestParam(required = false) status: String?
    ): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info - bpId: $bpId, storeType: $storeType, status: $status")

        return when {
            bpId != null && status != null -> storeService.findByBpId(bpId)
                .filter { it.status == status }
            bpId != null -> storeService.findByBpId(bpId)
            storeType != null -> storeService.findByStoreType(storeType)
            status != null -> storeService.findByStatus(status)
            else -> storeService.findAll()
        }
    }

    // ID로 매장 조회
    @GetMapping("/{id}")
    fun getStoreById(@PathVariable id: Long): Mono<ResponseEntity<BpStoreInfo>> {
        logger.info("GET /api/v1/bp-store-info/$id")

        return storeService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매장 코드로 조회
    @GetMapping("/code/{storeCode}")
    fun getStoreByCode(@PathVariable storeCode: String): Mono<ResponseEntity<BpStoreInfo>> {
        logger.info("GET /api/v1/bp-store-info/code/$storeCode")

        return storeService.findByStoreCode(storeCode)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // BP의 활성 매장 조회
    @GetMapping("/active/by-bp/{bpId}")
    fun getActiveStoresByBpId(@PathVariable bpId: Long): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/active/by-bp/$bpId")
        return storeService.findActiveStoresByBpId(bpId)
    }

    // 매장명으로 검색
    @GetMapping("/search/name")
    fun searchByStoreName(@RequestParam name: String): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/search/name - name: $name")
        return storeService.searchByStoreName(name)
    }

    // 주소로 검색
    @GetMapping("/search/address")
    fun searchByAddress(@RequestParam address: String): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/search/address - address: $address")
        return storeService.searchByAddress(address)
    }

    // 매니저명으로 검색
    @GetMapping("/search/manager")
    fun searchByManagerName(@RequestParam managerName: String): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/search/manager - managerName: $managerName")
        return storeService.searchByManagerName(managerName)
    }

    // 특정 날짜 기준 운영 중인 매장 조회
    @GetMapping("/operating")
    fun getOperatingStores(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Flux<BpStoreInfo> {
        val checkDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-store-info/operating - date: $checkDate")
        return storeService.findOperatingStoresAsOfDate(checkDate)
    }

    // 기간 내 개점한 매장 조회
    @GetMapping("/opened-between")
    fun getStoresOpenedBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/opened-between - period: $startDate ~ $endDate")
        return storeService.findStoresOpenedBetween(startDate, endDate)
    }

    // 기간 내 폐점한 매장 조회
    @GetMapping("/closed-between")
    fun getStoresClosedBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): Flux<BpStoreInfo> {
        logger.info("GET /api/v1/bp-store-info/closed-between - period: $startDate ~ $endDate")
        return storeService.findStoresClosedBetween(startDate, endDate)
    }

    // BP의 활성 매장 수 조회
    @GetMapping("/count/active/{bpId}")
    fun countActiveStoresByBpId(@PathVariable bpId: Long): Mono<Map<String, Long>> {
        logger.info("GET /api/v1/bp-store-info/count/active/$bpId")

        return storeService.countActiveStoresByBpId(bpId)
            .map { count -> mapOf("activeStoreCount" to count) }
    }

    // 매장 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createStore(@RequestBody store: BpStoreInfo): Mono<BpStoreInfo> {
        logger.info("POST /api/v1/bp-store-info - Creating store: ${store.storeCode}")
        return storeService.create(store)
    }

    // 매장 수정
    @PutMapping("/{id}")
    fun updateStore(
        @PathVariable id: Long,
        @RequestBody store: BpStoreInfo
    ): Mono<ResponseEntity<BpStoreInfo>> {
        logger.info("PUT /api/v1/bp-store-info/$id - Updating store")

        return storeService.update(id, store)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매장 상태 변경
    @PatchMapping("/{id}/status")
    fun updateStoreStatus(
        @PathVariable id: Long,
        @RequestParam status: String
    ): Mono<ResponseEntity<BpStoreInfo>> {
        logger.info("PATCH /api/v1/bp-store-info/$id/status - Updating status to: $status")

        return storeService.updateStatus(id, status)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매장 폐점 처리
    @PatchMapping("/{id}/close")
    fun closeStore(
        @PathVariable id: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) closingDate: LocalDate?
    ): Mono<ResponseEntity<BpStoreInfo>> {
        val closeDate = closingDate ?: LocalDate.now()
        logger.info("PATCH /api/v1/bp-store-info/$id/close - Closing on: $closeDate")

        return storeService.closeStore(id, closeDate)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    // 매장 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStore(@PathVariable id: Long): Mono<Void> {
        logger.info("DELETE /api/v1/bp-store-info/$id")
        return storeService.delete(id)
    }

    // 매장 코드 중복 체크
    @GetMapping("/check/code/{storeCode}")
    fun checkStoreCode(@PathVariable storeCode: String): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-store-info/check/code/$storeCode")

        return storeService.existsByStoreCode(storeCode)
            .map { exists -> mapOf("exists" to exists) }
    }

    // BP별 매장 코드 중복 체크
    @GetMapping("/check/bp-store")
    fun checkBpStoreCode(
        @RequestParam bpId: Long,
        @RequestParam storeCode: String
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-store-info/check/bp-store - bpId: $bpId, storeCode: $storeCode")

        return storeService.existsByBpIdAndStoreCode(bpId, storeCode)
            .map { exists -> mapOf("exists" to exists) }
    }
}