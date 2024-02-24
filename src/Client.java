import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 54320;

    public static void main(String[] args) {
        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);

            System.out.print("Mail Client starting on host: ");
            String clientHostName = scanner.nextLine();

            System.out.print("Type name of Mail server: ");
            String serverHostName = scanner.nextLine();

            // Send request to the server with email details
            String email = createEmail();
            byte[] sendData = email.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHostName);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            String confirmationMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + confirmationMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }

    private static String createEmail() {
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("Creating New Email..");
        System.out.print("To: ");
        String to = scanner.nextLine();
    
        System.out.print("From: ");
        String from = scanner.nextLine();
    
        System.out.print("Subject: ");
        String subject = scanner.nextLine();
    
        System.out.print("Body: ");
        String body = scanner.nextLine();
    
        // Use newline characters to separate fields
        return "TO: " + to + "\nFROM: " + from + "\nSUBJECT: " + subject + "\nBODY: " + body;
    }
    
}
