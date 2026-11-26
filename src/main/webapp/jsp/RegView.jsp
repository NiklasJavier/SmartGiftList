<%@page import="dev.smartgiftlist.security.HtmlUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList Registration</title>
<link rel="stylesheet" type="text/css" href="../css/SmartWishList.css">
<script type="text/javascript" src="../js/Owl.js"></script>
</head>
<body>
<jsp:useBean id="accb" class="dev.smartgiftlist.beans.AccountBean" scope="session"/>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>

<header>
  <div class="header-container">
    <div class="logo">
      <div class="logo-items"><img src="../img/icons/logo.svg" alt="SmartGiftList logo"/>SmartGiftList</div>
    </div>
    <div class="header-buttons"></div>
  </div>
</header>

<main>
  <div class="login-container">
    <div class="login-box">
      <a href="../jsp/LandingView.jsp" class="back-button">Home</a>
      <h1>Create Account</h1>
      <jsp:getProperty name="msg" property="messageHtml"/>
      <form id="regForm" action="./RegAppl.jsp" method="post" class="login-form" onsubmit="return checkInput(this)">
        <input type="text" id="username" name="username" placeholder="Username" value="<%= HtmlUtil.escape(accb.getUsername()) %>">
        <span id="msgUsername"></span>
        <input type="email" id="email" name="email" placeholder="Email Address" value="<%= HtmlUtil.escape(accb.getEmail()) %>">
        <span id="msgEmail" class="error-text"></span>
        <input type="date" id="birthday" name="birthday" placeholder="Date of Birth" value="<%= accb.getBirthday() != null ? accb.getBirthday() : "" %>">
        <span id="msgBirthday"></span>
        <input type="password" id="password" name="password" placeholder="Password" onkeyup="checkInput(document.getElementById('regForm'))" onchange="checkInput(document.getElementById('regForm'))">
        <input type="password" id="confirm-password" name="confirm_password" placeholder="Confirm Password">
        <span id="msgPassword"></span>
        <span id="msgPasswordNotEqual"></span>
        <button type="submit" name="register" value="Register" class="button login" onclick="setButtonClicked(this)">Create Account</button>
        <button type="submit" name="toLogin" value="Login" class="button create-account" onclick="setButtonClicked(this)">Sign In</button>
      </form>
    </div>
    <div class="info-section">
      <p class="progress-text">Collaborative wishlists and gift coordination made simple.</p>
      <div class="progress-bar"></div>
    </div>
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
        <li><a href="./LandingView.jsp">Home</a></li>
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
