ALTER TABLE contacts 
ADD COLUMN total_price NUMERIC GENERATED ALWAYS AS ((custom_attributes->>'price')::numeric + (custom_attributes->>'tax')::numeric) STORED;