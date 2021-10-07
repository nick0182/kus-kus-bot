package com.shaidulin.kuskusbot.update;

public enum Permission {
    MESSAGE(0),
    CALLBACK(1);

    /***
     * defines index in String of permissions
     */
    private final int index;

    Permission(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}