document.addEventListener("submit", (event) => {
  const form = event.target;
  const message = form.dataset.confirm;
  if (message && !window.confirm(message)) {
    event.preventDefault();
  }
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
