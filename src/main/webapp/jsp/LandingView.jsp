<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList</title>
<link rel="stylesheet" type="text/css" href="../css/SmartWishList.css">
<script type="text/javascript" src="../js/LandingPage.js"></script>
</head>
<body>

<header>
  <div class="header-container">
    <div class="logo">
      <div class="logo-items"><img src="../img/icons/logo.svg" alt="SmartGiftList logo"/>SmartGiftList</div>
    </div>
    <div class="header-buttons">
      <form action="./NavAppl.jsp" method="get">
        <button type="submit" name="toRegister" value="Register">Register</button>
        <button type="submit" name="toLogin" value="Login">Sign In</button>
      </form>
    </div>
  </div>
</header>

<main>
  <section class="hero">
    <div class="hero-content">
      <h1>Collaborative wishlists for family and friends</h1>
      <p>Organize gifts, coordinate group presents, and avoid duplicates effortlessly.</p><br>
      <form action="./NavAppl.jsp" method="get">
        <button type="submit" name="toRegister" value="Register" class="hero-button">Get Started</button>
      </form>
    </div>
  </section>

  <section class="faq-section">
    <h2>Frequently Asked Questions</h2>
    <div class="accordion">
      <div class="accordion-item">
        <button class="accordion-question" onclick="toggleAccordion(this)">How do I create a wishlist?</button>
        <div class="accordion-answer">
          <p>Sign in and click "Create Wishlist" on your wishlist dashboard.</p>
        </div>
      </div>
      <div class="accordion-item">
        <button class="accordion-question" onclick="toggleAccordion(this)">Can I keep my wishlist private?</button>
        <div class="accordion-answer">
          <p>Yes. Wishlists are only accessible to people you share your wishlist NetID with.</p>
        </div>
      </div>
    </div>
  </section>

  <section class="features-section">
    <div class="feature-card">
      <h3>Easy Wishlist Creation</h3>
      <p>Create personalized wishlists for birthdays, weddings, or holidays in seconds and list every gift idea in one place.</p>
    </div>
    <div class="feature-card">
      <h3>Share via Invite Codes</h3>
      <p>Share your list using a dedicated NetID so selected friends and family members can view items and join the discussion.</p>
    </div>
    <div class="feature-card">
      <h3>Seamless Group Gifts</h3>
      <p>Coordinate individual, group, or surprise gifts with real-time participant status tracking so nothing gets bought twice.</p>
    </div>
  </section>
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
        <li><a href="#">Home</a></li>
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
