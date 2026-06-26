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
        return '<span class="trend-cell trend-neutral">Primeira</span>';
    }
    if (trendPercent > 0) {
        return `<span class="trend-cell trend-positive">${trendIcon('up')} +${trendPercent.toFixed(1)}%</span>`;
    }
    if (trendPercent < 0) {
        return `<span class="trend-cell trend-negative">${trendIcon('down')} ${trendPercent.toFixed(1)}%</span>`;
    }
    return `<span class="trend-cell trend-warning">${trendIcon('flat')} ${trendPercent.toFixed(1)}%</span>`;
}

function renderChart(progression) {
    const ctx = document.getElementById('rm-chart');
    const styles = getComputedStyle(document.documentElement);
    const accent = styles.getPropertyValue('--accent').trim() || '#10B981';
    const ink = styles.getPropertyValue('--ink').trim() || '#0F172A';
    const muted = styles.getPropertyValue('--muted').trim() || '#475569';
    const border = styles.getPropertyValue('--border').trim() || '#E2E8F0';
    const faint = styles.getPropertyValue('--faint').trim() || '#94A3B8';
    const surface = styles.getPropertyValue('--surface').trim() || '#FFFFFF';
    const positive = styles.getPropertyValue('--progress-positive').trim() || '#16A34A';
    const warning = styles.getPropertyValue('--progress-warning').trim() || '#D97706';
    const negative = styles.getPropertyValue('--progress-negative').trim() || '#DC2626';
    const accentFill = 'rgba(16, 185, 129, 0.08)';
    const gridColor = colorWithAlpha(border, 0.72);
    const labels = progression.map((week) => `Semana ${week.week}`);
    const data = progression.map((week) => week.estimated1RM);
    const pointColors = progression.map((week) => {
        if (week.trendPercent === null) return faint;
        if (week.trendPercent > 0) return positive;
        if (week.trendPercent < 0) return negative;
        return warning;
    });
    const lastIndex = data.length - 1;
    const pointRadii = data.map((_, index) => index === lastIndex ? 6 : 4);
    const pointHoverRadii = data.map((_, index) => index === lastIndex ? 8 : 6);

    if (rmChart) {
        rmChart.data.labels = labels;
        rmChart.data.datasets[0].data = data;
        rmChart.data.datasets[0].pointBackgroundColor = pointColors;
        rmChart.data.datasets[0].pointRadius = pointRadii;
        rmChart.data.datasets[0].pointHoverRadius = pointHoverRadii;
        rmChart.data.datasets[0].borderColor = accent;
        rmChart.data.datasets[0].backgroundColor = accentFill;
        rmChart.update();
        return;
    }

    Chart.defaults.font.family = '"Inter", system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
    Chart.defaults.color = muted;

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
                pointBorderColor: surface,
                pointBorderWidth: 2,
                pointHoverBackgroundColor: accent,
                pointHoverBorderColor: surface,
                pointRadius: pointRadii,
                pointHoverRadius: pointHoverRadii,
                borderWidth: 2.5,
                tension: 0.32,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'index'
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: ink,
                    borderColor: colorWithAlpha(border, 0.24),
                    borderWidth: 1,
                    bodyColor: surface,
                    displayColors: false,
                    padding: 12,
                    titleColor: surface,
                    titleFont: { family: Chart.defaults.font.family, size: 12, weight: '600' },
                    bodyFont: { family: Chart.defaults.font.family, size: 13, weight: '500' },
                    callbacks: {
                        label: (context) => `${context.parsed.y.toFixed(1)} kg`
                    }
                }
            },
            scales: {
                x: {
                    border: { display: false },
                    grid: { color: gridColor, drawTicks: false },
                    ticks: {
                        color: muted,
                        font: { family: Chart.defaults.font.family, size: 12, weight: '500' },
                        padding: 10
                    }
                },
                y: {
                    border: { display: false },
                    grid: { color: gridColor, drawTicks: false },
                    ticks: {
                        color: muted,
                        font: { family: Chart.defaults.font.family, size: 12, weight: '500' },
                        padding: 10,
                        callback: (value) => `${value} kg`
                    }
                }
            }
        }
    });
}

function colorWithAlpha(hex, alpha) {
    const normalized = hex.replace('#', '');
    if (normalized.length !== 6) {
        return hex;
    }
    const r = parseInt(normalized.slice(0, 2), 16);
    const g = parseInt(normalized.slice(2, 4), 16);
    const b = parseInt(normalized.slice(4, 6), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function renderTrendBadge(progression) {
    const badge = document.getElementById('trend-badge');
    if (progression.length === 0) {
        badge.classList.add('d-none');
        return;
    }
    const last = progression[progression.length - 1];
    badge.classList.remove('d-none', 'bg-success', 'bg-warning', 'bg-secondary', 'text-dark', 'trend-positive', 'trend-warning', 'trend-negative', 'trend-neutral');
    if (last.trendPercent === null) {
        badge.classList.add('trend-neutral');
        badge.innerHTML = `${trendIcon('flat')} Primeira semana`;
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
