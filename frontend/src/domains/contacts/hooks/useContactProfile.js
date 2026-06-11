import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { toast } from 'react-hot-toast';

// Base API configuration containing the required tenant context header
const API_BASE = '/api/v1';
const getHeaders = () => {
  // In a real application, pull dynamically from your authorization context or state
  const businessUnitId = localStorage.getItem('active_bu_id') || '00000000-0000-0000-0000-000000000000';
  const token = localStorage.getItem('auth_token');
  return {
    'Content-Type': 'application/json',
    'X-BU-ID': businessUnitId,
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
};

export const useContactProfile = (contactId) => {
  const [profile, setProfile] = useState(null);
  const [historicalState, setHistoricalState] = useState(null);
  const [auditTrail, setAuditTrail] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProfile = useCallback(async () => {
    if (!contactId) return;
    try {
      setLoading(true);
      const res = await axios.get(`${API_BASE}/contacts/${contactId}`, { headers: getHeaders() });
      setProfile(res.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load profile data.');
      toast.error('Could not fetch profile record details.');
    } finally {
      setLoading(false);
    }
  }, [contactId]);

  const fetchAuditTrail = useCallback(async () => {
    if (!contactId) return;
    try {
      const res = await axios.get(`${API_BASE}/audit/entities/${contactId}`, { headers: getHeaders() });
      setAuditTrail(res.data);
    } catch (err) {
      console.error('Audit trail load failed:', err);
    }
  }, [contactId]);

  const fetchAsOf = async (isoString) => {
    if (!contactId || !isoString) return;
    try {
      const res = await axios.get(`${API_BASE}/contacts/${contactId}/historical`, {
        headers: getHeaders(),
        params: { asOf: isoString }
      });
      setHistoricalState(res.data);
      toast.success(`Loaded profile state snapshot as of requested timestamp.`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'No historical state found for that timestamp.');
    }
  };

  const triggerCallBridge = async (employeePhone) => {
    if (!profile?.phoneNumber) {
      toast.error('Destination customer phone route is missing.');
      return;
    }
    try {
      const params = new URLSearchParams();
      params.append('employeePhone', employeePhone);
      params.append('contactPhone', profile.phoneNumber);

      const res = await axios.post(`${API_BASE}/telephony/call`, params, {
        headers: { ...getHeaders(), 'Content-Type': 'application/x-www-form-urlencoded' }
      });
      toast.success('Telephony pipeline bridged. Leased trunk route live.');
      return res.data;
    } catch (err) {
      toast.error(err.response?.data || 'Telephony provider gateway failed.');
    }
  };

  const delegateAdmin = async () => {
    try {
      await axios.post(`${API_BASE}/contacts/${contactId}/delegate-admin`, {}, { headers: getHeaders() });
      toast.success('Administrative delegation privileges successfully provisioned.');
      fetchProfile();
    } catch (err) {
      toast.error('Delegation mapping collapsed on authorization layer.');
    }
  };

  const exportGdprData = async () => {
    try {
      const res = await axios.get(`${API_BASE}/privacy/contacts/${contactId}/export`, { headers: getHeaders() });
      const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `gdpr-data-export-${contactId}.json`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('Data portability ledger extracted successfully.');
    } catch (err) {
      toast.error('GDPR data packaging execution rejected.');
    }
  };

  const anonymizeGdprData = async () => {
    if (!window.confirm('CRITICAL ACTION: Are you certain you want to mask and delete all trace records for this individual permanently? This cannot be undone.')) return;
    try {
      await axios.put(`${API_BASE}/privacy/contacts/${contactId}/anonymize`, {}, { headers: getHeaders() });
      toast.success('Right to erasure masking successfully committed.');
      fetchProfile();
    } catch (err) {
      toast.error('Erasure sequence dropped on system lifecycle layer.');
    }
  };

  useEffect(() => {
    fetchProfile();
    fetchAuditTrail();
  }, [contactId, fetchProfile, fetchAuditTrail]);

  return {
    profile,
    historicalState,
    auditTrail,
    loading,
    error,
    refresh: fetchProfile,
    fetchAsOf,
    clearHistory: () => setHistoricalState(null),
    triggerCallBridge,
    delegateAdmin,
    exportGdprData,
    anonymizeGdprData
  };
};