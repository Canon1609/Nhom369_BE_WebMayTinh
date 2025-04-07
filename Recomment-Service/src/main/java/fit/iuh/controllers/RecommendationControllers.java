package fit.iuh.controllers;

import fit.iuh.models.Product;
import fit.iuh.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationControllers {
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public List<Product> getRecommendations(
            @RequestParam(required = false) String useCase,
            @RequestParam(required = false) Double budget,
            @RequestParam(required = false) String preference,
            @RequestParam(required = false) List<Long> viewedProductIds) {
        return recommendationService.getPersonalizedRecommendations(useCase, budget, preference, viewedProductIds);
    }
}
