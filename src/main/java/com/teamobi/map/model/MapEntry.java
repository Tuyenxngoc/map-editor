package com.teamobi.map.model;

public class MapEntry extends Entry {
    public byte icon;

    public MapEntry(byte icon, short x, short y) {
        super(x, y);
        this.icon = icon;
    }
}
