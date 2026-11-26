function toggleAccordion(element) {
    var answer = element.nextElementSibling;
    if (answer.style.display === "block") {
        answer.style.display = "none";
        element.classList.remove('is-open');
    } else {
        closeAll();
        answer.style.display = "block";
        element.classList.add('is-open');
    }
}

function closeAll() {
    document.querySelectorAll('.accordion-answer').forEach(function(answer) {
        answer.style.display = 'none';
    });
    document.querySelectorAll('.accordion-question').forEach(function(question) {
        question.classList.remove('is-open');
    });
}
