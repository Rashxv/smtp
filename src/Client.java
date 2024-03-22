import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String QUIT_COMMAND = "QUIT";

    public static void main(String[] args) throws IOException {
        // Set up client socket and scanner for user input
        DatagramSocket clientSocket = new DatagramSocket();
        Scanner scanner = new Scanner(System.in);

        // Display client host name
        System.out.print("Mail Client starting on host: ");
        String clientHostName = InetAddress.getLocalHost().getHostName();
        System.out.println(clientHostName);

        // Get mail server name from user
        String serverHostName = getValidHostName(scanner);

        // Get mail server port from user
        int serverPort = getValidPort(scanner);

        while (true) {
            // Create email
            String email = createEmail();
            if (email.equals(QUIT_COMMAND)) {
                System.out.println("Quitting the Mail Client.");
                break;
            }

            byte[] sendData = email.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHostName);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);

            // Send email to the server and wait for response
            clientSocket.send(sendPacket);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            // Display server response
            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + serverResponse);
        }

        // Close sockets and scanner
        clientSocket.close();
        scanner.close();
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

    // Create email by taking user input
    private static String createEmail() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Creating New Email..");

        // Switch the order, start with "From:"
        System.out.print("From: ");
        String from = scanner.nextLine();

        // Check for quit command
        if (from.equalsIgnoreCase(QUIT_COMMAND)) {
            return QUIT_COMMAND;
        }

        System.out.print("To: ");
        String to = scanner.nextLine();

        System.out.print("Subject: ");
        String subject = scanner.nextLine();

        System.out.print("Body: ");
        String body = scanner.nextLine();

        return "FROM: " + from + "\nTO: " + to + "\nSUBJECT: " + subject + "\n\n\n" + body;
    }
}
