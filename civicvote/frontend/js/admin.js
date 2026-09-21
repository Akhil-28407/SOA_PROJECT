/**
 * CivicVote Admin Dashboard Module
 */

document.addEventListener('DOMContentLoaded', () => {
    if (!requireAuth() || !isAdmin()) {
        window.location.href = 'login.html';
        return;
    }
    setupNavbar();
    loadDashboard();
});

async function loadDashboard() {
    try {
        const elections = await api.getElections();
        updateStats(elections);
        renderElectionsTable(elections);
    } catch (err) {
        showAlert(document.querySelector('.dashboard'), 'Failed to load dashboard: ' + err.message);
    }
}

function updateStats(elections) {
    const total = elections.length;
    const active = elections.filter(e => e.status === 'ACTIVE').length;
    const closed = elections.filter(e => e.status === 'CLOSED').length;
    const totalCandidates = elections.reduce((sum, e) => sum + (e.candidates?.length || 0), 0);

    document.getElementById('stat-total').textContent = total;
    document.getElementById('stat-active').textContent = active;
    document.getElementById('stat-closed').textContent = closed;
    document.getElementById('stat-candidates').textContent = totalCandidates;
}

function renderElectionsTable(elections) {
    const tbody = document.getElementById('elections-tbody');
    if (!tbody) return;

    if (elections.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:32px;">No elections yet. Create one to get started.</td></tr>';
        return;
    }

    tbody.innerHTML = elections.map(e => `
        <tr>
            <td><strong>${e.title}</strong></td>
            <td>${getStatusBadge(e.status)}</td>
            <td>${formatDate(e.startDate)}</td>
            <td>${formatDate(e.endDate)}</td>
            <td>${e.candidates?.length || 0}</td>
            <td>
                <button class="btn btn-sm btn-secondary" onclick="openCandidateModal(${e.electionId}, '${e.title}')">+ Candidate</button>
                <button class="btn btn-sm btn-secondary" onclick="viewResults(${e.electionId})">Results</button>
                <button class="btn btn-sm btn-danger" onclick="deleteElection(${e.electionId})">Delete</button>
            </td>
        </tr>
    `).join('');
}

// --- Create Election ---
function openCreateElectionModal() {
    document.getElementById('election-modal').classList.remove('hidden');
}

function closeElectionModal() {
    document.getElementById('election-modal').classList.add('hidden');
    document.getElementById('create-election-form').reset();
}

document.getElementById('create-election-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    btn.disabled = true;

    try {
        await api.createElection({
            title: document.getElementById('election-title').value,
            description: document.getElementById('election-desc').value,
            startDate: document.getElementById('election-start').value,
            endDate: document.getElementById('election-end').value
        });
        closeElectionModal();
        loadDashboard();
    } catch (err) {
        showAlert(document.getElementById('election-modal'), err.message);
    }
    btn.disabled = false;
});

// --- Add Candidate ---
let currentElectionId = null;

function openCandidateModal(electionId, title) {
    currentElectionId = electionId;
    document.getElementById('candidate-election-title').textContent = title;
    document.getElementById('candidate-modal').classList.remove('hidden');
    loadCandidatesList(electionId);
}

function closeCandidateModal() {
    document.getElementById('candidate-modal').classList.add('hidden');
    document.getElementById('add-candidate-form').reset();
    currentElectionId = null;
}

async function loadCandidatesList(electionId) {
    const list = document.getElementById('candidates-list');
    try {
        const candidates = await api.getCandidates(electionId);
        if (candidates.length === 0) {
            list.innerHTML = '<p style="color:var(--text-muted);font-size:0.85rem;">No candidates yet.</p>';
        } else {
            list.innerHTML = candidates.map(c =>
                `<div style="padding:8px 0;border-bottom:1px solid var(--border-color);font-size:0.9rem;">
                    <strong>${c.candidateName}</strong>
                    <span style="color:var(--text-muted);margin-left:8px;">${c.description || ''}</span>
                </div>`
            ).join('');
        }
    } catch (err) {
        list.innerHTML = '<p style="color:var(--danger);">Failed to load candidates</p>';
    }
}

document.getElementById('add-candidate-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!currentElectionId) return;

    const btn = e.target.querySelector('button[type="submit"]');
    btn.disabled = true;

    try {
        await api.addCandidate(currentElectionId, {
            candidateName: document.getElementById('candidate-name').value,
            description: document.getElementById('candidate-desc').value
        });
        document.getElementById('candidate-name').value = '';
        document.getElementById('candidate-desc').value = '';
        loadCandidatesList(currentElectionId);
        loadDashboard();
    } catch (err) {
        showAlert(document.querySelector('#candidate-modal .modal'), err.message);
    }
    btn.disabled = false;
});

// --- Delete Election ---
async function deleteElection(id) {
    if (!confirm('Are you sure you want to delete this election?')) return;
    try {
        await api.deleteElection(id);
        loadDashboard();
    } catch (err) {
        alert('Failed to delete: ' + err.message);
    }
}

// --- View Results ---
function viewResults(electionId) {
    window.location.href = `results.html?id=${electionId}`;
}
