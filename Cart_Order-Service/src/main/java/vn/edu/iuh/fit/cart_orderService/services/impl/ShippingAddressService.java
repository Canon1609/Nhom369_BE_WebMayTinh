package vn.edu.iuh.fit.cart_orderService.services.impl;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.cart_orderService.models.ShippingAddress;
import vn.edu.iuh.fit.cart_orderService.models.User;
import vn.edu.iuh.fit.cart_orderService.repositories.ShippingAddressRepository;
import vn.edu.iuh.fit.cart_orderService.repositories.UserRepository;

import java.util.List;

@Service
public class ShippingAddressService {

    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;

    public ShippingAddressService(UserRepository userRepository, ShippingAddressRepository shippingAddressRepository) {
        this.userRepository = userRepository;
        this.shippingAddressRepository = shippingAddressRepository;
    }

    public ShippingAddress handleAddShippingAddress(Long userId, String address, String phone, String name) {
        User user = userRepository.findById(userId).get();
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setUser(user);
        shippingAddress.setAddress(address);
        shippingAddress.setPhone(phone);
        shippingAddress.setName(name);
        shippingAddress = shippingAddressRepository.save(shippingAddress);
        return shippingAddress;
    }

    public ShippingAddress handleUpdateShippingAddress(Long addressId, String address, String phone, String name) {
        ShippingAddress shippingAddress = shippingAddressRepository.findById(addressId).get();
        shippingAddress.setAddress(address);
        shippingAddress.setPhone(phone);
        shippingAddress.setName(name);
        shippingAddress = shippingAddressRepository.save(shippingAddress);
        return shippingAddress;
    }

    public void handleDeleteShippingAddress(Long addressId) {
        shippingAddressRepository.deleteById(addressId);
    }

    public List<ShippingAddress> handleGetShippingAddressByUserId(Long userId) {
        User user = userRepository.findById(userId).get();
        if(user != null){
            return shippingAddressRepository.findByUser(user);
        }
        return null;
    }
}
