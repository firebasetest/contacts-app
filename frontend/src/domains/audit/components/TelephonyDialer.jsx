import React, { useState } from 'react';
import { Phone, PhoneCall, Loader2 } from 'lucide-react';

export default function TelephonyDialer({ onDial, targetName }) {
  const [employeeLine, setEmployeeLine] = useState(localStorage.getItem('saved_employee_phone') || '');
  const [calling, setCalling] = useState(false);

  const handleInitiateCall = async (e) => {
    e.preventDefault();
    if (!employeeLine) return;
    localStorage.setItem('saved_employee_phone', employeeLine);
    
    setCalling(true);
    await onDial(employeeLine);
    setCalling(false);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-md max-w-sm">
      <div className="flex items-center gap-2 mb-3">
        <Phone className="w-5 h-5 text-emerald-400" />
        <h4 className="text-sm font-semibold text-white">Multi-Tenant Bridge Dialer</h4>
      </div>
      <form onSubmit={handleInitiateCall} className="space-y-3">
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">
            Your Device Endpoint Routing Line
          </label>
          <input
            type="tel"
            required
            placeholder="+15550100"
            value={employeeLine}
            onChange={(e) => setEmployeeLine(e.target.value)}
            className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>
        <button
          type="submit"
          disabled={calling || !employeeLine}
          className={`w-full flex items-center justify-center gap-2 rounded-lg py-2 text-xs font-medium text-white shadow transition-all ${
            calling 
              ? 'bg-slate-800 cursor-not-allowed' 
              : 'bg-emerald-600 hover:bg-emerald-500 active:scale-[0.98]'
          }`}
        >
          {calling ? (
            <>
              <Loader2 className="w-3.5 h-3.5 animate-spin text-emerald-400" />
              <span>Pinging Cloud Bridge...</span>
            </>
          ) : (
            <>
              <PhoneCall className="w-3.5 h-3.5" />
              <span>Bridge Session to {targetName || 'Customer'}</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
}