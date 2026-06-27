# scramble-bundle — vendored cubing.js visualization bundle

This directory regenerates the prebuilt JavaScript that NanoTimer ships at:

    nanotimer/src/main/assets/scramble/bundle.js

`bundle.js` is the **visualization half of [cubing.js](https://github.com/cubing/cubing.js)** — it draws a 2D net of a scrambled state from a scramble string. NanoTimer generates its own scrambles in Java, so we never bundle `cubing/scramble` (the WASM two-phase solver). The bundle is committed as a vendored artifact (same as the `square1_*.dat` raw resources); the Gradle build stays pure-JVM and does **not** run Node.

This directory also serves as the GPL-3.0 "corresponding source" for that bundled, minified JavaScript.

## Regenerate (only when bumping cubing.js)

```sh
cd tools/scramble-bundle
npm install          # pinned versions, see package.json / package-lock.json
npm run build        # writes ../../nanotimer/src/main/assets/scramble/bundle.js
node verify.mjs      # asserts: no WASM, no dynamic import(), size in budget
```

Then commit the regenerated `bundle.js` together with any `package.json` /
`package-lock.json` version bumps.

## What's pinned

| package           | version | why                                                  |
|-------------------|---------|------------------------------------------------------|
| `cubing`          | 0.63.3  | `<twisty-player puzzle="fto">` — the only FTO drawer |
| `scramble-display`| 0.59.1  | `<scramble-display>` for the 11 WCA puzzles          |
| `esbuild`         | 0.28.1  | one-shot single-file IIFE bundler                    |

## Files

- `entry.mjs` — bundle entry; imports `scramble-display` + `cubing/twisty`.
- `build.mjs` — esbuild config (single file, IIFE, minified, `chrome61` target).
- `verify.mjs` — post-build invariant checks.

## Licenses & attribution

The bundle ships third-party code, so we carry the notices with it:

- **cubing.js** — `MPL-2.0 OR GPL-3.0-or-later`; used under **GPL-3.0**, same as
  this app.
- **scramble-display** — **GPL-3.0-or-later**.
- **three.js** — **MIT** (bundled transitively); MIT requires its copyright +
  permission notice to travel with the binary.
- **esbuild** — MIT, **build-time only** (never shipped), so no distribution
  obligation; noted here for completeness.

Where the notices live:
- `nanotimer/src/main/assets/scramble/THIRD_PARTY_LICENSES.txt` — shipped next to
  `bundle.js`; lists the components and reproduces three.js's full MIT text.
- The app's `LICENSE.txt` (repo root) is the full GPL-3.0 text covering the
  GPL-3.0 components.
- The About dialog (`app_license` string, en/fr/es) credits cubing.js /
  scramble-display / three.js in-app.
- **GPL "corresponding source"** for the minified `bundle.js` = this directory
  (pinned versions + the documented regenerate command).

Note: cubing.js publishes a pre-minified build with dep license banners already
stripped, so `esbuild`'s `legalComments` can't recover three.js's banner from the
input — that's why `THIRD_PARTY_LICENSES.txt` is curated by hand. `verify.mjs`
asserts it exists and mentions three.js/MIT. **When bumping cubing.js, re-check
the bundled dep set and update that file if it changed.**

## Known finding: three.js is included (accepted)

The bundle is ~1 MB raw / ~240 KB gzipped. The bulk is **three.js**, which
`scramble-display` pulls in statically even though we only ever render 2D
(`visualization="2D"`). Importing only `scramble-display` (no `cubing/twisty`)
does **not** drop it — three.js comes from `scramble-display` itself. Removing it
would mean patching cubing.js internals, which is fragile across version bumps and
not worth it for a rarely-updated asset.

This is acceptable because:

- the real delivery cost is the **gzipped** size (~240 KB, within the "few hundred
  KB" budget) — APK assets are stored compressed;
- the WebView is **on-demand** (only created when the user opens the scramble
  diagram), so three.js is never parsed during a solve;
- **no WASM** ships, and there are **no dynamic-import chunks** to resolve at
  runtime (verified by `verify.mjs`), so the single file works offline.

If a future cubing.js release splits 3D behind a lazy import, revisit this.
