#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;

uniform float DepthSensitivity;
uniform float ColorSensitivity;
uniform float Thickness;
uniform float Darkness;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    vec2 offset = oneTexel * clamp(Thickness, 0.75, 2.25);

    vec3 centerColor = texture(DiffuseSampler, texCoord).rgb;
    float centerDepth = texture(MainDepthSampler, texCoord).r;
    float centerLuma = luminance(centerColor);

    vec3 leftColor = texture(DiffuseSampler, texCoord - vec2(offset.x, 0.0)).rgb;
    vec3 rightColor = texture(DiffuseSampler, texCoord + vec2(offset.x, 0.0)).rgb;
    vec3 upColor = texture(DiffuseSampler, texCoord - vec2(0.0, offset.y)).rgb;
    vec3 downColor = texture(DiffuseSampler, texCoord + vec2(0.0, offset.y)).rgb;

    float leftDepth = texture(MainDepthSampler, texCoord - vec2(offset.x, 0.0)).r;
    float rightDepth = texture(MainDepthSampler, texCoord + vec2(offset.x, 0.0)).r;
    float upDepth = texture(MainDepthSampler, texCoord - vec2(0.0, offset.y)).r;
    float downDepth = texture(MainDepthSampler, texCoord + vec2(0.0, offset.y)).r;

    float depthScale = 1.0 + centerDepth * 48.0;
    float depthEdge = max(
        max(abs(leftDepth - centerDepth), abs(rightDepth - centerDepth)),
        max(abs(upDepth - centerDepth), abs(downDepth - centerDepth))
    ) * depthScale;
    float depthFactor = smoothstep(0.042, 0.48, depthEdge * max(1.0, DepthSensitivity * 2.15));

    float colorEdge = max(
        max(length(leftColor - centerColor), length(rightColor - centerColor)),
        max(length(upColor - centerColor), length(downColor - centerColor))
    );
    float colorFactor = smoothstep(0.12, 0.70, colorEdge * max(0.72, ColorSensitivity));

    float leftLuma = luminance(leftColor);
    float rightLuma = luminance(rightColor);
    float upLuma = luminance(upColor);
    float downLuma = luminance(downColor);
    float detailEdge = max(
        max(abs(leftLuma - centerLuma), abs(rightLuma - centerLuma)),
        max(abs(upLuma - centerLuma), abs(downLuma - centerLuma))
    );
    float detailFactor = smoothstep(0.048, 0.34, detailEdge * (0.72 + ColorSensitivity * 0.22));

    float edge = max(max(depthFactor, colorFactor * 0.50), detailFactor * (0.34 + depthFactor * 0.22));
    edge = saturate(edge + depthFactor * 0.07);

    float worldMask = smoothstep(0.08, 0.50, centerDepth);
    edge *= worldMask;

    float brightEdge = pow(smoothstep(0.06, 0.98, edge), 0.86);
    vec3 lifted = min(centerColor * brightEdge * 2.10, vec3(brightEdge * 0.90));
    vec3 brightColor = centerColor + lifted;

    float darkEdge = smoothstep(0.10, 0.94, edge) * clamp(Darkness, 0.0, 1.0);
    darkEdge *= (0.62 + depthFactor * 0.28) * worldMask;
    vec3 shaded = mix(brightColor, brightColor * 0.34, darkEdge * 0.54);

    float blend = smoothstep(0.0, 1.0, brightEdge * 0.98 + darkEdge * 0.45);
    vec3 finalColor = mix(centerColor, shaded, blend);

    fragColor = vec4(clamp(finalColor, 0.0, 1.0), 1.0);
}
