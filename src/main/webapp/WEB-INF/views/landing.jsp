<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="showLandingButtons" value="true" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>SmartGiftList</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/base.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/pages.css">
  <script src="${pageContext.request.contextPath}/js/landing.js" defer></script>
</head>
<body>

<%@ include file="/WEB-INF/views/partials/header.jspf" %>

<main>
  <section class="hero">
    <div class="hero-content">
      <h1>Collaborative wishlists for family and friends</h1>
      <p>Organize gifts, coordinate group presents, and avoid duplicates effortlessly.</p><br>
      <a href="${pageContext.request.contextPath}/register" class="hero-button">Get Started</a>
    </div>
  </section>

  <section class="faq-section">
    <h2>Frequently Asked Questions</h2>
    <div class="accordion">
      <div class="accordion-item">
        <button type="button" class="accordion-question" aria-expanded="false">How do I create a wishlist?</button>
        <div class="accordion-answer">
          <p>Sign in and click "Create Wishlist" on your wishlist dashboard.</p>
        </div>
      </div>
      <div class="accordion-item">
        <button type="button" class="accordion-question" aria-expanded="false">Can I keep my wishlist private?</button>
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

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

</body>
</html>
