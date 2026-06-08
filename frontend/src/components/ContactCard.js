import React from 'react';

const ContactCard = ({ contact, onEdit, onDelete }) => {
  return (
    <div className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition">
      <h3 className="text-lg font-bold text-gray-800 mb-2">{contact.name}</h3>
      <div className="text-gray-600 text-sm space-y-1 mb-4">
        <p>
          <span className="font-semibold">Email:</span>{' '}
          <a href={`mailto:${contact.email}`} className="text-blue-600 hover:underline">
            {contact.email}
          </a>
        </p>
        <p>
          <span className="font-semibold">Phone:</span>{' '}
          <a href={`tel:${contact.phone}`} className="text-blue-600 hover:underline">
            {contact.phone}
          </a>
        </p>
        {contact.notes && (
          <p>
            <span className="font-semibold">Notes:</span> {contact.notes}
          </p>
        )}
      </div>
      <div className="flex gap-2">
        <button
          onClick={() => onEdit(contact)}
          className="flex-1 bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-3 rounded transition text-sm"
        >
          ✏️ Edit
        </button>
        <button
          onClick={() => onDelete(contact.id)}
          className="flex-1 bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-3 rounded transition text-sm"
        >
          🗑️ Delete
        </button>
      </div>
    </div>
  );
};

export default ContactCard;
