package org.example.supply_gate_26514.contoller;

import jakarta.validation.Valid;
import org.example.supply_gate_26514.dto.ProductImageDto;
import org.example.supply_gate_26514.dto.ProductImageResponseDto;
import org.example.supply_gate_26514.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/product_images")
public class ProductImageController {
    @Autowired
    private ProductImageService imageService;

    @GetMapping("/allImages")
    public List<ProductImageResponseDto> getAllImages(){
        return imageService.getAllProductImages();
    }
    @PostMapping("/addAnImage")
    public ProductImageResponseDto addAnImage( @Valid @RequestBody ProductImageDto imageDto){
        return imageService.addProductImage(imageDto);

    }
    @PutMapping("/{imageId}")
    public ProductImageResponseDto updateImageLink(@PathVariable UUID imageId,ProductImageDto imageDto) {
        return imageService.updateProductImage(imageId, imageDto);
    }
    @DeleteMapping("/deleteImage/{imageId}")
    public void deleteImage(@PathVariable UUID imageId){
        imageService.deleteProductImage(imageId);
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
