package message_history;

import ConnectionService.ConnectionService;
import java.util.LinkedList;

/**
 * class MessageHistory отвечает за сохранение сообщений и
 * последовательную отсылку 10-ти последних сообщений из чата
 * вновь подключившемуся пользователю.
 */

public class MessageHistory {

    private static final LinkedList<String> messHistory = new LinkedList<>();

    public static void addHistoryEl(String el) {
        if (messHistory.size() >= 10) {
            messHistory.removeFirst();
        }
        messHistory.add(el);

    }

    public static void printMessHistory(ConnectionService connectionService) {
        if (messHistory.size() > 0) {
            connectionService.sendMessage("История 10 последних сообщений сообщений в чате:");
            for (String message : messHistory) {
                connectionService.sendMessage(message);
            }
            connectionService.sendMessage("/...конец истории сообщений.../");
        }
    }
}
