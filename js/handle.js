// 通用流程处理页：通过/驳回
async function loadTarget() {
  const sp = new URLSearchParams(location.search);
  const id = sp.get('id');
  const step = sp.get('step') || window.FLOW_STEP;
  if (!id) return;
  if (!step) {
    toast('缺少流程步骤参数 (step)');
    return;
  }
  const data = await request(`/api/exceptions/${id}`);
  const r = data.report;
  const dept = await resolveDept(r);
  document.getElementById('code').textContent = r.code || '—';
  document.getElementById('status').innerHTML = statusBadge(r.status);
  document.getElementById('implementer').textContent = r.implementer || '-';
  document.getElementById('occurredAt').textContent = `${fmtDateTime(r.occurredAt)} ${r.shift || ''}`;
  document.getElementById('reporter').textContent = r.reporter || '-';
  document.getElementById('exceptionPoint').textContent = `${dept.section} / ${dept.group} / ${dept.line} / ${dept.process}`;
  document.getElementById('description').textContent = r.description || '-';
  window.__EXCEPTION_ID = id;
  window.__FLOW_STEP = step;
}

async function resolveDept(r) {
  const [secs, gps, lns] = await Promise.all([
    request('/api/departments/sections'),
    r.groupId ? request(`/api/departments/groups?sectionId=${r.sectionId}`) : Promise.resolve([]),
    r.lineId ? request(`/api/departments/lines?groupId=${r.groupId}`) : Promise.resolve([])
  ]);
  const proc = r.processId ? await request(`/api/departments/processes?lineId=${r.lineId}`) : [];
  return {
    section: (secs.find(x => x.id === r.sectionId) || {}).name || '-',
    group: (gps.find(x => x.id === r.groupId) || {}).name || '-',
    line: (lns.find(x => x.id === r.lineId) || {}).name || '-',
    process: (proc.find(x => x.id === r.processId) || {}).name || '-'
  };
}

async function submitAction(action) {
  if (!window.__EXCEPTION_ID) return;
  let u = getCurrentUser();
  if (!u || (u.id == null && u.userId == null)) {
    try { u = await request('/api/auth/me'); setCurrentUser(u); } catch (e) {}
  }
  if (!u || (u.id == null && u.userId == null)) { toast('请先登录'); location.replace('login.html?return=' + encodeURIComponent(location.pathname + location.search)); return; }
  if (!window.__FLOW_STEP) { toast('缺少流程步骤参数 (step)'); return; }
  const handlerId = u.id || u.userId;
  const comment = document.getElementById('comment').value.trim();
  try {
    await request(`/api/exceptions/flow/${window.__FLOW_STEP}`, {
      method: 'POST',
      body: JSON.stringify({
        exceptionId: +window.__EXCEPTION_ID,
        handlerId: handlerId,
        action: action,
        comment: comment
      })
    });
    toast('操作成功');
    setTimeout(() => history.back(), 1000);
  } catch (e) {
    toast(e.message);
  }
}

function bindActions() {
  document.querySelectorAll('[data-action]').forEach(b => {
    b.addEventListener('click', () => submitAction(b.dataset.action));
  });
}

window.addEventListener('DOMContentLoaded', async () => {
  await loadTarget();
  bindActions();
});
