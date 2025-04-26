package vn.edu.iuh.fit.cart_orderService.dto;

public class AddToCartRequest {
    private Long productId;
    private Integer quantity;

    // Getters & Setters

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
