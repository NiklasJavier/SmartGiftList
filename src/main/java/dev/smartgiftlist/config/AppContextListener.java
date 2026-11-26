package dev.smartgiftlist.config;

import com.zaxxer.hikari.HikariDataSource;
import dev.smartgiftlist.repository.CommentRepository;
import dev.smartgiftlist.repository.GiftRepository;
import dev.smartgiftlist.repository.JdbcCommentRepository;
import dev.smartgiftlist.repository.JdbcGiftRepository;
import dev.smartgiftlist.repository.JdbcUserRepository;
import dev.smartgiftlist.repository.JdbcWishlistRepository;
import dev.smartgiftlist.repository.UserRepository;
import dev.smartgiftlist.repository.WishlistRepository;
import dev.smartgiftlist.service.AuthService;
import dev.smartgiftlist.service.CommentService;
import dev.smartgiftlist.service.GiftService;
import dev.smartgiftlist.service.WishlistService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps database pooling, schema assurance, repositories, and services on startup.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppContextListener.class);

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        this.dataSource = DataSourceFactory.create(config);
        SchemaInitializer.ensureSchema(this.dataSource, config.getSchema());

        UserRepository userRepo = new JdbcUserRepository(dataSource);
        WishlistRepository wishlistRepo = new JdbcWishlistRepository(dataSource);
        GiftRepository giftRepo = new JdbcGiftRepository(dataSource);
        CommentRepository commentRepo = new JdbcCommentRepository(dataSource);

        AuthService authService = new AuthService(userRepo);
        WishlistService wishlistService = new WishlistService(wishlistRepo, giftRepo, commentRepo);
        GiftService giftService = new GiftService(giftRepo, wishlistRepo);
        CommentService commentService = new CommentService(commentRepo, wishlistRepo);

        ServiceRegistry registry = new ServiceRegistry(
                config, dataSource, authService, wishlistService, giftService, commentService
        );
        sce.getServletContext().setAttribute(ServiceRegistry.ATTR_NAME, registry);
        LOG.info("SmartGiftList application context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (this.dataSource != null) {
            this.dataSource.close();
            LOG.info("Closed SmartGiftList HikariCP DataSource");
        }
    }
}
