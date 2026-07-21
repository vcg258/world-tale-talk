(function () {
  const editor = document.querySelector('[data-stat-editor]');
  if (!editor) return;

  const clampFilledValue = (value) => Math.max(0, Math.min(10, Number.isFinite(value) ? value : 0));

  const renderBar = (statName) => {
    const input = editor.querySelector(`[data-stat-input="${statName}"]`);
    const bar = editor.querySelector(`[data-stat-bar="${statName}"]`);
    if (!input || !bar) return;

    const value = clampFilledValue(parseInt(input.value, 10));
    bar.querySelectorAll('[data-stat-slot]').forEach((slot) => {
      const slotValue = Number(slot.dataset.statSlot);
      slot.classList.toggle('filled', slotValue <= value);
      slot.setAttribute('aria-pressed', slotValue <= value ? 'true' : 'false');
    });
  };

  editor.querySelectorAll('[data-stat-input]').forEach((input) => {
    const statName = input.dataset.statInput;

    input.addEventListener('input', () => renderBar(statName));
    input.addEventListener('blur', () => {
      const parsed = parseInt(input.value, 10);
      if (!Number.isFinite(parsed) || parsed < 0) input.value = 0;
      renderBar(statName);
    });

    renderBar(statName);
  });

  editor.querySelectorAll('[data-stat-bar]').forEach((bar) => {
    const statName = bar.dataset.statBar;
    const input = editor.querySelector(`[data-stat-input="${statName}"]`);
    if (!input) return;

    bar.addEventListener('click', (event) => {
      const slot = event.target.closest('[data-stat-slot]');
      if (!slot) return;
      input.value = slot.dataset.statSlot;
      input.dispatchEvent(new Event('input', {bubbles: true}));
      input.focus();
    });
  });
})();
