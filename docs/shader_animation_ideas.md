# 20 Advanced Shader Animation Ideas for Jetpack Compose

This document provides 20 creative and technical concepts for AGSL shader animations. Each idea includes a conceptual overview, the mathematical "secret sauce," and implementation tips.

---

## Group 1: Natural & Elemental

### 1. Liquid Mercury
*   **Concept:** A highly reflective, viscous metallic surface that flows like water.
*   **Math:** Use high-contrast **Value Noise** combined with a `pow()` function on the final color to simulate sharp spectral highlights (specular).
*   **Implementation:** Use a dark silver base color. Offset the UV coordinates using a low-frequency sine wave to create the "heavy" liquid feel.

### 2. Aurora Borealis
*   **Concept:** Shimmering green and purple curtains of light waving across a night sky.
*   **Math:** Layered **fBm (Fractional Brownian Motion)** where the Y-coordinate is warped by a slow sine wave. Use a vertical gradient to restrict the light to the top half of the screen.
*   **Implementation:** Use additive blending. Combine `sin(uv.x + iTime)` with noise to create the "curtain" folds.

### 3. Molten Lava
*   **Concept:** Slow-moving, glowing red-hot magma with dark cooling crusts.
*   **Math:** Use **Voronoi Noise** to create the "cracked" surface. Use `step()` or `smoothstep()` to separate the "crust" (dark) from the "lava" (glowing).
*   **Implementation:** Animate the Voronoi seeds slowly. Pulse the brightness of the lava using a slow `sin(iTime)`.

### 4. Ink in Water (Diffusion)
*   **Concept:** Silky threads of ink expanding and swirling in a clear liquid.
*   **Math:** **Domain Warping** (noise offsetting noise) with very low persistence. Use `abs(noise)` to create thin, stringy "veins."
*   **Implementation:** Start with a high-scale noise and slowly decrease the scale over time while increasing the "warp" intensity to simulate expansion.

### 5. Electric Lightning/Arc
*   **Concept:** Jagged bolts of electricity jumping between points.
*   **Math:** Use a 1D noise function. The "bolt" is defined by `abs(uv.y - noise(uv.x))`.
*   **Implementation:** To create branches, layer multiple noise functions with different seeds. Use a high-frequency `fract(iTime * speed)` to create the rapid flickering.

---

## Group 2: Sci-Fi & Cyberpunk

### 6. Retro VHS Glitch
*   **Concept:** Digital corruption, scan lines, and color-channel misalignment.
*   **Math:** Randomly offset `uv.x` based on `step(0.98, hash(floor(iTime * speed)))`.
*   **Implementation:** Separately sample R, G, and B channels with slight horizontal offsets. Add a horizontal `sin(uv.y * 100.0)` pattern for scan lines.

### 7. Cyberpunk Grid (Outrun)
*   **Concept:** A glowing neon grid receding into a distant horizon.
*   **Math:** Transform UVs into a perspective projection: `uv.y = 1.0 / uv.y`.
*   **Implementation:** Use `fract()` on the transformed UVs to create repeating lines. Use `smoothstep()` to give the lines a neon "glow" thickness.

### 8. Black Hole (Gravitational Lensing)
*   **Concept:** Light bending and swirling around a dark circular void.
*   **Math:** **Polar Coordinate Distortion**. Warp the radius based on distance from the center: `r = r + strength / r`.
*   **Implementation:** Use a secondary noise texture or procedural starfield as the "background" that gets distorted around the center `(0,0)`.

### 9. Digital Topographic Map
*   **Concept:** Pulsing contour lines representing a 3D terrain.
*   **Math:** `fract(fbm(uv) * levels + iTime)`. 
*   **Implementation:** Use `step()` to make the lines sharp. Color the lines based on the fBm value (higher = whiter, lower = darker blue).

### 10. Neural Network / Data Flow
*   **Concept:** Glowing nodes connected by moving pulses of light.
*   **Math:** **Voronoi edges**. Use the Voronoi "distance to edge" to draw the lines.
*   **Implementation:** Animate the pulse by taking `fract(distance_to_seed - iTime)`. High intensity where the pulse passes through.

---

## Group 3: Abstract & Artistic

### 11. Geometric Kaleidoscope
*   **Concept:** Perfectly symmetrical, rotating geometric patterns.
*   **Math:** **Modular Symmetry**. Fold the UV space using `abs(atan(uv.y, uv.x))` and `mod()`.
*   **Implementation:** Apply a simple shape (like a triangle or star) to the folded space. Rotating the entire UV space creates the kaleidoscope effect.

### 12. Prismatic Rainbow Refraction
*   **Concept:** Light splitting into colors as it passes through glass.
*   **Math:** Calculate color based on the angle of the UV vector. Use `cos()` with phase shifts: `col = 0.5 + 0.5 * cos(angle + vec3(0,2,4))`.
*   **Implementation:** Combine with a "glass" texture (sharp highlights) to make it look like a physical object.

### 13. Oil Spill (Iridescence)
*   **Concept:** Shimmering, multi-colored patterns on a dark surface.
*   **Math:** **Thin-film interference simulation**. Map noise values to a rainbow color ramp using `sin()` or a custom gradient.
*   **Implementation:** Animate the noise slowly and use very low contrast so the colors "bleed" into each other smoothly.

### 14. Frosted Glass (Procedural)
*   **Concept:** Blurring and distorting the "background" with a rough surface feel.
*   **Math:** Add high-frequency, low-amplitude white noise to the UV coordinates before sampling or generating colors.
*   **Implementation:** In AGSL, you can sample an input `RuntimeShader` (as a `uniform shader`) and add jitter to the `fragCoord`.

### 15. Paper Burn / Dissolve
*   **Concept:** An image or color disappearing with a glowing ember edge.
*   **Math:** `noise(uv) - threshold`. Use `threshold` as a uniform controlled by Compose animation.
*   **Implementation:** Pixels where `val < 0` are transparent. Pixels where `0 < val < 0.1` are colored orange/bright yellow (the embers).

---

## Group 4: Cosmic & Space

### 16. Star Birth (Protostar)
*   **Concept:** A swirling vortex of gas collapsing into a bright core.
*   **Math:** **Spiral Warping**. Rotate UVs by an amount proportional to `1.0 / length(uv)`.
*   **Implementation:** Combine with the "Nebula" logic. The center should be a bright white `exp()` glow.

### 17. Pulsar / Neutron Star
*   **Concept:** A rapidly spinning star emitting twin beams of intense light.
*   **Math:** Two narrow cones of light defined by `abs(dot(uv, beam_direction))`.
*   **Implementation:** Rotate the `beam_direction` vector rapidly using `sin(iTime)` and `cos(iTime)`. Use a strong blue/white glow.

### 18. Solar Flare
*   **Concept:** Violent eruptions of fire from the edge of a circle.
*   **Math:** **Polar fBm**. Convert UV to polar, then sample fBm using `(angle, iTime)`.
*   **Implementation:** Use the fBm result to extend the radius of a circle. Color with the "Fire" palette.

---

## Group 5: UI & UX Patterns

### 19. Magnetic Button Glow
*   **Concept:** A glow that follows the user's touch or hovers near edges.
*   **Math:** Use a `uniform float2 iMouse` to pass the touch coordinate. Calculate distance `d = length(uv - iMouse)`.
*   **Implementation:** The glow intensity is `1.0 / (d * d + small_constant)`. Great for button backgrounds.

### 20. Infinite Zoom (Fractal)
*   **Concept:** Constant movement "into" a pattern that never ends.
*   **Math:** **Logarithmic Scaling**. `uv * pow(2.0, fract(iTime))`.
*   **Implementation:** Layer multiple scales of the same pattern and cross-fade them using `fract(iTime)` to hide the "pop" when the loop restarts.

---

## Final Recommendations for Implementation

1.  **Uniform Management:** For the most interactive feel, pass `iMouse` (touch) and `iSize` (component size) as uniforms from Compose.
2.  **Color Constants:** Use hex colors in Compose and pass them as `half4` uniforms to the shader so you can change themes easily without editing the AGSL string.
3.  **Step-by-Step:** Start with the base geometry (coordinates), then add the motion (time), and finally the texture (noise/patterns).
4.  **Debugging:** Use the `FpsCounter` we built to see which of these 20 ideas needs more optimization on your target hardware.
