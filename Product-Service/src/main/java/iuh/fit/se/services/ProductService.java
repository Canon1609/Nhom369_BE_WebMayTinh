package iuh.fit.se.services;

import iuh.fit.se.models.Comment;
import iuh.fit.se.models.Product;
import iuh.fit.se.repositories.CommentRepository;
import iuh.fit.se.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProductById(long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> searchProductsByKeyword(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Comment addComment(Long userId, String userName, int rating, String comment, long productId) {
        Comment newComment = new Comment();
        newComment.setUserId(userId);
        newComment.setUserName(userName);
        newComment.setRating(rating);
        newComment.setComment(comment);
        newComment.setCreatedAt(LocalDateTime.now());
        Product product = productRepository.findById(productId).get();
        newComment.setProduct(product);
        return commentRepository.save(newComment);

    }

    public List<Comment> getCommentsByProductId(long productId) {
        return commentRepository.findByProduct_Id(productId);
    }



}