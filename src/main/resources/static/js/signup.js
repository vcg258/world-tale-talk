(function () {
  const form = document.querySelector('[data-signup-form]');
  if (!form) return;

  const checkedInputs = Array.from(form.querySelectorAll('[data-check-url]'));
  const email = form.querySelector('[data-email]');
  const otpField = form.querySelector('[data-otp-field]');
  const otp = form.querySelector('[data-otp]');
  const sendOtp = form.querySelector('[data-send-otp]');
  const verifyOtp = form.querySelector('[data-verify-otp]');
  const otpExpiry = form.querySelector('[data-otp-expiry]');
  const password = form.querySelector('[data-password]');
  const confirmPassword = form.querySelector('[data-confirm-password]');
  const submit = form.querySelector('[data-submit]');
  const state = new Map();
  const timers = new Map();
  let submitting = false;
  let emailVerified = false;
  let otpRequestPending = false;
  let otpExpired = true;
  let resendRemaining = 0;
  let otpCountdownTimer = null;

  const feedback = (name) => form.querySelector(`[data-feedback-for="${name}"]`);

  const setFeedback = (input, status, message) => {
    const target = feedback(input.name);
    if (!target) return;
    target.textContent = message;
    target.classList.remove('ok', 'error', 'checking');
    if (status) target.classList.add(status);
  };

  const setState = (input, value) => {
    state.set(input.name, value);
    updateSubmitState();
  };

  const hasHangul = (value) => /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(value);

  const validatePassword = () => {
    const value = password.value;
    password.setCustomValidity('');

    if (!value) {
      password.setCustomValidity('비밀번호를 입력해주세요.');
      setFeedback(password, '', '비밀번호 조건을 만족하는지 자동으로 확인합니다.');
    } else if (value.length < 8 || value.length > 50) {
      password.setCustomValidity('비밀번호는 8~50자로 입력해주세요.');
      setFeedback(password, 'error', '비밀번호는 8~50자로 입력해주세요.');
    } else if (hasHangul(value)) {
      password.setCustomValidity('비밀번호에 한글은 사용할 수 없습니다.');
      setFeedback(password, 'error', '비밀번호에 한글은 사용할 수 없습니다.');
    } else {
      setFeedback(password, 'ok', '사용 가능한 비밀번호입니다.');
    }

    validateConfirmPassword();
    updateSubmitState();
  };

  const validateConfirmPassword = () => {
    if (!confirmPassword.value) {
      confirmPassword.setCustomValidity('비밀번호 확인을 입력해주세요.');
      setFeedback(confirmPassword, '', '비밀번호와 같은 값을 입력하면 통과됩니다.');
    } else if (password.value !== confirmPassword.value) {
      confirmPassword.setCustomValidity('비밀번호가 일치하지 않습니다.');
      setFeedback(confirmPassword, 'error', '비밀번호가 일치하지 않습니다.');
    } else {
      confirmPassword.setCustomValidity('');
      setFeedback(confirmPassword, 'ok', '비밀번호가 일치합니다.');
    }
    updateSubmitState();
  };

  const checkDuplicate = (input) => {
    const value = input.value.trim();
    const kind = input.dataset.checkKind;

    input.value = value;
    input.setCustomValidity('');

    if (!value) {
      input.setCustomValidity(`${kind}를 입력해주세요.`);
      setFeedback(input, '', `${kind}를 입력해주세요.`);
      setState(input, false);
      return Promise.resolve(false);
    }

    if (!input.checkValidity()) {
      setFeedback(input, 'error', input.validationMessage);
      setState(input, false);
      return Promise.resolve(false);
    }

    setFeedback(input, 'checking', `${kind} 중복을 확인하고 있습니다.`);
    setState(input, false);

    const url = new URL(input.dataset.checkUrl, window.location.origin);
    url.searchParams.set(input.dataset.checkParam, value);

    return fetch(url, {headers: {'Accept': 'application/json'}})
      .then((response) => response.ok ? response.json() : Promise.reject(new Error('check failed')))
      .then((result) => {
        if (result.available) {
          input.setCustomValidity('');
          setFeedback(input, 'ok', `사용 가능한 ${kind}입니다.`);
          setState(input, true);
          return true;
        }
        input.setCustomValidity(`이미 사용중인 ${kind}입니다.`);
        setFeedback(input, 'error', `이미 사용중인 ${kind}입니다.`);
        setState(input, false);
        return false;
      })
      .catch(() => {
        input.setCustomValidity(`${kind} 중복 확인에 실패했습니다.`);
        setFeedback(input, 'error', `${kind} 중복 확인에 실패했습니다.`);
        setState(input, false);
        return false;
      });
  };

  const scheduleCheck = (input) => {
    window.clearTimeout(timers.get(input.name));
    timers.set(input.name, window.setTimeout(() => checkDuplicate(input), 350));
  };

  const setOtpFeedback = (status, message) => {
    const target = feedback('emailOtp');
    target.textContent = message;
    target.classList.remove('ok', 'error', 'checking');
    if (status) target.classList.add(status);
  };

  const stopOtpCountdown = () => {
    if (otpCountdownTimer !== null) {
      window.clearInterval(otpCountdownTimer);
      otpCountdownTimer = null;
    }
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60).toString().padStart(2, '0');
    const remainder = (seconds % 60).toString().padStart(2, '0');
    return `${minutes}:${remainder}`;
  };

  const resetOtpCountdown = () => {
    stopOtpCountdown();
    otpExpired = true;
    resendRemaining = 0;
    otpExpiry.textContent = '04:00';
    sendOtp.textContent = '인증번호 받기';
  };

  const startOtpCountdown = (resendSeconds, expirySeconds) => {
    stopOtpCountdown();
    const resendDeadline = Date.now() + resendSeconds * 1000;
    const expiryDeadline = Date.now() + expirySeconds * 1000;
    otpExpired = false;

    const tick = () => {
      resendRemaining = Math.max(0, Math.ceil((resendDeadline - Date.now()) / 1000));
      const expiryRemaining = Math.max(0, Math.ceil((expiryDeadline - Date.now()) / 1000));
      sendOtp.textContent = resendRemaining > 0
        ? `재발송 (${resendRemaining}초)`
        : '인증번호 재발송';
      otpExpiry.textContent = formatTime(expiryRemaining);

      if (expiryRemaining === 0) {
        otpExpired = true;
        stopOtpCountdown();
        if (!emailVerified) {
          setOtpFeedback('error', '인증번호가 만료되었습니다. 인증번호를 재발송해주세요.');
        }
      }
      updateSubmitState();
    };

    tick();
    otpCountdownTimer = window.setInterval(tick, 1000);
  };

  const postForm = (url, values) => fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
    },
    body: new URLSearchParams(values)
  }).then((response) => response.ok ? response.json() : Promise.reject(new Error('request failed')));

  const sendEmailOtp = async () => {
    if (otpRequestPending || emailVerified) return;
    window.clearTimeout(timers.get(email.name));
    if (!await checkDuplicate(email)) return;

    otpRequestPending = true;
    updateSubmitState();
    setFeedback(email, 'checking', '인증번호를 발송하고 있습니다.');
    try {
      const result = await postForm('/auth/signup/send-code', {email: email.value.trim()});
      if (!result.success) {
        setFeedback(email, 'error', result.message || '인증번호 발송에 실패했습니다.');
        return;
      }
      otpField.hidden = false;
      otp.value = '';
      otp.readOnly = false;
      startOtpCountdown(Number(result.resendAfterSeconds) || 60, Number(result.expiresInSeconds) || 240);
      setFeedback(email, 'ok', result.message || '인증번호를 발송했습니다.');
      setOtpFeedback('', '메일로 받은 6자리 인증번호를 입력해주세요.');
      otp.focus();
    } catch (error) {
      setFeedback(email, 'error', '인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      otpRequestPending = false;
      updateSubmitState();
    }
  };

  const verifyEmailOtp = async () => {
    const otpValue = otp.value.trim();
    if (otpRequestPending || emailVerified) return;
    if (otpExpired) {
      setOtpFeedback('error', '인증번호가 만료되었습니다. 인증번호를 재발송해주세요.');
      return;
    }
    if (!/^[0-9]{6}$/.test(otpValue)) {
      setOtpFeedback('error', '6자리 숫자 인증번호를 입력해주세요.');
      otp.focus();
      return;
    }

    otpRequestPending = true;
    updateSubmitState();
    setOtpFeedback('checking', '인증번호를 확인하고 있습니다.');
    try {
      const result = await postForm('/auth/signup/verify-code', {email: email.value.trim(), otp: otpValue});
      if (!result.verifyOtp) {
        setOtpFeedback('error', '인증번호가 올바르지 않거나 만료되었습니다.');
        return;
      }
      emailVerified = true;
      stopOtpCountdown();
      otpExpiry.textContent = '인증 완료';
      email.readOnly = true;
      otp.readOnly = true;
      setFeedback(email, 'ok', '이메일 인증이 완료되었습니다.');
      setOtpFeedback('ok', '인증이 완료되었습니다.');
    } catch (error) {
      setOtpFeedback('error', '인증 확인에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      otpRequestPending = false;
      updateSubmitState();
    }
  };

  function updateSubmitState() {
    const duplicateOk = checkedInputs.every((input) => state.get(input.name) === true);
    sendOtp.disabled = otpRequestPending || emailVerified || resendRemaining > 0 || state.get(email.name) !== true;
    verifyOtp.disabled = otpRequestPending || emailVerified || otpExpired || otpField.hidden;
    submit.disabled = !(duplicateOk && emailVerified && password.validity.valid && confirmPassword.validity.valid);
  }

  checkedInputs.forEach((input) => {
    state.set(input.name, false);
    input.addEventListener('input', () => {
      if (input === email) {
        emailVerified = false;
        email.readOnly = false;
        otp.readOnly = false;
        otp.value = '';
        otpField.hidden = true;
        resetOtpCountdown();
        setOtpFeedback('', '인증번호를 입력해주세요.');
      }
      scheduleCheck(input);
    });
    input.addEventListener('blur', () => checkDuplicate(input));
  });

  otp.addEventListener('input', () => {
    otp.value = otp.value.replace(/[^0-9]/g, '').slice(0, 6);
  });
  sendOtp.addEventListener('click', sendEmailOtp);
  verifyOtp.addEventListener('click', verifyEmailOtp);
  password.addEventListener('input', validatePassword);
  confirmPassword.addEventListener('input', validateConfirmPassword);

  form.addEventListener('submit', async (event) => {
    if (submitting) return;

    event.preventDefault();
    checkedInputs.forEach((input) => window.clearTimeout(timers.get(input.name)));
    validatePassword();
    validateConfirmPassword();

    const duplicateResults = await Promise.all(checkedInputs.map(checkDuplicate));
    if (!emailVerified) setFeedback(email, 'error', '이메일 인증을 완료해주세요.');
    if (!form.checkValidity() || duplicateResults.some((result) => !result) || !emailVerified) {
      form.reportValidity();
      return;
    }

    submitting = true;
    HTMLFormElement.prototype.submit.call(form);
  });

  validatePassword();
  validateConfirmPassword();
  updateSubmitState();
})();
