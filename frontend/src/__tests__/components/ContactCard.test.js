import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import ContactCard from '../../components/ContactCard';

describe('ContactCard Component', () => {
  const mockContact = {
    id: 1,
    name: 'John Doe',
    email: 'john@example.com',
    phone: '1234567890',
    notes: 'Test notes',
  };

  const mockOnEdit = jest.fn();
  const mockOnDelete = jest.fn();

  beforeEach(() => {
    mockOnEdit.mockClear();
    mockOnDelete.mockClear();
  });

  test('renders contact information', () => {
    render(
      <ContactCard
        contact={mockContact}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
      />
    );

    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('1234567890')).toBeInTheDocument();
  });

  test('calls onEdit when edit button is clicked', () => {
    render(
      <ContactCard
        contact={mockContact}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
      />
    );

    const editButton = screen.getByText('✏️ Edit');
    fireEvent.click(editButton);

    expect(mockOnEdit).toHaveBeenCalledWith(mockContact);
  });

  test('calls onDelete when delete button is clicked', () => {
    render(
      <ContactCard
        contact={mockContact}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
      />
    );

    const deleteButton = screen.getByText('🗑️ Delete');
    fireEvent.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith(mockContact.id);
  });
});
