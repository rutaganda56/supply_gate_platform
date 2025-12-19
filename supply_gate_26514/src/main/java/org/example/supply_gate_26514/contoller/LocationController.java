package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.model.Location;
import org.example.supply_gate_26514.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    @Autowired
    private LocationService locationService;

    @PostMapping(value = "/addOrganisationStructure", consumes = "application/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addOrganisationStructure(@RequestParam(required = false) String parentCode,@RequestBody Location location) {
        String result = locationService.saveGovernmentStructure(parentCode, location);
        if (result.equals("lower GovernmentStructure saved successfully")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else if (result.equals("one more GovernmentStructure already exists.")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        } else if (result.equals("no higher organisation structure exists.")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } else if (result.equals("higher GovernmentStructure saved successfully")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(result, HttpStatus.FOUND);
        }
    }
    @GetMapping("/getGovernmentStructureByStructureCode")
    public ResponseEntity<?> getOrganisationStructure(@RequestParam String structureCode) {
        Optional<Location> higherOrgStructure= locationService.getHigherOrgStructureByLowerOrgStructure(structureCode);
        if (higherOrgStructure.isPresent()) {
            return new ResponseEntity<>(higherOrgStructure.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }
    @GetMapping("/getGovernmentStructures")
    public ResponseEntity<?> getGovernmentStructure() {
        return locationService.getGovernmentStructures();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMsg = error.getDefaultMessage();
            errors.put(fieldName, errorMsg);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

    }

}
