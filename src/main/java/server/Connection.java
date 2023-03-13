package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket; // Socket позволяет устанавливать соединения через протоколы TCP/IP или UDP/IP.
    private final ObjectOutputStream out; // Поток вывода объекта.
    private final ObjectInputStream in; // Поток ввода объекта.


    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException { // Отправка сообщения.
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {  // Чтение сообщения.
        synchronized (in) {
            return (Message) in.readObject();
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress(); // возвращает удаленный адрес сокетного соединения
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}


/**
 *
 *   Этот класс представляет собой установленное сетевое соединение между двумя узлами,представленными сокетами.
 *   Это может быть клиент-серверное соединение, где сервер слушает и принимает соединения от клиентов,
 *   или соединение между двумя клиентами.
 *
 *   Конструктор класса 'server.Connection' принимает сокет в качестве параметра и инициализирует три поля класса:
 *   "socket", "out" и "in".
 *   [socket] - используется для управления сетевым соединением.
 *   [out и in] - используются для записи и чтения данных через сокет.
 *
 *                                         ***
 *
 *   Метод "send" используется для отправки сообщения через сокет.
 *   отправляет сообщение (server.Message) в исходящий поток данных (ObjectOutputStream),
 *   который был создан в конструкторе класса server.Connection.
 *
 *   Метод send() использует блокировку синхронизации на объекте out, чтобы гарантировать,
 *   что только один поток будет записывать данные в этот поток данных в любой момент времени.
 *   Это важно, чтобы избежать ошибок, связанных с одновременной записью в один и тот же поток данных.
 *
 *   Метод send() может выбрасывать исключение IOException, которое может возникнуть
 *   в процессе записи данных в исходящий поток данных.
 *   IOException обычно возникает при ошибках ввода-вывода, таких как разрыв соединения.
 *
 *                                         ***
 *
 *   Метод "receive" используется для чтения сообщения из входного потока [in].
 *   Он позволяет получить сообщение (server.Message) из входящего потока данных (ObjectInputStream),
 *   который был создан в конструкторе класса server.Connection.
 *
 *   Метод receive() использует блокировку синхронизации на объекте in, что гарантирует,
 *   что только один поток будет читать данные из этого потока данных в любой момент времени.
 *   Это важно, чтобы избежать ошибок, связанных с одновременным чтением и записью в один и тот же поток данных.
 *
 *   Метод receive() может выбрасывать исключения IOException и ClassNotFoundException,
 *   которые могут возникнуть в процессе чтения данных из входящего потока данных.
 *   IOException обычно возникает при ошибках ввода-вывода, таких как разрыв соединения,
 *   а ClassNotFoundException возникает, если класс server.Message не может быть найден при десериализации объекта.
 *
 *                                          ***
 *
 *   Метод "getRemoteSocketAddress" возвращает удаленный адрес сокетного соединения.
 *   Этот метод полезен, если необходимо узнать адрес удаленного узла, с которым происходит соединение.
 *
 *                                           ***
 *
 *   Метод "close" используется для закрытия сетевого соединения.
 *   Он закрывает входной и выходной потоки, а затем закрывает сокет.
 *   Это очистит ресурсы, занимаемые сетевым соединением, и освободит их для использования другими приложениями.
 *   Класс "server.Connection" реализует интерфейс "Closeable",
 *   который позволяет использовать этот класс в блоке [try-with-resources]
 *   для автоматического закрытия соединения при выходе из блока.
 *
 */