function restorePopupState() {
    var popup3 = document.getElementById('Owlpde-edit-gift-popup');
    if (popup3) {
        var popupState = localStorage.getItem('popup3State');
        popup3.style.display = (popupState === 'open') ? 'flex' : 'none';
    }
}

function saveScrollPosition() {
    var scrollPosition = window.pageYOffset || document.documentElement.scrollTop;
    localStorage.setItem('lastScrollPosition', scrollPosition.toString());
}

function restoreScrollPosition() {
    var savedPosition = localStorage.getItem('lastScrollPosition');
    if (savedPosition) {
        window.scrollTo(0, parseInt(savedPosition, 10));
    }
}

document.addEventListener('DOMContentLoaded', function() {
    restorePopupState();
    restoreScrollPosition();
});

window.addEventListener('pageshow', restoreScrollPosition);

document.addEventListener('DOMContentLoaded', function() {
    var createWishlistButton = document.getElementById('owl-create-wishlist-button');
    var popup = document.getElementById('owl-create-wishlist-popup');
    var closeButton = document.querySelector('.owl-create-close-button');

    var createWishlistButtonConnect = document.getElementById('owl-connect-wishlist-button');
    var popupConnect = document.getElementById('owl-connect-wishlist-popup');
    var closeButtonConnect = document.querySelector('.owl-connect-close-button');

    var createWishlistButton2 = document.getElementById('owlc-card-content-add');
    var createWishlistButtonConnect2 = document.getElementById('owlc-card-content-connect');

    var imageSelector = document.getElementById('owl-create-list-icon');
    var imagePreview = document.getElementById('list-image-preview');

    var checkboxes = document.querySelectorAll('.owlp-select-checkbox');
    var tableRows = document.querySelectorAll('.owlp-table tbody tr');

    var chatMessagesContainer = document.querySelector('.chat-messages');

    var addButton2 = document.getElementById('owlp-add-btn');
    var popup2 = document.getElementById('owlpedit-create-gift-popup');
    var closeButton2 = document.querySelector('.owlpedit-create-close-button');

    var imageSelector2 = document.getElementById('owlpedit-list-icon');
    var imagePreview2 = document.getElementById('owlpedit-image-preview');

    var giftTypeSelect = document.getElementById('Owlpde-gift-type');
    var groupGiftAmountContainer = document.getElementById('Owlpde-group-gift-amount-container');

    var detailsButton3 = document.getElementById('owlp-details-btn');
    var popup3 = document.getElementById('Owlpde-edit-gift-popup');
    var closeButton3 = document.querySelector('.Owlpde-create-close-button');

    var giftTypeSelectCreate = document.getElementById('owlpedit-gift-type');
    var groupGiftAmountInputCreate = document.getElementById('owlpedit-group-gift-amount');

    var personaSelect = document.getElementById('owlu-persona');
    var personaImage = document.getElementById('owluIconImage');

    if (detailsButton3 && popup3) {
        detailsButton3.addEventListener('click', function() {
            popup3.style.display = 'flex';
            localStorage.setItem('popup3State', 'open');
        });
    }

    if (closeButton3 && popup3) {
        closeButton3.addEventListener('click', function() {
            popup3.style.display = 'none';
            localStorage.setItem('popup3State', 'closed');
        });
    }

    if (popup3) {
        window.addEventListener('click', function(event) {
            if (event.target === popup3) {
                popup3.style.display = 'none';
                localStorage.setItem('popup3State', 'closed');
            }
        });
    }

    if (personaSelect && personaImage) {
        personaSelect.addEventListener('change', function() {
            personaImage.src = this.value;
        });
    }

    if (giftTypeSelect && groupGiftAmountContainer) {
        giftTypeSelect.addEventListener('change', function() {
            groupGiftAmountContainer.style.display = (this.value === 'group') ? 'block' : 'none';
        });
    }

    if (imageSelector2 && imagePreview2) {
        imageSelector2.addEventListener('change', function() {
            imagePreview2.src = this.value;
        });
    }

    if (addButton2 && popup2) {
        addButton2.addEventListener('click', function() {
            popup2.style.display = 'flex';
        });
    }

    if (closeButton2 && popup2) {
        closeButton2.addEventListener('click', function() {
            popup2.style.display = 'none';
        });
    }

    if (popup2) {
        window.addEventListener('click', function(event) {
            if (event.target === popup2) {
                popup2.style.display = 'none';
            }
        });
    }

    if (createWishlistButton && popup) {
        createWishlistButton.addEventListener('click', function() {
            popup.style.display = 'flex';
        });
    }

    if (createWishlistButtonConnect && popupConnect) {
        createWishlistButtonConnect.addEventListener('click', function() {
            popupConnect.style.display = 'flex';
        });
    }

    if (closeButtonConnect && popupConnect) {
        closeButtonConnect.addEventListener('click', function() {
            popupConnect.style.display = 'none';
        });
    }

    if (popupConnect) {
        window.addEventListener('click', function(event) {
            if (event.target === popupConnect) {
                popupConnect.style.display = 'none';
            }
        });
    }

    if (createWishlistButton2 && popup) {
        createWishlistButton2.addEventListener('click', function() {
            popup.style.display = 'flex';
        });
    }

    if (createWishlistButtonConnect2 && popupConnect) {
        createWishlistButtonConnect2.addEventListener('click', function() {
            popupConnect.style.display = 'flex';
        });
    }

    if (closeButton && popup) {
        closeButton.addEventListener('click', function() {
            popup.style.display = 'none';
        });
    }

    if (popup) {
        window.addEventListener('click', function(event) {
            if (event.target === popup) {
                popup.style.display = 'none';
            }
        });
    }

    if (imageSelector && imagePreview) {
        imageSelector.addEventListener('change', function() {
            imagePreview.src = this.value;
        });
    }

    checkboxes.forEach(function(checkbox) {
        checkbox.addEventListener('change', function() {
            checkboxes.forEach(function(otherCheckbox) {
                if (otherCheckbox !== checkbox) {
                    otherCheckbox.checked = false;
                }
            });
        });
    });

    tableRows.forEach(function(row) {
        row.addEventListener('click', function(event) {
            var checkbox = row.querySelector('.owlp-select-checkbox');
            if (!checkbox) {
                return;
            }
            if (event.target !== checkbox) {
                checkbox.checked = !checkbox.checked;
            }
            if (checkbox.checked) {
                tableRows.forEach(function(otherRow) {
                    if (otherRow !== row) {
                        var otherBox = otherRow.querySelector('.owlp-select-checkbox');
                        if (otherBox) {
                            otherBox.checked = false;
                        }
                    }
                });
            }
        });

        var rowData = row.dataset;
        if (rowData.status === 'single') {
            row.style.backgroundColor = '#90ee90'; // Light green
        } else if (rowData.status === 'claimed') {
            row.style.backgroundColor = '#ffcccb'; // Light red
        } else if (rowData.status === 'group') {
            row.style.backgroundColor = '#ffff99'; // Light yellow
        } else if (rowData.status === 'surprise') {
            row.style.backgroundColor = '#ffccff'; // Light pink
        }
    });

    if (chatMessagesContainer) {
        chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
    }

    document.querySelectorAll('.readonly-field').forEach(function(field) {
        field.addEventListener('click', function() {
            this.select();
            document.execCommand('copy');
        });
    });

    function handleGiftTypeChange() {
        if (!giftTypeSelectCreate || !groupGiftAmountInputCreate) {
            return;
        }
        if (giftTypeSelectCreate.value === 'single') {
            groupGiftAmountInputCreate.value = '1';
            groupGiftAmountInputCreate.readOnly = true;
            groupGiftAmountInputCreate.style.backgroundColor = '#f3f3f3';
        } else {
            groupGiftAmountInputCreate.readOnly = false;
            groupGiftAmountInputCreate.style.backgroundColor = '';
        }
    }

    if (giftTypeSelectCreate && groupGiftAmountInputCreate) {
        giftTypeSelectCreate.addEventListener('change', handleGiftTypeChange);
        handleGiftTypeChange();
    }
});

function checkInputLogin(myForm) {
    return Boolean(myForm);
}

function checkInput(myForm) {
    if (document.buttonClicked === "Login") {
        return true;
    }
    if (!myForm) {
        return false;
    }

    var myUsername = myForm.username.value;
    var myPassword = myForm.password.value;
    var confirmPassword = myForm.confirm_password.value;
    var myBirthday = myForm.birthday.value;
    var myEmail = myForm.email.value;

    var errorUsername = checkUserid(myUsername);
    var errorPassword = checkPassword(myPassword);
    var errorPasswordNotEqual = errorPassword === "" ? checkPasswordEqual(myPassword, confirmPassword) : "";
    var errorBirthday = checkAge(myBirthday);
    var errorEmail = checkEmail(myEmail);

    var editUsernameMsg = document.getElementById("msgUsername");
    if (editUsernameMsg) {
        editUsernameMsg.innerText = errorUsername;
        editUsernameMsg.className = setClassForInputMsg(errorUsername);
    }

    var editPasswordMsg = document.getElementById("msgPassword");
    if (editPasswordMsg) {
        editPasswordMsg.innerText = errorPassword;
        editPasswordMsg.className = setClassForInputMsg(errorPassword);
    }

    var editPasswordNotEqualMsg = document.getElementById("msgPasswordNotEqual");
    if (editPasswordNotEqualMsg) {
        editPasswordNotEqualMsg.innerText = errorPasswordNotEqual;
        editPasswordNotEqualMsg.className = setClassForInputMsg(errorPasswordNotEqual);
    }

    var editBirthdayMsg = document.getElementById("msgBirthday");
    if (editBirthdayMsg) {
        editBirthdayMsg.innerText = errorBirthday;
        editBirthdayMsg.className = setClassForInputMsg(errorBirthday);
    }

    var editEmailMsg = document.getElementById("msgEmail");
    if (editEmailMsg) {
        editEmailMsg.innerText = errorEmail;
        editEmailMsg.className = setClassForInputMsg(errorEmail);
    }

    return !(errorUsername || errorPassword || errorBirthday || errorEmail || errorPasswordNotEqual);
}

function setButtonClicked(myButton) {
    document.buttonClicked = myButton.value;
}

function checkEmail(myEmail) {
    return myEmail ? "" : "Please enter an email address.";
}

function checkAge(myBirthday) {
    return myBirthday ? "" : "Please enter your date of birth.";
}

function checkPassword(myPassword) {
    if (myPassword && myPassword.length >= 6) {
        return "";
    }
    return "Password must be at least 6 characters long.";
}

function checkUserid(myUsername) {
    return myUsername ? "" : "Please enter a username.";
}

function checkPasswordEqual(passOne, passTwo) {
    if (passOne && passTwo && passOne === passTwo) {
        return "";
    }
    return "Passwords must match.";
}

function setClassForInputMsg(value) {
    return value === "" ? "" : "message-red";
}
