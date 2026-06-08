export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

export const validatePhone = (phone) => {
  const re = /^[+]?[0-9]{10,13}$/;
  return re.test(phone.replace(/[\s-]/g, ''));
};

export const validatePassword = (password) => {
  return password.length >= 6;
};

export const validateForm = (data, type = 'contact') => {
  const errors = {};

  if (type === 'contact') {
    if (!data.name?.trim()) errors.name = 'Name is required';
    if (!data.email?.trim()) {
      errors.email = 'Email is required';
    } else if (!validateEmail(data.email)) {
      errors.email = 'Email is invalid';
    }
    if (!data.phone?.trim()) {
      errors.phone = 'Phone is required';
    } else if (!validatePhone(data.phone)) {
      errors.phone = 'Phone is invalid (10-13 digits)';
    }
  }

  if (type === 'register') {
    if (!data.email?.trim()) {
      errors.email = 'Email is required';
    } else if (!validateEmail(data.email)) {
      errors.email = 'Email is invalid';
    }
    if (!data.password?.trim()) {
      errors.password = 'Password is required';
    } else if (!validatePassword(data.password)) {
      errors.password = 'Password must be at least 6 characters';
    }
    if (!data.firstName?.trim()) errors.firstName = 'First name is required';
    if (!data.lastName?.trim()) errors.lastName = 'Last name is required';
  }

  if (type === 'login') {
    if (!data.email?.trim()) {
      errors.email = 'Email is required';
    } else if (!validateEmail(data.email)) {
      errors.email = 'Email is invalid';
    }
    if (!data.password?.trim()) errors.password = 'Password is required';
  }

  return errors;
};
