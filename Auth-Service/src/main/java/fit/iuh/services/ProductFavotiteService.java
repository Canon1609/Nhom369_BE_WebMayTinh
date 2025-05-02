package fit.iuh.services;

import fit.iuh.models.ProductFavorite;
import fit.iuh.repositories.ProductFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductFavotiteService {

    @Autowired
    private ProductFavoriteRepository productFavoriteRepository;
    @Autowired
    private UserService userService;



    // Phương thức để lưu sản phẩm yêu thích
    public ProductFavorite saveFavoriteProduct(ProductFavorite favoriteProduct) {
        return productFavoriteRepository.save(favoriteProduct);
    }


}
