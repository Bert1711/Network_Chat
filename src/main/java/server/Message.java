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

        String str = null;
        switch (type) {

            case TEXT -> {
                str = "Текстовое сообщение " + data;
            }
            case NAME_ACCEPTED -> {
                str = "Имя принято " + data;
            }
            case USER_ADDED -> {
                str = "Добавлен пользователь " + data;
            }
            case USER_REMOVED -> {
                str = "Пользователь удалён " + data;
            }
            case NAME_REQUEST -> {
                str = "Запрошено имя у " + data;
            }
            case USER_NAME -> {
                str = "Имя пользователя ";
            }
        }
        return str;
    }
}
