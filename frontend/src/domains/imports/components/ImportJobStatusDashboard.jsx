import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { toast } from 'react-hot-toast';
import { RefreshCw, CheckCircle2, AlertTriangle, FileText, Layers } from 'lucide-react';
import CustomFileProcessor from './CustomFileProcessor';

export default function ImportJobStatusDashboard() {
  const [jobs, setJobs] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  // Generates safety request headers supplying authorization tokens and the tenant identity context
  const getHeaders = () => {
    const businessUnitId = localStorage.getItem('active_bu_id') || '00000000-0000-0000-0000-000000000000';
    const token = localStorage.getItem('auth_token');
    return {
      'X-BU-ID': businessUnitId,
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  };

  // Queries the backend repository queue for processing jobs corresponding to the current tenant context
  const fetchJobs = useCallback(async (showIndicator = false) => {
    if (showIndicator) setRefreshing(true);
    try {
      const res = await axios.get('/api/v1/imports/jobs', { headers: getHeaders() });
      setJobs(res.data || []);
    } catch (err) {
      console.error('Failed to fetch import job history queue:', err);
    } finally {
      if (showIndicator) setRefreshing(false);
    }
  }, []);

  // Triggers the initial query map on initialization and establishes a background pooling interval
  useEffect(() => {
    fetchJobs();
    const interval = setInterval(() => fetchJobs(false), 5000);
    return () => clearInterval(interval);
  }, [fetchJobs]);

  // Processes the verified file layout returned from the local client quality gate components
  const handleInboundProcessorPayload = async ({ file, headerMappings }) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('mappings', JSON.stringify(headerMappings));

    try {
      await axios.post('/api/v1/imports/upload', formData, {
        headers: {
          ...getHeaders(),
          'Content-Type': 'multipart/form-data'
        }
      });
      toast.success('Asynchronous stream processor successfully provisioned!');
      fetchJobs(false); // Instantly refresh status records without waiting for the next polling cycle
    } catch (err) {
      toast.error(err.response?.data?.message || 'File parsing ingest handshake failed on system gate.');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-white space-y-8">
      
      {/* Upper Dashboard Dashboard Meta Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight flex items-center gap-2">
            <Layers className="w-6 h-6 text-indigo-400" /> Asynchronous Data Ingestion Engine
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Monitor and trigger scalable tenant-isolated batch jobs with asynchronous error isolation.
          </p>
        </div>
        <button
          onClick={() => fetchJobs(true)}
          disabled={refreshing}
          className="p-2 bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-400 hover:text-white rounded-xl transition-colors flex items-center gap-2 text-xs"
        >
          <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin text-indigo-400' : ''}`} />
          Refresh Registry
        </button>
      </div>

      {/* Main Structural Layout Grid Matrix */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        
        {/* Left Column Container: Houses the client-side pre-flight file verification logic */}
        <div className="lg:col-span-1">
          <CustomFileProcessor onProcessingComplete={handleInboundProcessorPayload} />
        </div>

        {/* Right Column Container: Renders dynamic background thread queue items */}
        <div className="lg:col-span-2 space-y-4">
          <h3 className="text-xs font-bold tracking-wider text-slate-400 uppercase">Active Core Thread Allocations</h3>
          
          {jobs.length === 0 ? (
            <div className="bg-slate-900/10 border border-slate-800/60 rounded-2xl p-12 text-center text-slate-500 italic text-sm">
              No recent import events have been logged inside this workspace partition context boundary.
            </div>
          ) : (
            <div className="space-y-3">
              {jobs.map((job) => {
                const percentage = job.totalRows > 0 ? Math.round((job.processedRows / job.totalRows) * 100) : 0;
                
                return (
                  <div key={job.id} className="bg-slate-900/50 border border-slate-800 rounded-xl p-4 space-y-3">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex items-center gap-2.5">
                        <FileText className="w-4 h-4 text-slate-400" />
                        <div>
                          <h4 className="text-sm font-bold text-slate-200">{job.fileName}</h4>
                          <p className="text-[10px] font-mono text-slate-500">Task-UUID: {job.id}</p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                          job.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                          job.status === 'FAILED' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                          'bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse'
                        }`}>
                          {job.status === 'COMPLETED' && <CheckCircle2 className="w-3 h-3" />}
                          {job.status === 'FAILED' && <AlertTriangle className="w-3 h-3" />}
                          {job.status}
                        </span>
                      </div>
                    </div>

                    {/* Progress Metrics Representation Layout */}
                    <div className="space-y-1">
                      <div className="flex justify-between text-[11px] font-mono text-slate-400">
                        <span>Ingested Rows: {job.processedRows} / {job.totalRows || 'Calculating...'}</span>
                        <span>{percentage}%</span>
                      </div>
                      <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden border border-slate-800">
                        <div
                          className={`h-1.5 rounded-full transition-all duration-500 ${
                            job.status === 'COMPLETED' ? 'bg-emerald-500' :
                            job.status === 'FAILED' ? 'bg-red-500' : 'bg-indigo-500'
                          }`}
                          style={{ width: `${Math.min(percentage, 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Exception Stack trace tracking panel layout */}
                    {job.errorMessage && (
                      <div className="bg-red-950/20 border border-red-900/30 text-red-400 p-2.5 rounded-lg font-mono text-[11px] break-all">
                        <span className="font-bold block uppercase text-[9px] text-red-500 tracking-wide mb-0.5">Execution Log Interruption:</span>
                        {job.errorMessage}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}