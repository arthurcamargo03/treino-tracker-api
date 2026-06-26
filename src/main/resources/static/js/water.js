const RING_RADIUS = 52;
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS;
const SVG_NS = 'http://www.w3.org/2000/svg';

document.addEventListener('DOMContentLoaded', () => {
    loadToday();
    loadSettings();

    document.getElementById('drink-bottle-btn').addEventListener('click', async (event) => {
        hideAlert();
        setButtonLoading(event.currentTarget, true, 'Registrando...');
        try {
            const today = await Api.postJson('/api/water/drink');
            updateRing(today);
        } catch (err) {
            showAlert(err.message);
        } finally {
            setButtonLoading(event.currentTarget, false);
        }
    });

    document.getElementById('drink-custom-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        clearFormErrors(event.target);
        const submitButton = event.submitter;
        const ml = parseIntegerField('custom-ml');
        setButtonLoading(submitButton, true, 'Registrando...');
        try {
            const today = await Api.postJson('/api/water/drink', { ml });
            event.target.reset();
            clearFormErrors(event.target);
            updateRing(today);
        } catch (err) {
            handleFormError(event.target, err);
        } finally {
            setButtonLoading(submitButton, false);
        }
    });

    document.getElementById('settings-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        clearFormErrors(event.target);
        const submitButton = event.submitter;
        const payload = {
            dailyGoalMl: parseIntegerField('daily-goal'),
            bottleSizeMl: parseIntegerField('bottle-size')
        };
        setButtonLoading(submitButton, true, 'Salvando...');
        try {
            await Api.putJson('/api/water/settings', payload);
            clearFormErrors(event.target);
            showAlert('Configurações salvas.', 'success');
            await loadToday();
        } catch (err) {
            handleFormError(event.target, err);
        } finally {
            setButtonLoading(submitButton, false);
        }
    });
});

async function loadToday() {
    setWaterLoading(true);
    try {
        const today = await Api.get('/api/water/today');
        updateRing(today);
    } catch (err) {
        showAlert(err.message);
    } finally {
        setWaterLoading(false);
    }
}

async function loadSettings() {
    try {
        const settings = await Api.get('/api/water/settings');
        document.getElementById('daily-goal').value = settings.dailyGoalMl;
        document.getElementById('bottle-size').value = settings.bottleSizeMl;
    } catch (err) {
        showAlert(err.message);
    }
}

function updateRing(today) {
    const percent = Math.min(today.percent, 100);
    renderBottleSegments(today.bottlesForGoal, today.completedBottles);

    document.getElementById('ring-percent').textContent = `${Math.round(today.percent)}%`;
    document.getElementById('ring-amounts').textContent = `${today.consumedMl} / ${today.goalMl} ml`;
    document.getElementById('bottle-summary').textContent = bottleSummary(today);

    document.getElementById('goal-badge').classList.toggle('d-none', !today.goalReached);
}

function setWaterLoading(isLoading) {
    const wrapper = document.querySelector('.water-ring-wrapper');
    wrapper?.classList.toggle('is-loading', isLoading);
    wrapper?.setAttribute('aria-busy', String(isLoading));

    if (!isLoading) return;
    renderBottleSegments(1, 0);
    document.getElementById('ring-percent').textContent = '...';
    document.getElementById('ring-amounts').textContent = 'Carregando';
    document.getElementById('bottle-summary').textContent = 'Carregando hidratação...';
    document.getElementById('goal-badge').classList.add('d-none');
}

function renderBottleSegments(totalSegments, completedSegments) {
    const group = document.getElementById('ring-segments');
    group.replaceChildren();

    const safeTotal = Math.max(1, totalSegments);
    const safeCompleted = Math.min(Math.max(0, completedSegments), safeTotal);
    const step = RING_CIRCUMFERENCE / safeTotal;
    const gap = safeTotal > 1 ? Math.min(5, step * 0.18) : 0;
    const segmentLength = Math.max(1, step - gap);

    for (let index = 0; index < safeTotal; index += 1) {
        group.appendChild(createSegment(index, segmentLength, gap, index < safeCompleted));
    }
}

function createSegment(index, segmentLength, gap, isFilled) {
    const circle = document.createElementNS(SVG_NS, 'circle');
    circle.setAttribute('cx', '60');
    circle.setAttribute('cy', '60');
    circle.setAttribute('r', String(RING_RADIUS));
    circle.setAttribute('class', isFilled ? 'ring-segment is-filled' : 'ring-segment');
    circle.style.strokeDasharray = `${segmentLength} ${RING_CIRCUMFERENCE - segmentLength}`;
    circle.style.strokeDashoffset = String(-(index * (segmentLength + gap)) - gap / 2);
    circle.style.animationDelay = isFilled ? `${index * 45}ms` : '0ms';
    return circle;
}

function bottleSummary(today) {
    const verb = today.remainingBottles === 1 ? 'falta' : 'faltam';
    return `Meta: ${today.bottlesForGoal} garrafas de ${today.bottleSizeMl}ml · ${verb} ${today.remainingBottles}`;
}
