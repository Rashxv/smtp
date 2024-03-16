import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    // Constants
    private static final int SERVER_PORT = 54;
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
        System.out.print("Type name of Mail server: ");
        String serverHostName = scanner.nextLine();

        while (true) {
            // Create email
            String email = createEmail();
            if (email.equals(QUIT_COMMAND)) {
                System.out.println("Quitting the Mail Client.");
                break;
            }

            byte[] sendData = email.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHostName);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);

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
