# PostgreSQL Database Schema

SmartGiftList stores users, wishlists, shared wishlist memberships, gifts, gift claims, and wishlist discussion comments inside a single configurable PostgreSQL schema (`DB_SCHEMA`, default `smartgiftlist`).

On container startup, [`docker/init.sql`](../docker/init.sql) creates the schema, all six tables, uniqueness constraints, and indexes automatically; [`SchemaInitializer`](../src/main/java/dev/smartgiftlist/config/SchemaInitializer.java) ensures the identical structure idempotently when the application boots.

---

## 1. Entity-Relationship Diagram

```mermaid
erDiagram
  USERS ||--o{ WISHLISTS : "owns"
  USERS ||--o{ USER_WISHLIST_CONNECTIONS : "joins"
  WISHLISTS ||--o{ USER_WISHLIST_CONNECTIONS : "shared with"
  WISHLISTS ||--o{ GIFTS : "contains"
  USERS ||--o{ USER_GIFT_CONNECTIONS : "claims"
  GIFTS ||--o{ USER_GIFT_CONNECTIONS : "claimed by"
  WISHLISTS ||--o{ WISHLIST_COMMENTS : "hosts"
  USERS ||--o{ WISHLIST_COMMENTS : "writes"

  USERS {
    serial UserID PK
    varchar Username UK
    varchar Email UK
    varchar Password
    date Birthday
    boolean IsAdmin
    varchar Icon
    timestamp JoinDate
  }

  WISHLISTS {
    serial WishlistID PK
    varchar NetID UK
    varchar Title
    varchar Event
    date EventDate
    text Description
    varchar URL
    varchar Icon
    int UserID FK
    timestamp CreateDate
  }

  GIFTS {
    serial GiftID PK
    int WishlistID FK
    varchar Title
    int NumberOfPeople
    boolean Surprise
    text Description
    decimal Price
    varchar Url
    varchar Icon
    timestamp CreateDate
    int CreatorUserID
  }

  USER_GIFT_CONNECTIONS {
    serial UserGiftConnectionID PK
    int UserID FK
    int GiftID FK
    timestamp LinkDate
  }

  USER_WISHLIST_CONNECTIONS {
    serial UserWishlistConnectionID PK
    int UserID FK
    int WishlistID FK
    boolean IsOwner
    timestamp LinkDate
  }

  WISHLIST_COMMENTS {
    serial CommentID PK
    int WishlistID FK
    int UserID FK
    text CommentText
    int CommentType
    timestamp CommentTime
  }
```

---

## 2. Table Contracts, Constraints & Indexes

| Table | Purpose | Cascades, Constraints & Indexes |
| :--- | :--- | :--- |
| `Users` | Registered accounts with PBKDF2 password hashes and avatar choice | `Username` and `Email` `UNIQUE NOT NULL` |
| `Wishlists` | Wishlists created by an owner (`UserID`) with shareable `SGL-*` `NetID` | `NetID` `UNIQUE NOT NULL`; `ON DELETE CASCADE` from `Users`; index `idx_wishlists_user` |
| `Gifts` | Requested items (`NumberOfPeople >= 1`, optional `Surprise`, `DECIMAL(10,2)` price, URL) | `ON DELETE CASCADE` from `Wishlists`; index `idx_gifts_wishlist` |
| `UserGiftConnections` | Tracks participant claims (`COUNT <= NumberOfPeople`) | `UNIQUE (UserID, GiftID)`; `ON DELETE CASCADE` from `Users` & `Gifts`; index `idx_ugc_gift` |
| `UserWishlistConnections` | Links invited friends to shared wishlists (`IsOwner` flag) | `UNIQUE (UserID, WishlistID)`; `ON DELETE CASCADE` from `Users` & `Wishlists`; index `idx_uwc_user` |
| `WishlistComments` | Per-wishlist discussion (`CommentType = 1`) and system events (`CommentType = 2`) | `CHECK (CommentType IN (1, 2))`; `ON DELETE CASCADE` from `Wishlists` & `Users`; index `idx_comments_wishlist_time` |
