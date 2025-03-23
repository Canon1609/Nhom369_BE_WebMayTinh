package fit.iuh.repositories;


import fit.iuh.models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, String> {
    List<UserActivity> findByUserId(String userId);
    List<UserActivity> findByLaptopId(String laptopId);
}

