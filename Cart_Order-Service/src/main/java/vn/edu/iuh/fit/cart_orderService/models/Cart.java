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
//    private double totalPrice;
//    private String status;
//    private LocalDate createAt;
    private int sum;
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER,  orphanRemoval = true)
    private List<CartDetail> cartDetails;

    public Cart(long id, int sum, User user, List<CartDetail> cartDetails) {
        this.id = id;
        this.sum = sum;
        this.user = user;
        this.cartDetails = cartDetails;
    }

    public Cart() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartDetail> getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(List<CartDetail> cartDetails) {
        this.cartDetails = cartDetails;
    }
}
