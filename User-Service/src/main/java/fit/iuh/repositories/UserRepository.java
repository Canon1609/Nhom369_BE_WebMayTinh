package fit.iuh.repositories;

import fit.iuh.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User getUsersByEmail(String email);
    User findByUsername(String username);
}
