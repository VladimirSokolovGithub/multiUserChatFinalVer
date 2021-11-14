package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import static ConnectionService.Constants.*;

/**
 * class Client обеспечивает работу программы в режиме клиента, запрашивает у пользователя
 * имя и организовывает обмен сообщениями с сервером.
 *
 * При запуске клиента запускается конструктор класса, с помощью класса Scanner считывает сообщения с консоли.
 * Создаётся сокет со стороны клиента и потоки ввода - вывода. При подключении пользователь вводит своё имя.
 * При помощи внутреннего класса "MessageResender" запускается вывод всех входящих сообщений в консоль,
 * до тех пор пока пользователь не введет служебное сообщение "exit" для выхода из чата.
 */

public class Client {

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String name;

    public Client() {
        Scanner scan = new Scanner(System.in);
        String ip = "127.0.0.1";
        try {
            socket = new Socket(ip, PORT); //формируем сокет со стороны клиента
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Введите свой ник:");
            name = scan.nextLine();
            out.println(name);

            // Запускает вывод всех входящих сообщений в консоль
            MessageResender messageResender = new MessageResender();
            messageResender.start();

            while (true) {
                String message = scan.nextLine();
                out.println(message);
                if(message.equals(EXIT_CODE)){
                    break;
                }
            }

            messageResender.setStop();
            scan.close();
            close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Закрывает входной и выходной потоки и сокет
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("Потоки не были закрыты!");
        }
    }

    //Внутренний класс клиента. Считывает в отдельном потоке все сообщения от сервера и печатает их в консоль.
    public class MessageResender extends Thread {

        private boolean stoped;

        public synchronized void setStop() {
            stoped = true;
            System.out.println(name + " Вы вышли из чата");
        }

        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при получении сообщения.");
                e.printStackTrace();
            }
        }
    }
}
