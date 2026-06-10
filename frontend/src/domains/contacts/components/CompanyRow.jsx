import React from 'react';
import { Building2, Layers, ArrowUpRight } from 'lucide-react';
import { TelephonyDialer } from '../../telephony/components/TelephonyDialer';

export const CompanyRow = ({ company, tenantConfig, onSelectCompany, employeePhone }) => {
  return (
    <tr className="hover:bg-gray-50 border-b border-gray-100 transition-colors group">
      <td className="px-6 py-4">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-slate-100 text-slate-600 rounded-lg group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
            <Building2 className="w-5 h-5" />
          </div>
          <div>
            <div className="font-semibold text-gray-900 flex items-center gap-1.5">
              <span>{company.name}</span>
              <button 
                onClick={() => onSelectCompany(company.id)}
                className="text-gray-400 hover:text-blue-600 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <ArrowUpRight className="w-3.5 h-3.5" />
              </button>
            </div>
            <span className="text-xs text-gray-500 font-mono">{company.taxId || 'No Registered Tax Registration'}</span>
          </div>
        </div>
      </td>
      
      <td className="px-6 py-4">
        <div className="flex items-center gap-1.5 text-sm text-gray-600">
          <Layers className="w-4 h-4 text-gray-400" />
          <span>{company.industry || 'General Corporate Profile'}</span>
        </div>
      </td>

      <td className="px-6 py-4">
        <div className="text-sm">
          <TelephonyDialer 
            phoneNumber={company.phoneNumber} 
            contactName={company.name} 
            tenantConfig={tenantConfig} 
            employeePhone={employeePhone}
          />
        </div>
      </td>

      <td className="px-6 py-4">
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
          company.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-amber-100 text-amber-800'
        }`}>
          {company.status}
        </span>
      </td>
    </tr>
  );
};