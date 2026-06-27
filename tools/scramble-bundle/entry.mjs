// Entry point for the vendored NanoTimer scramble-visualization bundle.
//
// We ship ONLY the visualization half of cubing.js — never `cubing/scramble`
// (the WASM two-phase solver) and never the 3D renderer (three.js). NanoTimer
// generates its own scrambles in Java and only needs "string in, 2D SVG out".
//
//   <scramble-display>  -> WCA puzzles (222 333 444 555 666 777 clock minx
//                          pyram skewb sq1)
//   <twisty-player>     -> FTO (puzzle="fto", not a WCA event id)
//
// Importing these modules registers the custom elements; nothing is exported.
// Forcing visualization="2D" in scramble.html keeps three.js out of the
// runtime path (and, we verify after building, out of the bundle).

import "scramble-display";
import "cubing/twisty";
