package server;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {

    public static final int PORT = 12345;
    public static final String KEY_APPENDIX = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static void main(String[] args) throws IOException {
        int port = PORT;
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Waiting for a client... ");

        while (true) {
            Socket socket = serverSocket.accept();
//            socket.setSoTimeout(20);

            Handler handler = new Handler(socket);
            handler.start();
        }


    }

    private static class Handler extends Thread {
        private Socket socket;
        private InputStream in;
        private OutputStream out;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Map<String, String> requsetHeaders;
        private Map<String, String> responseHeaders;

        public Handler(Socket socket) throws IOException {

            this.socket = socket;
            System.out.println("Got a client!");
            System.out.println();

            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();

            dis = new DataInputStream(in);
            dos = new DataOutputStream(out);
        }

        @Override
        public synchronized void start() {
            handShake();
            super.start();
        }

        @Override
        public void run() {
//            messageProcessing();
            try {
                readInputHeaders();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }

        private void handShake() {
            try {
                readInputHeaders();
                System.out.println("\n=====================================\n");
//                requsetHeaders.forEach((String key, String value) -> System.out.println(key + (value == null ? "" : ": " + value)));
                out.write(getResponse().getBytes());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        private void messageProcessing() {
            String message = null;
            try {
                while (true) {
                    // ждем сообщение от клиента
                    message = dis.readUTF();

                    System.out.println("Message:");
                    System.out.println("========================================================");
                    System.out.println(message);
                    System.out.println("========================================================");

                    // отвечаем клиенту
                    dos.writeUTF("Answer: " + message);
                    dos.flush();

                    if ("exit".equalsIgnoreCase(message)) {
                        socket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getResponse() {
            return getResponse(null);
        }

        private String getResponse(String message) {
            fillResponseHeaders(message);
            StringBuilder builder = new StringBuilder();
            responseHeaders.forEach((key, value) -> builder.append(key + (value == null ? "" : ": " + value) + "\r\n"));

            builder.append("\r\n\r\n");
            if (message != null) {
                builder.append(message);
            }

            System.out.println(builder);

            return builder.toString();
        }

        private void readInputHeaders() throws Throwable {
            requsetHeaders = new LinkedHashMap<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while(true) {
                String s = br.readLine();
                if(s == null || s.trim().length() == 0) {
                    break;
                } else {
                    String[] pair = getHeaderAndValue(s);
                    requsetHeaders.put(pair[0], pair[1]);
                    System.out.println(s);
                }
            }
        }

        private String[] getHeaderAndValue(String s) {
            String[] result = new String[2];
            int pos = s.indexOf(": ");
            if (pos > 0) {
                result[0] = s.substring(0, pos);
                result[1] = s.substring(pos + 2, s.length());
            } else {
                if (s.startsWith("GET")) {
                    /*requsetHeaders.put("Method", "GET");
                    requsetHeaders.put("Protocol", "HTTP/1.1");*/
                    result[0] = s;
                }
            }

            return result;
        }

        private void fillResponseHeaders() {
            fillResponseHeaders(null);
        };

        private void fillResponseHeaders(String message) {
            responseHeaders = new LinkedHashMap<>();
            if (requsetHeaders.containsKey("Upgrade")) {
                responseHeaders.put("HTTP/1.1 101 Switching Protocols", null);
                responseHeaders.put("Upgrade", "websocket");
                responseHeaders.put("Connection", "Upgrade");
                String key = requsetHeaders.get("Sec-WebSocket-Key");
                responseHeaders.put("Sec-WebSocket-Accept", getAcceptKey(key));
            } else {
                responseHeaders.put("HTTP/1.1 200 OK", "");
                responseHeaders.put("Server", "Inform-Systema");
                responseHeaders.put("Content-Type", "text/html");
                responseHeaders.put("Content-Length", (message != null ? String.valueOf(message.length()) : "0"));
                responseHeaders.put("", "");
                responseHeaders.put("Connection", "close");
            }
        }

        private String getAcceptKey(String key) {
            key += KEY_APPENDIX;
            byte[] SHA1 = DigestUtils.sha1(key);
            String base64 = Base64.getEncoder().encodeToString(SHA1);

            return base64;
        }

    }
}
