let rmChart;
let progressaoData = [];
let selectedPosicao = null;
let selectedMetric = 'rm';

const METRICS = {
    rm: { label: '1RM estimado', unit: 'kg', title: '1RM estimado por semana (fórmula de Epley)', value: (p) => p.estimated1RM, decimals: 1 },
    reps: { label: 'Repetições', unit: '', title: 'Repetições por semana', value: (p) => p.reps, decimals: 0 },
    carga: { label: 'Carga', unit: 'kg', title: 'Carga por semana', value: (p) => p.carga, decimals: 1 }
};

document.addEventListener('DOMContentLoaded', () => {
    const exerciseId = document.body.dataset.exerciseId;
    loadExercise(exerciseId);
    loadProgression(exerciseId);

    document.getElementById('posicao-select').addEventListener('change', (event) => {
        selectedPosicao = parseInt(event.target.value, 10);
        redrawChart();
    });

    document.getElementById('metric-select').addEventListener('change', (event) => {
        selectedMetric = event.target.value;
        redrawChart();
    });

    document.getElementById('log-session-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        clearFormErrors(event.target);
        const submitButton = event.submitter;

        const semana = parseIntegerField('session-week');
        const series = collectSeries(event.target);
        if (series === null) {
            showAlert('Preencha carga e repetições de todas as séries.');
            return;
        }

        setButtonLoading(submitButton, true, 'Registrando...');
        try {
            await Api.postJson(`/api/exercises/${exerciseId}/sessoes`, { semana, series });
            document.getElementById('session-week').value = '';
            clearSeriesInputs(event.target);
            clearFormErrors(event.target);
            await loadProgression(exerciseId);
        } catch (err) {
            handleFormError(event.target, err);
        } finally {
            setButtonLoading(submitButton, false);
        }
    });
});

async function loadExercise(exerciseId) {
    try {
        const exercise = await Api.get(`/api/exercises/${exerciseId}`);
        document.getElementById('exercise-title').textContent =
            `${exercise.name} · ${exercise.muscleGroup} · ${exercise.trainingDay.name}`;
        renderSeriesInputs(exercise.seriesValidas);
    } catch (err) {
        document.getElementById('exercise-title').textContent = 'Exercício não encontrado';
        document.getElementById('series-rows').innerHTML =
            '<div class="empty-state is-compact">Não foi possível carregar as séries.</div>';
        showAlert(err.message);
    }
}

function renderSeriesInputs(seriesValidas) {
    const container = document.getElementById('series-rows');
    const total = Number.isInteger(seriesValidas) && seriesValidas > 0 ? seriesValidas : 3;
    let html = '';
    for (let posicao = 1; posicao <= total; posicao++) {
        html += `
        <div class="row g-2 align-items-end series-row" data-posicao="${posicao}">
            <div class="col-2">
                <span class="series-tag">${posicao}ª</span>
            </div>
            <div class="col-5">
                ${posicao === 1 ? '<label class="form-label">Carga (kg)</label>' : ''}
                <input type="text" inputmode="decimal" class="form-control series-carga" placeholder="60,5" required>
            </div>
            <div class="col-5">
                ${posicao === 1 ? '<label class="form-label">Reps</label>' : ''}
                <input type="number" min="1" class="form-control series-reps" required>
            </div>
        </div>`;
    }
    container.innerHTML = html;
}

function collectSeries(form) {
    const rows = form.querySelectorAll('.series-row');
    if (rows.length === 0) return null;
    const series = [];
    for (const row of rows) {
        const posicao = parseInt(row.dataset.posicao, 10);
        const cargaInput = row.querySelector('.series-carga');
        const repsInput = row.querySelector('.series-reps');
        const carga = parseNumberValue(cargaInput.value);
        const reps = parseNumberValue(repsInput.value);
        if (carga === null || carga <= 0 || reps === null || reps <= 0) {
            return null;
        }
        series.push({ posicao, carga, reps });
    }
    return series;
}

function clearSeriesInputs(form) {
    form.querySelectorAll('.series-carga, .series-reps').forEach((input) => {
        input.value = '';
    });
}

function parseNumberValue(raw) {
    const value = raw.trim().replace(',', '.');
    if (value === '') return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

async function loadProgression(exerciseId) {
    setProgressionLoading();
    try {
        progressaoData = await Api.get(`/api/exercises/${exerciseId}/progressao-series`);
        renderPosicaoSelector(progressaoData);
        renderTable(progressaoData);
        redrawChart();
    } catch (err) {
        progressaoData = [];
        document.getElementById('progression-table-body').innerHTML = tableStateRow(6, 'Não foi possível carregar os registros.');
        document.getElementById('chart-controls').classList.add('d-none');
        setChartState('Não foi possível carregar o gráfico.', 'empty');
        document.getElementById('trend-badge').classList.add('d-none');
        showAlert(err.message);
    }
}

function redrawChart() {
    const pontos = pontosForPosicao(selectedPosicao);
    const metric = METRICS[selectedMetric];
    document.getElementById('chart-title').textContent = metric.title;
    renderChart(pontos, metric);
    renderTrendBadge(pontos, metric);
}

// Transforma a lista por posição em linhas por semana → série 1, série 2...
function renderTable(data) {
    const body = document.getElementById('progression-table-body');
    if (data.length === 0) {
        body.innerHTML = tableStateRow(6, 'Sem registros ainda — registre a primeira sessão acima.');
        return;
    }

    const semanas = new Map();
    data.forEach((posicao) => {
        posicao.pontos.forEach((ponto) => {
            if (!semanas.has(ponto.semana)) {
                semanas.set(ponto.semana, []);
            }
            semanas.get(ponto.semana).push({ posicao: posicao.posicao, ...ponto });
        });
    });

    // Semana mais recente no topo — é a que interessa comparar ao abrir.
    const semanasOrdenadas = [...semanas.keys()].sort((a, b) => b - a);
    let html = '';
    semanasOrdenadas.forEach((semana) => {
        const series = semanas.get(semana).sort((a, b) => a.posicao - b.posicao);
        series.forEach((serie, index) => {
            html += `
        <tr${index === 0 ? ' class="week-start"' : ''}>
            <td>${index === 0 ? semana : ''}</td>
            <td>${serie.posicao}ª</td>
            <td>${formatCarga(serie.carga)} kg</td>
            <td>${serie.reps}</td>
            <td>${serie.estimated1RM.toFixed(1)} kg</td>
            <td>${formatTrendCell(serie.trendPercent)}</td>
        </tr>`;
        });
    });
    body.innerHTML = html;
}

function renderPosicaoSelector(data) {
    const controls = document.getElementById('chart-controls');
    const posicaoGroup = document.getElementById('posicao-group');
    const select = document.getElementById('posicao-select');
    if (data.length === 0) {
        controls.classList.add('d-none');
        selectedPosicao = null;
        return;
    }

    controls.classList.remove('d-none');
    const posicoes = data.map((item) => item.posicao).sort((a, b) => a - b);
    if (selectedPosicao === null || !posicoes.includes(selectedPosicao)) {
        selectedPosicao = posicoes[0];
    }
    select.innerHTML = posicoes.map((posicao) =>
        `<option value="${posicao}"${posicao === selectedPosicao ? ' selected' : ''}>${posicao}ª série</option>`
    ).join('');
    // Só faz sentido escolher a posição quando há mais de uma série.
    posicaoGroup.classList.toggle('d-none', posicoes.length <= 1);
}

function pontosForPosicao(posicao) {
    const found = progressaoData.find((item) => item.posicao === posicao);
    return found ? found.pontos : [];
}

function formatCarga(carga) {
    return Number.isInteger(carga) ? String(carga) : carga.toFixed(1).replace('.', ',');
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

// Tendência da métrica escolhida entre a semana atual e a anterior da mesma
// posição. Usa o flag do servidor (trendPercent === null) para saber quando a
// posição não existia na semana anterior (primeira vez / semana pulada).
function metricTrend(pontos, index, metric) {
    const ponto = pontos[index];
    if (index === 0 || ponto.trendPercent === null) return null;
    const anterior = metric.value(pontos[index - 1]);
    const atual = metric.value(ponto);
    if (anterior === 0) return null;
    return ((atual - anterior) / anterior) * 100;
}

function formatMetricValue(metric, value) {
    const rounded = value.toFixed(metric.decimals);
    const pretty = metric.decimals > 0 ? rounded.replace('.', ',') : rounded;
    return metric.unit ? `${pretty} ${metric.unit}` : pretty;
}

function renderChart(progression, metric) {
    const ctx = document.getElementById('rm-chart');
    if (rmChart) {
        rmChart.destroy();
        rmChart = null;
    }
    if (progression.length === 0) {
        setChartState('Sem registros suficientes para montar o gráfico.', 'empty');
        return;
    }
    if (typeof Chart === 'undefined') {
        setChartState('Não foi possível carregar o gráfico.', 'empty');
        return;
    }

    setChartState(null);
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
    const labels = progression.map((ponto) => `Semana ${ponto.semana}`);
    const data = progression.map((ponto) => metric.value(ponto));
    const pointColors = progression.map((_, index) => {
        const trend = metricTrend(progression, index, metric);
        if (trend === null) return faint;
        if (trend > 0) return positive;
        if (trend < 0) return negative;
        return warning;
    });
    const lastIndex = data.length - 1;
    const pointRadii = data.map((_, index) => index === lastIndex ? 6 : 4);
    const pointHoverRadii = data.map((_, index) => index === lastIndex ? 8 : 6);

    Chart.defaults.font.family = '"Inter", system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
    Chart.defaults.color = muted;

    rmChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: metric.unit ? `${metric.label} (${metric.unit})` : metric.label,
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
                        label: (context) => formatMetricValue(metric, context.parsed.y)
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
                        callback: (value) => metric.unit ? `${value} ${metric.unit}` : `${value}`
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

function renderTrendBadge(progression, metric) {
    const badge = document.getElementById('trend-badge');
    if (progression.length === 0) {
        badge.classList.add('d-none');
        return;
    }
    const trend = metricTrend(progression, progression.length - 1, metric);
    badge.classList.remove('d-none', 'is-loading', 'bg-success', 'bg-warning', 'bg-secondary', 'text-dark', 'trend-positive', 'trend-warning', 'trend-negative', 'trend-neutral');
    if (trend === null) {
        badge.classList.add('trend-neutral');
        badge.innerHTML = `${trendIcon('flat')} Primeira semana`;
    } else if (trend > 0) {
        badge.classList.add('trend-positive');
        badge.innerHTML = `${trendIcon('up')} Progredindo +${trend.toFixed(1)}%`;
    } else if (trend < 0) {
        badge.classList.add('trend-negative');
        badge.innerHTML = `${trendIcon('down')} Queda ${trend.toFixed(1)}%`;
    } else {
        badge.classList.add('trend-warning');
        badge.innerHTML = `${trendIcon('flat')} Sem progresso ${trend.toFixed(1)}%`;
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

function setProgressionLoading() {
    document.getElementById('progression-table-body').innerHTML = tableStateRow(6, 'Carregando registros...', 'loading');
    setChartState('Carregando gráfico...', 'loading');

    const badge = document.getElementById('trend-badge');
    badge.className = 'badge trend-badge is-loading';
    badge.textContent = 'Atualizando tendência';
}

function setChartState(message, type = 'empty') {
    const chartFrame = document.querySelector('.chart-frame');
    const chartState = document.getElementById('chart-state');
    const chartCanvas = document.getElementById('rm-chart');
    if (!chartFrame || !chartState || !chartCanvas) return;

    if (!message) {
        chartState.className = 'chart-state d-none';
        chartState.textContent = '';
        chartFrame.classList.remove('has-state');
        chartCanvas.classList.remove('d-none');
        return;
    }

    chartState.className = `chart-state ${type === 'loading' ? 'loading-state' : 'empty-state'} is-compact`;
    chartState.textContent = message;
    chartFrame.classList.add('has-state');
    chartCanvas.classList.add('d-none');
}
