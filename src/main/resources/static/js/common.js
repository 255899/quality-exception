// 公共工具
const API = ''; // 同域
const LS_KEY_USER = 'qe.currentUser';
const LS_KEY_TOKEN = 'qe.authToken';

function $(s, root) { return (root || document).querySelector(s); }
function $all(s, root) { return Array.from((root || document).querySelectorAll(s)); }

function getCurrentUser() {
  try { return JSON.parse(localStorage.getItem(LS_KEY_USER) || 'null'); }
  catch (e) { return null; }
}
function setCurrentUser(u) { if (u) localStorage.setItem(LS_KEY_USER, JSON.stringify(u)); else localStorage.removeItem(LS_KEY_USER); }

function getToken() { return localStorage.getItem(LS_KEY_TOKEN) || ''; }
function setToken(t) { if (t) localStorage.setItem(LS_KEY_TOKEN, t); else localStorage.removeItem(LS_KEY_TOKEN); }

function logoutLocal() {
  setCurrentUser(null);
  setToken('');
  location.replace('login.html');
}

function toast(msg, duration) {
  const t = document.createElement('div');
  t.className = 'toast active';
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => { t.remove(); }, duration || 1800);
}

const MOCK_DATA = {
  sections: [{ id: 1, name: 'A组' }, { id: 2, name: 'B组' }],
  groups: { 1: [{ id: 11, name: 'A1组', sectionId: 1 }, { id: 12, name: 'A2组', sectionId: 1 }], 2: [{ id: 21, name: 'B1组', sectionId: 2 }, { id: 22, name: 'B2组', sectionId: 2 }] },
  lines: { 11: [{ id: 111, name: 'A1产线', groupId: 11 }], 12: [{ id: 121, name: 'A2产线', groupId: 12 }], 21: [{ id: 211, name: 'B1产线', groupId: 21 }], 22: [{ id: 221, name: 'B2产线', groupId: 22 }] },
  processes: { 111: [{ id: 1111, name: '焊接工程', lineId: 111 }, { id: 1112, name: '组装工程', lineId: 111 }], 121: [{ id: 1211, name: '测试工程', lineId: 121 }], 211: [{ id: 2111, name: '包装工程', lineId: 211 }], 221: [{ id: 2211, name: '质检工程', lineId: 221 }] },
  parts: { 1111: [{ id: 11111, name: '焊接件A', code: 'WJ-A001', processId: 1111 }], 1112: [{ id: 11121, name: '组装件B', code: 'ZZ-B002', processId: 1112 }], 1211: [{ id: 12111, name: '测试件C', code: 'CS-C003', processId: 1211 }], 2111: [{ id: 21111, name: '包装件D', code: 'BZ-D004', processId: 2111 }], 2211: [{ id: 22111, name: '质检件E', code: 'ZJ-E005', processId: 2211 }] },
  reasons: [
    { id: 1, name: '来料不良', category: 'MATERIAL' },
    { id: 2, name: '设备故障', category: 'EQUIPMENT' },
    { id: 3, name: '工艺问题', category: 'PROCESS' },
    { id: 4, name: '人员操作', category: 'HUMAN' },
    { id: 5, name: '环境因素', category: 'ENVIRONMENT' },
    { id: 6, name: '测量误差', category: 'MEASUREMENT' },
    { id: 7, name: '图纸错误', category: 'DOCUMENT' },
    { id: 8, name: '供应商问题', category: 'SUPPLIER' },
    { id: 9, name: '其他', category: 'OTHER' }
  ],
  qcUsers: [{ id: 1, username: 'qc_zhang', realName: '张品管', role: 'QC' }],
  exceptions: [
    { id: 1, code: 'QE-20260713-001', status: 'APPLIED', title: '焊接不良', applicant: '张三', sectionId: 1, groupId: 11, lineId: 111, processId: 1111, partId: 11111, severity: 'MAJOR', reason: '设备故障', findMotivation: '自检发现', createdAt: '2026-07-13 10:00' },
    { id: 2, code: 'QE-20260713-002', status: 'QC_HANDLED', title: '组装缺陷', applicant: '李四', sectionId: 2, groupId: 21, lineId: 211, processId: 2111, partId: 21111, severity: 'MINOR', reason: '工艺问题', findMotivation: '巡检发现', createdAt: '2026-07-13 11:00' },
    { id: 3, code: 'QE-20260713-003', status: 'SHIFT_CONFIRMED', title: '测试异常', applicant: '王五', sectionId: 1, groupId: 12, lineId: 121, processId: 1211, partId: 12111, severity: 'CRITICAL', reason: '来料不良', findMotivation: '客户反馈', createdAt: '2026-07-13 12:00' }
  ]
};

async function request(url, opts) {
  await new Promise(r => setTimeout(r, 200));
  const u = getCurrentUser();
  
  if (url === '/api/auth/me') {
    return u || { id: 0, username: 'guest', realName: '访客', role: 'GUEST' };
  }
  
  if (url === '/api/auth/logout') {
    logoutLocal();
    return {};
  }
  
  if (url === '/api/departments/sections') {
    return MOCK_DATA.sections;
  }
  
  if (url.startsWith('/api/departments/groups')) {
    const m = url.match(/sectionId=(\d+)/);
    return m ? (MOCK_DATA.groups[m[1]] || []) : [];
  }
  
  if (url.startsWith('/api/departments/lines')) {
    const m = url.match(/groupId=(\d+)/);
    return m ? (MOCK_DATA.lines[m[1]] || []) : [];
  }
  
  if (url.startsWith('/api/departments/processes')) {
    const m = url.match(/lineId=(\d+)/);
    return m ? (MOCK_DATA.processes[m[1]] || []) : [];
  }
  
  if (url.startsWith('/api/departments/parts')) {
    const m = url.match(/processId=(\d+)/);
    return m ? (MOCK_DATA.parts[m[1]] || []) : [];
  }
  
  if (url === '/api/exception-reasons') {
    return MOCK_DATA.reasons;
  }
  
  if (url === '/api/users/qc') {
    return MOCK_DATA.qcUsers;
  }
  
  if (url === '/api/exceptions/preview-code') {
    return 'QE-' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + '-001';
  }
  
  if (url === '/api/exceptions/apply' && opts && opts.method === 'POST') {
    const body = JSON.parse(opts.body);
    const code = 'QE-' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + '-' + String(MOCK_DATA.exceptions.length + 1).padStart(3, '0');
    return { id: Date.now(), code, ...body };
  }
  
  if (url.startsWith('/api/exceptions/')) {
    const m = url.match(/\/api\/exceptions\/(\d+)/);
    if (m) {
      return MOCK_DATA.exceptions.find(e => e.id == m[1]) || {};
    }
    if (url.startsWith('/api/exceptions?')) {
      const params = new URLSearchParams(url.split('?')[1]);
      const status = params.get('status');
      if (status) {
        return MOCK_DATA.exceptions.filter(e => e.status === status);
      }
      return MOCK_DATA.exceptions;
    }
  }
  
  if (url.startsWith('/api/exceptions/flow/')) {
    const step = url.split('/').pop();
    const m = url.match(/\/api\/exceptions\/(\d+)\/flow/);
    if (m) {
      const idx = MOCK_DATA.exceptions.findIndex(e => e.id == m[1]);
      if (idx >= 0) {
        MOCK_DATA.exceptions[idx].status = step;
      }
    }
    return { success: true };
  }
  
  if (url.startsWith('/api/exceptions/statistics')) {
    return {
      total: MOCK_DATA.exceptions.length,
      byStatus: { APPLIED: 1, QC_HANDLED: 1, SHIFT_CONFIRMED: 1, CLOSED: 0 },
      bySeverity: { CRITICAL: 1, MAJOR: 1, MINOR: 1 }
    };
  }
  
  if (url.startsWith('/api/upload')) {
    return { url: 'uploads/test.jpg' };
  }
  
  return {};
}

function requireLogin(redirectTo) {
  const tk = getToken();
  if (!tk) { location.replace('login.html?return=' + encodeURIComponent(redirectTo || location.href)); return false; }
  const me = getCurrentUser();
  if (!me || !me.role) { location.replace('login.html?return=' + encodeURIComponent(redirectTo || location.href)); return false; }
  return me;
}

function fmtDateTime(dt) {
  if (!dt) return '-';
  const d = new Date(dt);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function statusBadge(status) {
  const map = {
    DRAFT: ['草稿', '#999'],
    APPLIED: ['已申请', '#1f7be5'],
    QC_HANDLED: ['品管已处理', '#2aa884'],
    SHIFT_CONFIRMED: ['班长已首件', '#8e3fb5'],
    QC_CONFIRMED: ['品管已首件', '#1c3d80'],
    LEADER_CONFIRMED: ['组长已确认', '#2a8d4a'],
    CHIEF_CONFIRMED: ['课长已确认', '#6dc18c'],
    CLOSED: ['已关闭', '#444']
  };
  const m = map[status] || [status, '#1f7be5'];
  return `<span class="status" style="background:${m[1]}22;color:${m[1]}">${m[0]}</span>`;
}

/* 通用模态单选 */
function showPicker(options, onPicked) {
  const mask = document.createElement('div');
  mask.className = 'modal-mask active';
  mask.innerHTML = `
    <div class="modal">
      <div class="modal-header">请选择</div>
      <div class="modal-body"></div>
    </div>`;
  const body = mask.querySelector('.modal-body');
  options.forEach((o, idx) => {
    const div = document.createElement('div');
    div.className = 'modal-option' + (idx === 0 ? ' selected' : '');
    div.innerHTML = `<span>${o.label}</span><span class="radio"></span>`;
    div.addEventListener('click', () => {
      mask.remove();
      onPicked(o);
    });
    body.appendChild(div);
  });
  mask.addEventListener('click', e => { if (e.target === mask) mask.remove(); });
  document.body.appendChild(mask);
}