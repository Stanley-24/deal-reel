-- Create products table for storing Amazon product data
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    asin VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    image_url TEXT,
    price_amount DECIMAL(10,2),
    price_currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on ASIN for faster lookups
CREATE INDEX IF NOT EXISTS idx_products_asin ON products(asin);

-- Create index on created_at for sorting
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at); 