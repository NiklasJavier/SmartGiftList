<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>SmartGiftList Sign In</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/base.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/pages.css">
</head>
<body>

<%@ include file="/WEB-INF/views/partials/header.jspf" %>

<main class="login-container">
  <div class="login-box">
    <a href="${pageContext.request.contextPath}/" class="back-button">Home</a>
    <h1>Sign In</h1>
    <%@ include file="/WEB-INF/views/partials/flash.jspf" %>
    <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="text" name="username" id="username" placeholder="Username" required>
      <input type="password" name="password" id="password" placeholder="Password" required>
      <button type="submit" class="button login">Sign In</button>
    </form>
    <a href="${pageContext.request.contextPath}/register" class="button create-account">Create Account</a>
  </div>
  <div class="info-section">
    <p class="progress-text">Collaborative wishlists and gift coordination.</p>
    <div class="progress-bar"></div>
  </div>
</main>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

</body>
</html>
