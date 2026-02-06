package com.example.users.repositories.Users;

import com.example.users.model.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<Object> findByOtpCode(String otpCode);

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<Object> findByPhoneNumber(String phoneNumber);

    List<User> findByArchivedFalse();

    List<User> findByArchivedTrue();
}
