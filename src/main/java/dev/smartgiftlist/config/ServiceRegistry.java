package dev.smartgiftlist.config;

import javax.sql.DataSource;

import dev.smartgiftlist.service.AuthService;
import dev.smartgiftlist.service.CommentService;
import dev.smartgiftlist.service.GiftService;
import dev.smartgiftlist.service.WishlistService;
import jakarta.servlet.ServletContext;

/**
 * Application-scoped dependency container stored in {@link ServletContext}.
 */
public final class ServiceRegistry {

    public static final String ATTR_NAME = "smartgiftlist.services";

    private final DatabaseConfig databaseConfig;
    private final DataSource dataSource;
    private final AuthService authService;
    private final WishlistService wishlistService;
    private final GiftService giftService;
    private final CommentService commentService;

    public ServiceRegistry(
            DatabaseConfig databaseConfig,
            DataSource dataSource,
            AuthService authService,
            WishlistService wishlistService,
            GiftService giftService,
            CommentService commentService
    ) {
        this.databaseConfig = databaseConfig;
        this.dataSource = dataSource;
        this.authService = authService;
        this.wishlistService = wishlistService;
        this.giftService = giftService;
        this.commentService = commentService;
    }

    public static ServiceRegistry from(ServletContext context) {
        Object attr = context.getAttribute(ATTR_NAME);
        if (!(attr instanceof ServiceRegistry registry)) {
            throw new IllegalStateException("ServiceRegistry has not been initialized in ServletContext");
        }
        return registry;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public WishlistService getWishlistService() {
        return wishlistService;
    }

    public GiftService getGiftService() {
        return giftService;
    }

    public CommentService getCommentService() {
        return commentService;
    }
}
