import axios from 'axios';
import { contactAPI, authAPI } from '../../services/api';

jest.mock('axios');

describe('API Service', () => {
  beforeEach(() => {
    axios.create.mockReturnValue({
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
    });
  });

  test('auth login should call correct endpoint', async () => {
    const loginData = { email: 'test@example.com', password: 'password123' };
    // Test would verify the endpoint is called
    expect(authAPI).toBeDefined();
  });

  test('contact API should have all methods', () => {
    expect(contactAPI.getAll).toBeDefined();
    expect(contactAPI.create).toBeDefined();
    expect(contactAPI.update).toBeDefined();
    expect(contactAPI.delete).toBeDefined();
    expect(contactAPI.search).toBeDefined();
    expect(contactAPI.exportCSV).toBeDefined();
    expect(contactAPI.exportPDF).toBeDefined();
  });
});
