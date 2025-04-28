package iuh.fit.se.controllers;

import iuh.fit.se.models.Category;
import iuh.fit.se.services.CategoryService;
import iuh.fit.se.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private S3Service s3Service;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/categories")
    public ResponseEntity<Object> createCategory(
            @RequestPart(value = "category", required = true) Category category,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            System.out.println("Received category: " + category);
            System.out.println("Received image: " + (image != null ? image.getOriginalFilename() : "null"));

            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = s3Service.uploadFile(image);
                    category.setImage(imageUrl);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error uploading image to S3: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            Category savedCategory;
            try {
                savedCategory = categoryService.saveCategory(category);
            } catch (Exception e) {
                return new ResponseEntity<>("Error saving category to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedCategory.getId())
                    .toUri();
            return ResponseEntity.created(location).body(savedCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<HttpStatus> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Object> updateCategory(
            @PathVariable Long id,
            @RequestPart(value = "category", required = true) Category updatedCategory,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        if (existingCategory.isPresent()) {
            updatedCategory.setId(id);
            try {
                System.out.println("Updating category: " + updatedCategory);
                System.out.println("Received image: " + (image != null ? image.getOriginalFilename() : "null"));

                if (image != null && !image.isEmpty()) {
                    try {
                        String imageUrl = s3Service.uploadFile(image);
                        updatedCategory.setImage(imageUrl);
                    } catch (Exception e) {
                        return new ResponseEntity<>("Error uploading image to S3: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

                Category savedCategory;
                try {
                    savedCategory = categoryService.saveCategory(updatedCategory);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error saving category to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>(savedCategory, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}