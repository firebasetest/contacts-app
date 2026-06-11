import React, { useState } from 'react';
import { useContactProfile } from '../hooks/useContactProfile';
import TelephonyDialer from '../../audit/components/TelephonyDialer';
import { Calendar, ShieldAlert, History, Download, Trash2, ArrowLeft, RefreshCw } from 'lucide-react';

export default function ContactDetail({ contactId, onBack }) {
  const {
    profile,
    historicalState,
    auditTrail,
    loading,
    error,
    fetchAsOf,
    clearHistory,
    triggerCallBridge,
    delegateAdmin,
    exportGdprData,
    anonymizeGdprData
  } = useContactProfile(contactId);

  const [asOfTimestamp, setAsOfTimestamp] = useState('');

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-slate-400">
        <RefreshCw className="w-8 h-8 animate-spin text-indigo-500" />
        <p className="text-sm">Loading multi-tenant workspace registry...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-950/20 border border-red-900 rounded-xl p-6 text-center max-w-xl mx-auto my-8">
        <ShieldAlert className="w-10 h-10 text-red-500 mx-auto mb-2" />
        <h3 className="text-lg font-bold text-white mb-1">Access Restrained</h3>
        <p className="text-sm text-red-400">{error}</p>
        <button onClick={onBack} className="mt-4 inline-flex items-center gap-1 text-xs text-white bg-slate-800 px-3 py-2 rounded-lg hover:bg-slate-700">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Dashboard
        </button>
      </div>
    );
  }

  // Display historical state if time travel mode is active, fallback to current state
  const activeRecord = historicalState || profile;
  const isTimeTraveling = !!historicalState;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-white space-y-8">
      {/* Upper Navigation Header bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div className="flex items-center gap-4">
          <button onClick={onBack} className="p-2 hover:bg-slate-800 rounded-lg transition-colors border border-slate-700 text-slate-400 hover:text-white">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold tracking-tight">{activeRecord.name}</h1>
              {isTimeTraveling && (
                <span className="bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs px-2.5 py-0.5 rounded-full font-semibold animate-pulse flex items-center gap-1">
                  <History className="w-3 h-3" /> Historical Snapshot Mode
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400 mt-1 font-mono">Workspace Global UUID: {activeRecord.id}</p>
          </div>
        </div>

        {/* Temporal Time-Travel Controller */}
        <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 p-2 rounded-xl shadow-inner">
          <div className="flex items-center gap-1.5 text-slate-400 px-2 text-xs font-medium">
            <Calendar className="w-3.5 h-3.5 text-indigo-400" />
            <span>Temporal View</span>
          </div>
          <input
            type="datetime-local"
            value={asOfTimestamp}
            onChange={(e) => setAsOfTimestamp(e.target.value)}
            className="bg-slate-950 border border-slate-700 rounded-lg px-2 py-1 text-xs font-mono text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            onClick={() => fetchAsOf(new Date(asOfTimestamp).toISOString())}
            disabled={!asOfTimestamp}
            className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-600 px-3 py-1 rounded-lg text-xs font-semibold transition-colors"
          >
            Go
          </button>
          {isTimeTraveling && (
            <button onClick={clearHistory} className="text-xs text-slate-400 hover:text-white underline px-1">
              Reset
            </button>
          )}
        </div>
      </div>

      {/* Main Structural Layout Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Core Attributes Panel & Telephony Integration Component */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 backdrop-blur space-y-6">
            <h3 className="text-sm font-bold tracking-wider text-slate-400 uppercase">Core Directory Parameters</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-medium text-slate-500">Email Address Registry</label>
                <p className="text-sm font-medium mt-1 break-all">{activeRecord.email || '—'}</p>
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500">Phone Terminal Number</label>
                <p className="text-sm font-medium mt-1 font-mono">{activeRecord.phoneNumber || '—'}</p>
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500">System Role Scope</label>
                <p className="text-sm font-medium mt-1 text-indigo-400 font-mono">{activeRecord.systemRole}</p>
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500">Verification Source Engine</label>
                <p className="text-sm font-medium mt-1 text-slate-400 text-xs">{activeRecord.source || 'Direct Entry'}</p>
              </div>
            </div>

            {/* JSONB Custom Attribute Dictionary Inspector */}
            {activeRecord.customAttributes && Object.keys(activeRecord.customAttributes).length > 0 && (
              <div className="border-t border-slate-800 pt-6">
                <h4 className="text-xs font-bold text-slate-400 tracking-wider uppercase mb-3">Dynamic Extended Metadata (JSONB)</h4>
                <div className="bg-slate-950 rounded-xl p-4 border border-slate-800 grid grid-cols-1 sm:grid-cols-2 gap-4 font-mono text-xs">
                  {Object.entries(activeRecord.customAttributes).map(([key, val]) => (
                    <div key={key} className="flex flex-col gap-1 bg-slate-900/60 p-2.5 rounded-lg border border-slate-800">
                      <span className="text-slate-500 text-[10px] uppercase font-sans tracking-wide">{key.replace(/_/g, ' ')}</span>
                      <span className="text-slate-200 font-medium">{String(val)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Connected Action Systems Section */}
          {!isTimeTraveling && (
            <div className="flex flex-wrap gap-4 items-center justify-between p-6 bg-slate-900/20 border border-slate-800/60 rounded-2xl">
              <div>
                <h4 className="text-sm font-bold text-white">Delegated Administration Workflow</h4>
                <p className="text-xs text-slate-400 mt-0.5">Elevate profile status coordinates directly within the client domain partition.</p>
              </div>
              <button onClick={delegateAdmin} className="bg-slate-800 hover:bg-slate-700 text-xs font-semibold px-4 py-2 rounded-xl transition-colors border border-slate-700">
                Execute Administrative Escalation
              </button>
            </div>
          )}
        </div>

        {/* Action Panel Sidebar Container */}
        <div className="space-y-6">
          {/* Telephony Dialer Mounting Zone */}
          {!isTimeTraveling && (
            <TelephonyDialer onDial={triggerCallBridge} targetName={activeRecord.name} />
          )}

          {/* GDPR Governance Enforcement Console */}
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-5 space-y-4">
            <h3 className="text-xs font-bold text-slate-400 tracking-wider uppercase flex items-center gap-1.5">
              <ShieldAlert className="w-4 h-4 text-indigo-400" /> GDPR Compliance System Tools
            </h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Enforce structural parameters matching sovereign workspace compliance data privacy regulations.
            </p>
            <div className="grid grid-cols-1 gap-2.5 pt-1">
              <button
                onClick={exportGdprData}
                className="w-full flex items-center justify-center gap-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-semibold py-2 rounded-xl text-slate-200 transition-colors"
              >
                <Download className="w-3.5 h-3.5" /> Pack & Export Metadata (Art 20)
              </button>
              <button
                onClick={anonymizeGdprData}
                className="w-full flex items-center justify-center gap-2 bg-red-950/30 hover:bg-red-900/40 border border-red-900/50 text-xs font-semibold py-2 rounded-xl text-red-400 transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" /> Execute Data Erasure (Art 17)
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Database Change Ledger Timeline Component */}
      <div className="bg-slate-900/30 border border-slate-800/80 rounded-2xl p-6">
        <h3 className="text-sm font-bold tracking-wider text-slate-400 uppercase mb-5 flex items-center gap-2">
          <History className="w-4 h-4 text-indigo-400" /> Chronological Workspace Audit Trail Ledger
        </h3>
        {auditTrail.length === 0 ? (
          <p className="text-xs text-slate-500 font-mono italic">No mutating audit transactions recorded against this resource boundary context.</p>
        ) : (
          <div className="relative border-l-2 border-slate-800 pl-4 space-y-6 ml-2">
            {auditTrail.map((logItem) => (
              <div key={logItem.id} className="relative group">
                {/* Node Milestone Tracker icon bubble */}
                <div className="absolute -left-[23px] top-1 w-2.5 h-2.5 rounded-full bg-indigo-500 border border-slate-950 group-hover:scale-125 transition-transform" />
                <div className="text-xs font-mono text-slate-400 flex items-center gap-2">
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
                
                {/* Granular Update Delta mapping lists */}
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
    </div>
  );
}