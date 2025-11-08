import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticator {
    private static final String USER_FILE = "users.txt";
    public static final String DEFAULT_MOOD = "N/A";
    public static final String DEFAULT_TIME = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 10); // Date only

    /**
     * Helper class to manage user data loaded from file.
     */
    public static class UserData {
        String username;
        String password;
        String lastMood;
        String lastTimestamp;

        public UserData(String username, String password, String lastMood, String lastTimestamp) {
            this.username = username;
            this.password = password;
            this.lastMood = lastMood;
            this.lastTimestamp = lastTimestamp;
        }
    }

    public static void createUser(String username, String password) throws IOException {
        String data = username + "," + password + "," + DEFAULT_MOOD + "," + DEFAULT_TIME + "\n";
        Files.write(Paths.get(USER_FILE), data.getBytes(), 
                        java.nio.file.StandardOpenOption.CREATE, 
                        java.nio.file.StandardOpenOption.APPEND);
    }

    public static boolean authenticate(String username, String password) {
        try {
            if (!Files.exists(Paths.get(USER_FILE))) {
                createUser("admin", "password"); 
            }
            
            List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
            String target = username + "," + password;
            
            for (String line : lines) {
                if (line.startsWith(target + ",")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error accessing user file: " + e.getMessage());
            return false;
        }
    }
    
    public static UserData getUserData(String username) throws IOException {
         if (!Files.exists(Paths.get(USER_FILE))) return null;

         List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
         for (String line : lines) {
             String[] parts = line.split(",", 4); 
             if (parts.length >= 1 && parts[0].equals(username)) {
                 String password = parts.length > 1 ? parts[1] : "";
                 String mood = parts.length > 2 ? parts[2] : DEFAULT_MOOD;
                 String timestamp = parts.length > 3 ? parts[3] : DEFAULT_TIME;
                 return new UserData(parts[0], password, mood, timestamp);
             }
         }
         return null; 
    }
    
    public static void saveLastMood(String username, String mood, String timestamp) throws IOException {
        Path filePath = Paths.get(USER_FILE);
        if (!Files.exists(filePath)) return;

        List<String> lines = Files.readAllLines(filePath);
        List<String> newLines = new ArrayList<>();
        
        boolean updated = false;
        for (String line : lines) {
            if (line.startsWith(username + ",")) {
                String[] parts = line.split(",", 2); 
                String password = parts.length > 1 ? parts[1].split(",", 2)[0] : ""; 
                
                String updatedLine = parts[0] + "," + password + "," + mood + "," + timestamp;
                newLines.add(updatedLine);
                updated = true;
            } else {
                newLines.add(line);
            }
        }
        
        if (updated) {
             Files.write(filePath, newLines, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}