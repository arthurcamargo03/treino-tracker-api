let rmChart;

document.addEventListener('DOMContentLoaded', () => {
    const exerciseId = document.body.dataset.exerciseId;
    loadExercise(exerciseId);
    loadProgression(exerciseId);

    document.getElementById('log-set-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const payload = {
            week: Number(document.getElementById('set-week').value),
            weight: Number(document.getElementById('set-weight').value),
            reps: Number(document.getElementById('set-reps').value),
            sets: Number(document.getElementById('set-sets').value)
        };
        try {
            await Api.postJson(`/api/exercises/${exerciseId}/sets`, payload);
            event.target.reset();
            loadProgression(exerciseId);
        } catch (err) {
            showAlert(err.message);
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
            <td>${week.trendPercent === null ? '—' : (week.trendPercent > 0 ? '📈 ' : '📉 ') + week.trendPercent.toFixed(1) + '%'}</td>
        </tr>
    `).join('');
}

function renderChart(progression) {
    const ctx = document.getElementById('rm-chart');
    const labels = progression.map((week) => `Semana ${week.week}`);
    const data = progression.map((week) => week.estimated1RM);
    const pointColors = progression.map((week) => {
        if (week.trendPercent === null) return '#6c757d';
        return week.trendPercent > 0 ? '#198754' : '#dc3545';
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
                borderColor: '#0d6efd',
                backgroundColor: 'rgba(13, 110, 253, 0.1)',
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
    badge.classList.remove('d-none', 'bg-success', 'bg-warning', 'bg-secondary', 'text-dark');
    if (last.trendPercent === null) {
        badge.classList.add('bg-secondary');
        badge.textContent = 'Primeira semana';
    } else if (last.trendPercent > 0) {
        badge.classList.add('bg-success');
        badge.textContent = `📈 Progredindo +${last.trendPercent.toFixed(1)}%`;
    } else {
        badge.classList.add('bg-warning', 'text-dark');
        badge.textContent = `Sem progresso ${last.trendPercent.toFixed(1)}%`;
    }
}
