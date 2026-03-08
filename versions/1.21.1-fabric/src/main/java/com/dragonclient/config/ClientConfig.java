package com.dragonclient.config;

import java.util.HashMap;
import java.util.Map;

public class ClientConfig {
    public Map<String, Boolean> moduleStates = new HashMap<>();
    public Map<String, HudPosition> hudPositions = new HashMap<>();
    public Map<String, Map<String, Object>> moduleSettings = new HashMap<>();

    public static class HudPosition {
        public int x;
        public int y;

        public HudPosition() {}

        public HudPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
