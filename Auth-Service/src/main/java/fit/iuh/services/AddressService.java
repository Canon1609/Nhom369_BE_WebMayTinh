package fit.iuh.services;

import fit.iuh.models.Address;
import fit.iuh.models.User;
import fit.iuh.repositories.AddressRepository;
import fit.iuh.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Address> getAddressesByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return addressRepository.findByUserId(userId);
        }
        return null;
    }

    // Phương thức để lưu địa chỉ mới
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    // Phương thức lấy địa chỉ theo ID
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }

    // Phương thức để xóa địa chỉ
    public void deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
    }


}
