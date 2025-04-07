package fit.iuh.services;


import fit.iuh.models.Product;

import fit.iuh.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getPersonalizedRecommendations(
            String useCase, Double budget, String userPreferences, List<Long> viewedProductIds) {

        // Lấy tất cả sản phẩm từ repository
        List<Product> allProducts = productRepository.findAll();

        // Lấy danh sách sản phẩm đã xem (nếu có)
        List<Product> viewedProducts = viewedProductIds != null && !viewedProductIds.isEmpty()
                ? allProducts.stream()
                .filter(p -> viewedProductIds.contains(p.getId()))
                .collect(Collectors.toList())
                : Collections.emptyList();

        // Tính điểm phù hợp
        Map<Product, Double> productScores = new HashMap<>();
        for (Product product : allProducts) {
            double score = 0.0;

            if (useCase != null && !useCase.isEmpty() && product.getUseCase().equalsIgnoreCase(useCase)) {
                score += 0.4;
            }
            if (budget != null && product.getPrice() <= budget) {
                score += 0.3;
            }
            if (userPreferences != null && !userPreferences.isEmpty() &&
                    product.getPerformance().equalsIgnoreCase(userPreferences)) {
                score += 0.2;
            }
            if (!viewedProducts.isEmpty()) {
                for (Product viewed : viewedProducts) {
                    if (viewed.getUseCase().equals(product.getUseCase()) && !viewed.getId().equals(product.getId())) {
                        score += 0.1;
                        break;
                    }
                }
            }
            score += Math.random() * 0.2; // Giả lập popularity

            if (score > 0) {
                productScores.put(product, score);
            }
        }

        // Sắp xếp và giới hạn 5 sản phẩm
        return productScores.entrySet().stream()
                .sorted(Map.Entry.<Product, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }
}
