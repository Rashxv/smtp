import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMTPServer {
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(12345);
            byte[] receiveData = new byte[1024];

            System.out.println("Mail Server Starting...");

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Assuming message format: "TO: [toEmail] FROM: [fromEmail] SUBJECT: [subject] BODY: [body]"
                Pattern pattern = Pattern.compile("TO: (.+) FROM: (.+) SUBJECT: (.+) BODY: (.+)");
                Matcher matcher = pattern.matcher(message);

                if (matcher.find()) {
                    String toEmail = matcher.group(1);
                    String fromEmail = matcher.group(2);
                    String subject = matcher.group(3);
                    String body = matcher.group(4);

                    // Perform email validation here, for simplicity, just check for email format
                    if (isValidEmail(toEmail) && isValidEmail(fromEmail)) {
                        // Save email to a local directory (you need to implement this)
                        saveEmailToFile(fromEmail, toEmail, subject, body);

                        // Send confirmation message with timestamp
                        String timestamp = LocalDateTime.now().toString();
                        InetAddress clientAddress = receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();
                        String confirmationMessage = "250 OK\nTimestamp: " + timestamp;
                        byte[] sendData = confirmationMessage.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);

                        System.out.println("Email received successfully from " + fromEmail + " to " + toEmail);
                    } else {
                        // Invalid email addresses
                        sendError(serverSocket, receivePacket.getAddress(), receivePacket.getPort());
                    }
                } else {
                    // Invalid request format
                    sendError(serverSocket, receivePacket.getAddress(), receivePacket.getPort());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    private static void sendError(DatagramSocket socket, InetAddress clientAddress, int clientPort) throws IOException {
        // Send error message with timestamp
        String timestamp = LocalDateTime.now().toString();
        String errorMessage = "501 Error\nTimestamp: " + timestamp;
        byte[] sendData = errorMessage.getBytes();

        DatagramPacket errorPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        socket.send(errorPacket);

        System.out.println("Error: Invalid request or email addresses");
    }

    private static boolean isValidEmail(String email) {
        // Implement email validation logic (simplified for demonstration)
        return email.matches(".+@.+\\..+");
    }

    private static void saveEmailToFile(String from, String to, String subject, String body) {
        // Implement saving email to a local directory
        // You may need to create a folder for each user to store their emails
        // For simplicity, you can use a basic text file or a database to store emails
        // Make sure to handle file I/O operations carefully (exception handling, etc.)
    }
}
