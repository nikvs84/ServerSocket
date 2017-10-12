package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        int serverPort = SERVER_PORT;
        String address = "127.0.0.1";

        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(address);
            Socket socket = new Socket(ipAddress, serverPort);

            System.out.println("Connect!");

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // конвертируем потоки для удобства работы
            DataInputStream dis = new DataInputStream(is);
            DataOutputStream dos = new DataOutputStream(os);

            System.out.println();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String message = reader.readLine();

                    // посылаем сообщение серверу
                    dos.writeUTF(message);
                    dos.flush();

                    // условие завершения работы клиента и сервера
                    if ("exit".equalsIgnoreCase(message)) {
                        socket.close();
                        break;
                    } else {
                        // ждем ответа от сервера
                        message = dis.readUTF();

                        System.out.println("Ansver:");
                        System.out.println("========================================================");
                        System.out.println(message);
                        System.out.println("========================================================");
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
