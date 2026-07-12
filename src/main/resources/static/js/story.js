(function () {
  const root = document.querySelector("[data-story-session]");
  if (!root || !window.SockJS || !window.Stomp) {
    return;
  }

  const sessionId = root.dataset.storySession;
  const mode = root.dataset.storyMode || "story";
  const topic = mode === "epilogue" ? `/topic/epilogue/${sessionId}` : `/topic/story/${sessionId}`;
  const destination = mode === "epilogue" ? `/app/epilogue/${sessionId}` : `/app/story/${sessionId}`;
  const output = document.querySelector("[data-live-story]");
  const status = document.querySelector("[data-socket-status]");
  const form = document.querySelector("[data-story-form]");
  const freeText = document.querySelector("[data-story-input]");
  const choiceButtons = document.querySelectorAll("[data-choice-index]");

  const setStatus = (text) => {
    if (status) status.textContent = text;
  };

  const append = (text) => {
    if (output) output.textContent = `${output.textContent || ""}${text}`;
  };

  const stomp = Stomp.over(new SockJS("/ws"));
  stomp.debug = null;
  stomp.connect({}, () => {
    setStatus("연결됨");
    stomp.subscribe(topic, (frame) => {
      const payload = JSON.parse(frame.body);
      if (payload.event === "DELTA") append(payload.value || "");
      if (payload.event === "ERROR") setStatus(payload.value || "오류가 발생했습니다.");
      if (payload.event === "COMPLETE") window.setTimeout(() => window.location.reload(), 600);
    });
  }, () => setStatus("연결 실패"));

  if (form) {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const content = freeText.value.trim();
      if (!content) return;
      output.textContent = "";
      const payload = mode === "epilogue" ? {content} : {type: "FREE_TEXT", content};
      stomp.send(destination, {}, JSON.stringify(payload));
      freeText.value = "";
    });
  }

  choiceButtons.forEach((button) => {
    button.addEventListener("click", () => {
      output.textContent = "";
      if (button.dataset.choiceIndex === "start") {
        stomp.send(`/app/story/${sessionId}/start`, {}, "{}");
        return;
      }
      stomp.send(destination, {}, JSON.stringify({
        type: "CHOICE",
        choiceIndex: Number(button.dataset.choiceIndex)
      }));
    });
  });
})();
