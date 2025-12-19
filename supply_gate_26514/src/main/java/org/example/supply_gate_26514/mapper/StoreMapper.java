package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.StoreDto;
import org.example.supply_gate_26514.dto.StoreResponseDto;
import org.example.supply_gate_26514.model.Store;
import org.example.supply_gate_26514.model.User;
import org.springframework.stereotype.Service;

/**
 * Mapper for converting between Store entities and DTOs.
 * 
 * SECURITY: User entity is provided from authentication context,
 * not from client input, ensuring correct ownership.
 */
@Service
public class StoreMapper {

    /**
     * Converts StoreDto to Store entity, binding it to the authenticated user.
     * 
     * @param storeDto Store data from client
     * @param authenticatedUser User entity from authentication context
     * @return Store entity ready for persistence
     */
    public Store transformStoreToStoreDto(StoreDto storeDto, User authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user is required to create a store");
        }
        
        Store store = new Store();
        store.setStoreName(storeDto.storeName());
        store.setStoreEmail(storeDto.storeEmail());
        store.setPhoneNumber(storeDto.phoneNumber());
        
        // Bind store to authenticated user (from security context, not client input)
        store.setUser(authenticatedUser);
        
        return store;
    }
    public StoreResponseDto transformStoreDtoToStoreResponseDto(Store store) {
        return new StoreResponseDto(
                store.getStoreId(),
                store.getStoreName(),
                store.getPhoneNumber(),
                store.getStoreEmail()
        );
    }
}
