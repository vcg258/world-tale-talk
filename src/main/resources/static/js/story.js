(function () {
  const root = document.querySelector('[data-story-session]');
  if (!root || !window.SockJS || !window.Stomp) {
    return;
  }

  const sessionId = root.dataset.storySession;
  const mode = root.dataset.storyMode || 'story';
  const topic = mode === 'epilogue' ? `/topic/epilogue/${sessionId}` : `/topic/story/${sessionId}`;
  const destination = mode === 'epilogue' ? `/app/epilogue/${sessionId}` : `/app/story/${sessionId}`;
  const output = document.querySelector('[data-live-story]');
  const status = document.querySelector('[data-socket-status]');
  const form = document.querySelector('[data-story-form]');
  const freeText = document.querySelector('[data-story-input]');
  const sendChoiceButtons = document.querySelectorAll('[data-choice-send]');
  const editChoiceButtons = document.querySelectorAll('[data-choice-edit]');
  const startButton = document.querySelector('[data-choice-start]');

  const setStatus = (text) => {
    if (status) status.textContent = text;
  };

  const setOutput = (text) => {
    if (output) output.textContent = text || '';
  };

  const append = (text) => {
    if (output) output.textContent = `${output.textContent || ''}${text}`;
  };

  const fillInput = (text, shouldFocus) => {
    if (!freeText) return;
    freeText.value = text || '';
    freeText.dispatchEvent(new Event('input', {bubbles: true}));
    if (shouldFocus) {
      freeText.focus();
      freeText.setSelectionRange(freeText.value.length, freeText.value.length);
    }
  };

  const sendFreeText = (content) => {
    const text = (content || freeText.value).trim();
    if (!text || !stomp.connected) return;
    fillInput(text, false);
    setOutput('');
    const payload = mode === 'epilogue' ? {content: text} : {type: 'FREE_TEXT', content: text};
    stomp.send(destination, {}, JSON.stringify(payload));
    freeText.value = '';
  };

  const stomp = Stomp.over(new SockJS('/ws'));
  stomp.debug = null;
  stomp.connect({}, () => {
    setStatus('연결됨');
    stomp.subscribe(topic, (frame) => {
      const payload = JSON.parse(frame.body);
      if (payload.event === 'DELTA') append(payload.value || '');
      if (payload.event === 'ERROR') setStatus(payload.value || '오류가 발생했습니다.');
      if (payload.event === 'COMPLETE') window.setTimeout(() => window.location.reload(), 600);
    });
  }, () => setStatus('연결 실패'));

  if (form) {
    form.addEventListener('submit', (event) => {
      event.preventDefault();
      sendFreeText();
    });
  }

  if (freeText) {
    freeText.addEventListener('keydown', (event) => {
      if (event.key !== 'Enter' || event.shiftKey || event.isComposing) return;
      event.preventDefault();
      sendFreeText();
    });
  }

  sendChoiceButtons.forEach((button) => {
    button.addEventListener('click', () => {
      const text = button.dataset.choiceSend || button.textContent || '';
      fillInput(text, false);
      sendFreeText(text);
    });
  });

  editChoiceButtons.forEach((button) => {
    button.addEventListener('click', () => {
      fillInput(button.dataset.choiceEdit || '', true);
    });
  });

  if (startButton) {
    startButton.addEventListener('click', () => {
      if (!stomp.connected) return;
      setOutput('');
      stomp.send(`/app/story/${sessionId}/start`, {}, '{}');
    });
  }
})();
