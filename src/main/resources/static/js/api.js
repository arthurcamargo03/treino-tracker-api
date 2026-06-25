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
    async _handle(res) {
        if (res.status === 204) return null;
        const data = await res.json().catch(() => null);
        if (!res.ok) {
            throw new ApiError(data);
        }
        return data;
    }
};

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

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
