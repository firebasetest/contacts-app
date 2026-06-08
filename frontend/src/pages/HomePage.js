import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Header from '../components/Header';

const HomePage = () => {
  const { token } = useAuth();
  const navigate = useNavigate();

  React.useEffect(() => {
    if (token) {
      navigate('/dashboard');
    }
  }, [token, navigate]);

  return (
    <>
      <Header />
      <div className="min-h-screen bg-gradient-to-r from-blue-500 to-blue-600 flex items-center justify-center">
        <div className="text-center text-white px-4">
          <h1 className="text-5xl font-bold mb-4">📇 Contacts Manager</h1>
          <p className="text-xl mb-8">Organize and manage your contacts efficiently</p>
          <div className="space-x-4">
            <Link
              to="/login"
              className="inline-block bg-white text-blue-600 font-bold py-3 px-6 rounded-lg hover:bg-gray-100 transition"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="inline-block bg-blue-800 text-white font-bold py-3 px-6 rounded-lg hover:bg-blue-900 transition"
            >
              Register
            </Link>
          </div>
        </div>
      </div>
    </>
  );
};

export default HomePage;
