import React, { useState } from 'react';
import { contactAPI } from '../services/api';
import toast from 'react-hot-toast';

const ExportButton = ({ searchCriteria }) => {
  const [isExporting, setIsExporting] = useState(false);

  const handleExportCSV = async () => {
    setIsExporting(true);
    try {
      const response = await contactAPI.exportCSV(searchCriteria);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'contacts.csv');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      toast.success('Contacts exported to CSV!');
    } catch (error) {
      toast.error('Failed to export to CSV');
    } finally {
      setIsExporting(false);
    }
  };

  const handleExportPDF = async () => {
    setIsExporting(true);
    try {
      const response = await contactAPI.exportPDF(searchCriteria);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'contacts.pdf');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      toast.success('Contacts exported to PDF!');
    } catch (error) {
      toast.error('Failed to export to PDF');
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="flex gap-2">
      <button
        onClick={handleExportCSV}
        disabled={isExporting}
        className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
      >
        📊 Export CSV
      </button>
      <button
        onClick={handleExportPDF}
        disabled={isExporting}
        className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
      >
        📄 Export PDF
      </button>
    </div>
  );
};

export default ExportButton;
