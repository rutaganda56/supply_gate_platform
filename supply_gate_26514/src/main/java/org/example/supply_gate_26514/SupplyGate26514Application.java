package org.example.supply_gate_26514;

import org.example.supply_gate_26514.service.LocationDataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableAsync  // Enable async support for background loading
public class SupplyGate26514Application implements CommandLineRunner {

    @Autowired
    private LocationDataInitializer locationDataInitializer;

    public static void main(String[] args) {
        SpringApplication.run(SupplyGate26514Application.class, args);
    }

    @Override
    public void run(String... args) {
        // Trigger background initialization if enabled
        // This runs AFTER application context is fully loaded
        locationDataInitializer.initializeInBackground();
    }
}
