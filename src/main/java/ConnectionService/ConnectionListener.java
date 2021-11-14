package ConnectionService;

/**
 * interface ConnectionListener объявляет методы для работы с подключениями клиентов.
 * Входные параметры методов это подключение с которым ведется работа.
 */

public interface ConnectionListener {

    //что делать когда подключение установлено
    void onConnectionReady(ConnectionService connectionService);

    //что делать когда получено сообщение
    void onReceiveString(ConnectionService connectionService, String message);

    //что делать когда происходит отключение
    void onDisconnect(ConnectionService connectionService);

    //что делать когда возникает ошибка
    void onException(ConnectionService connectionService, Exception e);

}