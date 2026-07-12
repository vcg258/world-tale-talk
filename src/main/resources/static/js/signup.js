(function () {
  const form = document.querySelector('[data-signup-form]');
  if (!form) return;

  const checkedInputs = Array.from(form.querySelectorAll('[data-check-url]'));
  const password = form.querySelector('[data-password]');
  const confirmPassword = form.querySelector('[data-confirm-password]');
  const submit = form.querySelector('[data-submit]');
  const state = new Map();
  const timers = new Map();

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
      setFeedback(password, '', '8~50자, 한글 없이 입력해주세요.');
    } else if (value.length < 8 || value.length > 50) {
      password.setCustomValidity('비밀번호는 8~50자로 입력해주세요.');
      setFeedback(password, 'error', '비밀번호는 8~50자로 입력해주세요.');
    } else if (hasHangul(value)) {
      password.setCustomValidity('비밀번호에 한글은 사용할 수 없습니다.');
      setFeedback(password, 'error', '비밀번호에 한글은 사용할 수 없습니다.');
    } else {
      password.setCustomValidity('');
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
      return;
    }

    if (!input.checkValidity()) {
      setFeedback(input, 'error', input.validationMessage);
      setState(input, false);
      return;
    }

    setFeedback(input, 'checking', `${kind} 중복을 확인하고 있습니다.`);
    setState(input, false);

    const url = new URL(input.dataset.checkUrl, window.location.origin);
    url.searchParams.set(input.dataset.checkParam, value);

    fetch(url, {headers: {'Accept': 'application/json'}})
      .then((response) => response.ok ? response.json() : Promise.reject(new Error('check failed')))
      .then((result) => {
        if (result.available) {
          input.setCustomValidity('');
          setFeedback(input, 'ok', `사용 가능한 ${kind}입니다.`);
          setState(input, true);
          return;
        }
        input.setCustomValidity(`이미 사용중인 ${kind}입니다.`);
        setFeedback(input, 'error', `이미 사용중인 ${kind}입니다.`);
        setState(input, false);
      })
      .catch(() => {
        input.setCustomValidity(`${kind} 중복 확인에 실패했습니다.`);
        setFeedback(input, 'error', `${kind} 중복 확인에 실패했습니다.`);
        setState(input, false);
      });
  };

  const scheduleCheck = (input) => {
    window.clearTimeout(timers.get(input.name));
    timers.set(input.name, window.setTimeout(() => checkDuplicate(input), 350));
  };

  function updateSubmitState() {
    const duplicateOk = checkedInputs.every((input) => state.get(input.name) === true);
    submit.disabled = !(duplicateOk && password.validity.valid && confirmPassword.validity.valid);
  }

  checkedInputs.forEach((input) => {
    state.set(input.name, false);
    input.addEventListener('input', () => scheduleCheck(input));
    input.addEventListener('blur', () => checkDuplicate(input));
  });

  password.addEventListener('input', validatePassword);
  confirmPassword.addEventListener('input', validateConfirmPassword);

  form.addEventListener('submit', (event) => {
    checkedInputs.forEach(checkDuplicate);
    validatePassword();
    validateConfirmPassword();

    if (!form.checkValidity() || checkedInputs.some((input) => state.get(input.name) !== true)) {
      event.preventDefault();
      form.reportValidity();
    }
  });

  validatePassword();
  validateConfirmPassword();
  updateSubmitState();
})();
