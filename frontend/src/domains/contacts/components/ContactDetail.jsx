import React from 'react';
import { User, Info, History, Key, Trash2, Calendar } from 'lucide-react';
import { useContactProfile } from '../hooks/useContactProfile';
import { TelephonyDialer } from '../../telephony/components/TelephonyDialer';

export const ContactDetail = ({ entityId, tenantConfig, employeePhone }) => {
  const isAuditEnabled = tenantConfig?.isAuditViewEnabled;
  
  const {
    profile,
    changelog,
    loading,
    activeTab,
    switchTab,
    executeGdprErasure
  } = useContactProfile(entityId, isAuditEnabled);

  if (loading) return <div className="p-8 text-center text-gray-500 font-medium">Streaming profile elements...</div>;
  if (!profile) return <div className="p-8 text-center text-red-500 font-medium">Target profile tracking record missing.</div>;

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
      {/* Header Profile Info Panel Layout */}
      <div className="bg-slate-900 p-6 text-white flex justify-between items-start">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-white/10 rounded-xl flex items-center justify-center text-blue-400">
            <User className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold">{profile.name}</h2>
            <p className="text-sm text-slate-400 font-mono">UUID: {profile.id}</p>
          </div>
        </div>
        <span className="px-3 py-1 bg-blue-500/20 text-blue-300 border border-blue-500/30 text-xs font-bold rounded-md">
          {profile.email ? 'INDIVIDUAL CONTACT' : 'COMPANY ACCOUNT'}
        </span>
      </div>

      {/* Domain Navigation Controls */}
      <div className="flex border-b border-gray-200 bg-slate-50">
        <button
          onClick={() => switchTab('details')}
          className={`flex items-center gap-2 px-6 py-3 text-sm font-semibold border-b-2 transition-colors ${
            activeTab === 'details' ? 'border-blue-600 text-blue-600 bg-white' : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Info className="w-4 h-4" />
          <span>Profile Specifications</span>
        </button>
        {isAuditEnabled && (
          <button
            onClick={() => switchTab('audit')}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-semibold border-b-2 transition-colors ${
              activeTab === 'audit' ? 'border-blue-600 text-blue-600 bg-white' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            <History className="w-4 h-4" />
            <span>Temporal Audit Ledger</span>
          </button>
        )}
      </div>

      {/* Viewport Frame Router */}
      <div className="p-6">
        {activeTab === 'details' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">E-Mail Endpoint</label>
                <p className="text-gray-900 font-medium">{profile.email || 'N/A'}</p>
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">Telephony Linkage</label>
                <div>
                  <TelephonyDialer 
                    phoneNumber={profile.phoneNumber} 
                    contactName={profile.name} 
                    tenantConfig={tenantConfig}
                    employeePhone={employeePhone}
                  />
                </div>
              </div>
            </div>

            {/* Render Flexible Schemas Safely */}
            {profile.customAttributes && Object.keys(profile.customAttributes).length > 0 && (
              <div className="pt-4 border-t border-gray-100">
                <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Dynamic Schema Parameters</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 bg-slate-50 p-4 rounded-lg border border-gray-100">
                  {Object.entries(profile.customAttributes).map(([key, value]) => (
                    <div key={key} className="bg-white p-2.5 rounded border border-gray-200">
                      <span className="block text-[11px] font-bold text-slate-400 capitalize">{key.replace(/_/g, ' ')}</span>
                      <span className="text-sm font-medium text-slate-800">{String(value)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Secure Infrastructure Operations Frame */}
            {tenantConfig?.isGdprEnabled && (
              <div className="pt-6 border-t border-gray-100 flex justify-end gap-3 bg-red-50/50 -mx-6 -mb-6 p-6 mt-8">
                <div className="mr-auto self-center text-xs text-slate-500 font-medium flex items-center gap-1.5">
                  <Key className="w-3.5 h-3.5 text-red-400" />
                  <span>Regulatory Protection Module Live</span>
                </div>
                <button
                  onClick={executeGdprErasure}
                  className="flex items-center gap-2 px-4 py-2 bg-white text-red-700 hover:bg-red-50 border border-red-200 rounded-lg text-sm font-semibold shadow-sm transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Purge PII Ledger</span>
                </button>
              </div>
            )}
          </div>
        )}

        {activeTab === 'audit' && (
          <div className="flow-root">
            <h3 className="text-sm font-bold text-slate-800 mb-6">Chronological History Activity Trail</h3>
            <ul className="-mb-8">
              {changelog.map((entry, idx) => (
                <li key={entry.historyId}>
                  <div className="relative pb-8">
                    {idx !== changelog.length - 1 && (
                      <span className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200" aria-hidden="true" />
                    )}
                    <div className="relative flex space-x-3">
                      <div>
                        <span className={`h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white text-white ${
                          entry.captureType === 'INSERT' ? 'bg-green-500' : 'bg-blue-500'
                        }`}>
                          <Calendar className="w-4 h-4" />
                        </span>
                      </div>
                      <div className="flex-1 min-w-0 pt-1.5 flex justify-between space-x-4">
                        <div>
                          <p className="text-sm text-gray-500">
                            Action <span className="font-bold text-gray-800">{entry.captureType}</span> mapped by{' '}
                            <span className="font-medium text-slate-700 underline">{entry.modifiedBy}</span>
                          </p>
                          {entry.fieldDeltas && (
                            <div className="mt-2 text-xs bg-slate-50 p-2 rounded border border-gray-100 font-mono text-slate-600 space-y-1">
                              {Object.entries(entry.fieldDeltas).map(([field, details]) => (
                                <div key={field}>
                                  <span className="text-blue-600 font-semibold">{field}:</span> {details}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                        <div className="text-right text-xs whitespace-nowrap text-gray-400 self-start">
                          {new Date(entry.validFrom).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};