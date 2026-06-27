// Sanity-checks the vendored bundle after `npm run build`.
//
//     npm run build && node verify.mjs
//
// Asserts the invariants the visualization relies on:
//   - the bundle exists and is a single self-contained file;
//   - NO WebAssembly (the cubing/scramble two-phase solver must never ship);
//   - NO runtime dynamic import() (offline WebView can't fetch chunks).
//
// NOTE: three.js IS currently bundled — scramble-display pulls it in even on the
// 2D path (see README). That's a known, accepted finding, not a failure here, so
// we only WARN on it and report the size.

import { readFileSync, statSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import { gzipSync } from "node:zlib";

const here = dirname(fileURLToPath(import.meta.url));
const bundlePath = resolve(
  here,
  "../../nanotimer/src/main/assets/scramble/bundle.js"
);

let failed = false;
const fail = (m) => { console.error("FAIL: " + m); failed = true; };
const ok = (m) => console.log("ok:   " + m);
const warn = (m) => console.warn("warn: " + m);

let src;
try {
  src = readFileSync(bundlePath, "utf8");
} catch {
  fail("bundle not found at " + bundlePath + " — run `npm run build` first");
  process.exit(1);
}

const bytes = statSync(bundlePath).size;
const gz = gzipSync(src).length;
const kb = (n) => Math.round(n / 1024) + " KB";
console.log(`bundle: ${kb(bytes)} raw / ${kb(gz)} gzipped`);

// Hard requirements.
if (/wasm|WebAssembly/i.test(src)) fail("bundle references WebAssembly");
else ok("no WebAssembly");

if (/\bimport\s*\(/.test(src)) fail("bundle contains runtime dynamic import()");
else ok("no dynamic import() chunks");

// Soft / informational.
const threeHits = (src.match(/THREE\./g) || []).length;
if (threeHits > 0) warn(`three.js present (${threeHits} THREE. refs) — accepted, see README`);
else ok("no three.js");

// Single-file gut check: gzipped size should fit the "few hundred KB" budget.
if (gz > 400 * 1024) warn(`gzipped size ${kb(gz)} exceeds the ~400 KB budget`);
else ok(`gzipped size within budget (${kb(gz)})`);

process.exit(failed ? 1 : 0);
