package main;

import java.util.Scanner;
import server.Server;
import client.Client;

/**
 * Стартовая точка программы.
 * Просит пользователя выбрать режим работы программы (сервер или клиент)
 * и передаёт управление соответствующему классу
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Запустить программу в режиме сервера или клиента? (S(Server) / C(Client))");
        while (true) {
            char answer = Character.toLowerCase(scanner.nextLine().charAt(0));
            if (answer == 's') {
                new Server();
                break;
            } else if (answer == 'c') {
                new Client();
                break;
            } else {
                System.out.println("Некорректный ввод. Повторите ещё раз.");
            }
        }
    }

}