const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

const source = fs.readFileSync(path.resolve(__dirname, "../../main/resources/static/skill-graph.js"), "utf8");

test("graph nodes open skills on click, but dragging does not navigate", () => {
  const listeners = new Map();
  const canvas = {
    style: {},
    getContext: () => new Proxy({}, { get: () => () => {} }),
    getBoundingClientRect: () => ({ left: 0, top: 0, width: 600, height: 420 }),
    addEventListener: (event, handler) => listeners.set(event, handler),
    removeEventListener: event => listeners.delete(event),
    setPointerCapture() {},
    hasPointerCapture: () => false
  };
  const tooltip = { hidden: true, style: {} };
  const container = {
    querySelector: selector => selector === "canvas" ? canvas : tooltip,
    getBoundingClientRect: () => ({ width: 600, height: 420 })
  };
  const environment = {
    window: { devicePixelRatio: 1, matchMedia: () => ({ matches: true }) },
    ResizeObserver: class { observe() {} disconnect() {} },
    requestAnimationFrame: () => 1,
    cancelAnimationFrame() {}
  };
  vm.runInNewContext(`${source}\nthis.createGraph = createSkillGraph`, environment);
  const opened = [];
  const graph = environment.createGraph(container, [
    { id: "one", name: "Spring Boot", nodeType: "TRAINABLE", parentId: null }
  ], id => opened.push(id));

  listeners.get("pointerdown")({ button: 0, pointerId: 1, clientX: 300, clientY: 210 });
  listeners.get("pointerup")({ pointerId: 1 });
  assert.deepEqual(opened, ["one"]);

  listeners.get("pointerdown")({ button: 0, pointerId: 2, clientX: 300, clientY: 210 });
  listeners.get("pointermove")({ pointerId: 2, clientX: 340, clientY: 210 });
  listeners.get("pointerup")({ pointerId: 2 });
  assert.deepEqual(opened, ["one"]);

  listeners.get("pointerdown")({ button: 0, pointerId: 3, clientX: 340, clientY: 210 });
  listeners.get("pointercancel")({ pointerId: 3 });
  assert.deepEqual(opened, ["one"]);

  graph.zoom(1.2);
  graph.fit();
  graph.destroy();
  assert.equal(listeners.size, 0);
});
