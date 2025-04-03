package vn.edu.iuh.fit.cart_orderService.services.impl;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.cart_orderService.models.*;
import vn.edu.iuh.fit.cart_orderService.repositories.*;
import vn.edu.iuh.fit.cart_orderService.services.IService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShippingAddressRepository shippingAddressRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, UserRepository userRepository, CartRepository cartRepository, CartDetailRepository cartDetailRepository, PaymentMethodRepository paymentMethodRepository, ShippingAddressRepository shippingAddressRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.shippingAddressRepository = shippingAddressRepository;
    }

   public Order handlePlaceOrder(Long userId, Long addressId, Long paymentMethodId) {
       Optional<User> user = userRepository.findById(userId);
       Cart cart = cartRepository.findByUser(user);
       List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);
       PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId).get();
       ShippingAddress shippingAddress = shippingAddressRepository.findById(addressId).get();

       // create Order

       Order order = new Order();
       order.setCreateAt(LocalDate.now());
       order.setUser(user.get());
       order.setStatus("PENDING");
       order.setPaymentMethod(paymentMethod);
       order.setShippingAddress(shippingAddress);
       double sum = 0;
       for (CartDetail cartDetail : cartDetails) {
          sum += cartDetail.getPrice() * cartDetail.getQuantity();
       }
       order.setTotalPrice(sum);
       order = orderRepository.save(order);

       // create OrderDetail
       List<OrderDetail> orderDetails = new ArrayList<>();
       for (CartDetail cartDetail : cartDetails) {
              OrderDetail orderDetail = new OrderDetail();
              orderDetail.setOrder(order);
              orderDetail.setProduct(cartDetail.getProduct());
              orderDetail.setQuantity(cartDetail.getQuantity());
              orderDetail.setPrice(cartDetail.getPrice());
              orderDetails.add(orderDetail);
              orderDetailRepository.save(orderDetail);
       }
       order.setOrderDetails(orderDetails);

       // delete cartDetail

//         for (CartDetail cartDetail : cartDetails) {
//             cartDetailRepository.delete(cartDetail);
//             cart.getCartDetails().remove(cartDetail);
//         }
//       for (CartDetail cartDetail : cartDetails) {
//           cart.getCartDetails().remove(cartDetail); // orphanRemoval sẽ lo
//       }

       for (CartDetail cartDetail : cartDetails) {
           cart.getCartDetails().remove(cartDetail); // Xóa khỏi list để tránh tham chiếu
           cartDetailRepository.delete(cartDetail); // Xóa tay
       }

       // delete cart
//       cartRepository.delete(cart);
       cart.getUser().setCart(null);
       cart.setUser(null);
       cart.getCartDetails().clear(); // xóa tất cả
       cartRepository.delete(cart);
       return order;
    }


    public Order handleUpdateStatus(Long orderId, String status) {
        Optional<Order> order = orderRepository.findById(orderId);
        order.get().setStatus(status);
        return orderRepository.save(order.get());
    }


}
