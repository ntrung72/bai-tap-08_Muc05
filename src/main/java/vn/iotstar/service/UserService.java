package vn.iotstar.service;

import java.util.List;
import vn.iotstar.model.User;

public interface UserService {
    List<User> findAll(String keyword);
    User findById(Integer id);
    User save(User user);
    void deleteById(Integer id);
    boolean userNameExists(String userName, Integer excludedId);
    boolean emailExists(String email, Integer excludedId);
}
