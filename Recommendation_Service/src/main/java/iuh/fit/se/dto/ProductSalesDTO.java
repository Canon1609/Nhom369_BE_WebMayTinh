package iuh.fit.se.dto;

import java.util.Objects;

public class ProductSalesDTO {
    private Long id;
    private String name;
    private Long totalSold;
    private String image;
    private Double price;
    private Double originalPrice;
    private Double rating;
    private Integer reviews;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTotalSold() { return totalSold; }
    public void setTotalSold(Long totalSold) { this.totalSold = totalSold; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviews() { return reviews; }
    public void setReviews(Integer reviews) { this.reviews = reviews; }
}