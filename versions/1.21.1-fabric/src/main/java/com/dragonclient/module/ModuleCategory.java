package com.dragonclient.module;

public enum ModuleCategory {
    HUD("HUD", "HUD elements and overlays"),
    VISUAL("Visual", "Visual enhancements"),
    MOVEMENT("Movement", "Movement modifications"),
    PLAYER("Player", "Player utilities"),
    MISC("Misc", "Miscellaneous features");

    private final String name;
    private final String description;

    ModuleCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
