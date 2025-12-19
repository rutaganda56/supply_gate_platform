package org.example.supply_gate_26514.contoller;

import jakarta.validation.Valid;
import org.example.supply_gate_26514.dto.StoreDto;
import org.example.supply_gate_26514.dto.StoreResponseDto;
import org.example.supply_gate_26514.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * Get all stores (paginated with search).
     * Requires authentication.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param search Optional search term to filter stores
     */
    @GetMapping("/stores")
    public ResponseEntity<Page<StoreResponseDto>> getStores(
            @PageableDefault(size = 20, sort = "storeName") Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<StoreResponseDto> stores = storeService.getAllStores(pageable, search);
        return ResponseEntity.ok(stores);
    }
    @PostMapping("/createStore")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponseDto createStore(@Valid @RequestBody StoreDto storeDto) {
        return storeService.addStore(storeDto);
    }
    @PutMapping("/{id}")
    public StoreResponseDto updateStore(@PathVariable UUID id, @Valid @RequestBody StoreDto storeDto) {
        return storeService.updateStore(id,storeDto);
    }
    @DeleteMapping("/deleteStore/{id}")
    public void deleteStore(@PathVariable("id") UUID id) {
        storeService.deleteStore(id);
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
