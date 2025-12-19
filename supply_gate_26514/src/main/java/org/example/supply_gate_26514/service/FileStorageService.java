package org.example.supply_gate_26514.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file storage operations.
 * 
 * Follows enterprise standards for file management:
 * - Validates file types and sizes
 * - Generates unique filenames to prevent collisions
 * - Stores files in organized directory structure
 * - Provides secure file access
 */
@Service
public class FileStorageService {
    
    // Allowed image MIME types
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/gif"
    );
    
    // Allowed document MIME types (for verification documents)
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "application/pdf"
    );
    
    // Maximum file size: 5MB (in bytes)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // Maximum document size: 10MB (in bytes) - for verification documents
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    /**
     * Validates an image file.
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: JPG, PNG, WEBP, GIF"
            );
        }
        
        // Additional validation: check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File must have a filename");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp", "gif");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file extension. Allowed extensions: .jpg, .jpeg, .png, .webp, .gif"
            );
        }
    }
    
    /**
     * Stores an image file and returns the relative path.
     * 
     * @param file The file to store
     * @param subdirectory Subdirectory within uploads (e.g., "products")
     * @return Relative path to the stored file (e.g., "products/uuid-filename.jpg")
     * @throws IOException if file storage fails
     */
    public String storeImage(MultipartFile file, String subdirectory) throws IOException {
        validateImageFile(file);
        
        // Create directory structure if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename to prevent collisions
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "-" + sanitizeFilename(originalFilename);
        
        // Store file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return subdirectory + "/" + uniqueFilename;
    }
    
    /**
     * Stores multiple image files.
     * 
     * @param files List of files to store
     * @param subdirectory Subdirectory within uploads
     * @return List of relative paths to stored files
     * @throws IOException if file storage fails
     */
    public List<String> storeImages(List<MultipartFile> files, String subdirectory) throws IOException {
        List<String> storedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                storedPaths.add(storeImage(file, subdirectory));
            }
        }
        return storedPaths;
    }
    
    /**
     * Gets the full path to a stored file.
     * 
     * @param relativePath Relative path from database (e.g., "products/uuid-filename.jpg")
     * @return Full path to the file
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
    }
    
    /**
     * Validates a document file (PDF or image).
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    public void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }
        
        // Check file size
        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_DOCUMENT_SIZE / (1024 * 1024))
            );
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DOCUMENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: PDF, JPG, PNG"
            );
        }
        
        // Additional validation: check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File must have a filename");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = List.of("pdf", "jpg", "jpeg", "png");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file extension. Allowed extensions: .pdf, .jpg, .jpeg, .png"
            );
        }
    }
    
    /**
     * Stores a document file (PDF or image) and returns the relative path.
     * Used for verification documents.
     * 
     * @param file The file to store
     * @param subdirectory Subdirectory within uploads (e.g., "verifications")
     * @return Relative path to the stored file (e.g., "verifications/uuid-filename.pdf")
     * @throws IOException if file storage fails
     */
    public String storeDocument(MultipartFile file, String subdirectory) throws IOException {
        validateDocumentFile(file);
        
        // Create directory structure if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename to prevent collisions
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "-" + sanitizeFilename(originalFilename);
        
        // Store file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return subdirectory + "/" + uniqueFilename;
    }
    
    /**
     * Deletes a stored file.
     * 
     * @param relativePath Relative path to the file
     * @throws IOException if file deletion fails
     */
    public void deleteFile(String relativePath) throws IOException {
        Path filePath = getFilePath(relativePath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
    
    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * Sanitizes filename to prevent path traversal and other security issues.
     */
    private String sanitizeFilename(String filename) {
        // Remove path separators and other dangerous characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}











