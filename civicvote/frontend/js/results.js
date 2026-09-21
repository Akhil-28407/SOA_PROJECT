/**
 * CivicVote Results Module
 */

document.addEventListener('DOMContentLoaded', () => {
    if (!requireAuth()) return;
    setupNavbar();

    const urlParams = new URLSearchParams(window.location.search);
    const electionId = urlParams.get('id');

    if (electionId) {
        loadResults(electionId);
    } else {
        loadAllElectionsForResults();
    }
});

async function loadAllElectionsForResults() {
    const container = document.getElementById('results-container');
    try {
        const elections = await api.getElections();
        if (elections.length === 0) {
            container.innerHTML = '<div class="text-center" style="padding:48px;color:var(--text-muted);">No elections available.</div>';
            return;
        }

        document.getElementById('results-header').innerHTML = '<h1>All Election Results</h1><p style="color:var(--text-secondary);">Select an election to view detailed results</p>';

        container.innerHTML = elections.map(e => `
            <div class="election-card" onclick="window.location.href='results.html?id=${e.electionId}'" style="margin-bottom:16px;">
                <div class="card-header">
                    <h3>${e.title}</h3>
                    ${getStatusBadge(e.status)}
                </div>
                <p>${e.description || ''}</p>
                <div class="card-meta">
                    <span>📅 ${formatDate(e.startDate)} — ${formatDate(e.endDate)}</span>
                </div>
            </div>
        `).join('');
    } catch (err) {
        container.innerHTML = `<div class="alert alert-error">Failed to load elections: ${err.message}</div>`;
    }
}

async function loadResults(electionId) {
    const container = document.getElementById('results-container');
    container.innerHTML = '<div class="loading"><div class="spinner"></div> Loading results...</div>';

    try {
        const election = await api.getElection(electionId);
        const results = await api.getResults(electionId);

        document.getElementById('results-header').innerHTML = `
            <h1>${election.title}</h1>
            <p style="color:var(--text-secondary);">Election Results ${getStatusBadge(election.status)}</p>
        `;

        if (!results || results.length === 0) {
            container.innerHTML = '<div class="text-center" style="padding:48px;color:var(--text-muted);">No votes have been cast yet.</div>';
            return;
        }

        const totalVotes = results.reduce((sum, r) => sum + r.voteCount, 0);
        let html = '';

        // Winner banner
        try {
            const winner = await api.getWinner(electionId);
            if (winner) {
                html += `
                    <div class="winner-banner">
                        <div class="trophy">🏆</div>
                        <h3>${winner.candidateName}</h3>
                        <div class="stats">${winner.voteCount} votes • ${winner.percentage}%</div>
                    </div>
                `;
            }
        } catch (e) { /* no winner yet */ }

        // Result bars
        const barClasses = ['', 'secondary', 'third'];
        results.forEach((r, i) => {
            const barClass = barClasses[i] || 'third';
            html += `
                <div class="result-item">
                    <div class="result-header">
                        <span class="candidate-name">${i === 0 ? '🥇' : i === 1 ? '🥈' : '🥉'} ${r.candidateName}</span>
                        <span class="vote-info">${r.voteCount} votes • ${r.percentage}%</span>
                    </div>
                    <div class="result-bar-track">
                        <div class="result-bar-fill ${barClass}" style="width: ${Math.max(r.percentage, 2)}%">
                            ${r.percentage}%
                        </div>
                    </div>
                </div>
            `;
        });

        // Total votes
        html += `
            <div class="total-votes-bar">
                <div class="count">${totalVotes}</div>
                <div class="label">Total Votes Cast</div>
            </div>
        `;

        container.innerHTML = html;

        // Animate bars
        setTimeout(() => {
            document.querySelectorAll('.result-bar-fill').forEach(bar => {
                bar.style.width = bar.style.width;
            });
        }, 100);

    } catch (err) {
        container.innerHTML = `<div class="alert alert-error">Failed to load results: ${err.message}</div>`;
    }
}
