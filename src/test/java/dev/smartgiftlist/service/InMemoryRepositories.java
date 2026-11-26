package dev.smartgiftlist.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import dev.smartgiftlist.model.ChatComment;
import dev.smartgiftlist.model.CommentType;
import dev.smartgiftlist.model.Gift;
import dev.smartgiftlist.model.GiftClaimer;
import dev.smartgiftlist.model.IconCatalog;
import dev.smartgiftlist.model.User;
import dev.smartgiftlist.model.Wishlist;
import dev.smartgiftlist.model.WishlistParticipant;
import dev.smartgiftlist.repository.CommentRepository;
import dev.smartgiftlist.repository.GiftRepository;
import dev.smartgiftlist.repository.UserRepository;
import dev.smartgiftlist.repository.WishlistRepository;

/**
 * Shared in-memory store exposing typed test doubles for all four repositories.
 */
final class InMemoryRepositories {

    private final AtomicInteger seq = new AtomicInteger(1);
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private final Map<Integer, Wishlist> wishlists = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> wishlistMembers = new LinkedHashMap<>();
    private final Map<Integer, Gift> gifts = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> giftClaimers = new LinkedHashMap<>();
    private final List<ChatComment> comments = new ArrayList<>();

    private final UserRepository userRepo = new UserRepository() {
        @Override
        public Optional<User> findByUsername(String username) {
            return users.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }

        @Override
        public Optional<User> findById(int userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username).isPresent();
        }

        @Override
        public boolean existsByEmail(String email) {
            return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
        }

        @Override
        public boolean existsByUsernameExcludingUser(String username, int excludedUserId) {
            return users.values().stream()
                    .anyMatch(u -> u.getUsername().equals(username) && u.getId() != excludedUserId);
        }

        @Override
        public User create(String username, String email, String passwordHash, LocalDate birthday, String icon) {
            int id = seq.getAndIncrement();
            User u = new User(id, username, email, passwordHash, birthday, false, icon, LocalDateTime.now());
            users.put(id, u);
            return u;
        }

        @Override
        public boolean updateProfile(int userId, String username, String passwordHashOrNull, String icon) {
            User current = users.get(userId);
            if (current == null) {
                return false;
            }
            String nextHash = passwordHashOrNull != null ? passwordHashOrNull : current.getPasswordHash();
            users.put(userId, new User(
                    userId, username, current.getEmail(), nextHash, current.getBirthday(),
                    current.isAdmin(), icon, current.getJoinDate()
            ));
            return true;
        }

        @Override
        public boolean deleteById(int userId) {
            if (users.remove(userId) == null) {
                return false;
            }
            wishlists.values().removeIf(w -> w.getOwnerUserId() == userId);
            wishlistMembers.values().forEach(set -> set.remove(userId));
            giftClaimers.values().forEach(set -> set.remove(userId));
            return true;
        }
    };

    private final WishlistRepository wishlistRepo = new WishlistRepository() {
        @Override
        public Wishlist create(int ownerUserId, String netId, String title, String event,
                               LocalDate eventDate, String description, String url, String icon) {
            int id = seq.getAndIncrement();
            Wishlist w = new Wishlist(id, netId, title, event, eventDate, description, url, icon, ownerUserId, LocalDateTime.now());
            wishlists.put(id, w);
            wishlistMembers.put(id, new LinkedHashSet<>());
            return w;
        }

        @Override
        public Optional<Wishlist> findById(int wishlistId) {
            return Optional.ofNullable(wishlists.get(wishlistId));
        }

        @Override
        public Optional<Wishlist> findByNetId(String netId) {
            return wishlists.values().stream().filter(w -> w.getNetId().equals(netId)).findFirst();
        }

        @Override
        public List<Wishlist> findOwnedByUser(int userId) {
            return wishlists.values().stream().filter(w -> w.getOwnerUserId() == userId).toList();
        }

        @Override
        public List<Wishlist> findConnectedByUser(int userId) {
            return wishlists.values().stream()
                    .filter(w -> wishlistMembers.getOrDefault(w.getId(), Set.of()).contains(userId))
                    .toList();
        }

        @Override
        public Optional<Wishlist> findLatestAccessibleByUser(int userId) {
            List<Wishlist> accessible = wishlists.values().stream()
                    .filter(w -> isAccessibleByUser(w.getId(), userId))
                    .toList();
            return accessible.isEmpty() ? Optional.empty() : Optional.of(accessible.get(accessible.size() - 1));
        }

        @Override
        public boolean updateDetails(int wishlistId, String url, String event, String description, String icon, LocalDate eventDate) {
            Wishlist existing = wishlists.get(wishlistId);
            if (existing == null) {
                return false;
            }
            wishlists.put(wishlistId, new Wishlist(
                    wishlistId, existing.getNetId(), existing.getTitle(), event, eventDate,
                    description, url, icon, existing.getOwnerUserId(), existing.getCreateDate()
            ));
            return true;
        }

        @Override
        public boolean deleteById(int wishlistId) {
            if (wishlists.remove(wishlistId) == null) {
                return false;
            }
            wishlistMembers.remove(wishlistId);
            gifts.values().removeIf(g -> g.getWishlistId() == wishlistId);
            comments.removeIf(c -> c.getWishlistId() == wishlistId);
            return true;
        }

        @Override
        public boolean isOwnedByUser(int wishlistId, int userId) {
            Wishlist w = wishlists.get(wishlistId);
            return w != null && w.getOwnerUserId() == userId;
        }

        @Override
        public boolean isAccessibleByUser(int wishlistId, int userId) {
            return isOwnedByUser(wishlistId, userId)
                    || wishlistMembers.getOrDefault(wishlistId, Set.of()).contains(userId);
        }

        @Override
        public boolean addParticipant(int wishlistId, int userId) {
            return wishlistMembers.computeIfAbsent(wishlistId, k -> new LinkedHashSet<>()).add(userId);
        }

        @Override
        public boolean removeParticipant(int wishlistId, int userId) {
            return wishlistMembers.getOrDefault(wishlistId, Set.of()).remove(userId);
        }

        @Override
        public List<WishlistParticipant> findParticipants(int wishlistId) {
            Wishlist w = wishlists.get(wishlistId);
            if (w == null) {
                return List.of();
            }
            List<WishlistParticipant> list = new ArrayList<>();
            User owner = users.get(w.getOwnerUserId());
            if (owner != null) {
                list.add(new WishlistParticipant(owner.getId(), owner.getUsername(), owner.getIcon(), true));
            }
            for (Integer memberId : wishlistMembers.getOrDefault(wishlistId, Set.of())) {
                User member = users.get(memberId);
                if (member != null && member.getId() != w.getOwnerUserId()) {
                    list.add(new WishlistParticipant(member.getId(), member.getUsername(), member.getIcon(), false));
                }
            }
            return list;
        }

        @Override
        public boolean existsByNetId(String netId) {
            return findByNetId(netId).isPresent();
        }
    };

    private final GiftRepository giftRepo = new GiftRepository() {
        @Override
        public Gift create(int wishlistId, String title, String description, String url, String icon,
                           BigDecimal price, int numberOfPeople, boolean surprise, int creatorUserId) {
            int id = seq.getAndIncrement();
            Gift g = new Gift(id, wishlistId, title, description, url, icon, price, numberOfPeople, surprise, creatorUserId, 0, List.of());
            gifts.put(id, g);
            giftClaimers.put(id, new LinkedHashSet<>());
            return g;
        }

        @Override
        public Optional<Gift> findById(int giftId) {
            Gift base = gifts.get(giftId);
            return base == null ? Optional.empty() : Optional.of(hydrateGift(base));
        }

        @Override
        public List<Gift> findByWishlistIdWithClaims(int wishlistId) {
            return gifts.values().stream()
                    .filter(g -> g.getWishlistId() == wishlistId)
                    .map(InMemoryRepositories.this::hydrateGift)
                    .toList();
        }

        @Override
        public boolean deleteById(int giftId) {
            if (gifts.remove(giftId) == null) {
                return false;
            }
            giftClaimers.remove(giftId);
            return true;
        }

        @Override
        public List<GiftClaimer> findClaimers(int giftId) {
            return giftClaimers.getOrDefault(giftId, Set.of()).stream()
                    .map(users::get)
                    .filter(u -> u != null)
                    .map(u -> new GiftClaimer(u.getId(), u.getUsername(), u.getIcon()))
                    .toList();
        }

        @Override
        public ClaimOutcome claimGiftAtomically(int giftId, int userId) {
            Gift g = gifts.get(giftId);
            if (g == null) {
                return ClaimOutcome.NOT_FOUND;
            }
            Set<Integer> claimers = giftClaimers.computeIfAbsent(giftId, k -> new LinkedHashSet<>());
            if (claimers.contains(userId)) {
                return ClaimOutcome.ALREADY_CLAIMED;
            }
            if (g.getNumberOfPeople() > 0 && claimers.size() >= g.getNumberOfPeople()) {
                return ClaimOutcome.CAPACITY_FULL;
            }
            claimers.add(userId);
            return ClaimOutcome.CLAIMED;
        }

        @Override
        public boolean releaseClaim(int giftId, int userId) {
            return giftClaimers.getOrDefault(giftId, Set.of()).remove(userId);
        }
    };

    private final CommentRepository commentRepo = new CommentRepository() {
        @Override
        public List<ChatComment> findByWishlistId(int wishlistId) {
            List<ChatComment> list = new ArrayList<>();
            for (int i = comments.size() - 1; i >= 0; i--) {
                ChatComment c = comments.get(i);
                if (c.getWishlistId() == wishlistId) {
                    list.add(c);
                }
            }
            return list;
        }

        @Override
        public void addUserComment(int wishlistId, int userId, String commentText) {
            User u = users.get(userId);
            int id = seq.getAndIncrement();
            comments.add(new ChatComment(
                    id, wishlistId, userId,
                    u != null ? u.getUsername() : "",
                    u != null ? u.getIcon() : IconCatalog.DEFAULT_AVATAR_KEY,
                    commentText, CommentType.USER, LocalDateTime.now()
            ));
        }

        @Override
        public void addEventComment(int wishlistId, String commentText) {
            int id = seq.getAndIncrement();
            comments.add(new ChatComment(id, wishlistId, null, "", IconCatalog.DEFAULT_AVATAR_KEY, commentText, CommentType.EVENT, LocalDateTime.now()));
        }

        @Override
        public boolean withdrawLastUserComment(int wishlistId, int userId, String withdrawnNotice) {
            for (int i = comments.size() - 1; i >= 0; i--) {
                ChatComment c = comments.get(i);
                if (c.getWishlistId() == wishlistId && Integer.valueOf(userId).equals(c.getUserId()) && c.getType() == CommentType.USER) {
                    comments.set(i, new ChatComment(
                            c.getId(), wishlistId, c.getUserId(), c.getUsername(), c.getUserIcon(),
                            withdrawnNotice, CommentType.EVENT, c.getCreatedAt()
                    ));
                    return true;
                }
            }
            return false;
        }
    };

    UserRepository users() {
        return userRepo;
    }

    WishlistRepository wishlists() {
        return wishlistRepo;
    }

    GiftRepository gifts() {
        return giftRepo;
    }

    CommentRepository comments() {
        return commentRepo;
    }

    private Gift hydrateGift(Gift base) {
        Set<Integer> claimers = giftClaimers.getOrDefault(base.getId(), Set.of());
        List<String> icons = claimers.stream()
                .map(users::get)
                .filter(u -> u != null)
                .map(User::getIcon)
                .toList();
        return new Gift(
                base.getId(), base.getWishlistId(), base.getTitle(), base.getDescription(),
                base.getUrl(), base.getIcon(), base.getPrice(), base.getNumberOfPeople(),
                base.isSurprise(), base.getCreatorUserId(), claimers.size(), icons
        );
    }
}
