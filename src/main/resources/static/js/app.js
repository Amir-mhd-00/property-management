/* =========================================================================
   Ledger — Property Management Console
   Vanilla JS SPA. Talks to the Spring Boot API at the same origin.
   ========================================================================= */
(() => {
  "use strict";

  const API = ""; // same-origin; change to e.g. "http://localhost:8080" if serving frontend separately (needs CORS)

  const state = {
    me: null,           // LoginResponseDTO
    route: "dashboard",
    properties: { content: [], page: 0, size: 10, totalPages: 0, totalElements: 0, statusFilter: "" },
    users: { content: [] },
    assignments: { content: [] },
  };

  /* ----------------------------------------------------------------------
     API client
     ---------------------------------------------------------------------- */
  async function api(method, path, body) {
    const res = await fetch(API + path, {
      method,
      credentials: "include",
      headers: body ? { "Content-Type": "application/json" } : {},
      body: body ? JSON.stringify(body) : undefined,
    });

    if (res.status === 204) return null;

    let data = null;
    const text = await res.text();
    if (text) {
      try { data = JSON.parse(text); } catch { data = text; }
    }

    if (!res.ok) {
      const message = (data && (data.message || data.error)) || `Request failed (${res.status})`;
      const err = new Error(message);
      err.status = res.status;
      err.data = data;
      throw err;
    }
    return data;
  }

  const Api = {
    register: (dto) => api("POST", "/api/auth/register", dto),
    login: (dto) => api("POST", "/api/auth/login", dto),
    logout: () => api("POST", "/api/auth/logout"),
    me: () => api("GET", "/api/auth/me"),

    getProperty: (id) => api("GET", `/api/v1/properties/${id}`),
    createProperty: (dto) => api("POST", "/api/v1/properties", dto),
    listProperties: (page, size) => api("GET", `/api/v1/properties?page=${page}&size=${size}&sort=id,desc`),
    updateProperty: (id, dto) => api("PUT", `/api/v1/properties/${id}`, dto),
    patchProperty: (id, dto) => api("PATCH", `/api/v1/properties/${id}`, dto),
    deleteProperty: (id) => api("DELETE", `/api/v1/properties/${id}`),
    propertiesByStatus: (status) => api("GET", `/api/v1/properties/status?status=${encodeURIComponent(status)}`),
    propertyAssignments: (id) => api("GET", `/api/v1/properties/${id}/assignments`),

    getUser: (id) => api("GET", `/api/v1/users/${id}`),
    listUsers: () => api("GET", "/api/v1/users"),
    userAssignments: (id) => api("GET", `/api/v1/users/${id}/assignments`),
    patchUser: (id, dto) => api("PATCH", `/api/v1/users/${id}`, dto),
    deleteUser: (id) => api("DELETE", `/api/v1/users/${id}`),

    createAssignment: (dto) => api("POST", "/api/v1/assignments", dto),
    getAssignment: (id) => api("GET", `/api/v1/assignments/${id}`),
    listAssignments: () => api("GET", "/api/v1/assignments"),
    endAssignment: (id) => api("PATCH", `/api/v1/assignments/${id}/end`),
  };

  /* ----------------------------------------------------------------------
     Small utilities
     ---------------------------------------------------------------------- */
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const escapeHtml = (s) => (s ?? "").toString().replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const fmtMoney = (v) => v == null ? "—" : Number(v).toLocaleString(undefined, { style: "currency", currency: "USD", maximumFractionDigits: 0 });
  const fmtDate = (v) => v ? new Date(v).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" }) : "—";
  const initials = (a, b) => `${(a || "?")[0] || ""}${(b || "")[0] || ""}`.toUpperCase();

  function propertyStampClass(status) {
    const s = (status || "").toLowerCase();
    if (s === "available") return "stamp-green";
    if (s === "sold") return "stamp-rust";
    if (s === "rented") return "stamp-slate";
    if (s === "under contract") return "stamp-amber";
    return "stamp-neutral";
  }
  function assignmentStampClass(status) {
    const s = (status || "").toLowerCase();
    if (s === "active") return "stamp-green";
    if (s === "completed") return "stamp-slate";
    if (s === "cancelled") return "stamp-rust";
    if (s === "inactive") return "stamp-neutral";
    return "stamp-neutral";
  }

  /* ----------------------------------------------------------------------
     Toasts
     ---------------------------------------------------------------------- */
  function toast(title, body, kind = "default") {
    const stack = $("#toastStack");
    const el = document.createElement("div");
    el.className = `toast ${kind}`;
    el.innerHTML = `<div><div class="t-title">${escapeHtml(title)}</div>${body ? `<div class="t-body">${escapeHtml(body)}</div>` : ""}</div>`;
    stack.appendChild(el);
    setTimeout(() => { el.style.opacity = "0"; el.style.transition = "opacity .2s"; setTimeout(() => el.remove(), 200); }, 4200);
  }
  function toastError(err) { toast("Something went wrong", err?.message || String(err), "error"); }

  /* ----------------------------------------------------------------------
     Modal / Drawer / Confirm helpers
     ---------------------------------------------------------------------- */
  function openModal({ title, sub, bodyHtml, footHtml, onMount }) {
    closeOverlay();
    const overlay = document.createElement("div");
    overlay.className = "overlay";
    overlay.id = "activeOverlay";
    overlay.innerHTML = `
      <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-head">
          <div>
            <h3>${escapeHtml(title)}</h3>
            ${sub ? `<div class="modal-sub">${escapeHtml(sub)}</div>` : ""}
          </div>
          <button class="modal-close" data-close aria-label="Close">✕</button>
        </div>
        <div class="modal-body">${bodyHtml}</div>
        <div class="modal-foot">${footHtml}</div>
      </div>`;
    overlay.addEventListener("mousedown", (e) => { if (e.target === overlay) closeOverlay(); });
    overlay.querySelector("[data-close]").addEventListener("click", closeOverlay);
    document.body.appendChild(overlay);
    if (onMount) onMount(overlay);
  }
  function closeOverlay() { $("#activeOverlay")?.remove(); $("#activeDrawer")?.remove(); $("#activeDrawerOverlay")?.remove(); }

  function openDrawer({ title, sub, bodyHtml, footHtml, onMount }) {
    $("#activeDrawer")?.remove(); $("#activeDrawerOverlay")?.remove();
    const dOverlay = document.createElement("div");
    dOverlay.className = "drawer-overlay"; dOverlay.id = "activeDrawerOverlay";
    dOverlay.addEventListener("click", closeOverlay);
    const drawer = document.createElement("div");
    drawer.className = "drawer"; drawer.id = "activeDrawer";
    drawer.innerHTML = `
      <div class="drawer-head">
        <div><h3>${escapeHtml(title)}</h3>${sub ? `<div class="modal-sub">${escapeHtml(sub)}</div>` : ""}</div>
        <button class="modal-close" data-close aria-label="Close">✕</button>
      </div>
      <div class="drawer-body">${bodyHtml}</div>
      ${footHtml ? `<div class="drawer-foot">${footHtml}</div>` : ""}`;
    drawer.querySelector("[data-close]").addEventListener("click", closeOverlay);
    document.body.appendChild(dOverlay);
    document.body.appendChild(drawer);
    if (onMount) onMount(drawer);
  }

  function confirmAction({ title, body, confirmLabel = "Delete", danger = true, onConfirm }) {
    openModal({
      title,
      bodyHtml: `<div class="confirm-icon">⚠</div><p style="font-size:13.5px;color:var(--text-muted)">${escapeHtml(body)}</p>`,
      footHtml: `<button class="btn btn-ghost" data-cancel>Cancel</button><button class="btn ${danger ? "btn-danger" : "btn-primary"}" data-confirm>${escapeHtml(confirmLabel)}</button>`,
      onMount: (overlay) => {
        overlay.querySelector("[data-cancel]").addEventListener("click", closeOverlay);
        overlay.querySelector("[data-confirm]").addEventListener("click", async (e) => {
          e.target.disabled = true;
          e.target.innerHTML = `<span class="spinner"></span>`;
          try { await onConfirm(); closeOverlay(); } catch (err) { toastError(err); e.target.disabled = false; e.target.textContent = confirmLabel; }
        });
      },
    });
  }

  /* ----------------------------------------------------------------------
     Router / shell
     ---------------------------------------------------------------------- */
  const routes = ["dashboard", "properties", "users", "assignments"];
  const navIcons = {
    dashboard: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/></svg>`,
    properties: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 10.5 12 4l9 6.5"/><path d="M5 9.5V20h14V9.5"/><path d="M10 20v-6h4v6"/></svg>`,
    users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="9" cy="8" r="3.2"/><path d="M2.5 20c1-3.6 3.6-5.5 6.5-5.5s5.5 1.9 6.5 5.5"/><circle cx="17.5" cy="8.5" r="2.5"/><path d="M16 14.6c2.4.4 4 2.1 4.8 5"/></svg>`,
    assignments: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="4" y="3.5" width="16" height="17" rx="2"/><path d="M8 8h8M8 12h8M8 16h5"/></svg>`,
  };

  function renderShell() {
    const app = $("#app");
    app.innerHTML = `
      <div class="shell">
        <aside class="sidebar">
          <div class="brand">
            <div class="brand-stamp">PM</div>
            <div>
              <div class="brand-name">Ledger</div>
              <div class="brand-sub">Property Console</div>
            </div>
          </div>
          <nav class="nav" id="mainNav">
            ${routes.map(r => `
              <button class="nav-item" data-route="${r}">
                ${navIcons[r]}<span>${r.charAt(0).toUpperCase() + r.slice(1)}</span>
              </button>`).join("")}
          </nav>
          <div class="nav-foot">
            <button class="me-chip" id="meChip">
              <div class="avatar">${initials(state.me?.firstName, state.me?.lastName)}</div>
              <div class="me-info">
                <div class="me-name">${escapeHtml(state.me?.firstName)} ${escapeHtml(state.me?.lastName)}</div>
                <div class="me-role">${escapeHtml(state.me?.role)}</div>
              </div>
            </button>
          </div>
        </aside>
        <div class="main">
          <div class="topbar" id="topbar"></div>
          <div class="view" id="view"></div>
        </div>
      </div>`;
    $$("#mainNav .nav-item").forEach(btn => btn.addEventListener("click", () => navigate(btn.dataset.route)));
    $("#meChip").addEventListener("click", showProfileDrawer);
  }

  function setActiveNav() {
    $$("#mainNav .nav-item").forEach(btn => btn.classList.toggle("active", btn.dataset.route === state.route));
  }

  function navigate(route) {
    state.route = route;
    location.hash = `#/${route}`;
    setActiveNav();
    renderView();
  }

  window.addEventListener("hashchange", () => {
    const r = location.hash.replace("#/", "") || "dashboard";
    if (routes.includes(r)) { state.route = r; setActiveNav(); renderView(); }
  });

  function renderTopbar(title, eyebrow, actionsHtml) {
    $("#topbar").innerHTML = `
      <div class="topbar-title">
        <div class="eyebrow">${escapeHtml(eyebrow)}</div>
        <h1>${escapeHtml(title)}</h1>
      </div>
      <div class="topbar-actions">${actionsHtml || ""}</div>`;
  }

  function renderView() {
    if (state.route === "dashboard") return viewDashboard();
    if (state.route === "properties") return viewProperties();
    if (state.route === "users") return viewUsers();
    if (state.route === "assignments") return viewAssignments();
  }

  /* ----------------------------------------------------------------------
     Profile drawer (GET /me, logout)
     ---------------------------------------------------------------------- */
  function showProfileDrawer() {
    openDrawer({
      title: "Your profile",
      sub: "Session details from /api/auth/me",
      bodyHtml: `
        <div class="kv-list">
          <div class="kv-row"><div class="k">Name</div><div class="v">${escapeHtml(state.me.firstName)} ${escapeHtml(state.me.lastName)}</div></div>
          <div class="kv-row"><div class="k">Email</div><div class="v">${escapeHtml(state.me.email)}</div></div>
          <div class="kv-row"><div class="k">Role</div><div class="v"><span class="stamp stamp-amber">${escapeHtml(state.me.role)}</span></div></div>
          <div class="kv-row"><div class="k">User ID</div><div class="v data">#${state.me.id}</div></div>
        </div>`,
      footHtml: `<button class="btn btn-danger btn-block" id="logoutBtn">Log out</button>`,
      onMount: (drawer) => {
        drawer.querySelector("#logoutBtn").addEventListener("click", async (e) => {
          e.target.disabled = true;
          try { await Api.logout(); } catch (err) { /* ignore */ }
          closeOverlay();
          state.me = null;
          showAuth("login");
        });
      },
    });
  }

  /* ----------------------------------------------------------------------
     DASHBOARD
     ---------------------------------------------------------------------- */
  async function viewDashboard() {
    renderTopbar("Dashboard", "Overview", "");
    $("#view").innerHTML = `
      <div class="cards-row" id="statCards">
        ${["Properties", "Users", "Assignments", "Active assignments"].map(l => `
          <div class="stat-card"><div class="label">${l}</div><div class="value skeleton" style="width:60px;height:32px;"></div></div>`).join("")}
      </div>
      <div class="panel">
        <div class="panel-head"><h3>Recent assignments</h3></div>
        <div class="panel-body"><div class="loading-row"><span class="spinner"></span> Loading ledger…</div></div>
      </div>`;

    try {
      const [propsPage, users, assignments] = await Promise.all([
        Api.listProperties(0, 1),
        Api.listUsers(),
        Api.listAssignments(),
      ]);
      const activeCount = assignments.filter(a => a.status === "ACTIVE").length;
      const cards = [
        { label: "Properties", value: propsPage.totalElements ?? propsPage.content?.length ?? 0 },
        { label: "Users", value: users.length },
        { label: "Assignments", value: assignments.length },
        { label: "Active assignments", value: activeCount },
      ];
      $("#statCards").innerHTML = cards.map(c => `
        <div class="stat-card"><div class="label">${c.label}</div><div class="value">${c.value}</div></div>`).join("");

      const recent = [...assignments].sort((a, b) => (b.id || 0) - (a.id || 0)).slice(0, 8);
      const rows = recent.map(a => `
        <tr>
          <td class="data">#${a.id}</td>
          <td>Property #${a.propertyId}</td>
          <td>User #${a.userId}</td>
          <td>${escapeHtml(a.role)}</td>
          <td>${fmtDate(a.assignedDate)} → ${fmtDate(a.endDate)}</td>
          <td><span class="stamp ${assignmentStampClass(a.status)}">${escapeHtml(a.status)}</span></td>
        </tr>`).join("");
      $("#view .panel-body").innerHTML = recent.length ? `
        <div class="table-wrap"><table class="ledger">
          <thead><tr><th>ID</th><th>Property</th><th>User</th><th>Role</th><th>Term</th><th>Status</th></tr></thead>
          <tbody>${rows}</tbody>
        </table></div>` : emptyState("No assignments yet", "Assignments you create will show up here.");
    } catch (err) {
      toastError(err);
      $("#view .panel-body").innerHTML = emptyState("Couldn't load dashboard", err.message);
    }
  }

  function emptyState(title, sub) {
    return `<div class="empty-state"><span class="stamp stamp-neutral">Empty</span><h4>${escapeHtml(title)}</h4><p>${escapeHtml(sub || "")}</p></div>`;
  }

  /* ----------------------------------------------------------------------
     PROPERTIES
     ---------------------------------------------------------------------- */
  async function viewProperties() {
    renderTopbar("Properties", "Portfolio", `
      <select class="status-filter" id="statusFilter">
        <option value="">All statuses</option>
        <option value="Available">Available</option>
        <option value="Sold">Sold</option>
        <option value="Rented">Rented</option>
        <option value="Under Contract">Under Contract</option>
      </select>
      <button class="btn btn-primary" id="newPropertyBtn">+ New property</button>`);

    $("#view").innerHTML = `<div class="panel"><div class="panel-body"><div class="loading-row"><span class="spinner"></span> Loading properties…</div></div></div>`;

    $("#statusFilter").value = state.properties.statusFilter;
    $("#statusFilter").addEventListener("change", (e) => { state.properties.statusFilter = e.target.value; state.properties.page = 0; loadProperties(); });
    $("#newPropertyBtn").addEventListener("click", () => openPropertyForm());

    await loadProperties();
  }

  async function loadProperties() {
    const body = $("#view .panel-body") || $("#view");
    try {
      let content, page = state.properties.page, size = state.properties.size, totalPages = 1, totalElements;
      if (state.properties.statusFilter) {
        content = await Api.propertiesByStatus(state.properties.statusFilter);
        totalElements = content.length;
      } else {
        const pageRes = await Api.listProperties(page, size);
        content = pageRes.content; totalPages = pageRes.totalPages; totalElements = pageRes.totalElements;
      }
      state.properties.content = content;
      state.properties.totalPages = totalPages;
      state.properties.totalElements = totalElements;
      renderPropertiesTable();
    } catch (err) {
      toastError(err);
      body.innerHTML = emptyState("Couldn't load properties", err.message);
    }
  }

  function renderPropertiesTable() {
    const { content, page, totalPages, totalElements, statusFilter } = state.properties;
    const rows = content.map(p => `
      <tr data-id="${p.id}">
        <td class="data">#${p.id}</td>
        <td><div class="row-title">${escapeHtml(p.propertyName)}</div><div class="row-sub">${escapeHtml(p.location)}</div></td>
        <td>${escapeHtml(p.propertyType || "—")}</td>
        <td class="num">${fmtMoney(p.propertyValue)}</td>
        <td class="num">${p.rooms ?? "—"}</td>
        <td><span class="stamp ${propertyStampClass(p.propertyStatus)}">${escapeHtml(p.propertyStatus)}</span></td>
        <td>
          <div class="actions-cell">
            <button class="btn btn-ghost btn-sm" data-act="assignments">Assignments</button>
            <button class="btn btn-ghost btn-sm" data-act="edit">Edit</button>
            <button class="btn btn-danger btn-sm" data-act="delete">Delete</button>
          </div>
        </td>
      </tr>`).join("");

    $("#view").innerHTML = `
      <div class="panel">
        <div class="table-wrap">
          <table class="ledger">
            <thead><tr><th>ID</th><th>Property</th><th>Type</th><th class="num">Value</th><th class="num">Rooms</th><th>Status</th><th></th></tr></thead>
            <tbody>${rows || ""}</tbody>
          </table>
        </div>
        ${content.length ? "" : emptyState("No properties found", statusFilter ? `Nothing with status "${statusFilter}".` : "Create your first property to get started.")}
        ${!statusFilter && content.length ? `
        <div class="pagination">
          <div>Page <span class="pg-num">${page + 1}</span> of <span class="pg-num">${Math.max(totalPages, 1)}</span> · ${totalElements} total</div>
          <div class="pg-controls">
            <button class="btn btn-ghost btn-sm" id="prevPage" ${page <= 0 ? "disabled" : ""}>← Prev</button>
            <button class="btn btn-ghost btn-sm" id="nextPage" ${page + 1 >= totalPages ? "disabled" : ""}>Next →</button>
          </div>
        </div>` : ""}
      </div>`;

    if (!statusFilter) {
      $("#prevPage")?.addEventListener("click", () => { state.properties.page--; loadProperties(); });
      $("#nextPage")?.addEventListener("click", () => { state.properties.page++; loadProperties(); });
    }
    $$("table.ledger tbody tr").forEach(tr => {
      const id = tr.dataset.id;
      tr.addEventListener("click", (e) => {
        const act = e.target.closest("[data-act]")?.dataset.act;
        if (act === "edit") return openPropertyForm(id);
        if (act === "delete") return deleteProperty(id);
        if (act === "assignments") return showPropertyAssignments(id);
        showPropertyDrawer(id);
      });
    });
  }

  async function showPropertyDrawer(id) {
    openDrawer({ title: `Property #${id}`, sub: "Loading…", bodyHtml: `<div class="loading-row"><span class="spinner"></span></div>` });
    try {
      const p = await Api.getProperty(id);
      $("#activeDrawer .drawer-head .modal-sub").textContent = p.propertyName;
      $("#activeDrawer .drawer-body").innerHTML = `
        <div class="kv-list">
          <div class="kv-row"><div class="k">Status</div><div class="v"><span class="stamp ${propertyStampClass(p.propertyStatus)}">${escapeHtml(p.propertyStatus)}</span></div></div>
          <div class="kv-row"><div class="k">Type</div><div class="v">${escapeHtml(p.propertyType || "—")}</div></div>
          <div class="kv-row"><div class="k">Value</div><div class="v">${fmtMoney(p.propertyValue)}</div></div>
          <div class="kv-row"><div class="k">Rooms</div><div class="v">${p.rooms ?? "—"}</div></div>
          <div class="kv-row"><div class="k">Location</div><div class="v">${escapeHtml(p.location)}</div></div>
          <div class="kv-row"><div class="k">Owner ID</div><div class="v data">#${p.OwnerId ?? p.ownerId ?? "—"}</div></div>
          <div class="kv-row"><div class="k">Created</div><div class="v">${fmtDate(p.createdDate)}</div></div>
        </div>`;
      $("#activeDrawer").insertAdjacentHTML("beforeend", `
        <div class="drawer-foot">
          <button class="btn btn-ghost" id="viewAssignBtn">Assignments</button>
          <button class="btn btn-primary" id="editPropBtn">Edit</button>
        </div>`);
      $("#viewAssignBtn").addEventListener("click", () => showPropertyAssignments(id));
      $("#editPropBtn").addEventListener("click", () => { closeOverlay(); openPropertyForm(id); });
    } catch (err) { toastError(err); closeOverlay(); }
  }

  async function showPropertyAssignments(propertyId) {
    openDrawer({ title: `Assignments`, sub: `Property #${propertyId}`, bodyHtml: `<div class="loading-row"><span class="spinner"></span></div>` });
    try {
      const list = await Api.propertyAssignments(propertyId);
      $("#activeDrawer .drawer-body").innerHTML = list.length ? `
        <div class="mini-list">
          ${list.map(a => `
            <div class="mini-item">
              <div><strong>User #${a.userId}</strong> · ${escapeHtml(a.role)}<div class="row-sub">${fmtDate(a.assignedDate)} → ${fmtDate(a.endDate)}</div></div>
              <span class="stamp ${assignmentStampClass(a.status)}">${escapeHtml(a.status)}</span>
            </div>`).join("")}
        </div>` : emptyState("No assignments", "This property has no assignment history yet.");
    } catch (err) { toastError(err); closeOverlay(); }
  }

  function openPropertyForm(id) {
    const editing = !!id;
    const existing = editing ? state.properties.content.find(p => String(p.id) === String(id)) : null;
    openModal({
      title: editing ? "Edit property" : "New property",
      sub: editing ? `Property #${id} · PATCH partial update` : "POST /api/v1/properties",
      bodyHtml: `
        <form id="propForm">
          <div class="form-grid">
            <div class="field span-2"><label>Property name</label><input name="propertyName" required value="${escapeHtml(existing?.propertyName || "")}"></div>
            <div class="field"><label>Value (USD)</label><input name="propertyValue" type="number" step="0.01" required value="${existing?.propertyValue ?? ""}"></div>
            <div class="field"><label>Rooms</label><input name="rooms" type="number" min="0" value="${existing?.rooms ?? ""}"></div>
            <div class="field"><label>Type</label>
              <select name="propertyType">
                ${["Residential", "Commercial", "Industrial", "Land"].map(t => `<option value="${t}" ${existing?.propertyType === t ? "selected" : ""}>${t}</option>`).join("")}
              </select>
            </div>
            <div class="field"><label>Status</label>
              <select name="propertyStatus">
                ${["Available", "Sold", "Rented", "Under Contract"].map(t => `<option value="${t}" ${existing?.propertyStatus === t ? "selected" : ""}>${t}</option>`).join("")}
              </select>
            </div>
            <div class="field span-2"><label>Location</label><input name="location" required value="${escapeHtml(existing?.location || "")}"></div>
            ${editing ? "" : `<div class="field span-2"><label>Owner ID</label><input name="ownerId" type="number" required placeholder="e.g. 1"><div class="hint">The user ID this property belongs to.</div></div>`}
          </div>
        </form>`,
      footHtml: `<button class="btn btn-ghost" data-cancel>Cancel</button><button class="btn btn-primary" id="savePropBtn">${editing ? "Save changes" : "Create property"}</button>`,
      onMount: (overlay) => {
        overlay.querySelector("[data-cancel]").addEventListener("click", closeOverlay);
        overlay.querySelector("#savePropBtn").addEventListener("click", async (e) => {
          const form = $("#propForm");
          if (!form.reportValidity()) return;
          const fd = new FormData(form);
          const dto = {
            propertyName: fd.get("propertyName"),
            propertyValue: parseFloat(fd.get("propertyValue")),
            propertyType: fd.get("propertyType"),
            propertyStatus: fd.get("propertyStatus"),
            rooms: fd.get("rooms") ? parseInt(fd.get("rooms"), 10) : null,
            location: fd.get("location"),
          };
          e.target.disabled = true; e.target.innerHTML = `<span class="spinner"></span>`;
          try {
            if (editing) { await Api.patchProperty(id, dto); toast("Saved", `Property #${id} updated.`, "success"); }
            else { dto.ownerId = parseInt(fd.get("ownerId"), 10); await Api.createProperty(dto); toast("Created", "New property added.", "success"); }
            closeOverlay();
            loadProperties();
          } catch (err) { toastError(err); e.target.disabled = false; e.target.textContent = editing ? "Save changes" : "Create property"; }
        });
      },
    });
  }

  function deleteProperty(id) {
    confirmAction({
      title: "Delete property",
      body: `This permanently deletes property #${id}. This cannot be undone.`,
      onConfirm: async () => { await Api.deleteProperty(id); toast("Deleted", `Property #${id} removed.`, "success"); loadProperties(); },
    });
  }

  /* ----------------------------------------------------------------------
     USERS
     ---------------------------------------------------------------------- */
  async function viewUsers() {
    renderTopbar("Users", "Directory", `
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>
        <input id="userSearch" placeholder="Filter by name or email…">
      </div>`);
    $("#view").innerHTML = `<div class="panel"><div class="panel-body"><div class="loading-row"><span class="spinner"></span> Loading users…</div></div></div>`;
    try {
      const users = await Api.listUsers();
      state.users.content = users;
      renderUsersTable(users);
      $("#userSearch").addEventListener("input", (e) => {
        const q = e.target.value.toLowerCase();
        const filtered = state.users.content.filter(u =>
          `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(q));
        renderUsersTable(filtered);
      });
    } catch (err) {
      toastError(err);
      $("#view").innerHTML = emptyState("Couldn't load users", err.message);
    }
  }

  function renderUsersTable(users) {
    const rows = users.map(u => `
      <tr data-id="${u.id}">
        <td class="data">#${u.id}</td>
        <td><div class="avatar" style="width:28px;height:28px;font-size:11px;display:inline-flex;vertical-align:middle;margin-right:8px;">${initials(u.firstName, u.lastName)}</div><span class="row-title">${escapeHtml(u.firstName)} ${escapeHtml(u.lastName)}</span></td>
        <td>${escapeHtml(u.email)}</td>
        <td>${escapeHtml(u.phone || "—")}</td>
        <td><span class="role-pill">${escapeHtml(u.role)}</span></td>
        <td>
          <div class="actions-cell">
            <button class="btn btn-ghost btn-sm" data-act="assignments">Assignments</button>
            <button class="btn btn-ghost btn-sm" data-act="edit">Edit</button>
            <button class="btn btn-danger btn-sm" data-act="delete">Delete</button>
          </div>
        </td>
      </tr>`).join("");

    $("#view").innerHTML = `
      <div class="panel">
        <div class="table-wrap">
          <table class="ledger">
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th></th></tr></thead>
            <tbody>${rows}</tbody>
          </table>
        </div>
        ${users.length ? "" : emptyState("No users found", "Try a different search.")}
      </div>`;

    $$("table.ledger tbody tr").forEach(tr => {
      const id = tr.dataset.id;
      tr.addEventListener("click", (e) => {
        const act = e.target.closest("[data-act]")?.dataset.act;
        if (act === "edit") return openUserForm(id);
        if (act === "delete") return deleteUser(id);
        if (act === "assignments") return showUserAssignments(id);
        showUserDrawer(id);
      });
    });
  }

  async function showUserDrawer(id) {
    openDrawer({ title: `User #${id}`, sub: "Loading…", bodyHtml: `<div class="loading-row"><span class="spinner"></span></div>` });
    try {
      const u = await Api.getUser(id);
      $("#activeDrawer .drawer-head .modal-sub").textContent = `${u.firstName} ${u.lastName}`;
      $("#activeDrawer .drawer-body").innerHTML = `
        <div class="kv-list">
          <div class="kv-row"><div class="k">Email</div><div class="v">${escapeHtml(u.email)}</div></div>
          <div class="kv-row"><div class="k">Phone</div><div class="v">${escapeHtml(u.phone || "—")}</div></div>
          <div class="kv-row"><div class="k">Role</div><div class="v"><span class="stamp stamp-amber">${escapeHtml(u.role)}</span></div></div>
        </div>`;
      $("#activeDrawer").insertAdjacentHTML("beforeend", `
        <div class="drawer-foot">
          <button class="btn btn-ghost" id="viewAssignBtn">Assignments</button>
          <button class="btn btn-primary" id="editUserBtn">Edit</button>
        </div>`);
      $("#viewAssignBtn").addEventListener("click", () => showUserAssignments(id));
      $("#editUserBtn").addEventListener("click", () => { closeOverlay(); openUserForm(id); });
    } catch (err) { toastError(err); closeOverlay(); }
  }

  async function showUserAssignments(userId) {
    openDrawer({ title: `Assignments`, sub: `User #${userId}`, bodyHtml: `<div class="loading-row"><span class="spinner"></span></div>` });
    try {
      const list = await Api.userAssignments(userId);
      $("#activeDrawer .drawer-body").innerHTML = list.length ? `
        <div class="mini-list">
          ${list.map(a => `
            <div class="mini-item">
              <div><strong>Property #${a.propertyId}</strong> · ${escapeHtml(a.role)}<div class="row-sub">${fmtDate(a.assignedDate)} → ${fmtDate(a.endDate)}</div></div>
              <span class="stamp ${assignmentStampClass(a.status)}">${escapeHtml(a.status)}</span>
            </div>`).join("")}
        </div>` : emptyState("No assignments", "This user has no assignment history yet.");
    } catch (err) { toastError(err); closeOverlay(); }
  }

  function openUserForm(id) {
    const existing = state.users.content.find(u => String(u.id) === String(id));
    openModal({
      title: "Edit user",
      sub: `User #${id} · PATCH partial update`,
      bodyHtml: `
        <form id="userForm">
          <div class="form-grid">
            <div class="field"><label>First name</label><input name="firstName" required value="${escapeHtml(existing?.firstName || "")}"></div>
            <div class="field"><label>Last name</label><input name="lastName" required value="${escapeHtml(existing?.lastName || "")}"></div>
            <div class="field span-2"><label>Email</label><input name="email" type="email" required value="${escapeHtml(existing?.email || "")}"></div>
            <div class="field span-2"><label>Phone</label><input name="phone" value="${escapeHtml(existing?.phone || "")}"></div>
          </div>
        </form>`,
      footHtml: `<button class="btn btn-ghost" data-cancel>Cancel</button><button class="btn btn-primary" id="saveUserBtn">Save changes</button>`,
      onMount: (overlay) => {
        overlay.querySelector("[data-cancel]").addEventListener("click", closeOverlay);
        overlay.querySelector("#saveUserBtn").addEventListener("click", async (e) => {
          const form = $("#userForm");
          if (!form.reportValidity()) return;
          const fd = new FormData(form);
          const dto = { firstName: fd.get("firstName"), lastName: fd.get("lastName"), email: fd.get("email"), phone: fd.get("phone") };
          e.target.disabled = true; e.target.innerHTML = `<span class="spinner"></span>`;
          try {
            await Api.patchUser(id, dto);
            toast("Saved", `User #${id} updated.`, "success");
            closeOverlay();
            viewUsers();
          } catch (err) { toastError(err); e.target.disabled = false; e.target.textContent = "Save changes"; }
        });
      },
    });
  }

  function deleteUser(id) {
    confirmAction({
      title: "Delete user",
      body: `This permanently deletes user #${id}. This cannot be undone.`,
      onConfirm: async () => { await Api.deleteUser(id); toast("Deleted", `User #${id} removed.`, "success"); viewUsers(); },
    });
  }

  /* ----------------------------------------------------------------------
     ASSIGNMENTS
     ---------------------------------------------------------------------- */
  async function viewAssignments() {
    renderTopbar("Assignments", "Ledger of property ↔ user links", `<button class="btn btn-primary" id="newAssignBtn">+ New assignment</button>`);
    $("#view").innerHTML = `<div class="panel"><div class="panel-body"><div class="loading-row"><span class="spinner"></span> Loading assignments…</div></div></div>`;
    $("#newAssignBtn").addEventListener("click", () => openAssignmentForm());
    try {
      const list = await Api.listAssignments();
      state.assignments.content = list;
      renderAssignmentsTable(list);
    } catch (err) {
      toastError(err);
      $("#view").innerHTML = emptyState("Couldn't load assignments", err.message);
    }
  }

  function renderAssignmentsTable(list) {
    const sorted = [...list].sort((a, b) => (b.id || 0) - (a.id || 0));
    const rows = sorted.map(a => `
      <tr data-id="${a.id}">
        <td class="data">#${a.id}</td>
        <td>Property #${a.propertyId}</td>
        <td>User #${a.userId}</td>
        <td><span class="role-pill">${escapeHtml(a.role)}</span></td>
        <td>${fmtDate(a.assignedDate)} → ${fmtDate(a.endDate)}</td>
        <td><span class="stamp ${assignmentStampClass(a.status)}">${escapeHtml(a.status)}</span></td>
        <td>
          <div class="actions-cell">
            ${a.status === "ACTIVE" ? `<button class="btn btn-ghost btn-sm" data-act="end">End</button>` : ""}
          </div>
        </td>
      </tr>`).join("");

    $("#view").innerHTML = `
      <div class="panel">
        <div class="table-wrap">
          <table class="ledger">
            <thead><tr><th>ID</th><th>Property</th><th>User</th><th>Role</th><th>Term</th><th>Status</th><th></th></tr></thead>
            <tbody>${rows}</tbody>
          </table>
        </div>
        ${list.length ? "" : emptyState("No assignments yet", "Create one to link a property with a user.")}
      </div>`;

    $$("table.ledger tbody tr").forEach(tr => {
      const id = tr.dataset.id;
      tr.addEventListener("click", (e) => {
        const act = e.target.closest("[data-act]")?.dataset.act;
        if (act === "end") return endAssignment(id);
        showAssignmentDrawer(id);
      });
    });
  }

  async function showAssignmentDrawer(id) {
    openDrawer({ title: `Assignment #${id}`, sub: "Loading…", bodyHtml: `<div class="loading-row"><span class="spinner"></span></div>` });
    try {
      const a = await Api.getAssignment(id);
      $("#activeDrawer .drawer-head .modal-sub").textContent = `Property #${a.propertyId} · User #${a.userId}`;
      $("#activeDrawer .drawer-body").innerHTML = `
        <div class="kv-list">
          <div class="kv-row"><div class="k">Property</div><div class="v data">#${a.propertyId}</div></div>
          <div class="kv-row"><div class="k">User</div><div class="v data">#${a.userId}</div></div>
          <div class="kv-row"><div class="k">Role</div><div class="v">${escapeHtml(a.role)}</div></div>
          <div class="kv-row"><div class="k">Assigned</div><div class="v">${fmtDate(a.assignedDate)}</div></div>
          <div class="kv-row"><div class="k">Ends</div><div class="v">${fmtDate(a.endDate)}</div></div>
          <div class="kv-row"><div class="k">Status</div><div class="v"><span class="stamp ${assignmentStampClass(a.status)}">${escapeHtml(a.status)}</span></div></div>
        </div>`;
      if (a.status === "ACTIVE") {
        $("#activeDrawer").insertAdjacentHTML("beforeend", `<div class="drawer-foot"><button class="btn btn-danger" id="endAssignBtn">End assignment</button></div>`);
        $("#endAssignBtn").addEventListener("click", () => { closeOverlay(); endAssignment(id); });
      }
    } catch (err) { toastError(err); closeOverlay(); }
  }

  function openAssignmentForm() {
    openModal({
      title: "New assignment",
      sub: "POST /api/v1/assignments",
      bodyHtml: `
        <form id="assignForm">
          <div class="form-grid">
            <div class="field"><label>Property ID</label><input name="propertyId" type="number" required></div>
            <div class="field"><label>User ID</label><input name="userId" type="number" required></div>
            <div class="field"><label>Role</label>
              <select name="role">
                <option value="PROPERTY_MANAGER">Property manager</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="INSPECTOR">Inspector</option>
              </select>
            </div>
            <div class="field"><label>Status</label>
              <select name="status">
                <option value="ACTIVE">Active</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
            <div class="field"><label>Assigned date</label><input name="assignedDate" type="date" required></div>
            <div class="field"><label>End date</label><input name="endDate" type="date" required></div>
          </div>
        </form>`,
      footHtml: `<button class="btn btn-ghost" data-cancel>Cancel</button><button class="btn btn-primary" id="saveAssignBtn">Create assignment</button>`,
      onMount: (overlay) => {
        overlay.querySelector("[data-cancel]").addEventListener("click", closeOverlay);
        overlay.querySelector("#saveAssignBtn").addEventListener("click", async (e) => {
          const form = $("#assignForm");
          if (!form.reportValidity()) return;
          const fd = new FormData(form);
          const dto = {
            propertyId: parseInt(fd.get("propertyId"), 10),
            userId: parseInt(fd.get("userId"), 10),
            role: fd.get("role"),
            status: fd.get("status"),
            assignedDate: fd.get("assignedDate"),
            endDate: fd.get("endDate"),
          };
          e.target.disabled = true; e.target.innerHTML = `<span class="spinner"></span>`;
          try {
            await Api.createAssignment(dto);
            toast("Created", "New assignment added.", "success");
            closeOverlay();
            viewAssignments();
          } catch (err) { toastError(err); e.target.disabled = false; e.target.textContent = "Create assignment"; }
        });
      },
    });
  }

  function endAssignment(id) {
    confirmAction({
      title: "End assignment",
      body: `Mark assignment #${id} as ended? This cannot be undone.`,
      confirmLabel: "End assignment",
      onConfirm: async () => { await Api.endAssignment(id); toast("Ended", `Assignment #${id} marked ended.`, "success"); viewAssignments(); },
    });
  }

  /* ----------------------------------------------------------------------
     AUTH SCREENS
     ---------------------------------------------------------------------- */
  function showAuth(mode = "login") {
    const app = $("#app");
    app.innerHTML = `
      <div class="auth-screen">
        <div class="auth-card">
          <div class="auth-brand">
            <div class="brand-stamp">PM</div>
            <h2>${mode === "login" ? "Welcome back" : "Create your account"}</h2>
            <p>Ledger · Property Console</p>
          </div>
          <div id="authError"></div>
          <form id="authForm">
            ${mode === "register" ? `
              <div class="form-grid">
                <div class="field"><label>First name</label><input name="firstName" required></div>
                <div class="field"><label>Last name</label><input name="lastName" required></div>
              </div>` : ""}
            <div class="field"><label>Email</label><input name="email" type="email" required autocomplete="username"></div>
            ${mode === "register" ? `<div class="field"><label>Phone</label><input name="phone"></div>` : ""}
            <div class="field"><label>Password</label><input name="password" type="password" required autocomplete="${mode === "login" ? "current-password" : "new-password"}"></div>
            <button class="btn btn-primary btn-block" id="authSubmit" type="submit">${mode === "login" ? "Log in" : "Register"}</button>
          </form>
          <div class="auth-switch">
            ${mode === "login" ? `New here? <button id="toRegister">Create an account</button>` : `Already have an account? <button id="toLogin">Log in</button>`}
          </div>
        </div>
      </div>`;

    $("#toRegister")?.addEventListener("click", () => showAuth("register"));
    $("#toLogin")?.addEventListener("click", () => showAuth("login"));

    $("#authForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const btn = $("#authSubmit");
      btn.disabled = true; const label = btn.textContent; btn.innerHTML = `<span class="spinner"></span>`;
      $("#authError").innerHTML = "";
      try {
        if (mode === "login") {
          await Api.login({ email: fd.get("email"), password: fd.get("password") });
        } else {
          await Api.register({
            firstName: fd.get("firstName"), lastName: fd.get("lastName"),
            email: fd.get("email"), phone: fd.get("phone"), password: fd.get("password"),
          });
          await Api.login({ email: fd.get("email"), password: fd.get("password") });
        }
        await boot();
      } catch (err) {
        $("#authError").innerHTML = `<div class="auth-error">${escapeHtml(err.message)}</div>`;
        btn.disabled = false; btn.textContent = label;
      }
    });
  }

  /* ----------------------------------------------------------------------
     Boot
     ---------------------------------------------------------------------- */
  async function boot() {
    if (!$("#toastStack")) document.body.insertAdjacentHTML("beforeend", `<div class="toast-stack" id="toastStack"></div>`);
    try {
      state.me = await Api.me();
      renderShell();
      const initialRoute = (location.hash.replace("#/", "") || "dashboard");
      state.route = routes.includes(initialRoute) ? initialRoute : "dashboard";
      location.hash = `#/${state.route}`;
      setActiveNav();
      renderView();
    } catch {
      showAuth("login");
    }
  }

  document.addEventListener("DOMContentLoaded", boot);
})();