import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosClient from './axiosClient';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('auth_token'));
  const [loading, setLoading] = useState(false);

  // 1. Internal Fallback Login Method
  const loginInternal = async (username, password) => {
    setLoading(true);
    try {
      const response = await axiosClient.post('/auth/login', { username, password });
      const { token: jwtToken } = response.data;

      localStorage.setItem('auth_token', jwtToken);
      setToken(jwtToken);
      toast.success('Successfully logged in (Internal Auth)');
      return true;
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials');
      return false;
    } finally {
      setLoading(false);
    }
  };

  // 2. External SSO Authorization Server Redirect Method
  const loginExternalSSO = () => {
    // Redirect to your external OIDC Provider (e.g., Keycloak / Auth0 / Okta)
    const externalIssuerUri = "https://auth.yourdomain.com/realms/main/protocol/openid-connect/auth";
    const clientId = "your-app-client-id";
    const redirectUri = encodeURIComponent(`${window.location.origin}/oauth2/callback`);

    window.location.href = `${externalIssuerUri}?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=openid%20profile%20email`;
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    setToken(null);
    toast.success('Logged out successfully');
  };

  return (
    <AuthContext.Provider value={{ 
      token, 
      isAuthenticated: !!token, 
      loading, 
      loginInternal, 
      loginExternalSSO, 
      logout 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);