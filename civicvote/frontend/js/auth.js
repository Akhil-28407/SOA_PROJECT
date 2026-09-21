/**
 * CivicVote Auth Module — Login & Registration logic
 */

document.addEventListener('DOMContentLoaded', () => {
    // Login Form
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = loginForm.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.textContent = 'Signing in...';

            try {
                const data = await api.login({
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value
                });

                // Store auth data
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                localStorage.setItem('role', data.role);
                localStorage.setItem('userId', data.userId);

                // Redirect based on role
                if (data.role === 'ADMIN') {
                    window.location.href = 'admin-dashboard.html';
                } else {
                    window.location.href = 'elections.html';
                }
            } catch (err) {
                showAlert(loginForm, err.message || 'Invalid email or password');
                btn.disabled = false;
                btn.textContent = 'Sign In';
            }
        });
    }

    // Register Form
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = registerForm.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.textContent = 'Creating account...';

            try {
                const data = await api.register({
                    username: document.getElementById('username').value,
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value,
                    role: document.getElementById('role')?.value || 'VOTER'
                });

                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                localStorage.setItem('role', data.role);
                localStorage.setItem('userId', data.userId);

                if (data.role === 'ADMIN') {
                    window.location.href = 'admin-dashboard.html';
                } else {
                    window.location.href = 'elections.html';
                }
            } catch (err) {
                showAlert(registerForm, err.message || 'Registration failed');
                btn.disabled = false;
                btn.textContent = 'Create Account';
            }
        });
    }
});
