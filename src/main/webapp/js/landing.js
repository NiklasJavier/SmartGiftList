'use strict';

document.addEventListener('DOMContentLoaded', () => {
    const questions = document.querySelectorAll('.accordion-question');
    if (questions.length === 0) {
        return;
    }

    questions.forEach((question) => {
        question.addEventListener('click', () => {
            const answer = question.nextElementSibling;
            if (!answer) {
                return;
            }
            const willOpen = !question.classList.contains('is-open');

            questions.forEach((other) => {
                other.classList.remove('is-open');
                other.setAttribute('aria-expanded', 'false');
                if (other.nextElementSibling) {
                    other.nextElementSibling.style.display = 'none';
                }
            });

            if (willOpen) {
                question.classList.add('is-open');
                question.setAttribute('aria-expanded', 'true');
                answer.style.display = 'block';
            }
        });
    });
});
