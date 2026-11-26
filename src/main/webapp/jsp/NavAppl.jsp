<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList - Navigation Handler</title>
</head>
<body>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>
<jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session"/>
<jsp:useBean id="owlb" class="dev.smartgiftlist.beans.OwnWishListBean" scope="session"/>

<%
request.setCharacterEncoding("UTF-8");

String toRegister = request.getParameter("toRegister");
String toLogin = request.getParameter("toLogin");

String ownWishList = request.getParameter("ownWishList");
String netList = request.getParameter("netList");
String profil = request.getParameter("profil");
String logout = request.getParameter("logout");

if (toLogin == null) toLogin = "";
if (toRegister == null) toRegister = "";

if (ownWishList == null) ownWishList = "";
if (netList == null) netList = "";
if (profil == null) profil = "";
if (logout == null) logout = "";

if (toRegister.equals("Register")) {
    System.out.println("[Action] Navigate to registration");
    msg.setRegistrationWelcome();
    response.sendRedirect("./RegView.jsp");
} else if (toLogin.equals("Login")) {
    System.out.println("[Action] Navigate to sign in");
    msg.setGeneralWelcome();
    response.sendRedirect("./LoginView.jsp");
} else if (ownWishList.equals("own-wishlists")) {
    System.out.println("[Action] Navigate to wishlists");
    msg.setOWLWelcome();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (netList.equals("shared-wishlists")) {
    System.out.println("[Action] Navigate to shared wishlists");
    msg.setOWLWelcome();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (profil.equals("profile")) {
    System.out.println("[Action] Navigate to profile");
    owlb.fetchAccountDataForView();
    msg.setProfilMessage();
    response.sendRedirect("./ProfilView.jsp");
} else if (logout.equals("logout")) {
    System.out.println("[Action] User signed out");
    loginb.setLoggedIn(false);
    msg.setLogoutSuccessful();
    owlb.setSessionLogoutParameter();
    response.sendRedirect("./LoginView.jsp");
} else {
    response.sendRedirect("./LandingView.jsp");
}
%>

</body>
</html>
