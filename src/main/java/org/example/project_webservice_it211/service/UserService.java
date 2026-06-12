package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.CreateUserRequest;
import org.example.project_webservice_it211.dto.response.UserResponse;
import org.example.project_webservice_it211.entity.User;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setRole(req.getRole() != null ? req.getRole() : "CUSTOMER");
        user.setIsEnabled(true);

        return UserResponse.from(userRepository.save(user));
    }


    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }


    public List<UserResponse> search(String keyword) {
        return userRepository.searchByFullName(keyword)
                .stream()
                .map(UserResponse::from)
                .toList();
    }


    public UserResponse update(Long id, UserResponse req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setRole(req.getRole());
        user.setIsEnabled(req.getIsEnabled());

        return UserResponse.from(userRepository.save(user));
    }


    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User không tồn tại");
        }
        userRepository.deleteById(id);
    }
}
