<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Profile - SmartGiftList</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/base.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/pages.css">
  <script src="${pageContext.request.contextPath}/js/nav.js" defer></script>
  <script src="${pageContext.request.contextPath}/js/auth.js" defer></script>
</head>
<body>

<%@ include file="/WEB-INF/views/partials/header.jspf" %>
<%@ include file="/WEB-INF/views/partials/nav.jspf" %>

<main class="main-content">
  <div class="main-E1">
    <section class="E2-top1">
      <h1>Profile Management</h1>
      <%@ include file="/WEB-INF/views/partials/flash.jspf" %>
      <div class="E2-top1-anfangende"></div>
    </section>

    <section class="main-body">
      <div class="profil-container">
        <div class="profil-box">
          <form action="${pageContext.request.contextPath}/profile" method="post" id="profile-form">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="save">
            <div class="profil-create-form-row">
              <div class="profil-create-image-container">
                <img id="profile-avatar-preview" class="avatar-preview-lg"
                     src="${pageContext.request.contextPath}/${profileUser.avatarPath}"
                     alt="Profile avatar preview">
                <div class="owl-create-list-icon-container">
                  <label for="profile-avatar-select">Avatar:</label>
                  <select name="listIcon" id="profile-avatar-select" class="avatar-select"
                          data-preview-target="profile-avatar-preview">
                    <c:forEach var="opt" items="${avatarOptions}">
                      <option value="${opt.key}"
                              data-preview-src="${pageContext.request.contextPath}/${opt.relativePath}"
                              <c:if test="${profileUser.icon == opt.key}">selected</c:if>>
                        <c:out value="${opt.label}"/>
                      </option>
                    </c:forEach>
                  </select>
                </div>
              </div>
            </div>
            <div class="profil-create-input-container">
              <label for="profil-username-input">Username:</label>
              <input type="text" id="profil-username-input" name="profilUsername"
                     value="<c:out value='${profileUser.username}'/>" required>
              <label for="profil-password-input">New Password:</label>
              <input type="password" id="profil-password-input" name="password" placeholder="New password (optional)">
              <input type="password" id="profil-confirm-password-input" name="confirm_password" placeholder="Confirm password (optional)">
              <p class="field-hint">Leave password fields blank to keep your current password.</p>
              <span id="msgPassword"></span>
              <span id="msgPasswordNotEqual"></span>
              <button type="submit" class="button change-profile">Save</button>
            </div>
          </form>

          <form action="${pageContext.request.contextPath}/profile" method="post" id="profil-delete-form">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="delete">
            <button type="submit" class="button delete-account">Delete Account</button>
          </form>
        </div>
      </div>
    </section>
    <div class="E2-top1-anfangende"></div>
    <section class="E2-end1"></section>
  </div>
</main>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

</body>
</html>
