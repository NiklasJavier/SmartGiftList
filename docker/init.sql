-- SmartGiftList PostgreSQL Schema & Table Initialization
-- Mirrors dev.smartgiftlist.jdbc.AppTables for automatic Docker Compose provisioning.

CREATE SCHEMA IF NOT EXISTS smartgiftlist;
SET search_path TO smartgiftlist;

CREATE TABLE IF NOT EXISTS Users (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(200) UNIQUE,
    Email VARCHAR(200) UNIQUE,
    Password VARCHAR(200),
    Birthday DATE,
    IsAdmin BOOLEAN DEFAULT FALSE,
    Icon VARCHAR(200),
    JoinDate TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Wishlists (
    WishlistID SERIAL PRIMARY KEY,
    NetID VARCHAR(200) UNIQUE,
    Title VARCHAR(200),
    Event VARCHAR(200),
    EventDate DATE,
    Description TEXT,
    URL VARCHAR(200),
    Icon VARCHAR(200),
    UserID INT,
    CreateDate TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Gifts (
    GiftID SERIAL PRIMARY KEY,
    WishlistID INT,
    Title VARCHAR(200),
    NumberOfPeople INT,
    Surprise BOOLEAN,
    Description TEXT,
    Price DECIMAL,
    Url VARCHAR(200),
    Icon VARCHAR(200),
    CreateDate TIMESTAMP,
    CreatorUserID INT,
    FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS UserGiftConnections (
    UserGiftConnectionID SERIAL PRIMARY KEY,
    UserID INT,
    GiftID INT,
    LinkDate TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (GiftID) REFERENCES Gifts(GiftID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS UserWishlistConnections (
    UserWishlistConnectionID SERIAL PRIMARY KEY,
    UserID INT,
    WishlistID INT,
    IsOwner BOOLEAN DEFAULT FALSE,
    LinkDate TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS WishlistComments (
    CommentID SERIAL PRIMARY KEY,
    WishlistID INT,
    UserID INT,
    CommentText TEXT,
    CommentType INT CHECK (CommentType IN (1, 2)),
    CommentTime TIMESTAMP,
    FOREIGN KEY (WishlistID) REFERENCES Wishlists(WishlistID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
