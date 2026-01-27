const API_BASE = '/api/repositories';

document.addEventListener('DOMContentLoaded', () => {
    loadRepositories();
    document.getElementById('repo-url').textContent = window.location.protocol + "//" + window.location.host;
});

async function loadRepositories() {
    try {
        const response = await fetch(API_BASE);
        const result = await response.json();
        if (result.success) {
            renderTable(result.data);
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('加载仓库清单失败', 'error');
        console.error(error);
    }
}

function renderTable(repos) {
    const tbody = document.getElementById('repo-table');
    if (!repos || repos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">未发现需要代理的仓库。点击"添加仓库"新增代理仓库吧。</td></tr>';
        return;
    }
    tbody.innerHTML = repos.map(repo => `
        <tr>
            <td>${repo.id}</td>
            <td><strong>${escapeHtml(repo.name)}</strong></td>
            <td style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(repo.url)}">${escapeHtml(repo.url)}</td>
            <td><span class="status status-${repo.syncStatus.toLowerCase()}">${repo.syncStatus}</span></td>
            <td>${repo.lastSyncTime ? formatDate(repo.lastSyncTime) : '-'}</td>
            <td>
                <label class="toggle">
                    <input type="checkbox" ${repo.enabled ? 'checked' : ''} onchange="toggleEnabled(${repo.id}, this.checked)">
                    <span class="slider"></span>
                </label>
            </td>
            <td class="actions">
                <button class="btn btn-success btn-sm" onclick="syncRepository(${repo.id}, this)">同步</button>
                <button class="btn btn-primary btn-sm" onclick="editRepository(${repo.id})">编辑</button>
                <button class="btn btn-danger btn-sm" onclick="deleteRepository(${repo.id})">删除</button>
            </td>
        </tr>
    `).join('');
}

function openModal(repo = null) {
    const modal = document.getElementById('modal');
    const title = document.getElementById('modal-title');
    const form = document.getElementById('repo-form');
    form.reset();
    document.getElementById('repo-id').value = '';
    if (repo) {
        title.textContent = '编辑仓库';
        document.getElementById('repo-id').value = repo.id;
        document.getElementById('repo-name').value = repo.name;
        document.getElementById('repo-url-input').value = repo.url;
        document.getElementById('repo-desc').value = repo.description || '';
    } else {
        title.textContent = '添加仓库';
    }
    modal.classList.add('active');
}

function closeModal() {
    document.getElementById('modal').classList.remove('active');
}

async function saveRepository(event) {
    event.preventDefault();
    const id = document.getElementById('repo-id').value;
    const data = {
        name: document.getElementById('repo-name').value,
        url: document.getElementById('repo-url-input').value,
        description: document.getElementById('repo-desc').value,
        enabled: true
    };
    const submitBtn = document.getElementById('submit-btn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading"></span> 保存中...';
    try {
        const url = id ? `${API_BASE}/${id}` : API_BASE;
        const method = id ? 'PUT' : 'POST';
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await response.json();
        if (result.success) {
            showToast(result.message, 'success');
            closeModal();
            loadRepositories();
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('保存仓库失败', 'error');
        console.error(error);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save';
    }
}

async function editRepository(id) {
    try {
        const response = await fetch(`${API_BASE}/${id}`);
        const result = await response.json();
        if (result.success) {
            openModal(result.data);
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('修改仓库失败', 'error');
        console.error(error);
    }
}

async function deleteRepository(id) {
    if (!confirm('你确定要删除这个仓库吗?')) return;
    try {
        const response = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            showToast(result.message, 'success');
            loadRepositories();
        } else {
            showToast(result.message, 'error');
        }
    } catch (error) {
        showToast('删除仓库失败', 'error');
        console.error(error);
    }
}

async function toggleEnabled(id, enabled) {
    try {
        const response = await fetch(`${API_BASE}/${id}/toggle?enabled=${enabled}`, { method: 'PATCH' });
        const result = await response.json();
        if (result.success) {
            showToast(`Repository ${enabled ? 'enabled' : 'disabled'}`, 'success');
        } else {
            showToast(result.message, 'error');
            loadRepositories();
        }
    } catch (error) {
        showToast('更新仓库失败', 'error');
        loadRepositories();
        console.error(error);
    }
}

async function syncRepository(id, button) {
    const originalText = button.textContent;
    button.disabled = true;
    button.innerHTML = '<span class="loading"></span>';
    try {
        const response = await fetch(`${API_BASE}/${id}/sync`, { method: 'POST' });
        const result = await response.json();
        if (result.success) {
            showToast('同步成功', 'success');
            loadRepositories();
        } else {
            showToast(result.message, 'error');
            loadRepositories();
        }
    } catch (error) {
        showToast('同步失败', 'error');
        loadRepositories();
        console.error(error);
    } finally {
        button.disabled = false;
        button.textContent = originalText;
    }
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast toast-${type} show`;
    setTimeout(() => { toast.classList.remove('show'); }, 3000);
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
