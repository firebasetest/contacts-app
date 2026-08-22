// src/components/BusinessUnitFormModal.jsx
import React, { useState, useEffect } from 'react';
import { StatusDropdown } from './StatusDropdown'; // Assume this component exists

const BusinessUnitFormModal = ({ isOpen, onClose, mode, currentUnit, onSave }) => {
    const [formData, setFormData] = useState({
        businessUnitId: '',
        name: '',
        status: 'ACTIVE',
    });
    
    // Use useEffect to populate the form when the modal opens and we are in 'edit' mode
    useEffect(() => {
        if (isOpen && currentUnit) {
            setFormData({
                businessUnitId: currentUnit.businessUnitId,
                name: currentUnit.name,
                status: currentUnit.status,
            });
        } else if (isOpen && mode === 'create') {
             setFormData({ businessUnitId: '', name: '', status: 'ACTIVE' });
        }
    }, [isOpen, currentUnit, mode]);


    if (!isOpen) return null;

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <div className="modal-overlay" style={{ display: isOpen ? 'block' : 'none' }}>
            <div className="modal-content bg-white p-6 rounded shadow-xl w-full max-w-md">
                <h2 className="text-xl mb-4">{mode === 'create' ? 'Create New Business Unit' : 'Edit Business Unit'}</h2>
                
                <form onSubmit={handleSubmit}>
                    {/* 1. BUSINESS UNIT ID (Must be filled and unique) */}
                    <label className="block text-sm font-medium text-gray-700">Business Unit ID (X-Tenant-Id)*</label>
                    <input 
                        type="text" 
                        name="businessUnitId" 
                        value={formData.businessUnitId} 
                        onChange={handleChange} 
                        required 
                        className="form-input mt-1 block w-full border p-2 border-gray-300 rounded"
                    />

                    {/* 2. NAME */}
                    <label className="block text-sm font-medium text-gray-700 mt-4">Business Unit Name*</label>
                    <input 
                        type="text" 
                        name="name" 
                        value={formData.name} 
                        onChange={handleChange} 
                        required 
                        className="form-input mt-1 block w-full border p-2 border-gray-300 rounded"
                    />

                    {/* 3. STATUS (Controlled by StatusDropdown) */}
                    <label className="block text-sm font-medium text-gray-700 mt-4">Status*</label>
                    <StatusDropdown 
                        value={formData.status} 
                        onChange={handleChange} 
                        required 
                    />

                    {/* Buttons */}
                    <div className="flex justify-end gap-3 mt-6">
                        <button type="button" onClick={onClose} className="px-4 py-2 border rounded hover:bg-gray-50">
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className="bg-primary text-white py-2 px-4 rounded"
                        >
                            Save {mode === 'create' ? 'Unit' : 'Changes'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default BusinessUnitFormModal;
