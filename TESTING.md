# Testing Guide

Comprehensive testing guide for backend and frontend.

## Backend Testing (Spring Boot)

### Unit Tests

#### Run All Tests

```bash
cd backend
mvn test
```

#### Run Specific Test Class

```bash
mvn test -Dtest=ContactServiceTest
```

#### Run Tests with Coverage

```bash
mvn clean test jacoco:report
# View report at target/site/jacoco/index.html
```

### Test Structure

```
backend/src/test/java/com/example/contacts/
├── service/
│   ├── ContactServiceTest.java
│   └── AuthServiceTest.java
├── controller/
│   ├── ContactControllerTest.java
│   └── AuthControllerTest.java
└── repository/
    └── ContactRepositoryTest.java
```

### Example Backend Tests

**Service Test** (`ContactServiceTest.java`):

```java
class ContactServiceTest {
    @Mock
    private ContactRepository contactRepository;
    
    @InjectMocks
    private ContactService contactService;
    
    @Test
    void testGetAllContacts() {
        // Arrange
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findByUser(testUser)).thenReturn(contacts);
        
        // Act
        List<ContactDTO> result = contactService.getAllContacts(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
```

**Controller Test** (`ContactControllerTest.java`):

```java
@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser
    void testGetAllContacts() throws Exception {
        mockMvc.perform(get("/api/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }
}
```

---

## Frontend Testing (React)

### Run Tests

```bash
cd frontend
npm test
```

### Run Tests with Coverage

```bash
npm test -- --coverage
```

### Test Structure

```
frontend/src/__tests__/
├── components/
│   ├── SearchFilter.test.js
│   ├── ContactCard.test.js
│   └── ContactForm.test.js
├── pages/
│   ├── DashboardPage.test.js
│   └── LoginPage.test.js
└── services/
    └── api.test.js
```

### Example Frontend Tests

**Component Test** (`SearchFilter.test.js`):

```javascript
import { render, screen, fireEvent } from '@testing-library/react';
import SearchFilter from '../../components/SearchFilter';

describe('SearchFilter Component', () => {
  const mockOnSearch = jest.fn();
  
  test('renders search inputs', () => {
    render(<SearchFilter onSearch={mockOnSearch} isLoading={false} />);
    expect(screen.getByPlaceholderText(/Search/i)).toBeInTheDocument();
  });
  
  test('calls onSearch on button click', () => {
    render(<SearchFilter onSearch={mockOnSearch} isLoading={false} />);
    fireEvent.click(screen.getByText('🔍 Search'));
    expect(mockOnSearch).toHaveBeenCalled();
  });
});
```

**Service Test** (`api.test.js`):

```javascript
import { contactAPI } from '../../services/api';

jest.mock('axios');

describe('API Service', () => {
  test('contact API has all required methods', () => {
    expect(contactAPI.getAll).toBeDefined();
    expect(contactAPI.search).toBeDefined();
    expect(contactAPI.exportCSV).toBeDefined();
  });
});
```

---

## Integration Tests

### Backend Integration Tests

```bash
mvn test -Dtest=*IntegrationTest
```

### Frontend Integration Tests

```bash
npm test -- --testPathPattern=integration
```

---

## Test Coverage Goals

- **Backend**: 80%+ coverage
- **Frontend**: 70%+ coverage
- **Critical paths**: 100% coverage

---

## Continuous Integration

Tests run automatically on:
- Pull Requests
- Commits to main branch
- Scheduled daily runs

See `.github/workflows/` for CI configuration.
