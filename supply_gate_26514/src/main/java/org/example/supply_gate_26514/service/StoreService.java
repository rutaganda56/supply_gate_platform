package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.StoreDto;
import org.example.supply_gate_26514.dto.StoreResponseDto;
import org.example.supply_gate_26514.mapper.StoreMapper;
import org.example.supply_gate_26514.model.Store;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.StoreRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Gets all stores (paginated) with optional search.
     * 
     * @param pageable Pagination parameters
     * @param search Optional search term to filter stores (searches in storeName, storeEmail, phoneNumber)
     * @return Paginated stores
     */
    public Page<StoreResponseDto> getAllStores(Pageable pageable, String search) {
        Page<Store> stores;
        if (search != null && !search.trim().isEmpty()) {
            stores = storeRepository.findBySearch(search.trim(), pageable);
        } else {
            stores = storeRepository.findAll(pageable);
        }
        return stores.map(storeMapper::transformStoreDtoToStoreResponseDto);
    }

    /**
     * Gets all stores (non-paginated) - kept for backward compatibility.
     * @deprecated Use getAllStores(Pageable, String) instead
     */
    @Deprecated
    public List<StoreResponseDto> getAllStores() {
        return storeRepository.findAll().stream().map(storeMapper::transformStoreDtoToStoreResponseDto).collect(Collectors.toList());
    }
    
    public StoreResponseDto addStore(StoreDto storeDto) {
        // Get authenticated user from security context
        User authenticatedUser = securityUtils.getCurrentUser();
        var store = storeMapper.transformStoreToStoreDto(storeDto, authenticatedUser);
        var savedStore = storeRepository.save(store);
        return storeMapper.transformStoreDtoToStoreResponseDto(savedStore);
    }

    public StoreResponseDto updateStore(UUID storeId, StoreDto storeDto) {
        var existingStore = storeRepository.findById(storeId).orElse(new Store());
        existingStore.setPhoneNumber(storeDto.phoneNumber());
        existingStore.setStoreName(storeDto.storeName());
        existingStore.setStoreEmail(storeDto.storeEmail());
        // User is not updated - store remains bound to original user
        var updatedStore = storeRepository.save(existingStore);
        return storeMapper.transformStoreDtoToStoreResponseDto(updatedStore);
    }
    public String deleteStore(UUID storeId) {
        storeRepository.deleteById(storeId);
        return "Store deleted";
    }
}
