package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.Branch;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.Organization;
import com.queueless.domain.entity.ServiceEntity;
import com.queueless.domain.repository.BranchRepository;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.OrganizationRepository;
import com.queueless.domain.repository.ServiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Organization & Branch Management", description = "Tenant, Branch, Service, and Counter management")
public class OrgController {

    private final OrganizationRepository orgRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final CounterRepository counterRepository;

    public OrgController(OrganizationRepository orgRepository,
                         BranchRepository branchRepository,
                         ServiceRepository serviceRepository,
                         CounterRepository counterRepository) {
        this.orgRepository = orgRepository;
        this.branchRepository = branchRepository;
        this.serviceRepository = serviceRepository;
        this.counterRepository = counterRepository;
    }

    @GetMapping("/orgs")
    @Operation(summary = "List all registered organizations (tenants)")
    public ResponseEntity<ApiResponse<List<Organization>>> getOrganizations() {
        return ResponseEntity.ok(ApiResponse.success(orgRepository.findAll()));
    }

    @GetMapping("/orgs/{orgId}/branches")
    @Operation(summary = "List branches under an organization")
    public ResponseEntity<ApiResponse<List<Branch>>> getBranches(@PathVariable Long orgId) {
        return ResponseEntity.ok(ApiResponse.success(branchRepository.findByOrganizationId(orgId)));
    }

    @GetMapping("/branches/{branchId}/services")
    @Operation(summary = "List available queue services for a branch")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getServices(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(serviceRepository.findByBranchId(branchId)));
    }

    @GetMapping("/branches/{branchId}/counters")
    @Operation(summary = "List counters operating at a branch")
    public ResponseEntity<ApiResponse<List<Counter>>> getCounters(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(counterRepository.findByBranchId(branchId)));
    }
}
