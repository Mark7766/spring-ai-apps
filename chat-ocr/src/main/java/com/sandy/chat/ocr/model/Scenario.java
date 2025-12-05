package com.sandy.chat.ocr.model;

public enum Scenario {
    DESIGN("设计图纸审核", "📋"),
    MEDICAL("医学影像咨询", "🏥"),
    EQUIPMENT("设备故障诊断", "⚙️");

    private final String displayName;
    private final String icon;

    Scenario(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}

