package com.java2.lesson_7_8.Messages;

import java.util.UUID;

public abstract class Message {
    protected UUID uuid;
    protected MessageType type;
    protected Object data;
/*
    public Message(int id, MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }
*/
    public Message(MessageType type, Object data) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public MessageType getType() {
        return type;
    }

    public abstract String serialize();
    public abstract boolean deserialize(String data);
}