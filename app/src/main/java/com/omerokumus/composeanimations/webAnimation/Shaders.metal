//
//  Shaders.metal
//  webAnimation
//
//  Created by Youssef on 2026-03-26.
//

#include <metal_stdlib>
using namespace metal;

// MARK: - Shared Helpers

float hashVal(float2 p) {
    return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
}

float2 hash2(float2 p) {
    return fract(sin(float2(dot(p, float2(127.1, 311.7)),
                            dot(p, float2(269.5, 183.3)))) * 43758.5453);
}

float valueNoise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hashVal(i);
    float b = hashVal(i + float2(1.0, 0.0));
    float c = hashVal(i + float2(0.0, 1.0));
    float d = hashVal(i + float2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 6; i++) {
        v += a * valueNoise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

float3 hsv2rgb(float3 c) {
    float3 p = abs(fract(float3(c.x) + float3(0.0, 2.0/3.0, 1.0/3.0)) * 6.0 - 3.0);
    return c.z * mix(float3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

// MARK: - Original Shader

[[ stitchable ]] half4 shaderAnimation(float2 position, half4 color, float2 size, float time) {
    float2 resolution = size;
    float2 uv = (position * 2.0 - resolution) / min(resolution.x, resolution.y);
    float t = time * 0.05;
    float lineWidth = 0.002;

    float3 col = float3(0.0);
    for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 5; i++) {
            col[j] += lineWidth * float(i * i) / abs(fract(t - 0.01 * float(j) + float(i) * 0.01) * 5.0 - length(uv) + fmod(uv.x + uv.y, 0.2));
        }
    }

    return half4(half3(col), 1.0);
}

// MARK: - 1. Plasma Waves

[[ stitchable ]] half4 plasmaWaves(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.8;

    float v1 = sin(uv.x * 10.0 + t);
    float v2 = sin(uv.y * 10.0 + t * 0.7);
    float v3 = sin((uv.x + uv.y) * 10.0 + t * 1.3);
    float v4 = sin(length(uv) * 10.0 - t * 2.0);
    float value = (v1 + v2 + v3 + v4) / 4.0;

    float r = sin(value * M_PI_F + 0.0) * 0.5 + 0.5;
    float g = sin(value * M_PI_F + 2.094) * 0.5 + 0.5;
    float b = sin(value * M_PI_F + 4.189) * 0.5 + 0.5;

    return half4(half3(r, g, b), 1.0);
}

// MARK: - 2. Starfield

[[ stitchable ]] half4 starfield(float2 position, half4 color, float2 size, float time) {
    float2 uv = position / size;
    float3 col = float3(0.0);

    for (int layer = 0; layer < 3; layer++) {
        float scale = 50.0 + float(layer) * 80.0;
        float speed = 0.03 + float(layer) * 0.02;
        float brightness = 1.0 - float(layer) * 0.25;

        float2 st = uv * scale;
        st.y += time * speed * scale;
        float2 cell = floor(st);
        float2 f = fract(st);

        float h = hashVal(cell);
        if (h > 0.95) {
            float2 center = hash2(cell);
            float d = length(f - center);
            float twinkle = sin(time * 3.0 + h * 100.0) * 0.3 + 0.7;
            float star = smoothstep(0.1, 0.0, d) * twinkle * brightness;
            col += float3(star);
        }
    }

    col += float3(0.01, 0.01, 0.03);
    return half4(half3(col), 1.0);
}

// MARK: - 3. Water Ripples

[[ stitchable ]] half4 waterRipples(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.6;

    float2 e1 = float2(sin(t * 0.7) * 0.5, cos(t * 0.5) * 0.5);
    float2 e2 = float2(cos(t * 0.4) * 0.8, sin(t * 0.6) * 0.3);
    float2 e3 = float2(sin(t * 0.3) * 0.3, cos(t * 0.8) * 0.7);

    float wave = 0.0;
    float d1 = length(uv - e1);
    float d2 = length(uv - e2);
    float d3 = length(uv - e3);
    wave += sin(d1 * 20.0 - time * 4.0) / (1.0 + d1 * 5.0);
    wave += sin(d2 * 20.0 - time * 3.5) / (1.0 + d2 * 5.0);
    wave += sin(d3 * 20.0 - time * 4.5) / (1.0 + d3 * 5.0);

    float3 baseColor = float3(0.0, 0.1, 0.3);
    float3 col = baseColor + wave * float3(0.1, 0.3, 0.2);
    col += pow(max(wave, 0.0), 8.0) * float3(0.8, 0.9, 1.0);

    return half4(half3(col), 1.0);
}

// MARK: - 4. Fire

[[ stitchable ]] half4 fireShader(float2 position, half4 color, float2 size, float time) {
    float2 uv = position / size;
    float2 nUV = uv;
    nUV.y = 1.0 - nUV.y;

    float2 p = nUV * float2(4.0, 4.0);
    p.y -= time * 3.0;

    float n = 0.0;
    float amp = 0.5;
    float2 q = p;
    for (int i = 0; i < 5; i++) {
        n += amp * valueNoise(q);
        q *= 2.0;
        amp *= 0.5;
    }

    float mask = pow(1.0 - nUV.y, 1.5);
    float fire = n * mask * 2.0;
    fire = clamp(fire, 0.0, 1.0);

    float3 col;
    if (fire < 0.33) {
        col = mix(float3(0.0), float3(0.8, 0.1, 0.0), fire / 0.33);
    } else if (fire < 0.66) {
        col = mix(float3(0.8, 0.1, 0.0), float3(1.0, 0.5, 0.0), (fire - 0.33) / 0.33);
    } else {
        col = mix(float3(1.0, 0.5, 0.0), float3(1.0, 1.0, 0.3), (fire - 0.66) / 0.34);
    }

    return half4(half3(col), 1.0);
}

// MARK: - 5. Ocean Waves

[[ stitchable ]] half4 oceanWaves(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.5;

    float wave = 0.0;
    float amp = 0.4;
    float freq = 2.0;
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        wave += amp * sin(uv.x * freq + t * (1.0 + fi * 0.2) + fi * 0.8);
        wave += amp * 0.5 * sin(uv.x * freq * 1.3 - t * 0.7 + fi * 1.2);
        freq *= 1.6;
        amp *= 0.55;
    }

    float surface = smoothstep(0.02, 0.0, abs(uv.y - wave * 0.3));
    float underWater = smoothstep(wave * 0.3, wave * 0.3 - 1.5, uv.y);

    float3 skyColor = mix(float3(0.1, 0.1, 0.2), float3(0.02, 0.02, 0.08), uv.y * 0.5 + 0.5);
    float3 deepColor = float3(0.0, 0.05, 0.15);
    float3 shallowColor = float3(0.0, 0.2, 0.4);
    float3 waterColor = mix(shallowColor, deepColor, clamp(-uv.y + wave * 0.3, 0.0, 1.0));

    float3 col = mix(skyColor, waterColor, underWater);
    col += float3(0.5, 0.8, 0.9) * surface;

    float foam = valueNoise(float2(uv.x * 8.0 + t * 2.0, wave * 10.0)) * surface * 2.0;
    col += float3(foam) * 0.5;

    float caustics = valueNoise(float2(uv.x * 6.0 + t, uv.y * 6.0 + t * 0.5));
    col += float3(0.0, 0.1, 0.15) * caustics * underWater * 0.4;

    return half4(half3(col), 1.0);
}

// MARK: - 6. Matrix Rain

[[ stitchable ]] half4 matrixRain(float2 position, half4 color, float2 size, float time) {
    float2 uv = position / size;
    float columns = 40.0;
    float2 grid = float2(columns, columns * size.y / size.x);

    float2 cell = floor(uv * grid);
    float2 f = fract(uv * grid);

    float columnSpeed = 2.0 + hashVal(float2(cell.x, 0.0)) * 4.0;
    float columnOffset = hashVal(float2(cell.x, 1.0)) * 100.0;

    float rain = fract(-time * columnSpeed * 0.1 + cell.y * 0.05 + columnOffset);
    float trail = pow(rain, 3.0);

    float charFlicker = step(0.3, hashVal(cell + floor(time * 8.0)));
    float brightness = trail * charFlicker;

    float head = step(0.95, rain);
    float3 col = float3(0.0, brightness * 0.8, brightness * 0.2);
    col += float3(head * 0.8, head, head * 0.8);

    float charShape = smoothstep(0.1, 0.2, f.x) * smoothstep(0.9, 0.8, f.x)
                    * smoothstep(0.05, 0.15, f.y) * smoothstep(0.95, 0.85, f.y);
    col *= charShape;

    return half4(half3(col), 1.0);
}

// MARK: - 7. Neon Pulse

[[ stitchable ]] half4 neonPulse(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.6;
    float r = length(uv);
    float a = atan2(uv.y, uv.x);

    float3 col = float3(0.02, 0.01, 0.04);

    // Expanding neon rings
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        float ringTime = fract(t * 0.25 + fi / 6.0);
        float ringR = ringTime * 2.0;
        float fade = (1.0 - ringTime);
        fade *= fade;

        float dist = abs(r - ringR);
        float core = smoothstep(0.015, 0.002, dist) * fade;
        float glow = 0.006 / (dist + 0.006) * fade;

        // Each ring cycles through neon colors
        float hueShift = fi / 6.0 + t * 0.1;
        float3 neon = hsv2rgb(float3(fract(hueShift), 0.9, 1.0));
        float3 white = float3(1.0);

        col += mix(neon, white, core * 0.6) * (core + glow * 0.4);
    }

    // Central orb glow
    float orb = exp(-r * 5.0);
    float orbPulse = 0.7 + 0.3 * sin(t * 3.0);
    float3 orbColor = hsv2rgb(float3(fract(t * 0.08), 0.7, 1.0));
    col += orbColor * orb * orbPulse * 0.6;

    // Rotating neon rays
    for (int j = 0; j < 4; j++) {
        float fj = float(j);
        float rayAngle = a + t * (0.5 + fj * 0.15) + fj * 1.57;
        float ray = pow(max(cos(rayAngle * 3.0), 0.0), 20.0);
        ray *= exp(-r * 2.5);
        float3 rayCol = hsv2rgb(float3(fract(fj * 0.25 + t * 0.05), 0.8, 1.0));
        col += rayCol * ray * 0.25;
    }

    // Floating neon particles
    for (int k = 0; k < 8; k++) {
        float fk = float(k);
        float pAngle = fk * 0.785 + t * (0.3 + fk * 0.05);
        float pR = 0.4 + sin(t * 0.7 + fk * 1.2) * 0.3;
        float2 pPos = float2(cos(pAngle), sin(pAngle)) * pR;
        float pDist = length(uv - pPos);
        float particle = 0.003 / (pDist * pDist + 0.003);
        float3 pCol = hsv2rgb(float3(fract(fk / 8.0 + t * 0.12), 0.9, 1.0));
        col += pCol * particle * 0.15;
    }

    return half4(half3(col), 1.0);
}

// MARK: - 8. Tunnel

[[ stitchable ]] half4 tunnel(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float r = length(uv);
    float a = atan2(uv.y, uv.x);

    float tunnelU = 1.0 / (r + 0.001);
    float tunnelV = a / M_PI_F;

    tunnelU += time * 2.0;

    float pattern = fmod(floor(tunnelU * 4.0) + floor(tunnelV * 8.0), 2.0);
    float3 col = mix(float3(0.05, 0.0, 0.1), float3(0.2, 0.1, 0.4), pattern);

    float glow = 1.0 / (r * 8.0 + 1.0);
    col += float3(0.2, 0.5, 1.0) * glow;

    float fade = smoothstep(0.0, 0.3, r);
    col *= fade;

    return half4(half3(col), 1.0);
}

// MARK: - 9. Fractal Clouds

[[ stitchable ]] half4 fractalClouds(float2 position, half4 color, float2 size, float time) {
    float2 uv = position / size;
    uv *= 3.0;
    uv += float2(time * 0.08, time * 0.04);

    float f1 = fbm(uv);
    float f2 = fbm(uv + f1 * 2.0 + float2(time * 0.02, time * 0.03));

    float3 sky = float3(0.1, 0.15, 0.35);
    float3 cloud = float3(0.9, 0.9, 1.0);
    float3 col = mix(sky, cloud, clamp(f2, 0.0, 1.0));

    col += float3(0.1, 0.05, 0.0) * f1 * 0.5;

    return half4(half3(col), 1.0);
}

// MARK: - 10. Nebula

[[ stitchable ]] half4 nebula(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.15;

    // Domain-warped fBm for organic nebula shapes
    float2 p = uv * 1.5;
    float n1 = fbm(p + float2(t, t * 0.7));
    float n2 = fbm(p + n1 * 1.5 + float2(t * 0.3, -t * 0.2));
    float n3 = fbm(p + n2 * 1.5 + float2(-t * 0.2, t * 0.4));

    // Color layers: purple, blue, pink nebula gas
    float3 purple = float3(0.3, 0.1, 0.5) * smoothstep(0.2, 0.8, n1);
    float3 blue = float3(0.1, 0.2, 0.6) * smoothstep(0.3, 0.9, n2);
    float3 pink = float3(0.6, 0.15, 0.4) * smoothstep(0.3, 0.85, n3);
    float3 orange = float3(0.5, 0.2, 0.05) * smoothstep(0.5, 0.95, n1 * n2);

    float3 col = float3(0.02, 0.01, 0.04);
    col += purple + blue + pink + orange;

    // Bright core glow
    float core = exp(-length(uv + float2(sin(t), cos(t * 0.7)) * 0.3) * 2.5);
    col += float3(0.4, 0.2, 0.5) * core;

    // Stars
    float stars = hashVal(floor(uv * 200.0 + 0.5));
    float starBright = step(0.97, stars);
    float twinkle = sin(time * 2.0 + stars * 100.0) * 0.3 + 0.7;
    col += float3(0.9, 0.9, 1.0) * starBright * twinkle;

    // Dim stars
    float dimStars = hashVal(floor(uv * 80.0 + 10.0));
    col += float3(0.5, 0.5, 0.7) * step(0.96, dimStars) * 0.3;

    return half4(half3(col), 1.0);
}

// MARK: - 11. Shader Hero — cloud particles with warm glow (ported from GLSL)

float fbmHero(float2 p) {
    float t2 = 0.0;
    float a2 = 1.0;
    // Rotation matrix
    float2x2 m = float2x2(1.0, -0.5, 0.2, 1.2);
    for (int i = 0; i < 5; i++) {
        t2 += a2 * valueNoise(p);
        p = m * p * 2.0;
        a2 *= 0.5;
    }
    return t2;
}

float heroCloudsFn(float2 p) {
    float d = 1.0;
    float t2 = 0.0;
    for (float i = 0.0; i < 3.0; i += 1.0) {
        float a = d * fbmHero(float2(i * 10.0 + p.x * 0.2 + 0.2 * (1.0 + i) * p.y + d + i * i + p));
        t2 = mix(t2, d, a);
        d = a;
        p *= 2.0 / (i + 1.0);
    }
    return t2;
}

[[ stitchable ]] half4 shaderHero(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position - 0.5 * size) / min(size.x, size.y);
    float2 st = uv * float2(2.0, 1.0);

    float3 col = float3(0.0);
    float bg = heroCloudsFn(float2(st.x + time * 0.5, -st.y));

    uv *= 1.0 - 0.3 * (sin(time * 0.2) * 0.5 + 0.5);

    for (float i = 1.0; i < 12.0; i += 1.0) {
        uv += 0.1 * cos(i * float2(0.1 + 0.01 * i, 0.8) + i * i + time * 0.5 + 0.1 * uv.x);
        float2 p = uv;
        float d = length(p);
        col += 0.00125 / d * (cos(sin(i) * float3(1.0, 2.0, 3.0)) + 1.0);
        float b = valueNoise(float2(i + p.x + bg * 1.731, i + p.y + bg * 1.731));
        col += 0.002 * b / length(max(p, float2(b * p.x * 0.02, p.y)));
        col = mix(col, float3(bg * 0.25, bg * 0.137, bg * 0.05), d);
    }

    return half4(half3(col), 1.0);
}

// MARK: - 12. Anomalous Matter — wireframe sphere with noise displacement and glow

float hash3to1(float3 p) {
    return fract(sin(dot(p, float3(127.1, 311.7, 74.7))) * 43758.5453);
}

float snoise3D(float3 v) {
    float3 i = floor(v + (v.x + v.y + v.z) / 3.0);
    float3 x0 = v - i + (i.x + i.y + i.z) / 6.0;
    float3 g = step(x0.yzx, x0.xyz);
    float3 l = 1.0 - g;
    float3 i1 = min(g, l.zxy);
    float3 i2 = max(g, l.zxy);
    float3 x1 = x0 - i1 + 1.0 / 6.0;
    float3 x2 = x0 - i2 + 1.0 / 3.0;
    float3 x3 = x0 - 0.5;
    float4 m = max(0.6 - float4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m * m * m;
    float4 px = float4(
        hash3to1(i),
        hash3to1(i + i1),
        hash3to1(i + i2),
        hash3to1(i + 1.0)
    );
    return dot(m, px) * 4.0 - 1.0;
}

[[ stitchable ]] half4 anomalousMatter(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.25;

    // Camera
    float3 ro = float3(0.0, 0.0, 3.2);
    float3 rd = normalize(float3(uv, -1.5));

    // Base sphere radius
    float sphereR = 1.3;

    // Multi-layer noise displacement function for the organic blob shape
    // We'll trace against the displaced sphere using raymarching
    float3 col = float3(0.0);

    // Raymarch the displaced sphere
    float tRay = 0.0;
    bool hit = false;
    float3 hitPos;
    float3 hitNormal;

    for (int i = 0; i < 64; i++) {
        float3 p = ro + rd * tRay;

        float3 rp = p;

        float baseR = length(rp);
        float3 dir = normalize(rp);

        // Multi-octave noise displacement
        float disp = snoise3D(dir * 2.5 + float3(t * 0.4, t * 0.25, t * 0.55)) * 0.25;
        disp += snoise3D(dir * 5.0 + float3(-t * 0.3, t * 0.5, t * 0.2)) * 0.12;
        disp += snoise3D(dir * 10.0 + float3(t * 0.2, -t * 0.35, t * 0.45)) * 0.06;

        float surfaceR = sphereR + disp;
        float d = baseR - surfaceR;

        if (d < 0.005) {
            hit = true;
            hitPos = rp;
            hitNormal = normalize(rp);

            // Compute displaced normal via central differences
            float eps = 0.02;
            for (int axis = 0; axis < 3; axis++) {
                float3 offset = float3(0.0);
                if (axis == 0) offset.x = eps;
                else if (axis == 1) offset.y = eps;
                else offset.z = eps;

                float3 pp = rp + offset;
                float3 pm = rp - offset;
                float3 dp = normalize(pp);
                float3 dm = normalize(pm);
                float sp = sphereR + snoise3D(dp * 2.5 + float3(t * 0.4, t * 0.25, t * 0.55)) * 0.25
                          + snoise3D(dp * 5.0 + float3(-t * 0.3, t * 0.5, t * 0.2)) * 0.12
                          + snoise3D(dp * 10.0 + float3(t * 0.2, -t * 0.35, t * 0.45)) * 0.06;
                float sm = sphereR + snoise3D(dm * 2.5 + float3(t * 0.4, t * 0.25, t * 0.55)) * 0.25
                          + snoise3D(dm * 5.0 + float3(-t * 0.3, t * 0.5, t * 0.2)) * 0.12
                          + snoise3D(dm * 10.0 + float3(t * 0.2, -t * 0.35, t * 0.45)) * 0.06;
                float fp = length(pp) - sp;
                float fm = length(pm) - sm;
                if (axis == 0) hitNormal.x = fp - fm;
                else if (axis == 1) hitNormal.y = fp - fm;
                else hitNormal.z = fp - fm;
            }
            hitNormal = normalize(hitNormal);
            break;
        }

        tRay += max(d * 0.5, 0.01);
        if (tRay > 6.0) break;
    }

    if (hit) {
        float3 n = hitNormal;
        float3 dir = normalize(hitPos);

        // Noise-warped UV mapping for organic wireframe
        float noiseWarp = snoise3D(dir * 3.0 + float3(t * 0.3)) * 0.4;
        float theta = atan2(dir.z, dir.x) + noiseWarp;
        float phi = acos(clamp(dir.y, -1.0, 1.0)) + noiseWarp * 0.6;

        // Dense wireframe grid (high subdivision like icosahedron @64)
        float gridFreq = 24.0;
        float lineTheta = abs(fract(theta * gridFreq / 6.2832) - 0.5) * 2.0;
        float linePhi = abs(fract(phi * gridFreq / 3.14159) - 0.5) * 2.0;

        // Diagonal cross-lines for triangulated look
        float lineDiag = abs(fract((theta + phi) * gridFreq * 0.5 / 3.14159) - 0.5) * 2.0;

        float wire = min(min(lineTheta, linePhi), lineDiag);
        float wireEdge = 1.0 - smoothstep(0.03, 0.10, wire);

        // Fresnel for edge glow
        float fresnel = 1.0 - max(dot(n, normalize(ro - hitPos)), 0.0);
        fresnel = pow(fresnel, 2.0);

        // Lighting — soft directional
        float3 lightDir = normalize(float3(0.5, 1.0, 2.0));
        float diffuse = max(dot(n, lightDir), 0.0) * 0.5 + 0.5;

        // White/silver wireframe palette
        float3 wireColor = float3(0.75, 0.78, 0.82);
        float3 edgeGlow = float3(0.5, 0.55, 0.6);

        // Compose
        float3 surfaceCol = wireColor * wireEdge * diffuse;
        surfaceCol += edgeGlow * fresnel * 0.6;

        // Subtle inner glow through wireframe gaps
        float innerGlow = (1.0 - wireEdge) * fresnel * 0.08;
        surfaceCol += float3(0.3, 0.35, 0.4) * innerGlow;

        // Depth fade for back-facing areas
        float depthFade = smoothstep(0.0, 0.5, dot(n, normalize(ro - hitPos)));
        surfaceCol *= 0.3 + depthFade * 0.7;

        col = surfaceCol;
    }

    // Soft atmospheric glow around sphere
    float sphereDist = length(uv);
    float glow = exp(-sphereDist * 2.0) * 0.06;
    col += float3(0.4, 0.42, 0.45) * glow;

    return half4(half3(col), 1.0);
}

// ============================================================
// MARK: - STYLE TWO — Variations of the original ring shader
// ============================================================

// S2-1: Diamond Rings — diamond distance metric instead of circular
[[ stitchable ]] half4 diamondRings(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.05;
    float lw = 0.002;

    float3 col = float3(0.0);
    for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 6; i++) {
            float dist = abs(uv.x) + abs(uv.y);  // diamond distance
            col[j] += lw * float(i * i) / abs(fract(t - 0.01 * float(j) + float(i) * 0.012) * 5.0 - dist + fmod(uv.x * uv.y, 0.15));
        }
    }
    return half4(half3(col), 1.0);
}

// S2-2: Liquid Chrome — metallic fluid surface
[[ stitchable ]] half4 liquidChrome(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.3;

    // Domain warping for fluid motion
    float2 p = uv * 2.0;
    float n1 = valueNoise(p + float2(t, t * 0.6));
    float n2 = valueNoise(p + n1 * 1.5 + float2(-t * 0.4, t * 0.3));
    float n3 = valueNoise(p * 1.5 + n2 * 1.2 + float2(t * 0.2, -t * 0.5));

    // Chrome-like reflections: sharp highlights and dark valleys
    float chrome = n3 * 0.5 + 0.5;
    chrome = pow(chrome, 0.6);

    // Dark metallic color: deep shadows with subtle highlights
    float3 silver = float3(0.2, 0.2, 0.25);
    float3 highlight = float3(0.5, 0.5, 0.6);
    float3 shadow = float3(0.02, 0.01, 0.05);
    float3 tint = float3(0.15, 0.2, 0.4);

    float3 col = mix(shadow, silver, chrome);
    col = mix(col, highlight, smoothstep(0.8, 0.98, chrome));
    col += tint * smoothstep(0.3, 0.6, n1) * 0.15;

    // Subtle specular highlights
    float spec = pow(max(chrome, 0.0), 12.0);
    col += float3(0.6, 0.6, 0.8) * spec * 0.3;

    return half4(half3(col), 1.0);
}

// S2-3: Hologram — rainbow interference pattern
[[ stitchable ]] half4 hologram(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.5;

    // Scan lines
    float scan = sin(uv.y * 80.0 + t * 5.0) * 0.5 + 0.5;
    scan = pow(scan, 0.3) * 0.15 + 0.85;

    // Interference pattern — multiple overlapping wave sources
    float pattern = 0.0;
    pattern += sin(uv.x * 30.0 + t * 2.0) * 0.5;
    pattern += sin(uv.y * 25.0 - t * 1.5) * 0.5;
    pattern += sin((uv.x + uv.y) * 20.0 + t) * 0.3;
    pattern += sin(length(uv) * 15.0 - t * 3.0) * 0.4;

    // Rainbow color from angle + pattern
    float hue = fract(pattern * 0.15 + uv.x * 0.3 + uv.y * 0.2 + t * 0.1);
    float3 col = hsv2rgb(float3(hue, 0.6, 0.9));

    // Holographic shimmer: view-dependent color shift
    float shimmer = sin(uv.x * 50.0 + uv.y * 30.0 + t * 4.0) * 0.5 + 0.5;
    col = mix(col, float3(1.0), shimmer * 0.15);

    col *= scan;

    // Horizontal glitch lines
    float glitch = step(0.98, hashVal(float2(floor(uv.y * 40.0), floor(t * 10.0))));
    col += float3(0.3, 0.6, 1.0) * glitch * 0.5;

    return half4(half3(col), 1.0);
}

// S2-4: Supernova — explosive energy burst
[[ stitchable ]] half4 supernova(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float r = length(uv);
    float a = atan2(uv.y, uv.x);
    float t = time * 0.4;

    // Pulsing core
    float coreSize = 0.08 + sin(t * 3.0) * 0.02;
    float core = coreSize / (r + coreSize);
    core = pow(core, 2.0);

    // Shock wave rings expanding outward
    float3 col = float3(0.0);
    for (int i = 0; i < 4; i++) {
        float fi = float(i);
        float waveR = fract(t * 0.3 + fi * 0.25) * 2.5;
        float waveFade = exp(-fract(t * 0.3 + fi * 0.25) * 3.0);
        float wave = exp(-abs(r - waveR) * 20.0) * waveFade;
        float3 waveCol = hsv2rgb(float3(fract(fi * 0.15 + 0.05), 0.8, 1.0));
        col += waveCol * wave;
    }

    // Radial energy rays
    float rays = 0.0;
    for (int j = 0; j < 12; j++) {
        float fj = float(j);
        float rayAngle = fj * 0.5236 + t * 0.2 + sin(t + fj) * 0.3;
        float ray = pow(max(cos((a - rayAngle) * 6.0), 0.0), 40.0);
        ray *= exp(-r * 1.5) * (0.5 + 0.5 * sin(t * 2.0 + fj));
        rays += ray;
    }
    col += float3(1.0, 0.6, 0.2) * rays * 0.4;

    // Hot white core with orange/yellow gradient
    float3 coreCol = mix(float3(1.0, 0.4, 0.1), float3(1.0, 1.0, 0.9), smoothstep(0.3, 0.0, r));
    col += coreCol * core;

    // Particle debris
    for (int k = 0; k < 20; k++) {
        float fk = float(k);
        float pAngle = hashVal(float2(fk, 0.0)) * 6.283;
        float pSpeed = 0.3 + hashVal(float2(fk, 1.0)) * 0.7;
        float pR = fract(t * pSpeed * 0.2 + hashVal(float2(fk, 2.0))) * 1.8;
        float2 pPos = float2(cos(pAngle + t * 0.1), sin(pAngle + t * 0.1)) * pR;
        float pDist = length(uv - pPos);
        float p = 0.002 / (pDist * pDist + 0.002);
        float pFade = exp(-pR * 2.0);
        col += float3(1.0, 0.8, 0.4) * p * pFade * 0.08;
    }

    return half4(half3(col), 1.0);
}

// S2-5: Warp Speed — elongated horizontal rings like warp drive
[[ stitchable ]] half4 warpSpeed(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.07;
    float lw = 0.002;

    float3 col = float3(0.0);
    for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 6; i++) {
            float dist = length(uv * float2(0.4, 1.0));  // stretched ellipse
            col[j] += lw * float(i * i) / abs(fract(t - 0.008 * float(j) + float(i) * 0.015) * 4.0 - dist + fmod(uv.x, 0.3));
        }
    }
    // Cool blue-cyan tint
    col *= float3(0.6, 0.9, 1.3);
    return half4(half3(col), 1.0);
}

// S2-6: Magnetic Field — flowing field lines between two poles
[[ stitchable ]] half4 magneticField(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.3;

    float2 pole1 = float2(-0.5, 0.0);
    float2 pole2 = float2(0.5, 0.0);

    // Field direction from superposition of two poles
    float2 d1 = uv - pole1;
    float2 d2 = uv - pole2;
    float r1 = length(d1) + 0.001;
    float r2 = length(d2) + 0.001;
    float2 field = d1 / (r1 * r1) - d2 / (r2 * r2);

    // Field line pattern using atan of field direction
    float angle = atan2(field.y, field.x);
    float lines = sin(angle * 8.0 + t * 2.0);
    lines = pow(abs(lines), 0.3);

    // Field strength for brightness
    float strength = length(field);
    strength = clamp(strength * 0.3, 0.0, 1.0);

    // Color: cyan near pole1, magenta near pole2
    float blend = clamp((uv.x + 0.5) * 1.0, 0.0, 1.0);
    float3 cyan = float3(0.0, 0.8, 1.0);
    float3 magenta = float3(1.0, 0.2, 0.8);
    float3 col = mix(cyan, magenta, blend) * lines * strength;

    // Pole glow
    col += cyan * 0.3 / (r1 * 5.0 + 0.3);
    col += magenta * 0.3 / (r2 * 5.0 + 0.3);

    // Animated particles along field lines
    float flowPhase = fract(angle * 1.27 + t + hashVal(floor(float2(angle * 8.0, 0.0))) * 5.0);
    float particle = smoothstep(0.02, 0.0, abs(flowPhase - 0.5)) * strength;
    col += float3(1.0) * particle * 0.4;

    return half4(half3(col), 1.0);
}

// S2-7: Glitch Art — digital corruption with RGB split
[[ stitchable ]] half4 glitchArt(float2 position, half4 color, float2 size, float time) {
    float2 uv = position / size;
    float t = time;

    // Random block glitch offset
    float blockY = floor(uv.y * 20.0);
    float glitchSeed = floor(t * 3.0);
    float blockRand = hashVal(float2(blockY, glitchSeed));
    float isGlitched = step(0.85, blockRand);
    float offset = (hashVal(float2(blockY + 1.0, glitchSeed)) - 0.5) * 0.15 * isGlitched;

    // RGB channel split
    float2 uvR = uv + float2(offset + sin(t * 10.0) * 0.005, 0.0);
    float2 uvG = uv;
    float2 uvB = uv - float2(offset + sin(t * 10.0) * 0.005, 0.0);

    // Base pattern: animated geometric shapes
    float2 grid = fract(uv * 8.0 + float2(t * 0.2, t * 0.1));
    float basePattern = step(0.4, grid.x) * step(0.4, grid.y);
    basePattern += sin(uv.x * 30.0 + t * 5.0) * sin(uv.y * 20.0 - t * 3.0) * 0.3;

    float2 gridR = fract(uvR * 8.0 + float2(t * 0.2, t * 0.1));
    float2 gridB = fract(uvB * 8.0 + float2(t * 0.2, t * 0.1));
    float patR = step(0.4, gridR.x) * step(0.4, gridR.y) + sin(uvR.x * 30.0 + t * 5.0) * 0.3;
    float patB = step(0.4, gridB.x) * step(0.4, gridB.y) + sin(uvB.x * 30.0 + t * 5.0) * 0.3;

    float3 col = float3(patR * 0.9, basePattern * 0.9, patB * 0.9);

    // Scan lines
    float scan = sin(uv.y * 300.0) * 0.5 + 0.5;
    col *= 0.85 + scan * 0.15;

    // Random bright noise lines
    float noiseLine = step(0.97, hashVal(float2(floor(uv.y * 200.0), floor(t * 20.0))));
    col += float3(0.5, 1.0, 0.8) * noiseLine * 0.4;

    // Vignette
    float2 vc = uv - 0.5;
    col *= 1.0 - dot(vc, vc) * 0.8;

    return half4(half3(col), 1.0);
}

// S2-8: Ink Smoke — flowing ink in water simulation
[[ stitchable ]] half4 inkSmoke(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.2;

    // Multiple layers of domain-warped noise for smoke tendrils
    float2 p = uv * 1.8;

    // Layer 1: main smoke body
    float2 q = float2(fbm(p + float2(t * 0.4, t * 0.3)),
                       fbm(p + float2(t * 0.2, -t * 0.4)));
    float2 r2 = float2(fbm(p + q * 4.0 + float2(1.7, 9.2) + t * 0.15),
                        fbm(p + q * 4.0 + float2(8.3, 2.8) - t * 0.1));
    float f = fbm(p + r2 * 2.0);

    // Color palette: deep ink colors
    float3 ink1 = float3(0.05, 0.0, 0.1);     // deep purple-black
    float3 ink2 = float3(0.1, 0.2, 0.5);       // deep blue
    float3 ink3 = float3(0.4, 0.1, 0.3);       // burgundy
    float3 ink4 = float3(0.0, 0.3, 0.4);       // teal

    float3 col = mix(ink1, ink2, clamp(f * 2.0, 0.0, 1.0));
    col = mix(col, ink3, clamp(q.x * 1.5, 0.0, 1.0));
    col = mix(col, ink4, clamp(r2.y * 0.8, 0.0, 1.0));

    // Wispy highlights
    float wisp = pow(clamp(f * 1.5, 0.0, 1.0), 3.0);
    col += float3(0.3, 0.2, 0.4) * wisp;

    return half4(half3(col), 1.0);
}

// S2-9: Neon Orbit — rings with rotating angular color shift
[[ stitchable ]] half4 neonOrbit(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float t = time * 0.06;
    float lw = 0.002;
    float a = atan2(uv.y, uv.x);

    float3 col = float3(0.0);
    for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 5; i++) {
            float angularShift = sin(a * 3.0 + time * 0.5 + float(j)) * 0.08;
            col[j] += lw * float(i * i) / abs(fract(t - 0.01 * float(j) + float(i) * 0.01) * 5.0 - length(uv) + angularShift + fmod(uv.x + uv.y, 0.2));
        }
    }
    // Boost saturation
    col *= float3(1.1, 0.8, 1.2);
    return half4(half3(col), 1.0);
}

// S2-10: Plasma Globe — electric tendrils radiating from center
[[ stitchable ]] half4 plasmaGlobe(float2 position, half4 color, float2 size, float time) {
    float2 uv = (position * 2.0 - size) / min(size.x, size.y);
    float r = length(uv);
    float a = atan2(uv.y, uv.x);
    float t = time;

    // Dark glass sphere boundary
    float sphereR = 0.95;
    float sphereEdge = smoothstep(sphereR, sphereR - 0.03, r);
    float rimGlow = exp(-abs(r - sphereR) * 30.0) * 0.3;

    float3 col = float3(0.01, 0.0, 0.02);

    // Electric tendrils — 8 main bolts
    for (int i = 0; i < 8; i++) {
        float fi = float(i);
        float baseAngle = fi * 0.785 + t * 0.3;

        // Tendril path: noisy line from center to edge
        for (int s = 0; s < 15; s++) {
            float fs = float(s) / 14.0;  // 0 to 1 along tendril
            float segR = fs * sphereR * 0.9;

            // Wobble the angle using noise at each segment
            float wobble = (valueNoise(float2(fi * 7.0 + fs * 3.0, t * 2.0)) - 0.5) * 1.2;
            wobble += (valueNoise(float2(fi * 13.0 + fs * 8.0, t * 3.5)) - 0.5) * 0.5;
            float segAngle = baseAngle + wobble * fs;

            float2 segPos = float2(cos(segAngle), sin(segAngle)) * segR;
            float d = length(uv - segPos);

            // Tendril brightness: bright core, soft glow
            float core = 0.002 / (d * d + 0.002);
            float glow = 0.008 / (d + 0.008);

            // Color: purple core fading to blue/cyan at tips
            float3 tendrilCol = mix(float3(0.8, 0.3, 1.0), float3(0.3, 0.5, 1.0), fs);
            float3 whiteCore = float3(0.9, 0.8, 1.0);

            float brightness = core * 0.06 + glow * 0.03;
            brightness *= (0.7 + 0.3 * sin(t * 5.0 + fi * 2.0 + fs * 4.0));
            col += mix(tendrilCol, whiteCore, core * 0.3) * brightness;
        }
    }

    // Central orb glow
    float coreGlow = 0.04 / (r * r + 0.04);
    col += float3(0.6, 0.4, 0.9) * coreGlow * 0.5;
    float brightCore = 0.005 / (r * r + 0.005);
    col += float3(0.9, 0.85, 1.0) * brightCore * 0.3;

    // Apply sphere boundary
    col *= sphereEdge;

    // Glass rim
    col += float3(0.3, 0.2, 0.5) * rimGlow;

    // Subtle ambient flicker
    col *= 0.9 + 0.1 * sin(t * 8.0);

    return half4(half3(col), 1.0);
}
