package com.interplug.bpmastermanageservice.controller

import com.interplug.bpmastermanageservice.entity.BpMaster
import com.interplug.bpmastermanageservice.service.BpMasterService
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

@Tag(name = "BP Master", description = "Business Partner Master Data Management APIs")
@RestController
@RequestMapping("/api/v1/bp-master")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Get all Business Partners",
        description = "Retrieve all Business Partners with optional filtering by status, type, or primary PF code"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved Business Partners"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping
    fun getAllBpMasters(
        @Parameter(description = "Filter by status (e.g., ACTIVE, INACTIVE)")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Filter by BP type (e.g., VENDOR, CUSTOMER)")
        @RequestParam(required = false) type: String?,
        @Parameter(description = "Filter by primary PF code")
        @RequestParam(required = false) primaryPfCode: String?
    ): Flux<BpMaster> {
        logger.info("GET /api/v1/bp-master - status: $status, type: $type, primaryPfCode: $primaryPfCode")

        return when {
            status == "ACTIVE" && type != null -> bpMasterService.findActiveByType(type)
            status == "ACTIVE" -> bpMasterService.findAllActive()
            type != null -> bpMasterService.findByType(type)
            primaryPfCode != null -> bpMasterService.findByPrimaryPfCode(primaryPfCode)
            else -> bpMasterService.findAll()
        }
    }

    @Operation(
        summary = "Get Business Partner by ID",
        description = "Retrieve a specific Business Partner by its unique identifier"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Business Partner found"),
            ApiResponse(responseCode = "404", description = "Business Partner not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/{id}")
    fun getBpMasterById(
        @Parameter(description = "Business Partner ID", required = true)
        @PathVariable id: Long
    ): Mono<ResponseEntity<BpMaster>> {
        logger.info("GET /api/v1/bp-master/$id")

        return bpMasterService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Get Business Partner by Code",
        description = "Retrieve a specific Business Partner by its unique code"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Business Partner found"),
            ApiResponse(responseCode = "404", description = "Business Partner not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/code/{code}")
    fun getBpMasterByCode(
        @Parameter(description = "Business Partner Code", required = true)
        @PathVariable code: String
    ): Mono<ResponseEntity<BpMaster>> {
        logger.info("GET /api/v1/bp-master/code/$code")

        return bpMasterService.findByCode(code)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Search Business Partners by name",
        description = "Search Business Partners by name with partial matching"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/search")
    fun searchBpMasters(
        @Parameter(description = "Business Partner name to search", required = true, example = "테스트")
        @RequestParam name: String
    ): Flux<BpMaster> {
        logger.info("GET /api/v1/bp-master/search - name: $name")
        return bpMasterService.searchByName(name)
    }

    @Operation(
        summary = "Create Business Partner",
        description = "Create a new Business Partner with the provided information"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Business Partner created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "409", description = "Business Partner already exists"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBpMaster(
        @Parameter(description = "Business Partner data to create", required = true)
        @RequestBody bpMaster: BpMaster
    ): Mono<BpMaster> {
        logger.info("POST /api/v1/bp-master - Creating BP: ${bpMaster.bpCode}")
        return bpMasterService.create(bpMaster)
    }

    @Operation(
        summary = "Update Business Partner",
        description = "Update an existing Business Partner with new information"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Business Partner updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "404", description = "Business Partner not found"),
            ApiResponse(responseCode = "409", description = "Conflict with existing data"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PutMapping("/{id}")
    fun updateBpMaster(
        @Parameter(description = "Business Partner ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated Business Partner data", required = true)
        @RequestBody bpMaster: BpMaster
    ): Mono<ResponseEntity<BpMaster>> {
        logger.info("PUT /api/v1/bp-master/$id - Updating BP")

        return bpMasterService.update(id, bpMaster)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Update Business Partner status",
        description = "Change the status of a Business Partner (ACTIVE, INACTIVE, etc.)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Status updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid status value"),
            ApiResponse(responseCode = "404", description = "Business Partner not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PatchMapping("/{id}/status")
    fun updateBpMasterStatus(
        @Parameter(description = "Business Partner ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "New status (ACTIVE, INACTIVE, SUSPENDED)", required = true, example = "ACTIVE")
        @RequestParam status: String,
        @Parameter(description = "User who updated the status")
        @RequestParam(required = false) updatedBy: String?
    ): Mono<ResponseEntity<BpMaster>> {
        logger.info("PATCH /api/v1/bp-master/$id/status - Updating status to: $status")

        return bpMasterService.updateStatus(id, status, updatedBy)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Delete Business Partner",
        description = "Delete a Business Partner by ID. This operation is permanent."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Business Partner deleted successfully"),
            ApiResponse(responseCode = "404", description = "Business Partner not found"),
            ApiResponse(responseCode = "409", description = "Cannot delete due to existing dependencies"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBpMaster(
        @Parameter(description = "Business Partner ID", required = true)
        @PathVariable id: Long
    ): Mono<Void> {
        logger.info("DELETE /api/v1/bp-master/$id")
        return bpMasterService.delete(id)
    }

    @Operation(
        summary = "Check business registration number duplication",
        description = "Check if a business registration number already exists in the system"
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
    @GetMapping("/check/business-reg-no/{businessRegNo}")
    fun checkBusinessRegNo(
        @Parameter(description = "Business registration number to check", required = true, example = "123-45-67890")
        @PathVariable businessRegNo: String
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-master/check/business-reg-no/$businessRegNo")

        return bpMasterService.existsByBusinessRegNo(businessRegNo)
            .map { exists -> mapOf("exists" to exists) }
    }

    @Operation(
        summary = "Check Business Partner code duplication",
        description = "Check if a Business Partner code already exists in the system"
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
    @GetMapping("/check/code/{code}")
    fun checkCode(
        @Parameter(description = "Business Partner code to check", required = true, example = "BP001")
        @PathVariable code: String
    ): Mono<Map<String, Boolean>> {
        logger.info("GET /api/v1/bp-master/check/code/$code")

        return bpMasterService.existsByCode(code)
            .map { exists -> mapOf("exists" to exists) }
    }
}