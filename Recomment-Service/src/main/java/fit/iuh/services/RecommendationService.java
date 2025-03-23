package fit.iuh.services;


import fit.iuh.models.Product;
import fit.iuh.models.UserActivity;
import fit.iuh.repositories.ProductRepository;
import fit.iuh.repositories.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService
{
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserActivityRepository userActivityRepository;

    public List<Product> recommendLaptops(String userId) {
        List<UserActivity> activities = userActivityRepository.findByUserId(userId);
        Set<String> preferredCategories = new HashSet<>();
        Set<String> viewedLaptops = new HashSet<>();

        for (UserActivity activity : activities) {
            Product product =productRepository.findById(activity.getLaptopId()).orElse(null);

            if (product != null) {
                preferredCategories.add(product.getCategoryId());
                viewedLaptops.add(product.getId());
            }
        }

        // Tìm laptop mà user khác cũng xem sau khi xem sản phẩm này
        Set<String> relatedLaptops = new HashSet<>();
        for (String laptopId : viewedLaptops) {
            List<UserActivity> relatedActivities = userActivityRepository.findByLaptopId(laptopId);
            for (UserActivity act : relatedActivities) {
                if (!act.getUserId().equals(userId)) {
                    relatedLaptops.add(act.getLaptopId());
                }
            }
        }

        // Đề xuất sản phẩm từ danh mục user quan tâm
        List<Product> recommendedLaptops = new ArrayList<>();
        for (String categoryId : preferredCategories) {
            recommendedLaptops.addAll(productRepository.findByCategoryId(categoryId));
        }

        return recommendedLaptops.stream()
                .filter(laptop -> !viewedLaptops.contains(laptop.getId())) // Loại bỏ sản phẩm đã xem
                .collect(Collectors.toList());
    }

    public UserActivity saveActivity(UserActivity userActivity) {
        return userActivityRepository.save(userActivity);
    }
}
