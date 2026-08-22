import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';
import { 
  Upload, FileSpreadsheet, ArrowRight, CheckCircle2, AlertTriangle, 
  RefreshCw, Layers, Database, ChevronRight, HelpCircle, Check, Play, FileText 
} from 'lucide-react';
import { Sequence, Step } from '../../../components/Sequence'; // Ensure path alignment with your project structure
import axiosClient from '../../../api/axiosClient';

// ==========================================
// 1. Target Schemas & Sample Mock Registries
// ==========================================
const POLYMORPHIC_TARGET_SCHEMAS = {
  Contact: {
    label: 'Contact Directory Entity',
    requiredFields: ['name', 'email'],
    optionalFields: ['phoneNumber', 'systemRole', 'source', 'twitterHandle', 'contractValue', 'tier']
  },
  Account: {
    label: 'Corporate Account Partition',
    requiredFields: ['companyName', 'domain'],
    optionalFields: ['industry', 'marketCap', 'billingCountry', 'slaTier']
  }
};

const MOCK_SAMPLE_FILES = [
  { name: 'Q2_Inbound_Leads_EU.csv', headers: ['Full Name', 'Email Address', 'Mobile Terminal', 'Service Tier Preference', 'ARR Estimate'] },
  { name: 'Stark_Logistics_Export.xlsx', headers: ['companyName', 'domain', 'industry vertical', 'SLA level', 'Annual Contract Value'] }
];

// ==========================================
// 2. Custom File Processor Component
// ==========================================
export function CustomFileProcessor({ onProcessingComplete }) {
  const [currentStep, setCurrentStep] = useState(1);
  const [targetModel, setTargetModel] = useState('Contact');
  const [selectedFile, setSelectedFile] = useState(null); // File object or mock file object
  const [parsedHeaders, setParsedHeaders] = useState([]);
  const [columnMappings, setColumnMappings] = useState({});
  const [isProcessing, setIsProcessing] = useState(false);

  // Auto-match Heuristic String Comparison Engine
  const executeAutoMatchHeuristics = useCallback((headers, model) => {
    const schema = POLYMORPHIC_TARGET_SCHEMAS[model];
    if (!schema) return;
    
    const allTargetFields = [...schema.requiredFields, ...schema.optionalFields];
    const initialMappings = {};

    headers.forEach(rawHeader => {
      const sanitizedRaw = rawHeader.toLowerCase().replace(/[^a-z0-9]/g, '');
      const exactOrFuzzyMatch = allTargetFields.find(targetField => {
        const sanitizedTarget = targetField.toLowerCase();
        return sanitizedRaw === sanitizedTarget || 
               sanitizedRaw.includes(sanitizedTarget) || 
               sanitizedTarget.includes(sanitizedRaw);
      });

      initialMappings[rawHeader] = exactOrFuzzyMatch || '';
    });

    setColumnMappings(initialMappings);
  }, []);

  // Synchronize heuristic auto-matches on state changes
  useEffect(() => {
    if (selectedFile && parsedHeaders.length > 0) {
      executeAutoMatchHeuristics(parsedHeaders, targetModel);
    }
  }, [targetModel, selectedFile, parsedHeaders, executeAutoMatchHeuristics]);

  // Handle Mock File selection
  const handleSimulateUpload = (fileIndex) => {
    const targetFile = MOCK_SAMPLE_FILES[fileIndex];
    // Create dummy File instance for payload compatibility
    const mockFileObj = new File(["dummy content"], targetFile.name, { type: "text/csv" });
    setSelectedFile(mockFileObj);
    setParsedHeaders(targetFile.headers);
    setCurrentStep(2);
    toast.success(`Parsed ${targetFile.headers.length} headers from ${targetFile.name}`);
  };

  // Handle Native Real File Input Selection
  const handleNativeFileUpload = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setSelectedFile(file);
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result || '';
      const firstLine = text.split('\n')[0];
      const headers = firstLine ? firstLine.split(',').map(h => h.trim().replace(/^["']|["']$/g, '')) : [];
      
      if (headers.length > 0) {
        setParsedHeaders(headers);
        setCurrentStep(2);
        toast.success(`Parsed ${headers.length} headers from ${file.name}`);
      } else {
        toast.error("Could not parse valid headers from the selected file.");
      }
    };
    reader.readAsText(file);
  };

  const handleMapChange = (rawHeader, targetField) => {
    setColumnMappings(prev => ({
      ...prev,
      [rawHeader]: targetField
    }));
  };

  // Validation Matrix Guardrail
  const validateRequiredFields = () => {
    const schema = POLYMORPHIC_TARGET_SCHEMAS[targetModel];
    if (!schema) return [];
    const mappedTargetFields = Object.values(columnMappings);
    return schema.requiredFields.filter(reqField => !mappedTargetFields.includes(reqField));
  };

  const missingRequiredFields = validateRequiredFields();
  const isMappingValid = missingRequiredFields.length === 0;

  // Dispatch Payload to Processing Pipeline
  const handleDispatchPipeline = async () => {
    setIsProcessing(true);
    try {
      if (onProcessingComplete) {
        await onProcessingComplete({
          file: selectedFile,
          headerMappings: {
            targetModel,
            mappings: columnMappings
          }
        });
      }
      
      // Reset wizard state
      setCurrentStep(1);
      setSelectedFile(null);
      setParsedHeaders([]);
      setColumnMappings({});
    } catch (err) {
      toast.error("Pipeline dispatch error: Ingest pipeline failed to process request.");
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8 py-4 text-white">
      {/* Header Block */}
      <div className="flex items-center justify-between border-b border-slate-900 pb-5">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-indigo-600/10 border border-indigo-500/20 rounded-xl flex items-center justify-center">
            <FileSpreadsheet className="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <h2 className="text-xl font-bold tracking-tight">Polymorphic Stream Ingestion Inbound Framework</h2>
            <p className="text-xs text-slate-400 mt-0.5">Normalize dynamic datasets into isolated database schemas.</p>
          </div>
        </div>
      </div>

      {/* Procedural Step Management */}
      <Sequence>
        {/* Step 1: Destination Scope and Inbound Object Extraction */}
        <Step title="Establish Target Ingestion Sub-Type & Stream Drop" subtitle="Step 1 of 3">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
            {/* Target Sub-Type Switcher */}
            <div className="space-y-3 bg-slate-950 p-4 rounded-xl border border-slate-900">
              <label className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                <Database className="w-3.5 h-3.5 text-indigo-400" /> Destination Object Target
              </label>
              <p className="text-[11px] text-slate-500 leading-relaxed">Choose the polymorphic database table model structure intended to receive these records.</p>
              <div className="space-y-2 pt-2">
                {Object.entries(POLYMORPHIC_TARGET_SCHEMAS).map(([key, schema]) => (
                  <button
                    key={key} type="button" onClick={() => setTargetModel(key)}
                    className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold border transition-all flex items-center justify-between ${
                      targetModel === key 
                        ? 'bg-indigo-600/10 border-indigo-500 text-indigo-300' 
                        : 'bg-slate-900/50 border-slate-800 text-slate-400 hover:border-slate-700'
                    }`}
                  >
                    <span>{schema.label}</span>
                    {targetModel === key && <Check className="w-3.5 h-3.5 text-indigo-400" />}
                  </button>
                ))}
              </div>
            </div>

            {/* File Drop & Simulation Container */}
            <div className="md:col-span-2 border-2 border-dashed border-slate-800 hover:border-slate-700 bg-slate-900/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center transition-colors group relative">
              <input 
                type="file" 
                accept=".csv,.xlsx,.xls" 
                onChange={handleNativeFileUpload} 
                className="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10"
              />
              <Upload className="w-10 h-10 text-slate-600 group-hover:text-indigo-400 transition-colors mb-3" />
              <div className="text-xs font-semibold text-slate-300">Drop Delimited Inbound File Buffer Matrix</div>
              <p className="text-[11px] text-slate-500 max-w-xs mx-auto mt-1">Click or drag UTF-8 delimited text files directly here.</p>
              
              <div className="mt-5 w-full max-w-md bg-slate-950 p-3 rounded-xl border border-slate-900 text-left space-y-2 z-20">
                <div className="text-[10px] font-bold tracking-wider text-slate-500 uppercase px-1">Or Simulate Sample File Registry Uploads:</div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {MOCK_SAMPLE_FILES.map((file, idx) => (
                    <button
                      key={file.name} type="button" onClick={() => handleSimulateUpload(idx)}
                      className="p-2 bg-slate-900 border border-slate-800 rounded-lg text-left text-[11px] font-mono text-slate-300 hover:border-indigo-500 transition-all truncate flex items-center justify-between group/btn"
                    >
                      <span className="truncate">{file.name}</span>
                      <ChevronRight className="w-3 h-3 text-slate-600 group-hover/btn:translate-x-0.5 transition-transform shrink-0" />
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </Step>

        {/* Step 2: Interactive Mapping Dynamic Grid */}
        <Step title="Align Extracted File Headers with System Schema Architecture" subtitle="Step 2 of 3">
          {currentStep < 2 ? (
            <div className="p-4 bg-slate-900/30 text-slate-500 font-mono text-xs italic rounded-xl border border-slate-900 mt-4">
              Awaiting inbound streaming array context resolution from Step 1...
            </div>
          ) : (
            <div className="space-y-4 mt-4 animate-fadeIn">
              <div className="flex flex-wrap items-center justify-between gap-4 bg-slate-900/60 border border-slate-800 p-4 rounded-xl">
                <div className="flex items-center gap-2">
                  <Layers className="w-4 h-4 text-indigo-400" />
                  <span className="text-xs font-semibold text-slate-300">
                    File Context Target: <span className="text-mono font-mono text-indigo-300 bg-indigo-950/40 px-2 py-0.5 rounded border border-indigo-900/50">{selectedFile?.name}</span>
                  </span>
                </div>
                
                {isMappingValid ? (
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 bg-emerald-950/20 border border-emerald-900/40 px-3 py-1 rounded-full">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Core Target Coordinates Locked
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-amber-400 bg-amber-950/20 border border-amber-900/40 px-3 py-1 rounded-full">
                    <AlertTriangle className="w-3.5 h-3.5" /> Missing Required Fields: {missingRequiredFields.join(', ')}
                  </div>
                )}
              </div>

              {/* Column Matching Interface Grid Layout Matrix */}
              <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-inner">
                <div className="grid grid-cols-12 gap-4 px-4 py-2.5 bg-slate-950 border-b border-slate-800 text-[10px] uppercase font-bold tracking-wider font-mono text-slate-500">
                  <div className="col-span-5">Raw Uploaded File Field Coordinates</div>
                  <div className="col-span-2 text-center">Translation Path</div>
                  <div className="col-span-5">Engine Polymorphic Target Schema Field</div>
                </div>

                <div className="divide-y divide-slate-800/60 max-h-[320px] overflow-y-auto">
                  {parsedHeaders.map((rawHeader) => {
                    const activeMatch = columnMappings[rawHeader];
                    const schemaOptions = POLYMORPHIC_TARGET_SCHEMAS[targetModel];

                    return (
                      <div key={rawHeader} className="grid grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-slate-950/40 transition-colors">
                        <div className="col-span-5 flex items-center gap-2 min-w-0">
                          <FileSpreadsheet className="w-3.5 h-3.5 text-slate-600 shrink-0" />
                          <span className="font-mono text-xs text-slate-300 truncate" title={rawHeader}>{rawHeader}</span>
                        </div>

                        <div className="col-span-2 flex justify-center">
                          <div className={`p-1 rounded-lg border ${
                            activeMatch ? 'bg-indigo-950/30 border-indigo-900/50 text-indigo-400' : 'bg-slate-950 border-slate-800 text-slate-600'
                          }`}>
                            <ArrowRight className="w-3.5 h-3.5" />
                          </div>
                        </div>

                        <div className="col-span-5">
                          <select
                            value={activeMatch || ''}
                            onChange={(e) => handleMapChange(rawHeader, e.target.value)}
                            className={`w-full bg-slate-950 text-xs font-mono border rounded-lg px-2.5 py-1.5 focus:outline-none cursor-pointer transition-colors ${
                              activeMatch 
                                ? schemaOptions.requiredFields.includes(activeMatch)
                                  ? 'border-emerald-900 text-emerald-400 focus:border-emerald-500 bg-emerald-950/10'
                                  : 'border-slate-800 text-slate-300 focus:border-indigo-500'
                                : 'border-amber-900/60 text-amber-500 focus:border-amber-500 bg-amber-950/5'
                            }`}
                          >
                            <option value="">-- Discard Column Mapping --</option>
                            <optgroup label="Required Database Directives" className="font-sans text-slate-400 bg-slate-950">
                              {schemaOptions.requiredFields.map(f => (
                                <option key={f} value={f} className="font-mono text-white">
                                  {f} *
                                </option>
                              ))}
                            </optgroup>
                            <optgroup label="Optional/Dynamic Custom Meta Attributes" className="font-sans text-slate-400 bg-slate-950">
                              {schemaOptions.optionalFields.map(f => (
                                <option key={f} value={f} className="font-mono text-white">
                                  {f}
                                </option>
                              ))}
                            </optgroup>
                          </select>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="flex justify-end pt-2">
                <button
                  type="button" disabled={!isMappingValid} onClick={() => setCurrentStep(3)}
                  className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-900 disabled:text-slate-600 border border-transparent disabled:border-slate-800 text-xs font-semibold px-4 py-2 rounded-xl transition-all flex items-center gap-1.5 shadow-lg shadow-indigo-600/10"
                >
                  Confirm Transformations Mapping <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </Step>

        {/* Step 3: Stream Verification Framework Dispatch */}
        <Step title="Execute Async Processing Pipeline Compilation" subtitle="Step 3 of 3">
          {currentStep < 3 ? (
            <div className="p-4 bg-slate-900/30 text-slate-500 font-mono text-xs italic rounded-xl border border-slate-900 mt-4">
              Awaiting upstream schema validation compliance confirmation matrices...
            </div>
          ) : (
            <div className="bg-slate-900/40 border border-slate-800/80 p-6 rounded-2xl space-y-4 mt-4 animate-fadeIn">
              <div className="flex gap-4">
                <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center shrink-0">
                  <HelpCircle className="w-6 h-6 text-emerald-400" />
                </div>
                <div className="space-y-1">
                  <h4 className="text-sm font-bold text-slate-200">Ingestion Schema Validation Structural Review Succeeded</h4>
                  <p className="text-xs text-slate-400 leading-relaxed max-w-2xl">
                    All required database integrity anchors matching the polymorphic <span className="font-mono text-indigo-300">{targetModel}</span> core layout have been resolved from the spreadsheet layout parameters. Dispatched jobs pass directly to async tenant data layers.
                  </p>
                </div>
              </div>

              <div className="border-t border-slate-800 pt-4 flex items-center justify-between flex-wrap gap-4">
                <button
                  type="button" onClick={() => setCurrentStep(2)} disabled={isProcessing}
                  className="text-xs text-slate-400 hover:text-slate-200 transition-colors font-semibold"
                >
                  Modify Translation Vector Properties
                </button>
                
                <button
                  type="button" onClick={handleDispatchPipeline} disabled={isProcessing}
                  className="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-semibold px-5 py-2.5 rounded-xl shadow-lg shadow-emerald-600/10 transition-colors flex items-center gap-2"
                >
                  {isProcessing ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin" /> Inbound Pipeline Allocating...
                    </>
                  ) : (
                    <>
                      <Play className="w-3.5 h-3.5 fill-current" /> Fire Async Worker Threads
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </Step>
      </Sequence>
    </div>
  );
}
export default CustomFileProcessor;