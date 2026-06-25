const RING_CIRCUMFERENCE = 2 * Math.PI * 52;

document.addEventListener('DOMContentLoaded', () => {
    loadToday();
    loadSettings();

    document.getElementById('drink-bottle-btn').addEventListener('click', async () => {
        hideAlert();
        try {
            await Api.postJson('/api/water/drink');
            loadToday();
        } catch (err) {
            showAlert(err.message);
        }
    });

    document.getElementById('drink-custom-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const ml = Number(document.getElementById('custom-ml').value);
        try {
            await Api.postJson('/api/water/drink', { ml });
            event.target.reset();
            loadToday();
        } catch (err) {
            showAlert(err.message);
        }
    });

    document.getElementById('settings-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const payload = {
            dailyGoalMl: Number(document.getElementById('daily-goal').value),
            bottleSizeMl: Number(document.getElementById('bottle-size').value)
        };
        try {
            await Api.putJson('/api/water/settings', payload);
            showAlert('Configurações salvas.', 'success');
            loadToday();
        } catch (err) {
            showAlert(err.message);
        }
    });
});

async function loadToday() {
    try {
        const today = await Api.get('/api/water/today');
        updateRing(today);
    } catch (err) {
        showAlert(err.message);
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
    const offset = RING_CIRCUMFERENCE * (1 - percent / 100);
    const ring = document.getElementById('ring-progress');
    ring.style.strokeDasharray = String(RING_CIRCUMFERENCE);
    ring.style.strokeDashoffset = String(offset);

    document.getElementById('ring-percent').textContent = `${Math.round(today.percent)}%`;
    document.getElementById('ring-amounts').textContent = `${today.consumedMl} / ${today.goalMl} ml`;

    document.getElementById('goal-badge').classList.toggle('d-none', !today.goalReached);
}
