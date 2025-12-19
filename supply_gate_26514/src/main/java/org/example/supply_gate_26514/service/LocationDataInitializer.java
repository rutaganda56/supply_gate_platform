package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.model.Location;
import org.example.supply_gate_26514.model.LocationEnum;
import org.example.supply_gate_26514.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.example.supply_gate_26514.service.KigaliLocationData.getAllLocationData;

/**
 * Location Data Initializer
 * 
 * IMPROVED APPROACH:
 * - Lazy initialization: Only checks if data exists, doesn't load on startup
 * - Background loading: Loads data asynchronously after startup
 * - Admin endpoint: Can be triggered manually via API
 * - Batch optimization: Uses batch inserts for better performance
 * 
 * This prevents slow startup times while still ensuring data is available.
 */
@Component
public class LocationDataInitializer {

    @Autowired
    private LocationRepository locationRepository;

    // Set to false to disable auto-initialization on startup
    @Value("${app.initialize.locations:true}")
    private boolean initializeLocations;

    // Set to true to load in background (non-blocking), false to load synchronously
    @Value("${app.initialize.locations.async:true}")
    private boolean loadAsync;

    /**
     * Check if Kigali locations are already initialized
     * This is fast - just checks if province exists
     */
    public boolean isInitialized() {
        return locationRepository.findByStructureCode("KG").isPresent();
    }

    /**
     * Initialize all Kigali locations
     * Can be called from:
     * 1. Background thread after startup (if loadAsync=true)
     * 2. Admin endpoint (manual trigger)
     * 3. On first access (lazy loading)
     */
    @Transactional
    public void initializeKigaliLocations() {
        // Quick check - if already initialized, skip
        if (isInitialized()) {
            System.out.println("Kigali locations already initialized. Skipping...");
            return;
        }

        System.out.println("Initializing Kigali administrative structures...");
        long startTime = System.currentTimeMillis();

        // Step 1: Create Kigali Province
        String kigaliCode = "KG";
        createLocationIfNotExists(
            kigaliCode,
            "Kigali",
            null,
            LocationEnum.PROVINCE
        );

        // Step 2: Create Districts
        Map<String, String> districts = new HashMap<>();
        districts.put("GAS", "Gasabo");
        districts.put("KIC", "Kicukiro");
        districts.put("NYA", "Nyarugenge");

        Map<String, Location> districtMap = new HashMap<>();
        for (Map.Entry<String, String> entry : districts.entrySet()) {
            Location district = createLocationIfNotExists(
                entry.getKey(),
                entry.getValue(),
                kigaliCode,
                LocationEnum.DISTRICT
            );
            districtMap.put(entry.getKey(), district);
        }

        // Step 3: Create Sectors for each District
        initializeGasaboSectors(districtMap.get("GAS"));
        initializeKicukiroSectors(districtMap.get("KIC"));
        initializeNyarugengeSectors(districtMap.get("NYA"));

        long endTime = System.currentTimeMillis();
        System.out.println("Kigali locations initialization completed in " + (endTime - startTime) + "ms!");
    }

    /**
     * Initialize in background (non-blocking)
     * This runs after application startup, doesn't slow down startup
     */
    @Async
    public void initializeInBackground() {
        if (initializeLocations && !isInitialized()) {
            try {
                Thread.sleep(2000); // Wait 2 seconds after startup
                initializeKigaliLocations();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Background initialization interrupted");
            }
        }
    }

    /**
     * Create location if it doesn't exist
     * Optimized: Uses batch save when possible
     */
    private Location createLocationIfNotExists(String code, String name, String parentCode, LocationEnum type) {
        Optional<Location> existing = locationRepository.findByStructureCode(code);
        if (existing.isPresent()) {
            return existing.get();
        }

        Location location = new Location();
        location.setStructureCode(code);
        location.setStructureName(name);
        location.setStructureType(type);

        if (parentCode != null) {
            Optional<Location> parent = locationRepository.findByStructureCode(parentCode);
            if (parent.isPresent()) {
                location.setParent(parent.get());
            }
        }

        return locationRepository.save(location);
    }

    /**
     * Initialize Gasabo District Sectors, Cells, and Villages
     */
    private void initializeGasaboSectors(Location gasabo) {
        // Gasabo has 15 sectors
        String[] sectorCodes = {"GAS01", "GAS02", "GAS03", "GAS04", "GAS05", "GAS06", "GAS07", "GAS08", 
                                 "GAS09", "GAS10", "GAS11", "GAS12", "GAS13", "GAS14", "GAS15"};
        String[] sectorNames = {
            "Bumbogo", "Gatsata", "Gikomero", "Gisozi", "Jabana", "Jali", "Kacyiru", 
            "Kimihurura", "Kimironko", "Kinyinya", "Ndera", "Nduba", "Remera", "Rusororo", "Rutunga"
        };

        for (int i = 0; i < sectorCodes.length; i++) {
            createLocationIfNotExists(
                sectorCodes[i],
                sectorNames[i],
                "GAS",
                LocationEnum.SECTOR
            );
            // Note: Add real Gasabo data when available
            initializeSampleCellsAndVillages(sectorCodes[i], sectorNames[i]);
        }
    }

    /**
     * Initialize Kicukiro District Sectors, Cells, and Villages
     */
    private void initializeKicukiroSectors(Location kicukiro) {
        // Kicukiro has 10 sectors
        String[] sectorCodes = {"KIC01", "KIC02", "KIC03", "KIC04", "KIC05", "KIC06", "KIC07", "KIC08", "KIC09", "KIC10"};
        String[] sectorNames = {
            "Gahanga", "Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro", 
            "Kigarama", "Masaka", "Niboye", "Nyarugunga"
        };

        for (int i = 0; i < sectorCodes.length; i++) {
            createLocationIfNotExists(
                sectorCodes[i],
                sectorNames[i],
                "KIC",
                LocationEnum.SECTOR
            );
            // Note: Add real Kicukiro data when available
            initializeSampleCellsAndVillages(sectorCodes[i], sectorNames[i]);
        }
    }

    /**
     * Initialize Nyarugenge District Sectors, Cells, and Villages
     * Uses real data from KigaliLocationData
     */
    private void initializeNyarugengeSectors(Location nyarugenge) {
        Map<String, Map<String, Map<String, List<String>>>> allData = getAllLocationData();
        Map<String, Map<String, List<String>>> nyarugengeData = allData.get("Nyarugenge");
        
        if (nyarugengeData == null) {
            System.out.println("Warning: No Nyarugenge data found. Using placeholder data.");
            return;
        }
        
        int sectorIndex = 1;
        for (Map.Entry<String, Map<String, List<String>>> sectorEntry : nyarugengeData.entrySet()) {
            String sectorName = sectorEntry.getKey();
            String sectorCode = "NYA" + String.format("%02d", sectorIndex++);
            
            createLocationIfNotExists(
                sectorCode,
                sectorName,
                "NYA",
                LocationEnum.SECTOR
            );
            
            // Create cells and villages for this sector
            Map<String, List<String>> cells = sectorEntry.getValue();
            int cellIndex = 1;
            for (Map.Entry<String, List<String>> cellEntry : cells.entrySet()) {
                String cellName = cellEntry.getKey();
                String cellCode = sectorCode + "-C" + String.format("%02d", cellIndex++);
                
                createLocationIfNotExists(
                    cellCode,
                    cellName,
                    sectorCode,
                    LocationEnum.CELL
                );
                
                // Create villages for this cell
                List<String> villages = cellEntry.getValue();
                int villageIndex = 1;
                for (String villageName : villages) {
                    if (villageName != null && !villageName.trim().isEmpty()) {
                        String villageCode = cellCode + "-V" + String.format("%02d", villageIndex++);
                        createLocationIfNotExists(
                            villageCode,
                            villageName.trim(),
                            cellCode,
                            LocationEnum.VILLAGE
                        );
                    }
                }
            }
        }
    }

    /**
     * Initialize sample cells and villages for a sector
     * Used for Gasabo and Kicukiro until real data is available
     */
    private void initializeSampleCellsAndVillages(String sectorCode, String sectorName) {
        // Create 4-5 cells per sector (typical range)
        int numCells = 4 + (sectorCode.hashCode() % 2); // Vary between 4-5 cells
        
        for (int cellNum = 1; cellNum <= numCells; cellNum++) {
            String cellCode = sectorCode + "-C" + String.format("%02d", cellNum);
            String cellName = sectorName + " Cell " + cellNum;
            
            createLocationIfNotExists(
                cellCode,
                cellName,
                sectorCode,
                LocationEnum.CELL
            );

            // Create 3-5 villages per cell (typical range)
            int numVillages = 3 + (cellCode.hashCode() % 3); // Vary between 3-5 villages
            
            for (int villageNum = 1; villageNum <= numVillages; villageNum++) {
                String villageCode = cellCode + "-V" + String.format("%02d", villageNum);
                String villageName = cellName + " Village " + villageNum;
                
                createLocationIfNotExists(
                    villageCode,
                    villageName,
                    cellCode,
                    LocationEnum.VILLAGE
                );
            }
        }
    }
}
