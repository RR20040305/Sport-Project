const API_URL = '/api';
let token = localStorage.getItem('accessToken');
let currentUser = null;
let facilitiesCache = [];
let currentBookings = [];

if (token) initApp();

async function initApp() {
    try {
        const res = await fetch(`${API_URL}/auth/me`, { headers: { 'Authorization': `Bearer ${token}` } });
        if (!res.ok) throw new Error('Не авторизован');
        
        currentUser = await res.json();
        document.getElementById('login-screen').classList.add('hidden');
        document.getElementById('main-screen').classList.remove('hidden');
        
        document.getElementById('user-email').textContent = currentUser.email;
        document.getElementById('user-role').textContent = currentUser.roles.join(', ');
        document.getElementById('profile-email').textContent = currentUser.email;
        document.getElementById('profile-name').textContent = currentUser.fullName;
        document.getElementById('profile-roles').textContent = currentUser.roles.join(', ');

        const isAdmin = currentUser.roles.includes('ROLE_ADMIN');
        document.querySelectorAll('.admin-only').forEach(el => {
            if (isAdmin) el.classList.remove('hidden');
            else el.classList.add('hidden');
        });

        await loadFacilities();
        showView('bookings');
    } catch (e) {
        logout();
    }
}

async function login() {
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    try {
        const res = await fetch(`${API_URL}/auth/login`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        if (!res.ok) throw new Error('Неверный логин или пароль');
        const data = await res.json();
        localStorage.setItem('accessToken', data.accessToken);
        token = data.accessToken;
        await initApp();
    } catch (e) {
        document.getElementById('login-error').textContent = e.message;
    }
}

function logout() {
    localStorage.clear();
    location.reload();
}

function showView(viewName) {
    document.querySelectorAll('.view').forEach(el => el.classList.add('hidden'));
    document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));
    document.getElementById(`view-${viewName}`).classList.remove('hidden');
    document.querySelector(`.nav-btn[onclick="showView('${viewName}')"]`).classList.add('active');

    if (viewName === 'bookings') loadBookings();
    if (viewName === 'analytics') loadAnalytics();
    if (viewName === 'admin-facilities') loadAdminFacilities();
    if (viewName === 'admin-users') loadUsers();
    if (viewName === 'admin-audit') loadAudit();
}

async function loadFacilities() {
    const res = await fetch(`${API_URL}/references`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (res.ok) {
        facilitiesCache = await res.json();
        const select = document.getElementById('facility-select');
        select.innerHTML = '';
        facilitiesCache.filter(f => f.active).forEach(f => {select.innerHTML += `<option value="${f.id}">${f.name} (${f.pricePerHour} руб/час)</option>`;});
    }
}

async function loadBookings() {
    const search = document.getElementById('search-input').value;
    const res = await fetch(`${API_URL}/bookings?search=${search}&size=50`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (!res.ok) return;
    const data = await res.json();
    currentBookings = data.content;
    const tbody = document.getElementById('bookings-table');
    tbody.innerHTML = currentBookings.length ? '' : '<tr><td colspan="7" style="text-align:center;">Нет данных</td></tr>';
    currentBookings.forEach(b => {
        tbody.innerHTML += `
            <tr>
                <td>${b.bookingCode}</td>
                <td>${b.clientName} <br><small>${b.clientPhone}</small></td>
                <td>${b.facilityName}</td>
                <td>${new Date(b.startTime).toLocaleString()}</td>
                <td>${b.status}</td>
                <td>${b.totalPrice} ₽</td>
                <td>
                    <button onclick="editBooking('${b.id}')" style="padding: 4px; font-size:12px;">Изм.</button>
                    <button onclick="deleteBooking('${b.id}')" class="danger" style="padding: 4px; font-size:12px;">В архив</button>
                </td>
            </tr>`;
    });
}

function showBookingModal() {
    document.getElementById('booking-modal').classList.remove('hidden');
    document.getElementById('modal-title').textContent = 'Новая бронь';
    document.getElementById('booking-id').value = '';
    document.getElementById('booking-version').value = '';
    document.getElementById('client-name').value = '';
    document.getElementById('client-phone').value = '';
    document.getElementById('start-time').value = '';
    document.getElementById('end-time').value = '';
    document.getElementById('booking-status').value = 'CREATED';
    document.getElementById('status-group').style.display = 'none'; // Скрываем статус при создании!
    if (facilitiesCache.length) document.getElementById('facility-select').value = facilitiesCache[0].id;
}

function hideBookingModal() { document.getElementById('booking-modal').classList.add('hidden'); }

function editBooking(id) {
    const b = currentBookings.find(x => x.id === id);
    if (!b) return;
    showBookingModal();
    document.getElementById('modal-title').textContent = 'Редактирование';
    document.getElementById('booking-id').value = b.id;
    document.getElementById('booking-version').value = b.version;
    document.getElementById('client-name').value = b.clientName;
    document.getElementById('client-phone').value = b.clientPhone;
    document.getElementById('start-time').value = b.startTime.substring(0, 16);
    document.getElementById('end-time').value = b.endTime.substring(0, 16);
    document.getElementById('booking-status').value = b.status;
    document.getElementById('booking-priority').value = b.priorityLevel;
    document.getElementById('status-group').style.display = 'block';
    const f = facilitiesCache.find(x => x.name === b.facilityName);
    if (f) document.getElementById('facility-select').value = f.id;
}

async function saveBooking() {
    const id = document.getElementById('booking-id').value;
    const body = {
        facilityId: document.getElementById('facility-select').value,
        clientName: document.getElementById('client-name').value,
        clientPhone: document.getElementById('client-phone').value,
        startTime: document.getElementById('start-time').value,
        endTime: document.getElementById('end-time').value,
        status: document.getElementById('booking-status').value,
        isPaid: false,
        priorityLevel: document.getElementById('booking-priority').value,
        version: document.getElementById('booking-version').value ? parseInt(document.getElementById('booking-version').value) : null
    };
    const res = await fetch(id ? `${API_URL}/bookings/${id}` : `${API_URL}/bookings`, {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(body)
    });
    if (res.ok) { hideBookingModal(); loadBookings(); }
    else { alert('Ошибка сохранения'); }
}

async function deleteBooking(id) {
    if (!confirm('В архив?')) return;
    await fetch(`${API_URL}/bookings/${id}`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` } });
    loadBookings();
}

async function exportCsv() {
    try {
        const res = await fetch(`${API_URL}/bookings/export`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!res.ok) {
            alert('Ошибка экспорта. Возможно, нет прав или сервер недоступен.');
            return;
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = url;
        a.download = 'bookings.csv';
        document.body.appendChild(a);
        a.click();
        a.remove();
        
        window.URL.revokeObjectURL(url);
    } catch (e) {
        alert("Не удалось скачать CSV: " + e.message);
    }
}

async function importCsv(input) {
    if (!input.files || input.files.length === 0) return;
    const formData = new FormData();
    formData.append("file", input.files[0]);
    const previewRes = await fetch(`${API_URL}/bookings/import/preview`, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` }, body: formData });
    const preview = await previewRes.json();
    if (!confirm(`Валидных: ${preview.validCount}. Ошибок: ${preview.errorCount}. Применить?`)) return;
    formData.append("mode", "ONLY_NEW");
    await fetch(`${API_URL}/bookings/import/apply`, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` }, body: formData });
    alert('Импорт завершен'); loadBookings(); loadFacilities();
}

async function loadAnalytics() {
    const res = await fetch(`${API_URL}/analytics/summary`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (!res.ok) return;
    const data = await res.json();
    
    document.getElementById('analytics-cards').innerHTML = `
        <div class="stat-card"><div>Всего</div><div class="value">${data.totalCount}</div></div>
        <div class="stat-card"><div>Активных</div><div class="value">${data.activeCount}</div></div>
        <div class="stat-card"><div>В архиве</div><div class="value">${data.archivedCount}</div></div>
        <div class="stat-card"><div>Оплачено</div><div class="value">${data.paidCount}</div></div>
        <div class="stat-card"><div>Средняя цена</div><div class="value">${data.averagePrice} ₽</div></div>
    `;

    const renderGroup = (title, list) => {
        if (!list || list.length === 0) return `<div style="margin-bottom:20px;"><h4>${title}</h4><p style="color:#666;font-style:italic;">Нет данных</p></div>`;
        return `<div style="margin-bottom:20px;"><h4>${title}</h4><div class="cards-grid">${list.map(i => `<div class="stat-card" style="padding:10px;"><div>${i.name}</div><div class="value" style="font-size:18px;">${i.count}</div></div>`).join('')}</div></div>`;
    };

    document.getElementById('analytics-groups').innerHTML = 
        renderGroup('По статусам', data.byStatus) +
        renderGroup('По площадкам', data.byFacility) +
        renderGroup('По приоритету', data.byPriority);
}

async function loadAdminFacilities() {
    const res = await fetch(`${API_URL}/references`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (!res.ok) return;
    const data = await res.json();
    const tbody = document.getElementById('facilities-table');
    tbody.innerHTML = '';
    data.forEach(f => {
        tbody.innerHTML += `
            <tr>
                <td>${f.name}</td>
                <td>${f.description || '-'}</td>
                <td>${f.pricePerHour} ₽</td>
                <td>${f.active ? '<span style="color:green">Активна</span>' : '<span style="color:red">Отключена</span>'}</td>
                <td>
                    <button onclick="showFacilityModal('${f.id}', '${f.name}', '${f.description || ''}', ${f.active}, ${f.pricePerHour})" style="padding: 4px; font-size:12px;">Изм.</button>
                    <button onclick="toggleFacility('${f.id}')" class="secondary" style="padding: 4px; font-size:12px;">Вкл/Выкл</button>
                </td>
            </tr>`;
    });
}

function showFacilityModal(id = null, name = '', desc = '', active = true, price = '') {
    document.getElementById('facility-modal').classList.remove('hidden');
    document.getElementById('facility-modal-title').textContent = id ? 'Редактирование' : 'Новая площадка';
    document.getElementById('facility-edit-id').value = id || '';
    document.getElementById('facility-name').value = name;
    document.getElementById('facility-desc').value = desc;
    document.getElementById('facility-active').value = active.toString();
    document.getElementById('facility-price').value = price || '';
}

function hideFacilityModal() { document.getElementById('facility-modal').classList.add('hidden'); }

async function saveFacility() {
    const id = document.getElementById('facility-edit-id').value;
    const body = {
        name: document.getElementById('facility-name').value,
        description: document.getElementById('facility-desc').value,
        active: document.getElementById('facility-active').value === 'true',
        pricePerHour: parseFloat(document.getElementById('facility-price').value)
    };
    const res = await fetch(id ? `${API_URL}/references/${id}` : `${API_URL}/references`, {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(body)
    });
    if (res.ok) { hideFacilityModal(); loadAdminFacilities(); loadFacilities(); }
    else { alert('Ошибка сохранения'); }
}

async function toggleFacility(id) {
    await fetch(`${API_URL}/references/${id}/toggle`, { method: 'PATCH', headers: { 'Authorization': `Bearer ${token}` } });
    loadAdminFacilities(); loadFacilities();
}

async function loadUsers() {
    const res = await fetch(`${API_URL}/admin/users`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (!res.ok) return;
    const data = await res.json();
    document.getElementById('users-table').innerHTML = data.content.map(u => `
        <tr><td>${u.email}</td><td>${u.fullName}</td><td>${u.roles.join(', ')}</td><td>${u.active ? 'Да' : 'Нет'}</td>
        <td><button onclick="resetPassword('${u.id}')" style="padding:4px; font-size:12px;">Сбросить пароль</button></td></tr>
    `).join('');
}

async function resetPassword(id) {
    if (!confirm('Сбросить пароль?')) return;
    const res = await fetch(`${API_URL}/admin/users/${id}/reset-password`, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` } });
    const data = await res.json();
    alert(`Новый пароль для ${data.email}: ${data.temporaryPassword}`);
}

async function loadAudit() {
    const search = document.getElementById('audit-search').value;
    const res = await fetch(`${API_URL}/admin/audit?search=${search}`, { headers: { 'Authorization': `Bearer ${token}` } });
    if (!res.ok) return;
    const data = await res.json();
    document.getElementById('audit-table').innerHTML = data.content.map(e => `
        <tr><td>${new Date(e.createdAt).toLocaleString()}</td><td>${e.userName}</td><td>${e.eventType}</td><td>${e.entityType || '-'}</td><td>${e.description || '-'}</td></tr>
    `).join('');
}

async function changePassword() {
    const oldP = document.getElementById('old-password').value;
    const newP = document.getElementById('new-password').value;
    const res = await fetch(`${API_URL}/auth/change-password`, {
        method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ oldPassword: oldP, newPassword: newP })
    });
    if (res.ok) alert('Пароль изменен!'); else alert('Ошибка смены пароля');
}