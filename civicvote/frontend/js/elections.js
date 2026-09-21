/**
 * CivicVote Elections Listing Module (Voter view)
 */

document.addEventListener('DOMContentLoaded', () => {
    if (!requireAuth()) return;
    setupNavbar();
    loadElections();
});

async function loadElections() {
    const grid = document.getElementById('elections-grid');
    grid.innerHTML = '<div class="loading"><div class="spinner"></div> Loading elections...</div>';

    try {
        const elections = await api.getElections();

        if (elections.length === 0) {
            grid.innerHTML = '<div class="text-center" style="padding:48px;color:var(--text-muted);">No elections available at the moment.</div>';
            return;
        }

        grid.innerHTML = elections.map(e => `
            <div class="election-card" onclick="viewElection(${e.electionId})">
                <div class="card-header">
                    <h3>${e.title}</h3>
                    ${getStatusBadge(e.status)}
                </div>
                <p>${e.description || 'No description'}</p>
                <div class="card-meta">
                    <span>📅 ${formatDate(e.startDate)}</span>
                    <span>🕐 ${formatDate(e.endDate)}</span>
                </div>
                <div class="card-meta mt-1">
                    <span>👥 ${e.candidates?.length || 0} candidates</span>
                </div>
            </div>
        `).join('');

    } catch (err) {
        grid.innerHTML = `<div class="alert alert-error">Failed to load elections: ${err.message}</div>`;
    }
}

function viewElection(id) {
    window.location.href = `vote.html?id=${id}`;
}
