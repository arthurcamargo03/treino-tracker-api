document.addEventListener('DOMContentLoaded', () => {
    loadExercises();

    document.getElementById('add-exercise-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const name = document.getElementById('exercise-name').value.trim();
        const muscleGroup = document.getElementById('exercise-group').value.trim();
        try {
            await Api.postJson('/api/exercises', { name, muscleGroup });
            event.target.reset();
            loadExercises();
        } catch (err) {
            showAlert(err.message);
        }
    });
});

async function loadExercises() {
    const container = document.getElementById('exercise-cards');
    try {
        const exercises = await Api.get('/api/exercises');
        if (exercises.length === 0) {
            container.innerHTML = '<p class="text-muted">Nenhum exercício cadastrado ainda.</p>';
            return;
        }
        container.innerHTML = exercises.map(cardHtml).join('');
        container.querySelectorAll('.exercise-card').forEach((card) => {
            card.addEventListener('click', () => {
                window.location.href = `/exercises/${card.dataset.id}`;
            });
        });
        exercises.forEach((exercise) => loadTrendBadge(exercise.id));
    } catch (err) {
        container.innerHTML = '';
        showAlert(err.message);
    }
}

function cardHtml(exercise) {
    return `
        <div class="col-md-4">
            <div class="card exercise-card shadow-sm h-100" data-id="${exercise.id}">
                <div class="card-body">
                    <h2 class="h5 mb-1">${escapeHtml(exercise.name)}</h2>
                    <span class="badge bg-secondary">${escapeHtml(exercise.muscleGroup)}</span>
                    <span class="badge bg-light text-muted ms-1 d-none" id="trend-badge-${exercise.id}">...</span>
                </div>
            </div>
        </div>`;
}

async function loadTrendBadge(exerciseId) {
    const badge = document.getElementById(`trend-badge-${exerciseId}`);
    if (!badge) return;
    try {
        const progression = await Api.get(`/api/exercises/${exerciseId}/progression`);
        if (progression.length === 0) {
            badge.remove();
            return;
        }
        const last = progression[progression.length - 1];
        if (last.trendPercent === null) {
            badge.remove();
            return;
        }
        badge.classList.remove('d-none', 'bg-light', 'text-muted');
        if (last.trendPercent > 0) {
            badge.classList.add('bg-success');
            badge.textContent = `📈 Progredindo +${last.trendPercent.toFixed(1)}%`;
        } else {
            badge.classList.add('bg-warning', 'text-dark');
            badge.textContent = `Sem progresso ${last.trendPercent.toFixed(1)}%`;
        }
    } catch {
        badge.remove();
    }
}
