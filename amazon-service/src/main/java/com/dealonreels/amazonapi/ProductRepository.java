package com.dealonreels.amazonapi;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
    
    public void saveProduct(Product product) throws SQLException {
        String sql = """
            INSERT INTO products (asin, title, image_url, price_amount, price_currency)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (asin) 
            DO UPDATE SET 
                title = EXCLUDED.title,
                image_url = EXCLUDED.image_url,
                price_amount = EXCLUDED.price_amount,
                price_currency = EXCLUDED.price_currency,
                updated_at = CURRENT_TIMESTAMP
            """;
            
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getAsin());
            stmt.setString(2, product.getTitle());
            stmt.setString(3, product.getImageUrl());
            stmt.setBigDecimal(4, product.getPriceAmount());
            stmt.setString(5, product.getPriceCurrency());
            
            stmt.executeUpdate();
        }
    }
    
    public void saveProducts(List<Product> products) throws SQLException {
        String sql = """
            INSERT INTO products (asin, title, image_url, price_amount, price_currency)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (asin) 
            DO UPDATE SET 
                title = EXCLUDED.title,
                image_url = EXCLUDED.image_url,
                price_amount = EXCLUDED.price_amount,
                price_currency = EXCLUDED.price_currency,
                updated_at = CURRENT_TIMESTAMP
            """;
            
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Product product : products) {
                stmt.setString(1, product.getAsin());
                stmt.setString(2, product.getTitle());
                stmt.setString(3, product.getImageUrl());
                stmt.setBigDecimal(4, product.getPriceAmount());
                stmt.setString(5, product.getPriceCurrency());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    public Optional<Product> findByAsin(String asin) throws SQLException {
        String sql = "SELECT id, asin, title, image_url, price_amount, price_currency, created_at, updated_at FROM products WHERE asin = ?";
        
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, asin);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        }
        
        return Optional.empty();
    }
    
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT id, asin, title, image_url, price_amount, price_currency, created_at, updated_at FROM products ORDER BY created_at DESC";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        
        return products;
    }
    
    public List<Product> findRecent(int limit) throws SQLException {
        String sql = "SELECT id, asin, title, image_url, price_amount, price_currency, created_at, updated_at FROM products ORDER BY created_at DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        
        return products;
    }
    
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        
        try (Connection conn = DatabaseClient.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setAsin(rs.getString("asin"));
        product.setTitle(rs.getString("title"));
        product.setImageUrl(rs.getString("image_url"));
        product.setPriceAmount(rs.getBigDecimal("price_amount"));
        product.setPriceCurrency(rs.getString("price_currency"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
} 