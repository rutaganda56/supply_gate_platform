package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

/**
 * Controller for serving uploaded images.
 * 
 * This endpoint provides public access to product images.
 * Images are served securely without exposing internal file paths.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Serves an image file.
     * 
     * @param path Relative path to the image (e.g., "products/uuid-filename.jpg")
     * @return Image file as resource
     */
    @GetMapping
    public ResponseEntity<Resource> serveImage(@RequestParam String path) {
        try {
            Path filePath = fileStorageService.getFilePath(path);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = determineContentType(path);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Determines content type based on file extension.
     * Supports both images and documents (PDF).
     */
    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }
}











