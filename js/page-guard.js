// 页面级权限守门 — 严格隔离：每个模块仅允许指定角色
// 用法：在页面 <body> 内 <script> 中调用 requireRole('apply')。
// 未登录 → 跳 login.html；登录后无权限 → 弹窗并跳回 index.html。
(function () {
  window.PAGE_ACCESS = {
    'apply':          new Set(['SHIFT_LEADER_SUPER', 'SHIFT_LEADER']),                    // 现场班长 / 班长 申请
    'shift-confirm':  new Set(['SHIFT_LEADER_SUPER', 'SHIFT_LEADER']),                    // 现场班长 / 班长 首件确认
    'qc-handle':      new Set(['QC']),                                                   // 现场品管 处理
    'qc-confirm':     new Set(['QC']),                                                   // 现场品管 首件确认
    'leader-confirm': new Set(['SECTION_LEADER']),                                       // 组长 确认
    'chief-confirm':  new Set(['SECTION_CHIEF']),                                        // 课长 确认
    'quality-confirm':new Set(['QUALITY_CHIEF']),                                        // 品质部长 确认
    'admin':          new Set(['QUALITY_CHIEF'])                                         // 管理后台
  };

  // 同步版本：本地有 token + 缓存角色时直接判断（无 token 跳登录）
  function ensureLocalAuth() {
    if (!getToken()) { location.replace('login.html?return=' + encodeURIComponent(location.href)); return null; }
    const u = getCurrentUser();
    return u;
  }

  // 异步校验并跳转
  window.requireRole = async function (moduleKey) {
    const allowed = PAGE_ACCESS[moduleKey];
    if (!allowed) return; // 未配置则放行
    const local = ensureLocalAuth();
    if (!local) return; // 已跳转

    // 调一次 /me 拿最新角色
    let me;
    try { me = await request('/api/auth/me'); } catch (e) { location.replace('login.html'); return; }
    setCurrentUser(me);

    if (!allowed.has(me.role)) {
      // 隐藏页面内容，提示
      const root = document.querySelector('.app') || document.body;
      try { root.style.display = 'none'; } catch (_) {}
      const overlay = document.createElement('div');
      overlay.style.cssText = 'position:fixed;inset:0;background:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;z-index:9999;text-align:center;padding:24px';
      overlay.innerHTML = `
        <div style="font-size:48px;margin-bottom:12px">🚫</div>
        <div style="font-size:18px;font-weight:600;color:#d33;margin-bottom:8px">无权访问该功能</div>
        <div style="font-size:14px;color:#666;margin-bottom:18px">当前角色：${me.realName}（${me.role}）</div>
        <button id="goBack" style="padding:10px 24px;background:#1f7be5;color:#fff;border:none;border-radius:20px;font-size:14px;cursor:pointer">返回主页</button>
      `;
      document.body.appendChild(overlay);
      overlay.querySelector('#goBack').addEventListener('click', () => { location.replace('index.html'); });
    }
  };
})();
