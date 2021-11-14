package server;

import ConnectionService.ConnectionService;
import db.DatabaseService;
import db.User;
import ConnectionService.ConnectionListener;
import ConnectionService.Constants;
import message_history.MessageHistory;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.*;
import static ConnectionService.Constants.*;

/**
 * class Server implements ConnectionListener обеспечивает работу программы в режиме сервера и
 * реализует методы интерфейса ConnectionListener.
 * Создаёт синхронизированный список в котором будут храниться наши подключения клиентов (наши потоки).
 * При запуске сервера, конструктор создаёт сервер с ранее определённым портом в качестве параметра.
 * Затем для каждого подключения создаётся объект "ConnectionService", выполняется конструктор класса
 * ConnectionService, создаётся socket и запускается в отдельном потоке.
 * В переопределенных методах интерфейса ConnectionListener используем ключевое слово synchronized, которое
 * позволяет обеспечить согласованность между потоками. Это значит, что блок может выполняться
 * только одним потоком одновременно.
 * Так же class содержит перегруженный метод "sendMessage" отвечающий за отправку служебных сообщений о
 * подключении, отключении пользователей и сообщений пользователей. + методы "currentDateAndTime", возвращающий
 * текущую дату и время и метод "userStatus", сообщающий вновь подключившемуся пользователю о том, какие
 * пользователи сейчас on-line.
 */

public class Server implements ConnectionListener {

    private final List<ConnectionService> connectionServices = Collections.synchronizedList(new ArrayList<>());
    private final ServerSocket server;
    private final ArrayList<User> users = new ArrayList<>();
    private User user;

    public Server() {
        System.out.println("Сервер запущен");
        try {
            server = new ServerSocket(Constants.PORT);
            while (true) {
                try {
                    new ConnectionService(this, server.accept());
                } catch (IOException e) {
                    System.out.println("Ошибка соединения " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            serverClose();
        }
    }

    private void serverClose() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //далее идёт реализация всех методов интерфейса ConnectionListener
    @Override
    public synchronized void onConnectionReady(ConnectionService connectionService) {
        connectionServices.add(connectionService);
        userStatus(connectionService);
        user = new User(connectionService.getUserName(), currentDateAndTime());
        DatabaseService.createUser(user);
        users.add(user);

        System.out.println(ANSI_YELLOW + "(" + currentDateAndTime() + ") " + "Подключение пользователя: " +
                connectionService.getUserName() + ANSI_RESET);
        sendMessage(ANSI_YELLOW + "(" + currentDateAndTime() + ") " + connectionService.getUserName() +
                " - заходит в чат" + ANSI_RESET);
        MessageHistory.printMessHistory(connectionService);
    }

    @Override
    public synchronized void onReceiveString(ConnectionService connectionService, String message) {
        sendMessage(connectionService, message);
        DatabaseService.createMessage(connectionService, message);
    }

    @Override
    public synchronized void onDisconnect(ConnectionService connectionService) {
        connectionServices.remove(connectionService);
        System.out.println(ANSI_RED + "(" + currentDateAndTime() + ") " + "Отключение пользователя: " +
                connectionService.getUserName() + ANSI_RESET);
        sendMessage(ANSI_RED + "(" + currentDateAndTime() + ") " + connectionService.getUserName() +
                " - выходит из чата" + ANSI_RESET);

        for (User user : users) {
            if (user.getName().equals(connectionService.getUserName())) {
                DatabaseService.deleteMessage(user);
                DatabaseService.deleteUser(user);
                System.out.println(ANSI_RED + "пользователь: " + user.getName() + " удален из базы данных" + ANSI_RESET);
            }
        }
    }

    @Override
    public synchronized void onException(ConnectionService connectionService, Exception e) {
        System.out.println("Ошибка соединения: " + e);
    }


    private synchronized void sendMessage(String message) {
        Iterator<ConnectionService> connectionIterator = connectionServices.iterator();
        while (connectionIterator.hasNext()) {
            connectionIterator.next().sendMessage(message);
        }
    }

    private synchronized void sendMessage(ConnectionService connectionService, String message) {
        String finalMessage = (ANSI_YELLOW + "(" + currentDateAndTime() + ") " + ANSI_RESET + ANSI_RED +
                connectionService.getUserName() + ANSI_RESET + ": " + message);
        MessageHistory.addHistoryEl(finalMessage);
        Iterator<ConnectionService> connectionIterator = connectionServices.iterator();
        while (connectionIterator.hasNext()) {
            ConnectionService nextConnectionService = connectionIterator.next();
            if (!connectionService.equals(nextConnectionService))
                nextConnectionService.sendMessage(finalMessage);
        }
    }

    public String currentDateAndTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }

    public void userStatus(ConnectionService connectionService) {
        if (connectionServices.size() > 1) {
            connectionService.sendMessage("Сейчас пользователи он лайн: ");
            Iterator<ConnectionService> connectionIterator = connectionServices.iterator();
            while (connectionIterator.hasNext()) {
                ConnectionService nextConnectionService = connectionIterator.next();
                if (!connectionService.equals(nextConnectionService)) {
                    connectionService.sendMessage(nextConnectionService.getUserName());
                }
            }
        }
    }
}