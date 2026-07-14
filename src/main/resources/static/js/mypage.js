(function () {
  const nicknameForm = document.querySelector('[data-mypage-nickname-form]');
  if (nicknameForm) {
    const input = nicknameForm.querySelector('[data-nickname-input]');
    const feedback = nicknameForm.querySelector('[data-nickname-feedback]');
    const submit = nicknameForm.querySelector('[data-nickname-submit]');
    const currentNickname = (input.dataset.currentNickname || '').trim();
    let nicknameAvailable = false;
    let timer;

    const setFeedback = (status, message) => {
      feedback.textContent = message;
      feedback.classList.remove('ok', 'error', 'checking');
      if (status) feedback.classList.add(status);
    };

    const updateSubmit = () => {
      submit.disabled = !nicknameAvailable;
    };

    const checkNickname = () => {
      const value = input.value.trim();
      input.value = value;
      input.setCustomValidity('');

      if (!value) {
        input.setCustomValidity('닉네임을 입력해주세요.');
        nicknameAvailable = false;
        setFeedback('', '닉네임을 입력해주세요.');
        updateSubmit();
        return Promise.resolve(false);
      }

      if (value === currentNickname) {
        nicknameAvailable = true;
        setFeedback('ok', '현재 사용 중인 닉네임입니다.');
        updateSubmit();
        return Promise.resolve(true);
      }

      setFeedback('checking', '닉네임 중복을 확인하고 있습니다.');
      nicknameAvailable = false;
      updateSubmit();

      const url = new URL('/auth/nickname/check', window.location.origin);
      url.searchParams.set('nickname', value);

      return fetch(url, {headers: {'Accept': 'application/json'}})
        .then((response) => response.ok ? response.json() : Promise.reject(new Error('check failed')))
        .then((result) => {
          nicknameAvailable = Boolean(result.available);
          if (nicknameAvailable) {
            input.setCustomValidity('');
            setFeedback('ok', '사용 가능한 닉네임입니다.');
          } else {
            input.setCustomValidity('이미 사용중인 닉네임입니다.');
            setFeedback('error', '이미 사용중인 닉네임입니다.');
          }
          updateSubmit();
          return nicknameAvailable;
        })
        .catch(() => {
          nicknameAvailable = false;
          input.setCustomValidity('닉네임 중복 확인에 실패했습니다.');
          setFeedback('error', '닉네임 중복 확인에 실패했습니다.');
          updateSubmit();
          return false;
        });
    };

    input.addEventListener('input', () => {
      window.clearTimeout(timer);
      timer = window.setTimeout(checkNickname, 350);
    });
    input.addEventListener('blur', checkNickname);
    nicknameForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      window.clearTimeout(timer);
      const ok = await checkNickname();
      if (!ok || !nicknameForm.checkValidity()) {
        nicknameForm.reportValidity();
        return;
      }
      HTMLFormElement.prototype.submit.call(nicknameForm);
    });
    checkNickname();
  }

  const emailSource = document.querySelector('[data-new-email-source]');
  const emailTarget = document.querySelector('[data-new-email-target]');
  if (emailSource && emailTarget) {
    const syncEmail = () => {
      emailTarget.value = emailSource.value.trim();
    };
    emailSource.addEventListener('input', syncEmail);
    document.querySelector('[data-email-verify-form]')?.addEventListener('submit', syncEmail);
    syncEmail();
  }

  const emailCodeTimer = document.querySelector('[data-email-code-timer]');
  if (emailCodeTimer) {
    let remainingSeconds = Number(emailCodeTimer.dataset.durationSeconds || 240);
    let intervalId;

    const formatTime = (seconds) => {
      const minutes = String(Math.floor(seconds / 60)).padStart(2, '0');
      const restSeconds = String(seconds % 60).padStart(2, '0');
      return minutes + ':' + restSeconds;
    };

    const renderTimer = () => {
      if (remainingSeconds <= 0) {
        emailCodeTimer.classList.remove('checking');
        emailCodeTimer.classList.add('error');
        emailCodeTimer.textContent = '시간이 만료되었습니다 인증코드를 재발송해주세요';
        window.clearInterval(intervalId);
        return;
      }

      emailCodeTimer.textContent = '인증코드 유효 시간 ' + formatTime(remainingSeconds);
      remainingSeconds -= 1;
    };

    intervalId = window.setInterval(renderTimer, 1000);
    renderTimer();
  }
})();
