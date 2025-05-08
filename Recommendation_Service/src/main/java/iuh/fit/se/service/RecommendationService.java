package iuh.fit.se.service;

import iuh.fit.se.dto.ProductRecommendationDTO;
import iuh.fit.se.dto.ProductSalesDTO;
import iuh.fit.se.entity.Recommendation;
import iuh.fit.se.entity.User;
import iuh.fit.se.model.Product;
import iuh.fit.se.repository.OrderDetailRepository;
import iuh.fit.se.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Value("${product.service.url:http://localhost:8082/api/products}")
    private String productServiceUrl;

    @Value("${auth.service.url:http://localhost:8080/users/account}")
    private String authServiceUrl;

    private List<Product> getProducts() {
        try {
            Product[] products = restTemplate.getForObject(productServiceUrl, Product[].class);
            return products != null ? Arrays.asList(products) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private double getFactoryScore(String factory) {
        Set<String> highPriority = new HashSet<>(Arrays.asList("Dell", "Intel", "ASUS"));
        Set<String> mediumPriority = new HashSet<>(Arrays.asList("Samsung", "Apple", "Xiaomi"));
        if (highPriority.contains(factory)) {
            return 3.0;
        } else if (mediumPriority.contains(factory)) {
            return 2.0;
        } else {
            return 1.0;
        }
    }

    public List<ProductRecommendationDTO> getRecommendedProducts() {
        List<Product> products = getProducts();

        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductRecommendationDTO> recommendations = products.stream()
                .map(product -> {
                    ProductRecommendationDTO dto = new ProductRecommendationDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setPrice(product.getPrice());
                    dto.setImage(product.getImage());
                    dto.setDescription(product.getDescription());
                    dto.setQuantity(product.getQuantity());
                    dto.setFactory(product.getFactory());

                    double priceScore = product.getPrice() < 10.0 ? 3.0 : (product.getPrice() < 20.0 ? 2.0 : 0.0);
                    double quantityScore = product.getQuantity() >= 50 ? 3.0 : (product.getQuantity() >= 10 ? 2.0 : 0.0);
                    double factoryScore = getFactoryScore(product.getFactory());
                    double totalScore = (priceScore * 2) + (quantityScore * 2) + (factoryScore * 1);
                    dto.setScore(totalScore);

                    return dto;
                })
                .sorted((p1, p2) -> Double.compare(p2.getScore(), p1.getScore()))
                .limit(8)
                .collect(Collectors.toList());

        recommendations.forEach(dto -> {
            Recommendation recommendation = new Recommendation();
            recommendation.setProductId(dto.getId());
            recommendation.setProductName(dto.getName());
            recommendation.setPrice(dto.getPrice());
            recommendation.setImage(dto.getImage());
            recommendation.setDescription(dto.getDescription());
            recommendation.setQuantity(dto.getQuantity());
            recommendation.setFactory(dto.getFactory());
            recommendation.setScore(dto.getScore());
            recommendation.setCreatedAt(LocalDateTime.now());
            recommendationRepository.save(recommendation);
        });

        return recommendations;
    }

    public List<ProductSalesDTO> getProductSales() {
        List<Object[]> results = orderDetailRepository.findProductSales();
        return results.stream()
                .map(result -> new ProductSalesDTO(
                        ((Number) result[0]).longValue(),
                        (String) result[1],
                        ((Number) result[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public User getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token is missing or invalid format");
        }

        String jwtToken = token.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("user")) {
                    Map<String, Object> userInfo = (Map<String, Object>) responseBody.get("user");

                    User user = new User();
                    user.setUsername((String) userInfo.get("username"));
                    user.setRole((String) userInfo.get("role"));
                    Integer userId = (Integer) userInfo.get("id");
                    user.setId(userId != null ? Long.valueOf(userId) : null);
                    return user;
                }
            }
            throw new RuntimeException("Failed to get user information from AuthService");
        } catch (Exception e) {
            throw new RuntimeException("Error calling AuthService: " + e.getMessage());
        }
    }
}