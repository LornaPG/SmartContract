package com.smartcontract;

public enum MessageTitle {
    UPLOAD_CONTENT("uploadContent"),
    UPLOAD_HOLD_CONTENT("uploadHoldContent"),
    REMOVE_CONTENT("removeContent"),
    EVENT("event"),
    ADD_DEAL("addDeal"),
    REMOVE_DEAL("removeDeal");

    MessageTitle(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static MessageTitle fromValue(String val) {
        for (MessageTitle title : MessageTitle.values()) {
            if (title.getValue().equals(val)) {
                return title;
            }
        }
        return null;
    }

    private final String value;
}
