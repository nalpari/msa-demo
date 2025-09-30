package com.interplug.bpmastermanageservice.service

import com.interplug.bpmastermanageservice.entity.BpContractInfo
import com.interplug.bpmastermanageservice.repository.BpContractInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BpContractInfoService(
    private val bpContractInfoRepository: BpContractInfoRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 모든 계약 조회
    fun findAll(): Flux<BpContractInfo> {
        logger.debug("Finding all contracts")
        return bpContractInfoRepository.findAll()
    }

    // ID로 계약 조회
    fun findById(id: Long): Mono<BpContractInfo> {
        logger.debug("Finding contract by id: $id")
        return bpContractInfoRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with id: $id")))
    }

    // 계약 코드로 조회
    fun findByContractCode(contractCode: String): Mono<BpContractInfo> {
        logger.debug("Finding contract by code: $contractCode")
        return bpContractInfoRepository.findByContractCode(contractCode)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with code: $contractCode")))
    }

    // 계약자 BP ID로 조회
    fun findByContractorBpId(contractorBpId: Long): Flux<BpContractInfo> {
        logger.debug("Finding contracts by contractor BP id: $contractorBpId")
        return bpContractInfoRepository.findByContractorBpId(contractorBpId)
    }

    // 계약 상대방 BP ID로 조회
    fun findByContracteeBpId(contracteeBpId: Long): Flux<BpContractInfo> {
        logger.debug("Finding contracts by contractee BP id: $contracteeBpId")
        return bpContractInfoRepository.findByContracteeBpId(contracteeBpId)
    }

    // PF ID로 조회
    fun findByPfId(pfId: Long): Flux<BpContractInfo> {
        logger.debug("Finding contracts by PF id: $pfId")
        return bpContractInfoRepository.findByPfId(pfId)
    }

    // 계약 타입별 조회
    fun findByContractType(contractType: String): Flux<BpContractInfo> {
        logger.debug("Finding contracts by type: $contractType")
        return bpContractInfoRepository.findByContractType(contractType)
    }

    // 상태별 조회
    fun findByStatus(status: String): Flux<BpContractInfo> {
        logger.debug("Finding contracts by status: $status")
        return bpContractInfoRepository.findByStatus(status)
    }

    // BP ID로 활성 계약 조회 (계약자 또는 계약상대방)
    fun findActiveContractsByBpId(bpId: Long): Flux<BpContractInfo> {
        logger.debug("Finding active contracts for BP id: $bpId")
        return bpContractInfoRepository.findActiveContractsByBpId(bpId)
    }

    // 특정 날짜에 유효한 계약 조회
    fun findEffectiveContracts(date: LocalDate = LocalDate.now()): Flux<BpContractInfo> {
        logger.debug("Finding effective contracts on date: $date")
        return bpContractInfoRepository.findEffectiveContracts(date)
    }

    // 두 BP 간 활성 계약 조회
    fun findActiveContractBetweenBps(
        contractorBpId: Long,
        contracteeBpId: Long,
        pfId: Long,
        date: LocalDate = LocalDate.now()
    ): Mono<BpContractInfo> {
        logger.debug("Finding active contract between BP $contractorBpId and BP $contracteeBpId for PF $pfId")
        return bpContractInfoRepository.findActiveContractBetweenBps(contractorBpId, contracteeBpId, pfId, date)
    }

    // 계약 생성
    @Transactional
    fun create(contract: BpContractInfo): Mono<BpContractInfo> {
        logger.debug("Creating new contract: ${contract.contractCode}")

        return bpContractInfoRepository.existsByContractCode(contract.contractCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("Contract with code ${contract.contractCode} already exists"))
                } else {
                    // 중복 계약 체크
                    bpContractInfoRepository.existsOverlappingContract(
                        contract.contractorBpId,
                        contract.contracteeBpId,
                        contract.pfId,
                        contract.contractStartDate,
                        contract.contractEndDate ?: LocalDate.of(9999, 12, 31)
                    )
                }
            }
            .flatMap { hasOverlapping ->
                if (hasOverlapping) {
                    Mono.error(IllegalArgumentException("Overlapping contract exists for the same period"))
                } else {
                    val newContract = contract.copy(
                        createdDate = LocalDateTime.now()
                    )
                    bpContractInfoRepository.save(newContract)
                }
            }
            .doOnSuccess { logger.info("Successfully created contract: ${it.contractCode}") }
            .doOnError { logger.error("Failed to create contract: ${contract.contractCode}", it) }
    }

    // 계약 수정
    @Transactional
    fun update(id: Long, updateRequest: BpContractInfo): Mono<BpContractInfo> {
        logger.debug("Updating contract id: $id")

        return bpContractInfoRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    contractorBpId = updateRequest.contractorBpId,
                    contracteeBpId = updateRequest.contracteeBpId,
                    contractType = updateRequest.contractType,
                    pfId = updateRequest.pfId,
                    contractStartDate = updateRequest.contractStartDate,
                    contractEndDate = updateRequest.contractEndDate,
                    contractTerms = updateRequest.contractTerms,
                    feeRate = updateRequest.feeRate,
                    status = updateRequest.status
                )
                bpContractInfoRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated contract id: $id") }
            .doOnError { logger.error("Failed to update contract id: $id", it) }
    }

    // 계약 상태 변경
    @Transactional
    fun updateStatus(id: Long, status: String): Mono<BpContractInfo> {
        logger.debug("Updating status for contract id: $id to $status")

        return bpContractInfoRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status
                )
                bpContractInfoRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated status for contract id: $id to $status") }
    }

    // 계약 종료
    @Transactional
    fun terminateContract(id: Long, endDate: LocalDate = LocalDate.now()): Mono<BpContractInfo> {
        logger.debug("Terminating contract id: $id on date: $endDate")

        return bpContractInfoRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    contractEndDate = endDate,
                    status = "TERMINATED"
                )
                bpContractInfoRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully terminated contract id: $id") }
    }

    // 계약 삭제
    @Transactional
    fun delete(id: Long): Mono<Void> {
        logger.debug("Deleting contract id: $id")

        return bpContractInfoRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Contract not found with id: $id")))
            .flatMap { bpContractInfoRepository.delete(it) }
            .doOnSuccess { logger.info("Successfully deleted contract id: $id") }
            .doOnError { logger.error("Failed to delete contract id: $id", it) }
    }

    // 계약 코드 중복 체크
    fun existsByContractCode(contractCode: String): Mono<Boolean> {
        logger.debug("Checking if contract code exists: $contractCode")
        return bpContractInfoRepository.existsByContractCode(contractCode)
    }

    // 중복 계약 체크
    fun hasOverlappingContract(
        contractorBpId: Long,
        contracteeBpId: Long,
        pfId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Mono<Boolean> {
        logger.debug("Checking for overlapping contracts")
        return bpContractInfoRepository.existsOverlappingContract(
            contractorBpId,
            contracteeBpId,
            pfId,
            startDate,
            endDate
        )
    }
}