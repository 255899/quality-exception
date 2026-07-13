// 流程处理页公共逻辑
// 用法：FLOW_CONFIG = { title, step, filterStatus, actionLabel }
async function renderList(container) {
  const C = window.FLOW_CONFIG;
  const list = await request(`/api/exceptions?status=${C.filterStatus}`);
  if (!list.length) {
    container.innerHTML = '<div class="empty">暂无待处理单据</div>';
    return;
  }
  container.innerHTML = list.map(r => `
    <div class="list-item" data-id="${r.id}">
      <div class="row1">
        <span class="code">${r.code || '—'}</span>
        ${statusBadge(r.status)}
      </div>
      <div class="meta">发生：${fmtDateTime(r.occurredAt)} · 班次：${r.shift || '-'}</div>
      <div class="desc">${r.description || ''}</div>
      <div class="meta">报告者：${r.reporter || '-'}</div>
      <div style="margin-top:10px; display:flex; gap:8px;">
        <button class="btn btn-outline" onclick="goDetail(${r.id})">查看</button>
        <button class="btn" onclick="openHandle(${r.id})">${C.actionLabel}</button>
      </div>
    </div>
  `).join('');
}

function goDetail(id) { location.href = 'detail.html?id=' + id; }

function openHandle(id) {
  location.href = `handle.html?step=${window.FLOW_CONFIG.step}&id=${id}`;
}

window.addEventListener('DOMContentLoaded', async () => {
  const u = getCurrentUser();
  if (u) {
    document.getElementById('userName') && (document.getElementById('userName').textContent = u.realName);
  }
  const c = document.getElementById('listContainer');
  await renderList(c);
});
