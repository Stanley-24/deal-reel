package com.dealonreels.amazonapi;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestPostgresConnection {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPass = dotenv.get("DB_PASS");

        // Clean URL like in migration service
        dbUrl = dbUrl.replace("&channel_binding=require", "").replace("?channel_binding=require", "");

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                System.out.println("✅ Connection successful!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Connection failed.");
        }
    }
}
