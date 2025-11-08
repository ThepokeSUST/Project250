// // Import necessary JavaFX components
// import javafx.application.Application;
// import javafx.application.Platform;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
// import javafx.scene.paint.Color;

// // Import necessary utility components
// import java.io.ByteArrayInputStream;
// import java.util.Random;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// // Import OpenCV components
// import org.opencv.core.Core;
// import org.opencv.core.Mat;
// import org.opencv.core.MatOfByte;
// import org.opencv.core.MatOfRect;
// import org.opencv.core.Rect;
// import org.opencv.core.Scalar;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;

// /**
//  * MoodScannerApp
//  * A JavaFX desktop application that uses OpenCV to access the webcam, detect faces,
//  * and simulate mood analysis to suggest music.
//  */
// public class MoodScannerApp extends Application {

//     // --- Configuration ---
//     // !!! CRITICAL: UPDATE THIS WITH THE PATH TO YOUR OPENCV NATIVE LIBRARY !!!
//     // Example (Windows 64-bit): "C:\\opencv\\build\\java\\x64\\opencv_java4XX.dll"
//     private static final String NATIVE_LIBRARY_PATH = "C:\\opencv\\opencv\\build\\java\\x64\\opencv_java4120.dll";
    
//     // Path to the Haar Cascade XML file for face detection (Must be in TRY/resources)
//     private static final String FACE_CASCADE_PATH = "resources/haarcascade_frontalface_alt.xml";

//     // --- UI Elements and State ---
//     private ImageView imageView;
//     private Label statusLabel;
//     private Label suggestionLabel;
//     private Button scanButton;

//     private VideoCapture capture;
//     private ScheduledExecutorService timer;
//     private CascadeClassifier faceDetector;
//     private boolean isScanning = false;

//     // --- Main Application Start ---
//     @Override
//     public void start(Stage primaryStage) {
//         // 1. Setup essential UI elements immediately so they can be referenced
//         //    even if critical initialization fails (to avoid NullPointerException).
//         imageView = new ImageView();
//         imageView.setFitWidth(640);
//         imageView.setPreserveRatio(true);
//         statusLabel = new Label("Webcam Ready. Waiting for face...");
//         suggestionLabel = new Label("Music Suggestion: Press 'Analyze Mood'");
//         scanButton = new Button("Analyze Mood");


//         // 2. Initialize OpenCV and Face Detector
//         boolean success = initializeOpenCV();
        
//         if (!success) {
//             // If initialization fails, set error message on the already initialized label
//             // and proceed to setup the rest of the UI to display the error.
//             statusLabel.setText("CRITICAL ERROR: OpenCV/Webcam failed to load! Check NATIVE_LIBRARY_PATH, resources, and ensure webcam is available.");
//             setupUI(primaryStage, false); // Pass failure state to UI setup
//             return;
//         }

//         // 3. Setup UI (styles and layout)
//         setupUI(primaryStage, true);
        
//         // 4. Start Webcam Feed
//         startWebcamFeed();
//     }

//     /**
//      * Initializes the OpenCV native library and the face detector.
//      * @return true if successful, false otherwise.
//      */
//     private boolean initializeOpenCV() {
//         try {
//             // Load the native library using the defined path
//             System.load(NATIVE_LIBRARY_PATH);
            
//             // Initialize the Cascade Classifier for face detection
//             faceDetector = new CascadeClassifier(FACE_CASCADE_PATH);
//             if (faceDetector.empty()) {
//                 System.err.println("ERROR: Could not load the face detection cascade XML. Check path: " + FACE_CASCADE_PATH);
//                 return false;
//             }

//             // Initialize VideoCapture
//             capture = new VideoCapture(0); // 0 is usually the default webcam
//             if (!capture.isOpened()) {
//                 System.err.println("ERROR: Could not open the webcam (VideoCapture).");
//                 return false;
//             }
//             return true;

//         } catch (Exception e) {
//             System.err.println("Exception during OpenCV initialization: " + e.getMessage());
//             e.printStackTrace();
//             return false;
//         }
//     }

//     /**
//      * Sets up the JavaFX graphical user interface.
//      * @param primaryStage The primary stage for this application.
//      * @param opencvSuccess Flag indicating if OpenCV was initialized successfully.
//      */
//     private void setupUI(Stage primaryStage, boolean opencvSuccess) {
//         // --- Apply Styling ---
        
//         // Status Label styling
//         statusLabel.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold;");
        
//         // Suggestion Label styling
//         suggestionLabel.setStyle("-fx-font-size: 14pt; -fx-padding: 10px; -fx-background-color: #ECF0F1; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
//         // Mood Analysis Button styling
//         scanButton.setStyle("-fx-font-size: 18pt; -fx-padding: 15 30 15 30; -fx-background-color: #3498DB; -fx-text-fill: white; -fx-border-radius: 8px; -fx-background-radius: 8px;");

//         // Conditional UI setup
//         if (opencvSuccess) {
//             statusLabel.setTextFill(Color.web("#2ECC71")); // Green for ready
//             scanButton.setOnAction(e -> analyzeMood());
//             scanButton.setDisable(true); // Disable until face is detected
//         } else {
//             statusLabel.setTextFill(Color.web("#E74C3C")); // Red for error
//             suggestionLabel.setText("Cannot run mood analysis. Check console for details.");
//             scanButton.setDisable(true);
//         }

//         // --- Layout ---
//         VBox controlsBox = new VBox(15, statusLabel, scanButton, suggestionLabel);
//         controlsBox.setStyle("-fx-alignment: center; -fx-padding: 20px; -fx-background-color: #2C3E50;");

//         BorderPane root = new BorderPane();
//         root.setCenter(imageView);
//         root.setBottom(controlsBox);
//         root.setStyle("-fx-background-color: #F8F8F8;");

//         Scene scene = new Scene(root, 700, 700);
        
//         primaryStage.setTitle("Mood Scan Music Suggestor");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         // Ensure proper shutdown when closing the window
//         primaryStage.setOnCloseRequest(e -> shutdown());
//     }

//     /**
//      * Starts the scheduled task to grab and process frames from the webcam.
//      */
//     private void startWebcamFeed() {
//         // Set up a scheduled executor to grab a frame every 33 milliseconds (~30 FPS)
//         timer = Executors.newSingleThreadScheduledExecutor();
//         timer.scheduleAtFixedRate(this::grabFrame, 0, 33, TimeUnit.MILLISECONDS);
//     }
    
//     /**
//      * The core loop: grabs a frame, detects faces, and updates the JavaFX image view.
//      */
//     private void grabFrame() {
//         // Mat must be released, so it's declared here
//         Mat frame = new Mat(); 
        
//         try {
//             if (capture.isOpened() && capture.read(frame)) {
                
//                 // Mirror the frame horizontally for a more natural webcam view
//                 Core.flip(frame, frame, 1);
                
//                 // Convert to grayscale for faster face detection
//                 Mat grayFrame = new Mat();
//                 Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                
//                 // Detect faces
//                 MatOfRect faces = new MatOfRect();
//                 // detectMultiScale detects objects of different sizes in the input image. 
//                 // Arguments: image, objects (output), scale factor, min neighbors, min size
//                 faceDetector.detectMultiScale(grayFrame, faces, 1.1, 2, 0, new org.opencv.core.Size(30, 30), new org.opencv.core.Size());

//                 int faceCount = faces.toArray().length;

//                 // Update UI based on face detection result
//                 Platform.runLater(() -> {
//                     // Only enable scan button if a face is detected AND we are not currently analyzing
//                     if (faceCount > 0) {
//                         statusLabel.setText("Face Detected! Ready for Analysis.");
//                         statusLabel.setTextFill(Color.web("#27AE60"));
//                         if (!isScanning) {
//                             scanButton.setDisable(false);
//                         }
//                     } else {
//                         statusLabel.setText("Webcam Active. Please show your face.");
//                         statusLabel.setTextFill(Color.web("#E74C3C"));
//                         scanButton.setDisable(true);
//                     }
//                 });

//                 // Draw rectangles around detected faces
//                 for (Rect rect : faces.toArray()) {
//                     Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3); // Green rectangle
//                 }
                
//                 // Convert the OpenCV Mat to a JavaFX Image and display it
//                 Image imageToShow = mat2Image(frame);
//                 Platform.runLater(() -> imageView.setImage(imageToShow));
                
//                 // Release grayscale mat
//                 grayFrame.release();
//                 faces.release();
//             }
//         } catch (Exception e) {
//              System.err.println("Error processing frame: " + e.getMessage());
//         } finally {
//             // Clean up memory
//             if (frame != null) {
//                 frame.release();
//             }
//         }
//     }

//     /**
//      * Converts an OpenCV Mat object into a JavaFX Image object.
//      */
//     private Image mat2Image(Mat frame) {
//         // Create a buffer for the image data
//         MatOfByte buffer = new MatOfByte();
//         // Encode the Mat into a .png format (or .jpg, etc.)
//         Imgcodecs.imencode(".png", frame, buffer);
//         // Convert the buffer to a JavaFX Image
//         return new Image(new ByteArrayInputStream(buffer.toArray()));
//     }

//     /**
//      * Simulates mood analysis and provides a music suggestion.
//      */
//     private void analyzeMood() {
//         if (isScanning) return;
        
//         isScanning = true;
//         scanButton.setDisable(true);
//         suggestionLabel.setText("Analyzing mood... Please hold still...");
//         suggestionLabel.setStyle("-fx-font-size: 14pt; -fx-padding: 10px; -fx-background-color: #F39C12; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
//         // Simulate a 3 second analysis delay
//         new Thread(() -> {
//             try {
//                 Thread.sleep(3000); 
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }

//             // --- Simplified Mood Logic ---
//             // In a real application, you would use more advanced OpenCV/Deep Learning 
//             // techniques (e.g., DNN, custom trained models) here to detect actual emotion.
//             String[] moods = {"Happy", "Calm", "Focused", "Stressed", "Energetic"};
//             String[] suggestions = {
//                 "Suggesting: Upbeat Pop (e.g., 'Walking on Sunshine')",
//                 "Suggesting: Ambient Instrumental (e.g., 'Weightless' by Marconi Union)",
//                 "Suggesting: Lofi Beats (e.g., 'Study Girl' Playlist)",
//                 "Suggesting: Classical Meditation Music (e.g., Chopin Nocturnes)",
//                 "Suggesting: Rock Anthems (e.g., 'Don't Stop Me Now' by Queen)"
//             };

//             Random rand = new Random();
//             int index = rand.nextInt(moods.length);
//             String detectedMood = moods[index];
//             String suggestedMusic = suggestions[index];
            
//             // Update UI on the JavaFX Application Thread
//             Platform.runLater(() -> {
//                 suggestionLabel.setText("Mood Detected: " + detectedMood + ". " + suggestedMusic);
//                 suggestionLabel.setStyle("-fx-font-size: 14pt; -fx-padding: 10px; -fx-background-color: #27AE60; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;");
//                 isScanning = false;
//                 // The next grabFrame will re-enable the scan button if a face is still visible.
//             });

//         }).start();
//     }


//     /**
//      * Stops the webcam and cleans up resources on application exit.
//      */
//     private void shutdown() {
//         System.out.println("Shutting down resources...");
//         if (timer != null && !timer.isShutdown()) {
//             try {
//                 // Stop the timer task
//                 timer.shutdown();
//                 timer.awaitTermination(500, TimeUnit.MILLISECONDS);
//             } catch (InterruptedException e) {
//                 System.err.println("Webcam timer interrupted during shutdown.");
//             }
//         }
        
//         // Release the camera
//         if (capture != null && capture.isOpened()) {
//             capture.release();
//         }
        
//         // Finalize (optional, but good practice for native objects)
//         // Note: Java's garbage collector usually handles this, but calling release is primary.
//         if (faceDetector != null) {
//             // faceDetector.finalize(); // finalize() is often protected/deprecated, release is preferred.
//         }
        
//         // Exit the JavaFX application
//         Platform.exit();
//         System.exit(0);
//     }

//     // Standard main method to launch the JavaFX application
//     public static void main(String[] args) {
//         launch(args);
//     }
// }






// import javafx.application.Application;
// import javafx.application.Platform;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.ScrollPane;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.*;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.stage.Stage;
// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import java.io.ByteArrayInputStream;
// import java.util.Random;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// public class MoodScannerApp extends Application {

//     private ImageView imageView;
//     private Label statusLabel;
//     private Label suggestionLabel;
//     private Button scanButton, captureButton, retakeButton;

//     private VideoCapture capture;
//     private ScheduledExecutorService timer;
//     private CascadeClassifier faceDetector;

//     private Mat capturedFrame;
//     private boolean photoCaptured = false;

//     @Override
//     public void start(Stage primaryStage) {
//         // Title
//         Label title = new Label("🎭 Mood Scanner Music Suggestor 🎶");
//         title.setFont(Font.font("Arial Rounded MT Bold", 26));
//         title.setTextFill(Color.WHITE);
//         title.setPadding(new Insets(10));
//         title.setAlignment(Pos.CENTER);
//         title.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

//         // Image View
//         imageView = new ImageView();
//         imageView.setPreserveRatio(true);
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         StackPane imagePane = new StackPane(imageView);
//         imagePane.setStyle("-fx-background-color: black; -fx-border-color: #16A085; -fx-border-width: 4; -fx-border-radius: 12;");
//         imagePane.setPadding(new Insets(10));

//         // --- Right Side Panel ---
//         // Status Label
//         statusLabel = new Label("📷 Webcam Ready. Waiting for face...");
//         statusLabel.setFont(Font.font(16));
//         statusLabel.setTextFill(Color.LIGHTGREEN);

//         // Suggestion Label inside ScrollPane
//         suggestionLabel = new Label("💡 Music Suggestion: Capture a photo first.");
//         suggestionLabel.setFont(Font.font(16));
//         suggestionLabel.setTextFill(Color.WHITE);
//         suggestionLabel.setWrapText(true);
//         suggestionLabel.setPadding(new Insets(10));
//         suggestionLabel.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8;");

//         ScrollPane suggestionScroll = new ScrollPane(suggestionLabel);
//         suggestionScroll.setFitToWidth(true);
//         suggestionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//         suggestionScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//         suggestionScroll.setPrefHeight(300);
//         VBox.setVgrow(suggestionScroll, Priority.ALWAYS);

//         // Buttons
//         captureButton = new Button("📸 Capture");
//         scanButton = new Button("🎶 Analyze");
//         retakeButton = new Button("🔄 Retake");
//         captureButton.setStyle(buttonStyle("#1ABC9C"));
//         scanButton.setStyle(buttonStyle("#2980B9"));
//         retakeButton.setStyle(buttonStyle("#E67E22"));
//         scanButton.setDisable(true);
//         retakeButton.setDisable(true);

//         VBox buttonBox = new VBox(20, captureButton, scanButton, retakeButton);
//         buttonBox.setAlignment(Pos.CENTER);

//         VBox rightPanel = new VBox(20, statusLabel, buttonBox, suggestionScroll);
//         rightPanel.setAlignment(Pos.TOP_CENTER);
//         rightPanel.setPadding(new Insets(15));
//         rightPanel.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 15;");
//         rightPanel.setPrefWidth(280);

//         // Main HBox: Image on left, controls on right
//         HBox mainContent = new HBox(15, imagePane, rightPanel);
//         mainContent.setPadding(new Insets(15));
//         HBox.setHgrow(imagePane, Priority.ALWAYS);

//         VBox root = new VBox(10, title, mainContent);
//         root.setStyle("-fx-background-color: linear-gradient(to bottom, #1A252F, #2C3E50);");
//         VBox.setVgrow(mainContent, Priority.ALWAYS);

//         Scene scene = new Scene(root, 900, 700);
//         primaryStage.setScene(scene);
//         primaryStage.setTitle("Mood Scanner Music Suggestor");
//         primaryStage.show();

//         // Event Handlers
//         captureButton.setOnAction(e -> capturePhoto());
//         scanButton.setOnAction(e -> analyzeMood());
//         retakeButton.setOnAction(e -> retakeProcess());

//         // Initialize OpenCV
//         if (!initializeOpenCV()) {
//             statusLabel.setText("⚠️ Error: OpenCV or webcam not loaded.");
//             statusLabel.setTextFill(Color.RED);
//             captureButton.setDisable(true);
//         } else {
//             startWebcamFeed();
//         }

//         primaryStage.setOnCloseRequest(e -> shutdown());
//     }

//     private String buttonStyle(String color) {
//         return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px; " +
//                 "-fx-background-radius: 20; -fx-padding: 12 25;";
//     }

//     private boolean initializeOpenCV() {
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             faceDetector = new CascadeClassifier("resources/haarcascade_frontalface_alt.xml");
//             if (faceDetector.empty()) return false;
//             capture = new VideoCapture(0);
//             return capture.isOpened();
//         } catch (Exception e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     private void startWebcamFeed() {
//         timer = Executors.newSingleThreadScheduledExecutor();
//         timer.scheduleAtFixedRate(this::grabFrame, 0, 20, TimeUnit.MILLISECONDS);
//     }

//     private void grabFrame() {
//         if (photoCaptured) return;
//         Mat frame = new Mat();
//         if (capture.isOpened() && capture.read(frame)) {
//             Core.flip(frame, frame, 1);
//             Mat gray = new Mat();
//             Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
//             MatOfRect faces = new MatOfRect();
//             faceDetector.detectMultiScale(gray, faces);
//             for (Rect rect : faces.toArray()) {
//                 Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
//             }
//             Image img = mat2Image(frame);
//             Platform.runLater(() -> imageView.setImage(img));
//             gray.release();
//             faces.release();
//             frame.release();
//         }
//     }

//     private void capturePhoto() {
//         if (capture.isOpened()) {
//             Mat frame = new Mat();
//             if (capture.read(frame)) {
//                 Core.flip(frame, frame, 1);
//                 capturedFrame = frame.clone();
//                 photoCaptured = true;
//                 shutdown();
//                 Platform.runLater(() -> imageView.setImage(mat2Image(capturedFrame)));
//                 statusLabel.setText("✅ Photo captured. Ready to analyze.");
//                 statusLabel.setTextFill(Color.LIGHTGREEN);
//                 scanButton.setDisable(false);
//                 retakeButton.setDisable(false);
//             }
//         }
//     }

//     private void analyzeMood() {
//         if (capturedFrame == null) return;
//         statusLabel.setText("⏳ Analyzing mood...");
//         scanButton.setDisable(true);
//         new Thread(() -> {
//             try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//             String[] moods = {"😊 Happy", "😌 Calm", "🎯 Focused", "😟 Stressed", "⚡ Energetic"};
//             String[] suggestions = {"Upbeat Pop 🎶","Ambient Instrumental 🎵","Lofi Beats 📚","Classical Meditation 🎼","Rock Anthems 🎸"};
//             int index = new Random().nextInt(moods.length);
//             Platform.runLater(() -> {
//                 suggestionLabel.setText("Mood Detected: " + moods[index] +
//                         "\n💡 Suggestion: " + suggestions[index]);
//                 suggestionLabel.setStyle("-fx-font-size: 18px; -fx-padding: 15px; " +
//                         "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 8;");
//             });
//         }).start();
//     }

//     private void retakeProcess() {
//         capturedFrame = null;
//         photoCaptured = false;
//         statusLabel.setText("📷 Webcam Ready. Waiting for face...");
//         statusLabel.setTextFill(Color.LIGHTGREEN);
//         suggestionLabel.setText("💡 Music Suggestion: Capture a photo first.");
//         suggestionLabel.setStyle("-fx-padding: 15px; -fx-background-color: #34495E; -fx-text-fill: white; -fx-background-radius: 8;");
//         scanButton.setDisable(true);
//         retakeButton.setDisable(true);
//         capture = new VideoCapture(0);
//         if (capture.isOpened()) startWebcamFeed();
//         else {
//             statusLabel.setText("⚠️ Cannot reopen webcam!");
//             statusLabel.setTextFill(Color.RED);
//         }
//     }

//     private Image mat2Image(Mat frame) {
//         MatOfByte buffer = new MatOfByte();
//         Imgcodecs.imencode(".png", frame, buffer);
//         return new Image(new ByteArrayInputStream(buffer.toArray()));
//     }

//     private void shutdown() {
//         if (timer != null && !timer.isShutdown()) timer.shutdown();
//         if (capture != null && capture.isOpened()) capture.release();
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }
// }

