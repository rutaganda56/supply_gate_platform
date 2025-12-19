package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.GlobalSearchResultDto;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.example.supply_gate_26514.model.VerificationStatus;
import org.example.supply_gate_26514.repository.*;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GlobalSearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Performs global search across multiple entities.
     * SECURITY: Results are filtered based on user role and permissions.
     * 
     * @param query Search query string
     * @param limit Maximum results per category (default: 5)
     * @return GlobalSearchResultDto with categorized results
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public GlobalSearchResultDto search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new GlobalSearchResultDto(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0
            );
        }

        String searchTerm = query.trim().toLowerCase();
        Pageable pageable = PageRequest.of(0, limit);

        List<GlobalSearchResultDto.SearchResultItem> products = new ArrayList<>();
        List<GlobalSearchResultDto.SearchResultItem> stores = new ArrayList<>();
        List<GlobalSearchResultDto.SearchResultItem> categories = new ArrayList<>();
        List<GlobalSearchResultDto.SearchResultItem> verifications = new ArrayList<>();
        List<GlobalSearchResultDto.SearchResultItem> messages = new ArrayList<>();

        UUID currentUserId = null;
        UserEnum userRole = null;
        
        // Search Products (all users can see verified products - public and authenticated)
        Page<org.example.supply_gate_26514.model.Product> productResults = 
                productRepository.findByVerifiedSuppliersAndSearch(searchTerm, pageable);
        
        products = productResults.getContent().stream()
                .map(p -> new GlobalSearchResultDto.SearchResultItem(
                        p.getProductId().toString(),
                        p.getProductName(),
                        p.getProductDescription() != null ? 
                            (p.getProductDescription().length() > 100 ? 
                                p.getProductDescription().substring(0, 100) + "..." : 
                                p.getProductDescription()) : "",
                        "product",
                        "/website/products?search=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8),
                        p.getProductPrice() != null ? String.format("Price: $%.2f", p.getProductPrice()) : ""
                ))
                .collect(Collectors.toList());
        
        try {
            currentUserId = securityUtils.getCurrentUserId();
            User currentUser = securityUtils.getCurrentUser();
            userRole = currentUser.getUserType();

            // Search Stores (only if user is supplier or industry worker)
            if (userRole == UserEnum.SUPPLIER || userRole == UserEnum.INDUSTRY_WORKER) {
                Page<org.example.supply_gate_26514.model.Store> storeResults = 
                        storeRepository.findBySearch(searchTerm, pageable);
                
                stores = storeResults.getContent().stream()
                        .map(s -> new GlobalSearchResultDto.SearchResultItem(
                                s.getStoreId().toString(),
                                s.getStoreName(),
                                s.getStoreEmail() != null ? "Email: " + s.getStoreEmail() : "",
                                "store",
                                "/dashboard/store",
                                s.getPhoneNumber() != null ? "Phone: " + s.getPhoneNumber() : ""
                        ))
                        .collect(Collectors.toList());
            }

            // Search Categories (all authenticated users)
            Page<org.example.supply_gate_26514.model.Category> categoryResults = 
                    categoryRepository.findBySearch(searchTerm, pageable);
            
            categories = categoryResults.getContent().stream()
                    .map(c -> new GlobalSearchResultDto.SearchResultItem(
                            c.getCategoryId().toString(),
                            c.getCategoryName(),
                            "Category",
                            "category",
                            "/dashboard/store",
                            ""
                    ))
                    .collect(Collectors.toList());

            // Search Verifications (only industry workers can see all verifications)
            if (userRole == UserEnum.INDUSTRY_WORKER) {
                Page<org.example.supply_gate_26514.model.Verification> verificationResults = 
                        verificationRepository.findBySearch(searchTerm, pageable);
                
                verifications = verificationResults.getContent().stream()
                        .map(v -> {
                            String status = v.getStatus() != null ? v.getStatus().toString() : "UNKNOWN";
                            String metadata = "Status: " + status;
                            if (v.getCompanyName() != null) {
                                metadata += " | Company: " + v.getCompanyName();
                            }
                            return new GlobalSearchResultDto.SearchResultItem(
                                    v.getVerificationId().toString(),
                                    v.getUser() != null ? 
                                        (v.getUser().getFirstName() != null ? v.getUser().getFirstName() : "") + 
                                        " " + (v.getUser().getLastName() != null ? v.getUser().getLastName() : "") : 
                                        "Unknown Supplier",
                                    v.getCompanyName() != null ? v.getCompanyName() : "",
                                    "verification",
                                    "/industryDashBoard/verification",
                                    metadata
                            );
                        })
                        .collect(Collectors.toList());
            } else if (userRole == UserEnum.SUPPLIER) {
                // Suppliers can only see their own verification
                var verificationOpt = verificationRepository.findByUser_UserId(currentUserId);
                if (verificationOpt.isPresent()) {
                    var v = verificationOpt.get();
                    String searchLower = searchTerm.toLowerCase();
                    boolean matches = (v.getCompanyName() != null && v.getCompanyName().toLowerCase().contains(searchLower)) ||
                                     (v.getUser() != null && 
                                      ((v.getUser().getFirstName() != null && v.getUser().getFirstName().toLowerCase().contains(searchLower)) ||
                                       (v.getUser().getLastName() != null && v.getUser().getLastName().toLowerCase().contains(searchLower)) ||
                                       (v.getUser().getEmail() != null && v.getUser().getEmail().toLowerCase().contains(searchLower))));
                    
                    if (matches) {
                        String status = v.getStatus() != null ? v.getStatus().toString() : "UNKNOWN";
                        verifications.add(new GlobalSearchResultDto.SearchResultItem(
                                v.getVerificationId().toString(),
                                "My Verification",
                                v.getCompanyName() != null ? v.getCompanyName() : "",
                                "verification",
                                "/dashboard/verification",
                                "Status: " + status
                        ));
                    }
                }
            }

            // Search Messages (only suppliers can see their own messages)
            if (userRole == UserEnum.SUPPLIER) {
                Page<org.example.supply_gate_26514.model.Message> messageResults = 
                        messageRepository.findBySupplier_UserIdAndSearchOrderByCreatedAtDesc(currentUserId, searchTerm, pageable);
                
                messages = messageResults.getContent().stream()
                        .map(m -> new GlobalSearchResultDto.SearchResultItem(
                                m.getMessageId().toString(),
                                m.getSubject(),
                                m.getMessageContent() != null ? 
                                    (m.getMessageContent().length() > 100 ? 
                                        m.getMessageContent().substring(0, 100) + "..." : 
                                        m.getMessageContent()) : "",
                                "message",
                                "/dashboard/notifications",
                                "From: " + (m.getSenderName() != null ? m.getSenderName() : m.getSenderEmail())
                        ))
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            // User is not authenticated - products are already searched above
            // This is expected for public users - they can only see products
        }

        int totalResults = products.size() + stores.size() + categories.size() + 
                          verifications.size() + messages.size();

        return new GlobalSearchResultDto(
                products,
                stores,
                categories,
                verifications,
                messages,
                totalResults
        );
    }
}
