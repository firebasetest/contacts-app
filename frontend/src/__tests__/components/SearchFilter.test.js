import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import SearchFilter from '../../components/SearchFilter';

describe('SearchFilter Component', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    mockOnSearch.mockClear();
  });

  test('renders search filter inputs', () => {
    render(<SearchFilter onSearch={mockOnSearch} isLoading={false} />);
    
    expect(screen.getByPlaceholderText(/Search by name/i)).toBeInTheDocument();
    expect(screen.getByDisplayValue('createdAt')).toBeInTheDocument();
  });

  test('calls onSearch when search button is clicked', () => {
    render(<SearchFilter onSearch={mockOnSearch} isLoading={false} />);
    
    const searchButton = screen.getByText('🔍 Search');
    fireEvent.click(searchButton);
    
    expect(mockOnSearch).toHaveBeenCalled();
  });

  test('resets form when reset button is clicked', () => {
    render(<SearchFilter onSearch={mockOnSearch} isLoading={false} />);
    
    const searchInput = screen.getByPlaceholderText(/Search by name/i);
    fireEvent.change(searchInput, { target: { value: 'test' } });
    
    const resetButton = screen.getByText('↺ Reset');
    fireEvent.click(resetButton);
    
    expect(searchInput.value).toBe('');
  });
});
