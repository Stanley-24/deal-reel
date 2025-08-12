package com.dealonreels.amazonapi;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class ProductDatabaseTest {
    public static void main(String[] args) {
        try {
            System.out.println("🔄 Starting Product Database Test...\n");
            
            // Test 1: Fetch products from Amazon API (mock mode) and save to database
            System.out.println("📦 Step 1: Fetching products from Amazon API...");
            AmazonApiClient client = new AmazonApiClient();
            String[] itemIds = {"B07PGL2ZSL", "B08N5WRWNW"};
            
            JsonNode response = client.getItems(itemIds);
            System.out.println("✅ Amazon API response received");
            System.out.println("📄 Response preview: " + response.get("ItemsResult").get("Items").size() + " items found\n");
            
            // Test 2: Retrieve products from database
            System.out.println("💾 Step 2: Retrieving products from database...");
            ProductService productService = new ProductService();
            
            // Get all products
            List<Product> allProducts = productService.getAllProducts();
            System.out.println("📊 Total products in database: " + allProducts.size());
            
            // Get recent products
            List<Product> recentProducts = productService.getRecentProducts(5);
            System.out.println("🕒 Recent products (last 5): " + recentProducts.size());
            
            // Display product details
            System.out.println("\n📋 Product Details:");
            System.out.println("=".repeat(80));
            for (Product product : recentProducts) {
                System.out.printf("ASIN: %-15s | Title: %-30s | Price: $%-8s | Image: %s%n",
                    product.getAsin(),
                    truncate(product.getTitle(), 30),
                    product.getPriceAmount() != null ? product.getPriceAmount().toString() : "N/A",
                    truncate(product.getImageUrl(), 40));
            }
            System.out.println("=".repeat(80));
            
            // Test 3: Search for specific product
            System.out.println("\n🔍 Step 3: Searching for specific product...");
            String searchAsin = "B07PGL2ZSL";
            var foundProduct = productService.getProductByAsin(searchAsin);
            
            if (foundProduct.isPresent()) {
                Product product = foundProduct.get();
                System.out.println("✅ Found product: " + product.getTitle());
                System.out.println("   Price: $" + product.getPriceAmount());
                System.out.println("   Created: " + product.getCreatedAt());
            } else {
                System.out.println("❌ Product not found: " + searchAsin);
            }
            
            System.out.println("\n🎉 Product Database Test completed successfully! ✅");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }
} 