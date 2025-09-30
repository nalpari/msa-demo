package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.PfCodeMaster
import com.interplug.bpmastermanageservice.repository.PfCodeMasterRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PfCodeMasterService(
    private val pfCodeMasterRepository: PfCodeMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 PF 코드 조회
    fun findAll(): Flux<PfCodeMaster> {
        logger.debug("Finding all PF codes")
        return pfCodeMasterRepository.findAll()
    }

    // ID로 PF 코드 조회
    fun findById(id: Long): Mono<PfCodeMaster> {
        logger.debug("Finding PF code by id: $id")
        return pfCodeMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with id: $id")))
    }

    // PF 코드로 조회
    fun findByCode(pfCode: String): Mono<PfCodeMaster> {
        logger.debug("Finding PF code by code: $pfCode")
        return pfCodeMasterRepository.findByPfCode(pfCode)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with code: $pfCode")))
    }

    // 활성화된 PF 코드만 조회
    fun findAllActive(): Flux<PfCodeMaster> {
        logger.debug("Finding all active PF codes")
        return pfCodeMasterRepository.findAllActive()
    }

    // PF 이름으로 검색
    fun searchByName(pfName: String): Flux<PfCodeMaster> {
        logger.debug("Searching PF codes by name: $pfName")
        return pfCodeMasterRepository.searchByPfName(pfName)
    }

    // 상태별 조회
    fun findByStatus(status: String): Flux<PfCodeMaster> {
        logger.debug("Finding PF codes by status: $status")
        return pfCodeMasterRepository.findByStatus(status)
    }

    // 특정 속성을 가진 활성 PF 조회
    fun findActiveWithAttributes(
        hasMasterData: String? = null,
        canOwnStores: String? = null,
        canFranchise: String? = null,
        billingCapable: String? = null
    ): Flux<PfCodeMaster> {
        logger.debug("Finding active PF codes with attributes - hasMasterData: $hasMasterData, canOwnStores: $canOwnStores, canFranchise: $canFranchise, billingCapable: $billingCapable")
        return pfCodeMasterRepository.findActiveWithAttributes(hasMasterData, canOwnStores, canFranchise, billingCapable)
    }

    // 마스터 데이터 보유 PF 조회
    fun findWithMasterData(): Flux<PfCodeMaster> {
        logger.debug("Finding PF codes with master data")
        return pfCodeMasterRepository.findByHasMasterData("Y")
    }

    // 매장 소유 가능 PF 조회
    fun findCanOwnStores(): Flux<PfCodeMaster> {
        logger.debug("Finding PF codes that can own stores")
        return pfCodeMasterRepository.findByCanOwnStores("Y")
    }

    // 프랜차이즈 가능 PF 조회
    fun findCanFranchise(): Flux<PfCodeMaster> {
        logger.debug("Finding PF codes that can franchise")
        return pfCodeMasterRepository.findByCanFranchise("Y")
    }

    // 빌링 가능 PF 조회
    fun findBillingCapable(): Flux<PfCodeMaster> {
        logger.debug("Finding PF codes that are billing capable")
        return pfCodeMasterRepository.findByBillingCapable("Y")
    }

    // PF 코드 생성
    @Transactional
    fun create(pfCodeMaster: PfCodeMaster): Mono<PfCodeMaster> {
        logger.debug("Creating new PF code: ${pfCodeMaster.pfCode}")

        return pfCodeMasterRepository.existsByPfCode(pfCodeMaster.pfCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("PF code ${pfCodeMaster.pfCode} already exists"))
                } else {
                    val newPf = pfCodeMaster.copy(
                        createdDate = LocalDateTime.now()
                    )
                    pfCodeMasterRepository.save(newPf)
                }
            }
            .doOnSuccess { logger.info("Successfully created PF code: ${it.pfCode}") }
            .doOnError { logger.error("Failed to create PF code: ${pfCodeMaster.pfCode}", it) }
    }

    // PF 코드 수정
    @Transactional
    fun update(id: Long, updateRequest: PfCodeMaster): Mono<PfCodeMaster> {
        logger.debug("Updating PF code id: $id")

        return pfCodeMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    pfName = updateRequest.pfName,
                    pfDescription = updateRequest.pfDescription,
                    hasMasterData = updateRequest.hasMasterData,
                    canOwnStores = updateRequest.canOwnStores,
                    canFranchise = updateRequest.canFranchise,
                    billingCapable = updateRequest.billingCapable,
                    status = updateRequest.status
                )
                pfCodeMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated PF code id: $id") }
            .doOnError { logger.error("Failed to update PF code id: $id", it) }
    }

    // PF 코드 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String): Mono<PfCodeMaster> {
        logger.debug("Updating status for PF code id: $id to $status")

        return pfCodeMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status
                )
                pfCodeMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for PF code id: $id to $status") }
    }

    // PF 코드 속성 업데이트
    @Transactional
    fun updateAttributes(
        id: Long,
        hasMasterData: String? = null,
        canOwnStores: String? = null,
        canFranchise: String? = null,
        billingCapable: String? = null
    ): Mono<PfCodeMaster> {
        logger.debug("Updating attributes for PF code id: $id")

        return pfCodeMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    hasMasterData = hasMasterData ?: existing.hasMasterData,
                    canOwnStores = canOwnStores ?: existing.canOwnStores,
                    canFranchise = canFranchise ?: existing.canFranchise,
                    billingCapable = billingCapable ?: existing.billingCapable
                )
                pfCodeMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated attributes for PF code id: $id") }
    }

    // PF 코드 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting PF code id: $id")

        return pfCodeMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("PF code not found with id: $id")))
            .flatMap { pfCodeMasterRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted PF code id: $id") }
            .doOnError { logger.error("Failed to delete PF code id: $id", it) }
    }

    // PF 코드 중복 체크
    fun existsByCode(pfCode: String): Mono<Boolean> {
        logger.debug("Checking if PF code exists: $pfCode")
        return pfCodeMasterRepository.existsByPfCode(pfCode)
    }
}