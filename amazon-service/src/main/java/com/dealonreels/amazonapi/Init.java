package com.dealonreels.amazonapi;

public class Init {
    public static void main(String[] args) {
        System.out.println("Running database migrations...");
        MigrationService.migrate();
        System.out.println("Migration completed ✅");

        // Continue your app logic here...
    }
}
