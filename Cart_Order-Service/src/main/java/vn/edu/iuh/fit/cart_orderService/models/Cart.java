package vn.edu.iuh.fit.cart_orderService.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int sum;

    @JoinColumn(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER,  orphanRemoval = true)
    private List<CartDetail> cartDetails;

    public Cart() {
    }

    public Cart(long id, int sum, Long userId, List<CartDetail> cartDetails) {
        this.id = id;
        this.sum = sum;
        this.userId = userId;
        this.cartDetails = cartDetails;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartDetail> getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(List<CartDetail> cartDetails) {
        this.cartDetails = cartDetails;
    }
}
