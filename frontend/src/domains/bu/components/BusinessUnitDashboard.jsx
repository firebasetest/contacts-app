// src/components/BusinessUnitDashboard.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchAllBusinessUnits, createBusinessUnit, updateBusinessUnit, deleteBusinessUnit } from '../services/businessUnitApi';
import BusinessUnitTable from './BusinessUnitTable';
import BusinessUnitFormModal from './BusinessUnitFormModal';

const BusinessUnitDashboard = () => {
    const { user } = useAuth();
    const [units, setUnits] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // State for the Modal: null = viewing, id = editing, 'create' = creating
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedUnitId, setSelectedUnitId] = useState(null);
    const [mode, setMode] = useState(null); // 'create' or 'edit'

    // --- Core Data Fetching ---
    const loadBusinessUnits = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchAllBusinessUnits();
            setUnits(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadBusinessUnits();
    }, [loadBusinessUnits]);

    // --- Action Handlers ---

    const handleOpenCreateModal = () => {
        if (user.roles.includes('SUPER_ADMIN')) {
            setMode('create');
            setSelectedUnitId(null);
            setIsModalOpen(true);
        } else {
            alert("Access Denied: Only Super Admins can create Business Units.");
        }
    };

    const handleOpenEditModal = (unitId) => {
        setSelectedUnitId(unitId);
        setMode('edit');
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedUnitId(null);
        setMode(null);
    };
    
    // --- Action Execution Handlers ---

    const handleSaveUnit = async (formData) => {
        try {
            if (mode === 'create') {
                await createBusinessUnit(formData);
            } else {
                await updateBusinessUnit(selectedUnitId, formData);
            }
            // Success! Refetch the list to reflect changes.
            loadBusinessUnits();
            alert('Operation successful!');
        } catch (err) {
            alert(`Error: ${err.message}`);
        } finally {
            handleCloseModal();
        }
    };

    const handleDeleteUnit = async (unitId, unitName) => {
        if (!window.confirm(`[DANGER] Are you sure you want to delete "${unitName}"? This action is irreversible.`)) {
            return;
        }
        try {
            await deleteBusinessUnit(unitId);
            loadBusinessUnits();
            alert('Business Unit deleted successfully.');
        } catch (err) {
            alert(`Deletion Failed: ${err.message}`);
        }
    };

    if (loading) return <div>Loading Business Unit data...</div>;
    if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;

    return (
        <div className="container mx-auto p-4">
            <header className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Business Unit Management ({user.currentBusinessUnitId})</h1>
                {/* Role-based Button Visibility */}
                {user.roles.includes('SUPER_ADMIN') && (
                    <button 
                        className="bg-primary text-white py-2 px-4 rounded" 
                        onClick={handleOpenCreateModal}
                    >
                        + Add New Business Unit
                    </button>
                )}
            </header>

            <BusinessUnitTable 
                units={units}
                onEdit={handleOpenEditModal}
                onDelete={handleDeleteUnit}
            />

            <BusinessUnitFormModal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                mode={mode}
                currentUnit={/* ... logic to fetch unit data for display ... */}
                onSave={handleSaveUnit}
            />
        </div>
    );
};

export default BusinessUnitDashboard;
