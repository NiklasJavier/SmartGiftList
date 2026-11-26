<%@page import="dev.smartgiftlist.beans.LoginBean"%>
<%@page import="dev.smartgiftlist.beans.MessageBean"%>
<%@page import="dev.smartgiftlist.beans.OwnWishListBean"%>
<%@page import="dev.smartgiftlist.security.HtmlUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>My Wishlists - SmartGiftList</title>
<link rel="stylesheet" type="text/css" href="../css/SmartWishList.css">
<script type="text/javascript" src="../js/Owl.js"></script>
<script src="../js/Dashboard.js"></script>
</head>
<body>
<jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session"/>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>
<jsp:useBean id="owlb" class="dev.smartgiftlist.beans.OwnWishListBean" scope="session"/>

<%
    if (!loginb.isLoggedIn()) {
        response.sendRedirect("./LoginAppl.jsp");
        return;
    } else {
        owlb.fetchAndSetUserIdByUsername(loginb.getUsername());
    }
%>

    <header>
        <div class="header-container">
            <div class="logo">
                <div class="logo-items"><img src="../img/icons/logo.svg" alt="SmartGiftList logo" />SmartGiftList</div>
            </div>
            <div class="header-buttons"></div>
        </div>
    </header>

    <header class="secondary-header">
        <div class="menu-icon">
            <img src="../img/icons/list.svg" alt="Menu" id="open-icon">
            <img src="../img/icons/x.svg" alt="Close" id="close-icon" style="display: none;">
        </div>
        <form action="./NavAppl.jsp" method="get" class="nav">
            <div class="nav-section left-section">
                <button type="submit" name="ownWishList" value="own-wishlists" class="nav-link" onclick="saveScrollPosition()">
                    <img src="../img/icons/card-heading.svg" alt="Wishlists icon"> My Wishlists
                </button>
            </div>
            <div class="nav-section right-section">
                <button type="submit" name="profil" value="profile" class="nav-link" onclick="saveScrollPosition()">
                    <img src="../img/icons/person-fill-gear.svg" alt="Profile icon"> Profile
                </button>
                <button type="submit" name="logout" value="logout" class="nav-link logout-button" onclick="saveScrollPosition()">
                    <img src="../img/icons/box-arrow-right.svg" alt="Sign out icon"> Sign Out
                </button>
            </div>
        </form>
    </header>

    <!-- Popup 0: Connect Shared Wishlist via NetID -->
    <div id="owl-connect-wishlist-popup" class="owl-connect-popup">
        <div class="owl-connect-popup-content">
            <h2>Connect Wishlist</h2>
            <div id="owlc-desc">
                <p>Join a shared wishlist by entering its NetID invite code.</p>
            </div>
            <span class="owl-connect-close-button">&times;</span>
            <form action="./OwnWishListAppl.jsp" method="post" id="owl-connect-wishlist-form">
                <div class="owl-create-form-row">
                    <div class="owl-create-input-container">
                        <label for="owl-connect-wishlist-netid">NetID:</label>
                        <input type="text" id="owl-connect-wishlist-netid" name="NetIDConnect" placeholder="Enter NetID..." required>
                    </div>
                </div>
                <input type="submit" name="wishlistConnect" value="Connect Wishlist" class="owl-create-submit-button" onclick="saveScrollPosition()">
            </form>
        </div>
    </div>

    <!-- Popup 1: Create Wishlist -->
    <div id="owl-create-wishlist-popup" class="owl-create-popup">
        <div class="owl-create-popup-content">
            <h2>Create Wishlist</h2>
            <div id="owlc-desc">
                <p>Enter the details for your new wishlist below.</p>
            </div>
            <span class="owl-create-close-button">&times;</span>
            <form action="./OwnWishListAppl.jsp" method="post" id="owl-create-wishlist-form">
                <div class="owl-create-form-row">
                    <div class="owl-create-image-container">
                        <img id="list-image-preview" src="../img/anlaesse/PaperList.webp" alt="Cover preview" />
                        <div class="owl-create-list-icon-container">
                            <label for="owl-create-list-icon">Theme:</label>
                            <select name="list_icon" id="owl-create-list-icon">
                                <option value="../img/anlaesse/PaperList.webp">PaperList</option>
                                <option value="../img/anlaesse/BrozeList.webp">BrozeList</option>
                                <option value="../img/anlaesse/SilverList.webp">SilverList</option>
                                <option value="../img/anlaesse/GoldenList.webp">GoldenList</option>
                            </select>
                        </div>
                    </div>
                    <div class="owl-create-input-container">
                        <label for="owl-create-wishlist-name">Wishlist Title:</label>
                        <input type="text" id="owl-create-wishlist-name" name="wishlist_name" required>
                        <label for="owl-create-wishlist-occasion">Occasion:</label>
                        <input type="text" id="owl-create-wishlist-occasion" name="occasion">
                        <label for="owl-create-wishlist-date">Occasion Date:</label>
                        <input type="date" id="owl-create-wishlist-date" name="date">
                        <label for="owl-create-wishlist-description">Description:</label>
                        <textarea id="owl-create-wishlist-description" name="description"></textarea>
                        <label for="owl-create-wishlist-links">Reference URL:</label>
                        <input type="url" id="owl-create-wishlist-links" name="links">
                    </div>
                </div>
                <input type="submit" name="create_wishlist" value="Create Wishlist" class="owl-create-submit-button" onclick="saveScrollPosition()">
            </form>
        </div>
    </div>

    <!-- Popup 2: Add Gift -->
    <div id="owlpedit-create-gift-popup" class="owlpedit-create-popup">
        <div class="owlpedit-create-popup-content">
            <h2>Add Gift</h2>
            <div id="owlc-desc">
                <p>Add a new gift idea to the selected wishlist.</p>
            </div>
            <span class="owlpedit-create-close-button">&times;</span>
            <form action="./OwnWishListAppl.jsp" method="post" id="owlpedit-create-gift-form">
                <div class="owlpedit-create-form-row">
                    <div class="owlpedit-image-container">
                        <img id="owlpedit-image-preview" src="../img/anlaesse/PaperList.webp" alt="Gift image preview" />
                        <div class="owlpedit-list-icon-container">
                            <label for="owlpedit-list-icon">Image:</label>
                            <select id="owlpedit-list-icon" name="owlpEditListIcon">
                                <option value="../img/anlaesse/PaperList.webp">PaperList</option>
                                <option value="../img/anlaesse/BrozeList.webp">BrozeList</option>
                                <option value="../img/anlaesse/SilverList.webp">SilverList</option>
                                <option value="../img/anlaesse/GoldenList.webp">GoldenList</option>
                            </select>
                        </div>
                    </div>
                    <div class="owlpedit-input-container">
                        <label for="owlpedit-gift-name">Gift Name:</label>
                        <input type="text" id="owlpedit-gift-name" name="owlpEditGiftName" required>

                        <label for="owlpedit-gift-type">Gift Type:</label>
                        <select id="owlpedit-gift-type">
                            <option value="single">Individual Gift</option>
                            <option value="group">Group Gift</option>
                        </select>

                        <label for="owlpedit-group-gift-amount">Participants Needed:</label>
                        <input type="number" id="owlpedit-group-gift-amount" name="owlpEditGroupGiftAmount" min="1" value="1">

                        <label for="owlpedit-surprise-gift" class="owlpedit-checkbox-label">
                            Surprise Gift:
                            <input type="checkbox" id="owlpedit-surprise-gift" name="owlpEditSurpriseGift">
                        </label>

                        <label for="owlpedit-gift-description">Description:</label>
                        <textarea id="owlpedit-gift-description" name="owlpEditDescription"></textarea>

                        <label for="owlpedit-gift-price">Price (optional):</label>
                        <input type="text" id="owlpedit-gift-price" name="owlpEditPrice">

                        <label for="owlpedit-gift-link">Product URL (optional):</label>
                        <input type="url" id="owlpedit-gift-link" name="owlpEditLink">
                    </div>
                </div>
                <div class="owlpedit-button-container">
                    <button type="submit" class="owlpedit-submit-button" name="owlpEditSubmitButton" value="Save Gift" onclick="saveScrollPosition()">Save</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Popup 3: Gift Details & Claim/Release -->
    <div id="Owlpde-edit-gift-popup" class="Owlpde-create-popup">
        <div class="Owlpde-create-popup-content">
            <div class="Owlpde-image-and-buttons-container">
                <h2>Gift Details</h2>
                <img id="Owlpde-image-preview" src="<%= HtmlUtil.escape(owlb.getGift().getIcon()) %>" alt="Gift preview" />
                <button form="Owlpde-detail-popup-form" type="submit" id="Owlpde-takeover-btn" class="Owlpde-button" name="OwlpDeBtnTakeover" value="OwlpDeBtnTakeover">Claim</button>
                <button form="Owlpde-detail-popup-form" type="submit" id="Owlpde-release-btn" class="Owlpde-button" name="OwlpDeBtnRelease" value="OwlpDeBtnRelease">Release</button>
                <label id="Owlpde-taken-label"></label>
                <ul id="Owlpde-taken-list">
                    <jsp:getProperty property="userGiftConnectionsHTML" name="owlb"/>
                </ul>
            </div>
            <div class="Owlpde-input-container">
                <form action="./OwnWishListAppl.jsp" method="post" id="Owlpde-detail-popup-form">
                    <span class="Owlpde-create-close-button">&times;</span>
                    <input type="text" name="OwlpDeGiftID" class="versteckt" value="<%= HtmlUtil.escape(owlb.getGift().getGiftID()) %>" readonly>
                </form>
                <label for="Owlpde-gift-name">Gift Name:</label>
                <input placeholder="No gift selected" type="text" id="Owlpde-gift-name" name="gift-name" class="readonly-field" value="<%= HtmlUtil.escape(owlb.getGift().getTitle()) %>" readonly>
                <label for="Owlpde-gift-type">Gift Type:</label>
                <select id="Owlpde-gift-type" name="gift-type" class="readonly-field" disabled>
                    <option value="type"><%= HtmlUtil.escape(owlb.getGift().getWishlistType()) %></option>
                </select>
                <label for="Owlpde-group-gift-amount">Participants Needed:</label>
                <input type="number" id="Owlpde-group-gift-amount" name="group-gift-amount" class="readonly-field" value="<%= HtmlUtil.escape(owlb.getGift().getNumberOfPeople()) %>" readonly>
                <label for="Owlpde-gift-description">Description:</label>
                <textarea id="Owlpde-gift-description" name="description" class="readonly-field" readonly><%= HtmlUtil.escape(owlb.getGift().getDescription()) %></textarea>
                <label for="Owlpde-gift-price">Price (optional):</label>
                <input type="text" id="Owlpde-gift-price" name="price" class="readonly-field" value="<%= HtmlUtil.escape(owlb.getGift().getPrice()) %>" readonly>
                <label for="Owlpde-gift-link">Product URL (optional):</label>
                <input type="url" id="Owlpde-gift-link" name="link" class="readonly-field" value="<%= HtmlUtil.escape(owlb.getGift().getUrl()) %>" readonly>
            </div>
        </div>
    </div>

    <main class="main-content">
        <div class="main-E1">
            <section class="E2-top1">
                <h1>Wishlists</h1>
                <jsp:getProperty name="msg" property="portalMessageHtml" />
            </section>
            <section class="E2-top1-anfangende"></section>

            <section class="main-body">
                <div class="owl-container">
                    <div class="owl-top-area">
                        <div class="selector-container">
                            <form action="./OwnWishListAppl.jsp" method="post">
                                <jsp:getProperty name="owlb" property="wishlistHTMLDropdownWithConnected"/>
                                <button type="submit" name="showWishlist" value="showWishlist" class="button-black select-wishlist" onclick="saveScrollPosition()">Select</button>
                            </form>
                        </div>

                        <div class="spacer"></div>

                        <div id="owl-buttons-addwishlist">
                            <button id="owl-create-wishlist-button" class="button-black create-wishlist" onclick="saveScrollPosition()"><img src="../img/icons/folder-plus.svg" alt="Create" class="button-icon">Create Wishlist</button>
                            <button id="owl-connect-wishlist-button" class="button-black create-wishlist" onclick="saveScrollPosition()"><img src="../img/icons/folder-plus.svg" alt="Connect" class="button-icon">Connect Wishlist (NetID)</button>
                        </div>
                    </div>

                    <div class="owl-middle-area">
                        <div class="owl-left-box">
                            <form action="./OwnWishListAppl.jsp" method="post" id="owl-update-wishlist-form">
                                <h2>Wishlist Details</h2>
                                <div id="owlc-desc">
                                    <p>Configure your active wishlist settings and participants.</p>
                                </div><br>

                                <div class="owlu-client-details-container">
                                    <div class="owlu-client-details-content">
                                        <div class="owlu-client-details-left">
                                            <img src="<%= HtmlUtil.escape(owlb.getWishlist().getIcon() != null && !owlb.getWishlist().getIcon().isBlank() ? owlb.getWishlist().getIcon() : "../img/anlaesse/PaperList.webp") %>" id="owluIconImage" alt="Wishlist cover" class="owlu-gift-image">
                                            <label for="owlu-persona">Icon:</label>
                                            <select name="owluWishListIcon" id="owlu-persona">
                                                <jsp:getProperty property="iconDropdownHtml" name="owlb"/>
                                            </select>
                                        </div>
                                        <div class="owlu-client-details-right">
                                            <label for="owlu-wishlist-name">Wishlist Title:</label>
                                            <input type="text" placeholder="No wishlist selected" name="owluWishlistName" id="owlu-wishlist-name" class="owlu-input" value="<%= HtmlUtil.escape(owlb.getWishlist().getTitle()) %>" readonly>

                                            <label for="owlu-event">Occasion:</label>
                                            <input type="text" placeholder="No occasion specified" name="owluWislistEvent" id="owlu-event" value="<%= HtmlUtil.escape(owlb.getWishlist().getEvent()) %>">

                                            <label for="owlu-date">Occasion Date:</label>
                                            <input type="date" name="owluWishlistDate" id="owlu-date" value="<%= owlb.getWishlist().getEventDate() != null ? owlb.getWishlist().getEventDate() : "" %>">
                                        </div>
                                    </div>

                                    <label for="owlu-description">Description:</label>
                                    <textarea placeholder="No description provided..." name="owluWishlistDescription" id="owlu-description"><%= HtmlUtil.escape(owlb.getWishlist().getDescription()) %></textarea>

                                    <label for="owlu-links">Reference URL:</label>
                                    <input placeholder="No URL provided..." type="url" name="owluWishlistLinks" id="owlu-links" value="<%= HtmlUtil.escape(owlb.getWishlist().getUrl()) %>">

                                    <div class="owlu-net-id-instruction">
                                        Share this NetID invite code with friends or family so they can connect to your wishlist:
                                    </div>

                                    <div class="owlu-net-id-container">
                                        <label for="owlu-net-id">Net-ID:</label>
                                        <input type="text" name="owluWishlistNetID" id="owlu-net-id" class="owlu-net-id-input" value="<%= HtmlUtil.escape(owlb.getWishlist().getNetid()) %>" readonly>
                                    </div>

                                    <div class="owlu-net-id-teilnehmer">
                                        Current Participants:
                                    </div>

                                    <div class="owlu-participant-table-container">
                                        <table class="owlu-participant-table">
                                            <thead>
                                                <tr>
                                                    <th></th>
                                                    <th>ID</th>
                                                    <th>Name</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <jsp:getProperty name="owlb" property="wishlistParticipantsHTML"/>
                                            </tbody>
                                        </table>
                                    </div>

                                    <div class="owlu-action-buttons">
                                        <button type="submit" name="owluWishlistDeleteButton" value="delete" class="owlu-delete-btn" onclick="saveScrollPosition()">Remove</button>
                                        <button type="submit" name="owluWishlistSaveButton" value="save" class="owlu-edit-btn" onclick="saveScrollPosition()">Save</button>
                                    </div>
                                </div>
                            </form>
                        </div>

                        <div class="owl-right-box">
                            <h2>Gifts</h2>
                            <div id="owlc-desc">
                                <p>Track requested items, group contributions, and surprise gifts.</p>
                            </div><br>

                            <div class="owlp-stats-desc" id="owlp-stats-desc">
                                <p id="owlp-uv"><img src="../img/status/uv.png" alt="status icon"> IG - Individual Gift</p>
                                <p id="owlp-gg"><img src="../img/status/gg.png" alt="status icon"> GG - Group Gift</p>
                                <p id="owlp-sg"><img src="../img/status/ug.png" alt="status icon"> SG - Surprise Gift</p>
                                <p id="owlp-v"><img src="../img/status/v.png" alt="status icon"> CL - Claimed</p>
                            </div>

                            <div class="owlp-table-container">
                                <form action="./OwnWishListAppl.jsp" method="post" id="owlp-stats-desc-form">
                                    <table class="owlp-table">
                                        <thead>
                                            <tr>
                                                <th></th>
                                                <th>Name</th>
                                                <th>Description</th>
                                                <th>Attributes</th>
                                                <th>Contributors</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <jsp:getProperty name="owlb" property="giftListForWishlistAsHtml"/>
                                        </tbody>
                                    </table>
                                </form>
                            </div>
                            <div class="owlp-button-container">
                                <button form="owlp-stats-desc-form" type="submit" class="owlp-button" id="owlp-details-btn" name="owlpButtonForDetails" value="owlpButtonForDetails" onclick="saveScrollPosition()">Details</button>
                                <button class="owlp-button" id="owlp-add-btn" onclick="saveScrollPosition()">+ Add Gift</button>
                                <button form="owlp-stats-desc-form" type="submit" class="owlp-button" id="owlp-delete-btn" name="owlpButtonForDelete" value="owlpButtonForDelete"><img src="../img/icons/trash3-fill.svg" alt="Delete" class="button-icon" onclick="saveScrollPosition()">Delete</button>
                            </div>

                            <h2 class="owlp-chatbox-title">Discussion</h2>
                            <div id="owlc-desc">
                                <p>Share gift ideas, coordinate surprises, and exchange notes.</p>
                            </div><br>

                            <div class="chat-container">
                                <div class="chat-messages">
                                    <jsp:getProperty name="owlb" property="renderWishlistComments"/>
                                </div>
                                <div class="chat-input">
                                    <input form="chatinput-form" type="text" placeholder="Write a message..." name="ChatUserMessage">
                                    <button form="chatinput-form" type="submit" class="send-btn" name="ChatUserButton" value="sendMessage" onclick="saveScrollPosition()"><img src="../img/icons/send-plus-fill.svg" alt="Send"> SEND</button>
                                    <button form="chatinput-form" type="submit" class="load-btn" name="ChatUserButton" value="loadMessage" onclick="saveScrollPosition()"><img src="../img/icons/arrow-clockwise.svg" alt="Refresh"></button>
                                    <button form="chatinput-form" type="submit" class="load-btn" name="ChatUserButton" value="deleteMessage" onclick="saveScrollPosition()"><img src="../img/icons/trash3-fill.svg" alt="Withdraw last message"></button>
                                    <form action="./OwnWishListAppl.jsp" method="post" id="chatinput-form"></form>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="owl-bottom-area">
                        <h2 class="owlc-section-title">Overview</h2>
                        <div id="owlc-desc">
                            <p>Browse your personal and shared wishlists at a glance.</p>
                        </div><br>

                        <h3 class="owlc-section-title">My Wishlists</h3>
                        <div class="owlc-card-scroll-container">
                            <div class="owlc-card-container">
                                <jsp:getProperty name="owlb" property="wishlistCardsHtmlForUser"/>
                                <div class="owlc-card">
                                    <div class="owlc-card-image"></div>
                                    <div class="owlc-card-content">
                                        <p><button id="owlc-card-content-add" class="owlc-select-button" onclick="saveScrollPosition()">+ Create</button></p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <h3 class="owlc-section-title">Shared Wishlists from Friends</h3>
                        <div class="owlc-card-scroll-container">
                            <div class="owlc-card-container">
                                <jsp:getProperty name="owlb" property="wishlistCardsHtmlForUserConnectedLists"/>
                                <div class="owlc-card">
                                    <div class="owlc-card-image"></div>
                                    <div class="owlc-card-content">
                                        <p><button id="owlc-card-content-connect" class="owlc-select-button" onclick="saveScrollPosition()">+ Connect via NetID</button></p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
            <section class="E2-top1-anfangende"></section>
            <section class="E2-end1"></section>
        </div>
    </main>

    <footer>
        <div id="footer-divider"></div>
        <div class="footer-container">
            <div class="footer-section">
                <h3>Our Vision</h3>
                <p>Coordinating group gifts — simple, transparent, and user-friendly.</p>
                <br>
                <div class="social-icons">
                    <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer"><img src="../img/facebook.png" alt="Facebook" class="social-icon"></a>
                    <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer"><img src="../img/instagram.png" alt="Instagram" class="social-icon"></a>
                    <a href="https://www.youtube.com" target="_blank" rel="noopener noreferrer"><img src="../img/youtube.png" alt="YouTube" class="social-icon"></a>
                </div>
                <br>
            </div>
            <div class="footer-section">
                <h3>Links</h3>
                <ul>
                    <li><a href="./OwnWishListView.jsp">Home</a></li>
                </ul>
            </div>
            <div class="footer-section">
                <h3>Legal</h3>
                <ul>
                    <li><a href="#">Imprint</a></li>
                    <li><a href="#">Privacy Notice</a></li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <p>SmartGiftList</p>
        </div>
    </footer>

</body>
</html>
