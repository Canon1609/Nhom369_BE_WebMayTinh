//package iuh.fit.se.controller;
//
//
//import iuh.fit.se.dto.ProductRecommendationDTO;
//import iuh.fit.se.dto.ProductSalesDTO;
//import iuh.fit.se.dto.Response;
//import iuh.fit.se.entity.User;
//import iuh.fit.se.service.RecommendationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/recommendations")
//public class RecommendationController {
//
//    @Autowired
//    private RecommendationService recommendationService;
//
//    @GetMapping
//    public List<ProductRecommendationDTO> getRecommendations() {
//        return recommendationService.getRecommendedProducts();
//    }
//
//    @GetMapping("/product-sales")
//    public ResponseEntity<Response> getProductSales(@RequestHeader("Authorization") String token) {
//        try {
//            User user = recommendationService.getUserFromToken(token);
//            if (user == null || !"ADMIN".equals(user.getRole())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                        new Response(HttpStatus.FORBIDDEN.value(), "Unauthorized: Admin role required", null)
//                );
//            }
//
//            List<ProductSalesDTO> productSales = recommendationService.getProductSales();
//
//            if (productSales.isEmpty()) {
//                return ResponseEntity.ok(
//                        new Response(HttpStatus.OK.value(), "No sales data found", productSales)
//                );
//            }
//
//            return ResponseEntity.ok(
//                    new Response(HttpStatus.OK.value(), "Get product sales success", productSales)
//            );
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new Response(HttpStatus.BAD_REQUEST.value(), "Get product sales failed: " + e.getMessage(), null)
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null)
//            );
//        }
//    }
//}


package iuh.fit.se.controller;

import iuh.fit.se.dto.ProductRecommendationDTO;
import iuh.fit.se.dto.ProductSalesDTO;
import iuh.fit.se.dto.Response;
import iuh.fit.se.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public List<ProductRecommendationDTO> getRecommendations() {
        return recommendationService.getRecommendedProducts();
    }


    @GetMapping("/product-sales")
    public ResponseEntity<Response> getProductSales() {
        try {
            List<ProductSalesDTO> productSales = recommendationService.getProductSales();

            if (productSales.isEmpty()) {
                return ResponseEntity.ok(
                        new Response(HttpStatus.OK.value(), "No sales data found", productSales)
                );
            }

            return ResponseEntity.ok(
                    new Response(HttpStatus.OK.value(), "Get product sales success", productSales)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response(HttpStatus.BAD_REQUEST.value(), "Get product sales failed: " + e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null)
            );
        }
    }

}