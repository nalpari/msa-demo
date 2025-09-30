package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.BpContractInfo
import com.interplug.bpmastermanageservice.service.BpContractInfoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Tag(name = "BP Contract Info", description = "Business Partner Contract Information Management APIs")
@RestController
@RequestMapping("/api/v1/bp-contract-info")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpContractInfoController(
    private val contractService: BpContractInfoService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Get all contracts",
        description = "Retrieve all contracts with optional filtering by contractor, contractee, platform, type, or status"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved contracts"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping
    fun getAllContracts(
        @Parameter(description = "Filter by contractor BP ID")
        @RequestParam(required = false) contractorBpId: Long?,
        @Parameter(description = "Filter by contractee BP ID")
        @RequestParam(required = false) contracteeBpId: Long?,
        @Parameter(description = "Filter by platform ID")
        @RequestParam(required = false) pfId: Long?,
        @Parameter(description = "Filter by contract type")
        @RequestParam(required = false) contractType: String?,
        @Parameter(description = "Filter by contract status (ACTIVE, EXPIRED, TERMINATED)")
        @RequestParam(required = false) status: String?
    ): Flux<BpContractInfo> {
        logger.info("GET /api/v1/bp-contract-info - contractorBpId: $contractorBpId, contracteeBpId: $contracteeBpId, pfId: $pfId, type: $contractType, status: $status")

        return when {
            contractorBpId != null && status != null -> contractService.findByContractorBpId(contractorBpId)
                .filter { it.status == status }
            contractorBpId != null -> contractService.findByContractorBpId(contractorBpId)
            contracteeBpId != null -> contractService.findByContracteeBpId(contracteeBpId)
            pfId != null -> contractService.findByPfId(pfId)
            contractType != null -> contractService.findByContractType(contractType)
            status != null -> contractService.findByStatus(status)
            else -> contractService.findAll()
        }
    }

    @Operation(
        summary = "Get contract by ID",
        description = "Retrieve a specific contract by its unique identifier"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Contract found"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/{id}")
    fun getContractById(
        @Parameter(description = "Contract ID", required = true)
        @PathVariable id: Long
    ): Mono<ResponseEntity<BpContractInfo>> {
        logger.info("GET /api/v1/bp-contract-info/$id")

        return contractService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Get contract by code",
        description = "Retrieve a specific contract by its unique contract code"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Contract found"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/code/{contractCode}")
    fun getContractByCode(
        @Parameter(description = "Contract code", required = true)
        @PathVariable contractCode: String
    ): Mono<ResponseEntity<BpContractInfo>> {
        logger.info("GET /api/v1/bp-contract-info/code/$contractCode")

        return contractService.findByContractCode(contractCode)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Get active contracts by BP ID",
        description = "Retrieve all active contracts for a specific Business Partner"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved active contracts"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/active/by-bp/{bpId}")
    fun getActiveContractsByBpId(
        @Parameter(description = "Business Partner ID", required = true)
        @PathVariable bpId: Long
    ): Flux<BpContractInfo> {
        logger.info("GET /api/v1/bp-contract-info/active/by-bp/$bpId")
        return contractService.findActiveContractsByBpId(bpId)
    }

    @Operation(
        summary = "Get effective contracts",
        description = "Retrieve all contracts effective on a specific date (defaults to today)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved effective contracts"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/effective")
    fun getEffectiveContracts(
        @Parameter(description = "Date to check for effective contracts (defaults to today)", example = "2024-01-01")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Flux<BpContractInfo> {
        val effectiveDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-contract-info/effective - date: $effectiveDate")
        return contractService.findEffectiveContracts(effectiveDate)
    }

    @Operation(
        summary = "Get active contract between BPs",
        description = "Retrieve active contract between two Business Partners on a platform for a specific date"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Active contract found"),
            ApiResponse(responseCode = "404", description = "No active contract found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/active-between")
    fun getActiveContractBetweenBps(
        @Parameter(description = "Contractor BP ID", required = true)
        @RequestParam contractorBpId: Long,
        @Parameter(description = "Contractee BP ID", required = true)
        @RequestParam contracteeBpId: Long,
        @Parameter(description = "Platform ID", required = true)
        @RequestParam pfId: Long,
        @Parameter(description = "Date to check (defaults to today)", example = "2024-01-01")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Mono<ResponseEntity<BpContractInfo>> {
        val checkDate = date ?: LocalDate.now()
        logger.info("GET /api/v1/bp-contract-info/active-between - contractor: $contractorBpId, contractee: $contracteeBpId, pf: $pfId, date: $checkDate")

        return contractService.findActiveContractBetweenBps(contractorBpId, contracteeBpId, pfId, checkDate)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Create contract",
        description = "Create a new contract between Business Partners"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Contract created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "409", description = "Contract already exists or overlapping period"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createContract(
        @Parameter(description = "Contract data to create", required = true)
        @RequestBody contract: BpContractInfo
    ): Mono<BpContractInfo> {
        logger.info("POST /api/v1/bp-contract-info - Creating contract: ${contract.contractCode}")
        return contractService.create(contract)
    }

    @Operation(
        summary = "Update contract",
        description = "Update an existing contract with new information"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Contract updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "409", description = "Conflict with existing contracts"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PutMapping("/{id}")
    fun updateContract(
        @Parameter(description = "Contract ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated contract data", required = true)
        @RequestBody contract: BpContractInfo
    ): Mono<ResponseEntity<BpContractInfo>> {
        logger.info("PUT /api/v1/bp-contract-info/$id - Updating contract")

        return contractService.update(id, contract)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Update contract status",
        description = "Change the status of a contract (ACTIVE, EXPIRED, TERMINATED)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Status updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid status value"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PatchMapping("/{id}/status")
    fun updateContractStatus(
        @Parameter(description = "Contract ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "New status (ACTIVE, EXPIRED, TERMINATED)", required = true, example = "ACTIVE")
        @RequestParam status: String
    ): Mono<ResponseEntity<BpContractInfo>> {
        logger.info("PATCH /api/v1/bp-contract-info/$id/status - Updating status to: $status")

        return contractService.updateStatus(id, status)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Terminate contract",
        description = "Terminate a contract on a specific date"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Contract terminated successfully"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PatchMapping("/{id}/terminate")
    fun terminateContract(
        @Parameter(description = "Contract ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Termination date (defaults to today)", example = "2024-01-01")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): Mono<ResponseEntity<BpContractInfo>> {
        val terminationDate = endDate ?: LocalDate.now()
        logger.info("PATCH /api/v1/bp-contract-info/$id/terminate - Terminating on: $terminationDate")

        return contractService.terminateContract(id, terminationDate)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Delete contract",
        description = "Delete a contract by ID. This operation is permanent."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Contract deleted successfully"),
            ApiResponse(responseCode = "404", description = "Contract not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteContract(
        @Parameter(description = "Contract ID", required = true)
        @PathVariable id: Long
    ): Mono<Void> {
        logger.info("DELETE /api/v1/bp-contract-info/$id")
        return contractService.delete(id)
    }

    @Operation(
        summary = "Check contract code duplication",
        description = "Check if a contract code already exists in the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Check result returned",
                content = [Content(schema = Schema(example = "{\"exists\": false}"))]
            ),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/check/code/{contractCode}")
    fun checkContractCode(
        @Parameter(description = "Contract code to check", required = true, example = "CONT001")
        @PathVariable contractCode: String
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-contract-info/check/code/$contractCode")

        return contractService.existsByContractCode(contractCode)
            .map { exists -> mapOf("exists" to exists) }
    }

    @Operation(
        summary = "Check overlapping contracts",
        description = "Check if there are any overlapping contracts for the specified period"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Check result returned",
                content = [Content(schema = Schema(example = "{\"hasOverlapping\": false}"))]
            ),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/check/overlapping")
    fun checkOverlappingContract(
        @Parameter(description = "Contractor BP ID", required = true)
        @RequestParam contractorBpId: Long,
        @Parameter(description = "Contractee BP ID", required = true)
        @RequestParam contracteeBpId: Long,
        @Parameter(description = "Platform ID", required = true)
        @RequestParam pfId: Long,
        @Parameter(description = "Contract start date", required = true, example = "2024-01-01")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "Contract end date", required = true, example = "2024-12-31")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-contract-info/check/overlapping - contractor: $contractorBpId, contractee: $contracteeBpId, pf: $pfId, period: $startDate ~ $endDate")

        return contractService.hasOverlappingContract(contractorBpId, contracteeBpId, pfId, startDate, endDate)
            .map { exists -> mapOf("hasOverlapping" to exists) }
    }
}