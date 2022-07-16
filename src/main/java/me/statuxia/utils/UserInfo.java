package me.statuxia.utils;

public class UserInfo {
    public final long USER;
    public final String NAME;
    public final int MAX_ENTRY;
    public final String ALLOWEDS;
    public final String DENIEDS;

    UserInfo(long user, String name, int maxEntry, String allowed, String denieds) {
        this.USER = user;
        this.NAME = name;
        this.MAX_ENTRY = maxEntry;
        this.ALLOWEDS = allowed;
        this.DENIEDS = denieds;
    }
}
