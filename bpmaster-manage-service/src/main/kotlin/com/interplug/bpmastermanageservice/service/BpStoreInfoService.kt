package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.BpStoreInfo
import com.interplug.bpmastermanageservice.repository.BpStoreInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BpStoreInfoService(
    private val storeRepository: BpStoreInfoRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 매장 조회
    fun findAll(): Flux<BpStoreInfo> {
        logger.debug("Finding all stores")
        return storeRepository.findAll()
    }

    // ID로 매장 조회
    fun findById(id: Long): Mono<BpStoreInfo> {
        logger.debug("Finding store by id: $id")
        return storeRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with id: $id")))
    }

    // 매장 코드로 조회
    fun findByStoreCode(storeCode: String): Mono<BpStoreInfo> {
        logger.debug("Finding store by code: $storeCode")
        return storeRepository.findByStoreCode(storeCode)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with code: $storeCode")))
    }

    // BP ID로 매장 조회
    fun findByBpId(bpId: Long): Flux<BpStoreInfo> {
        logger.debug("Finding stores by BP id: $bpId")
        return storeRepository.findByBpId(bpId)
    }

    // 매장 타입별 조회
    fun findByStoreType(storeType: String): Flux<BpStoreInfo> {
        logger.debug("Finding stores by type: $storeType")
        return storeRepository.findByStoreType(storeType)
    }

    // 상태별 조회
    fun findByStatus(status: String): Flux<BpStoreInfo> {
        logger.debug("Finding stores by status: $status")
        return storeRepository.findByStatus(status)
    }

    // BP의 활성 매장 조회
    fun findActiveStoresByBpId(bpId: Long): Flux<BpStoreInfo> {
        logger.debug("Finding active stores for BP id: $bpId")
        return storeRepository.findActiveStoresByBpId(bpId)
    }

    // 매장명으로 검색
    fun searchByStoreName(name: String): Flux<BpStoreInfo> {
        logger.debug("Searching stores by name: $name")
        return storeRepository.searchByStoreName(name)
    }

    // 주소로 검색
    fun searchByAddress(address: String): Flux<BpStoreInfo> {
        logger.debug("Searching stores by address: $address")
        return storeRepository.searchByAddress(address)
    }

    // 매니저명으로 검색
    fun searchByManagerName(managerName: String): Flux<BpStoreInfo> {
        logger.debug("Searching stores by manager name: $managerName")
        return storeRepository.searchByManagerName(managerName)
    }

    // 특정 날짜 기준 운영 중인 매장 조회
    fun findOperatingStoresAsOfDate(date: LocalDate = LocalDate.now()): Flux<BpStoreInfo> {
        logger.debug("Finding operating stores as of date: $date")
        return storeRepository.findOperatingStoresAsOfDate(date)
    }

    // 기간 내 개점한 매장 조회
    fun findStoresOpenedBetween(startDate: LocalDate, endDate: LocalDate): Flux<BpStoreInfo> {
        logger.debug("Finding stores opened between $startDate and $endDate")
        return storeRepository.findStoresOpenedBetween(startDate, endDate)
    }

    // 기간 내 폐점한 매장 조회
    fun findStoresClosedBetween(startDate: LocalDate, endDate: LocalDate): Flux<BpStoreInfo> {
        logger.debug("Finding stores closed between $startDate and $endDate")
        return storeRepository.findStoresClosedBetween(startDate, endDate)
    }

    // BP의 활성 매장 수 조회
    fun countActiveStoresByBpId(bpId: Long): Mono<Long> {
        logger.debug("Counting active stores for BP id: $bpId")
        return storeRepository.countActiveStoresByBpId(bpId)
    }

    // 매장 생성
    @Transactional
    fun create(store: BpStoreInfo): Mono<BpStoreInfo> {
        logger.debug("Creating new store: ${store.storeCode}")

        return storeRepository.existsByStoreCode(store.storeCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("Store with code ${store.storeCode} already exists"))
                } else {
                    val newStore = store.copy(
                        createdDate = LocalDateTime.now()
                    )
                    storeRepository.save(newStore)
                }
            }
            .doOnSuccess { logger.info("Successfully created store: ${it.storeCode}") }
            .doOnError { logger.error("Failed to create store: ${store.storeCode}", it) }
    }

    // 매장 수정
    @Transactional
    fun update(id: Long, updateRequest: BpStoreInfo): Mono<BpStoreInfo> {
        logger.debug("Updating store id: $id")

        return storeRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    bpId = updateRequest.bpId,
                    storeName = updateRequest.storeName,
                    storeType = updateRequest.storeType,
                    address = updateRequest.address,
                    phoneNumber = updateRequest.phoneNumber,
                    managerName = updateRequest.managerName,
                    openingDate = updateRequest.openingDate,
                    closingDate = updateRequest.closingDate,
                    status = updateRequest.status
                )
                storeRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated store id: $id") }
            .doOnError { logger.error("Failed to update store id: $id", it) }
    }

    // 매장 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String): Mono<BpStoreInfo> {
        logger.debug("Updating status for store id: $id to $status")

        return storeRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status
                )
                storeRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for store id: $id to $status") }
    }

    // 매장 폐점 처리
    @Transactional
    fun closeStore(id: Long, closingDate: LocalDate = LocalDate.now()): Mono<BpStoreInfo> {
        logger.debug("Closing store id: $id on date: $closingDate")

        return storeRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    closingDate = closingDate,
                    status = "CLOSED"
                )
                storeRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully closed store id: $id") }
    }

    // 매장 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting store id: $id")

        return storeRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Store not found with id: $id")))
            .flatMap { storeRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted store id: $id") }
            .doOnError { logger.error("Failed to delete store id: $id", it) }
    }

    // 매장 코드 중복 체크
    fun existsByStoreCode(storeCode: String): Mono<Boolean> {
        logger.debug("Checking if store code exists: $storeCode")
        return storeRepository.existsByStoreCode(storeCode)
    }

    // BP별 매장 코드 중복 체크
    fun existsByBpIdAndStoreCode(bpId: Long, storeCode: String): Mono<Boolean> {
        logger.debug("Checking if store code exists for BP id: $bpId, code: $storeCode")
        return storeRepository.existsByBpIdAndStoreCode(bpId, storeCode)
    }
}