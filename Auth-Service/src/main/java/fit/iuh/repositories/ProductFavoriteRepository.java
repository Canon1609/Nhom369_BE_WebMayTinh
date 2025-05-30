package fit.iuh.repositories;

import fit.iuh.models.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
}
