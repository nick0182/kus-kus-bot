package com.shaidulin.kuskusbot.update;

public enum Permission {
    COMMAND(0),
    MESSAGE(1),
    CALLBACK(2);

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