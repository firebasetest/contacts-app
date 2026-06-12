import React, { useState, useEffect } from 'react';
import axiosClient from '../../../api/axiosClient';
import { useContactProfile } from '../hooks/useContactProfile';
import TelephonyDialer from '../../audit/components/TelephonyDialer';
import { 
  Calendar, ShieldAlert, History, Download, Trash2, ArrowLeft, 
  RefreshCw, Edit2, Save, X, User, Phone, Mail, Building, Plus, ShieldCheck, Loader2 
} from 'lucide-react';
import toast from 'react-hot-toast';

// Mock Metadata Schema fetched from your backend's MetadataRegistry per tenant
const MOCK_METADATA_FIELDS = [
  { name: 'twitterHandle', label: 'Twitter Handle', type: 'text', placeholder: '@username', required: false },
  { name: 'contractValue', label: 'ARR Contract Value ($)', type: 'number', placeholder: '50000', required: false },
  { name: 'tier', label: 'Service Level Tier', type: 'select', options: ['Enterprise VIP', 'Growth Mid-Market', 'Self-Serve Sandbox'], required: true },
  { name: 'vipStatus', label: 'Executive Governance Scope', type: 'boolean', required: false }
];

const BLANK_FORM_TEMPLATE = {
  name: '',
  email: '',
  phoneNumber: '',
  systemRole: 'Standard User',
  source: 'Direct Entry',
  customAttributes: {}
};

export default function ContactDetail({ contactId, onBack }) {
  const isCreateMode = !contactId || contactId === 'new';

  // 1. Hook Integration Lifecycle 
  const {
    profile,
    historicalState,
    auditTrail = [],
    loading,
    error,
    fetchAsOf,
    clearHistory,
    triggerCallBridge,
    delegateAdmin,
    exportGdprData,
    anonymizeGdprData
  } = useContactProfile(isCreateMode ? null : contactId);

  // 2. Component Core States
  const [isEditing, setIsEditing] = useState(isCreateMode);
  const [isSaving, setIsSaving] = useState(false);
  const [asOfTimestamp, setAsOfTimestamp] = useState('');
  const [metadataFields, setMetadataFields] = useState(MOCK_METADATA_FIELDS);
  const [formData, setFormData] = useState(BLANK_FORM_TEMPLATE);

  // Evaluate structural fallback bindings
  const activeRecord = historicalState || profile;
  const isTimeTraveling = !!historicalState;

  // 3. Hydrate form elements whenever structural dataset records clear or load
  useEffect(() => {
    if (isCreateMode) {
      const initialCustoms = {};
      metadataFields.forEach(f => {
        initialCustoms[f.name] = f.type === 'boolean' ? false : '';
      });
      setFormData({ ...BLANK_FORM_TEMPLATE, customAttributes: initialCustoms });
    } else if (activeRecord) {
      setFormData({
        name: activeRecord.name || '',
        email: activeRecord.email || '',
        phoneNumber: activeRecord.phoneNumber || '',
        systemRole: activeRecord.systemRole || 'Standard User',
        source: activeRecord.source || 'Direct Entry',
        customAttributes: { ...activeRecord.customAttributes }
      });
    }
  }, [activeRecord, isCreateMode, metadataFields]);

  // 4. Input Mutator Normalization Core Engine
  const handleStandardChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCustomAttributeChange = (name, value, type) => {
    let normalizedValue = value;
    if (type === 'number') normalizedValue = value === '' ? '' : Number(value);
    if (type === 'boolean') normalizedValue = value === 'true' || value === true;

    setFormData(prev => ({
      ...prev,
      customAttributes: { ...prev.customAttributes, [name]: normalizedValue }
    }));
  };

  // 5. Dual-Route Persistence Dispatcher
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      if (isCreateMode) {
        // await axiosClient.post('/contacts', formData);
        await new Promise(resolve => setTimeout(resolve, 800));
        toast.success("Identity record injected into directory space.");
        onBack();
      } else {
        // await axiosClient.put(`/contacts/${contactId}`, formData);
        await new Promise(resolve => setTimeout(resolve, 800));
        
        if (profile) {
          Object.assign(profile, formData);
        }
        setIsEditing(false);
        toast.success("Profile records mutated successfully.");
      }
    } catch (err) {
      toast.error(err.response?.data?.message || "Data boundary validation failure.");
    } finally {
      setIsSaving(false);
    }
  };

  // Guardrail Layer: Loading State
  if (loading && !isCreateMode) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-slate-400">
        <RefreshCw className="w-8 h-8 animate-spin text-indigo-500" />
        <p className="text-sm font-mono text-slate-500">Loading multi-tenant workspace registry...</p>
      </div>
    );
  }

  // Guardrail Layer: Error Boundary
  if (error && !isCreateMode) {
    return (
      <div className="bg-red-950/20 border border-red-900 rounded-xl p-6 text-center max-w-xl mx-auto my-8">
        <ShieldAlert className="w-10 h-10 text-red-500 mx-auto mb-2" />
        <h3 className="text-lg font-bold text-white mb-1">Access Restrained</h3>
        <p className="text-sm text-red-400 font-mono">{error}</p>
        <button onClick={onBack} className="mt-4 inline-flex items-center gap-1 text-xs text-white bg-slate-800 px-3 py-2 rounded-lg hover:bg-slate-700 font-semibold transition-colors">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-white space-y-8 animate-fadeIn">
      
      {/* Upper Navigation Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800/60 pb-5">
        <div className="flex items-center gap-4">
          <button 
            type="button" 
            onClick={onBack} 
            className="p-2 hover:bg-slate-800 rounded-xl transition-colors border border-slate-800 text-slate-400 hover:text-white bg-slate-950"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold tracking-tight text-slate-100">
                {isCreateMode ? 'Provision New Directory Entry' : activeRecord?.name}
              </h1>
              {isTimeTraveling && (
                <span className="bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs px-2.5 py-0.5 rounded-full font-semibold animate-pulse flex items-center gap-1 font-mono">
                  <History className="w-3 h-3" /> Historical Snapshot Mode
                </span>
              )}
            </div>
            {!isCreateMode && activeRecord && (
              <p className="text-xs text-slate-500 mt-1 font-mono tracking-wide">Workspace Global UUID: {activeRecord.id}</p>
            )}
          </div>
        </div>

        {/* Toolbar Grid: Handles Mode Changes, Persistence, and Temporal View Adjustments */}
        <div className="flex flex-wrap items-center gap-3">
          {isEditing ? (
            <div className="flex items-center gap-2">
              {!isCreateMode && (
                <button
                  type="button"
                  onClick={() => { setIsEditing(false); setFormData(activeRecord); }}
                  className="bg-slate-950 text-slate-400 hover:text-slate-200 border border-slate-800 text-xs font-semibold px-4 py-2 rounded-xl transition-colors"
                >
                  Cancel
                </button>
              )}
              <button
                type="submit"
                onClick={handleSubmit}
                disabled={isSaving}
                className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold px-4 py-2 rounded-xl shadow-lg shadow-indigo-600/10 transition-colors flex items-center gap-1.5 disabled:opacity-50"
              >
                {isSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                {isCreateMode ? 'Commit Entry' : 'Save Changes'}
              </button>
            </div>
          ) : (
            !isTimeTraveling && (
              <button
                type="button"
                onClick={() => setIsEditing(true)}
                className="bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-800 text-xs font-semibold px-4 py-2 rounded-xl transition-all flex items-center gap-2"
              >
                <Edit2 className="w-3.5 h-3.5 text-indigo-400" /> Edit Profile Mapping
              </button>
            )
          )}

          {/* Temporal Audit Plane Controls */}
          {!isCreateMode && (
            <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 p-1.5 rounded-xl shadow-inner">
              <div className="flex items-center gap-1.5 text-slate-400 px-2 text-xs font-medium font-mono">
                <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                <span>Temporal View</span>
              </div>
              <input
                type="datetime-local"
                value={asOfTimestamp}
                onChange={(e) => setAsOfTimestamp(e.target.value)}
                className="bg-slate-950 border border-slate-700/60 rounded-lg px-2 py-1 text-xs font-mono text-white focus:outline-none focus:border-indigo-500"
              />
              <button
                type="button"
                onClick={() => fetchAsOf(new Date(asOfTimestamp).toISOString())}
                disabled={!asOfTimestamp}
                className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-600 px-3 py-1 rounded-lg text-xs font-semibold transition-colors"
              >
                Go
              </button>
              {isTimeTraveling && (
                <button type="button" onClick={clearHistory} className="text-xs text-slate-400 hover:text-white underline px-1 font-mono">
                  Reset
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Core Component Operations Layout Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Core Attributes Panel Block */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 backdrop-blur space-y-6">
            <h3 className="text-sm font-bold tracking-wider text-slate-400 uppercase flex items-center gap-2 font-mono">
              <User className="w-4 h-4 text-indigo-400" /> Core Platform Parameters
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              {isEditing ? (
                <div className="sm:col-span-2">
                  <label className="block text-xs font-medium text-slate-500 mb-1.5 font-mono">Full Corporate Name</label>
                  <input
                    type="text" name="name" value={formData.name} onChange={handleStandardChange} required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-indigo-500 transition-colors"
                  />
                </div>
              ) : null}

              <div>
                <label className="block text-xs font-medium text-slate-500 font-mono">Email Address Registry</label>
                {isEditing ? (
                  <input
                    type="email" name="email" value={formData.email} onChange={handleStandardChange} required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm mt-1.5 text-slate-200 focus:outline-none focus:border-indigo-500 transition-colors"
                  />
                ) : (
                  <p className="text-sm font-medium mt-1.5 break-all text-indigo-300 font-mono">{activeRecord?.email || '—'}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-500 font-mono">Phone Terminal Number</label>
                {isEditing ? (
                  <input
                    type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleStandardChange}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm mt-1.5 text-slate-200 focus:outline-none focus:border-indigo-500 transition-colors"
                  />
                ) : (
                  <p className="text-sm font-medium mt-1.5 font-mono text-slate-300">{activeRecord?.phoneNumber || '—'}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-500 font-mono">System Role Scope</label>
                {isEditing ? (
                  <select
                    name="systemRole" value={formData.systemRole} onChange={handleStandardChange}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm mt-1.5 text-slate-200 focus:outline-none focus:border-indigo-500 cursor-pointer font-mono"
                  >
                    <option value="Standard User">Standard User</option>
                    <option value="Delegated Admin">Delegated Admin</option>
                    <option value="System Architect">System Architect</option>
                  </select>
                ) : (
                  <p className="text-sm font-medium mt-1.5 text-indigo-400 font-mono flex items-center gap-1.5">
                    {activeRecord?.systemRole === 'Delegated Admin' && <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 fill-emerald-500/10" />}
                    {activeRecord?.systemRole}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-500 font-mono">Verification Source Engine</label>
                {isEditing ? (
                  <input
                    type="text" name="source" value={formData.source} onChange={handleStandardChange}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm mt-1.5 text-slate-200 focus:outline-none focus:border-indigo-500 transition-colors font-mono"
                  />
                ) : (
                  <p className="text-sm font-medium mt-1.5 text-slate-400 font-mono">{activeRecord?.source || 'Direct Entry'}</p>
                )}
              </div>
            </div>

            {/* Extensible JSONB Polymorphic Data Attribute Workspace */}
            <div className="border-t border-slate-800/80 pt-6">
              <h4 className="text-xs font-bold text-slate-400 tracking-wider uppercase mb-4 flex items-center gap-1.5 font-mono">
                <Plus className="w-3.5 h-3.5 text-emerald-500" /> Dynamic Extended Metadata Schema (JSONB)
              </h4>
              
              <div className="bg-slate-950/40 rounded-xl p-4 border border-slate-800/60 grid grid-cols-1 sm:grid-cols-2 gap-4">
                {metadataFields.map((field) => {
                  const currentValue = isEditing 
                    ? formData.customAttributes[field.name] 
                    : activeRecord?.customAttributes?.[field.name];

                  return (
                    <div 
                      key={field.name} 
                      className={field.type === 'boolean' ? 'flex items-center justify-between sm:col-span-2 bg-slate-900/20 p-3 rounded-xl border border-slate-800/40' : 'flex flex-col gap-1.5'}
                    >
                      <label className="text-slate-400 font-mono text-xs flex items-center gap-1">
                        {field.label}
                        {field.required && isEditing && <span className="text-rose-500 text-[10px] font-mono">*</span>}
                      </label>

                      {isEditing ? (
                        <div className={field.type !== 'boolean' ? 'w-full' : ''}>
                          {field.type === 'text' && (
                            <input
                              type="text" placeholder={field.placeholder} required={field.required}
                              value={currentValue ?? ''} onChange={(e) => handleCustomAttributeChange(field.name, e.target.value, field.type)}
                              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 font-mono focus:outline-none focus:border-indigo-500"
                            />
                          )}

                          {field.type === 'number' && (
                            <input
                              type="number" placeholder={field.placeholder} required={field.required}
                              value={currentValue ?? ''} onChange={(e) => handleCustomAttributeChange(field.name, e.target.value, field.type)}
                              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 font-mono focus:outline-none focus:border-indigo-500"
                            />
                          )}

                          {field.type === 'select' && (
                            <select
                              required={field.required} value={currentValue ?? ''}
                              onChange={(e) => handleCustomAttributeChange(field.name, e.target.value, field.type)}
                              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 font-mono focus:outline-none focus:border-indigo-500 cursor-pointer"
                            >
                              <option value="">-- Choose Option --</option>
                              {field.options.map(opt => <option key={opt} value={opt}>{opt}</option>)}
                            </select>
                          )}

                          {field.type === 'boolean' && (
                            <button
                              type="button"
                              onClick={() => handleCustomAttributeChange(field.name, !currentValue, field.type)}
                              className={`w-10 h-5 flex items-center rounded-full p-0.5 transition-colors duration-200 focus:outline-none ${
                                currentValue ? 'bg-emerald-500' : 'bg-slate-800'
                              }`}
                            >
                              <div className={`bg-white w-4 h-4 rounded-full shadow transform transition-transform duration-200 ${
                                currentValue ? 'translate-x-5' : 'translate-x-0'
                              }`} />
                            </button>
                          )}
                        </div>
                      ) : (
                        <div>
                          {field.type === 'boolean' ? (
                            <span className={`text-[10px] font-bold font-mono px-2 py-0.5 rounded border ${
                              currentValue ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-slate-800 text-slate-500 border-transparent'
                            }`}>
                              {currentValue ? 'TRUE' : 'FALSE'}
                            </span>
                          ) : (
                            <span className="text-slate-200 font-medium font-mono">
                              {field.type === 'number' && currentValue ? `$${Number(currentValue).toLocaleString()}` : String(currentValue ?? '—')}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Delegated Administration Lifecycle Container */}
          {!isTimeTraveling && !isCreateMode && (
            <div className="flex flex-wrap gap-4 items-center justify-between p-6 bg-slate-900/20 border border-slate-800/60 rounded-2xl">
              <div>
                <h4 className="text-sm font-bold text-white flex items-center gap-1.5 font-mono">
                  <ShieldCheck className="w-4 h-4 text-emerald-400" /> Delegated Administration Workflow
                </h4>
                <p className="text-xs text-slate-400 mt-0.5">
                  {activeRecord?.systemRole === 'Delegated Admin' 
                    ? 'This identity features administrative clearance permissions inside their domain footprint.' 
                    : 'Elevate profile status coordinates directly within the client domain partition.'}
                </p>
              </div>
              <button 
                type="button"
                onClick={delegateAdmin} 
                className={`text-xs font-semibold px-4 py-2 rounded-xl transition-colors border font-mono ${
                  activeRecord?.systemRole === 'Delegated Admin'
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20 hover:bg-emerald-500/20'
                    : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
                }`}
              >
                {activeRecord?.systemRole === 'Delegated Admin' ? 'Modify Admin Scope Settings' : 'Execute Administrative Escalation'}
              </button>
            </div>
          )}
        </div>

        {/* Sidebar Container: Call Infrastructure & Compliance Consoles */}
        <div className="space-y-6">
          
          {/* Telephony Dialer Component Integration */}
          {!isTimeTraveling && !isCreateMode && activeRecord && (
            <TelephonyDialer onDial={triggerCallBridge} targetName={activeRecord.name} />
          )}

          {/* GDPR Governance Data Privacy Module */}
          {!isCreateMode && (
            <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-5 space-y-4">
              <h3 className="text-xs font-bold text-slate-400 tracking-wider uppercase flex items-center gap-1.5 font-mono">
                <ShieldAlert className="w-4 h-4 text-indigo-400" /> GDPR Compliance System Tools
              </h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                Enforce structural parameters matching sovereign workspace compliance data privacy regulations.
              </p>
              <div className="grid grid-cols-1 gap-2.5 pt-1">
                <button
                  type="button" onClick={exportGdprData}
                  className="w-full flex items-center justify-center gap-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-semibold py-2 rounded-xl text-slate-200 transition-colors font-mono"
                >
                  <Download className="w-3.5 h-3.5" /> Pack & Export Metadata (Art 20)
                </button>
                <button
                  type="button" onClick={anonymizeGdprData}
                  className="w-full flex items-center justify-center gap-2 bg-red-950/30 hover:bg-red-900/40 border border-red-900/50 text-xs font-semibold py-2 rounded-xl text-red-400 transition-colors font-mono"
                >
                  <Trash2 className="w-3.5 h-3.5" /> Execute Data Erasure (Art 17)
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Relational Multi-Tenant DB Temporal Ledger Logging Engine */}
      {!isCreateMode && (
        <div className="bg-slate-900/30 border border-slate-800/80 rounded-2xl p-6 shadow-md">
          <h3 className="text-sm font-bold tracking-wider text-slate-400 uppercase mb-5 flex items-center gap-2 font-mono">
            <History className="w-4 h-4 text-indigo-400" /> Chronological Workspace Audit Trail Ledger
          </h3>
          {auditTrail.length === 0 ? (
            <p className="text-xs text-slate-500 font-mono italic">No mutating audit transactions recorded against this resource boundary context.</p>
          ) : (
            <div className="relative border-l-2 border-slate-800 pl-4 space-y-6 ml-2">
              {auditTrail.map((logItem) => (
                <div key={logItem.id} className="relative group">
                  <div className="absolute -left-[23px] top-1 w-2.5 h-2.5 rounded-full bg-indigo-500 border border-slate-950 group-hover:scale-125 transition-transform" />
                  <div className="text-xs font-mono text-slate-400 flex flex-wrap items-center gap-2">
                    <span className="text-white font-semibold">{new Date(logItem.validFrom).toLocaleString()}</span>
                    <span className="text-slate-600">|</span>
                    <span>Actor: <span className="text-slate-300 font-sans">{logItem.modifiedBy}</span></span>
                    <span className="text-slate-600">|</span>
                    <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                      logItem.captureType === 'INSERT' ? 'bg-emerald-500/10 text-emerald-400' :
                      logItem.captureType === 'DELETE' ? 'bg-red-500/10 text-red-400' : 'bg-blue-500/10 text-blue-400'
                    }`}>{logItem.captureType}</span>
                    <span className="text-slate-600">|</span>
                    <span>Rev: v{logItem.version}</span>
                  </div>
                  
                  {/* Delta diff mapping rendering */}
                  {logItem.fieldDeltas && Object.keys(logItem.fieldDeltas).length > 0 && (
                    <div className="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-2 bg-slate-950 p-3 rounded-xl border border-slate-800/60 font-mono text-xs">
                      {Object.entries(logItem.fieldDeltas).map(([field, deltaStr]) => (
                        <div key={field} className="text-slate-400">
                          <span className="text-slate-500 font-semibold">{field}:</span> <span className="text-slate-300">{String(deltaStr)}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}