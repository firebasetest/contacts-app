// src/context/AuthContext.js
import React, { useContext, createContext, useState } from 'react';

// Mock global context for roles and IDs
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    // Simulate fetching user details from a /user endpoint
    const [user, setUser] = useState({
        isAuthenticated: true,
        roles: ['BUSINESS_UNIT_ADMIN'], // Example: Role determined at login
        currentBusinessUnitId: 'acme-corp' // The tenant ID the user is logged into
    });

    // Add actual login/logout logic here
    return (
        <AuthContext.Provider value={{ user, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
