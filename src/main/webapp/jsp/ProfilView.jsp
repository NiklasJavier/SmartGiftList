<%@page import="dev.smartgiftlist.beans.ProfilManager"%>
<%@page import="dev.smartgiftlist.beans.OwnWishListBean"%>
<%@page import="dev.smartgiftlist.beans.MessageBean"%>
<%@page import="dev.smartgiftlist.beans.LoginBean"%>
<%@page import="dev.smartgiftlist.beans.AccountBean"%>
<%@page import="dev.smartgiftlist.security.HtmlUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="../css/SmartWishList.css">
<script type="text/javascript" src="../js/Profil.js"></script>
<script type="text/javascript" src="../js/Owl.js"></script>
<script type="text/javascript" src="../js/Dashboard.js"></script>
<title>Profile</title>
</head>
<body>
    <jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session" />
    <jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session" />
    <jsp:useBean id="owlb" class="dev.smartgiftlist.beans.OwnWishListBean" scope="session" />
    <jsp:useBean id="profilb" class="dev.smartgiftlist.beans.ProfilManager" scope="session" />
    <jsp:useBean id="acc" class="dev.smartgiftlist.beans.AccountBean" scope="session" />
<%
    if (!loginb.isLoggedIn()) {
        response.sendRedirect("./LoginAppl.jsp");
        return;
    } else {
        owlb.fetchAndSetUserIdByUsername(loginb.getUsername());
        owlb.fetchAccountDataForView();
    }
%>
    <header>
        <div class="header-container">
            <div class="logo">
                <div class="logo-items">
                    <img src="../img/icons/logo.svg" alt="SmartGiftList logo" />SmartGiftList
                </div>
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
                <button type="submit" name="ownWishList" value="own-wishlists" class="nav-link">
                    <img src="../img/icons/card-heading.svg" alt="Wishlists icon"> My Wishlists
                </button>
            </div>
            <div class="nav-section right-section">
                <button type="submit" name="profil" value="profile" class="nav-link">
                    <img src="../img/icons/person-fill-gear.svg" alt="Profile icon"> Profile
                </button>
                <button type="submit" name="logout" value="logout" class="nav-link logout-button">
                    <img src="../img/icons/box-arrow-right.svg" alt="Sign out icon"> Sign Out
                </button>
            </div>
        </form>
    </header>

    <main class="main-content">
      <div class="main-E1">
            <section class="E2-top1">
                <h1>Profile Management</h1>
                <jsp:getProperty name="msg" property="portalMessageHtml" />
            <section class="E2-top1-anfangende"></section>
            </section>

      <section class="main-body">
        <div class="profil-container">
            <div class="profil-box">
                <form action="./ProfilAppl.jsp" method="post" id="owl-create-wishlist-form" onsubmit="return checkInput2(this)">
                    <div class="profil-create-form-row">
                        <div class="profil-create-image-container">
                            <img id="owlprofil-profil-preview" src="../img/anlaesse/<%= HtmlUtil.escape(owlb.getProfilIcon() != null && !owlb.getProfilIcon().isBlank() ? owlb.getProfilIcon() : "Lumibella") %>.webp" alt="Profile avatar preview" />
                            <div class="owl-create-list-icon-container">
                                <label for="owlprofil-profil-icon">Avatar:</label>
                                <select name="listIcon" id="owlprofil-profil-icon">
                                    <jsp:getProperty name="owlb" property="profilOptionIconAsHTML" />
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="profil-create-input-container">
                        <label for="profil-username-input">Username:</label>
                        <input type="text" id="profil-username-input" placeholder="Username" name="profilUsername" value="<%= HtmlUtil.escape(owlb.getProfilUsername()) %>" required>
                        <label for="profil-password-input">New Password:</label>
                        <input type="password" placeholder="New password" id="profil-password-input" name="password">
                        <input type="password" placeholder="Confirm password" id="profil-confirm-password-input" name="confirm_password">
                        <span id="msgPassword"></span><span id="msgPasswordNotEqual"></span><br><br>
                        <input type="submit" name="ProfilBtnSave" value="Save" class="button change-profile" onclick="setButtonClicked(this)">
                    </div>
                </form>
                <form action="./ProfilAppl.jsp" method="post" id="profil-delete-form">
                    <input type="submit" name="delete" value="Delete Account" class="button delete-account"/>
                </form>
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
