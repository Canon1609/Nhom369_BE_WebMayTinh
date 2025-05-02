package vn.edu.iuh.fit.cart_orderService.dto;

import vn.edu.iuh.fit.cart_orderService.models.Product;

import java.util.List;

public class CreateOderRequest {
    private List<Product> products;
    private Long paymentMethodId;
    private String shippingAddress;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
