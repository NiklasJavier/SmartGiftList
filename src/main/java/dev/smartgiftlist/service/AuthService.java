package dev.smartgiftlist.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import dev.smartgiftlist.model.FlashMessage;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.repository.UserRepository;
import dev.smartgiftlist.security.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Account registration, credential verification, and profile management service.
 */
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 200;

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ServiceResult<User> register(
            String rawUsername,
            String rawEmail,
            String rawBirthday,
            String rawPassword,
            String rawConfirmPassword
    ) {
        String username = rawUsername != null ? rawUsername.trim() : "";
        String email = rawEmail != null ? rawEmail.trim() : "";
        String password = rawPassword != null ? rawPassword : "";
        String confirmPassword = rawConfirmPassword != null ? rawConfirmPassword : "";

        if (username.isEmpty() || username.length() > MAX_USERNAME_LENGTH) {
            return ServiceResult.failure(FlashMessage.error(
                    "Invalid username.",
                    "Please enter a username between 1 and " + MAX_USERNAME_LENGTH + " characters."
            ));
        }
        if (email.isEmpty() || email.length() > MAX_EMAIL_LENGTH || !email.contains("@")) {
            return ServiceResult.failure(FlashMessage.error(
                    "Invalid email address.",
                    "Please enter a valid email address."
            ));
        }
        if (password.length() < MIN_PASSWORD_LENGTH || !password.equals(confirmPassword)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Passwords do not match.",
                    "Please enter identical passwords of at least " + MIN_PASSWORD_LENGTH + " characters."
            ));
        }

        LocalDate birthday;
        try {
            birthday = parseOptionalDate(rawBirthday);
        } catch (DateTimeParseException ex) {
            return ServiceResult.failure(FlashMessage.error(
                    "Invalid date of birth.",
                    "Please provide your date of birth in YYYY-MM-DD format."
            ));
        }

        if (userRepository.existsByUsername(username)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Username '" + username + "' is already taken.",
                    "Please choose a different username."
            ));
        }
        if (userRepository.existsByEmail(email)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Email address '" + email + "' is already registered.",
                    "Please use a different email address."
            ));
        }

        String hash = PasswordUtil.hashPassword(password);
        User created = userRepository.create(username, email, hash, birthday, IconCatalog.DEFAULT_AVATAR_KEY);
        LOG.info("Registered account userId={}", created.getId());
        return ServiceResult.success(
                created,
                FlashMessage.info(
                        "Welcome, " + created.getUsername() + ".",
                        "Your account has been created. Sign in below to continue."
                )
        );
    }

    public ServiceResult<User> authenticate(String rawUsername, String rawPassword) {
        String username = rawUsername != null ? rawUsername.trim() : "";
        String password = rawPassword != null ? rawPassword : "";

        if (username.isEmpty() || password.isEmpty()) {
            return ServiceResult.failure(FlashMessage.error(
                    "Sign-in failed.",
                    "Please check your username and password and try again."
            ));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !PasswordUtil.verifyPassword(password, userOpt.get().getPasswordHash())) {
            return ServiceResult.failure(FlashMessage.error(
                    "Sign-in failed.",
                    "Please check your username and password and try again."
            ));
        }

        User user = userOpt.get();
        return ServiceResult.success(
                user,
                FlashMessage.banner("You have signed in successfully, " + user.getUsername() + ".")
        );
    }

    public Optional<User> getUserById(int userId) {
        if (userId <= 0) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public ServiceResult<User> updateProfile(
            int userId,
            String rawUsername,
            String rawPassword,
            String rawConfirmPassword,
            String rawAvatarIcon
    ) {
        String username = rawUsername != null ? rawUsername.trim() : "";
        String password = rawPassword != null ? rawPassword : "";
        String confirmPassword = rawConfirmPassword != null ? rawConfirmPassword : "";

        if (username.isEmpty() || username.length() > MAX_USERNAME_LENGTH) {
            return ServiceResult.failure(FlashMessage.error(
                    "Invalid username.",
                    "Please enter a username between 1 and " + MAX_USERNAME_LENGTH + " characters."
            ));
        }

        if (userRepository.existsByUsernameExcludingUser(username, userId)) {
            return ServiceResult.failure(FlashMessage.error(
                    "Username unavailable.",
                    "This username is already taken. Please choose another name."
            ));
        }

        String newHash = null;
        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            if (password.length() < MIN_PASSWORD_LENGTH || !password.equals(confirmPassword)) {
                return ServiceResult.failure(FlashMessage.error(
                        "Invalid password.",
                        "New passwords must match and contain at least " + MIN_PASSWORD_LENGTH + " characters."
                ));
            }
            newHash = PasswordUtil.hashPassword(password);
        }

        String normalizedAvatar = IconCatalog.normalizeAvatarKey(rawAvatarIcon);
        boolean updated = userRepository.updateProfile(userId, username, newHash, normalizedAvatar);
        if (!updated) {
            return ServiceResult.failure(FlashMessage.error(
                    "Profile update failed.",
                    "Your account record could not be updated."
            ));
        }

        User reloaded = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Updated user missing for ID " + userId));
        LOG.info("Updated profile for userId={}", userId);
        return ServiceResult.success(reloaded, FlashMessage.banner("Profile updated."));
    }

    public ServiceResult<Boolean> deleteAccount(int userId) {
        boolean deleted = userRepository.deleteById(userId);
        if (!deleted) {
            return ServiceResult.failure(FlashMessage.error(
                    "Account deletion failed.",
                    "Your account could not be deleted due to an error."
            ));
        }
        LOG.info("Deleted account userId={}", userId);
        return ServiceResult.success(
                true,
                FlashMessage.banner("Your account has been deleted. Create a new account at any time.")
        );
    }

    private static LocalDate parseOptionalDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(rawDate.trim());
    }
}
