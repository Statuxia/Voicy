package me.statuxia.utils;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Getter @Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserInfo {

    public static final Map<Long, UserInfo> USER_BY_ID = new HashMap<>();

    long userID;
    String voiceName;
    int maxEntry;
    String alloweds;
    String denieds;

    public UserInfo(long user, String name, int maxEntry, String allowed, String denieds) {
        this.userID = user;
        this.voiceName = name;
        this.maxEntry = maxEntry;
        this.alloweds = allowed;
        this.denieds = denieds;
        USER_BY_ID.put(userID, this);
    }

    public static UserInfo get(long id) {
        return USER_BY_ID.get(id);
    }

}