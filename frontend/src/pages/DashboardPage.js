import React, { useState, useEffect } from 'react';
import { contactAPI } from '../services/api';
import ContactForm from '../components/ContactForm';
import ContactList from '../components/ContactList';
const SearchFilter = React.lazy(() => import('../components/SearchFilter'));
const ExportButton = React.lazy(() => import('../components/ExportButton'));
import Header from '../components/Header';
import toast from 'react-hot-toast';

const DashboardPage = () => {
  const [contacts, setContacts] = useState([]);
  const [editingContact, setEditingContact] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isFormLoading, setIsFormLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [searchCriteria, setSearchCriteria] = useState({
    keyword: '',
    sortBy: 'createdAt',
    sortOrder: 'desc',
    page: 0,
    size: 10,
  });
  const [pageData, setPageData] = useState(null);
  const [useSearch, setUseSearch] = useState(false);

  useEffect(() => {
    if (useSearch) {
      searchContacts();
    } else {
      fetchContacts();
    }
  }, [useSearch]);

  const fetchContacts = async () => {
    setIsLoading(true);
    try {
      const response = await contactAPI.getAll();
      setContacts(response.data);
    } catch (error) {
      toast.error('Failed to fetch contacts');
    } finally {
      setIsLoading(false);
    }
  };

  const searchContacts = async () => {
    setIsLoading(true);
    try {
      const response = await contactAPI.search(searchCriteria);
      setPageData(response.data);
      setContacts(response.data.content);
    } catch (error) {
      toast.error('Failed to search contacts');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (criteria) => {
    setSearchCriteria(criteria);
    setUseSearch(true);
  };

  const handleAddContact = async (formData) => {
    setIsFormLoading(true);
    try {
      const response = await contactAPI.create(formData);
      setContacts([response.data, ...contacts]);
      toast.success('Contact added successfully!');
      setShowForm(false);
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to add contact';
      toast.error(message);
    } finally {
      setIsFormLoading(false);
    }
  };

  const handleUpdateContact = async (formData) => {
    setIsFormLoading(true);
    try {
      const response = await contactAPI.update(editingContact.id, formData);
      setContacts(
        contacts.map((c) => (c.id === editingContact.id ? response.data : c))
      );
      toast.success('Contact updated successfully!');
      setEditingContact(null);
      setShowForm(false);
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update contact';
      toast.error(message);
    } finally {
      setIsFormLoading(false);
    }
  };

  const handleDeleteContact = async (id) => {
    if (window.confirm('Are you sure you want to delete this contact?')) {
      try {
        await contactAPI.delete(id);
        setContacts(contacts.filter((c) => c.id !== id));
        toast.success('Contact deleted successfully!');
      } catch (error) {
        toast.error('Failed to delete contact');
      }
    }
  };

  const handleEditContact = (contact) => {
    setEditingContact(contact);
    setShowForm(true);
  };

  const handleCancelEdit = () => {
    setEditingContact(null);
    setShowForm(false);
  };

  const handlePageChange = (newPage) => {
    setSearchCriteria({ ...searchCriteria, page: newPage });
    setUseSearch(true);
  };

  return (
    <>
      <Header />
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <div className="mb-8 flex justify-between items-center">
            <h1 className="text-4xl font-bold text-gray-800">My Contacts</h1>
            {!showForm && (
              <button
                onClick={() => {
                  setShowForm(true);
                  setEditingContact(null);
                }}
                className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                ✚ Add New Contact
              </button>
            )}
          </div>

          {!showForm && (
            <React.Suspense fallback={<div>Loading...</div>}>
              <SearchFilter onSearch={handleSearch} isLoading={isLoading} />
            </React.Suspense>
          )}

          {!showForm && (
            <div className="mb-6">
              <React.Suspense fallback={<div>Loading...</div>}>
                <ExportButton searchCriteria={searchCriteria} />
              </React.Suspense>
            </div>
          )}

          {showForm && (
            <div className="mb-8">
              <ContactForm
                onSubmit={editingContact ? handleUpdateContact : handleAddContact}
                initialData={editingContact}
                isLoading={isFormLoading}
              />
              <button
                onClick={handleCancelEdit}
                className="mt-4 w-full bg-gray-400 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                Cancel
              </button>
            </div>
          )}

          <ContactList
            contacts={contacts}
            onEdit={handleEditContact}
            onDelete={handleDeleteContact}
            isLoading={isLoading}
          />

          {pageData && !showForm && (
            <div className="mt-8 flex justify-center items-center gap-2">
              <button
                onClick={() => handlePageChange(searchCriteria.page - 1)}
                disabled={searchCriteria.page === 0 || isLoading}
                className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg disabled:opacity-50"
              >
                Previous
              </button>
              <span className="text-gray-700">
                Page {searchCriteria.page + 1} of {pageData.totalPages}
              </span>
              <button
                onClick={() => handlePageChange(searchCriteria.page + 1)}
                disabled={pageData.last || isLoading}
                className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default DashboardPage;
