package client;
import server.*;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class Client {
    protected Connection connection;

    private volatile boolean clientConnected;

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите ваше имя:");
        return ConsoleHelper.readString();
    }

    public class SocketThread extends Thread {
        @Override
        public void run() {

            try {// Получаем из файла "settings.json" адрес и порт сервера.
                String serverSetting = "settings.json";
                ServerSettings.loadFromFile(serverSetting);

                // Создаем соединение с сервером
                connection = new Connection(new Socket(ServerSettings.getHost(), ServerSettings.getPort()));

                addLoggingClient();
                ConsoleHelper.writeMessage("Вы подключились к серверу.");
                LOGGER.info("Успешное подключение к серверу.");
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.NAME_REQUEST) { // Сервер запросил имя пользователя
                    ConsoleHelper.writeMessage("Сервер запросил Ваше имя.");
                    LOGGER.info("Сервер запросил имя.");
                    // Запрашиваем ввод имени с консоли
                    String name = getUserName();
                    // Отправляем имя на сервер
                    connection.send(new Message(MessageType.USER_NAME, name));
                    LOGGER.info("Отправка имени '" + name + "' на сервер");

                } else if (message.getType() == MessageType.NAME_ACCEPTED) { // Сервер принял имя пользователя
                    // Сообщаем главному потоку, что он может продолжить работу
                    ConsoleHelper.writeMessage("Сервер принял Ваше имя.");
                    LOGGER.info("Сервер принял имя");
                    notifyConnectionStatusChanged(true);
                    return;

                } else {
                    LOGGER.warning("Неизвестный тип сообщения");
                    throw new IOException("Неизвестный тип сообщения");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            // Цикл обработки сообщений сервера
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) { // Сервер прислал сообщение с текстом
                    processIncomingMessage(message.getData());
                } else if (MessageType.USER_ADDED == message.getType()) {
                    informAboutAddingNewUser(message.getData());
                } else if (MessageType.USER_REMOVED == message.getType()) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    LOGGER.warning("Неизвестный тип сообщения");
                    throw new IOException("Неизвестный тип сообщения");
                }
            }
        }


        protected void processIncomingMessage(String message) {  // Вывод текста сообщения в консоль
            ConsoleHelper.writeMessage(message);
            LOGGER.info("Полученное сообщение: " + message);
        }

        protected void informAboutAddingNewUser(String userName) {  // Вывод информации о добавлении участника
            ConsoleHelper.writeMessage("Участник '" + userName + "' присоединился к чату.");
            LOGGER.info("Участник '" + userName + "' присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {  // Вывод информации о выходе участника
            ConsoleHelper.writeMessage("Участник '" + userName + "' покинул чат.");
            LOGGER.info("Участник '" + userName + "' покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

    }


    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
            LOGGER.info("Отправлено сообщение: " + text);

        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение");
            LOGGER.warning("Не удалось отправить сообщение");
            clientConnected = false;
        }
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    public void run() {
        SocketThread socketThread = new SocketThread();

        socketThread.setDaemon(true);  // Помечаем поток как daemon
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            LOGGER.warning("Произошла ошибка во время работы клиента.");
            return;
        }

        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            LOGGER.info("Соединение установлено.");
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            LOGGER.warning("Произошла ошибка во время работы клиента.");
        }


        // Пока не будет введена команда exit, считываем сообщения с консоли и отправляем их на сервер
        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equalsIgnoreCase("exit"))
                break;
            if (shouldSendTextFromConsole())
                sendTextMessage(text);
        }
    }

    private void addLoggingClient() {
        String nameFileClientLog = "client.log";
        FileHandler fileHandler = null;  // создаем обработчик файлового вывода
        try {
            fileHandler = new FileHandler(nameFileClientLog, true);
        } catch (IOException e) {
            LOGGER.severe("Ошибка при создании log-обработчика.");
        }
        LOGGER.setUseParentHandlers(false); // отключаем логирование в консоль.
        LOGGER.addHandler(fileHandler);  // добавляем обработчик в наш логгер.
    }
}


/**
 *
 *   Данный класс реализует простейший чат-клиент, подключаемый к серверу через сокетное соединение.
 *
 *   Класс Client имеет вложенный класс SocketThread, который реализует Runnable интерфейс
 *   и переопределяет метод run(). Этот метод осуществляет подключение к серверу,
 *   принимает и обрабатывает сообщения от сервера.
 *
 *   В методе run() создается новый поток SocketThread, помеченный как daemon, и запускается методом start().
 *   Затем главный поток приостанавливается до тех пор, пока не будет уведомлен о том, что клиент подключился к серверу.
 *
 *   После подключения клиента, если соединение было успешно установлено,
 *   выводится сообщение о подключении и возможности отправки сообщений.
 *   При отправке сообщения, клиент использует метод sendTextMessage(),
 *   который формирует и отправляет объект типа Message, содержащий текст сообщения.
 *
 *  SocketThread имеет ряд защищенных методов, в том числе:
 *
 *    clientHandshake() - обмен сообщениями с сервером при подключении клиента,
 *    в том числе получение имени пользователя и отправку его на сервер.
 *
 *    clientMainLoop() - цикл обработки сообщений от сервера,
 *    в том числе получение текстовых сообщений, уведомлений о новых пользователях и выходе пользователя из чата.
 *
 *    processIncomingMessage() - вывод текста сообщения в консоль.
 *
 *    informAboutAddingNewUser() - вывод информации о добавлении нового пользователя в чат.
 *
 *    informAboutDeletingNewUser() - вывод информации о выходе пользователя из чата.
 *
 *    notifyConnectionStatusChanged() - уведомление главного потока о том, что клиент подключился к серверу.
 *
 *  Client также содержит защищенный метод getUserName(), который запрашивает у пользователя имя.
 *
 *  Класс Logger используется для ведения логов работы клиента.
 *
 *  Клиент работает в бесконечном цикле, пока пользователь не введет команду "exit".
 *
 */