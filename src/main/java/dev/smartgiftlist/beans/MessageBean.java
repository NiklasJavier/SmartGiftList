package dev.smartgiftlist.beans;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Session bean providing status and feedback banners for JSP views.
 */
public class MessageBean {

    String infoMessage;
    String actionMessage;

    public MessageBean() {
        this.setGeneralWelcome();
    }

    public String getMessageHtml() {
        String html = "";
        html += "<span class='message-yellowUp'>" + this.getInfoMessage() + "</span>\n";
        html += "<span class='message-yellowDown'>" + this.getActionMessage() + "</span>\n";
        return html;
    }

    public String getPortalMessageHtml() {
        String html = "";
        html += "<span class='message-yellowDown'>" + this.getActionMessage() + "</span>\n";
        return html;
    }

    public void setGeneralWelcome() {
        this.infoMessage = "Welcome to SmartGiftList.";
        this.actionMessage = "Please sign in with your credentials to access your account.";
    }

    public void setRegistrationWelcome() {
        this.infoMessage = "Welcome to SmartGiftList.";
        this.actionMessage = "Complete the registration form below to create a new account.";
    }

    public void setPortalWelcome() {
        this.setInfoMessage("");
        this.setActionMessage("Please select an application view.");
    }

    public void setOWLWelcome() {
        this.setInfoMessage("");
        this.setActionMessage("Create and manage your personal and shared wishlists here.");
    }

    public void setPortalWelcome(String username) {
        this.setInfoMessage("");
        this.setActionMessage("You have signed in successfully, " + HtmlUtil.escape(username) + ".");
    }

    public void setLoginSuccessful() {
        this.setInfoMessage("Signed in successfully.");
        this.setActionMessage("You now have access to your wishlists.");
    }

    public void setLogoutSuccessful() {
        this.setInfoMessage("You have been signed out.");
        this.setActionMessage("Sign in again at any time.");
    }

    public void setLoginFailed() {
        this.setInfoMessage("Sign-in failed.");
        this.setActionMessage("Please check your username and password and try again.");
    }

    public void setRegisterSuccessful(String username) {
        this.setInfoMessage("Welcome, " + HtmlUtil.escape(username) + ".");
        this.setActionMessage("Your account has been created. Sign in below to continue.");
    }

    public void setPasswordMismatch() {
        this.setInfoMessage("Passwords do not match.");
        this.setActionMessage("Please enter identical passwords of at least 6 characters.");
    }

    public void setUsernameAlreadyExists(String username) {
        this.setInfoMessage("Username '" + HtmlUtil.escape(username) + "' is already taken.");
        this.setActionMessage("Please choose a different username.");
    }

    public void setEmailAlreadyExists(String email) {
        this.setInfoMessage("Email address '" + HtmlUtil.escape(email) + "' is already registered.");
        this.setActionMessage("Please use a different email address.");
    }

    public void setGeneralError() {
        this.setInfoMessage("An unexpected error occurred.");
        this.setActionMessage("Please try again or contact an administrator.");
    }

    public void setProfilMessage() {
        this.setInfoMessage("Profile Settings");
        this.setActionMessage("Update your username, password, or avatar below.");
    }

    /* ----- Wishlist & Gift Feedback Messages ----- */

    public void setSuccessfulSelectWishlistMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Wishlist selected.");
    }

    public void setRemoveWishlistMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Wishlist removed.");
    }

    public void setUpdateWishlistMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Wishlist updated.");
    }

    public void setDeleteGiftMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Gift removed from the wishlist.");
    }

    public void setSelectedGiftMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Gift selected.");
    }

    public void setNoGiftSelectedMessage() {
        this.setInfoMessage("");
        this.setActionMessage("No gift was selected.");
    }

    public void setAddGiftToWishlistMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Gift added to the wishlist.");
    }

    public void setClaimGiftMessage() {
        this.setInfoMessage("");
        this.setActionMessage("You have claimed this gift.");
    }

    public void setRemoveGiftMessage() {
        this.setInfoMessage("");
        this.setActionMessage("You have released your claim on this gift.");
    }

    public void setClaimErrorGiftMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Unable to claim this gift (already full or already claimed by you).");
    }

    public void setLinkWishlistToUserMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Wishlist connected to your account.");
    }

    public void setLinkErrorWishlistToUserMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Unable to connect this wishlist to your account.");
    }

    public void setUsernameExistsMessage() {
        this.setInfoMessage("");
        this.setActionMessage("This username is already taken. Please choose another name.");
    }

    public void setProfileDataOverwrittenMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Profile updated. Please sign in again with your new credentials.");
    }

    public void setAccountDeletedMessage() {
        this.setInfoMessage("");
        this.setActionMessage("Your account has been deleted. Create a new account at any time.");
    }

    public void setAccountDeletionError() {
        this.setInfoMessage("");
        this.setActionMessage("Your account could not be deleted due to an error.");
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }
}
