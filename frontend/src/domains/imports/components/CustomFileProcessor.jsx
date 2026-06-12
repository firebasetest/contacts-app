import React, { useState, useEffect } from 'react';
import { 
  Upload, FileSpreadsheet, ArrowRight, CheckCircle2, AlertTriangle, 
  RefreshCw, Layers, Database, ChevronRight, HelpCircle, Check, Play 
} from 'lucide-react';
import toast from 'react-hot-toast';
import { Sequence, Step } from '../../../components/Sequence'; // Ensure path alignment with your project structure

// 1. Polymorphic Target Schemas Registry Definition
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

// Mock raw incoming spreadsheet configurations for simulation
const MOCK_SAMPLE_FILES = [
  { name: 'Q2_Inbound_Leads_EU.csv', headers: ['Full Name', 'Email Address', 'Mobile Terminal', 'Service Tier Preference', 'ARR Estimate'] },
  { name: 'Stark_Logistics_Export.xlsx', headers: ['companyName', 'domain', 'industry vertical', 'SLA level', 'Annual Contract Value'] }
];

export default function CustomFileProcessor({ onJobCreated }) {
  // Processing Pipeline States
  const [currentStep, setCurrentStep] = useState(1);
  const [targetModel, setTargetModel] = useState('Contact');
  const [selectedFile, setSelectedFile] = useState(null);
  const [parsedHeaders, setParsedHeaders] = useState([]);
  const [columnMappings, setColumnMappings] = useState({});
  const [isProcessing, setIsProcessing] = useState(false);

  // 2. Automatch Heuristic String Comparison Engine
  const executeAutoMatchHeuristics = (headers, model) => {
    const schema = POLYMORPHIC_TARGET_SCHEMAS[model];
    const allTargetFields = [...schema.requiredFields, ...schema.optionalFields];
    const initialMappings = {};

    headers.forEach(rawHeader => {
      // Clean and sanitize string vectors for loose token comparison
      const sanitizedRaw = rawHeader.toLowerCase().replace(/[^a-z0-9]/g, '');
      
      const exactOrFuzzyMatch = allTargetFields.find(targetField => {
        const sanitizedTarget = targetField.toLowerCase();
        return sanitizedRaw === sanitizedTarget || 
               sanitizedRaw.includes(sanitizedTarget) || 
               sanitizedTarget.includes(sanitizedRaw);
      });

      if (exactOrFuzzyMatch) {
        initialMappings[rawHeader] = exactOrFuzzyMatch;
      } else {
        initialMappings[rawHeader] = ''; // Leave unassigned for explicit user interaction
      }
    });

    setColumnMappings(initialMappings);
  };

  // Synchronize matches when the file context or destination target flips
  useEffect(() => {
    if (selectedFile) {
      executeAutoMatchHeuristics(parsedHeaders, targetModel);
    }
  }, [targetModel, selectedFile, parsedHeaders]);

  // Handle Mock file drop simulator injection
  const handleSimulateUpload = (fileIndex) => {
    const targetFile = MOCK_SAMPLE_FILES[fileIndex];
    setSelectedFile(targetFile.name);
    setParsedHeaders(targetFile.headers);
    setCurrentStep(2);
    toast.success(`Parsed ${targetFile.headers.length} headers from ${targetFile.name}`);
  };

  const handleMapChange = (rawHeader, targetField) => {
    setColumnMappings(prev => ({
      ...prev,
      [rawHeader]: targetField
    }));
  };

  // 3. Validation Matrix Guardrail
  const validateRequiredFields = () => {
    const schema = POLYMORPHIC_TARGET_SCHEMAS[targetModel];
    const mappedTargetFields = Object.values(columnMappings);
    return schema.requiredFields.filter(reqField => !mappedTargetFields.includes(reqField));
  };

  const missingRequiredFields = validateRequiredFields();
  const isMappingValid = missingRequiredFields.length === 0;

  // 4. Dispatch Payload to Async Processing Worker Pool Pipeline
  const handleDispatchPipeline = async () => {
    setIsProcessing(true);
    try {
      // In production contexts, construct a form mapping payload out to your ingestion controller:
      // const payload = { targetModel, columnMappings, filename: selectedFile };
      // await axiosClient.post('/imports/jobs', payload);
      
      await new Promise(resolve => setTimeout(resolve, 1500));
      toast.success("Structural map accepted. Ingestion transaction dispatched to background workers.");
      
      if (onJobCreated) onJobCreated();
      
      // Reset State Machine
      setCurrentStep(1);
      setSelectedFile(null);
      setColumnMappings({});
    } catch (err) {
      toast.error("Pipeline failure: Could not allocate memory context to target worker threads.");
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8 py-4 text-white">
      
      {/* Component Module Identification Header Block */}
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

      {/* Procedural Step Management Execution Container */}
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

            {/* Simulated File Injection Zone Container */}
            <div className="md:col-span-2 border-2 border-dashed border-slate-800 hover:border-slate-700 bg-slate-900/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center transition-colors group">
              <Upload className="w-10 h-10 text-slate-600 group-hover:text-indigo-400 transition-colors mb-3" />
              <div className="text-xs font-semibold text-slate-300">Drop Delimited Inbound File Buffer Matrix</div>
              <p className="text-[11px] text-slate-500 max-w-xs mx-auto mt-1">Accepts variable columns across UTF-8 text envelopes.</p>
              
              <div className="mt-5 w-full max-w-md bg-slate-950 p-3 rounded-xl border border-slate-900 text-left space-y-2">
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

        {/* Step 2: Interactive Mapping Dynamic Grid Component */}
        <Step title="Align Extracted File Headers with System Schema Architecture" subtitle="Step 2 of 3">
          {currentStep < 2 ? (
            <div className="p-4 bg-slate-900/30 text-slate-500 font-mono text-xs italic rounded-xl border border-slate-900 mt-4">
              Awaiting inbound streaming array context resolution from Step 1...
            </div>
          ) : (
            <div className="space-y-4 mt-4 animate-fadeIn">
              
              {/* Informational Diagnostic Mapping Info Bar */}
              <div className="flex flex-wrap items-center justify-between gap-4 bg-slate-900/60 border border-slate-800 p-4 rounded-xl">
                <div className="flex items-center gap-2">
                  <Layers className="w-4 h-4 text-indigo-400" />
                  <span className="text-xs font-semibold text-slate-300">
                    File Context Target: <span className="text-mono font-mono text-indigo-300 bg-indigo-950/40 px-2 py-0.5 rounded border border-indigo-900/50">{selectedFile}</span>
                  </span>
                </div>
                
                {isMappingValid ? (
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 bg-emerald-950/20 border border-emerald-900/40 px-3 py-1 rounded-full">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Core Target Coordinates Locked
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-amber-400 bg-amber-950/20 border border-amber-900/40 px-3 py-1 rounded-full">
                    <AlertTriangle className="w-3.5 h-3.5" /> Missing Schema Mapping Injections: {missingRequiredFields.join(', ')}
                  </div>
                )}
              </div>

              {/* High-Fidelity Column Matching Interface Grid Layout Matrix */}
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
                        
                        {/* Left Side: Uploaded File Coordinates */}
                        <div className="col-span-5 flex items-center gap-2 min-w-0">
                          <FileSpreadsheet className="w-3.5 h-3.5 text-slate-600 shrink-0" />
                          <span className="font-mono text-xs text-slate-300 truncate" title={rawHeader}>{rawHeader}</span>
                        </div>

                        {/* Mid-Point Interceptor Transition Indicator Arrow */}
                        <div className="col-span-2 flex justify-center">
                          <div className={`p-1 rounded-lg border ${
                            activeMatch ? 'bg-indigo-950/30 border-indigo-900/50 text-indigo-400' : 'bg-slate-950 border-slate-800 text-slate-600'
                          }`}>
                            <ArrowRight className="w-3.5 h-3.5" />
                          </div>
                        </div>

                        {/* Right Side: Mapping Registry Destination Configuration Dropdown Selector */}
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

              {/* Progression Controls */}
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