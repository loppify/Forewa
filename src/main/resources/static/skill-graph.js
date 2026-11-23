"use strict";

// A small, dependency-free force graph for the existing skill hierarchy.
// The graph is a view of API data: dragging a dot never changes its saved parent.
function createSkillGraph(container, skills, openSkill) {
  const canvas = container.querySelector("canvas");
  const tip = container.querySelector(".graph-tooltip");
  const ctx = canvas.getContext("2d");
  if (!ctx) return { zoom() {}, fit() {}, destroy() {} };

  const nodes = skills.map((skill, index) => {
    const angle = index * 2.39996323;
    const radius = Math.sqrt(index) * 77;
    return { ...skill, x: Math.cos(angle) * radius, y: Math.sin(angle) * radius, vx: 0, vy: 0 };
  });
  const byId = new Map(nodes.map(node => [node.id, node]));
  const edges = nodes.filter(node => byId.has(node.parentId)).map(node => [byId.get(node.parentId), node]);
  const camera = { x: 0, y: 0, scale: 1 };
  let width = 1, height = 1, frame = 0, framesLeft = 160, hovered = null, pointer = null, alive = true;
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  function screen(node) {
    return { x: node.x * camera.scale + camera.x, y: node.y * camera.scale + camera.y };
  }

  function pointerPosition(event) {
    const bounds = canvas.getBoundingClientRect();
    return { x: event.clientX - bounds.left, y: event.clientY - bounds.top };
  }

  function findNode(point) {
    for (let i = nodes.length - 1; i >= 0; i--) {
      const position = screen(nodes[i]);
      if (Math.hypot(point.x - position.x, point.y - position.y) < Math.max(19, 15 * camera.scale)) return nodes[i];
    }
    return null;
  }

  function resize() {
    if (!alive) return;
    const bounds = container.getBoundingClientRect();
    const oldWidth = width, oldHeight = height;
    width = Math.max(1, bounds.width);
    height = Math.max(1, bounds.height);
    const pixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.round(width * pixelRatio);
    canvas.height = Math.round(height * pixelRatio);
    ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    camera.x += (width - oldWidth) / 2;
    camera.y += (height - oldHeight) / 2;
    draw();
  }

  function fit() {
    if (!nodes.length) return;
    const xs = nodes.map(node => node.x), ys = nodes.map(node => node.y);
    const minX = Math.min(...xs), maxX = Math.max(...xs);
    const minY = Math.min(...ys), maxY = Math.max(...ys);
    camera.scale = Math.min(1.35, width / (maxX - minX + 220), height / (maxY - minY + 160));
    camera.x = width / 2 - (minX + maxX) / 2 * camera.scale;
    camera.y = height / 2 - (minY + maxY) / 2 * camera.scale;
    draw();
  }

  function zoom(factor, point = { x: width / 2, y: height / 2 }) {
    const next = Math.min(3, Math.max(.28, camera.scale * factor));
    const ratio = next / camera.scale;
    camera.x = point.x - (point.x - camera.x) * ratio;
    camera.y = point.y - (point.y - camera.y) * ratio;
    camera.scale = next;
    draw();
  }

  function simulate() {
    if (!nodes.length) return;
    for (let i = 0; i < nodes.length; i++) {
      const a = nodes[i];
      for (let j = i + 1; j < nodes.length; j++) {
        const b = nodes[j];
        const dx = a.x - b.x || .1, dy = a.y - b.y || .1;
        const distanceSquared = Math.max(300, dx * dx + dy * dy);
        const force = Math.min(.8, 1500 / distanceSquared) / Math.sqrt(distanceSquared);
        a.vx += dx * force; a.vy += dy * force;
        b.vx -= dx * force; b.vy -= dy * force;
      }
    }
    for (const [parent, child] of edges) {
      const dx = child.x - parent.x, dy = child.y - parent.y;
      const distance = Math.max(1, Math.hypot(dx, dy));
      const strength = (distance - 125) * .007;
      parent.vx += dx / distance * strength; parent.vy += dy / distance * strength;
      child.vx -= dx / distance * strength; child.vy -= dy / distance * strength;
    }
    for (const node of nodes) {
      node.vx = (node.vx - node.x * .0004) * .78;
      node.vy = (node.vy - node.y * .0004) * .78;
      if (pointer?.node === node) { node.vx = 0; node.vy = 0; continue; }
      node.x += Math.max(-6, Math.min(6, node.vx));
      node.y += Math.max(-6, Math.min(6, node.vy));
    }
  }

  function draw() {
    if (!alive) return;
    ctx.clearRect(0, 0, width, height);
    ctx.save();
    ctx.translate(camera.x, camera.y);
    ctx.scale(camera.scale, camera.scale);
    ctx.lineWidth = 1.2 / camera.scale;
    for (const [parent, child] of edges) {
      ctx.beginPath(); ctx.moveTo(parent.x, parent.y); ctx.lineTo(child.x, child.y);
      ctx.strokeStyle = "rgba(183,167,236,.4)"; ctx.stroke();
    }
    for (const node of nodes) {
      const category = node.nodeType === "CATEGORY";
      const selected = node === hovered;
      const color = category ? "#b7a7ec" : "#91d9d0";
      const radius = category ? 9 : 6;
      ctx.beginPath(); ctx.arc(node.x, node.y, radius + (selected ? 8 : 5), 0, Math.PI * 2);
      ctx.fillStyle = category ? "rgba(183,167,236,.13)" : "rgba(145,217,208,.13)"; ctx.fill();
      ctx.beginPath(); ctx.arc(node.x, node.y, radius, 0, Math.PI * 2);
      ctx.fillStyle = color; ctx.fill();
      if (category || selected) { ctx.strokeStyle = "rgba(255,255,255,.65)"; ctx.lineWidth = 1 / camera.scale; ctx.stroke(); }
      if (camera.scale >= .55 || selected) {
        ctx.font = `${category ? 600 : 500} 12px system-ui, sans-serif`;
        ctx.textAlign = "left"; ctx.textBaseline = "middle";
        ctx.fillStyle = selected ? "#fff" : "#d7d9e4";
        const label = node.name.length > 24 && !selected ? `${node.name.slice(0, 22)}…` : node.name;
        ctx.fillText(label, node.x + radius + 11, node.y);
      }
    }
    ctx.restore();
  }

  function animate() {
    frame = 0;
    if (!alive) return;
    if (framesLeft > 0 && !reducedMotion) {
      simulate(); framesLeft--; draw();
      frame = requestAnimationFrame(animate);
    } else draw();
  }

  function wake() {
    framesLeft = Math.max(framesLeft, 36);
    if (!frame && !reducedMotion) frame = requestAnimationFrame(animate);
    else draw();
  }

  function tooltip(node, point) {
    tip.hidden = !node;
    if (!node) return;
    tip.textContent = `${node.name} · ${node.nodeType === "CATEGORY" ? "category" : "trainable skill"}`;
    tip.style.left = `${Math.min(point.x + 16, width - Math.min(220, width - 16))}px`;
    tip.style.top = `${Math.min(point.y + 17, height - 42)}px`;
  }

  function pointerDown(event) {
    if (event.button !== 0) return;
    const point = pointerPosition(event);
    pointer = { id: event.pointerId, node: findNode(point), start: point, point, moved: false };
    canvas.setPointerCapture(event.pointerId);
    canvas.style.cursor = pointer.node ? "grabbing" : "grab";
  }

  function pointerMove(event) {
    const point = pointerPosition(event);
    if (pointer && pointer.id === event.pointerId) {
      const dx = point.x - pointer.point.x, dy = point.y - pointer.point.y;
      if (Math.hypot(point.x - pointer.start.x, point.y - pointer.start.y) > 4) pointer.moved = true;
      if (pointer.node) {
        pointer.node.x += dx / camera.scale;
        pointer.node.y += dy / camera.scale;
        wake();
      } else {
        camera.x += dx;
        camera.y += dy;
        draw();
      }
      pointer.point = point;
    }
    hovered = pointer?.node || findNode(point);
    canvas.style.cursor = pointer ? "grabbing" : hovered ? "pointer" : "grab";
    tooltip(hovered, point);
    draw();
  }

  function pointerUp(event) {
    if (!pointer || pointer.id !== event.pointerId) return;
    const selected = pointer.node && !pointer.moved ? pointer.node.id : null;
    pointer = null;
    if (canvas.hasPointerCapture(event.pointerId)) canvas.releasePointerCapture(event.pointerId);
    canvas.style.cursor = hovered ? "pointer" : "grab";
    if (selected) openSkill(selected);
  }

  function pointerCancel(event) {
    if (!pointer || pointer.id !== event.pointerId) return;
    pointer = null;
    hovered = null;
    tooltip(null);
    canvas.style.cursor = "grab";
    draw();
  }

  function pointerLeave() {
    if (pointer) return;
    hovered = null; tooltip(null); draw();
  }

  function wheel(event) {
    event.preventDefault();
    zoom(event.deltaY < 0 ? 1.12 : .89, pointerPosition(event));
  }

  const observer = new ResizeObserver(resize);
  observer.observe(container);
  canvas.addEventListener("pointerdown", pointerDown);
  canvas.addEventListener("pointermove", pointerMove);
  canvas.addEventListener("pointerup", pointerUp);
  canvas.addEventListener("pointercancel", pointerCancel);
  canvas.addEventListener("pointerleave", pointerLeave);
  canvas.addEventListener("wheel", wheel, { passive: false });
  resize(); fit();
  if (nodes.length > 1) frame = requestAnimationFrame(animate);

  return {
    zoom, fit,
    destroy() {
      alive = false;
      cancelAnimationFrame(frame);
      observer.disconnect();
      canvas.removeEventListener("pointerdown", pointerDown);
      canvas.removeEventListener("pointermove", pointerMove);
      canvas.removeEventListener("pointerup", pointerUp);
      canvas.removeEventListener("pointercancel", pointerCancel);
      canvas.removeEventListener("pointerleave", pointerLeave);
      canvas.removeEventListener("wheel", wheel);
    }
  };
}
