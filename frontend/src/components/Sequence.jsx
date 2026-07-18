import React from 'react';

export function Sequence({ children, title }) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-950/40 p-4">
      {title && <h3 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">{title}</h3>}
      <div className="space-y-2">{children}</div>
    </div>
  );
}

export function Step({ children, title }) {
  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/60 p-3">
      {title && <div className="mb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-400">{title}</div>}
      <div className="text-sm text-slate-300">{children}</div>
    </div>
  );
}

export default Sequence;
