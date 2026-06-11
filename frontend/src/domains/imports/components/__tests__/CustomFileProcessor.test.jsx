import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import CustomFileProcessor from '../CustomFileProcessor';

// Mocking react-hot-toast out of execution path loops
jest.mock('react-hot-toast', () => ({
  toast: {
    error: jest.fn(),
    success: jest.fn()
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
    expect(screen.getByText(/Schema Ingestion Quality Gate/i)).toBeInTheDocument();
    expect(screen.getByText(/Click to import or drop target files here/i)).toBeInTheDocument();
  });

  test('should parse CSV headers locally and transition state views into alignment mapping tables', async () => {
    const mockCallback = jest.fn();
    render(<CustomFileProcessor onProcessingComplete={mockCallback} />);

    const file = createMockFileBlob("name,email,phoneNumber\nJohn Doe,john@test.com,+15550000", "contacts.csv");

    // Polyfill simple FileReader mock behaviors for target node testing execution context environment
    const originalFileReader = global.FileReader;
    class MockFileReader {
      readAsText(blob) {
        this.result = "name,email,phoneNumber\nJohn Doe,john@test.com,+15550000";
        if (this.onload) {
          this.onload({ target: { result: this.result } });
        }
      }
    }
    global.FileReader = MockFileReader;

    const input = screen.getByTagName('input');
    fireEvent.change(input, { target: { files: [file] } });

    await waitFor(() => {
      expect(screen.getByText(/Align Resource Data Layout Positions/i)).toBeInTheDocument();
    });

    // Clean up file reader context mutations
    global.FileReader = originalFileReader;
  });

  test('should block process advancement when mandatory required layout links are dropped', async () => {
    render(<CustomFileProcessor onProcessingComplete={jest.fn()} />);
    
    // Simulating immediate transition into active mapping layout views
    const file = createMockFileBlob("mismatchedHeaderOne,mismatchedHeaderTwo\nval1,val2", "mismatched.csv");
    
    const originalFileReader = global.FileReader;
    class MockFileReader {
      readAsText(blob) {
        this.result = "mismatchedHeaderOne,mismatchedHeaderTwo\nval1,val2";
        this.onload({ target: { result: this.result } });
      }
    }
    global.FileReader = MockFileReader;

    fireEvent.change(screen.getByTagName('input'), { target: { files: [file] } });

    await waitFor(() => {
      expect(screen.getByText(/Advance to Pipeline Processing Sandbox Preview/i)).toBeInTheDocument();
    });

    // Fire preview generation request button while allocations are missing configuration alignments
    const advanceButton = screen.getByText(/Advance to Pipeline Processing Sandbox Preview/i);
    fireEvent.click(advanceButton);

    // Assert that the interface prevents advancing due to the missing required fields
    expect(screen.queryByText(/Client-Side Structural Parsing Dry Run/i)).not.toBeInTheDocument();

    global.FileReader = originalFileReader;
  });
});