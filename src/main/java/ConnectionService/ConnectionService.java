package ConnectionService;

import server.Server;
import static ConnectionService.Constants.EXIT_CODE;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class отвечает за обслуживание клиента сервером.
 * У класса есть метод "sendMessage", отвечающий за отправку сообщений пользователей и метод
 * "currentDateAndTime" возвращающий текущую дату и время.
 *
 * Когда запускается Server вызывается конструктор класса.
 * В конструктор передаётся два объекта eventListener и socket (предоставляется сервером).
 * В конструкторе инициализируются поля eventListener и socket, создаются потоки ввода - вывода,
 * запускается поток, который при помощи метода "onConnectionReady" добавляет текущее подключение
 * в синхронизированный список подключений, если пользователей больше чем один, отправляет вновь
 * подключившемуся пользователю список пользователей которые сейчас on-line. Новый пользователь
 * добавляется в базу данных, сервер выводит сообщение о вновь подключившемся пользователе. Всем
 * пользователям включая подключившегося отправляется служебное сообщение о том что пользователь
 * зашёл в чат. Отправляет новому пользователю историю переписки пользователей(последние 10 сообщений).
 * Далее пока пользователь не введет служебное сообщение "exit" для выхода из чата, ожидается
 * входящее сообщение от клиента, сервер ведет log журнал и фиксирует время отправляемых сообщений.
 *
 * При вводе служебного сообщения "exit" отрабатывает метод "onDisconnect", который отвечает за удаление
 * текущего подключения из списка подключений, Server выводит служебное сообщение об отключении пользователя,
 * всем пользователям отправляется сообщение что пользователь выходит из чата. Пользователь и все его сообщения
 * удаляются из базы данных.
 */

public class ConnectionService extends Thread {

    private String name;
    private final Socket socket; //сокет клиента
    private final Server eventListener; //слушатель подключений
    private final BufferedReader in;
    private final PrintWriter out;

    public ConnectionService(Server eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        start();
    }

    @Override
    public void run() {
        try {
            name = in.readLine();
            eventListener.onConnectionReady(ConnectionService.this);
            while (true) {
                String message = in.readLine();
                eventListener.onReceiveString(ConnectionService.this, message);
                System.out.println("logging: " + "пользователь " + ConnectionService.this.getUserName() +
                        " отправил сообщение - " +  "\"" + message + "\"" + " * " + currentDateAndTime());
                if(message.equals(EXIT_CODE)){
                    break;
                }
            }
            eventListener.onDisconnect(ConnectionService.this);
        } catch (IOException e) {
            eventListener.onException(ConnectionService.this, e);
        } finally {
            close();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Потоки не были закрыты!");
        }
    }

    public synchronized void sendMessage(String message) {
        out.println(message);
    }

    public String getUserName() {
        return name;
    }

    public String currentDateAndTime() {
        Date date = new Date(); // текущая дата
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }
}

