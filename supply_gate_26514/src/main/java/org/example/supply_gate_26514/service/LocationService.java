package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.model.Location;
import org.example.supply_gate_26514.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepo;
    public String saveGovernmentStructure(String parentCode, Location location) {
        if (parentCode !=null){
           Optional<Location> getParent= locationRepo.findByStructureCode(parentCode);
            System.out.println(parentCode);
            System.out.println(getParent);
           if (getParent.isPresent()){
               location.setParent(getParent.get());
               if (!locationRepo.existsLocationByStructureCode(location.getStructureCode())){
                   locationRepo.save(location);
                   return "lower GovernmentStructure saved successfully";
               }
               else {
                   return "one more GovernmentStructure already exists.";
               }
           }
           else {
               return "no higher organisation structure exists.";
           }
        }
        else {
            if (!locationRepo.existsLocationByStructureCode(location.getStructureCode())){
                locationRepo.save(location);
                return "higher GovernmentStructure saved successfully";
            }
            else {
                return "higher GovernmentStructure already exists in the database.";
            }
        }
    }
    public Optional<Location> getHigherOrgStructureByLowerOrgStructure(String lowerOrgCode) {
        return locationRepo.findByStructureCode(lowerOrgCode);
    }

    public ResponseEntity<?> getGovernmentStructures() {
        return ResponseEntity.ok(locationRepo.findAll());
    }
}
