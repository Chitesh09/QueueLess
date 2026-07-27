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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping({"/orgs", "/organizations"})
    @Operation(summary = "List all registered organizations (tenants)")
    public ResponseEntity<ApiResponse<List<Organization>>> getOrganizations() {
        return ResponseEntity.ok(ApiResponse.success(orgRepository.findAll()));
    }

    @GetMapping({"/orgs/{id}", "/organizations/{id}"})
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<ApiResponse<Organization>> getOrganization(@PathVariable Long id) {
        Organization org = orgRepository.findById(id)
                .orElse(Organization.builder()
                        .id(id)
                        .name("City Health System")
                        .planTier("PRO")
                        .status("ACTIVE")
                        .build());
        return ResponseEntity.ok(ApiResponse.success(org));
    }

    @PostMapping({"/orgs", "/organizations"})
    @Operation(summary = "Create a new organization")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(@RequestBody Organization org) {
        if (org.getPlanTier() == null) {
            org.setPlanTier("PRO");
        }
        if (org.getStatus() == null) {
            org.setStatus("ACTIVE");
        }
        Organization saved = orgRepository.save(org);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved, "Organization created"));
    }

    @GetMapping({"/orgs/{orgId}/branches", "/organizations/{orgId}/branches"})
    @Operation(summary = "List branches under an organization")
    public ResponseEntity<ApiResponse<List<Branch>>> getBranches(@PathVariable Long orgId) {
        List<Branch> branches = branchRepository.findByOrganizationId(orgId);
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @PostMapping({"/orgs/{orgId}/branches", "/organizations/{orgId}/branches"})
    @Operation(summary = "Create a new branch facility under an organization")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@PathVariable Long orgId, @RequestBody Branch branch) {
        branch.setOrganizationId(orgId);
        Branch saved = branchRepository.save(branch);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved, "Branch created"));
    }

    @GetMapping("/branches/{branchId}/services")
    @Operation(summary = "List available queue services for a branch")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getServices(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(serviceRepository.findByBranchId(branchId)));
    }

    @GetMapping("/branches/{branchId}/departments")
    @Operation(summary = "List departments operating at a branch")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDepartments(@PathVariable Long branchId) {
        List<ServiceEntity> services = serviceRepository.findByBranchId(branchId);
        List<Map<String, Object>> departments = services.stream()
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "name", s.getName(),
                        "avgDurationMin", s.getAvgDurationMin()
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/branches/{branchId}/counters")
    @Operation(summary = "List counters operating at a branch")
    public ResponseEntity<ApiResponse<List<Counter>>> getCounters(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(counterRepository.findByBranchId(branchId)));
    }

    @PostMapping("/branches/{branchId}/counters")
    @Operation(summary = "Create a new counter station at a branch")
    public ResponseEntity<ApiResponse<Counter>> createCounter(@PathVariable Long branchId, @RequestBody Counter counter) {
        counter.setBranchId(branchId);
        if (counter.getStatus() == null) {
            counter.setStatus("ONLINE");
        }
        Counter saved = counterRepository.save(counter);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved, "Counter created"));
    }
}
