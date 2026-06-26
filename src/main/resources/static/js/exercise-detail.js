let rmChart;

document.addEventListener('DOMContentLoaded', () => {
    const exerciseId = document.body.dataset.exerciseId;
    loadExercise(exerciseId);
    loadProgression(exerciseId);

    document.getElementById('log-set-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        clearFormErrors(event.target);
        const payload = {
            week: parseIntegerField('set-week'),
            weight: parseDecimalField('set-weight'),
            reps: parseIntegerField('set-reps'),
            sets: parseIntegerField('set-sets')
        };
        try {
            await Api.postJson(`/api/exercises/${exerciseId}/sets`, payload);
            event.target.reset();
            clearFormErrors(event.target);
            loadProgression(exerciseId);
        } catch (err) {
            handleFormError(event.target, err);
        }
    });
});

async function loadExercise(exerciseId) {
    try {
        const exercise = await Api.get(`/api/exercises/${exerciseId}`);
        document.getElementById('exercise-title').textContent =
            `${exercise.name} · ${exercise.muscleGroup} · ${exercise.trainingDay.name}`;
    } catch (err) {
        document.getElementById('exercise-title').textContent = 'Exercício não encontrado';
        showAlert(err.message);
    }
}

async function loadProgression(exerciseId) {
    try {
        const progression = await Api.get(`/api/exercises/${exerciseId}/progression`);
        renderTable(progression);
        renderChart(progression);
        renderTrendBadge(progression);
    } catch (err) {
        showAlert(err.message);
    }
}

function renderTable(progression) {
    const body = document.getElementById('progression-table-body');
    if (progression.length === 0) {
        body.innerHTML = '<tr><td colspan="6" class="text-muted">Sem dados ainda.</td></tr>';
        return;
    }
    body.innerHTML = progression.map((week) => `
        <tr>
            <td>${week.week}</td>
            <td>${week.weight} kg</td>
            <td>${week.reps}</td>
            <td>${week.sets}</td>
            <td>${week.estimated1RM.toFixed(1)} kg</td>
            <td>${formatTrendCell(week.trendPercent)}</td>
        </tr>
    `).join('');
}

function formatTrendCell(trendPercent) {
    if (trendPercent === null) {
        return '—';
    }
    if (trendPercent > 0) {
        return `Alta +${trendPercent.toFixed(1)}%`;
    }
    if (trendPercent < 0) {
        return `Queda ${trendPercent.toFixed(1)}%`;
    }
    return `Sem progresso ${trendPercent.toFixed(1)}%`;
}

function renderChart(progression) {
    const ctx = document.getElementById('rm-chart');
    const styles = getComputedStyle(document.documentElement);
    const accent = styles.getPropertyValue('--accent').trim() || '#10B981';
    const accentFill = 'rgba(16, 185, 129, 0.10)';
    const labels = progression.map((week) => `Semana ${week.week}`);
    const data = progression.map((week) => week.estimated1RM);
    const pointColors = progression.map((week) => {
        if (week.trendPercent === null) return styles.getPropertyValue('--faint').trim() || '#94A3B8';
        return week.trendPercent > 0
            ? styles.getPropertyValue('--progress-positive').trim() || '#16A34A'
            : styles.getPropertyValue('--progress-negative').trim() || '#DC2626';
    });

    if (rmChart) {
        rmChart.data.labels = labels;
        rmChart.data.datasets[0].data = data;
        rmChart.data.datasets[0].pointBackgroundColor = pointColors;
        rmChart.update();
        return;
    }

    rmChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: '1RM estimado (kg)',
                data,
                borderColor: accent,
                backgroundColor: accentFill,
                pointBackgroundColor: pointColors,
                pointRadius: 5,
                tension: 0.3,
                fill: true
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { ticks: { callback: (value) => `${value} kg` } } }
        }
    });
}

function renderTrendBadge(progression) {
    const badge = document.getElementById('trend-badge');
    if (progression.length === 0) {
        badge.classList.add('d-none');
        return;
    }
    const last = progression[progression.length - 1];
    badge.classList.remove('d-none', 'bg-success', 'bg-warning', 'bg-secondary', 'text-dark', 'trend-positive', 'trend-warning', 'trend-negative');
    if (last.trendPercent === null) {
        badge.classList.add('bg-secondary');
        badge.textContent = 'Primeira semana';
    } else if (last.trendPercent > 0) {
        badge.classList.add('trend-positive');
        badge.innerHTML = `${trendIcon('up')} Progredindo +${last.trendPercent.toFixed(1)}%`;
    } else if (last.trendPercent < 0) {
        badge.classList.add('trend-negative');
        badge.innerHTML = `${trendIcon('down')} Queda ${last.trendPercent.toFixed(1)}%`;
    } else {
        badge.classList.add('trend-warning');
        badge.innerHTML = `${trendIcon('flat')} Sem progresso ${last.trendPercent.toFixed(1)}%`;
    }
}

function trendIcon(direction) {
    const paths = {
        up: '<path d="M7 17 17 7"/><path d="M7 7h10v10"/>',
        down: '<path d="m7 7 10 10"/><path d="M17 7v10H7"/>',
        flat: '<path d="M5 12h14"/><path d="m15 8 4 4-4 4"/>'
    };
    return `<svg class="trend-icon" aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${paths[direction]}</svg>`;
}
