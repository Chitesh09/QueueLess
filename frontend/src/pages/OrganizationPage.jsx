import React, { useState } from 'react';
import {
  useOrganizationsQuery,
  useOrganizationQuery,
  useOrganizationBranchesQuery,
  useBranchDepartmentsQuery,
  useBranchCountersQuery,
  useCreateOrganizationMutation,
  useCreateBranchMutation,
  useCreateCounterMutation,
} from '../hooks/useOrganizationHooks';
import { organizationService } from '../services/organizationService';
import { OrganizationHeaderCard } from '../components/organization/OrganizationHeaderCard';
import { BranchCard } from '../components/organization/BranchCard';
import { CreateOrganizationModal } from '../components/organization/CreateOrganizationModal';
import { CreateBranchModal } from '../components/organization/CreateBranchModal';
import { CreateCounterModal } from '../components/organization/CreateCounterModal';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Skeleton } from '../components/ui/Skeleton';
import { Building2, Search, Plus, MapPin, Layers, Filter } from 'lucide-react';

export const OrganizationPage = () => {
  const [selectedOrgId, setSelectedOrgId] = useState(() => organizationService.getStoredOrgId());
  const [selectedBranchId, setSelectedBranchId] = useState(() => organizationService.getStoredBranchId());
  const [searchQuery, setSearchQuery] = useState('');

  const [isOrgModalOpen, setIsOrgModalOpen] = useState(false);
  const [isBranchModalOpen, setIsBranchModalOpen] = useState(false);
  const [isCounterModalOpen, setIsCounterModalOpen] = useState(false);

  // TanStack Query Hooks
  const { data: organization } = useOrganizationQuery(selectedOrgId);
  const { data: branches, isLoading: isBranchesLoading } = useOrganizationBranchesQuery(selectedOrgId);
  const { data: departments } = useBranchDepartmentsQuery(selectedBranchId);

  const createOrgMutation = useCreateOrganizationMutation(() => {
    setIsOrgModalOpen(false);
  });

  const createBranchMutation = useCreateBranchMutation(() => {
    setIsBranchModalOpen(false);
  });

  const createCounterMutation = useCreateCounterMutation(() => {
    setIsCounterModalOpen(false);
  });

  const handleCreateOrg = (formData) => {
    createOrgMutation.mutate(formData);
  };

  const handleCreateBranch = (formData) => {
    createBranchMutation.mutate({
      orgId: selectedOrgId,
      ...formData,
    });
  };

  const handleCreateCounter = (formData) => {
    createCounterMutation.mutate({
      branchId: selectedBranchId,
      ...formData,
    });
  };

  const handleOpenAddCounter = (branchId) => {
    setSelectedBranchId(branchId);
    organizationService.setStoredBranchId(branchId);
    setIsCounterModalOpen(true);
  };

  // Default fallback branch data for UI preview
  const defaultBranches = [
    {
      id: 1,
      name: 'Main Campus Branch',
      code: 'MAIN-01',
      address: '100 Medical Center Way, Metropolitan City',
      activeCountersCount: 4,
    },
    {
      id: 2,
      name: 'West Wing Specialty Branch',
      code: 'WEST-02',
      address: '450 Healthcare Boulevard, Westside',
      activeCountersCount: 2,
    },
  ];

  const branchList = branches && branches.length > 0 ? branches : defaultBranches;

  const filteredBranches = branchList.filter(
    (b) =>
      b.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (b.code && b.code.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="space-y-8 animate-in fade-in-50 duration-300">
      {/* Tenant Summary Header */}
      <OrganizationHeaderCard
        organization={organization}
        branchCount={filteredBranches.length}
        departmentCount={4}
        counterCount={6}
      />

      {/* Action Header & Search Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <Building2 className="w-5 h-5 text-sky-400" />
            Branch Facilities Catalog
          </h2>
          <p className="text-xs text-slate-400">
            Manage multi-tenant branch facilities, department mappings, and counter stations
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            onClick={() => setIsOrgModalOpen(true)}
            variant="outline"
            className="font-bold whitespace-nowrap text-xs hover:border-sky-500/50 hover:text-sky-400"
          >
            <Building2 className="w-4 h-4 mr-1.5" />
            Add Organization
          </Button>

          <Button
            onClick={() => setIsBranchModalOpen(true)}
            variant="primary"
            className="font-bold whitespace-nowrap text-xs flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            Add New Branch
          </Button>
        </div>
      </div>

      {/* Search Bar Input */}
      <Card className="p-4 border-slate-800 bg-slate-900/60">
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
          <Input
            type="text"
            placeholder="Search branches by name or code (e.g. Main Campus, WEST-02)..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 text-xs"
          />
        </div>
      </Card>

      {/* Branch Catalog Grid */}
      {isBranchesLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton className="h-40 w-full rounded-2xl" />
          <Skeleton className="h-40 w-full rounded-2xl" />
        </div>
      ) : filteredBranches.length === 0 ? (
        <Card className="p-8 text-center space-y-2 border-dashed border-slate-800 bg-slate-900/40">
          <MapPin className="w-10 h-10 text-slate-600 mx-auto" />
          <h3 className="text-sm font-bold text-slate-300">No Matching Branches Found</h3>
          <p className="text-xs text-slate-500 max-w-xs mx-auto">
            Try adjusting your search query or click "Add New Branch" above to register a new facility.
          </p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredBranches.map((b) => (
            <BranchCard key={b.id} branch={b} onAddCounter={handleOpenAddCounter} />
          ))}
        </div>
      )}

      {/* Modals */}
      <CreateOrganizationModal
        isOpen={isOrgModalOpen}
        onClose={() => setIsOrgModalOpen(false)}
        onSubmit={handleCreateOrg}
        isLoading={createOrgMutation.isPending}
      />

      <CreateBranchModal
        isOpen={isBranchModalOpen}
        onClose={() => setIsBranchModalOpen(false)}
        onSubmit={handleCreateBranch}
        isLoading={createBranchMutation.isPending}
      />

      <CreateCounterModal
        isOpen={isCounterModalOpen}
        onClose={() => setIsCounterModalOpen(false)}
        onSubmit={handleCreateCounter}
        isLoading={createCounterMutation.isPending}
        departments={departments}
      />
    </div>
  );
};
