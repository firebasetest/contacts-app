import React, { useEffect, useMemo, useState } from 'react';
import axiosClient from '../../../api/axiosClient';
import { ShieldCheck, Users, KeyRound, RefreshCw, Trash2, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';

export default function DelegatedAdminManager() {
  const [delegates, setDelegates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  const loadDelegates = async () => {
    setLoading(true);
    try {
      const { data } = await axiosClient.get('/delegated-admin/contacts');
      setDelegates(Array.isArray(data) ? data : []);
    } catch (error) {
      toast.error('Unable to load delegated admin assignments.');
      setDelegates([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDelegates();
  }, []);

  const summary = useMemo(() => {
    const activeCount = delegates.filter((delegate) => delegate.systemRole?.toUpperCase() === 'DELEGATED_ADMIN').length;
    return {
      activeCount,
      label: activeCount === 1 ? 'active delegate assignment' : 'active delegate assignments'
    };
  }, [delegates]);

  const revokeDelegate = async (delegate) => {
    if (!delegate?.id) return;
    setBusyId(delegate.id);
    try {
      await axiosClient.post(`/delegated-admin/contacts/${delegate.id}/revoke`);
      toast.success(`${delegate.name || 'Delegate'} was reverted to a standard user.`);
      await loadDelegates();
    } catch (error) {
      toast.error('The revocation request failed.');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="rounded-2xl border border-slate-800/80 bg-slate-900/60 p-6 shadow-2xl">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-3">
            <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/10 p-2 text-emerald-400">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-white">Delegated Admin Workspace</h2>
              <p className="text-sm text-slate-400">
                Review tenant-scoped delegates and revoke access when scope changes.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={loadDelegates}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-700 bg-slate-950 px-3 py-2 text-sm font-medium text-slate-300 transition hover:border-indigo-500 hover:text-white"
          >
            <RefreshCw className="h-4 w-4" /> Refresh
          </button>
        </div>

        <div className="mt-6 grid gap-4 lg:grid-cols-[1.2fr_0.8fr]">
          <div className="rounded-xl border border-slate-800 bg-slate-950/50 p-4">
            <div className="flex items-center gap-2 text-slate-300">
              <Users className="h-4 w-4 text-indigo-400" />
              <span className="text-sm font-semibold">Assigned Delegates</span>
            </div>
            <p className="mt-2 text-sm text-slate-400">
              {summary.activeCount} {summary.label} in the current tenant boundary.
            </p>

            {loading ? (
              <div className="mt-4 flex items-center gap-2 text-sm text-slate-400">
                <Loader2 className="h-4 w-4 animate-spin" /> Loading delegates...
              </div>
            ) : delegates.length === 0 ? (
              <div className="mt-4 rounded-lg border border-dashed border-slate-800 p-4 text-sm text-slate-500">
                No delegated admins are currently assigned in this tenant workspace.
              </div>
            ) : (
              <div className="mt-4 space-y-3">
                {delegates.map((delegate) => (
                  <div key={delegate.id} className="flex items-center justify-between rounded-lg border border-slate-800 bg-slate-900/70 px-3 py-3">
                    <div>
                      <div className="font-medium text-white">{delegate.name || 'Unnamed delegate'}</div>
                      <div className="text-xs text-slate-500">{delegate.email || 'No email on record'}</div>
                    </div>
                    <button
                      type="button"
                      onClick={() => revokeDelegate(delegate)}
                      disabled={busyId === delegate.id}
                      className="inline-flex items-center gap-2 rounded-lg border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-300 transition hover:border-rose-400 hover:bg-rose-500/20 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {busyId === delegate.id ? <Loader2 className="h-4 w-4 animate-spin" /> : <Trash2 className="h-4 w-4" />}
                      Revoke
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-950/50 p-4">
            <div className="flex items-center gap-2 text-slate-300">
              <KeyRound className="h-4 w-4 text-amber-400" />
              <span className="text-sm font-semibold">Access Controls</span>
            </div>
            <ul className="mt-3 space-y-2 text-sm text-slate-400">
              <li>• Tenant-scoped delegates inherit elevated review and escalation access.</li>
              <li>• Revoke actions immediately reduce the assignment back to a standard profile.</li>
              <li>• This workspace is now backed by the same role lifecycle as the contact detail view.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
