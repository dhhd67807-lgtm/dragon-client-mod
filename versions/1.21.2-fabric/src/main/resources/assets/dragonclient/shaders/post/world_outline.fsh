#version 330

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform OutlineConfig {
    float DepthSensitivity; // World outline intensity (Complementary WORLD_OUTLINE_I style)
    float ColorSensitivity; // Reserved
    float Thickness;        // World outline thickness (1..4)
    float Darkness;         // Dark outline intensity (0..1)
};

out vec4 fragColor;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec3 centerColor = texture(MainSampler, texCoord).rgb;
    float centerDepth = texture(MainDepthSampler, texCoord).r;
    float centerLuma = luminance(centerColor);

    vec2 offsets[8] = vec2[](
        vec2(1.0, 0.0),
        vec2(-1.0, 0.0),
        vec2(0.0, 1.0),
        vec2(0.0, -1.0),
        vec2(1.0, 1.0),
        vec2(-1.0, 1.0),
        vec2(1.0, -1.0),
        vec2(-1.0, -1.0)
    );

    float thickness = clamp(Thickness, 0.75, 2.25);
    float depthEdgePrimary = 0.0;
    float depthEdgeSecondary = 0.0;
    float colorEdge = 0.0;
    float detailEdge = 0.0;
    float lumaMin = centerLuma;
    float lumaMax = centerLuma;
    vec3 blurAccum = centerColor * 1.8;
    float blurWeight = 1.8;

    for (int i = 0; i < 8; i++) {
        vec2 off1 = offsets[i] * oneTexel * thickness;
        vec2 off2 = offsets[i] * oneTexel * (thickness * 2.0);

        vec3 c1 = texture(MainSampler, texCoord + off1).rgb;
        vec3 c2 = texture(MainSampler, texCoord + off2).rgb;
        float d1 = texture(MainDepthSampler, texCoord + off1).r;
        float d2 = texture(MainDepthSampler, texCoord + off2).r;
        float l1 = luminance(c1);
        float l2 = luminance(c2);

        float depthScale = 1.0 + centerDepth * 48.0;
        depthEdgePrimary = max(depthEdgePrimary, abs(d1 - centerDepth) * depthScale);
        depthEdgeSecondary = max(depthEdgeSecondary, abs(d2 - centerDepth) * depthScale);

        colorEdge = max(colorEdge, length(c1 - centerColor));
        detailEdge = max(detailEdge, abs(l1 - centerLuma) * 0.72 + abs(l2 - centerLuma) * 0.26);
        lumaMin = min(lumaMin, min(l1, l2));
        lumaMax = max(lumaMax, max(l1, l2));

        float w1 = 1.0 / (1.0 + length(offsets[i]));
        float w2 = w1 * 0.6;
        blurAccum += c1 * w1 + c2 * w2;
        blurWeight += w1 + w2;
    }

    // In first person, suppress very-near geometry (hand/tool) so world edges dominate.
    float worldMask = smoothstep(0.08, 0.50, centerDepth);

    float depthEdge = max(depthEdgePrimary, depthEdgeSecondary * 0.75);
    float depthFactor = smoothstep(0.042, 0.48, depthEdge * max(1.0, DepthSensitivity * 2.15));
    float colorFactor = smoothstep(0.12, 0.70, colorEdge * max(0.72, ColorSensitivity));
    float localContrast = max(0.0, lumaMax - lumaMin);
    float detailFactor = smoothstep(
        0.048,
        0.34,
        (detailEdge + localContrast * 0.42) * (0.72 + ColorSensitivity * 0.22)
    );
    float baseEdge = max(depthFactor, colorFactor * 0.50);
    float thirdLayer = detailFactor * (0.34 + depthFactor * 0.22);
    float edge = max(baseEdge, thirdLayer);
    edge = saturate(edge + depthFactor * 0.07) * worldMask;

    // Complementary-like bright contour with soft blend.
    float brightEdge = pow(smoothstep(0.06, 0.98, edge), 0.86);
    vec3 softBlur = blurAccum / max(0.0001, blurWeight);
    vec3 lifted = min(centerColor * brightEdge * 2.10, vec3(brightEdge * 0.90));
    vec3 brightColor = centerColor + lifted;
    brightColor = mix(brightColor, softBlur, brightEdge * 0.13);

    // Soft dark companion line for contrast (no harsh black stroke).
    float darkEdge = smoothstep(0.10, 0.94, edge) * clamp(Darkness, 0.0, 1.0);
    darkEdge *= (0.62 + depthFactor * 0.28) * worldMask;
    vec3 shaded = mix(brightColor, brightColor * 0.34, darkEdge * 0.54);

    float blend = smoothstep(0.0, 1.0, brightEdge * 0.98 + darkEdge * 0.45);
    vec3 finalColor = mix(centerColor, shaded, blend);

    fragColor = vec4(clamp(finalColor, 0.0, 1.0), 1.0);
}
