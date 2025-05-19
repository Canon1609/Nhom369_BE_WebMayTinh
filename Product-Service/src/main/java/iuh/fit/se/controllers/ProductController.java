package iuh.fit.se.controllers;

import iuh.fit.se.dto.AddCommentRequest;
import iuh.fit.se.models.Comment;
import iuh.fit.se.models.Product;
import iuh.fit.se.services.ProductService;
import iuh.fit.se.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;


@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ProductController {
    @Autowired
    private ProductService productService;



    @Autowired
    private S3Service s3Service;

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") long id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            return new ResponseEntity<>(product.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Object> createProduct(
            @RequestPart(value = "product", required = true) Product product,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            product.setPriceAfterDiscount(product.getPrice() - (product.getPrice() * product.getDiscount() / 100));
            System.out.println("Received product: " + product);
            System.out.println("Received image: " + (image != null ? image.getOriginalFilename() : "null"));

            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = s3Service.uploadFile(image);
                    product.setImage(imageUrl);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error uploading image to S3: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            Product savedProduct;
            try {
                savedProduct = productService.saveProduct(product);
            } catch (Exception e) {
                return new ResponseEntity<>("Error saving product to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedProduct.getId())
                    .toUri();
            return ResponseEntity.created(location).body(savedProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable("id") long id) {
        try {
            productService.deleteProductById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(
            @PathVariable("id") long id,
            @RequestPart(value = "product", required = true) Product product,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        Optional<Product> existingProduct = productService.getProductById(id);
        if (existingProduct.isPresent()) {
            product.setId(id);
            try {
                System.out.println("Updating product: " + product);
                System.out.println("Received image: " + (image != null ? image.getOriginalFilename() : "null"));

                if (image != null && !image.isEmpty()) {
                    try {
                        String imageUrl = s3Service.uploadFile(image);
                        product.setImage(imageUrl);
                    } catch (Exception e) {
                        return new ResponseEntity<>("Error uploading image to S3: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

                product.setPriceAfterDiscount(product.getPrice() - (product.getPrice() * product.getDiscount() / 100));

                Product savedProduct;
                try {
                    savedProduct = productService.saveProduct(product);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error saving product to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>(savedProduct, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/products/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam("minPrice") double minPrice,
            @RequestParam("maxPrice") double maxPrice) {
        try {
            if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
            }
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            if (products.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProductsByKeyword(@RequestParam("keyword") String keyword) {
        try {
            List<Product> products = productService.searchProductsByKeyword(keyword);
            if (products.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<Map<String, Object>> getProductInventory(@PathVariable("productId") Long productId) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.NOT_FOUND);
            }
            Product product = productOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("productId", product.getId());
            response.put("name", product.getName());
            response.put("quantity", product.getQuantity());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{productId}/inventory")
    public ResponseEntity<Product> updateProductInventory(
            @PathVariable("productId") Long productId,
            @RequestBody Map<String, Integer> request) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Integer quantity = request.get("quantity");
            if (quantity == null || quantity < 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Product product = productOpt.get();
            product.setQuantity(quantity);
            Product updatedProduct = productService.saveProduct(product);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable("categoryId") Long categoryId) {
        try {
            List<Product> products = productService.getProductsByCategoryId(categoryId);
            if (products.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/products/update-quantity/{productId}")
    public ResponseEntity<Product> updateProductQuantity(
            @PathVariable("productId") Long productId,
            @RequestBody Product productDto) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Long quantity = productDto.getQuantity();
            if (quantity == null || quantity < 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Product product = productOpt.get();
            product.setQuantity(quantity);
            Product updatedProduct = productService.saveProduct(product);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/products/category/{categoryId}/factories")
    public ResponseEntity<List<String>> getFactoriesByCategory(@PathVariable("categoryId") Long categoryId) {
        try {
            List<Product> products = productService.getProductsByCategoryId(categoryId);
            if (products.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
            }
            // Lấy danh sách duy nhất các factory
            Set<String> uniqueFactories = new HashSet<>();
            for (Product product : products) {
                if (product.getFactory() != null && !product.getFactory().isEmpty()) {
                    uniqueFactories.add(product.getFactory());
                }
            }
            List<String> factories = new ArrayList<>(uniqueFactories);
            return new ResponseEntity<>(factories, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/product/addComment/{productId}")
    public ResponseEntity<Comment> addCommentToProduct(
            @PathVariable("productId") Long productId,
            @RequestBody AddCommentRequest comment) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (!productOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Long userId = comment.getUserId();
            String userName = comment.getUserName();
            int rating = comment.getRating();
            String commentText = comment.getComment();
            Comment addComment = productService.addComment(userId, userName, rating, commentText, productId);
            return new ResponseEntity<>(addComment, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{productId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByProductId(@PathVariable("productId") Long productId) {
        try {
            List<Comment> comments = productService.getCommentsByProductId(productId);
            if (comments.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/checkUserComment/{productId}/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserComment(@PathVariable("productId") Long productId, @PathVariable("userId") Long userId) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Comment> comments = productService.getCommentsByProductId(productId);
            System.out.println("Comments: " + comments);
            boolean hasCommented = false;
            for (Comment comment : comments) {
                if (comment.getUserId().equals(userId)) {
                    hasCommented = true;
                    break;
                }
            }
            response.put("hasCommented", hasCommented);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

