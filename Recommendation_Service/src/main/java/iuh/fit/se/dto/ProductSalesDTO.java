package iuh.fit.se.dto;

import java.util.Objects;

public class ProductSalesDTO {
    private Long productId;
    private String productName;
    private Long totalSold;

    // Constructor không tham số
    public ProductSalesDTO() {
    }

    // Constructor đầy đủ tham số
    public ProductSalesDTO(Long productId, String productName, Long totalSold) {
        this.productId = productId;
        this.productName = productName;
        this.totalSold = totalSold;
    }

    // Getter và Setter
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(Long totalSold) {
        this.totalSold = totalSold;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductSalesDTO that = (ProductSalesDTO) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(totalSold, that.totalSold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, totalSold);
    }
}