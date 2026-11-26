<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>SmartGiftList Registration</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/base.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/pages.css">
  <script src="${pageContext.request.contextPath}/js/auth.js" defer></script>
</head>
<body>

<%@ include file="/WEB-INF/views/partials/header.jspf" %>

<main class="login-container">
  <div class="login-box">
    <a href="${pageContext.request.contextPath}/" class="back-button">Home</a>
    <h1>Create Account</h1>
    <%@ include file="/WEB-INF/views/partials/flash.jspf" %>
    <form id="regForm" action="${pageContext.request.contextPath}/register" method="post" class="login-form">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="text" id="username" name="username" placeholder="Username" value="<c:out value='${draftUsername}'/>" required>
      <span id="msgUsername"></span>
      <input type="email" id="email" name="email" placeholder="Email Address" value="<c:out value='${draftEmail}'/>" required>
      <span id="msgEmail"></span>
      <input type="date" id="birthday" name="birthday" placeholder="Date of Birth" value="<c:out value='${draftBirthday}'/>">
      <span id="msgBirthday"></span>
      <input type="password" id="password" name="password" placeholder="Password (min. 6 characters)" required>
      <input type="password" id="confirm-password" name="confirm_password" placeholder="Confirm Password" required>
      <span id="msgPassword"></span>
      <span id="msgPasswordNotEqual"></span>
      <button type="submit" class="button login">Create Account</button>
    </form>
    <a href="${pageContext.request.contextPath}/login" class="button create-account">Sign In</a>
  </div>
  <div class="info-section">
    <p class="progress-text">Collaborative wishlists and gift coordination made simple.</p>
    <div class="progress-bar"></div>
  </div>
</main>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

</body>
</html>
