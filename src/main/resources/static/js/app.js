document.addEventListener("submit", (event) => {
  const form = event.target;
  const message = form.dataset.confirm;
  if (message && !window.confirm(message)) {
    event.preventDefault();
  }
});


document.addEventListener("change", (event) => {
  const field = event.target.closest("[data-auto-submit]");
  if (!field || !field.form) return;
  field.form.requestSubmit();
});

document.addEventListener("click", (event) => {
  const button = event.target.closest("[data-demo-login]");
  if (!button) return;

  const form = document.querySelector("[data-login-form]");
  const loginId = document.querySelector("#loginId");
  const password = document.querySelector("#password");
  if (!form || !loginId || !password) return;

  loginId.value = button.dataset.loginId || "";
  password.value = button.dataset.password || "";
  form.requestSubmit();
});


document.addEventListener("click", (event) => {
  const opener = event.target.closest("[data-modal-target]");
  if (opener) {
    const modal = document.getElementById(opener.dataset.modalTarget);
    if (modal) {
      modal.hidden = false;
      document.body.classList.add("modal-open");
      const closeButton = modal.querySelector("[data-modal-close]");
      if (closeButton) closeButton.focus();
    }
    return;
  }

  const closeButton = event.target.closest("[data-modal-close]");
  const backdrop = event.target.matches("[data-modal]") ? event.target : null;
  const modal = closeButton ? closeButton.closest("[data-modal]") : backdrop;
  if (modal) {
    modal.hidden = true;
    document.body.classList.remove("modal-open");
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    document.querySelectorAll("[data-modal]:not([hidden])").forEach((modal) => {
      modal.hidden = true;
    });
    document.body.classList.remove("modal-open");
    return;
  }

  if (event.key === "Enter") {
    const opener = event.target.closest("[data-modal-target]");
    if (opener) {
      opener.click();
    }
  }
});
