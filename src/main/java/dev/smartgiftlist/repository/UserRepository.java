package dev.smartgiftlist.repository;

import java.time.LocalDate;
import java.util.Optional;

import dev.smartgiftlist.model.User;

/**
 * Persistence port for SmartGiftList user accounts.
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(int userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameExcludingUser(String username, int excludedUserId);

    User create(String username, String email, String passwordHash, LocalDate birthday, String icon);

    boolean updateProfile(int userId, String username, String passwordHashOrNull, String icon);

    boolean deleteById(int userId);
}
