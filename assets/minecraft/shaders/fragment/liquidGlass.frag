#version 120

uniform sampler2D sceneTexture;

uniform vec2 resolution;      // framebuffer size in real pixels
uniform vec2 guiResolution;   // ScaledResolution size in GUI pixels
uniform float time;

uniform vec4 rect;            // x, y, w, h in GUI coords (top-left origin)
uniform float radius;         // GUI pixels
uniform float blur;           // blur radius in real pixels
uniform float distortion;     // refraction strength
uniform float opacity;        // how strongly the tint shows
uniform vec3 tint;

vec3 sampleScene(vec2 uv) {
    return texture2D(sceneTexture, clamp(uv, vec2(0.001), vec2(0.999))).rgb;
}

vec3 blurScene(vec2 uv, float px) {
    vec3 sum = sampleScene(uv);
    float total = 1.0;
    for (int i = 0; i < 16; i++) {
        float a = float(i) * 2.399963;          // golden angle
        float rr = sqrt((float(i) + 0.5) / 16.0) * px;
        vec2 o = vec2(cos(a), sin(a)) * rr / resolution;
        sum += sampleScene(uv + o);
        total += 1.0;
    }
    return sum / total;
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    // gl_FragCoord is bottom-left origin; GUI coords are top-left origin.
    vec2 guiPoint = vec2(
            gl_FragCoord.x * guiResolution.x / resolution.x,
            (resolution.y - gl_FragCoord.y) * guiResolution.y / resolution.y
    );

    vec2 halfSize = rect.zw * 0.5;
    vec2 local = guiPoint - (rect.xy + halfSize);
    float r = min(radius, min(halfSize.x, halfSize.y));

    vec2 q = abs(local) - halfSize + r;
    float d = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;

    float aa = max(0.75, guiResolution.x / resolution.x);
    float mask = 1.0 - smoothstep(0.0, aa, d);
    if (mask <= 0.0) discard;

    // Outward normal of the rounded box (in 2D).
    vec2 n;
    if (q.x > 0.0 || q.y > 0.0) {
        n = sign(local) * normalize(max(q, 0.0) + 1e-5);
    } else if (q.x > q.y) {
        n = vec2(sign(local.x), 0.0);
    } else {
        n = vec2(0.0, sign(local.y));
    }
    // Scene texture is bottom-left origin, so flip Y of the GUI-space normal.
    n.y = -n.y;

    // ---- Fake glass bevel -------------------------------------------------
    // bt = 0 right at the edge, 1 once we're past the bevel (flat glass).
    float bevelW = 20.0;                       // GUI px, width of the curved rim
    float bt = clamp(-d / bevelW, 0.0, 1.0);
    float slope = pow(1.0 - bt, 2.0) * 1.6;    // surface tilts steeply at the very edge
    vec3 n3 = normalize(vec3(n * slope, 1.0)); // 3D surface normal

    // ---- Real refraction, with dispersion --------------------------------
    float pxPerGui = resolution.x / guiResolution.x;
    float thickness = 28.0 * distortion * pxPerGui;   // how far the light bends

    vec2 wobble = vec2(
            sin(time * 0.8 + guiPoint.y * 0.03),
            cos(time * 0.7 + guiPoint.x * 0.03)
    );
    float wobbleAmt = 2.0 * distortion * pxPerGui;    // strength in real pixels
    vec2 wobbleUv = wobble * wobbleAmt * (0.35 + 0.65 * (1.0 - bt)) / resolution;

    vec3 I = vec3(0.0, 0.0, -1.0);             // view ray going into the glass
    vec2 uvR = uv + wobbleUv + refract(I, n3, 1.0 / 1.47).xy * thickness / resolution;
    vec2 uvG = uv + wobbleUv + refract(I, n3, 1.0 / 1.50).xy * thickness / resolution;
    vec2 uvB = uv + wobbleUv + refract(I, n3, 1.0 / 1.53).xy * thickness / resolution;

    // 3x the blur cost. If it's slow, drop the loop in blurScene to 8 taps.
    vec3 col = vec3(
            blurScene(uvR, blur).r,
            blurScene(uvG, blur).g,
            blurScene(uvB, blur).b
    );

    col = mix(col, tint, clamp(opacity, 0.0, 1.0) * 0.35);

    // ---- Lighting on the bevel -------------------------------------------
    vec3 V  = vec3(0.0, 0.0, 1.0);
    vec3 L1 = normalize(vec3(-0.45,  0.75, 0.5));   // key light, upper left
    vec3 L2 = normalize(vec3( 0.45, -0.75, 0.5));   // bounce light, lower right

    float s1 = pow(max(dot(n3, normalize(L1 + V)), 0.0), 24.0);
    float s2 = pow(max(dot(n3, normalize(L2 + V)), 0.0), 24.0);
    float rimMask = 1.0 - bt;                       // only light the bevel

    col += vec3(1.0) * (s1 * 0.9 + s2 * 0.35) * rimMask;

    // Fresnel: edges that tilt away from the viewer reflect more.
    float fres = pow(1.0 - n3.z, 2.0);
    col += vec3(1.0) * fres * 0.25;

    // Thin bright border line right at the edge.
    float border = 1.0 - smoothstep(0.0, 1.5, abs(d + 0.75));
    col += vec3(1.0) * border * 0.20;

    // Vertical gradient (lighter top, darker bottom). rect.w is the panel height.
    float gy = clamp((guiPoint.y - rect.y) / rect.w, 0.0, 1.0);
    col += (0.5 - gy) * 0.07;

    gl_FragColor = vec4(col, mask);
}
