# PostgreSQL Database Schema

SmartGiftList stores users, wishlists, shared wishlist memberships, gifts, gift claims, and wishlist discussion comments inside a single configurable PostgreSQL schema (`DB_SCHEMA`, default `smartgiftlist`).

On container startup, [`docker/init.sql`](../docker/init.sql) creates the schema and all six tables automatically.

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

## 2. Table Contracts & Integrity Rules

| Table | Purpose | Cascades & Constraints |
| :--- | :--- | :--- |
| `Users` | Registered accounts with PBKDF2 password hashes and avatar choice | `Username` and `Email` `UNIQUE` |
| `Wishlists` | Wishlists created by an owner (`UserID`) with shareable `NetID` | `NetID` `UNIQUE`; `ON DELETE CASCADE` from `Users` |
| `Gifts` | Requested items (`NumberOfPeople` slots, optional `Surprise` flag, price, URL) | `ON DELETE CASCADE` from `Wishlists` |
| `UserGiftConnections` | Tracks which users claimed slots on a gift (`COUNT <= NumberOfPeople`) | `ON DELETE CASCADE` from `Users` and `Gifts` |
| `UserWishlistConnections` | Links invited friends to shared wishlists (`IsOwner` flag) | `ON DELETE CASCADE` from `Users` and `Wishlists` |
| `WishlistComments` | Per-wishlist discussion (`CommentType = 1`) and system events (`CommentType = 2`) | `CHECK (CommentType IN (1, 2))`; `ON DELETE CASCADE` from `Wishlists` & `Users` |
