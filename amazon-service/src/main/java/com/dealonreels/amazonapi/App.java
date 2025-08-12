package com.dealonreels.amazonapi;

public class App {
    public static void main(String[] args) {
        System.out.println("Running DB migrations...");
        MigrationService.migrate();
        System.out.println("Migrations complete!");
    }
}
