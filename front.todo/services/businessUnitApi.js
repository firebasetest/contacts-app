// src/services/businessUnitApi.js
import { getAuthContext } from '../context/AuthContext'; // Assumed context hook

const BASE_URL = '/api/v1/business-units';

/**
 * Helper function to extract the current authenticated user's ID.
 * @returns {string} The BusinessUnit ID of the current user context.
 */
const getTenantId = () => {
    // In a real app, this would come from a secure cookie or context
    return getAuthContext().getCurrentBusinessUnitId(); 
};

// --- CRUD API Functions ---

/**
 * Fetches all Business Units (for the dashboard overview). 
 * NOTE: Requires adding a GET /list endpoint to the backend.
 */
export const fetchAllBusinessUnits = async () => {
    // Assuming a bulk GET endpoint is available for the dashboard view
    const response = await fetch(`${BASE_URL}/list`); 
    if (!response.ok) throw new Error('Failed to fetch business units.');
    return response.json();
};

/**
 * Creates a new Business Unit. Requires SUPER_ADMIN role.
 */
export const createBusinessUnit = async (data) => {
    // The backend handles the actual role check via @PreAuthorize
    const response = await fetch(`${BASE_URL}/admin/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to create Business Unit.');
    }
    return response.json();
};

/**
 * Updates an existing Business Unit's details.
 */
export const updateBusinessUnit = async (businessUnitId, data) => {
    // The backend enforces that businessUnitId must match the current tenant context.
    const response = await fetch(`${BASE_URL}/${businessUnitId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update Business Unit.');
    }
    return response.json();
};

/**
 * Deletes a Business Unit. Requires SUPER_ADMIN role and must match context ID.
 */
export const deleteBusinessUnit = async (businessUnitId) => {
    const response = await fetch(`${BASE_URL}/${businessUnitId}`, {
        method: 'DELETE',
    });
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Deletion failed.');
    }
    return { success: true };
};
