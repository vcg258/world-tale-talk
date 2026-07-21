(function () {
  const root = document.querySelector('[data-story-session]');
  if (!root) {
    return;
  }

  const sessionId = root.dataset.storySession;
  const mode = root.dataset.storyMode || 'story';
  const shouldAutoStart = root.dataset.storyAutoStart === 'true';
  const topic = mode === 'epilogue' ? `/topic/epilogue/${sessionId}` : `/topic/story/${sessionId}`;
  const destination = mode === 'epilogue' ? `/app/epilogue/${sessionId}` : `/app/story/${sessionId}`;
  const output = document.querySelector('[data-live-story]');
  const form = document.querySelector('[data-story-form]');
  const freeText = document.querySelector('[data-story-input]');
  const sendChoiceButtons = document.querySelectorAll('[data-choice-send]');
  const editChoiceButtons = document.querySelectorAll('[data-choice-edit]');
  const startButton = document.querySelector('[data-choice-start]');
  const chatLog = document.querySelector('[data-chat-log]');
  const submitButton = form ? form.querySelector('button[type="submit"]') : null;
  let stomp = null;
  let hasStartedAutomatically = false;
  let isWaitingForStory = shouldAutoStart;
  let isAwaitingResponse = shouldAutoStart;
  let reconnectTimer = null;
  let reconnectAttempts = 0;

  const setInputLocked = (locked) => {
    isWaitingForStory = locked;
    if (freeText) freeText.disabled = locked;
    if (submitButton) submitButton.disabled = locked;
    sendChoiceButtons.forEach((button) => { button.disabled = locked; });
    editChoiceButtons.forEach((button) => { button.disabled = locked; });
  };

  const setOutput = (text) => {
    if (output) output.textContent = text || '';
  };

  const append = (text) => {
    if (output) output.textContent = `${output.textContent || ''}${text}`;
  };

  const scrollChatToBottom = () => {
    if (!chatLog) return;
    chatLog.scrollTop = chatLog.scrollHeight;
  };

  const showConnectionNotice = () => {
    if (!output || output.textContent.trim()) return;
    output.textContent = '연결이 불안정해 재연결을 시도하고 있습니다. 잠시만 기다려주세요.';
  };

  const scheduleReconnect = () => {
    if (reconnectTimer) return;
    setInputLocked(true);
    showConnectionNotice();
    const delay = Math.min(10000, 1200 + reconnectAttempts * 1200);
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null;
      reconnectAttempts += 1;
      connectSocket();
    }, delay);
  };

  const startStory = () => {
    if (mode !== 'story' || !stomp || !stomp.connected || hasStartedAutomatically) return;
    hasStartedAutomatically = true;
    isAwaitingResponse = true;
    setInputLocked(true);
    setOutput('이야기를 준비하고 있습니다...');
    stomp.send(`/app/story/${sessionId}/start`, {}, '{}');
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
    const text = (content || (freeText ? freeText.value : '')).trim();
    if (!text || !stomp || !stomp.connected || isWaitingForStory) return;
    fillInput(text, false);
    isAwaitingResponse = true;
    setInputLocked(true);
    setOutput('');
    const payload = mode === 'epilogue' ? {content: text} : {type: 'FREE_TEXT', content: text};
    stomp.send(destination, {}, JSON.stringify(payload));
    if (freeText) freeText.value = '';
  };

  const handleSocketError = (message) => {
    isAwaitingResponse = false;
    setInputLocked(false);
    if (message === '권한이 없습니다.') {
      window.alert(message);
      return;
    }
    window.alert(message || '오류가 발생했습니다.');
  };

  function connectSocket() {
    if (!window.SockJS || !window.Stomp) {
      setInputLocked(true);
      showConnectionNotice();
      scheduleReconnect();
      return;
    }

    stomp = Stomp.over(new SockJS('/ws'));
    stomp.debug = null;
    stomp.connect({}, () => {
      reconnectAttempts = 0;
      if (!isAwaitingResponse) setInputLocked(false);
      stomp.subscribe(topic, (frame) => {
        const payload = JSON.parse(frame.body);
        if (payload.event === 'DELTA') append(payload.value || '');
        if (payload.event === 'ERROR') handleSocketError(payload.value);
        if (payload.event === 'COMPLETE') window.setTimeout(() => window.location.reload(), 600);
      });
      if (shouldAutoStart) startStory();
    }, scheduleReconnect);
  }

  setInputLocked(shouldAutoStart);
  scrollChatToBottom();
  connectSocket();

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
      if (isWaitingForStory) return;
      fillInput(button.dataset.choiceEdit || '', true);
    });
  });

  if (startButton) {
    startButton.addEventListener('click', startStory);
  }
})();
