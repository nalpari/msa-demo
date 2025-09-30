package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.BpPfMapping
import com.interplug.bpmastermanageservice.repository.BpPfMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BpPfMappingService(
    private val bpPfMappingRepository: BpPfMappingRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 매핑 조회
    fun findAll(): Flux<BpPfMapping> {
        logger.debug("Finding all BP-PF mappings")
        return bpPfMappingRepository.findAll()
    }

    // ID로 매핑 조회
    fun findById(id: Long): Mono<BpPfMapping> {
        logger.debug("Finding BP-PF mapping by id: $id")
        return bpPfMappingRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP-PF mapping not found with id: $id")))
    }

    // BP ID로 매핑 조회
    fun findByBpId(bpId: Long): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by BP id: $bpId")
        return bpPfMappingRepository.findByBpId(bpId)
    }

    // PF ID로 매핑 조회
    fun findByPfId(pfId: Long): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by PF id: $pfId")
        return bpPfMappingRepository.findByPfId(pfId)
    }

    // Target BP ID로 매핑 조회
    fun findByTargetBpId(targetBpId: Long): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by target BP id: $targetBpId")
        return bpPfMappingRepository.findByTargetBpId(targetBpId)
    }

    // Parent BP ID로 매핑 조회
    fun findByParentBpId(parentBpId: Long): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by parent BP id: $parentBpId")
        return bpPfMappingRepository.findByParentBpId(parentBpId)
    }

    // BP ID와 상태로 조회
    fun findByBpIdAndStatus(bpId: Long, status: String): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by BP id: $bpId and status: $status")
        return bpPfMappingRepository.findByBpIdAndStatus(bpId, status)
    }

    // 특정 날짜에 유효한 매핑 조회
    fun findEffectiveMappings(bpId: Long, date: LocalDate = LocalDate.now()): Flux<BpPfMapping> {
        logger.debug("Finding effective BP-PF mappings for BP id: $bpId on date: $date")
        return bpPfMappingRepository.findEffectiveMappings(bpId, date)
    }

    // BP와 PF 조합으로 조회
    fun findByBpIdAndPfId(bpId: Long, pfId: Long): Flux<BpPfMapping> {
        logger.debug("Finding BP-PF mappings by BP id: $bpId and PF id: $pfId")
        return bpPfMappingRepository.findByBpIdAndPfId(bpId, pfId)
    }

    // 매핑 생성
    @Transactional
    fun create(bpPfMapping: BpPfMapping): Mono<BpPfMapping> {
        logger.debug("Creating new BP-PF mapping for BP: ${bpPfMapping.bpId}, PF: ${bpPfMapping.pfId}")

        return bpPfMappingRepository.existsByBpIdAndPfIdAndTargetBpIdAndEffectiveDate(
            bpPfMapping.bpId,
            bpPfMapping.pfId,
            bpPfMapping.targetBpId,
            bpPfMapping.effectiveDate
        ).flatMap { exists ->
            if (exists) {
                Mono.error(IllegalArgumentException("BP-PF mapping already exists for the given combination"))
            } else {
                val newMapping = bpPfMapping.copy(
                    createdDate = LocalDateTime.now(),
                    updatedDate = LocalDateTime.now()
                )
                bpPfMappingRepository.save(newMapping)
            }
        }
        .doOnSuccess { logger.info("Successfully created BP-PF mapping: ${it.mappingId}") }
        .doOnError { logger.error("Failed to create BP-PF mapping", it) }
    }

    // 매핑 수정
    @Transactional
    fun update(id: Long, updateRequest: BpPfMapping): Mono<BpPfMapping> {
        logger.debug("Updating BP-PF mapping id: $id")

        return bpPfMappingRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP-PF mapping not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    bpId = updateRequest.bpId,
                    pfId = updateRequest.pfId,
                    targetBpId = updateRequest.targetBpId,
                    parentBpId = updateRequest.parentBpId,
                    mappingType = updateRequest.mappingType,
                    effectiveDate = updateRequest.effectiveDate,
                    expiryDate = updateRequest.expiryDate,
                    status = updateRequest.status,
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updateRequest.updatedBy
                )
                bpPfMappingRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated BP-PF mapping id: $id") }
            .doOnError { logger.error("Failed to update BP-PF mapping id: $id", it) }
    }

    // 매핑 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String, updatedBy: String? = null): Mono<BpPfMapping> {
        logger.debug("Updating status for BP-PF mapping id: $id to $status")

        return bpPfMappingRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP-PF mapping not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status,
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updatedBy
                )
                bpPfMappingRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for BP-PF mapping id: $id to $status") }
    }

    // 매핑 만료 처리
    @Transactional
    fun expire(id: Long, expiryDate: LocalDate = LocalDate.now(), updatedBy: String? = null): Mono<BpPfMapping> {
        logger.debug("Expiring BP-PF mapping id: $id on date: $expiryDate")

        return bpPfMappingRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP-PF mapping not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    expiryDate = expiryDate,
                    status = "EXPIRED",
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updatedBy
                )
                bpPfMappingRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully expired BP-PF mapping id: $id") }
    }

    // 매핑 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting BP-PF mapping id: $id")

        return bpPfMappingRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP-PF mapping not found with id: $id")))
            .flatMap { bpPfMappingRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted BP-PF mapping id: $id") }
            .doOnError { logger.error("Failed to delete BP-PF mapping id: $id", it) }
    }

    // 중복 체크
    fun existsByBpPfTargetAndDate(
        bpId: Long,
        pfId: Long,
        targetBpId: Long?,
        effectiveDate: LocalDate
    ): Mono<Boolean> {
        logger.debug("Checking if BP-PF mapping exists for BP: $bpId, PF: $pfId, Target: $targetBpId, Date: $effectiveDate")
        return bpPfMappingRepository.existsByBpIdAndPfIdAndTargetBpIdAndEffectiveDate(bpId, pfId, targetBpId, effectiveDate)
    }
}