package db;

/**
 * class для работы с базой данных.
 * Подключается к БД и использует методы для работы с БД.
 * С помощью методода "createUser" добавляет пользователя в БД.
 * С помощью методода "deleteUser" удаляет пользователя из БД.
 * С помощью методода "createMessage" добавляет сообщение в БД.
 * С помощью методода "deleteMessage" удаляет сообщение из БД.
 */

import ConnectionService.ConnectionService;
import java.sql.*;

public class DatabaseService {

    final static String URL = "jdbc:mysql://127.0.0.1:3306/chatdb" +
            "?serverTimezone=Europe/Moscow&useSSL=false";

    final static String LOGIN = "root";
    final static String PASSWORD = "depeche150580";
    private static Connection connection;

    private static Connection getConnection() {
        if(connection != null){
            return connection;
        }
        try {
            connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean createUser(User user){
        boolean result = false;
        try {
            String commandText = "INSERT INTO chatusers (name, dateandtime) VALUES(?,?)";
            PreparedStatement preparedStatement = getConnection().prepareStatement(commandText);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getDateandTime());
            result = preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public static boolean deleteUser(User user){
        boolean result = false;
        try {
            String commandText = "DELETE FROM chatusers WHERE name = ?";
            PreparedStatement preparedStatement = getConnection().prepareStatement(commandText);
            preparedStatement.setString(1, user.getName());
            result = preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public static boolean createMessage(ConnectionService connectionService, String message){
        boolean result = false;
        try {
            String commandText = "INSERT INTO messagehistory (name, message, dateandtime) VALUES(?,?,?)";
            PreparedStatement preparedStatement = getConnection().prepareStatement(commandText);
            preparedStatement.setString(1, connectionService.getUserName());
            preparedStatement.setString(2, message);
            preparedStatement.setString(3, connectionService.currentDateAndTime());
            result = preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return result;
    }

    public static boolean deleteMessage(User user){
        boolean result = false;
        try {
            String commandText = "DELETE FROM messagehistory WHERE name = ?";
            PreparedStatement preparedStatement = getConnection().prepareStatement(commandText);
            preparedStatement.setString(1, user.getName());
            result = preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }
}
