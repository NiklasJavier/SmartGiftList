document.addEventListener('DOMContentLoaded', function() {
    var imageSelector3 = document.getElementById('owlprofil-profil-icon');
    var imagePreview3 = document.getElementById('owlprofil-profil-preview');

    if (imageSelector3 && imagePreview3) {
        imageSelector3.addEventListener('change', function() {
            imagePreview3.src = this.value;
        });
    }
});

function checkInput2(myForm) {
    var myPassword = myForm.password.value;
    var confirmPassword = myForm.confirm_password.value;

    var errorPassword = checkPassword(myPassword);
    var errorPasswordNotEqual = checkPasswordEqual(myPassword, confirmPassword);

    var editPasswordMsg = document.getElementById("msgPassword");
    editPasswordMsg.innerText = errorPassword;
    editPasswordMsg.className = setClassForInputMsg(errorPassword);

    var editPasswordNotEqualMsg = document.getElementById("msgPasswordNotEqual");
    editPasswordNotEqualMsg.innerText = errorPasswordNotEqual;
    editPasswordNotEqualMsg.className = setClassForInputMsg(errorPasswordNotEqual);

    return !(errorPassword || errorPasswordNotEqual);
}

function checkPassword(myPassword) {
    if (myPassword && myPassword.length >= 6) {
        return "";
    }
    return "Password must be at least 6 characters long.";
}

function checkPasswordEqual(passOne, passTwo) {
    if (passOne === passTwo) {
        return "";
    }
    return "Passwords must match.";
}

function setClassForInputMsg(value) {
    return value ? "message-red" : "";
}
