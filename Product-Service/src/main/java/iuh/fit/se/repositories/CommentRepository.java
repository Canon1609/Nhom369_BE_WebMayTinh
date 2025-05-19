package iuh.fit.se.repositories;

import iuh.fit.se.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c where c.product.id = ?1")
    List<Comment> findByProduct_Id(long id);
}
