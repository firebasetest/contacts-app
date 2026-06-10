import React, { useState } from 'react';
import { Phone, PhoneCall, Loader2 } from 'lucide-react';
import axios from 'axios';

export const TelephonyDialer = ({ phoneNumber, contactName, tenantConfig, employeePhone }) => {
  const [isDialing, setIsDialing] = useState(false);
  
  if (!phoneNumber) {
    return <span className="text-gray-400 italic text-sm">Not provided</span>;
  }
  
  const provider = tenantConfig?.telephonyProvider || 'NATIVE_TEL';
  const cleanNumber = phoneNumber.replace(/[^\d+]/g, '');

  // Tier 1: System Baseline Protocol Interception
  if (provider === 'NATIVE_TEL') {
    return (
      <a
        href={`tel:${cleanNumber}`}
        className="inline-flex items-center gap-1.5 text-blue-600 hover:text-blue-800 font-medium hover:underline group"
        title={`Dial ${contactName} via system link`}
      >
        <Phone className="w-3.5 h-3.5 text-blue-500 group-hover:scale-110 transition-transform" />
        <span>{phoneNumber}</span>
      </a>
    );
  }

  // Tier 2: Cloud VoIP Call Bridging (Twilio Infrastructure Engine)
  if (provider === 'TWILIO') {
    const executeVoipBridge = async () => {
      if (!employeePhone) {
        alert("Please set your personal employee device extension before executing cloud outbound dialing.");
        return;
      }
      setIsDialing(true);
      try {
        await axios.post('/api/v1/telephony/call', null, {
          params: { employeePhone, contactPhone: cleanNumber }
        });
        alert(`Routing call. Please pick up your extension line (${employeePhone}) to connect.`);
      } catch (err) {
        alert("Telephony Ingestion Gateway Error: " + (err.response?.data || err.message));
      } finally {
        setIsDialing(false);
      }
    };

    return (
      <button
        onClick={executeVoipBridge}
        disabled={isDialing}
        className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-green-50 text-green-700 hover:bg-green-100 border border-green-200 rounded-md text-xs font-semibold shadow-sm transition-all disabled:opacity-50"
      >
        {isDialing ? (
          <Loader2 className="w-3 h-3 animate-spin text-green-600" />
        ) : (
          <PhoneCall className="w-3 h-3 text-green-600" />
        )}
        <span>Cloud Call ({phoneNumber})</span>
      </button>
    );
  }

  // Tier 3: Client Application Deep-Linking (Microsoft Teams Interceptor)
  if (provider === 'MS_TEAMS') {
    return (
      <a
        href={`https://teams.microsoft.com/l/call/0/0?users=${cleanNumber}`}
        className="inline-flex items-center gap-1.5 text-purple-700 hover:text-purple-900 font-medium hover:underline bg-purple-50 px-2 py-0.5 rounded border border-purple-100 text-xs"
      >
        <span className="text-[10px] font-bold bg-purple-600 text-white px-1 py-0.2 rounded-sm mr-0.5">TEAMS</span>
        <span>{phoneNumber}</span>
      </a>
    );
  }

  return <span className="text-gray-700">{phoneNumber}</span>;
};