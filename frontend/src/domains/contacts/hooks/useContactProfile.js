import { useState, useEffect } from 'react';
import axios from 'axios';

export const useContactProfile = (entityId, isAuditEnabled) => {
  const [profile, setProfile] = useState(null);
  const [changelog, setChangelog] = useState([]);
  const [uiState, setUiState] = useState({ loading: true, activeTab: 'details' });

  const fetchProfileData = async () => {
    setUiState(prev => ({ ...prev, loading: true }));
    try {
      const res = await axios.get(`/api/v1/contacts/${entityId}`);
      setProfile(res.data);
    } catch (err) {
      console.error("Critical failure pulling target core base profile details", err);
    } finally {
      setUiState(prev => ({ ...prev, loading: false }));
    }
  };

  const fetchAuditLogs = async () => {
    try {
      const res = await axios.get(`/api/v1/audit/entities/${entityId}`);
      setChangelog(res.data);
    } catch (err) {
      console.error("Failure processing temporal history logs context payload", err);
    }
  };

  useEffect(() => {
    if (entityId) fetchProfileData();
  }, [entityId]);

  useEffect(() => {
    if (uiState.activeTab === 'audit' && isAuditEnabled) {
      fetchAuditLogs();
    }
  }, [uiState.activeTab]);

  const switchTab = (tabName) => {
    setUiState(prev => ({ ...prev, activeTab: tabName }));
  };

  const executeGdprErasure = async () => {
    if (!window.confirm("CRITICAL COMPLIANCE NOTICE: This operation irreversibly overwrites PII. Continue?")) return;
    try {
      await axios.put(`/api/v1/privacy/contacts/${entityId}/anonymize`);
      alert("PII data arrays successfully purged and anonymized.");
      fetchProfileData();
    } catch (err) {
      alert("Privacy pipeline exception encountered: " + err.message);
    }
  };

  return {
    profile,
    changelog,
    loading: uiState.loading,
    activeTab: uiState.activeTab,
    switchTab,
    executeGdprErasure
  };
};