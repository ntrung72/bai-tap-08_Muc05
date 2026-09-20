package vn.iotstar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User>
            findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingOrderByIdDesc(
            String userName, String fullName, String email, String phone);
    List<User> findAllByOrderByIdDesc();
    boolean existsByUserNameIgnoreCase(String userName);
    boolean existsByUserNameIgnoreCaseAndIdNot(String userName, Integer id);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
}