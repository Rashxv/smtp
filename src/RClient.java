import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RClient {
    private static final String RECEIVER_INBOX_DIRECTORY = "C:\\Users\\USER\\Codes\\oop_codes\\smtp\\Receiver_Inbox";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (DatagramSocket clientSocket = new DatagramSocket(1235)) { // Listening on port 1235
            // Start listening for messages from the server
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Receive message from the server
                clientSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Check if the message contains an email
                if (message.startsWith("EMAIL")) {
                    // Extract email content and save to local directory
                    String emailContent = message.substring(6); // Skip "EMAIL" prefix
                    saveEmail(emailContent);
                    System.out.println("Received email saved to local directory.");
                }

                // Clear the buffer for the next message
                receiveData = new byte[1024];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEmail(String email) {
        try {
            // Save email to local directory
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = RECEIVER_INBOX_DIRECTORY + "\\email_" + timestamp + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println(email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


/*
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RClient {
    private static final String RECEIVER_INBOX_DIRECTORY = "C:\\Users\\USER\\Codes\\oop_codes\\smtp\\Receiver_Inbox";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            // Get mail server name from user
            String serverHost = getValidHostName(scanner);

            // Get mail server port from user
            int serverPort = getValidPort(scanner);

            // Create a socket address for the server
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            SocketAddress serverSocketAddress = new InetSocketAddress(serverAddress, serverPort);

            // Start listening for messages from the server
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Receive message from the server
                clientSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Check if the message contains an email
                if (message.startsWith("EMAIL")) {
                    // Extract email content and save to local directory
                    String emailContent = message.substring(6); // Skip "EMAIL" prefix
                    saveEmail(emailContent);
                    System.out.println("Received email saved to local directory.");
                }

                // Clear the buffer for the next message
                receiveData = new byte[1024];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEmail(String email) {
        try {
            // Save email to local directory
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = RECEIVER_INBOX_DIRECTORY + "\\email_" + timestamp + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println(email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get a valid host name from the user
    private static String getValidHostName(Scanner scanner) {
        String serverHostName = "";
        boolean validHostName = false;
        while (!validHostName) {
            try {
                System.out.print("Type name of Mail server: ");
                serverHostName = scanner.nextLine();
                InetAddress.getByName(serverHostName); // Attempt to resolve the host name
                validHostName = true; // If no exception is thrown, the host name is valid
            } catch (UnknownHostException e) {
                System.out.println("Host does not exist. Please enter a valid host name.");
            }
        }
        return serverHostName;
    }

    // Get a valid port number from the user
    private static int getValidPort(Scanner scanner) {
        int port = 0;
        boolean validPort = false;
        while (!validPort) {
            try {
                System.out.print("Enter the port number: ");
                port = Integer.parseInt(scanner.nextLine());
                if (port > 0 && port <= 65535) {
                    validPort = true;
                } else {
                    System.out.println("Port number must be between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Please enter a valid integer.");
            }
        }
        return port;
    }
}
*/