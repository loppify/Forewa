"use strict";

const API = "/api/v1";
const state = { me: null, paths: [], csrf: null, viewSequence: 0, skillView: "tree", graph: null };
const view = document.querySelector("#view");
const modalRoot = document.querySelector("#modal-root");
const esc = value => String(value ?? "").replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
const urlId = id => encodeURIComponent(id);
const dateLabel = value => value ? new Date(`${value}T12:00:00`).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" }) : "";

function notify(message, type = "success") {
  const root = document.querySelector("#toast-root");
  root.innerHTML = `<div class="toast ${type}">${esc(message)}</div>`;
  clearTimeout(notify.timer);
  notify.timer = setTimeout(() => root.replaceChildren(), 4000);
}

async function api(endpoint, { method = "GET", body } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (!["GET", "HEAD"].includes(method)) {
    if (!state.csrf) state.csrf = await api("/csrf");
    headers[state.csrf.headerName] = state.csrf.token;
  }
  let response;
  try {
    response = await fetch(`${API}${endpoint}`, {
      method, headers, credentials: "same-origin",
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    });
  } catch {
    throw Object.assign(new Error("Could not reach the server. Check your connection and try again."), { status: 0 });
  }
  const contentType = response.headers.get("content-type") || "";
  const result = contentType.includes("json") ? await response.json() : null;
  if (!response.ok) {
    if (response.status === 401) { state.me = null; state.csrf = null; showLogin(); }
    if (response.status === 403) state.csrf = null;
    const error = new Error(result?.detail || result?.title || `Request failed (${response.status}).`);
    error.status = response.status;
    error.code = result?.code;
    throw error;
  }
  return result;
}

function showLogin() {
  state.graph?.destroy(); state.graph = null;
  document.querySelector("#app").hidden = true;
  document.querySelector("#login").hidden = false;
  modalRoot.replaceChildren();
  document.title = "Welcome · Forewa";
}

function showApp() {
  document.querySelector("#login").hidden = true;
  document.querySelector("#app").hidden = false;
  const name = state.me.displayName || state.me.email?.split("@")[0] || "Your space";
  document.querySelector("#profile-name").textContent = name;
  document.querySelector("#avatar").textContent = name[0].toUpperCase();
  document.querySelector("#top-profile").textContent = name[0].toUpperCase();
  document.querySelector("#today-label").textContent = new Date().toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" });
}

async function boot() {
  try {
    state.me = await api("/me");
    try { state.skillView = localStorage.getItem(`forewa:skill-view:${state.me.id}`) === "graph" ? "graph" : "tree"; }
    catch { state.skillView = "tree"; }
    showApp();
    await route();
    if (!state.me.timezone) openTimezone(true);
  } catch (error) {
    if (error.status === 401 || error.status === 404) showLogin();
    else {
      document.querySelector("#login").hidden = true;
      document.querySelector("#app").hidden = false;
      showError(error);
    }
  }
}

function showError(error) {
  view.innerHTML = `<div class="error-card"><h3>Something went wrong</h3><p>${esc(error.message)}</p><button class="button button-secondary" type="button" data-action="retry">Try again</button></div>`;
}

const loading = () => `<div class="loading"><span class="loading-icon">✳</span>Finding your way…</div>`;
const empty = (icon, title, message, button = "") => `<div class="empty"><div class="empty-symbol">${icon}</div><h3>${esc(title)}</h3><p>${esc(message)}</p>${button}</div>`;
const link = (route, label) => `<a href="${route}">${esc(label)}</a>`;

async function route() {
  if (!state.me) return;
  const sequence = ++state.viewSequence;
  state.graph?.destroy(); state.graph = null;
  const hash = decodeURIComponent(location.hash.slice(1) || "/");
  const parts = hash.split("/").filter(Boolean);
  const section = parts[0] || "home";
  document.querySelectorAll("[data-nav]").forEach(item => item.classList.toggle("active", item.dataset.nav === (section === "home" ? "home" : "paths")));
  document.querySelector("#location-label").textContent = { home: "Overview", paths: "My paths", skills: "Skill detail" }[section] || "Overview";
  view.innerHTML = loading();
  try {
    let html;
    if (section === "paths" && parts[1]) html = await pathPage(parts[1]);
    else if (section === "skills" && parts[1]) html = await skillPage(parts[1]);
    else if (section === "paths") html = await pathsPage();
    else html = await homePage();
    if (sequence !== state.viewSequence || !state.me) return;
    view.innerHTML = html;
    if (section === "paths" && parts[1] && state.skillView === "graph" && state.skills.length) {
      state.graph = createSkillGraph(document.querySelector("#skill-graph"), state.skills, id => { location.hash = `#/skills/${urlId(id)}`; });
    }
    document.title = `${document.querySelector("#location-label").textContent} · Forewa`;
  } catch (error) {
    if (sequence !== state.viewSequence || !state.me) return;
    showError(error);
  }
}

function pathCard(path, index) {
  return `<a class="card path-card" href="#/paths/${urlId(path.id)}"><span class="path-number">PATH ${String(index + 1).padStart(2, "0")}</span><h3>${esc(path.name)}</h3><p>${esc(path.description || "A new direction, waiting for its first step.")}</p><div class="path-footer"><span>Explore this path</span><span class="path-arrow">↗</span></div></a>`;
}

async function homePage() {
  state.paths = await api("/paths");
  const firstName = (state.me.displayName || "friend").split(" ")[0];
  const cards = state.paths.slice(0, 4).map(pathCard).join("");
  return `<span class="eyebrow"><span class="eyebrow-line"></span> THE JOURNEY CONTINUES</span>
    <section class="card hero"><h1>Make space for<br><span class="highlight">what you can become.</span></h1><p>Welcome back, ${esc(firstName)}. Each path begins with one meaningful step.</p><button class="button button-primary" type="button" data-action="new-path">＋ Create a path <span aria-hidden="true">↗</span></button></section>
    <section class="stat-row" aria-label="Your progress"><div class="card stat-card"><span class="stat-icon">◇</span><div><strong>${state.paths.length}</strong><small>Paths started</small></div></div><div class="card stat-card"><span class="stat-icon">✦</span><div><strong>${state.paths.length ? "Keep going" : "Begin"}</strong><small>Your next step</small></div></div><div class="card stat-card"><span class="stat-icon">◷</span><div><strong>Today</strong><small>Is a fresh start</small></div></div></section>
    <div class="section-heading"><div><span class="section-kicker">YOUR WORLD</span><h2>Paths to explore</h2></div>${link("#/paths", "View all ↗")}</div>
    ${state.paths.length ? `<div class="path-grid">${cards}<button class="card path-card add-card" type="button" data-action="new-path"><span class="add-icon">＋</span><strong>New path</strong><span>Begin something meaningful</span></button></div>` : empty("✦", "Your journey starts here", "Create a path for a skill or direction you want to develop.", `<button class="button button-primary" type="button" data-action="new-path">Create your first path</button>`)}`;
}

async function pathsPage() {
  state.paths = await api("/paths");
  return `<span class="eyebrow"><span class="eyebrow-line"></span> YOUR DIRECTIONS</span><div class="page-header"><div><h1>My <em>paths.</em></h1><p>Every path holds the skills you're growing. Choose one to see where it leads.</p></div><button class="button button-primary" type="button" data-action="new-path">＋ New path</button></div>
  ${state.paths.length ? `<div class="path-grid">${state.paths.map(pathCard).join("")}<button class="card path-card add-card" type="button" data-action="new-path"><span class="add-icon">＋</span><strong>Start another path</strong><span>Choose a new direction</span></button></div>` : empty("◇", "No paths yet", "Start with something you'd like to learn, practice, or master.", `<button class="button button-primary" type="button" data-action="new-path">Create a path</button>`)}`;
}

function skillTree(skills) {
  const children = new Map();
  const ids = new Set(skills.map(skill => skill.id));
  for (const skill of skills) {
    const parent = ids.has(skill.parentId) ? skill.parentId : null;
    if (!children.has(parent)) children.set(parent, []);
    children.get(parent).push(skill);
  }
  for (const siblings of children.values()) siblings.sort((a, b) => a.sortOrder - b.sortOrder || a.name.localeCompare(b.name));
  const visit = (parent, seen = new Set()) => (children.get(parent) || []).map(skill => {
    if (seen.has(skill.id)) return "";
    const branchSeen = new Set(seen); branchSeen.add(skill.id);
    return `<div class="tree-branch"><div class="tree-node ${skill.nodeType === "TRAINABLE" ? "trainable" : ""}"><span class="node-bullet" aria-hidden="true">${skill.nodeType === "CATEGORY" ? "◇" : "✧"}</span><a href="#/skills/${urlId(skill.id)}" class="tree-node-main"><strong>${esc(skill.name)}</strong><small>${skill.nodeType === "CATEGORY" ? "CATEGORY" : "TRAINABLE SKILL"}</small></a><div class="tree-node-actions">${skill.nodeType === "CATEGORY" ? `<button class="icon-button" type="button" data-action="add-child" data-id="${esc(skill.id)}" aria-label="Add skill under ${esc(skill.name)}" title="Add a child">＋</button>` : ""}<a class="icon-button node-chevron" href="#/skills/${urlId(skill.id)}" aria-label="Open ${esc(skill.name)}">↗</a></div></div>${children.has(skill.id) ? `<div class="tree-children">${visit(skill.id, branchSeen)}</div>` : ""}</div>`;
  }).join("");
  return `<div class="tree">${visit(null)}</div>`;
}

async function pathPage(pathId) {
  const [path, skills] = await Promise.all([api(`/paths/${urlId(pathId)}`), api(`/paths/${urlId(pathId)}/skills`)]);
  state.path = path; state.skills = skills;
  return `<div class="breadcrumb">${link("#/paths", "My paths")}<span>/</span><strong>${esc(path.name)}</strong></div><span class="eyebrow"><span class="eyebrow-line"></span> PATH DETAIL</span>
    <div class="page-header"><div><h1>${esc(path.name)}<em>.</em></h1><p>${esc(path.description || "A place to organize the skills you want to grow.")}</p></div><div class="header-actions"><button type="button" class="button button-ghost" data-action="edit-path">Edit path</button><button type="button" class="button button-primary" data-action="add-skill">＋ Add skill</button></div></div>
    <div class="two-column ${state.skillView === "graph" ? "graph-layout" : ""}"><section class="card panel"><div class="panel-head"><div><span class="eyebrow-small">THE MAP</span><h2>${state.skillView === "graph" ? "Skill graph" : "Your skill tree"}</h2></div><span class="panel-note">${skills.length} ${skills.length === 1 ? "node" : "nodes"}</span></div>
      ${skills.length ? state.skillView === "graph" ? `<div class="graph-wrap"><div class="graph-toolbar"><div class="graph-legend"><span><i class="legend-dot category-dot"></i> Category</span><span><i class="legend-dot trainable-dot"></i> Trainable</span></div><div class="graph-controls"><button class="icon-button" type="button" data-action="graph-zoom-in" aria-label="Zoom in" title="Zoom in">＋</button><button class="icon-button" type="button" data-action="graph-zoom-out" aria-label="Zoom out" title="Zoom out">−</button><button class="icon-button graph-fit" type="button" data-action="graph-fit" aria-label="Fit graph" title="Fit graph">⌗</button></div></div><div id="skill-graph" class="skill-graph"><canvas aria-label="Interactive graph of skills in ${esc(path.name)}"></canvas><div class="graph-tooltip" hidden></div></div><p class="graph-hint">Drag nodes to explore · drag the background to pan · scroll or use the buttons to zoom · select a node to open it</p><details class="graph-access-list"><summary>Browse skills as a list</summary><div>${skills.map(skill => `<a href="#/skills/${urlId(skill.id)}">${esc(skill.name)}</a>`).join("")}</div></details></div>` : skillTree(skills) : empty("✧", "A blank canvas", "Begin with a category or a trainable skill.", `<button class="button button-primary button-small" type="button" data-action="add-skill">Add your first skill</button>`)}</section>
      <aside class="card info-card"><span class="info-symbol">✦</span><span class="eyebrow-small">HOW IT WORKS</span><h3>Build your path, one skill at a time.</h3><p>Categories organize your tree. Trainable skills are the leaves—give each one a concrete goal and make it your own.</p><button class="button button-secondary" type="button" data-action="add-skill">＋ Add a node</button><div class="divider"></div><button class="button-link" type="button" data-action="archive-path">Archive this path</button></aside></div>`;
}

function goalCard(goal, kind) {
  const metadata = [goal.targetValue != null ? `Target: ${goal.targetValue}${goal.unit ? ` ${goal.unit}` : ""}` : "", goal.targetDate ? `By ${dateLabel(goal.targetDate)}` : "", goal.baselineValue != null ? `Starting at ${goal.baselineValue}${goal.unit ? ` ${goal.unit}` : ""}` : ""].filter(Boolean);
  const actions = kind === "current" ? `<button class="button button-small button-primary" type="button" data-action="goal-achieve" data-id="${esc(goal.id)}">✓ Mark achieved</button><button class="button button-small button-secondary" type="button" data-action="goal-replace" data-id="${esc(goal.id)}">Replace</button><button class="button button-small button-ghost" type="button" data-action="goal-abandon" data-id="${esc(goal.id)}">Abandon</button>` : kind === "planned" ? `<button class="button button-small button-secondary" type="button" data-action="goal-activate" data-id="${esc(goal.id)}">Set as current →</button><button class="button button-small button-ghost" type="button" data-action="goal-abandon" data-id="${esc(goal.id)}">Remove</button>` : "";
  return `<article class="card goal-card ${kind}"><div class="goal-top"><div><h3>${esc(goal.title)}</h3><p>${esc(goal.successCriterion)}</p></div><span class="badge ${kind}">${kind === "history" ? esc(goal.status) : kind.toUpperCase()}</span></div>${metadata.length ? `<div class="goal-meta">${metadata.map(item => `<span>${esc(item)}</span>`).join("")}</div>` : ""}<div class="goal-actions">${actions}${kind !== "history" ? `<button class="button button-small button-ghost" type="button" data-action="edit-goal" data-id="${esc(goal.id)}">Edit details</button>` : ""}</div></article>`;
}

async function skillPage(skillId) {
  const [skill, goals] = await Promise.all([api(`/skills/${urlId(skillId)}`), api(`/skills/${urlId(skillId)}/goals`)]);
  const path = await api(`/paths/${urlId(skill.pathId)}`);
  state.skill = skill; state.goals = goals; state.path = path;
  const current = goals.find(goal => goal.status === "ACTIVE");
  const planned = goals.filter(goal => goal.status === "PLANNED");
  const history = goals.filter(goal => !["ACTIVE", "PLANNED"].includes(goal.status)).sort((a, b) => (b.closedAt || "").localeCompare(a.closedAt || ""));
  const isTrainable = skill.nodeType === "TRAINABLE";
  return `<div class="breadcrumb">${link("#/paths", "My paths")}<span>/</span>${link(`#/paths/${urlId(path.id)}`, path.name)}<span>/</span><strong>${esc(skill.name)}</strong></div><span class="eyebrow"><span class="eyebrow-line"></span> ${isTrainable ? "TRAINABLE SKILL" : "CATEGORY"}</span>
    <div class="page-header"><div><h1>${esc(skill.name)}<em>.</em></h1><p>${esc(skill.description || (isTrainable ? "Give this skill a clear destination." : "An organizing space for the skills below."))}</p></div><div class="header-actions"><button class="button button-ghost" type="button" data-action="edit-skill">Edit skill</button>${isTrainable ? `<button class="button button-primary" type="button" data-action="new-goal" data-status="${current ? "PLANNED" : "ACTIVE"}">＋ ${current ? "Plan next goal" : "Set a goal"}</button>` : `<button class="button button-primary" type="button" data-action="add-child" data-id="${esc(skill.id)}">＋ Add child</button>`}</div></div>
    <div class="card skill-banner"><div><span class="eyebrow-small">${isTrainable ? "YOUR NEXT STEP" : "SKILL BRANCH"}</span><h2>${isTrainable ? current ? "Your destination is set." : "Where do you want to get?" : "Your skills live here."}</h2><p>${isTrainable ? current ? "Keep the goal in sight. Your practice will have a purpose." : "Choose one clear, verifiable outcome to work toward." : "Add categories or trainable skills under this branch to build your tree."}</p></div><span class="info-symbol" aria-hidden="true">✦</span></div>
    ${isTrainable ? `<div class="goal-grid"><section class="goal-section"><div class="section-heading"><div><span class="section-kicker">01 / IN FOCUS</span><h2>Current goal</h2></div></div>${current ? goalCard(current, "current") : empty("◎", "No current goal", "Set an outcome you can recognize when you reach it.", `<button class="button button-primary button-small" type="button" data-action="new-goal" data-status="ACTIVE">Set current goal</button>`)}</section>
    <section class="goal-section"><div class="section-heading"><div><span class="section-kicker">02 / UP NEXT</span><h2>Planned goals <span class="goal-count">${planned.length}</span></h2></div><button class="button-link" type="button" data-action="new-goal" data-status="PLANNED">＋ Add planned</button></div>${planned.length ? `<div class="goal-grid">${planned.map(goal => goalCard(goal, "planned")).join("")}</div>` : empty("◇", "Room to dream ahead", "Add a future goal while you work on the one in front of you.")}</section>
    <section class="goal-section"><div class="section-heading"><div><span class="section-kicker">03 / YOUR STORY</span><h2>Goal history <span class="goal-count">${history.length}</span></h2></div></div>${history.length ? `<div class="goal-grid">${history.map(goal => goalCard(goal, "history")).join("")}</div>` : empty("✧", "A story in the making", "Goals you achieve, replace, or leave behind will appear here.")}</section></div>` : `<div class="two-column"><section class="card panel"><div class="panel-head"><h2>Skills in this branch</h2></div><p class="panel-note">Return to the path to see the complete skill tree and add skills here.</p><a class="button button-secondary" style="margin-top:20px" href="#/paths/${urlId(path.id)}">Open skill tree ↗</a></section></div>`}
    <button class="button-link" style="margin-top:15px" type="button" data-action="archive-skill">Archive this ${isTrainable ? "skill" : "category"}</button>`;
}

function closeModal() { modalRoot.replaceChildren(); document.body.style.overflow = ""; }

function openModal(title, subtitle, content, onSubmit, buttonText = "Save") {
  modalRoot.innerHTML = `<div class="modal-backdrop"><div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><button class="modal-close" type="button" data-close aria-label="Close dialog">×</button><span class="eyebrow-small">FOREWA / EDIT</span><h2 id="modal-title">${esc(title)}</h2><p class="modal-subtitle">${esc(subtitle)}</p><form id="modal-form"><div id="form-error" class="form-error" role="alert" hidden></div>${content}<div class="form-actions"><button class="button button-secondary" type="button" data-close>Cancel</button><button class="button button-primary" type="submit">${esc(buttonText)}</button></div></form></div></div>`;
  document.body.style.overflow = "hidden";
  const form = document.querySelector("#modal-form");
  form.querySelector("input,textarea,select")?.focus();
  form.addEventListener("submit", async event => {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]'); submit.disabled = true;
    document.querySelector("#form-error").hidden = true;
    try {
      await onSubmit(new FormData(form));
      closeModal();
    } catch (error) {
      const errorBox = document.querySelector("#form-error");
      if (errorBox) { errorBox.textContent = error.message; errorBox.hidden = false; }
    } finally {
      if (submit.isConnected) submit.disabled = false;
    }
  });
}

const field = (name, label, { value = "", placeholder = "", required = false, max = null, kind = "text", hint = "", options = [], step = "any" } = {}) => {
  const attrs = `name="${esc(name)}" id="field-${esc(name)}" ${required ? "required" : ""} ${max ? `maxlength="${max}"` : ""}`;
  let control;
  if (kind === "textarea") control = `<textarea ${attrs} placeholder="${esc(placeholder)}" rows="3">${esc(value)}</textarea>`;
  else if (kind === "select") control = `<select ${attrs}>${options.map(([key, text]) => `<option value="${esc(key)}" ${value === key ? "selected" : ""}>${esc(text)}</option>`).join("")}</select>`;
  else control = `<input type="${kind}" ${attrs} value="${esc(value)}" placeholder="${esc(placeholder)}" ${kind === "number" ? `step="${esc(step)}"` : ""}>`;
  return `<label class="field" for="field-${esc(name)}"><span>${esc(label)}</span>${control}${hint ? `<small>${esc(hint)}</small>` : ""}</label>`;
};

function openPathForm(path = null) {
  openModal(path ? "Edit path" : "A new direction", path ? "Update the name and description of this path." : "Give a name to the direction you want to grow in.",
    field("name", "Path name", { value: path?.name, placeholder: "e.g. Calisthenics", required: true, max: 120 }) + field("description", "Description · optional", { value: path?.description, kind: "textarea", placeholder: "What does this path mean to you?" }),
    async form => {
      const payload = { name: form.get("name").trim(), description: form.get("description").trim() || null };
      if (!payload.name) throw new Error("Give this path a name.");
      const result = path ? await api(`/paths/${urlId(path.id)}`, { method: "PATCH", body: payload }) : await api("/paths", { method: "POST", body: payload });
      notify(path ? "Path updated." : "Your new path is ready.");
      if (path) await route(); else location.hash = `#/paths/${urlId(result.id)}`;
    }, path ? "Save changes" : "Create path");
}

function openSkillForm(path, nodes = [], { skill = null, parentId = null } = {}) {
  const candidates = nodes.filter(node => node.nodeType === "CATEGORY" && node.id !== skill?.id);
  const parentOptions = [...(!skill || !skill.parentId ? [["", "No parent · root level"]] : []), ...candidates.map(node => [node.id, node.name])];
  openModal(skill ? "Edit skill" : "Add to your path", skill ? "Shape this skill without losing its place in the tree." : "Use a category to organize, or a trainable skill for a capability you can practice.",
    field("name", "Name", { value: skill?.name, required: true, max: 120, placeholder: "e.g. Pull-ups" }) +
    (!skill ? field("nodeType", "Type", { kind: "select", value: "TRAINABLE", options: [["TRAINABLE", "Trainable skill"], ["CATEGORY", "Category"]] }) : "") +
    field("parentId", "Under", { kind: "select", value: skill?.parentId || parentId || "", options: parentOptions, hint: skill?.parentId ? "This skill can be moved to another category." : "Categories can contain other skills." }) +
    field("description", "Description · optional", { value: skill?.description, kind: "textarea", placeholder: "What does this skill cover?" }) +
    field("sortOrder", "Display order · optional", { kind: "number", step: "1", value: skill?.sortOrder == null ? "" : String(skill.sortOrder), hint: "Leave blank to use the default placement." }),
    async form => {
      const payload = { name: form.get("name").trim(), description: form.get("description").trim() };
      if (!payload.name) throw new Error("Give this skill a name.");
      if (!skill) payload.nodeType = form.get("nodeType");
      if (form.get("parentId")) payload.parentId = form.get("parentId");
      if (form.get("sortOrder") !== "") payload.sortOrder = Number(form.get("sortOrder"));
      const result = skill ? await api(`/skills/${urlId(skill.id)}`, { method: "PATCH", body: payload }) : await api(`/paths/${urlId(path.id)}/skills`, { method: "POST", body: payload });
      notify(skill ? "Skill updated." : "Skill added to your path.");
      if (skill) await route(); else location.hash = `#/skills/${urlId(result.id)}`;
    }, skill ? "Save changes" : "Add skill");
}

function openGoalForm(skill, goal = null, status = "PLANNED") {
  openModal(goal ? "Edit goal" : status === "ACTIVE" ? "Set your destination" : "Plan what's next", goal ? "Make the outcome more precise. Blank optional values keep their existing values." : "A strong goal describes an outcome you can recognize when you reach it.",
    field("title", "Goal title", { value: goal?.title, required: true, max: 160, placeholder: "e.g. 10 strict pull-ups" }) +
    field("successCriterion", "How will you know you've achieved it?", { value: goal?.successCriterion, kind: "textarea", required: true, placeholder: "Perform 10 strict dead-hang pull-ups in one set." }) +
    `<div class="field-grid">${field("baselineValue", "Starting point · optional", { kind: "number", value: goal?.baselineValue ?? "", placeholder: "e.g. 4" })}${field("targetValue", "Target · optional", { kind: "number", value: goal?.targetValue ?? "", placeholder: "e.g. 10" })}</div>` +
    `<div class="field-grid">${field("unit", "Unit · optional", { value: goal?.unit, max: 40, placeholder: "e.g. reps" })}${field("targetDate", "Target date · optional", { kind: "date", value: goal?.targetDate ?? "" })}</div>` +
    (!goal ? field("status", "Start when?", { kind: "select", value: status, options: [["ACTIVE", "Start now · current goal"], ["PLANNED", "Later · planned goal"]] }) : ""),
    async form => {
      const payload = { title: form.get("title").trim(), successCriterion: form.get("successCriterion").trim() };
      if (!payload.title || !payload.successCriterion) throw new Error("Add a title and a clear success criterion.");
      for (const key of ["baselineValue", "targetValue"]) if (form.get(key) !== "") payload[key] = Number(form.get(key));
      for (const key of ["unit", "targetDate"]) if (form.get(key)) payload[key] = form.get(key);
      if (!goal) payload.status = form.get("status");
      await api(goal ? `/goals/${urlId(goal.id)}` : `/skills/${urlId(skill.id)}/goals`, { method: goal ? "PATCH" : "POST", body: payload });
      notify(goal ? "Goal updated." : "Goal added.");
      await route();
    }, goal ? "Save changes" : "Create goal");
}

function openTimezone(firstVisit = false) {
  const detected = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";
  openModal(firstVisit ? "Where are you growing from?" : "Your account", firstVisit ? "Confirm your timezone so your dates and future progress make sense." : "Your account settings and local timezone.",
    `<div class="profile-panel"><div class="profile-row">Signed in as<strong>${esc(state.me.email)}</strong></div></div><div class="divider"></div>` + field("timezone", "Your timezone", { value: state.me.timezone || detected, required: true, hint: "Use an IANA zone, such as Europe/Berlin." }) + `<div class="divider"></div><label class="settings-toggle" for="skill-view-toggle"><span><strong>Graph view</strong><small>Show paths as connected nodes instead of the nested tree.</small></span><input id="skill-view-toggle" type="checkbox" role="switch" ${state.skillView === "graph" ? "checked" : ""}><span class="toggle-track" aria-hidden="true"></span></label><p class="settings-footnote">Saved in this browser for your account. You can switch back anytime.</p><div class="divider"></div><button id="signout-button" type="button" class="button button-ghost" style="width:100%">Sign out</button>`,
    async form => {
      state.me = await api("/me", { method: "PATCH", body: { timezone: form.get("timezone").trim() } });
      notify("Timezone saved."); showApp();
    }, "Save timezone");
  document.querySelector("#signout-button").addEventListener("click", signOut);
  document.querySelector("#skill-view-toggle").addEventListener("change", event => {
    state.skillView = event.target.checked ? "graph" : "tree";
    try { localStorage.setItem(`forewa:skill-view:${state.me.id}`, state.skillView); }
    catch { notify("View preference will reset when you close this browser.", "error"); }
    if (location.hash.startsWith("#/paths/")) route();
  });
}

async function signOut() {
  try {
    if (!state.csrf) state.csrf = await api("/csrf");
    const response = await fetch("/logout", { method: "POST", credentials: "same-origin", headers: { [state.csrf.headerName]: state.csrf.token } });
    if (!response.ok) throw new Error("Could not sign out. Please try again.");
    state.me = null; state.csrf = null; state.paths = []; state.graph?.destroy(); state.graph = null; location.hash = "#/";
    showLogin();
  } catch (error) { notify(error.message, "error"); }
}

async function action(event) {
  const trigger = event.target.closest("[data-action]");
  if (!trigger) return;
  const id = trigger.dataset.id;
  try {
    switch (trigger.dataset.action) {
      case "retry": return boot();
      case "graph-zoom-in": return state.graph?.zoom(1.25);
      case "graph-zoom-out": return state.graph?.zoom(0.8);
      case "graph-fit": return state.graph?.fit();
      case "new-path": return openPathForm();
      case "edit-path": return openPathForm(state.path);
      case "add-skill": return openSkillForm(state.path, state.skills);
      case "add-child": return openSkillForm(state.path, await api(`/paths/${urlId(state.path.id)}/skills`), { parentId: id });
      case "edit-skill": return openSkillForm(state.path, await api(`/paths/${urlId(state.path.id)}/skills`), { skill: state.skill });
      case "new-goal": return openGoalForm(state.skill, null, trigger.dataset.status);
      case "edit-goal": return openGoalForm(state.skill, state.goals.find(goal => goal.id === id));
      case "archive-path": {
        if (!confirm(`Archive “${state.path.name}”? It will leave your active paths list.`)) return;
        await api(`/paths/${urlId(state.path.id)}/archive`, { method: "POST" }); notify("Path archived."); location.hash = "#/paths"; return;
      }
      case "archive-skill": {
        if (!confirm(`Archive “${state.skill.name}”?`)) return;
        await api(`/skills/${urlId(state.skill.id)}/archive`, { method: "POST" }); notify("Skill archived."); location.hash = `#/paths/${urlId(state.path.id)}`; return;
      }
      case "goal-activate": {
        if (state.goals.some(goal => goal.status === "ACTIVE") && !confirm("A current goal is already active. Try activating this planned goal anyway?")) return;
        await api(`/goals/${urlId(id)}/activate`, { method: "POST" }); notify("Your goal is now in focus."); return route();
      }
      case "goal-achieve": case "goal-replace": case "goal-abandon": {
        const operation = trigger.dataset.action.replace("goal-", "");
        if (!confirm(`${operation === "achieve" ? "Mark this goal achieved" : operation === "replace" ? "Close this goal as replaced" : "Abandon this goal"}?`)) return;
        await api(`/goals/${urlId(id)}/${operation}`, { method: "POST" }); notify(operation === "achieve" ? "Goal achieved. Keep going!" : "Goal moved to history."); return route();
      }
    }
  } catch (error) { notify(error.message, "error"); }
}

document.addEventListener("click", action);
document.querySelector("#profile-button").addEventListener("click", () => openTimezone());
document.querySelector("#top-profile").addEventListener("click", () => openTimezone());
modalRoot.addEventListener("click", event => { if (event.target === modalRoot.firstElementChild || event.target.closest("[data-close]")) closeModal(); });
document.addEventListener("keydown", event => { if (event.key === "Escape" && modalRoot.firstElementChild) closeModal(); });
window.addEventListener("hashchange", route);
boot();
