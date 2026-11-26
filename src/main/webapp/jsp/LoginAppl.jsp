<%@page import="java.sql.SQLException"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList - Authentication Handler</title>
</head>
<body>
<jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session"/>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>

<%
request.setCharacterEncoding("UTF-8");

String username = request.getParameter("username");
String password = request.getParameter("password");

String login = request.getParameter("login");
String toRegister = request.getParameter("toRegister");

if (login == null) login = "";
if (toRegister == null) toRegister = "";

if (login.equals("Login")) {
    try {
        loginb.setUsername(username);
        loginb.setPassword(password);
        boolean found = loginb.checkUsernamePassword();
        if (found) {
            msg.setPortalWelcome(username);
            loginb.setLoggedIn(true);
            response.sendRedirect("./OwnWishListView.jsp");
        } else {
            msg.setLoginFailed();
            loginb.setLoggedIn(false);
            response.sendRedirect("./LoginView.jsp");
        }
    } catch (SQLException se) {
        se.printStackTrace();
        msg.setGeneralError();
        response.sendRedirect("./LoginView.jsp");
    }
} else if (toRegister.equals("Register")) {
    msg.setRegistrationWelcome();
    response.sendRedirect("./RegView.jsp");
} else {
    response.sendRedirect("./LoginView.jsp");
}
%>

</body>
</html>
