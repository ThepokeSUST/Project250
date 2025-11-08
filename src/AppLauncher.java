
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils; // Required for Mat to Image conversion
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task; // For non-blocking analysis

import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class AppLauncher extends Application {

    // --- OpenCV Configuration Paths (Defined in MoodDetector, repeated here for utility class access) ---
    private static final String FACE_PROTO = "deploy.prototxt"; 
    private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
    private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
    private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

    // --- Application State and Components ---
    private Stage primaryStage;
    private VideoCapture camera;
    private ImageView imageView;
    private Label moodLabel;
    private volatile boolean showingLiveFeed = false;
    private Thread cameraThread; 
    private Mat lastCapturedFrame = null;
    
    // Non-static field to track current user
    private String currentUsername = null; 
    
    // --- Mood Tracking Components ---
    // Record to hold mood data
    private static record MoodEntry(String mood, LocalDateTime timestamp) {
        @Override
        public String toString() {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | Mood: " + mood;
        }
    }
    
    // ObservableList for JavaFX ListView
    private ObservableList<MoodEntry> moodHistory = FXCollections.observableArrayList();
    private ListView<MoodEntry> historyListView;
    // ------------------------------------

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV library check passed.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV library. Ensure the native libraries are correctly configured in your system path.");
            return; 
        }

        primaryStage.setOnCloseRequest(event -> {
            stopLiveFeedAndReleaseCamera();
            if (currentUsername != null) {
                // Attempt to save the last known mood before closing
                String lastMood = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_MOOD : moodHistory.get(0).mood();
                String lastTime = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_TIME : moodHistory.get(0).timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                try {
                    UserAuthenticator.saveLastMood(currentUsername, lastMood, lastTime);
                } catch (IOException ignored) {}
            }
        });

        showLoginScene();
        primaryStage.show();
    }
    
    private void stopLiveFeedAndReleaseCamera() {
        showingLiveFeed = false;
        if (cameraThread != null && cameraThread.isAlive()) {
            try {
                cameraThread.interrupt(); 
                cameraThread.join(100); 
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
            camera = null; 
        }
    }

    // -----------------------------------------------------------------
    // ## 1. Login Scene
    // -----------------------------------------------------------------
    private void showLoginScene() {
        stopLiveFeedAndReleaseCamera();

        Label title = new Label("Mood Scanner Login 👤");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: white;");
        TextField userField = new TextField();
        userField.setPromptText("Enter Username");
        
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: white;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter Password");
        
        Button loginBtn = new Button("🔑 Login");
        loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
        Button signupBtn = new Button("✍️ Create Account");
        signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
        Label messageLabel = new Label("Welcome! (Default: admin/password)");
        messageLabel.setStyle("-fx-text-fill: #00BCD4;");

        // Layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        
        grid.add(userLabel, 0, 1);
        grid.add(userField, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(passField, 1, 2);

        HBox buttonBar = new HBox(15, loginBtn, signupBtn);
        buttonBar.setAlignment(Pos.CENTER);

        VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
        Scene scene = new Scene(root, 450, 400); 
        
        primaryStage.setTitle("Login Required");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene(); 
        primaryStage.centerOnScreen();
        
        // Login Logic
        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (UserAuthenticator.authenticate(username, password)) {
                messageLabel.setText("Login Successful! Starting scanner...");
                
                // Set the current user and clear mood history
                currentUsername = username;
                moodHistory.clear(); 
                
                // --- Load previous session data ---
                String lastMood = UserAuthenticator.DEFAULT_MOOD;
                String lastTime = UserAuthenticator.DEFAULT_TIME;
                try {
                    UserAuthenticator.UserData data = UserAuthenticator.getUserData(username);
                    if (data != null) {
                        lastMood = data.lastMood;
                        lastTime = data.lastTimestamp;
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to load last mood on login: " + ex.getMessage());
                }

                // Switch to the main application scene, passing the previous mood
                showMainApplicationScene(lastMood, lastTime); 
            } else {
                messageLabel.setText("❌ Invalid Username or Password. Try again.");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
            }
        });

        // Signup Action
        signupBtn.setOnAction(e -> {
            showSignupScene();
        });
    }

    // -----------------------------------------------------------------
    // ## 2. Sign Up Scene
    // -----------------------------------------------------------------
    private void showSignupScene() {
        stopLiveFeedAndReleaseCamera();
        
        Label title = new Label("Create New Account ✍️");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
        Label userLabel = new Label("New Username:");
        userLabel.setStyle("-fx-text-fill: white;");
        TextField userField = new TextField();
        
        Label passLabel = new Label("New Password:");
        passLabel.setStyle("-fx-text-fill: white;");
        PasswordField passField = new PasswordField();

        Button registerBtn = new Button("✅ Register");
        registerBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
        Button backBtn = new Button("⬅️ Back to Login");
        backBtn.setStyle("-fx-background-color: #384A5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
        Label messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: white;");

        // Layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        
        grid.add(userLabel, 0, 1);
        grid.add(userField, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(passField, 1, 2);

        HBox buttonBar = new HBox(15, registerBtn, backBtn);
        buttonBar.setAlignment(Pos.CENTER);

        VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
        Scene scene = new Scene(root, 450, 400);
        primaryStage.setTitle("User Registration");
        primaryStage.setScene(scene); 
        primaryStage.sizeToScene(); 
        primaryStage.centerOnScreen();

        // Registration Logic
        registerBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("❌ Username and Password cannot be empty.");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            try {
                // Check for existing username by attempting to get data
                if (UserAuthenticator.getUserData(username) != null) {
                    messageLabel.setText("❌ Username already exists. Try logging in.");
                    messageLabel.setStyle("-fx-text-fill: #F44336;");
                    return;
                }

                UserAuthenticator.createUser(username, password);
                messageLabel.setText("🎉 Registration successful for " + username + "! Returning to login...");
                messageLabel.setStyle("-fx-text-fill: #00BCD4;");
                
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(this::showLoginScene);
                }).start();

            } catch (IOException ex) {
                messageLabel.setText("❌ Error saving user data.");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> showLoginScene());
    }

    // -----------------------------------------------------------------
    // ## 3. Main Application Scene (The Scanner)
    // -----------------------------------------------------------------
    private void showMainApplicationScene(String lastSessionMood, String lastSessionTimestamp) {
        
        // 1. Stop and release any existing camera resources cleanly
        stopLiveFeedAndReleaseCamera();
        
        // Check if all necessary cascade/model files are present
        if (!MoodDetector.checkRequiredFilesExist()) return;

        // Initialize DNN/Cascades if they weren't initialized before
        if (!MoodDetector.isInitialized()) {
             try {
                MoodDetector.initOpenCV();
                System.out.println("Face detection models loaded.");
            } catch (Exception e) {
                System.err.println("CRITICAL ERROR: Failed to load one or more face detection models: " + e.getMessage());
                return;
            }
        }
        
        // --- Camera Initialization with Retry Loop for Robustness ---
        camera = new VideoCapture(0);
        int maxRetries = 5;
        int currentRetry = 0;
        
        while (!camera.isOpened() && currentRetry < maxRetries) {
            System.err.println("Camera failed to open. Retrying in 500ms... (Attempt " + (currentRetry + 1) + ")");
            currentRetry++;
            try {
                Thread.sleep(500); 
            } catch (InterruptedException ignored) {}
            
            if (camera != null && camera.isOpened()) camera.release();
            camera = new VideoCapture(0); 
        }
        
        Button backToLoginBtn = new Button("⬅️ Return to Login"); 
        backToLoginBtn.setOnAction(e -> showLoginScene()); 
        
        if (!camera.isOpened()) {
            Label errorLabel = new Label("❌ ERROR: Camera not found! Check connection.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            VBox errorRoot = new VBox(50.0, errorLabel, backToLoginBtn);
            errorRoot.setAlignment(Pos.CENTER);
            errorRoot.setStyle("-fx-background-color: #1E2A38;");
            primaryStage.setScene(new Scene(errorRoot, 450, 400));
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            return;
        }
        // ----------------------------------------------------

        // --- UI Element Setup (Scanner Palette) ---
        
        imageView = new ImageView();
        imageView.setFitWidth(550);
        imageView.setFitHeight(450);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

        moodLabel = new Label("📷 Ready to capture");
        moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 
        
        // --- Display previous session mood on the main screen (Header) ---
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
        String lastMoodDisplay = lastSessionMood.equals(UserAuthenticator.DEFAULT_MOOD) ? 
                                 "No previous log found." : 
                                 lastSessionMood + " (on " + lastSessionTimestamp.substring(0, 10) + ")";

        Label prevMoodLabel = new Label("Last Logged Mood: " + lastMoodDisplay);
        prevMoodLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFC107; -fx-font-style: italic;");
        
        VBox headerBox = new VBox(5, welcomeLabel, prevMoodLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10, 0, 10, 0));
        // ----------------------------------------------------------------------


        Button takePicBtn = new Button("📸 Capture Moment");
        Button retakeBtn = new Button("🔁 Recompose");
        Button analyzeBtn = new Button("🧠 Scan Mood");
        Button exitBtn = new Button("🚪 Log Out");
        
        String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
        takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
        retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
        exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

        retakeBtn.setDisable(true);
        analyzeBtn.setDisable(true);

        takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
        retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
        analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
        
        exitBtn.setOnAction(e -> {
             // 1. Save the last analyzed mood before exiting
            String lastMood = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_MOOD : moodHistory.get(0).mood();
            String lastTime = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_TIME : moodHistory.get(0).timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            try {
                UserAuthenticator.saveLastMood(currentUsername, lastMood, lastTime);
            } catch (IOException ex) {
                System.err.println("Failed to save last mood data for " + currentUsername + ": " + ex.getMessage());
            }
            
            // 2. Log out switches back to the login screen
            showLoginScene(); 
        });

        VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(50, 20, 20, 20));

        VBox imageContainer = new VBox(10, headerBox, imageView, moodLabel);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(20));

        // --- Mood History UI ---
        Label historyLabel = new Label("Mood History 📊");
        historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
        historyListView = new ListView<>(moodHistory);
        historyListView.setPrefHeight(450); 
        historyListView.setPrefWidth(280);
        historyListView.setStyle("-fx-control-inner-background: #384A5C; -fx-font-family: 'Monospace'; -fx-text-fill: white; -fx-background-insets: 0;");
        
        VBox historyBox = new VBox(10, historyLabel, historyListView);
        historyBox.setPadding(new Insets(20));
        historyBox.setPrefWidth(300);
        // ------------------------------

        HBox centerBox = new HBox(30, imageContainer, buttonBox);
        centerBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(centerBox);
        root.setRight(historyBox); // Place the history to the right

        root.setStyle("-fx-background-color: #1E2A38;"); 

        // Background loading (Assuming 'bg.jpg' is in resources folder, or remove if not used)
        try {
            Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
            BackgroundImage backgroundImage = new BackgroundImage(
                            bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER, new BackgroundSize(
                            BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );
            root.setBackground(new Background(backgroundImage));
        } catch (Exception ex) {
            System.err.println("⚠️ Could not load background image. Using solid color background.");
        }
        
        Scene scene = new Scene(root, 1300, 650); 
        primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech - Logged in as: " + currentUsername);
        primaryStage.setScene(scene); 
        primaryStage.sizeToScene(); 
        primaryStage.centerOnScreen();

        // 2. Start the new live feed
        startLiveFeed();
    }
    
    // -----------------------------------------------------------------
    // ## 4. Camera & Action Methods
    // -----------------------------------------------------------------
    private void startLiveFeed() {
        showingLiveFeed = true;
        Task<Void> cameraTask = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                while (showingLiveFeed && camera != null && camera.isOpened()) {
                    if (camera.read(frame)) {
                        lastCapturedFrame = frame.clone(); // Clone for safety
                        Image image = FXUtils.matToImage(frame);
                        javafx.application.Platform.runLater(() -> imageView.setImage(image));
                    }
                    try { 
                        Thread.sleep(33); // ~30 FPS
                    } catch (InterruptedException ignored) {
                        break; 
                    } 
                }
                frame.release();
                return null;
            }
        };
        cameraThread = new Thread(cameraTask); 
        cameraThread.setDaemon(true); 
        cameraThread.start();
    }

    private void createPhotoDirectory(String directoryName) {
        try {
            Path dirPath = Paths.get(directoryName);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created directory: " + directoryName);
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory " + directoryName + ": " + e.getMessage());
        }
    }


    private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
        if (camera == null || !camera.isOpened() || lastCapturedFrame == null) return;
        
        // --- MODIFIED: Ensure the photos directory exists and save there ---
        createPhotoDirectory("photo");
        // ------------------------------------------------------------------
        
        // Stop the live feed to freeze the captured frame (using the last valid frame)
        showingLiveFeed = false; 
        
        Image image = FXUtils.matToImage(lastCapturedFrame);
        imageView.setImage(image);

        // Save snapshot to the 'photo/' directory
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        MoodDetector.saveImage("photo/snapshot_" + timestamp + ".jpg", lastCapturedFrame); 
        
        Platform.runLater(() -> moodLabel.setText("✅ Picture captured! Ready for analysis."));
        
        takePicBtn.setDisable(true);
        retakeBtn.setDisable(false);
        analyzeBtn.setDisable(false);
    }

    private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
        moodLabel.setText("📷 Ready to capture");
        if (camera == null || !camera.isOpened()) {
            showMainApplicationScene(UserAuthenticator.DEFAULT_MOOD, UserAuthenticator.DEFAULT_TIME); 
            return;
        }
        
        if (!showingLiveFeed) {
            startLiveFeed();
        }
        
        takePicBtn.setDisable(false);
        retakeBtn.setDisable(true);
        analyzeBtn.setDisable(true);
    }

    private void analyzeMoodFromCapture() {
        if (lastCapturedFrame == null) {
            moodLabel.setText("⚠️ No image captured yet!");
            return;
        }
        
        // Run analysis in a non-blocking way (using Task)
        Task<String> analysisTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // Pass a clone, as analysis modifies the image
                Mat frameCopy = lastCapturedFrame.clone(); 
                // This call both analyzes and draws rectangles on frameCopy
                String mood = MoodDetector.detectAndAnalyzeMood(frameCopy); 
                
                // Update lastCapturedFrame with the annotated frame for display
                lastCapturedFrame.release();
                lastCapturedFrame = frameCopy;
                
                return mood;
            }

            @Override
            protected void succeeded() {
                String currentMood = getValue();
                
                // 1. Update mood history
                MoodEntry newEntry = new MoodEntry(currentMood, LocalDateTime.now());
                moodHistory.add(0, newEntry); 
                historyListView.refresh(); 

                // 2. Display the annotated frame
                Image processed = FXUtils.matToImage(lastCapturedFrame);
                imageView.setImage(processed);
                
                moodLabel.setText("🧠 Mood analysis complete! Detected: " + currentMood);
            }

            @Override
            protected void failed() {
                moodLabel.setText("❌ Analysis failed: " + getException().getMessage());
            }
        };
        
        moodLabel.setText("⏳ Analyzing mood...");
        new Thread(analysisTask).start();
    }
}