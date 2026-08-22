import React, { useCallback } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { BusinessUnitDto, BusinessUnitSearchResponse, BusinessUnitInputDto, BusinessUnitStatus } from '../../common/dto/BusinessUnitDto';

// --- API Client Simulation ---
// In a real app, this is where you'd use Axios or Fetch
const useFetchBusinessUnits = () => {
  return useInfiniteQuery({
    queryKey: ['businessUnits'],
    queryFn: async ({ pageParam = 0, queryKey }) => {
      console.log(`[API Call] Fetching page ${pageParam}...`);
      // 🚀 Calls the backend: GET /api/v1/business-units?page=...&size=...
      const response = await fetch(`/api/v1/business-units?page=${pageParam}&size=10&searchQuery=${queryKey.searchQuery}`);
      if (!response.ok) {
        throw new Error("Failed to fetch business units. Check permissions.");
      }
      // Assume the backend returns a structure matching BusinessUnitSearchResponse
      return response.json();
    },
    // Ensures pagination logic works correctly
    getNextPageParam: (lastPage) => {
      // Check if there are more pages to load
      const totalPages = lastPage.totalPages;
      const currentPage = lastPage.number + 1;
      return totalPages > currentPage ? currentPage : undefined;
    },
  });
};

// --- Component Definition ---
export function BusinessUnitTable({ onUnitSelect }: { onUnitSelect: (unitId: string) => void }) {
  const { data, fetchNextPage, hasNextPage, isFetching, error, pageQuery } = useFetchBusinessUnits();

  const handleCreateUnit = useCallback(() => {
    // 💡 Trigger modal for creating a new Business Unit
    alert('Opening "Create Business Unit" Modal...');
    // In reality: setIsCreating(true);
  }, []);

  const handleUpdateStatus = useCallback(async (unitId: string, newStatus: BusinessUnitStatus) => {
    if (!window.confirm(`Are you sure you want to change status for ${unitId}?`)) return;
    
    // 🚀 Calls the backend: PUT /api/v1/business-units/{id}/status?newStatus=ACTIVE
    await fetch(`/api/v1/business-units/${unitId}/status?newStatus=${newStatus}`);
    
    // Critical step: Invalidate the cache to force a re-fetch of the list, showing the updated state immediately.
    // This is the power of React Query.
    // queryClient.invalidateQueries(['businessUnits']); 
    console.log('Status updated and cache invalidated.');
  }, []);

  if (error) return <div className="alert alert-danger">Error: {error.message}</div>;
  if (!data && !isFetching) return <div className="card p-4">No Business Units found.</div>;

  return (
    <div className="card p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Business Units Scope Management</h3>
        <button className="btn btn-primary" onClick={handleCreateUnit}>
          + Create New Business Unit
        </button>
      </div>

      {/* Data Table */}
      <div className="table-responsive">
        <table className="table table-striped table-hover">
          <thead>
            <tr>
              <th>Unit Name</th>
              <th>Slug</th>
              <th>Description</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {data?.pages.flatMap(page => page.content).map((unit) => (
              <tr key={unit.businessUnitId}>
                <td>{unit.name}</td>
                <td><code>{unit.slug}</code></td>
                <td>{unit.description}</td>
                <td><span className={`badge bg-${unit.status === 'ACTIVE' ? 'success' : 'warning'}`}>{unit.status}</span></td>
                <td>
                  <button className="btn btn-sm me-2" onClick={() => onUnitSelect(unit.businessUnitId)}>
                    View Details
                  </button>
                  {/* Status change buttons */}
                  <button className="btn btn-sm btn-outline-secondary" onClick={() => handleUpdateStatus(unit.businessUnitId, 'INACTIVE')}>
                    Deactivate
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      <div className="mt-4 d-flex justify-content-center">
        <button 
          className="btn btn-secondary me-2" 
          onClick={() => fetchNextPage()} 
          disabled={!hasNextPage || isFetching}
        >
          {isFetching ? 'Loading...' : 'Next Page'}
        </button>
      </div>
    </div>
  );
}
