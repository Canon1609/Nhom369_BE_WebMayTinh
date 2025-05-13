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

    @Value("${order.service.url:http://localhost:8085/api/v1/order}")
    private String orderServiceUrl;
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

        // Lấy top 10 sản phẩm bán chạy
        List<ProductSalesDTO> topSellingProducts = getProductSales();
        Set<Long> topSellingProductIds = topSellingProducts.stream()
                .map(ProductSalesDTO::getId)
                .collect(Collectors.toSet());

        // Chọn ngẫu nhiên 1 sản phẩm để cộng thêm 8 điểm
        Random random = new Random();
        Product randomProduct = products.get(random.nextInt(products.size()));
        Long randomProductId = randomProduct.getId();

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

                    // Tính điểm cơ bản
                    double priceScore = product.getPrice() < 10.0 ? 3.0 : (product.getPrice() < 20.0 ? 2.0 : 0.0);
                    double quantityScore = product.getQuantity() >= 50 ? 3.0 : (product.getQuantity() >= 10 ? 2.0 : 0.0);
                    double factoryScore = getFactoryScore(product.getFactory());
                    double totalScore = (priceScore * 2) + (quantityScore * 2) + (factoryScore * 1);

                    // Cộng điểm nếu sản phẩm nằm trong top 10 bán chạy
                    if (topSellingProductIds.contains(product.getId())) {
                        totalScore += 4.0; // +4 điểm cho top 10
                    }

                    // Cộng điểm ngẫu nhiên cho 1 sản phẩm
                    if (product.getId().equals(randomProductId)) {
                        totalScore += 5.0; // +8 điểm cho sản phẩm ngẫu nhiên
                    }

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
        try {
            // Gọi endpoint /getProductSold từ Cart_order_service
            ResponseEntity<Map> response = restTemplate.exchange(
                    orderServiceUrl + "/getProductSold",
                    HttpMethod.GET,
                    null,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to fetch product sales from Order Service");
            }

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> productsSold = (List<Map<String, Object>>) responseBody.get("products");

            if (productsSold == null || productsSold.isEmpty()) {
                return Collections.emptyList();
            }

            // Lấy chi tiết sản phẩm từ Product Service
            List<ProductSalesDTO> result = new ArrayList<>();
            for (Map<String, Object> productSold : productsSold) {
                Long productId = ((Number) productSold.get("id")).longValue();
                // Gọi API chi tiết sản phẩm
                Product product = restTemplate.getForObject(productServiceUrl + "/" + productId, Product.class);
                if (product != null) {
                    ProductSalesDTO dto = new ProductSalesDTO();
                    dto.setId(productId);
                    dto.setName((String) productSold.get("name"));
                    dto.setTotalSold(((Number) productSold.get("totalSold")).longValue());
                    dto.setImage(product.getImage() != null ? product.getImage() : "https://via.placeholder.com/150"); // Hình ảnh mặc định nếu không có
                    dto.setPrice(product.getPrice());
                    dto.setOriginalPrice(product.getPrice() * 1.2); // Giả sử giá gốc cao hơn 20%
                    dto.setRating(4.5); // Giá trị giả lập, thay bằng dữ liệu thực nếu có
                    dto.setReviews(65); // Giá trị giả lập
                    result.add(dto);
                }
            }

            // Sắp xếp và giới hạn top 10
            return result.stream()
                    .sorted((p1, p2) -> Long.compare(p2.getTotalSold(), p1.getTotalSold()))
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching product sales: " + e.getMessage());
        }
    }
}