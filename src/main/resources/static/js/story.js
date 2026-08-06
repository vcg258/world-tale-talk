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
  const choiceList = document.querySelector('[data-choice-list]');
  const startButton = document.querySelector('[data-choice-start]');
  const chatLog = document.querySelector('[data-chat-log]');
  const submitButton = form ? form.querySelector('button[type="submit"]') : null;
  const progressValue = document.querySelector('[data-progress-value]');
  const progressTurn = document.querySelector('[data-progress-turn]');
  const progressFill = document.querySelector('[data-progress-fill]');
  const inventoryList = document.querySelector('[data-inventory-list]');
  const inventoryEmpty = document.querySelector('[data-inventory-empty]');
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
  const targetTurn = Number(root.dataset.targetTurn || 0) || null;
  let stomp = null;
  let hasStartedAutomatically = false;
  let isWaitingForStory = shouldAutoStart;
  let isAwaitingResponse = shouldAutoStart;
  let reconnectTimer = null;
  let reconnectAttempts = 0;
  let currentTurn = Number(root.dataset.currentTurn || 0);

  const readCurrentTurn = () => {
    if (!progressTurn) return currentTurn;
    const match = (progressTurn.textContent || '').match(/\d+/);
    return match ? Number(match[0]) : 0;
  };

  currentTurn = readCurrentTurn();

  const setInputLocked = (locked) => {
    isWaitingForStory = locked;
    if (freeText) freeText.disabled = locked;
    if (submitButton) submitButton.disabled = locked;
    document.querySelectorAll('[data-choice-send], [data-choice-edit]').forEach((button) => {
      button.disabled = locked;
    });
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

  const appendChatMessage = (role, turn, content) => {
    if (!chatLog || !content || !content.trim()) return;

    const message = document.createElement('div');
    message.className = role === 'USER' ? 'message user' : 'message';

    const meta = document.createElement('div');
    meta.className = 'meta';
    const roleNode = document.createElement('span');
    roleNode.textContent = role;
    const turnNode = document.createElement('span');
    turnNode.textContent = `턴 ${turn}`;
    meta.append(roleNode, turnNode);

    const body = document.createElement('p');
    body.textContent = content;

    message.append(meta, body);
    chatLog.appendChild(message);
    scrollChatToBottom();
  };

  const moveCurrentStoryToHistory = () => {
    if (!output) return;
    const currentStory = output.textContent || '';
    if (!currentStory.trim() || currentStory === '이야기를 준비하고 있습니다...') return;
    appendChatMessage('ASSISTANT', currentTurn, currentStory);
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

  const sendFreeText = (content, choiceIndex) => {
    const text = (content || (freeText ? freeText.value : '')).trim();
    if (!text || !stomp || !stomp.connected || isWaitingForStory) return;
    fillInput(text, false);
    moveCurrentStoryToHistory();
    const nextTurn = mode === 'story' ? currentTurn + 1 : currentTurn;
    appendChatMessage('USER', nextTurn, text);
    isAwaitingResponse = true;
    setInputLocked(true);
    setOutput('');
    if (choiceList) choiceList.innerHTML = '';
    const payload = mode === 'epilogue'
      ? {content: text}
      : {type: choiceIndex ? 'CHOICE' : 'FREE_TEXT', content: text, choiceIndex: choiceIndex || null};
    stomp.send(destination, {}, JSON.stringify(payload));
    if (freeText) freeText.value = '';
  };

  const formatStatPreview = (statChanges) => {
    if (!statChanges) return '스탯 변화 없음';
    const labels = {wisdom: '지혜', strength: '힘', agility: '민첩', charisma: '매력', luck: '행운'};
    const parts = Object.keys(labels)
      .map((key) => [labels[key], Number(statChanges[key] || 0)])
      .filter(([, value]) => value !== 0)
      .map(([label, value]) => `${label} ${value > 0 ? '+' : ''}${value}`);
    return parts.length ? parts.join(' / ') : '스탯 변화 없음';
  };

  const renderChoices = (choices, statChangesByChoice) => {
    if (!choiceList) return;
    choiceList.innerHTML = '';
    if (!Array.isArray(choices) || !choices.length) return;

    choices.forEach((choice, index) => {
      const item = document.createElement('div');
      item.className = 'choice-item';

      const sendButton = document.createElement('button');
      sendButton.className = 'choice-text';
      sendButton.type = 'button';
      sendButton.dataset.choiceSend = choice;
      sendButton.dataset.choiceIndex = String(index + 1);

      const textSpan = document.createElement('span');
      textSpan.textContent = choice;
      const preview = document.createElement('small');
      preview.textContent = `예상 변화: ${formatStatPreview(statChangesByChoice ? statChangesByChoice[`choice${index + 1}`] : null)}`;
      sendButton.append(textSpan, preview);

      const editButton = document.createElement('button');
      editButton.className = 'btn secondary small';
      editButton.type = 'button';
      editButton.dataset.choiceEdit = choice;
      editButton.textContent = '수정';

      item.append(sendButton, editButton);
      choiceList.appendChild(item);
    });
  };

  const updateProgress = (result) => {
    if (!result) return;
    const percent = Number(result.progressPercent || 0);
    const nextCurrentTurn = result.session ? Number(result.session.currentTurn || 0) : null;
    if (progressValue) progressValue.textContent = `${percent}%`;
    if (progressFill) progressFill.style.width = `${Math.min(100, Math.max(0, percent))}%`;
    if (progressTurn && nextCurrentTurn !== null) {
      progressTurn.textContent = targetTurn ? `${nextCurrentTurn} / ${targetTurn}턴` : `${nextCurrentTurn}턴 진행`;
    }
    if (nextCurrentTurn !== null) currentTurn = nextCurrentTurn;
  };

  const updateStats = (statLog) => {
    if (!statLog) return;
    const deltas = {
      wisdom: Number(statLog.wisdomDelta || 0),
      strength: Number(statLog.strengthDelta || 0),
      agility: Number(statLog.agilityDelta || 0),
      charisma: Number(statLog.charismaDelta || 0),
      luck: Number(statLog.luckDelta || 0),
    };

    Object.entries(deltas).forEach(([statName, delta]) => {
      if (!delta) return;
      const valueNode = document.querySelector(`[data-stat-value="${statName}"]`);
      if (!valueNode) return;
      const nextValue = Number(valueNode.textContent || 0) + delta;
      valueNode.textContent = String(nextValue);

      const row = valueNode.closest('.persona-stat-row');
      if (!row) return;
      row.querySelectorAll('.persona-stat-bar > span').forEach((slot, index) => {
        slot.classList.toggle('filled', nextValue >= index + 1);
      });
    });
  };

  const refreshInventoryEmptyState = () => {
    if (!inventoryList || !inventoryEmpty) return;
    const hasItems = Boolean(inventoryList.querySelector('[data-inventory-item]'));
    inventoryEmpty.style.display = hasItems ? 'none' : '';
  };

  const createCsrfInput = () => {
    if (!csrfToken) return null;
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = '_csrf';
    input.value = csrfToken;
    return input;
  };

  const renderInventoryItem = (item) => {
    if (!inventoryList || !item || !item.id) return;
    const existing = inventoryList.querySelector(`[data-inventory-item][data-item-id="${item.id}"]`);
    if (existing) existing.remove();

    const form = document.createElement('form');
    form.method = 'post';
    form.action = `/sessions/${sessionId}/items/${item.id}/drop`;
    form.setAttribute('data-inventory-item', '');
    form.dataset.itemId = String(item.id);

    const csrfInput = createCsrfInput();
    if (csrfInput) form.appendChild(csrfInput);

    const name = document.createElement('strong');
    name.textContent = item.itemName || '아이템';

    const description = document.createElement('p');
    description.className = 'muted';
    description.textContent = item.itemDescription || '설명 없음';

    const button = document.createElement('button');
    button.className = 'btn secondary small';
    button.type = 'submit';
    button.textContent = '버리기';

    form.append(name, description, button);
    inventoryList.insertBefore(form, inventoryEmpty || null);
  };

  const updateInventory = (result) => {
    if (!result || !inventoryList) return;

    if (result.removedItemId) {
      const itemNode = inventoryList.querySelector(`[data-inventory-item][data-item-id="${result.removedItemId}"]`);
      if (itemNode) itemNode.remove();
    }

    if (result.createdItem) {
      renderInventoryItem(result.createdItem);
    }

    refreshInventoryEmptyState();
  };

  const handleComplete = (result) => {
    isAwaitingResponse = false;
    if (result && result.endingType) {
      window.setTimeout(() => {
        window.location.href = `/sessions/${sessionId}/ending`;
      }, 450);
      return;
    }
    if (result && mode === 'story') {
      setOutput(result.story || '');
      updateProgress(result);
      updateStats(result.appliedStatLog);
      updateInventory(result);
      renderChoices(result.choices || [], result.statChangesByChoice || null);
    }
    setInputLocked(false);
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
        if (payload.event === 'COMPLETE') handleComplete(payload.result);
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

  document.addEventListener('click', (event) => {
    const sendButton = event.target.closest('[data-choice-send]');
    if (sendButton) {
      const text = sendButton.dataset.choiceSend || sendButton.textContent || '';
      const choiceIndex = Number(sendButton.dataset.choiceIndex || 0) || null;
      fillInput(text, false);
      sendFreeText(text, choiceIndex);
      return;
    }

    const editButton = event.target.closest('[data-choice-edit]');
    if (editButton) {
      if (isWaitingForStory) return;
      fillInput(editButton.dataset.choiceEdit || '', true);
    }
  });

  if (startButton) {
    startButton.addEventListener('click', startStory);
  }
})();
