/**
 * CivicVote API Module
 * Central API communication layer — all requests go through the API Gateway (port 8080).
 */

const API_BASE = 'http://localhost:8080';

const api = {
    /**
     * Make an authenticated API request.
     */
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${API_BASE}${endpoint}`, {
                ...options,
                headers
            });

            // Handle no-content responses
            if (response.status === 204) {
                return { ok: true, status: 204 };
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const error = new Error(data?.message || `HTTP ${response.status}`);
                error.status = response.status;
                error.data = data;
                throw error;
            }

            return data;
        } catch (err) {
            if (err.status === 401) {
                // Token expired or invalid
                localStorage.clear();
                window.location.href = 'login.html';
                return;
            }
            throw err;
        }
    },

    // --- Auth ---
    register(data) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    login(data) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // --- Elections ---
    getElections() {
        return this.request('/elections');
    },

    getElection(id) {
        return this.request(`/elections/${id}`);
    },

    createElection(data) {
        return this.request('/elections', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateElection(id, data) {
        return this.request(`/elections/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    deleteElection(id) {
        return this.request(`/elections/${id}`, {
            method: 'DELETE'
        });
    },

    getCandidates(electionId) {
        return this.request(`/elections/${electionId}/candidates`);
    },

    addCandidate(electionId, data) {
        return this.request(`/elections/${electionId}/candidates`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    getElectionStatus(electionId) {
        return this.request(`/elections/${electionId}/status`);
    },

    // --- Voting ---
    castVote(data) {
        return this.request('/votes', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    getVoteStatus(electionId) {
        return this.request(`/votes/status/${electionId}`);
    },

    // --- Results ---
    getResults(electionId) {
        return this.request(`/results/${electionId}`);
    },

    getWinner(electionId) {
        return this.request(`/results/${electionId}/winner`);
    }
};

// --- Utility Functions ---

function getUser() {
    return {
        token: localStorage.getItem('token'),
        username: localStorage.getItem('username'),
        role: localStorage.getItem('role'),
        userId: localStorage.getItem('userId')
    };
}

function isLoggedIn() {
    return !!localStorage.getItem('token');
}

function isAdmin() {
    return localStorage.getItem('role') === 'ADMIN';
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function showAlert(container, message, type = 'error') {
    const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ';
    const alertEl = document.createElement('div');
    alertEl.className = `alert alert-${type}`;
    alertEl.innerHTML = `<span>${icon}</span> ${message}`;

    // Remove existing alerts
    const existing = container.querySelector('.alert');
    if (existing) existing.remove();

    container.prepend(alertEl);

    // Auto-remove after 5 seconds
    setTimeout(() => alertEl.remove(), 5000);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleString('en-IN', {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function getStatusBadge(status) {
    const cls = status === 'ACTIVE' ? 'badge-active'
        : status === 'UPCOMING' ? 'badge-upcoming'
        : 'badge-closed';
    return `<span class="badge ${cls}">${status}</span>`;
}

function setupNavbar() {
    const user = getUser();
    const navUser = document.getElementById('nav-user');
    if (!navUser) return;

    if (user.token) {
        const badgeCls = user.role === 'ADMIN' ? 'admin' : 'voter';
        navUser.innerHTML = `
            <span class="user-badge ${badgeCls}">${user.role}</span>
            <span style="color: var(--text-secondary); font-size: 0.85rem;">${user.username}</span>
            <button class="btn-logout" onclick="logout()">Logout</button>
        `;
    }
}
