import React, { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import ContactDetail from './domains/contacts/components/ContactDetail';
import ImportJobStatusDashboard from './domains/imports/components/ImportJobStatusDashboard';
import { Users, Layers, ShieldCheck, Database } from 'lucide-react';

// Pre-defined multi-tenant testing UUID structures (corresponding to your database seeds)
const CONFIG_MOCK_TENANTS = [
  { id: '11111111-1111-1111-1111-111111111111', name: 'Acme Corporate Europe' },
  { id: '22222222-2222-2222-2222-222222222222', name: 'Stark Global Logistics' },
  { id: '00000000-0000-0000-0000-000000000000', name: 'Default Root Sandbox' }
];

export default function App() {
  const [currentView, setCurrentView] = useState('contacts-dashboard'); // View toggles
  const [selectedContactId, setSelectedContactId] = useState(null);
  
  // Set up tenant tracking inside the application configuration context
  const [activeTenant, setActiveTenant] = useState(
    localStorage.getItem('active_bu_id') || CONFIG_MOCK_TENANTS[0].id
  );

  const handleTenantSwitch = (e) => {
    const selectedId = e.target.value;
    localStorage.setItem('active_bu_id', selectedId);
    setActiveTenant(selectedId);
    // Force reset application viewport loops to verify fresh security context hydration
    setSelectedContactId(null);
    window.dispatchEvent(new Event('storage'));
  };

  const navigateToContact = (id) => {
    setSelectedContactId(id);
    setCurrentView('contact-detail');
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500/30">
      <Toaster position="top-right" toastOptions={{ duration: 4000, style: { background: '#0f172a', color: '#fff', border: '1px solid #1e293b' } }} />
      
      {/* Universal Upper Control bar navigation layout */}
      <header className="bg-slate-900 border-b border-slate-800/80 sticky top-0 z-50 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
          
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2 cursor-pointer" onClick={() => setCurrentView('contacts-dashboard')}>
              <Database className="w-5 h-5 text-indigo-400 fill-indigo-500/10" />
              <span className="font-bold tracking-tight text-white text-sm">Enterprise Multi-Tenant App</span>
            </div>
            
            <nav className="flex items-center gap-1.5 text-xs font-semibold">
              <button
                onClick={() => setCurrentView('contacts-dashboard')}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'contacts-dashboard' || currentView === 'contact-detail'
                    ? 'bg-indigo-600 text-white shadow'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Users className="w-3.5 h-3.5" /> Directory Rows
              </button>
              <button
                onClick={() => setCurrentView('async-imports')}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'async-imports'
                    ? 'bg-indigo-600 text-white shadow'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Layers className="w-3.5 h-3.5" /> Async Workers
              </button>
            </nav>
          </div>

          {/* Global Multi-Tenant Boundary Switcher Widget */}
          <div className="flex items-center gap-2 bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 shadow-inner">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <label className="text-[10px] uppercase tracking-wider font-bold text-slate-500">Active Boundary Isolation Context:</label>
            <select
              value={activeTenant}
              onChange={handleTenantSwitch}
              className="bg-slate-900 border border-slate-700 rounded-lg text-xs text-white px-2 py-0.5 font-mono focus:outline-none focus:border-emerald-500 cursor-pointer"
            >
              {CONFIG_MOCK_TENANTS.map((tenant) => (
                <option key={tenant.id} value={tenant.id} className="font-sans">
                  {tenant.name}
                </option>
              ))}
            </select>
          </div>

        </div>
      </header>

      {/* Dynamic Main Page Content Rendering Hub */}
      <main className="flex-1">
        {currentView === 'contacts-dashboard' && (
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="bg-slate-900/30 border border-slate-800/80 rounded-2xl p-6 text-center space-y-4">
              <h2 className="text-xl font-bold">Contact Directory Dashboard</h2>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                Toggle between isolation environments using the header switch above, then pick an individual to verify temporal database auditing trail sets.
              </p>
              {/* Mock Selection Trigger to launch specific view layout detail pane */}
              <div className="pt-4 flex justify-center gap-3">
                <button
                  onClick={() => navigateToContact('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11')}
                  className="bg-slate-800 hover:bg-slate-700 text-xs font-semibold px-4 py-2 rounded-xl transition-colors border border-slate-700"
                >
                  Inspect Mock Contact Alpha
                </button>
                <button
                  onClick={() => navigateToContact('b1ffbc88-9d0c-5ef9-cc7e-7cc0cd491b22')}
                  className="bg-slate-800 hover:bg-slate-700 text-xs font-semibold px-4 py-2 rounded-xl transition-colors border border-slate-700"
                >
                  Inspect Mock Contact Beta
                </button>
              </div>
            </div>
          </div>
        )}

        {currentView === 'contact-detail' && (
          <ContactDetail
            contactId={selectedContactId}
            onBack={() => setCurrentView('contacts-dashboard')}
          />
        )}

        {currentView === 'async-imports' && <ImportJobStatusDashboard />}
      </main>

    </div>
  );
}