package vn.iotstar.service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.model.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;
import vn.iotstar.util.TextEncodingUtils;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<User> findAll(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        List<User> users;

        if (value.isEmpty()) {
            users = repository.findAllByOrderByIdDesc();
        } else {
            users = repository
                    .findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingOrderByIdDesc(
                            value,
                            value,
                            value,
                            value);
        }
        users.forEach(this::normalizeText);
        return users;
    }

    @Override
    public User findById(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng có mã " + id + "."));
        normalizeText(user);
        return user;
    }

    @Override
    @Transactional
    public User save(User user) {
        user.setEmail(TextEncodingUtils.normalize(user.getEmail()).trim());
        user.setUserName(TextEncodingUtils.normalize(user.getUserName()).trim());
        user.setFullName(TextEncodingUtils.normalize(user.getFullName()).trim());
        user.setPhone(TextEncodingUtils.normalize(user.getPhone()).trim());
        if (user.getAvatar() != null) {
            user.setAvatar(user.getAvatar().trim());
        }
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(LocalDate.now());
        }
        return repository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id);
        repository.deleteById(id);
        repository.flush();
    }

    @Override
    public boolean userNameExists(String userName, Integer excludedId) {
        if (userName == null || userName.isBlank()) {
            return false;
        }

        if (excludedId == null) {
            return repository.existsByUserNameIgnoreCase(userName.trim());
        }

        return repository.existsByUserNameIgnoreCaseAndIdNot(
                userName.trim(),
                excludedId);
    }

    @Override
    public boolean emailExists(String email, Integer excludedId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (excludedId == null) {
            return repository.existsByEmailIgnoreCase(email.trim());
        }

        return repository.existsByEmailIgnoreCaseAndIdNot(
                email.trim(),
                excludedId);
    }

    private void normalizeText(User user) {
        user.setUserName(TextEncodingUtils.normalize(user.getUserName()));
        user.setFullName(TextEncodingUtils.normalize(user.getFullName()));
        user.setEmail(TextEncodingUtils.normalize(user.getEmail()));
        user.setPhone(TextEncodingUtils.normalize(user.getPhone()));
    }
}
