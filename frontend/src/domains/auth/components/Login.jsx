import React, { useState } from 'react';
import { useAuth } from './AuthContext';
import { Shield, KeyRound, ArrowRight, Lock, User, RefreshCw } from 'lucide-react';

export default function Login() {
  const { loginInternal, loginExternalSSO, loading } = useAuth();
  const [showFallback, setShowFallback] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmitInternal = async (e) => {
    e.preventDefault();
    await loginInternal(username, password);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-2xl p-8 space-y-6 shadow-2xl">
        
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 bg-indigo-600/10 border border-indigo-500/20 rounded-2xl flex items-center justify-center mx-auto text-indigo-400">
            <Shield className="w-6 h-6" />
          </div>
          <h2 className="text-xl font-bold tracking-tight">System Authentication</h2>
          <p className="text-xs text-slate-400">Choose your preferred access method to continue</p>
        </div>

        {/* Primary Option: External SSO */}
        <div className="space-y-3">
          <button
            onClick={loginExternalSSO}
            type="button"
            className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 px-4 rounded-xl text-xs transition-all flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/20"
          >
            <KeyRound className="w-4 h-4" /> Sign in with Enterprise SSO (OIDC)
          </button>
        </div>

        {/* Divider */}
        <div className="relative flex items-center justify-center">
          <div className="border-t border-slate-800 w-full" />
          <span className="bg-slate-900 px-3 text-[10px] font-bold text-slate-500 uppercase tracking-wider absolute">
            Or
          </span>
        </div>

        {/* Secondary Option: Internal Fallback Toggle */}
        {!showFallback ? (
          <button
            onClick={() => setShowFallback(true)}
            type="button"
            className="w-full bg-slate-950 border border-slate-800 hover:border-slate-700 text-slate-300 font-semibold py-2.5 px-4 rounded-xl text-xs transition-all flex items-center justify-center gap-2"
          >
            Use Internal Account Credentials <ArrowRight className="w-3.5 h-3.5" />
          </button>
        ) : (
          /* Internal Fallback Form */
          <form onSubmit={handleSubmitInternal} className="space-y-4 pt-2 animate-fadeIn">
            <div className="space-y-1">
              <label className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Username</label>
              <div className="relative">
                <User className="w-4 h-4 absolute left-3 top-3 text-slate-500" />
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="admin"
                  className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-200 outline-none"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 absolute left-3 top-3 text-slate-500" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-200 outline-none"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold py-2.5 px-4 rounded-xl text-xs transition-all flex items-center justify-center gap-2"
            >
              {loading ? <RefreshCw className="w-4 h-4 animate-spin" /> : 'Authenticate (Internal Fallback)'}
            </button>
          </form>
        )}

      </div>
    </div>
  );
}