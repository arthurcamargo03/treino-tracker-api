class ApiError extends Error {
    constructor(body) {
        super(ApiError.describe(body));
        this.body = body;
    }

    static describe(body) {
        if (!body) return 'Erro inesperado.';
        if (body.fieldErrors) {
            return Object.entries(body.fieldErrors).map(([field, msg]) => `${field}: ${msg}`).join(' · ');
        }
        return body.message || 'Erro inesperado.';
    }
}

const Api = {
    async get(url) {
        const res = await fetch(url);
        return Api._handle(res);
    },
    async postJson(url, body) {
        const options = { method: 'POST' };
        if (body !== undefined) {
            options.headers = { 'Content-Type': 'application/json' };
            options.body = JSON.stringify(body);
        }
        const res = await fetch(url, options);
        return Api._handle(res);
    },
    async putJson(url, body) {
        const res = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        return Api._handle(res);
    },
    async delete(url) {
        const res = await fetch(url, { method: 'DELETE' });
        return Api._handle(res);
    },
    async _handle(res) {
        if (res.status === 204) return null;
        const data = await res.json().catch(() => null);
        if (!res.ok) {
            throw new ApiError(data);
        }
        return data;
    }
};

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form').forEach((form) => {
        form.addEventListener('input', (event) => clearFieldError(event.target));
        form.addEventListener('change', (event) => clearFieldError(event.target));
    });
});

function showAlert(message, type = 'danger') {
    const box = document.getElementById('alert-box');
    if (!box) return;
    box.textContent = message;
    box.className = `alert alert-${type}`;
}

function hideAlert() {
    const box = document.getElementById('alert-box');
    if (!box) return;
    box.className = 'alert d-none';
}

function clearFormErrors(form) {
    if (!form) return;
    form.querySelectorAll('.is-invalid').forEach((field) => {
        clearFieldError(field);
    });
}

function showFormErrors(form, fieldErrors) {
    if (!form || !fieldErrors) return;
    clearFormErrors(form);
    Object.entries(fieldErrors).forEach(([field, message]) => {
        const control = form.querySelector(`[data-field="${field}"]`);
        if (!control) return;
        const error = document.createElement('div');
        const errorId = `${control.id || field}-error`;
        error.id = errorId;
        error.className = 'invalid-feedback';
        error.textContent = message;
        control.classList.add('is-invalid');
        control.setAttribute('aria-describedby', errorId);
        const inputGroup = control.closest('.input-group');
        if (inputGroup) {
            inputGroup.insertAdjacentElement('afterend', error);
        } else {
            control.insertAdjacentElement('afterend', error);
        }
    });
}

function clearFieldError(control) {
    if (!control?.classList?.contains('is-invalid')) return;
    const describedBy = control.getAttribute('aria-describedby');
    control.classList.remove('is-invalid');
    control.removeAttribute('aria-describedby');
    if (describedBy) {
        document.getElementById(describedBy)?.remove();
    }
}

function handleFormError(form, err) {
    showAlert(err.message);
    showFormErrors(form, err.body?.fieldErrors);
}

function loadingState(message = 'Carregando...') {
    return `<div class="loading-state">${escapeHtml(message)}</div>`;
}

function emptyState(message) {
    return `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function tableStateRow(colspan, message, type = 'empty') {
    const stateClass = type === 'loading' ? 'loading-state' : 'empty-state';
    return `<tr><td colspan="${colspan}" class="table-state"><div class="${stateClass} is-compact">${escapeHtml(message)}</div></td></tr>`;
}

function setButtonLoading(button, isLoading, loadingText = 'Carregando...') {
    if (!button) return;
    if (isLoading) {
        button.dataset.originalText = button.dataset.originalText || button.textContent;
        button.textContent = loadingText;
        button.disabled = true;
        button.classList.add('is-loading');
        button.setAttribute('aria-busy', 'true');
        return;
    }

    button.textContent = button.dataset.originalText || button.textContent;
    delete button.dataset.originalText;
    button.disabled = false;
    button.classList.remove('is-loading');
    button.removeAttribute('aria-busy');
}

function parseIntegerField(id) {
    return parseNumberField(id) ?? 0;
}

function parseOptionalIntegerField(id) {
    return parseNumberField(id);
}

function parseDecimalField(id) {
    return parseNumberField(id) ?? 0;
}

function parseNumberField(id) {
    const value = document.getElementById(id).value.trim().replace(',', '.');
    if (value === '') return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
