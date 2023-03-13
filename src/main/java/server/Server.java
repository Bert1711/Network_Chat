package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    // Ключом является имя клиента, а значением - соединение с ним.
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {

        setupLogger(); // Логирование

        // Запуск сервера
        ConsoleHelper.writeMessage("Введите порт сервера: 4444");
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Чат-сервер запущен.");
            LOGGER.info("Чат-сервер запущен.");
            while (true) {
                Socket socket = serverSocket.accept(); // Ожидаем входящее соединение.
                new ServerHandler(socket).start();  // После того как соединение установлено - запускаем отдельный поток.
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Произошла ошибка при запуске или при работе сервера.");
            e.printStackTrace();
        }
    }

    private static void setupLogger() {
        FileHandler fileHandler = null;  // создаем обработчик файлового вывода
        try {
            fileHandler = new FileHandler("server.log", true);
        } catch (IOException e) {
            LOGGER.severe("Ошибка при создании обработчика файлового вывода.");
        }
        LOGGER.setUseParentHandlers(false); // отключаем логирование в консоль.
        LOGGER.addHandler(fileHandler);  // добавляем обработчик в наш логгер
    }

    public static class ServerHandler extends Thread {
        private Socket socket;
        public ServerHandler(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с " + socket.getRemoteSocketAddress());
            LOGGER.info("Установлено новое соединение с " + socket.getRemoteSocketAddress());

            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = chatRegistration(connection);
                LOGGER.info("Добавлен участник: " + userName);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                LOGGER.severe("Ошибка при обмене данных с " + socket.getRemoteSocketAddress());
                e.printStackTrace();

            }
            if (userName != null) {
                connectionMap.remove(userName);
                LOGGER.info("Удалён участник: " + userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто");
            LOGGER.info("Соединение с " + socket.getRemoteSocketAddress() + " закрыто");
        }

        // Регистрация в чате.
        private String chatRegistration(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                ConsoleHelper.writeMessage("Запрошено имя у " + connection.getRemoteSocketAddress());
                LOGGER.info("Запрошено имя у " + connection.getRemoteSocketAddress());
                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Получено сообщение от " + connection.getRemoteSocketAddress()
                            + ". Тип сообщения не соответствует протоколу");
                    LOGGER.warning("Получено сообщение от " + connection.getRemoteSocketAddress()
                            + ". Тип сообщения не соответствует протоколу");
                    continue;
                }
                String userName = message.getData();
                if (userName.isEmpty()) {
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с пустым именем от "
                            + connection.getRemoteSocketAddress());
                    LOGGER.warning("Попытка подключения к серверу с пустым именем от "
                            + connection.getRemoteSocketAddress());
                    continue;
                }
                if (Server.connectionMap.containsKey(userName)) {
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с уже используемым именем от "
                            + connection.getRemoteSocketAddress());
                    LOGGER.warning("Попытка подключения к серверу с уже используемым именем от "
                            + connection.getRemoteSocketAddress());
                    continue;
                }
                Server.connectionMap.put(userName, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                ConsoleHelper.writeMessage("Имя принято у " + connection.getRemoteSocketAddress());
                LOGGER.info("Имя принято у " + connection.getRemoteSocketAddress());
                return userName;
            }
        }

        // Уведомление пользователя о других участников чата.
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : Server.connectionMap.keySet()) {
                if (name.equals(userName))
                    continue;
                connection.send(new Message(MessageType.USER_ADDED, name));
                LOGGER.info("Уведомление пользователя "
                        + connection.getRemoteSocketAddress()
                        + " о других участников чата.");
            }
        }

        // Основной цикл сервера.
        protected void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String data = message.getData();
                    LOGGER.info("Входящее сообщение от " + userName + ": " + data);
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
                    LOGGER.info(userName + ": " + data);
                } else {
                    ConsoleHelper.writeMessage("Получено сообщение от " + connection.getRemoteSocketAddress()
                            + ". Тип сообщения не соответствует протоколу.");
                    LOGGER.warning("Получено сообщение от " + connection.getRemoteSocketAddress()
                            + ". Тип сообщения не соответствует протоколу.");
                }
            }
        }
    }

    // Метод для отправки сообщения всем клиентам
    private static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
                ConsoleHelper.writeMessage(connection.getRemoteSocketAddress()
                        + " отправлено сообщение: " + message);
                LOGGER.info(connection.getRemoteSocketAddress()
                        + " отправлено сообщение: " + message);
            } catch (IOException e) {
                LOGGER.warning("Не получилось отправить сообщение " + connection.getRemoteSocketAddress());
                e.printStackTrace();
            }
        }
    }
}

/**
 * "ServerSocket" - это класс в Java, который представляет сокет сервера.
 * Он используется для создания серверных приложений, которые ожидают входящих соединений от клиентов.
 * <p>
 * Когда сервер запускается, он создает объект [ServerSocket],
 * который связывается с определенным портом на компьютере.
 * Затем сервер ожидает входящих соединений от клиентов.
 * Когда клиент устанавливает соединение с сервером, сервер принимает соединение
 * и создает новый объект Socket для обмена данными с клиентом.
 * <p>
 * "ServerSocket" предоставляет методы для прослушивания входящих соединений и управления серверным сокетом.
 * Кроме того, ServerSocket предоставляет ряд конструкторов и методов, которые позволяют
 * задать различные параметры сокета, такие как размер очереди входящих соединений и таймауты.
 * <p>
 * Метод public static void main(String[] args) - главный метод класса,
 * который запускает сервер на заданном порту и ожидает входящих подключений.
 * При получении нового соединения создает новый объект класса Handler и запускает его в новом потоке.
 *
 * Метод public static void sendBroadcastMessage(Message message) - метод для отправки сообщения всем клиентам,
 * используя их соединения из словаря connectionMap.
 *
 * Класс private static class Handler extends Thread - вложенный класс, который обрабатывает подключения клиентов.
 * Каждый объект класса Handler работает с одним клиентом и выполняет его регистрацию в чате,
 * обмен сообщениями и т.д. Каждый объект класса Handler работает в отдельном потоке.
 *
 * Метод private String chatRegistration(Connection connection) - метод для регистрации клиента в чате.
 * Метод запрашивает у клиента его имя и проверяет его на уникальность.
 * Если имя прошло проверку, клиент добавляется в словарь connectionMap.
 * Если имя не прошло проверку, клиенту возвращается запрос на ввод имени заново.
 *
 * Метод public static Map<String, Connection> connectionMap - статическое поле,
 * которое содержит соединения с клиентами в виде словаря,
 * где ключом является имя клиента, а значением - соединение с ним.
 *
 * Поле private static final Logger LOGGER - объект класса Logger,
 * который используется для логирования событий сервера в файл server.log.
 */