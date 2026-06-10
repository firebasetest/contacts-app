import React from 'react';
import { Phone, PhoneForwarded } from 'lucide-react';
import axios from 'axios';

export const TelephonyDialer = ({ contactPhone, contactName, tenantConfig, employeePhone }) => {
  if (!contactPhone) return <span className="text-gray-400">No number</span>;

  const provider = tenantConfig?.telephony?.provider || 'NATIVE_TEL';
  const cleanNumber = contactPhone.replace(/[^\d+]/g, '');

  // Tier 1: Small BUs (Zero Cost Native Protocol)
  if (provider === 'NATIVE_TEL') {
    return (
      <a
        href={`tel:${cleanNumber}`}
        className="inline-flex items-center gap-1.5 text-blue-600 hover:underline"
        title="Dial via system default app"
      >
        <Phone className="w-4 h-4" />
        <span>{contactPhone}</span>
      </a>
    );
  }

  // Tier 2: Enterprise Twilio Cloud Ingestion
  if (provider === 'TWILIO') {
    const triggerTwilioCall = async () => {
      try {
        await axios.post('/api/v1/telephony/call', null, {
          params: { employeePhone, contactPhone: cleanNumber }
        });
      } catch (err) {
        alert("Cloud dialer error: " + err.message);
      }
    };

    return (
      <button
        onClick={triggerTwilioCall}
        className="inline-flex items-center gap-1.5 bg-green-50 text-green-700 hover:bg-green-100 px-2 py-1 rounded border border-green-200 text-xs font-medium"
      >
        <PhoneForwarded className="w-3.5 h-3.5" />
        <span>Call via Cloud ({contactPhone})</span>
      </button>
    );
  }

  // Tier 3: Enterprise Microsoft Teams Deep Linking
  if (provider === 'MS_TEAMS') {
    // Teams utilizes a specific URI protocol handler to trigger its client engine directly
    const teamsUrl = `https://teams.microsoft.com/l/call/0/0?users=${cleanNumber}`;
    return (
      <a
        href={teamsUrl}
        className="inline-flex items-center gap-1.5 text-purple-600 hover:underline font-medium text-xs"
        title="Launch Microsoft Teams Direct Dial"
      >
        <span className="bg-purple-100 text-purple-700 px-1.5 py-0.5 rounded text-[10px] font-bold mr-1">TEAMS</span>
        <span>{contactPhone}</span>
      </a>
    );
  }

  // Fallback
  return <span>{contactPhone}</span>;
};