package com.dealonreels.amazonapi;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService() {
        this.productRepository = new ProductRepository();
    }
    
    public void processAndSaveProducts(JsonNode amazonResponse) throws Exception {
        List<Product> products = parseAmazonResponse(amazonResponse);
        
        if (!products.isEmpty()) {
            productRepository.saveProducts(products);
            System.out.println("✅ Saved " + products.size() + " products to database");
        } else {
            System.out.println("⚠️  No products found in Amazon response");
        }
    }
    
    private List<Product> parseAmazonResponse(JsonNode response) {
        List<Product> products = new ArrayList<>();
        
        try {
            JsonNode itemsResult = response.get("ItemsResult");
            if (itemsResult != null && itemsResult.has("Items")) {
                JsonNode items = itemsResult.get("Items");
                
                for (JsonNode item : items) {
                    Product product = parseProductFromJson(item);
                    if (product != null) {
                        products.add(product);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error parsing Amazon response: " + e.getMessage());
        }
        
        return products;
    }
    
    private Product parseProductFromJson(JsonNode item) {
        try {
            // Extract ASIN
            String asin = item.get("ASIN").asText();
            
            // Extract title
            String title = "";
            JsonNode itemInfo = item.get("ItemInfo");
            if (itemInfo != null && itemInfo.has("Title")) {
                JsonNode titleNode = itemInfo.get("Title");
                if (titleNode.has("DisplayValue")) {
                    title = titleNode.get("DisplayValue").asText();
                }
            }
            
            // Extract image URL
            String imageUrl = "";
            JsonNode images = item.get("Images");
            if (images != null && images.has("Primary")) {
                JsonNode primary = images.get("Primary");
                if (primary.has("Small")) {
                    JsonNode small = primary.get("Small");
                    if (small.has("URL")) {
                        imageUrl = small.get("URL").asText();
                    }
                }
            }
            
            // Extract price
            BigDecimal priceAmount = null;
            JsonNode offers = item.get("Offers");
            if (offers != null && offers.has("Listings")) {
                JsonNode listings = offers.get("Listings");
                if (listings.isArray() && listings.size() > 0) {
                    JsonNode firstListing = listings.get(0);
                    if (firstListing.has("Price")) {
                        JsonNode price = firstListing.get("Price");
                        if (price.has("DisplayAmount")) {
                            String displayAmount = price.get("DisplayAmount").asText();
                            priceAmount = parsePrice(displayAmount);
                        }
                    }
                }
            }
            
            // Only create product if we have essential data
            if (asin != null && !asin.isEmpty() && title != null && !title.isEmpty()) {
                return new Product(asin, title, imageUrl, priceAmount);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing product: " + e.getMessage());
        }
        
        return null;
    }
    
    private BigDecimal parsePrice(String displayAmount) {
        if (displayAmount == null || displayAmount.isEmpty()) {
            return null;
        }
        
        // Remove currency symbols and extract numeric value
        // Handles formats like "$19.99", "€29.99", "£15.50", etc.
        Pattern pattern = Pattern.compile("([\\$€£¥]?)\\s*([\\d,]+(?:\\.\\d{2})?)");
        Matcher matcher = pattern.matcher(displayAmount);
        
        if (matcher.find()) {
            String numericValue = matcher.group(2).replace(",", "");
            try {
                return new BigDecimal(numericValue);
            } catch (NumberFormatException e) {
                System.err.println("❌ Could not parse price: " + displayAmount);
            }
        }
        
        return null;
    }
    
    public List<Product> getAllProducts() throws Exception {
        return productRepository.findAll();
    }
    
    public List<Product> getRecentProducts(int limit) throws Exception {
        return productRepository.findRecent(limit);
    }
    
    public Optional<Product> getProductByAsin(String asin) throws Exception {
        return productRepository.findByAsin(asin);
    }
    
    public int getProductCount() throws Exception {
        return productRepository.count();
    }
} 