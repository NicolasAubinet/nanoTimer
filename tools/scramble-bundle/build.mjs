// Builds the single self-contained, 2D-only visualization bundle that NanoTimer
// vendors at nanotimer/src/main/assets/scramble/bundle.js.
//
// Run once locally after `npm install` when bumping cubing.js:
//     npm run build
//
// Goals (verified by verify.mjs afterwards):
//   - one file, no code-split chunks to resolve at runtime (offline WebView);
//   - no WASM (the cubing/scramble two-phase solver must never be pulled in);
//   - keep three.js / 3D out of the bundle (we only ever render 2D).
//
// This is intentionally NOT wired into Gradle — the build stays pure-JVM.

import { build } from "esbuild";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";

const here = dirname(fileURLToPath(import.meta.url));
const outFile = resolve(
  here,
  "../../nanotimer/src/main/assets/scramble/bundle.js"
);

await build({
  entryPoints: [resolve(here, "entry.mjs")],
  bundle: true,
  splitting: false,
  format: "iife",
  minify: true,
  // Single offline file; no dynamic chunk loading from the WebView.
  target: ["chrome61"], // matches our minSdk-21 WebView floor (ES modules >= 61)
  // Collect bundled deps' license/copyright banners into bundle.js.LEGAL.txt
  // (shipped alongside bundle.js) instead of stripping them. Required for the
  // MIT/BSD deps (e.g. three.js) whose notices must travel with the binary.
  legalComments: "linked",
  outfile: outFile,
});

console.log(`Wrote ${outFile}`);
