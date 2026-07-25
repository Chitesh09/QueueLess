import { organizationApi } from '../api/organizationApi';

const ACTIVE_ORG_KEY = 'queueless_active_organization_id';
const ACTIVE_BRANCH_KEY = 'queueless_active_branch_id';

export const organizationService = {
  fetchOrganizations: async () => {
    const res = await organizationApi.getOrganizations();
    return res?.data || [];
  },

  fetchOrganization: async (id = 1) => {
    const res = await organizationApi.getOrganization(id);
    return res?.data || null;
  },

  createOrganization: async (orgData) => {
    const res = await organizationApi.createOrganization(orgData);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Failed to create organization');
  },

  fetchBranches: async (organizationId = 1) => {
    const res = await organizationApi.getBranches(organizationId);
    return res?.data || [];
  },

  createBranch: async (organizationId = 1, branchData) => {
    const res = await organizationApi.createBranch(organizationId, branchData);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Failed to create branch');
  },

  fetchDepartments: async (branchId = 1) => {
    const res = await organizationApi.getDepartments(branchId);
    return res?.data || [];
  },

  fetchCounters: async (branchId = 1) => {
    const res = await organizationApi.getCounters(branchId);
    return res?.data || [];
  },

  createCounter: async (branchId = 1, counterData) => {
    const res = await organizationApi.createCounter(branchId, counterData);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Failed to create counter');
  },

  getStoredOrgId: () => {
    const saved = localStorage.getItem(ACTIVE_ORG_KEY);
    return saved ? Number(saved) : 1;
  },

  setStoredOrgId: (orgId) => {
    localStorage.setItem(ACTIVE_ORG_KEY, String(orgId));
  },

  getStoredBranchId: () => {
    const saved = localStorage.getItem(ACTIVE_BRANCH_KEY);
    return saved ? Number(saved) : 1;
  },

  setStoredBranchId: (branchId) => {
    localStorage.setItem(ACTIVE_BRANCH_KEY, String(branchId));
  },
};
