import React, { useState, createContext, useContext, useEffect } from 'react';
import { Toaster } from 'react-hot-toast';

// Core Application Domain View Components
import HomePage from './domains/dashboard/components/HomePage';
import LoginPage from './domains/auth/components/LoginPage';
import ContactDetail from './domains/contacts/components/ContactDetail';
import ImportJobStatusDashboard from './domains/imports/components/ImportJobStatusDashboard';
import DelegatedAdminManager from './domains/contacts/components/DelegatedAdminManager';

// Iconography Asset Pack
import { Users, Layers, ShieldCheck, Database, Search, Plus, Building2, ArrowRight, Home, LogOut } from 'lucide-react';

// Pre-defined multi-tenant testing UUID structures
const CONFIG_MOCK_TENANTS = [
  { id: '11111111-1111-1111-1111-111111111111', name: 'Acme Corporate Europe' },
  { id: '22222222-2222-2222-2222-222222222222', name: 'Stark Global Logistics' },
  { id: '00000000-0000-0000-0000-000000000000', name: 'Default Root Sandbox' }
];

const MOCK_CONTACTS_REGISTRY = [
  { id: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', name: 'Peter Gibbons', email: 'peter@initech.com', phoneNumber: '+1-555-0199', systemRole: 'Standard User', company: 'Initech Corp', tenantId: '11111111-1111-1111-1111-111111111111' },
  { id: 'b1ffbc88-9d0c-5ef9-cc7e-7cc0cd491b22', name: 'Samir Nagheenanajar', email: 'samir@initech.com', phoneNumber: '+1-555-0144', systemRole: 'Delegated Admin', company: 'Initech Corp', tenantId: '11111111-1111-1111-1111-111111111111' },
  { id: 'c4fdc994-2c0b-4ef8-dd8e-8dd9bd380a33', name: 'Tony Stark', email: 'tony@starkindustries.com', phoneNumber: '+1-555-3000', systemRole: 'Delegated Admin', company: 'Stark Industries', tenantId: '22222222-2222-2222-2222-222222222222' },
  { id: 'd5eed101-3d0b-4ef8-ee9f-9ee9bd380a44', name: 'Michael Bolton', email: 'mbolton@initech.com', phoneNumber: '+1-555-0122', systemRole: 'Standard User', company: 'Initech Corp', tenantId: '00000000-0000-0000-0000-000000000000' }
];

const TenantContext = createContext(null);

export function TenantProvider({ children, onTenantChange }) {
  const [activeTenant, setActiveTenant] = useState(() => {
    return localStorage.getItem('active_bu_id') || CONFIG_MOCK_TENANTS[0].id;
  });

  const switchTenant = (tenantId) => {
    localStorage.setItem('active_bu_id', tenantId);
    setActiveTenant(tenantId);
    if (onTenantChange) onTenantChange();
    window.dispatchEvent(new Event('storage'));
  };

  useEffect(() => {
    const handleStorageSync = () => {
      const currentValue = localStorage.getItem('active_bu_id') || CONFIG_MOCK_TENANTS[0].id;
      if (currentValue !== activeTenant) {
        setActiveTenant(currentValue);
        if (onTenantChange) onTenantChange();
      }
    };
    window.addEventListener('storage', handleStorageSync);
    return () => window.removeEventListener('storage', handleStorageSync);
  }, [activeTenant, onTenantChange]);

  return (
    <TenantContext.Provider value={{ activeTenant, switchTenant }}>
      {children}
    </TenantContext.Provider>
  );
}

export const useTenant = () => {
  const context = useContext(TenantContext);
  if (!context) throw new Error('useTenant must be used within a TenantProvider hierarchy.');
  return context;
};

// Main Routing Framework Node
function AppContent() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  // Default to centralized platform dashboard plane upon login authorization
  const [currentView, setCurrentView] = useState('home'); 
  const [selectedContactId, setSelectedContactId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const { activeTenant, switchTenant } = useTenant();

  const handleTenantSwitch = (e) => {
    switchTenant(e.target.value);
    setSelectedContactId(null);
  };

  const navigateToContact = (id) => {
    setSelectedContactId(id);
    setCurrentView('contact-detail');
  };

  const localFilteredContacts = MOCK_CONTACTS_REGISTRY.filter(contact => {
    const matchesTenant = contact.tenantId === activeTenant;
    const matchesSearch = searchQuery === '' || 
      contact.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      contact.email.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTenant && matchesSearch;
  });

  // Security Interceptor Guardrail: Escape directly to login view if tokens aren't signed
  if (!isAuthenticated) {
    return <LoginPage onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500/30 antialiased">
      <Toaster 
        position="top-right" 
        toastOptions={{ 
          duration: 4000, 
          style: { background: '#0f172a', color: '#fff', border: '1px solid #1e293b' } 
        }} 
      />
      
      {/* Universal Upper Control Bar Navigation Layout */}
      <header className="bg-slate-950 border-b border-slate-900 sticky top-0 z-50 backdrop-blur-md bg-slate-950/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex flex-col md:flex-row md:items-center justify-between gap-4 py-2 md:py-0">
          
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2 cursor-pointer" onClick={() => { setCurrentView('home'); setSelectedContactId(null); }}>
              <Database className="w-5 h-5 text-indigo-400 fill-indigo-500/10" />
              <span className="font-bold tracking-tight text-white text-sm">Enterprise Multi-Tenant App</span>
            </div>
            
            <nav className="flex items-center gap-1.5 text-xs font-semibold">
              <button
                onClick={() => { setCurrentView('home'); setSelectedContactId(null); }}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'home' ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Home className="w-3.5 h-3.5" /> Cockpit
              </button>
              <button
                onClick={() => { setCurrentView('contacts-dashboard'); setSelectedContactId(null); }}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'contacts-dashboard' || currentView === 'contact-detail'
                    ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Users className="w-3.5 h-3.5" /> Directory Rows
              </button>
              <button
                onClick={() => setCurrentView('async-imports')}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'async-imports' ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <Layers className="w-3.5 h-3.5" /> Async Workers
              </button>
              <button
                onClick={() => setCurrentView('delegated-admin')}
                className={`px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
                  currentView === 'delegated-admin' ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5" /> Delegated Admin
              </button>
            </nav>
          </div>

          {/* Global Multi-Tenant Boundary Switcher Widget & Logout Button */}
          <div className="flex items-center gap-3 self-start md:self-auto">
            <div className="flex items-center gap-2 bg-slate-900 border border-slate-800/80 rounded-xl px-2.5 py-1.5 shadow-inner">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <label className="text-[10px] uppercase tracking-wider font-bold text-slate-400">Isolation Scope:</label>
              <select
                value={activeTenant}
                onChange={handleTenantSwitch}
                className="bg-slate-950 border border-slate-800 rounded-lg text-xs text-white px-2 py-0.5 font-mono focus:outline-none focus:border-emerald-500 cursor-pointer"
              >
                {CONFIG_MOCK_TENANTS.map((tenant) => (
                  <option key={tenant.id} value={tenant.id}>
                    {tenant.name}
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={() => setIsAuthenticated(false)}
              className="p-2 bg-slate-900 hover:bg-red-950/40 border border-slate-800 text-slate-400 hover:text-red-400 rounded-xl transition-colors"
              title="Terminate Token Session Context"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>

        </div>
      </header>

      {/* Dynamic Main Page Content Rendering Hub */}
      <main className="flex-1">
        {currentView === 'home' && (
          <HomePage onNavigate={(targetView) => setCurrentView(targetView)} />
        )}

        {currentView === 'contacts-dashboard' && (
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900/40 p-4 rounded-2xl border border-slate-900/60 backdrop-blur">
              <div className="relative flex-1 max-w-md">
                <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Scan isolated context registry vectors..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800/80 rounded-xl pl-9 pr-4 py-2 text-xs font-mono text-slate-300 placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors"
                />
              </div>

              <button
                type="button"
                onClick={() => navigateToContact('new')}
                className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold px-4 py-2.5 rounded-xl transition-all shadow-lg shadow-indigo-600/10 flex items-center justify-center gap-1.5 self-start sm:self-auto"
              >
                <Plus className="w-4 h-4" /> Provision Identity Record
              </button>
            </div>

            <div className="bg-slate-900 border border-slate-800/80 rounded-2xl overflow-hidden shadow-2xl">
              <div className="px-6 py-4 border-b border-slate-800 flex items-center gap-2">
                <Users className="w-4 h-4 text-indigo-400" />
                <h2 className="text-sm font-bold text-slate-300 uppercase tracking-wider">Tenant Partition Rows</h2>
              </div>

              {localFilteredContacts.length === 0 ? (
                <div className="p-12 text-center text-slate-500 font-mono text-xs italic">
                  No active data boundaries map to current isolation scope or query filters.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-800 bg-slate-950/40 font-mono text-[11px] text-slate-500 uppercase tracking-wider">
                        <th className="px-6 py-3.5 font-semibold">Profile Identity Coordinate</th>
                        <th className="px-6 py-3.5 font-semibold">Corporate Context</th>
                        <th className="px-6 py-3.5 font-semibold">System Clearances</th>
                        <th className="px-6 py-3.5 font-semibold text-right">Action Interface</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/60 text-xs">
                      {localFilteredContacts.map((contact) => (
                        <tr key={contact.id} className="hover:bg-slate-900/30 group transition-colors">
                          <td className="px-6 py-4">
                            <div className="font-semibold text-slate-200 text-sm">{contact.name}</div>
                            <div className="font-mono text-slate-500 text-[11px] mt-0.5">{contact.email}</div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-1.5 text-slate-300">
                              <Building2 className="w-3.5 h-3.5 text-slate-500" />
                              {contact.company}
                            </div>
                            <div className="font-mono text-[10px] text-slate-600 mt-0.5">Terminal: {contact.phoneNumber}</div>
                          </td>
                          <td className="px-6 py-4">
                            <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-mono text-[10px] font-bold border ${
                              contact.systemRole === 'Delegated Admin'
                                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                                : 'bg-slate-950 text-indigo-400 border-slate-800'
                            }`}>
                              {contact.systemRole === 'Delegated Admin' && <ShieldCheck className="w-3 h-3" />}
                              {contact.systemRole}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-right">
                            <button
                              type="button"
                              onClick={() => navigateToContact(contact.id)}
                              className="text-slate-400 hover:text-white bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-lg px-3 py-1.5 font-semibold transition-all inline-flex items-center gap-1"
                            >
                              Inspect Map <ArrowRight className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
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
        {currentView === 'delegated-admin' && <DelegatedAdminManager />}
      </main>
    </div>
  );
}

export default function App() {
  return (
    <TenantProvider>
      <AppContent />
    </TenantProvider>
  );
}