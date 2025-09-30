package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.BpMaster
import com.interplug.bpmastermanageservice.repository.BpMasterRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 BP 조회
    fun findAll(): Flux<BpMaster> {
        logger.debug("Finding all business partners")
        return bpMasterRepository.findAll()
    }

    // ID로 BP 조회
    fun findById(id: Long): Mono<BpMaster> {
        logger.debug("Finding business partner by id: $id")
        return bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
    }

    // BP 코드로 조회
    fun findByCode(bpCode: String): Mono<BpMaster> {
        logger.debug("Finding business partner by code: $bpCode")
        return bpMasterRepository.findByBpCode(bpCode)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with code: $bpCode")))
    }

    // 타입별 BP 조회
    fun findByType(bpType: String): Flux<BpMaster> {
        logger.debug("Finding business partners by type: $bpType")
        return bpMasterRepository.findByBpType(bpType)
    }

    // 활성화된 BP만 조회
    fun findAllActive(): Flux<BpMaster> {
        logger.debug("Finding all active business partners")
        return bpMasterRepository.findAllActive()
    }

    // 타입별 활성화된 BP 조회
    fun findActiveByType(bpType: String): Flux<BpMaster> {
        logger.debug("Finding active business partners by type: $bpType")
        return bpMasterRepository.findActiveByType(bpType)
    }

    // BP 이름으로 검색
    fun searchByName(bpName: String): Flux<BpMaster> {
        logger.debug("Searching business partners by name: $bpName")
        return bpMasterRepository.searchByBpName(bpName)
    }

    // Primary PF Code로 조회
    fun findByPrimaryPfCode(pfCode: String): Flux<BpMaster> {
        logger.debug("Finding business partners by primary PF code: $pfCode")
        return bpMasterRepository.findByPrimaryPfCode(pfCode)
    }

    // BP 생성
    @Transactional
    fun create(bpMaster: BpMaster): Mono<BpMaster> {
        logger.debug("Creating new business partner: ${bpMaster.bpCode}")

        return bpMasterRepository.existsByBpCode(bpMaster.bpCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("Business partner with code ${bpMaster.bpCode} already exists"))
                } else {
                    bpMaster.businessRegNo?.let { regNo ->
                        bpMasterRepository.existsByBusinessRegNo(regNo)
                    } ?: Mono.just(false)
                }
            }
            .flatMap { businessRegNoExists ->
                if (businessRegNoExists) {
                    Mono.error(IllegalArgumentException("Business partner with registration number ${bpMaster.businessRegNo} already exists"))
                } else {
                    val newBp = bpMaster.copy(
                        createdDate = LocalDateTime.now(),
                        updatedDate = LocalDateTime.now()
                    )
                    bpMasterRepository.save(newBp)
                }
            }
            .doOnSuccess { logger.info("Successfully created business partner: ${it.bpCode}") }
            .doOnError { logger.error("Failed to create business partner: ${bpMaster.bpCode}", it) }
    }

    // BP 수정
    @Transactional
    fun update(id: Long, updateRequest: BpMaster): Mono<BpMaster> {
        logger.debug("Updating business partner id: $id")

        return bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    bpName = updateRequest.bpName,
                    bpType = updateRequest.bpType,
                    businessRegNo = updateRequest.businessRegNo,
                    representativeName = updateRequest.representativeName,
                    address = updateRequest.address,
                    phoneNumber = updateRequest.phoneNumber,
                    email = updateRequest.email,
                    primaryPfCode = updateRequest.primaryPfCode,
                    status = updateRequest.status,
                    erpUsageFee = updateRequest.erpUsageFee,
                    commissionRate = updateRequest.commissionRate,
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updateRequest.updatedBy
                )
                bpMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated business partner id: $id") }
            .doOnError { logger.error("Failed to update business partner id: $id", it) }
    }

    // BP 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String, updatedBy: String? = null): Mono<BpMaster> {
        logger.debug("Updating status for business partner id: $id to $status")

        return bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status,
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updatedBy
                )
                bpMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for business partner id: $id to $status") }
    }

    // BP 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting business partner id: $id")

        return bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
            .flatMap { bpMasterRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted business partner id: $id") }
            .doOnError { logger.error("Failed to delete business partner id: $id", it) }
    }

    // 사업자등록번호 중복 체크
    fun existsByBusinessRegNo(businessRegNo: String): Mono<Boolean> {
        logger.debug("Checking if business registration number exists: $businessRegNo")
        return bpMasterRepository.existsByBusinessRegNo(businessRegNo)
    }

    // BP 코드 중복 체크
    fun existsByCode(bpCode: String): Mono<Boolean> {
        logger.debug("Checking if business partner code exists: $bpCode")
        return bpMasterRepository.existsByBpCode(bpCode)
    }
}