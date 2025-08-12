package com.dealonreels.amazonapi;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Connection;

public class MainApp {
    public static void main(String[] args) {
        try (Connection conn = DatabaseClient.getConnection()) {
            System.out.println("✅ Connected to Neon Database!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        AmazonApiClient client = new AmazonApiClient();
        try {
            String[] itemIds = {"B07PGL2ZSL", "B08N5WRWNW"}; // Sample ASINs
            JsonNode response = client.getItems(itemIds);
            System.out.println(response.toPrettyString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
