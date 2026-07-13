// 详情页
async function loadDetail() {
  const id = new URLSearchParams(location.search).get('id');
  if (!id) return;
  const data = await request(`/api/exceptions/${id}`);
  const r = data.report;
  const dept = await resolveDept(r);

  document.getElementById('code').textContent = r.code || '—';
  document.getElementById('status').innerHTML = statusBadge(r.status);
  document.getElementById('implementer').textContent = r.implementer || '-';
  document.getElementById('occurredAt').textContent = `${fmtDateTime(r.occurredAt)} ${r.shift || ''}`;
  document.getElementById('reporter').textContent = r.reporter || '-';
  const pointLabel = dept.process
    + (r.partId ? ` ｜ ${dept.part || ''}` : '');
  document.getElementById('exceptionPoint').textContent = `${dept.section} / ${dept.group} / ${dept.line} / ${pointLabel}`;
  document.getElementById('description').textContent = r.description || '-';
  const reasonEl = document.getElementById('discoveryReason');
  if (reasonEl) reasonEl.textContent = r.discoveryReason || '-';

  const flowBox = document.getElementById('flowBox');
  if (data.flows && data.flows.length) {
    flowBox.innerHTML = data.flows.map(f => `
      <div class="flow-item">
        <div class="step">${f.step || ''} · ${f.action || ''}</div>
        <div class="meta">${f.handlerName || '系统'} · ${fmtDateTime(f.processedAt)}</div>
        <div class="comment">${f.comment || ''}</div>
      </div>
    `).join('');
  } else {
    flowBox.innerHTML = '<div class="empty">暂无流程</div>';
  }
}

async function resolveDept(r) {
  const [secs, gps, lns] = await Promise.all([
    request('/api/departments/sections'),
    r.groupId ? request(`/api/departments/groups?sectionId=${r.sectionId}`) : Promise.resolve([]),
    r.lineId ? request(`/api/departments/lines?groupId=${r.groupId}`) : Promise.resolve([])
  ]);
  const proc = r.processId ? await request(`/api/departments/processes?lineId=${r.lineId}`) : [];
  const parts = r.processId ? await request(`/api/departments/parts?processId=${r.processId}`) : [];
  const part = (parts.find(x => x.id === r.partId) || {});
  return {
    section: (secs.find(x => x.id === r.sectionId) || {}).name || '-',
    group: (gps.find(x => x.id === r.groupId) || {}).name || '-',
    line: (lns.find(x => x.id === r.lineId) || {}).name || '-',
    process: (proc.find(x => x.id === r.processId) || {}).name || '-',
    part: part.code ? `${part.code} - ${part.name}` : ''
  };
}

window.addEventListener('DOMContentLoaded', loadDetail);
