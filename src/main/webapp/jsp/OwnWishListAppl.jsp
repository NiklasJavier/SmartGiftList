<%@page import="dev.smartgiftlist.jdbc.NoConnectionException"%>
<%@page import="java.sql.Date"%>
<%@page import="dev.smartgiftlist.beans.LoginBean"%>
<%@page import="dev.smartgiftlist.beans.MessageBean"%>
<%@page import="dev.smartgiftlist.beans.OwnWishListBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%!
    private static Date parseSqlDateOrDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return Date.valueOf("0001-01-01");
        }
        try {
            return Date.valueOf(raw.trim());
        } catch (IllegalArgumentException ex) {
            return Date.valueOf("0001-01-01");
        }
    }

    private static double parseDoubleOrDefault(String raw, double fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static int parseIntOrDefault(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>SmartGiftList - Wishlist Handler</title>
</head>
<body>
<jsp:useBean id="loginb" class="dev.smartgiftlist.beans.LoginBean" scope="session"/>
<jsp:useBean id="msg" class="dev.smartgiftlist.beans.MessageBean" scope="session"/>
<jsp:useBean id="owlb" class="dev.smartgiftlist.beans.OwnWishListBean" scope="session"/>

<%
request.setCharacterEncoding("UTF-8");

if (!loginb.isLoggedIn()) {
    response.sendRedirect("./LoginView.jsp");
    return;
}

// Parameters for creating a wishlist
String list_icon = request.getParameter("list_icon");
String wishlist_name = request.getParameter("wishlist_name");
String occasion = request.getParameter("occasion");
String date = request.getParameter("date");
String description = request.getParameter("description");
String links = request.getParameter("links");
String create_wishlist = request.getParameter("create_wishlist");

if (create_wishlist == null) create_wishlist = "";
if (occasion == null) occasion = "";
if (description == null) description = "";
if (links == null) links = "";
if (list_icon == null || list_icon.isBlank()) list_icon = "../img/anlaesse/PaperList.webp";

// Parameters for selecting a wishlist
String netId = request.getParameter("netId");
String showWishlist = request.getParameter("showWishlist");
if (netId == null) netId = "";
if (showWishlist == null) showWishlist = "";

// Parameters for updating or deleting the active wishlist
String owluWislistEvent = request.getParameter("owluWislistEvent");
String owluWishlistDescription = request.getParameter("owluWishlistDescription");
String owluWishlistDate = request.getParameter("owluWishlistDate");
String owluWishlistLinks = request.getParameter("owluWishlistLinks");
String owluWishlistDeleteButton = request.getParameter("owluWishlistDeleteButton");
String owluWishlistSaveButton = request.getParameter("owluWishlistSaveButton");
String owluWishListIcon = request.getParameter("owluWishListIcon");

if (owluWishlistDeleteButton == null) owluWishlistDeleteButton = "";
if (owluWishlistSaveButton == null) owluWishlistSaveButton = "";
if (owluWishlistLinks == null) owluWishlistLinks = "";
if (owluWishlistDescription == null) owluWishlistDescription = "";
if (owluWislistEvent == null) owluWislistEvent = "";
if (owluWishListIcon == null || owluWishListIcon.isBlank()) owluWishListIcon = "../img/anlaesse/PaperList.webp";

// Parameters for creating a gift
String owlpEditListIcon = request.getParameter("owlpEditListIcon");
String owlpEditGiftName = request.getParameter("owlpEditGiftName");
String owlpEditGroupGiftAmount = request.getParameter("owlpEditGroupGiftAmount");
boolean owlpEditSurpriseGift = request.getParameter("owlpEditSurpriseGift") != null;
String owlpEditDescription = request.getParameter("owlpEditDescription");
String owlpEditPrice = request.getParameter("owlpEditPrice");
String owlpEditLink = request.getParameter("owlpEditLink");
String owlpEditSubmitButton = request.getParameter("owlpEditSubmitButton");

if (owlpEditListIcon == null || owlpEditListIcon.isBlank()) owlpEditListIcon = "../img/anlaesse/PaperList.webp";
if (owlpEditGiftName == null) owlpEditGiftName = "";
if (owlpEditDescription == null) owlpEditDescription = "";
if (owlpEditLink == null) owlpEditLink = "";
if (owlpEditSubmitButton == null) owlpEditSubmitButton = "";

// Parameters for gift table actions
String owlpGiftIDForSelect = request.getParameter("owlpGiftIDForSelect");
String owlpButtonForDetails = request.getParameter("owlpButtonForDetails");
String owlpButtonForDelete = request.getParameter("owlpButtonForDelete");
String owlpButtonForAddGift = request.getParameter("owlpButtonForAddGift");

if (owlpGiftIDForSelect == null || owlpGiftIDForSelect.isBlank()) owlpGiftIDForSelect = "0";
if (owlpButtonForDetails == null) owlpButtonForDetails = "";
if (owlpButtonForDelete == null) owlpButtonForDelete = "";
if (owlpButtonForAddGift == null) owlpButtonForAddGift = "";

// Parameters for claiming/releasing a gift
String OwlpDeBtnTakeover = request.getParameter("OwlpDeBtnTakeover");
String OwlpDeBtnRelease = request.getParameter("OwlpDeBtnRelease");
String OwlpDeGiftID = request.getParameter("OwlpDeGiftID");

if (OwlpDeBtnTakeover == null) OwlpDeBtnTakeover = "";
if (OwlpDeBtnRelease == null) OwlpDeBtnRelease = "";
if (OwlpDeGiftID == null || OwlpDeGiftID.isBlank()) OwlpDeGiftID = "0";

// Parameters for wishlist discussion chat
String ChatUserButton = request.getParameter("ChatUserButton");
String ChatUserMessage = request.getParameter("ChatUserMessage");

if (ChatUserButton == null) ChatUserButton = "";
if (ChatUserMessage == null) ChatUserMessage = "";

// Parameters for connecting via NetID
String NetIDConnect = request.getParameter("NetIDConnect");
String wishlistConnect = request.getParameter("wishlistConnect");

if (NetIDConnect == null || NetIDConnect.isBlank()) NetIDConnect = "0";
if (wishlistConnect == null) wishlistConnect = "";

// Dispatch action
if (create_wishlist.equals("Create Wishlist")) {
    System.out.println("[Action] Create wishlist requested");
    try {
        owlb.createWishlist(wishlist_name, occasion, parseSqlDateOrDefault(date), description, links, list_icon);
    } catch (NoConnectionException e) {
        e.printStackTrace();
    }
    owlb.fetchAndSetLastNetIDForUser();
    owlb.getWishlistByNetID();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (showWishlist.equals("showWishlist")) {
    System.out.println("[Action] Select wishlist requested (netId=" + netId + ")");
    owlb.setNetID(netId);
    owlb.getWishlistByNetID();
    msg.setSuccessfulSelectWishlistMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owluWishlistDeleteButton.equals("delete")) {
    System.out.println("[Action] Delete/leave wishlist requested");
    owlb.deleteWishlist();
    owlb.fetchAndSetLastNetIDForUser();
    owlb.getWishlistByNetID();
    msg.setRemoveWishlistMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owluWishlistSaveButton.equals("save")) {
    System.out.println("[Action] Save wishlist changes requested");
    owlb.updateWishlistDetails(owluWishlistLinks, owluWislistEvent, owluWishlistDescription, owluWishListIcon, parseSqlDateOrDefault(owluWishlistDate));
    owlb.getWishlistByNetID();
    msg.setUpdateWishlistMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owlpEditSubmitButton.equals("Save Gift")) {
    System.out.println("[Action] Save gift requested");
    owlb.addGift(
            owlpEditGiftName,
            owlpEditDescription,
            owlpEditLink,
            owlpEditListIcon,
            parseDoubleOrDefault(owlpEditPrice, 0.0),
            parseIntOrDefault(owlpEditGroupGiftAmount, 1),
            owlpEditSurpriseGift
    );
    msg.setAddGiftToWishlistMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owlpButtonForDetails.equals("owlpButtonForDetails")) {
    int selectedGiftId = parseIntOrDefault(owlpGiftIDForSelect, 0);
    owlb.setGiftID(selectedGiftId);
    owlb.getGiftByGiftID();
    if (selectedGiftId == 0) {
        msg.setNoGiftSelectedMessage();
    } else {
        msg.setSelectedGiftMessage();
    }
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owlpButtonForDelete.equals("owlpButtonForDelete")) {
    int selectedGiftId = parseIntOrDefault(owlpGiftIDForSelect, 0);
    owlb.setGiftID(selectedGiftId);
    owlb.deleteGift();
    msg.setDeleteGiftMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (owlpButtonForAddGift.equals("owlpButtonForAddGift")) {
    response.sendRedirect("./OwnWishListView.jsp");
} else if (OwlpDeBtnTakeover.equals("OwlpDeBtnTakeover")) {
    owlb.setGiftID(parseIntOrDefault(OwlpDeGiftID, 0));
    boolean claimed = owlb.assignUserToGift();
    if (claimed) {
        msg.setClaimGiftMessage();
    } else {
        msg.setClaimErrorGiftMessage();
    }
    response.sendRedirect("./OwnWishListView.jsp");
} else if (OwlpDeBtnRelease.equals("OwlpDeBtnRelease")) {
    owlb.setGiftID(parseIntOrDefault(OwlpDeGiftID, 0));
    owlb.removeUserFromGift();
    msg.setRemoveGiftMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (ChatUserButton.equals("sendMessage")) {
    owlb.addNormalMessage(ChatUserMessage);
    response.sendRedirect("./OwnWishListView.jsp");
} else if (ChatUserButton.equals("loadMessage")) {
    response.sendRedirect("./OwnWishListView.jsp");
} else if (ChatUserButton.equals("deleteMessage")) {
    owlb.withdrawLastUserMessage();
    response.sendRedirect("./OwnWishListView.jsp");
} else if (wishlistConnect.equals("Connect Wishlist")) {
    System.out.println("[Action] Connect wishlist requested (NetID=" + NetIDConnect + ")");
    owlb.setNetID(NetIDConnect);
    boolean connected = owlb.assignUserToWishlist();
    if (connected) {
        msg.setLinkWishlistToUserMessage();
    } else {
        msg.setLinkErrorWishlistToUserMessage();
    }
    owlb.getWishlistByNetID();
    response.sendRedirect("./OwnWishListView.jsp");
} else {
    response.sendRedirect("./OwnWishListView.jsp");
}
%>

</body>
</html>
