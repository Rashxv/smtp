import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.time.LocalDateTime;
import java.util.HashSet;

public class Server {

    private static final int SERVER_PORT = 54320;
    private static final String SAVE_DIRECTORY = "C:\\Users\\USER\\Codes\\oop_codes\\smtp\\emails\\";
    private static final HashSet<String> VALID_EMAILS = new HashSet<>(); // Add your valid email addresses here

    static {
        // Add your valid email addresses to the set
        VALID_EMAILS.add("r@kok.com");
        VALID_EMAILS.add("a@kok.com");
        VALID_EMAILS.add("s@kok.com");
        VALID_EMAILS.add("$a@kok.com");
    }

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
        System.out.println("Mail Server Starting at host: " + InetAddress.getLocalHost().getHostName());
        System.out.println("Waiting to be contacted for transferring Mail...");

        while (true) {
            try {
                // Receive email
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String email = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Validate email
                if (isValidEmail(extractToAddress(email))) {
                    String toAddress = extractToAddress(email);
                    if (isValidRecipient(toAddress)) {
                        // Process and save email
                        processEmail(email, receivePacket.getAddress(), receivePacket.getPort());
                        sendConfirmationMessage(receivePacket.getAddress(), receivePacket.getPort(), true);
                    } else {
                        sendConfirmationMessage(receivePacket.getAddress(), receivePacket.getPort(), false,
                                "Recipient email not found.");
                    }
                } else {
                    sendConfirmationMessage(receivePacket.getAddress(), receivePacket.getPort(), false,
                            "Invalid email format.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processEmail(String email, InetAddress clientAddress, int clientPort) throws IOException {
        // Extract header and body (assuming simple format)
        String[] parts = email.split("\n\n", 2);
        String header = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        // Save email to file
        String timestamp = LocalDateTime.now().toString().replace(":", "-").replace(".", "-");
        String fileName = SAVE_DIRECTORY + "email_" + timestamp + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(header);
            writer.println(body);
        }

        System.out.println("Email saved to file: " + fileName);
    }

    private static void sendConfirmationMessage(InetAddress clientAddress, int clientPort, boolean success,
            String... errorMessages) throws IOException {
        String message;
        if (success) {
            message = "250 OK\nEmail received successfully at " + LocalDateTime.now().toString();
        } else {
            StringBuilder errorMessageBuilder = new StringBuilder("501 Error\n");
            for (String errorMessage : errorMessages) {
                errorMessageBuilder.append(errorMessage).append("\n");
            }
            message = errorMessageBuilder.toString();
        }

        byte[] sendData = message.getBytes();
        DatagramSocket replySocket = new DatagramSocket();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        replySocket.send(sendPacket);
        replySocket.close();
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Split using "@" symbol to separate username and domain parts
        String[] parts = email.split("@", 2);
        if (parts.length != 2) {
            return false; // Must have exactly one "@" symbol
        }
         
        // Check username validity (allow letters, numbers, periods, hyphens, and underscores)
        String username = parts[0];
        if (!username.matches("^[\\w\\.-]+$")) {
            return false;
        }

        // Check domain validity (allow letters, numbers, hyphens, and periods)
        String domain = parts[1];
        if (!domain.matches("^[\\w\\.-]+$")) {
            return false;
        }
        
        return true;
    }
    

    private static String extractToAddress(String email) {
        // Assuming simple format, extract the first line after "TO:"
        String[] lines = email.split("\n");
        for (String line : lines) {
            if (line.startsWith("TO:")) {
                return line.substring(3).trim();
            }
        }
        return null;
    }
    
    

    private static boolean isValidRecipient(String email) {
        return VALID_EMAILS.contains(email);
    }
}
