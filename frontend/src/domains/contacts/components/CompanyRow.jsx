import React from 'react';
import { Building2, ShieldCheck, UserCheck } from 'lucide-react';

export default function CompanyRow({ company, onSelect, onDelegate }) {
  const { id, name, systemRole, status, industry, taxId } = company;

  return (
    <tr className="hover:bg-slate-800/50 transition-colors border-b border-slate-800/80 cursor-pointer" onClick={() => onSelect?.(id)}>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-indigo-500/10 rounded-lg text-indigo-400">
            <Building2 className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-semibold text-white">{name}</div>
            <div className="text-xs text-slate-400">ID: {id.substring(0, 8)}...</div>
          </div>
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-300">
        <span className="bg-slate-900 border border-slate-700 px-2 py-1 rounded-md text-xs text-slate-400 font-mono">
          {industry || 'Unassigned Sector'}
        </span>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-slate-400">
        {taxId || 'N/A'}
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium ${
          status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-slate-800 text-slate-400'
        }`}>
          {status}
        </span>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm">
        <span className="inline-flex items-center gap-1 bg-indigo-500/10 text-indigo-400 px-2 py-0.5 rounded text-xs border border-indigo-500/20">
          {systemRole === 'DELEGATED_ADMIN' && <ShieldCheck className="w-3 h-3 text-amber-400" />}
          {systemRole}
        </span>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium" onClick={(e) => e.stopPropagation()}>
        <button
          onClick={() => onDelegate?.(id)}
          className="inline-flex items-center gap-1 text-xs font-semibold text-indigo-400 hover:text-indigo-300 transition-colors border border-indigo-500/30 hover:border-indigo-500 rounded-lg px-3 py-1.5 bg-indigo-950/20"
        >
          <UserCheck className="w-3.5 h-3.5" />
          <span>Delegate Admin</span>
        </button>
      </td>
    </tr>
  );
}