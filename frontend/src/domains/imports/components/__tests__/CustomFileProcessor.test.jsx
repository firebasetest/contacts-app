import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import CustomFileProcessor from '../CustomFileProcessor';

// Mocking react-hot-toast default export used by the component
jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: {
    success: jest.fn(),
    error: jest.fn()
  }
}));

describe('CustomFileProcessor UI Logic Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Helper sequence layout simulation to mimic web file reader API executions
  const createMockFileBlob = (content, name, type = 'text/csv') => {
    const blob = new Blob([content], { type });
    blob.name = name;
    return blob;
  };

  test('should render file drag-and-drop landing lane initially', () => {
    render(<CustomFileProcessor onProcessingComplete={jest.fn()} />);
    // Updated expectations to match current component wording
    expect(screen.getByText(/Polymorphic Stream Ingestion Inbound Framework/i)).toBeInTheDocument();
    expect(screen.getByText(/Drop Delimited Inbound File Buffer Matrix/i)).toBeInTheDocument();
  });

  test('should parse sample file via simulate upload and show mapping UI', async () => {
    const mockCallback = jest.fn();
    render(<CustomFileProcessor onProcessingComplete={mockCallback} />);

    // Click the first simulated sample file button provided by the component
    const sampleButton = screen.getByText(/Q2_Inbound_Leads_EU.csv/i);
    fireEvent.click(sampleButton);

    // Expect the mapping UI and file context to appear
    await waitFor(() => {
      expect(screen.getByText(/File Context Target:/i)).toBeInTheDocument();
    });

    // There should be select elements populated for parsed headers
    expect(document.querySelectorAll('select').length).toBeGreaterThan(0);
  });

  test('should prevent advancing when required mappings are missing', async () => {
    render(<CustomFileProcessor onProcessingComplete={jest.fn()} />);

    // Click the second simulated sample file which maps to a different schema (should leave required Contact fields missing)
    const sampleButton = screen.getByText(/Stark_Logistics_Export.xlsx/i);
    fireEvent.click(sampleButton);

    // The Confirm Transformations Mapping button should be present but disabled until required mappings are set
    await waitFor(() => {
      const confirmBtn = screen.getByText(/Confirm Transformations Mapping/i);
      expect(confirmBtn).toBeInTheDocument();
      expect(confirmBtn).toBeDisabled();
    });
  });
});