(function () {
  const rating = document.querySelector('[data-star-rating]');
  const scoreInput = document.querySelector('[data-rating-score]');
  const label = document.querySelector('[data-rating-label]');

  if (!rating || !scoreInput) {
    return;
  }

  const buttons = Array.from(rating.querySelectorAll('[data-score]'));

  const render = (score) => {
    buttons.forEach((button) => {
      const buttonScore = Number(button.dataset.score || 0);
      const filled = buttonScore <= score;
      button.classList.toggle('filled', filled);
      button.setAttribute('aria-checked', String(filled && buttonScore === score));
    });

    if (label) {
      label.textContent = score > 0 ? `${score}점을 선택했습니다.` : '별을 클릭해서 점수를 선택해주세요.';
    }
  };

  const selectScore = (score) => {
    scoreInput.value = String(score);
    render(score);
  };

  buttons.forEach((button) => {
    button.addEventListener('click', () => selectScore(Number(button.dataset.score || 0)));
    button.addEventListener('keydown', (event) => {
      if (event.key !== 'Enter' && event.key !== ' ') return;
      event.preventDefault();
      selectScore(Number(button.dataset.score || 0));
    });
  });

  render(Number(scoreInput.value || 0));
})();
