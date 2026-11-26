<%@page import="dev.smartgiftlist.beans.LoginBean"%>
<%@page import="dev.smartgiftlist.beans.MessageBean"%>
<%@page import="dev.smartgiftlist.beans.ProfilManager"%>
<%@page import="dev.smartgiftlist.beans.OwnWishListBean"%>
<%@page import="dev.smartgiftlist.beans.AccountBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList - Profile Handler</title>
</head>
<body>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>
<jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session"/>
<jsp:useBean id="profilb" class="dev.smartgiftlist.beans.ProfilManager" scope="session"/>
<jsp:useBean id="owlb" class="dev.smartgiftlist.beans.OwnWishListBean" scope="session"/>
<jsp:useBean id="acc" class="dev.smartgiftlist.beans.AccountBean" scope="session"/>
<%
request.setCharacterEncoding("UTF-8");

if (!loginb.isLoggedIn()) {
    response.sendRedirect("./LoginView.jsp");
    return;
}

String profilIcon = request.getParameter("listIcon");
String profilName = request.getParameter("profilUsername");
String password = request.getParameter("password");
String btnSave = request.getParameter("ProfilBtnSave");
String btnDelete = request.getParameter("delete");

if (btnSave == null) btnSave = "";
if (btnDelete == null) btnDelete = "";

if (btnSave.equals("Save")) {
    boolean updated = owlb.changeProfil(password, profilName, profilIcon);
    if (updated) {
        msg.setProfileDataOverwrittenMessage();
        loginb.setLoggedIn(false);
        System.out.println("[DB] Profile settings updated for user");
        owlb.setSessionLogoutParameter();
        response.sendRedirect("./LoginView.jsp");
    } else {
        msg.setUsernameExistsMessage();
        response.sendRedirect("./ProfilView.jsp");
    }
} else if (btnDelete.equals("Delete Account")) {
    boolean deleted = owlb.deleteUserById();
    if (deleted) {
        System.out.println("[Action] Account deletion requested");
        msg.setAccountDeletedMessage();
        loginb.setLoggedIn(false);
        owlb.setSessionLogoutParameter();
        System.out.println("[DB] User removed from database");
        response.sendRedirect("./LoginView.jsp");
    } else {
        msg.setAccountDeletionError();
        response.sendRedirect("./ProfilView.jsp");
    }
} else {
    response.sendRedirect("./LoginView.jsp");
}
%>
</body>
</html>
