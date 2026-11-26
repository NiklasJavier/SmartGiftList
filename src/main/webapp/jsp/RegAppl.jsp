<%@page import="java.sql.SQLException"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList - Registration Handler</title>
</head>
<body>
<jsp:useBean id="accb" class="dev.smartgiftlist.beans.AccountBean" scope="session"/>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>

<%
request.setCharacterEncoding("UTF-8");

String username = request.getParameter("username");
String email = request.getParameter("email");
String birthday = request.getParameter("birthday");
String password = request.getParameter("password");
String confirmPassword = request.getParameter("confirm_password");
String register = request.getParameter("register");
String toLogin = request.getParameter("toLogin");

if (toLogin == null) toLogin = "";
if (register == null) register = "";

if (toLogin.equals("Login")) {
    System.out.println("[Action] Sign In pressed");
    msg.setGeneralWelcome();
    response.sendRedirect("./LoginView.jsp");
} else if (register.equals("Register")) {
    System.out.println("[Action] Register pressed for username=" + username);

    accb.setUsername(username);
    accb.setEmail(email);
    accb.setStringToSqlDate(birthday);
    accb.setPassword(password);
    accb.setConfirm_password(confirmPassword);
    accb.setAdmin(false);

    try {
        if (accb.comparePasswords()) {
            boolean created = accb.insertAccountIfUsernameEmailNotExists();
            if (created) {
                msg.setRegisterSuccessful(username);
                response.sendRedirect("./LoginView.jsp");
            } else {
                if (accb.checkUsernameExist()) {
                    msg.setUsernameAlreadyExists(username);
                    System.out.println("[Account] Registration rejected: username already exists (" + username + ")");
                } else if (accb.checkEmailExist()) {
                    msg.setEmailAlreadyExists(email);
                    System.out.println("[Account] Registration rejected: email already exists");
                }
                response.sendRedirect("./RegView.jsp");
            }
        } else {
            msg.setPasswordMismatch();
            System.out.println("[Account] Registration rejected: password confirmation mismatch");
            response.sendRedirect("./RegView.jsp");
        }
    } catch (SQLException se) {
        se.printStackTrace();
        msg.setGeneralError();
        response.sendRedirect("./RegView.jsp");
    }
} else {
    System.out.println("[Action] No action specified");
    msg.setGeneralWelcome();
    response.sendRedirect("./LoginView.jsp");
}
%>

</body>
</html>
