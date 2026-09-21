/**
 * CivicVote Voting Module
 */

let selectedCandidateId = null;

document.addEventListener('DOMContentLoaded', () => {
    if (!requireAuth()) return;
    setupNavbar();

    const urlParams = new URLSearchParams(window.location.search);
    const electionId = urlParams.get('id');

    if (!electionId) {
        window.location.href = 'elections.html';
        return;
    }

    loadElectionForVoting(electionId);
});

async function loadElectionForVoting(electionId) {
    const container = document.getElementById('vote-container');

    try {
        const election = await api.getElection(electionId);
        const candidates = await api.getCandidates(electionId);

        // Check if already voted
        let hasVoted = false;
        try {
            const status = await api.getVoteStatus(electionId);
            hasVoted = status.hasVoted;
        } catch (e) { /* ignore */ }

        // Render election info
        document.getElementById('election-title').textContent = election.title;
        document.getElementById('election-desc').textContent = election.description || '';
        document.getElementById('election-status').innerHTML = getStatusBadge(election.status);
        document.getElementById('election-dates').textContent =
            `${formatDate(election.startDate)} — ${formatDate(election.endDate)}`;

        // Render candidates
        const list = document.getElementById('candidate-list');

        if (hasVoted) {
            list.innerHTML = '';
            showAlert(container, 'You have already voted in this election.', 'info');
            document.getElementById('vote-btn').classList.add('hidden');
            return;
        }

        if (election.status !== 'ACTIVE') {
            list.innerHTML = '';
            showAlert(container, `This election is ${election.status}. Voting is not available.`, 'info');
            document.getElementById('vote-btn').classList.add('hidden');

            // Still show candidates for reference
            if (candidates.length > 0) {
                list.innerHTML = candidates.map(c => `
                    <div class="candidate-option" style="cursor:default;">
                        <div class="radio-circle"></div>
                        <div class="candidate-info">
                            <h4>${c.candidateName}</h4>
                            <p>${c.description || ''}</p>
                        </div>
                    </div>
                `).join('');
            }
            return;
        }

        if (candidates.length === 0) {
            list.innerHTML = '<p style="color:var(--text-muted);padding:24px;">No candidates registered for this election.</p>';
            document.getElementById('vote-btn').classList.add('hidden');
            return;
        }

        list.innerHTML = candidates.map(c => `
            <div class="candidate-option" data-id="${c.candidateId}" onclick="selectCandidate(this, ${c.candidateId})">
                <div class="radio-circle"></div>
                <div class="candidate-info">
                    <h4>${c.candidateName}</h4>
                    <p>${c.description || ''}</p>
                </div>
            </div>
        `).join('');

    } catch (err) {
        showAlert(container, 'Failed to load election: ' + err.message);
    }
}

function selectCandidate(element, candidateId) {
    // Deselect all
    document.querySelectorAll('.candidate-option').forEach(el => el.classList.remove('selected'));
    // Select this one
    element.classList.add('selected');
    selectedCandidateId = candidateId;
    document.getElementById('vote-btn').disabled = false;
}

async function submitVote() {
    if (!selectedCandidateId) {
        alert('Please select a candidate');
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const electionId = parseInt(urlParams.get('id'));
    const btn = document.getElementById('vote-btn');
    const container = document.getElementById('vote-container');

    btn.disabled = true;
    btn.textContent = 'Submitting vote...';

    try {
        const result = await api.castVote({
            electionId: electionId,
            candidateId: selectedCandidateId
        });

        showAlert(container, result.message || 'Vote successfully recorded!', 'success');
        btn.classList.add('hidden');

        // Disable candidate selection
        document.querySelectorAll('.candidate-option').forEach(el => {
            el.style.pointerEvents = 'none';
            el.style.opacity = '0.6';
        });

        // Keep the selected one highlighted
        document.querySelector(`.candidate-option[data-id="${selectedCandidateId}"]`).style.opacity = '1';

    } catch (err) {
        if (err.status === 409) {
            showAlert(container, 'You have already voted in this election.', 'error');
            btn.classList.add('hidden');
        } else {
            showAlert(container, err.message || 'Failed to submit vote');
            btn.disabled = false;
            btn.textContent = 'Cast Your Vote';
        }
    }
}
