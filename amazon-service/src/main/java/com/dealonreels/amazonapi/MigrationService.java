package com.dealonreels.amazonapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;

public class MigrationService {

    public static void migrate() {
        System.out.println("Loading environment variables from .env file...");
        Dotenv dotenv = Dotenv.load();

        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPass = dotenv.get("DB_PASS");

        if (dbUrl == null || dbUser == null || dbPass == null) {
            throw new RuntimeException("Database environment variables not set properly.");
        }

        System.out.println("Original DB URL: " + dbUrl);

        // Remove query parameters for Flyway compatibility
        if (dbUrl.contains("?")) {
            System.out.println("Removing query parameters from DB URL for Flyway compatibility");
            dbUrl = dbUrl.substring(0, dbUrl.indexOf("?"));
        }
        System.out.println("Clean DB URL: " + dbUrl);

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL driver not found.");
            e.printStackTrace();
            return;
        }

        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, dbUser, dbPass)
                .driver("org.postgresql.Driver")  // Explicitly specify driver here
                .locations("classpath:db/migration")
                .load();

        System.out.println("Running database migrations...");
        flyway.migrate();
        System.out.println("Migration completed ✅");
    }

    public static void main(String[] args) {
        migrate();
    }
}
