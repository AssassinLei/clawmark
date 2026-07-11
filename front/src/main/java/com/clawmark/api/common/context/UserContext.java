package com.clawmark.api.common.context;

public final class UserContext {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<String>();

    private UserContext() {
    }

    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
