package com.interplug.bpmastermanageservice.repository

import com.interplug.bpmastermanageservice.entity.PfCodeMaster
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PfCodeMasterRepository : ReactiveCrudRepository<PfCodeMaster, Long> {

    // PF 코드로 조회
    fun findByPfCode(pfCode: String): Mono<PfCodeMaster>

    // 상태별 조회
    fun findByStatus(status: String): Flux<PfCodeMaster>

    // PF 이름으로 검색 (LIKE 검색)
    @Query("SELECT * FROM pf_code_master WHERE pf_name LIKE CONCAT('%', :pfName, '%')")
    fun searchByPfName(pfName: String): Flux<PfCodeMaster>

    // PF 코드 중복 체크
    fun existsByPfCode(pfCode: String): Mono<Boolean>

    // 활성화된 PF 코드만 조회
    @Query("SELECT * FROM pf_code_master WHERE status = 'ACTIVE' ORDER BY pf_name")
    fun findAllActive(): Flux<PfCodeMaster>

    // 마스터 데이터 보유 PF 조회
    fun findByHasMasterData(hasMasterData: String): Flux<PfCodeMaster>

    // 매장 소유 가능 PF 조회
    fun findByCanOwnStores(canOwnStores: String): Flux<PfCodeMaster>

    // 프랜차이즈 가능 PF 조회
    fun findByCanFranchise(canFranchise: String): Flux<PfCodeMaster>

    // 빌링 가능 PF 조회
    fun findByBillingCapable(billingCapable: String): Flux<PfCodeMaster>

    // 특정 속성을 가진 활성 PF 조회
    @Query("""
        SELECT * FROM pf_code_master
        WHERE status = 'ACTIVE'
        AND (:hasMasterData IS NULL OR has_master_data = :hasMasterData)
        AND (:canOwnStores IS NULL OR can_own_stores = :canOwnStores)
        AND (:canFranchise IS NULL OR can_franchise = :canFranchise)
        AND (:billingCapable IS NULL OR billing_capable = :billingCapable)
        ORDER BY pf_name
    """)
    fun findActiveWithAttributes(
        hasMasterData: String?,
        canOwnStores: String?,
        canFranchise: String?,
        billingCapable: String?
    ): Flux<PfCodeMaster>
}