package server;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String data;


    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {

        String message = null;
        switch (type) {

            case TEXT -> {
                message = "Текстовое сообщение " + data;
            }
            case NAME_ACCEPTED -> {
                message = "Имя принято " + data;
            }
            case USER_ADDED -> {
                message = "Добавлен пользователь: " + data;
            }
            case USER_REMOVED -> {
                message = "Пользователь удалён " + data;
            }
            case NAME_REQUEST -> {
                message = "Запрошено имя у " + data;
            }
            case USER_NAME -> {
                message = "Имя пользователя ";
            }
        }
        return message;
    }
}
