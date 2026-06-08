import React, { useState } from 'react';
import toast from 'react-hot-toast';

const SearchFilter = ({ onSearch, isLoading }) => {
  const [keyword, setKeyword] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState('desc');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const handleSearch = () => {
    onSearch({
      keyword,
      sortBy,
      sortOrder,
      page,
      size,
    });
  };

  const handleReset = () => {
    setKeyword('');
    setSortBy('createdAt');
    setSortOrder('desc');
    setPage(0);
    onSearch({ keyword: '', sortBy: 'createdAt', sortOrder: 'desc', page: 0, size });
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-md mb-6">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
        <div>
          <label className="block text-gray-700 font-bold mb-2">Search</label>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search by name, email, or phone"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none"
            disabled={isLoading}
          />
        </div>

        <div>
          <label className="block text-gray-700 font-bold mb-2">Sort By</label>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none"
            disabled={isLoading}
          >
            <option value="name">Name</option>
            <option value="email">Email</option>
            <option value="createdAt">Date Created</option>
            <option value="updatedAt">Date Updated</option>
          </select>
        </div>

        <div>
          <label className="block text-gray-700 font-bold mb-2">Order</label>
          <select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none"
            disabled={isLoading}
          >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </div>

        <div>
          <label className="block text-gray-700 font-bold mb-2">Items Per Page</label>
          <select
            value={size}
            onChange={(e) => setSize(parseInt(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none"
            disabled={isLoading}
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
          </select>
        </div>
      </div>

      <div className="flex gap-2">
        <button
          onClick={handleSearch}
          disabled={isLoading}
          className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
        >
          🔍 Search
        </button>
        <button
          onClick={handleReset}
          disabled={isLoading}
          className="flex-1 bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
        >
          ↺ Reset
        </button>
      </div>
    </div>
  );
};

export default SearchFilter;
