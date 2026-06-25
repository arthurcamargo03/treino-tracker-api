let activeDayFilter = 'all';

document.addEventListener('DOMContentLoaded', () => {
    loadAll();

    document.getElementById('add-exercise-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const name = document.getElementById('exercise-name').value.trim();
        const muscleGroup = document.getElementById('exercise-group').value.trim();
        const trainingDayId = Number(document.getElementById('exercise-training-day').value);
        try {
            await Api.postJson('/api/exercises', { name, muscleGroup, trainingDayId });
            event.target.reset();
            loadAll();
        } catch (err) {
            showAlert(err.message);
        }
    });

    document.getElementById('add-training-day-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();
        const name = document.getElementById('training-day-name').value.trim();
        const dayOfWeek = document.getElementById('training-day-of-week').value;
        try {
            await Api.postJson('/api/training-days', { name, dayOfWeek });
            event.target.reset();
            loadAll();
        } catch (err) {
            showAlert(err.message);
        }
    });
});

async function loadAll() {
    const groupsContainer = document.getElementById('exercise-groups');
    try {
        const [trainingDays, exercises] = await Promise.all([
            Api.get('/api/training-days'),
            Api.get('/api/exercises')
        ]);
        renderTrainingDaySelect(trainingDays);
        renderDayNav(trainingDays);
        renderExerciseGroups(trainingDays, exercises);
    } catch (err) {
        groupsContainer.innerHTML = '';
        showAlert(err.message);
    }
}

function renderTrainingDaySelect(trainingDays) {
    const select = document.getElementById('exercise-training-day');
    const placeholder = select.querySelector('option[value=""]');
    select.innerHTML = '';
    select.appendChild(placeholder);
    trainingDays.forEach((day) => {
        const option = document.createElement('option');
        option.value = day.id;
        option.textContent = `${day.name} — ${capitalize(day.dayOfWeekLabel)}`;
        select.appendChild(option);
    });
}

function renderDayNav(trainingDays) {
    const nav = document.getElementById('day-nav');
    nav.innerHTML = '';
    nav.appendChild(navButton('Todos', 'all', activeDayFilter === 'all'));
    trainingDays.forEach((day) => {
        nav.appendChild(navButton(day.name, String(day.id), activeDayFilter === String(day.id)));
    });
    nav.querySelectorAll('button').forEach((btn) => {
        btn.addEventListener('click', () => {
            activeDayFilter = btn.dataset.filter;
            document.querySelectorAll('#exercise-groups [data-day-id]').forEach((section) => {
                section.classList.toggle('d-none', activeDayFilter !== 'all' && section.dataset.dayId !== activeDayFilter);
            });
            nav.querySelectorAll('button').forEach((b) => {
                b.classList.toggle('btn-primary', b === btn);
                b.classList.toggle('active', b === btn);
                b.classList.toggle('btn-outline-primary', b !== btn);
            });
        });
    });
}

function navButton(label, filterValue, isActive) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `btn btn-sm ${isActive ? 'btn-primary active' : 'btn-outline-primary'}`;
    btn.dataset.filter = filterValue;
    btn.textContent = label;
    return btn;
}

function renderExerciseGroups(trainingDays, exercises) {
    const container = document.getElementById('exercise-groups');
    if (trainingDays.length === 0) {
        container.innerHTML = '<p class="text-muted">Cadastre um treino para começar a organizar seus exercícios.</p>';
        return;
    }

    const exercisesByDay = new Map();
    exercises.forEach((exercise) => {
        const dayId = exercise.trainingDay.id;
        if (!exercisesByDay.has(dayId)) {
            exercisesByDay.set(dayId, []);
        }
        exercisesByDay.get(dayId).push(exercise);
    });

    container.innerHTML = trainingDays.map((day) => groupHtml(day, exercisesByDay.get(day.id) || [])).join('');

    container.querySelectorAll('[data-day-id]').forEach((section) => {
        section.classList.toggle('d-none', activeDayFilter !== 'all' && section.dataset.dayId !== activeDayFilter);
    });

    container.querySelectorAll('.exercise-card').forEach((card) => {
        card.addEventListener('click', () => {
            window.location.href = `/exercises/${card.dataset.id}`;
        });
    });

    exercises.forEach((exercise) => loadTrendBadge(exercise.id));
}

function groupHtml(day, exercises) {
    const cards = exercises.length > 0
        ? exercises.map(cardHtml).join('')
        : '<p class="text-muted">Nenhum exercício cadastrado neste treino ainda.</p>';
    return `
        <section data-day-id="${day.id}" class="mb-4">
            <h2 class="h5 mt-4 mb-3 pb-2 border-bottom">
                ${escapeHtml(day.name)}
                <small class="text-muted">${capitalize(day.dayOfWeekLabel)}</small>
            </h2>
            <div class="row g-3">${cards}</div>
        </section>`;
}

function cardHtml(exercise) {
    return `
        <div class="col-md-4">
            <div class="card exercise-card shadow-sm h-100" data-id="${exercise.id}">
                <div class="card-body">
                    <h3 class="h5 mb-1">${escapeHtml(exercise.name)}</h3>
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

function capitalize(str) {
    return str.length === 0 ? str : str.charAt(0).toUpperCase() + str.slice(1);
}
