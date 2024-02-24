import java.io.*;
import java.net.*;
import java.time.LocalDateTime;

public class Server {
    private static final String SERVER_HOST_NAME = "localhost";
    private static final int SERVER_PORT = 54320;
    private static final String SAVE_DIRECTORY = "C:\\Users\\USER\\Codes\\oop_codes\\smtp\\emails\\";

    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            byte[] receiveData = new byte[1024];

            System.out.println("Mail Server Starting at host: " + SERVER_HOST_NAME);
            System.out.println("Waiting to be contacted for transferring Mail...");

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                processEmail(message, receivePacket.getAddress(), receivePacket.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    private static void processEmail(String email, InetAddress clientAddress, int clientPort) {
        // Validate TO field (assumed simple email format check)
        if (isValidEmail(email)) {
            // Save email to a local directory
            saveEmailToFile(email);
    
            // Reply with confirmation message and timestamp
            String timestamp = LocalDateTime.now().toString();
            String confirmationMessage = "250 OK\n" + "Email received successfully at " + timestamp;
            byte[] sendData = confirmationMessage.getBytes();
    
            DatagramSocket replySocket = null;
            try {
                replySocket = new DatagramSocket();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                replySocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (replySocket != null && !replySocket.isClosed()) {
                    replySocket.close();
                }
            }
        } else {
            // Reply with error message and timestamp
            String timestamp = LocalDateTime.now().toString();
            String errorMessage = "501 Error\n" + "Invalid email address at " + timestamp;
            byte[] sendData = errorMessage.getBytes();
    
            DatagramSocket replySocket = null;
            try {
                replySocket = new DatagramSocket();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                replySocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (replySocket != null && !replySocket.isClosed()) {
                    replySocket.close();
                }
            }
        }
    }
    
    private static boolean isValidEmail(String email) {
        // Assume any non-empty string is a valid email
        return email != null;
    }
    

    private static void saveEmailToFile(String email) {
        try {
            String timestamp = LocalDateTime.now().toString().replace(":", "-").replace(".", "-");
            String fileName = SAVE_DIRECTORY + "email_" + timestamp + ".txt";

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println(email);
            }

            System.out.println("Email saved to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
