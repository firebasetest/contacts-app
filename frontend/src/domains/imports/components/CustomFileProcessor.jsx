import React, { useState, useEffect } from 'react';
import { FileSpreadsheet, ArrowRight, CheckCircle, AlertTriangle, Cpu, FileJson, Sheet } from 'lucide-react';
import { toast } from 'react-hot-toast';

// Target fields available for alignment inside the metadata engine schema
const TARGET_SCHEMA_FIELDS = [
  { key: 'name', label: 'Full Name', required: true, type: 'STRING' },
  { key: 'email', label: 'Email Address', required: true, type: 'EMAIL' },
  { key: 'phoneNumber', label: 'Phone Number', required: false, type: 'STRING' },
  { key: 'notes', label: 'Internal Notes', required: false, type: 'STRING' }
];

export default function CustomFileProcessor({ onProcessingComplete }) {
  const [file, setFile] = useState(null);
  const [rawHeaders, setRawHeaders] = useState([]);
  const [previewRows, setPreviewRows] = useState([]);
  const [mappings, setMappings] = useState({});
  const [processingStep, setProcessingStep] = useState('upload'); // upload -> map -> preview

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (!selectedFile) return;

    setFile(selectedFile);
    const reader = new FileReader();

    reader.onload = (event) => {
      const text = event.target.result;
      if (selectedFile.name.endsWith('.json')) {
        parseJsonData(text);
      } else {
        parseCsvData(text);
      }
    };

    reader.readAsText(selectedFile);
  };

  const parseCsvData = (text) => {
    const lines = text.split(/\r?\n/).filter(line => line.trim() !== '');
    if (lines.length === 0) {
      toast.error('The selected CSV payload contains no structured data lines.');
      return;
    }

    // Naive raw string splitter matching common spreadsheet delimiters
    const headers = lines[0].split(',').map(h => h.replace(/["']/g, '').trim());
    const rows = lines.slice(1, 6).map(line => line.split(',').map(cell => cell.replace(/["']/g, '').trim()));

    setRawHeaders(headers);
    setPreviewRows(rows);
    
    // Auto-initialize fallback layout mappings based on string alignment heuristics
    const initialMappings = {};
    TARGET_SCHEMA_FIELDS.forEach(target => {
      const match = headers.find(h => h.toLowerCase() === target.key.toLowerCase() || h.toLowerCase().includes(target.key.toLowerCase()));
      if (match) initialMappings[target.key] = match;
    });
    setMappings(initialMappings);
    setProcessingStep('map');
  };

  const parseJsonData = (text) => {
    try {
      const parsed = JSON.parse(text);
      const dataArray = Array.isArray(parsed) ? parsed : [parsed];
      if (dataArray.length === 0) {
        toast.error('Empty JSON payload detected.');
        return;
      }

      const headers = Object.keys(dataArray[0]);
      const rows = dataArray.slice(0, 5).map(obj => headers.map(h => String(obj[h] ?? '')));

      setRawHeaders(headers);
      setPreviewRows(rows);

      const initialMappings = {};
      TARGET_SCHEMA_FIELDS.forEach(target => {
        if (headers.includes(target.key)) initialMappings[target.key] = target.key;
      });
      setMappings(initialMappings);
      setProcessingStep('map');
    } catch (err) {
      toast.error('Malformed JSON array syntax detected during file execution parsing.');
    }
  };

  const handleMappingChange = (targetKey, sourceHeader) => {
    setMappings(prev => ({ ...prev, [targetKey]: sourceHeader }));
  };

  const validateMappings = () => {
    // Verify mandatory target field requirements
    const missingFields = TARGET_SCHEMA_FIELDS.filter(f => f.required && !mappings[f.key]);
    if (missingFields.length > 0) {
      toast.error(`Mapping abort: Missing mandatory structural fields: ${missingFields.map(f => f.label).join(', ')}`);
      return;
    }
    setProcessingStep('preview');
  };

  const getMappedValue = (row, targetKey) => {
    const sourceHeader = mappings[targetKey];
    if (!sourceHeader) return '';
    const headerIndex = rawHeaders.indexOf(sourceHeader);
    return row[headerIndex] || '';
  };

  const validateRowCell = (targetKey, value) => {
    if (!value) {
      const field = TARGET_SCHEMA_FIELDS.find(f => f.key === targetKey);
      return field?.required ? 'MISSING' : 'VALID';
    }
    if (targetKey === 'email' && !/\S+@\S+\.\S+/.test(value)) {
      return 'INVALID_FORMAT';
    }
    return 'VALID';
  };

  const submitToIngestionEngine = () => {
    toast.success('Local alignment verified. Streaming schema maps to background tasks...');
    if (onProcessingComplete) {
      onProcessingComplete({
        file,
        headerMappings: mappings
      });
    }
    // Return interface context to initial state
    setProcessingStep('upload');
    setFile(null);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl text-slate-100 transition-all">
      <div className="flex items-center gap-3 border-b border-slate-800 pb-4 mb-6">
        <Cpu className="w-5 h-5 text-indigo-400 animate-pulse" />
        <div>
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-200">Schema Ingestion Quality Gate</h2>
          <p className="text-[11px] text-slate-400">Validate file structures and map variable fields client-side prior to background pool processing.</p>
        </div>
      </div>

      {/* STEP 1: Upload / Drop Zone */}
      {processingStep === 'upload' && (
        <div className="border-2 border-dashed border-slate-700 hover:border-indigo-500/50 rounded-xl p-8 text-center transition-all relative bg-slate-950/40">
          <input
            type="file"
            accept=".csv,.json"
            onChange={handleFileChange}
            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
          />
          <FileSpreadsheet className="w-12 h-12 text-slate-500 mx-auto mb-3" />
          <p className="text-xs font-semibold text-slate-300">Click to import or drop target files here</p>
          <p className="text-[10px] text-slate-500 mt-1">Supports strict structural UTF-8 CSV matrix streams or array-based JSON formats</p>
        </div>
      )}

      {/* STEP 2: Schema Mapper Grid Matrix */}
      {processingStep === 'map' && (
        <div className="space-y-6">
          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800/60 flex items-center justify-between text-xs font-mono">
            <span className="text-slate-400 flex items-center gap-1.5">
              {file?.name.endsWith('.json') ? <FileJson className="w-4 h-4 text-amber-400" /> : <Sheet className="w-4 h-4 text-emerald-400" />}
              Loaded Source: <span className="text-white font-bold">{file?.name}</span>
            </span>
            <button 
              onClick={() => setProcessingStep('upload')}
              className="text-slate-500 hover:text-red-400 transition-colors text-[10px] uppercase font-bold"
            >
              Abort Ingestion
            </button>
          </div>

          <div className="space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wide">Align Resource Data Layout Positions</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {TARGET_SCHEMA_FIELDS.map((target) => (
                <div key={target.key} className="bg-slate-950/60 border border-slate-800 rounded-xl p-3 flex flex-col justify-between gap-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold flex items-center gap-1.5">
                      {target.label}
                      {target.required && <span className="text-[9px] bg-red-500/10 text-red-400 px-1.5 py-0.2 rounded font-mono">* Required</span>}
                    </span>
                    <span className="text-[10px] font-mono text-slate-500">{target.type}</span>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <ArrowRight className="w-3.5 h-3.5 text-slate-600 shrink-0" />
                    <select
                      value={mappings[target.key] || ''}
                      onChange={(e) => handleMappingChange(target.key, e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg text-xs font-mono p-1.5 text-white focus:outline-none focus:border-indigo-500"
                    >
                      <option value="">-- Drop to Ignore Field Element --</option>
                      {rawHeaders.map((head, idx) => (
                        <option key={idx} value={head}>{head}</option>
                      ))}
                    </select>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={validateMappings}
            className="w-full bg-indigo-600 hover:bg-indigo-500 text-xs font-bold text-white py-2.5 rounded-xl shadow transition-all active:scale-[0.99]"
          >
            Advance to Pipeline Processing Sandbox Preview
          </button>
        </div>
      )}

      {/* STEP 3: Client Validation Inspection Matrix Preview */}
      {processingStep === 'preview' && (
        <div className="space-y-5">
          <div className="flex justify-between items-center">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wide">Client-Side Structural Parsing Dry Run</h3>
            <button 
              onClick={() => setProcessingStep('map')}
              className="text-xs text-indigo-400 hover:text-indigo-300 transition-colors font-semibold"
            >
              Modify Header Targets
            </button>
          </div>

          <div className="overflow-x-auto border border-slate-800 rounded-xl bg-slate-950">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-900 border-b border-slate-800 text-slate-400 font-mono text-[11px]">
                  {TARGET_SCHEMA_FIELDS.map(f => (
                    <th key={f.key} className="p-3 font-semibold">{f.label}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-[11px]">
                {previewRows.map((row, rIdx) => (
                  <tr key={rIdx} className="hover:bg-slate-900/30 transition-colors">
                    {TARGET_SCHEMA_FIELDS.map(f => {
                      const val = getMappedValue(row, f.key);
                      const status = validateRowCell(f.key, val);
                      
                      return (
                        <td key={f.key} className="p-3 max-w-[200px] truncate">
                          {status === 'VALID' && (
                            <span className="text-slate-200">{val || <span className="text-slate-600 italic">null</span>}</span>
                          )}
                          {status === 'MISSING' && (
                            <span className="text-red-400 bg-red-950/30 px-1.5 py-0.5 rounded border border-red-900/40 flex items-center gap-1 w-fit text-[10px]">
                              <AlertTriangle className="w-3 h-3" /> Empty Required Field
                            </span>
                          )}
                          {status === 'INVALID_FORMAT' && (
                            <span className="text-amber-400 bg-amber-950/30 px-1.5 py-0.5 rounded border border-amber-900/40 flex items-center gap-1 w-fit text-[10px]" title={val}>
                              <AlertTriangle className="w-3 h-3" /> Invalid Email Regex
                            </span>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex gap-3">
            <button
              onClick={() => setProcessingStep('map')}
              className="w-1/3 bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 py-2.5 rounded-xl transition-all"
            >
              Back to Layout
            </button>
            <button
              onClick={submitToIngestionEngine}
              className="w-2/3 bg-emerald-600 hover:bg-emerald-500 text-xs font-bold text-white py-2.5 rounded-xl shadow transition-all active:scale-[0.99] flex items-center justify-center gap-2"
            >
              <CheckCircle className="w-4 h-4" /> Commit Batch to Worker Pools
            </button>
          </div>
        </div>
      )}
    </div>
  );
}