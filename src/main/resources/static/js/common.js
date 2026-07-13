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

async function request(url, opts) {
  opts = opts || {};
  const headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
  const tk = getToken();
  if (tk) headers['X-Auth-Token'] = tk;
  const res = await fetch(API + url, Object.assign({}, opts, { headers }));
  if (res.status === 401) {
    logoutLocal();
    throw new Error('登录已失效');
  }
  const contentType = res.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    const text = await res.text();
    let msg = '服务器返回非JSON数据';
    if (res.status === 404) msg = '接口不存在';
    else if (res.status === 500) msg = '服务器内部错误';
    else if (text && text.length > 0) msg = text.substring(0, 100);
    throw new Error(msg);
  }
  const json = await res.json();
  if (json.code !== 0) {
    throw new Error(json.message || '请求失败');
  }
  return json.data;
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