import React from 'react';
import { Home, Layers, ShieldCheck, ArrowRight } from 'lucide-react';

export default function HomePage({ onNavigate }) {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="rounded-2xl border border-slate-800/80 bg-slate-900/60 p-8 shadow-2xl">
        <div className="flex items-center gap-3">
          <div className="rounded-xl border border-indigo-500/20 bg-indigo-500/10 p-2 text-indigo-400">
            <Home className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-2xl font-semibold text-white">Platform Cockpit</h1>
            <p className="text-sm text-slate-400">
              Welcome to the tenant-aware contacts workspace. Choose a module to explore the experience.
            </p>
          </div>
        </div>

        <div className="mt-8 grid gap-4 md:grid-cols-2">
          <button
            type="button"
            onClick={() => onNavigate && onNavigate('contacts-dashboard')}
            className="rounded-xl border border-slate-800 bg-slate-950/50 p-5 text-left transition hover:border-indigo-500/40"
          >
            <div className="flex items-center gap-2 text-slate-200">
              <Layers className="h-4 w-4 text-indigo-400" />
              <span className="font-semibold">Directory Rows</span>
            </div>
            <p className="mt-2 text-sm text-slate-400">Open the contact directory and review tenant-scoped records.</p>
          </button>

          <button
            type="button"
            onClick={() => onNavigate && onNavigate('delegated-admin')}
            className="rounded-xl border border-slate-800 bg-slate-950/50 p-5 text-left transition hover:border-emerald-500/40"
          >
            <div className="flex items-center gap-2 text-slate-200">
              <ShieldCheck className="h-4 w-4 text-emerald-400" />
              <span className="font-semibold">Delegated Admin</span>
            </div>
            <p className="mt-2 text-sm text-slate-400">Review delegated admin controls and access policies.</p>
          </button>
        </div>

        <div className="mt-6 inline-flex items-center gap-2 text-sm text-slate-400">
          <span>Ready for the next workflow</span>
          <ArrowRight className="h-4 w-4" />
        </div>
      </div>
    </div>
  );
}
