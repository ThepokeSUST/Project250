


// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Pos;

// import org.opencv.core.Core;
// import org.opencv.core.Mat;
// import org.opencv.core.MatOfByte;
// import org.opencv.core.MatOfRect;
// import org.opencv.core.Point;
// import org.opencv.core.Rect;
// import org.opencv.core.Scalar;
// import org.opencv.core.Size;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;

// // DNN Imports
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// /**
//  * Mood Scanner Application using OpenCV + JavaFX UI.
//  * Keeps all your original logic intact and just adds a JavaFX interface.
//  */
// public class AppLauncher extends Application {

//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String FACE_CASCADE_PATH = "haarcascade_frontalface_default.xml";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;

//     private VideoCapture camera;
//     private ImageView imageView;
//     private volatile boolean capturing = false;

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage primaryStage) {
//         // Load OpenCV
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library loaded successfully.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV native library.");
//             return;
//         }

//         // Check files
//         if (!checkRequiredFilesExist()) {
//             return;
//         }

//         // Load models
//         try {
//             faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             System.out.println("Deep Learning Face Model loaded successfully.");
//         } catch (Exception e) {
//             System.err.println("Error loading DNN model: " + e.getMessage());
//             faceNet = null;
//         }

//         smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//         eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//         mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);

//         camera = new VideoCapture(0);
//         if (!camera.isOpened()) {
//             System.err.println("Could not open webcam.");
//             return;
//         }

//         imageView = new ImageView();
//         imageView.setFitWidth(800);
//         imageView.setPreserveRatio(true);

//         Button captureButton = new Button("Capture & Detect Mood");
//         captureButton.setOnAction(e -> {
//             if (!capturing) {
//                 capturing = true;
//                 captureFrame();
//             }
//         });

//         Button exitButton = new Button("Exit");
//         exitButton.setOnAction(e -> {
//             capturing = false;
//             if (camera.isOpened())
//                 camera.release();
//             primaryStage.close();
//         });

//         HBox buttonBox = new HBox(10, captureButton, exitButton);
//         buttonBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(imageView);
//         root.setBottom(buttonBox);

//         Scene scene = new Scene(root, 900, 700);
//         primaryStage.setTitle("Mood Scanner (JavaFX + OpenCV)");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         // Show live camera preview
//         startCameraFeed();
//     }

//     /** Show live camera feed in ImageView using a background thread */
//     private void startCameraFeed() {
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try {
//                         Thread.sleep(33);
//                     } catch (InterruptedException ignored) {
//                     }
//                 }
//                 return null;
//             }
//         };
//         Thread camThread = new Thread(cameraTask);
//         camThread.setDaemon(true);
//         camThread.start();
//     }

//     /** Capture one frame and run mood detection logic */
//     private void captureFrame() {
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             System.out.println("Photo captured!");

//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             String filename = "captured_mood_" + timestamp + ".jpg";
//             Imgcodecs.imwrite(filename, frame);
//             System.out.println("Captured image saved as: " + filename);

//             detectMood(frame);
//             Image processedImage = matToImage(frame);
//             imageView.setImage(processedImage);
//         }
//         capturing = false;
//     }

//     // --- Your Original Methods (Unchanged) ---

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) {
//             System.err.println("Error: DNN Proto file not found at " + FACE_PROTO);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(FACE_MODEL))) {
//             System.err.println("Error: DNN Model file not found at " + FACE_MODEL);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) {
//             System.err.println("Error: Smile cascade file not found at " + SMILE_CASCADE_PATH);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) {
//             System.err.println("Error: Eye cascade file not found at " + EYE_CASCADE_PATH);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) {
//             System.err.println("Error: Mouth cascade file not found at " + MOUTH_CASCADE_PATH);
//             allFound = false;
//         }
//         return allFound;
//     }

//     private static void detectMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

//         Rect[] facesArray = null;

//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();

//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));

//             List<Rect> dnnFaces = new ArrayList<>();

//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) {
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     x1 = Math.max(0, x1);
//                     y1 = Math.max(0, y1);
//                     x2 = Math.min(cols, x2);
//                     y2 = Math.min(rows, y2);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_CASCADE_PATH);
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections, 1.1, 2, 0, new Size(30, 30), new Size());
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         System.out.println(String.format("Detected %s faces", facesArray.length));

//         for (Rect faceRect : facesArray) {
//             Mat faceRegionGray = grayImage.submat(faceRect);
//             String mood = analyzeMoodHeuristic(faceRegionGray, faceRect.width);
//             faceRegionGray.release();

//             Scalar textColor = getMoodColor(mood);
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                     new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                     new Scalar(0, 255, 0), 3);

//             Imgproc.putText(image, "Mood: " + mood, new Point(faceRect.x, faceRect.y - 10),
//                     Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, textColor, 2);

//             System.out.println(String.format("Face detected at x: %d, y: %d. Estimated Mood: %s",
//                     faceRect.x, faceRect.y, mood));
//         }

//         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//         Imgcodecs.imwrite("mood_detected_" + timestamp + ".jpg", image);
//         grayImage.release();
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect mouthDetections = new MatOfRect();
//         Rect lowerHalfFace = new Rect(faceRegionGray.width() / 4, faceRegionGray.height() * 2 / 3,
//                 faceRegionGray.width() / 2, faceRegionGray.height() / 3);
//         Mat mouthRegion = faceRegionGray.submat(lowerHalfFace);
//         mouthDetector.detectMultiScale(mouthRegion, mouthDetections, 1.1, 5, 0, new Size(20, 10), new Size());

//         boolean isMouthDetectedAndSmall = false;

//         MatOfRect eyeDetections = new MatOfRect();
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         if (mouthDetections.toArray().length > 0) {
//             Rect mouthRect = mouthDetections.toArray()[0];
//             if (mouthRect.width < (faceWidth * 0.15)) {
//                 isMouthDetectedAndSmall = true;
//             }
//         }


//      System.out.println("'''''''''''''''''''''''''");
//        System.out.println(smileDetections.toArray().length);
//        System.out.println(eyeDetections.toArray().length);

//        System.out.println(isMouthDetectedAndSmall);
//        System.out.println("'''''''''''''''''''''''''");



        

//     //     if ( eyeDetections.toArray().length  >=2&&smileDetections.toArray().length >=0) {
//     //         return "Angry/Contempt";
//     //     }

//     //     else if (smileDetections.toArray().length >0 && eyeDetections.toArray().length ==0) {
//     //         mood = "Pensive/Tired"; 
//     //     } 
//     //     else  if (smileDetections.toArray().length==1 ) {
//     //         smileRegion.release();
//     //         smileDetections.release();
//     //         return "Happy (Smile)";
//     //     } 
//     //    else{
//     //         mood = "Neutral";
//     //     }
//     if (smileDetections.toArray().length > 0 && eyeDetections.toArray().length == 0) {
//             mood = "Pensive";
//         }
        
//         else if (eyeDetections.toArray().length >=2) {
//             mood = "Angry";
//         } 
//         else if (smileDetections.toArray().length==1) {
//             mood = "Happy";
//         }
//         else {
//             mood = "Neutral";
//         }

//         eyeRegion.release();
//         eyeDetections.release();
//         mouthRegion.release();
//         mouthDetections.release();
//         smileRegion.release();
//         smileDetections.release();

//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         switch (mood) {
//             case "Happy (Smile)":
//                 return new Scalar(0, 255, 255);
//             case "Angry/Contempt":
//                 return new Scalar(0, 0, 255);
//             case "Pensive/Tired":
//                 return new Scalar(255, 0, 0);
//             default:
//                 return new Scalar(0, 255, 0);
//         }
//     }

//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             System.err.println("Error converting Mat to BufferedImage: " + e.getMessage());
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }

















// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Pos;

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String FACE_CASCADE_PATH = "haarcascade_frontalface_default.xml";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;

//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = true;
//     private Mat lastCapturedFrame = null;

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage primaryStage) {
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV loaded successfully.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library.");
//             return;
//         }

//         if (!checkRequiredFilesExist()) return;

//         try {
//             faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             System.out.println("Face DNN loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading DNN model: " + e.getMessage());
//             faceNet = null;
//         }

//         smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//         eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//         mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);

//         camera = new VideoCapture(0);
//         if (!camera.isOpened()) {
//             System.err.println("Camera not found!");
//             return;
//         }

//         imageView = new ImageView();
//         imageView.setFitWidth(800);
//         imageView.setPreserveRatio(true);

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: green;");

//         Button takePicBtn = new Button("📸 Take Picture");
//         Button retakeBtn = new Button("🔁 Retake");
//         Button analyzeBtn = new Button("🧠 Analyze Mood");
//         Button exitBtn = new Button("❌ Exit");

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
//         exitBtn.setOnAction(e -> {
//             showingLiveFeed = false;
//             if (camera.isOpened()) camera.release();
//             primaryStage.close();
//         });

//         HBox buttonBox = new HBox(15, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);

//         VBox bottomBox = new VBox(10, moodLabel, buttonBox);
//         bottomBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(imageView);
//         root.setBottom(bottomBox);

//         Scene scene = new Scene(root, 900, 700);
//         primaryStage.setTitle("Mood Scanner (Classic UI)");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         startLiveFeed();
//     }

//     /** Live camera preview */
//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try {
//                         Thread.sleep(33);
//                     } catch (InterruptedException ignored) {}
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         Thread camThread = new Thread(cameraTask);
//         camThread.setDaemon(true);
//         camThread.start();
//     }

//     /** Capture one frame */
//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (!camera.isOpened()) return;

//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
//             showingLiveFeed = false;
//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured!");
//         }

//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     /** Resume camera feed */
//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (!camera.isOpened()) return;

//         showingLiveFeed = true;
//         startLiveFeed();

//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     /** Analyze captured image */
//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }

//         Mat frameCopy = lastCapturedFrame.clone();
//         detectMood(frameCopy);
//         Image processed = matToImage(frameCopy);
//         imageView.setImage(processed);
//         moodLabel.setText("🧠 Mood analyzed (see image)");
//         frameCopy.release();
//     }

//     // ---------- Detection & Utility Methods (unchanged) ----------

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) {
//             System.err.println("Missing: " + FACE_PROTO);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(FACE_MODEL))) {
//             System.err.println("Missing: " + FACE_MODEL);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) {
//             System.err.println("Missing: " + SMILE_CASCADE_PATH);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) {
//             System.err.println("Missing: " + EYE_CASCADE_PATH);
//             allFound = false;
//         }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) {
//             System.err.println("Missing: " + MOUTH_CASCADE_PATH);
//             allFound = false;
//         }
//         return allFound;
//     }

//     private static void detectMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;

//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();

//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));

//             List<Rect> dnnFaces = new ArrayList<>();
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) {
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_CASCADE_PATH);
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         for (Rect faceRect : facesArray) {
//             Mat faceGray = grayImage.submat(faceRect);
//             String mood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();

//             Scalar color = getMoodColor(mood);
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                     new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                     new Scalar(0, 255, 0), 3);
//             Imgproc.putText(image, "Mood: " + mood,
//                     new Point(faceRect.x, faceRect.y - 10),
//                     Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         if (smileDetections.toArray().length > 0 && eyeDetections.toArray().length == 0)
//             mood = "Pensive";
//         else if (eyeDetections.toArray().length >= 2)
//             mood = "Angry";
//         else if (smileDetections.toArray().length == 1)
//             mood = "Happy";
//         else
//             mood = "Neutral";

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255);
//             case "Angry": return new Scalar(0, 0, 255);
//             case "Pensive": return new Scalar(255, 0, 0);
//             default: return new Scalar(0, 255, 0);
//         }
//     }

//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }





















// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String FACE_CASCADE_PATH = "haarcascade_frontalface_default.xml";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;

//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = true;
//     private Mat lastCapturedFrame = null;

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage primaryStage) {
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV loaded successfully.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Make sure OpenCV is configured correctly.");
//             return;
//         }

//         if (!checkRequiredFilesExist()) return;

//         try {
//             faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             System.out.println("Face DNN loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading DNN model: " + e.getMessage());
//             faceNet = null;
//         }

//         smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//         eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//         mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);

//         camera = new VideoCapture(0);
//         if (!camera.isOpened()) {
//             System.err.println("Camera not found!");
//             return;
//         }

//         // -------------------- UI Element Setup and Styling 🎨 --------------------
        
//         // 1. Image View (Camera Feed - Reduced Size)
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         // Subtle border to frame the camera feed
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         // Mood Label (Primary Accent Color: Cyan/Aqua)
//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 

//         // 2. Buttons (Moved to the right and styled)
//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("❌ Close Scanner");
        
//         // Applying the Harmonious Tech Palette
        
//         // Complementary Color (Deep Orange/Warm) for main action
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
        
//         // Neutral Warm (Gold/Amber) for reset/retake
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         // Analogous Color (Blue/Cool) for analysis
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
        
//         // Standard Danger Color (Red)
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
//         exitBtn.setOnAction(e -> {
//             showingLiveFeed = false;
//             if (camera.isOpened()) camera.release();
//             primaryStage.close();
//         });

//         // 3. Layout Structure
        
//         // VBox for buttons (right side)
//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         // VBox for image and label (left side)
//         VBox imageContainer = new VBox(10, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         // HBox to combine image/label and buttons (Center of BorderPane)
//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         // Main Root
//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);

//         // 4. Background (The Canvas - Deep Dark Blue/Gray)
//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         try {
//             // Attempt to load the background image
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                     bgImage,
//                     BackgroundRepeat.NO_REPEAT,
//                     BackgroundRepeat.NO_REPEAT,
//                     BackgroundPosition.CENTER,
//                     new BackgroundSize(
//                             BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true
//                     )
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         // 5. Scene Setup
//         Scene scene = new Scene(root, 1000, 650);
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         startLiveFeed();
//     }

//     // ----------------- Helper Methods -----------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         // Update UI on the JavaFX application thread
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { Thread.sleep(33); } catch (InterruptedException ignored) {} // approx 30 FPS
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         Thread camThread = new Thread(cameraTask);
//         camThread.setDaemon(true);
//         camThread.start();
//     }

//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (!camera.isOpened()) return;
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             // Store the captured frame and stop the live feed
//             lastCapturedFrame = frame.clone();
//             showingLiveFeed = false;
//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             // Save snapshot
//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (!camera.isOpened()) return;
//         // Re-enable live feed
//         showingLiveFeed = true;
//         startLiveFeed();
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
//         Mat frameCopy = lastCapturedFrame.clone();
//         detectMood(frameCopy);
//         Image processed = matToImage(frameCopy);
//         imageView.setImage(processed);
//         moodLabel.setText("🧠 Mood analysis complete!");
//         frameCopy.release();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     private static void detectMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;

//         // Use DNN for face detection if loaded, fallback to Cascade
//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             // Filter detections by confidence
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { // Confidence threshold
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             // Fallback: Haar Cascade Detection
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_CASCADE_PATH);
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         for (Rect faceRect : facesArray) {
//             Mat faceGray = grayImage.submat(faceRect);
//             String mood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(mood);
            
//             // Draw rectangle around the face
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                     new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                     new Scalar(0, 255, 0), 3);
            
//             // Put mood text
//             Imgproc.putText(image, "Mood: " + mood,
//                     new Point(faceRect.x, faceRect.y - 10),
//                     Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         // Look for smiles in the lower half of the face
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         // Look for eyes in the upper half of the face
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         // Simple Heuristic Mood Analysis
//         if (numSmiles > 0 && numEyes < 2)
//             // Smile detected, but not enough eyes (maybe profile/squinting)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             // Two eyes detected, no smile (could imply anger/focus)
//             mood = "Angry";
//         else if (numSmiles > 0 && numEyes >= 2)
//             // Smile + two eyes
//              mood = "Happy";
//         else
//             // Default
//             mood = "Neutral";

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         // Colors for OpenCV drawing (BGR format)
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); // Yellow/Cyan
//             case "Angry": return new Scalar(0, 0, 255);   // Red
//             case "Pensive": return new Scalar(255, 0, 0); // Blue
//             default: return new Scalar(0, 255, 0);        // Green
//         }
//     }

//     // Utility to convert OpenCV Mat to JavaFX Image
//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }










//login only


// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String FACE_CASCADE_PATH = "haarcascade_frontalface_default.xml";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;

//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = true;
//     private Mat lastCapturedFrame = null;
    
//     // --- STATIC USER AUTHENTICATOR (FILE-BASED) ---
//     private static class UserAuthenticator {
//         private static final String USER_FILE = "users.txt";

//         // Creates a user and appends to the file
//         public static void createUser(String username, String password) throws IOException {
//             // WARNING: Storing plain text password is for demonstration only. Use hashing in production.
//             String data = username + "," + password + "\n";
//             Files.write(Paths.get(USER_FILE), data.getBytes(), 
//                         java.nio.file.StandardOpenOption.CREATE, 
//                         java.nio.file.StandardOpenOption.APPEND);
//         }

//         // Authenticates a user against the file content
//         public static boolean authenticate(String username, String password) {
//             try {
//                 // If the file doesn't exist, create it with a default user
//                 if (!Files.exists(Paths.get(USER_FILE))) {
//                     createUser("admin", "password"); 
//                     System.out.println("Default user 'admin' created in users.txt.");
//                 }
                
//                 // Read all lines and check for an exact match
//                 List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//                 String target = username + "," + password;
//                 return lines.contains(target);
//             } catch (IOException e) {
//                 System.err.println("Error accessing user file: " + e.getMessage());
//                 return false;
//             }
//         }
//     }
//     // ---------------------------------------------

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage primaryStage) {
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library check passed.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Make sure OpenCV is configured correctly.");
//             return;
//         }

//         // Immediately launch the login screen
//         showLoginStage(primaryStage);
//     }
    
//     // --- NEW METHOD: LOGIN STAGE ---
//     private void showLoginStage(Stage primaryStage) {
//         // UI Elements
//         Label title = new Label("Mood Scanner Login 👤");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         Label userLabel = new Label("Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
//         userField.setPromptText("Enter Username");
        
//         Label passLabel = new Label("Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();
//         passField.setPromptText("Enter Password");
        
//         Button loginBtn = new Button("🔑 Login");
//         loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("Welcome! (Default: admin/password)");
//         messageLabel.setStyle("-fx-text-fill: #00BCD4;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         VBox root = new VBox(25, title, grid, loginBtn, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 400, 350);
        
//         primaryStage.setTitle("Login Required");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         // Login Logic
//         loginBtn.setOnAction(e -> {
//             String username = userField.getText();
//             String password = passField.getText();

//             if (UserAuthenticator.authenticate(username, password)) {
//                 messageLabel.setText("Login Successful! Starting scanner...");
//                 primaryStage.close(); 
//                 // Launch the main application logic in a new stage
//                 showMainApplication(new Stage());
//             } else {
//                 messageLabel.setText("❌ Invalid Username or Password. Try again.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//             }
//         });
//     }
    
//     // --- MAIN APPLICATION STAGE (Original Content) ---
//     private void showMainApplication(Stage primaryStage) {
        
//         if (!checkRequiredFilesExist()) return;

//         try {
//             // Initialize DNN/Cascades if they weren't initialized before
//             if (faceNet == null) faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             if (smileDetector == null) smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//             if (eyeDetector == null) eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//             if (mouthDetector == null) mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);
            
//             System.out.println("Face DNN loaded in main app.");
//         } catch (Exception e) {
//             System.err.println("Error loading DNN model: " + e.getMessage());
//             faceNet = null;
//         }

//         camera = new VideoCapture(0);
//         if (!camera.isOpened()) {
//             System.err.println("Camera not found!");
//             return;
//         }

//         // -------------------- UI Element Setup and Styling 🎨 --------------------
        
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 

//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("❌ Close Scanner");
        
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
//         exitBtn.setOnAction(e -> {
//             showingLiveFeed = false;
//             if (camera.isOpened()) camera.release();
//             primaryStage.close();
//         });

//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         VBox imageContainer = new VBox(10, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);

//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         try {
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                     bgImage,
//                     BackgroundRepeat.NO_REPEAT,
//                     BackgroundRepeat.NO_REPEAT,
//                     BackgroundPosition.CENTER,
//                     new BackgroundSize(
//                             BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true
//                     )
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         Scene scene = new Scene(root, 1000, 650);
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene);
//         primaryStage.show();

//         startLiveFeed();
//     }

//     // ----------------- Helper Methods (Unchanged) -----------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { Thread.sleep(33); } catch (InterruptedException ignored) {} // approx 30 FPS
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         Thread camThread = new Thread(cameraTask);
//         camThread.setDaemon(true);
//         camThread.start();
//     }

//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (!camera.isOpened()) return;
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
//             showingLiveFeed = false;
//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (!camera.isOpened()) return;
//         showingLiveFeed = true;
//         startLiveFeed();
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
//         Mat frameCopy = lastCapturedFrame.clone();
//         detectMood(frameCopy);
//         Image processed = matToImage(frameCopy);
//         imageView.setImage(processed);
//         moodLabel.setText("🧠 Mood analysis complete!");
//         frameCopy.release();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     private static void detectMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;

//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { 
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_CASCADE_PATH);
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         for (Rect faceRect : facesArray) {
//             Mat faceGray = grayImage.submat(faceRect);
//             String mood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(mood);
            
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                     new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                     new Scalar(0, 255, 0), 3);
            
//             Imgproc.putText(image, "Mood: " + mood,
//                     new Point(faceRect.x, faceRect.y - 10),
//                     Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         if (numSmiles > 0 && numEyes < 2)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             mood = "Angry";
//         else if (numSmiles > 0 && numEyes >= 2)
//              mood = "Happy";
//         else
//             mood = "Neutral";

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); // Yellow/Cyan
//             case "Angry": return new Scalar(0, 0, 255);   // Red
//             case "Pensive": return new Scalar(255, 0, 0); // Blue
//             default: return new Scalar(0, 255, 0);        // Green
//         }
//     }

//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }



















// //signsup login

// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     // --- OpenCV Configuration Paths (Requires these files in the execution directory) ---
//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;
    
//     // --- Application State and Components ---
//     private Stage primaryStage;
//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = false;
//     private Thread cameraThread; 
//     private Mat lastCapturedFrame = null;
    
//     /**
//      * Nested class for simple file-based user management (simulating a database).
//      * Users are stored in 'users.txt' as username,password
//      */
//     private static class UserAuthenticator {
//         private static final String USER_FILE = "users.txt";

//         public static void createUser(String username, String password) throws IOException {
//             String data = username + "," + password + "\n";
//             // Append the new user data to the file, creating it if it doesn't exist.
//             Files.write(Paths.get(USER_FILE), data.getBytes(), 
//                         java.nio.file.StandardOpenOption.CREATE, 
//                         java.nio.file.StandardOpenOption.APPEND);
//         }

//         public static boolean authenticate(String username, String password) {
//             try {
//                 // Initialize with a default user if file is missing
//                 if (!Files.exists(Paths.get(USER_FILE))) {
//                     createUser("admin", "password"); 
//                 }
                
//                 List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//                 String target = username + "," + password;
//                 return lines.contains(target);
//             } catch (IOException e) {
//                 System.err.println("Error accessing user file: " + e.getMessage());
//                 return false;
//             }
//         }
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage stage) {
//         this.primaryStage = stage;
        
//         try {
//             // Load the native OpenCV library
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library check passed.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Ensure the native libraries are correctly configured in your system path.");
//             // Stop application if the library can't be loaded
//             return; 
//         }

//         // Ensure resources are cleaned up when the user closes the window manually
//         primaryStage.setOnCloseRequest(event -> {
//             stopLiveFeedAndReleaseCamera();
//         });

//         // Application starts with the Login screen
//         showLoginScene();
//         primaryStage.show();
//     }
    
//     /**
//      * Gracefully stops the camera feed thread and releases the hardware resource.
//      * This is crucial to prevent camera locking errors when switching scenes or closing.
//      */
//     private void stopLiveFeedAndReleaseCamera() {
//         showingLiveFeed = false;
//         if (cameraThread != null && cameraThread.isAlive()) {
//             try {
//                 cameraThread.interrupt(); 
//                 cameraThread.join(100); // Wait briefly for thread cleanup
//             } catch (InterruptedException ignored) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//         if (camera != null && camera.isOpened()) {
//             camera.release();
//             camera = null; 
//         }
//     }

//     // -----------------------------------------------------------------
//     // ## 1. Login Scene
//     // -----------------------------------------------------------------
//     private void showLoginScene() {
//         // Stop camera if coming from the main app
//         stopLiveFeedAndReleaseCamera();

//         // UI Elements for Login
//         Label title = new Label("Mood Scanner Login 👤");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         Label userLabel = new Label("Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
//         userField.setPromptText("Enter Username");
        
//         Label passLabel = new Label("Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();
//         passField.setPromptText("Enter Password");
        
//         Button loginBtn = new Button("🔑 Login");
//         loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button signupBtn = new Button("✍️ Create Account");
//         signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("Welcome! (Default: admin/password)");
//         messageLabel.setStyle("-fx-text-fill: #00BCD4;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, loginBtn, signupBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400); 
        
//         primaryStage.setTitle("Login Required");
//         primaryStage.setScene(scene);
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();
        
//         // Login Logic
//         loginBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (UserAuthenticator.authenticate(username, password)) {
//                 messageLabel.setText("Login Successful! Starting scanner...");
//                 showMainApplicationScene(); // Switch to the main application scene
//             } else {
//                 messageLabel.setText("❌ Invalid Username or Password. Try again.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//             }
//         });

//         // Signup Action
//         signupBtn.setOnAction(e -> {
//             showSignupScene();
//         });
//     }

//     // -----------------------------------------------------------------
//     // ## 2. Sign Up Scene
//     // -----------------------------------------------------------------
//     private void showSignupScene() {
//         stopLiveFeedAndReleaseCamera();
        
//         Label title = new Label("Create New Account ✍️");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         Label userLabel = new Label("New Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
        
//         Label passLabel = new Label("New Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();

//         Button registerBtn = new Button("✅ Register");
//         registerBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button backBtn = new Button("⬅️ Back to Login");
//         backBtn.setStyle("-fx-background-color: #384A5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("");
//         messageLabel.setStyle("-fx-text-fill: white;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, registerBtn, backBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400);
//         primaryStage.setTitle("User Registration");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // Registration Logic
//         registerBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (username.isEmpty() || password.isEmpty()) {
//                 messageLabel.setText("❌ Username and Password cannot be empty.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 return;
//             }

//             try {
//                 // Simple check for existing username
//                 List<String> lines = Files.readAllLines(Paths.get(UserAuthenticator.USER_FILE));
//                 for (String line : lines) {
//                     if (line.split(",")[0].equals(username)) {
//                         messageLabel.setText("❌ Username already exists. Try logging in.");
//                         messageLabel.setStyle("-fx-text-fill: #F44336;");
//                         return;
//                     }
//                 }

//                 UserAuthenticator.createUser(username, password);
//                 messageLabel.setText("🎉 Registration successful for " + username + "! Returning to login...");
//                 messageLabel.setStyle("-fx-text-fill: #00BCD4;");
                
//                 // Go back to the login screen after a short delay
//                 new Thread(() -> {
//                     try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//                     javafx.application.Platform.runLater(this::showLoginScene);
//                 }).start();

//             } catch (IOException ex) {
//                 messageLabel.setText("❌ Error saving user data.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 ex.printStackTrace();
//             }
//         });

//         backBtn.setOnAction(e -> showLoginScene());
//     }

//     // -----------------------------------------------------------------
//     // ## 3. Main Application Scene (The Scanner)
//     // -----------------------------------------------------------------
//     private void showMainApplicationScene() {
        
//         // 1. Stop and release any existing camera resources cleanly
//         stopLiveFeedAndReleaseCamera();
        
//         // Check if all necessary cascade/model files are present
//         if (!checkRequiredFilesExist()) return;

//         try {
//             // Initialize DNN/Cascades if they weren't initialized before
//             if (faceNet == null) faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             if (smileDetector == null) smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//             if (eyeDetector == null) eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//             if (mouthDetector == null) mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);
            
//             System.out.println("Face detection models loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading face detection models: " + e.getMessage());
//             faceNet = null; // Set to null if DNN fails
//         }

//         // --- Camera Initialization with Retry Loop for Robustness ---
//         camera = new VideoCapture(0);
//         int maxRetries = 5;
//         int currentRetry = 0;
        
//         while (!camera.isOpened() && currentRetry < maxRetries) {
//             System.err.println("Camera failed to open. Retrying in 500ms... (Attempt " + (currentRetry + 1) + ")");
//             currentRetry++;
//             try {
//                 Thread.sleep(500); 
//             } catch (InterruptedException ignored) {}
            
//             if (camera != null && camera.isOpened()) camera.release();
//             camera = new VideoCapture(0); 
//         }
        
//         // --- Create a button to handle the navigation back to login
//         Button backToLoginBtn = new Button("Back to Login"); // Create button with text only
//         backToLoginBtn.setOnAction(e -> showLoginScene()); // Set action separately

//         if (!camera.isOpened()) {
//             System.err.println("Camera not found after multiple retries! Returning to login.");
//             Label errorLabel = new Label("❌ ERROR: Camera not found! Returning to login...");
//             errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            
//             // FIX APPLIED HERE: Use the correctly constructed button
//             VBox errorRoot = new VBox(50.0, errorLabel, backToLoginBtn);
//             errorRoot.setAlignment(Pos.CENTER);
//             errorRoot.setStyle("-fx-background-color: #1E2A38;");
//             primaryStage.setScene(new Scene(errorRoot, 450, 400));
//             primaryStage.sizeToScene();
//             primaryStage.centerOnScreen();
//             return;
//         }
//         // ----------------------------------------------------

//         // --- UI Element Setup (Scanner Palette) ---
        
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 

//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("🚪 Log Out");
        
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
//         exitBtn.setOnAction(e -> {
//             // Log out switches back to the login screen
//             showLoginScene(); 
//         });

//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         VBox imageContainer = new VBox(10, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);

//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         // Attempt to load background image (requires /bg.jpg resource)
//         try {
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                     bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                     BackgroundPosition.CENTER, new BackgroundSize(
//                     BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             // Fallback to solid color if image fails
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         Scene scene = new Scene(root, 1000, 650);
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // 2. Start the new live feed
//         startLiveFeed();
//     }

//     // -----------------------------------------------------------------
//     // ## 4. OpenCV Helper Methods
//     // -----------------------------------------------------------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera != null && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         // Update UI on the JavaFX application thread
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { 
//                         Thread.sleep(33); // Approx 30 FPS
//                     } catch (InterruptedException ignored) {
//                         break; 
//                     } 
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         cameraThread = new Thread(cameraTask); 
//         cameraThread.setDaemon(true); // Allows the application to exit even if the thread is running
//         cameraThread.start();
//     }

//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (camera == null || !camera.isOpened()) return;
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
            
//             // Stop the live feed to freeze the captured frame
//             showingLiveFeed = false; 

//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             // Save snapshot
//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (camera == null || !camera.isOpened()) {
//              // If camera was closed, attempt to re-initialize by switching back to main scene
//              showMainApplicationScene(); 
//              return;
//         }
        
//         // Restart the live feed
//         if (!showingLiveFeed) {
//             startLiveFeed();
//         }
        
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
//         Mat frameCopy = lastCapturedFrame.clone();
//         detectMood(frameCopy);
//         Image processed = matToImage(frameCopy);
//         imageView.setImage(processed);
//         moodLabel.setText("🧠 Mood analysis complete!");
//         frameCopy.release();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     /**
//      * Performs face and feature detection on the image. Uses DNN if available, falls back to Haar Cascade.
//      */
//     private static void detectMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;

//         // Use DNN (Caffe model) for more robust face detection
//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             // Filter detections by confidence
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { // Confidence threshold
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             // Fallback to Haar Cascade
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_PROTO); // Note: using FACE_PROTO path for consistency if we don't load the haar XML file in the start method
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         for (Rect faceRect : facesArray) {
//             Mat faceGray = grayImage.submat(faceRect);
//             String mood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(mood);
            
//             // Draw rectangle and text
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                     new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                     color, 3);
            
//             Imgproc.putText(image, "Mood: " + mood,
//                     new Point(faceRect.x, faceRect.y - 10),
//                     Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//     }

//     /**
//      * Simple heuristic analysis based on eye and smile detection counts.
//      */
//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         // Focus smile detection on the lower half of the face
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         // Focus eye detection on the upper half of the face
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         // Heuristic Logic
//         if (numSmiles > 0)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             mood = "Neutral"; // Assume neutral or slightly focused if eyes are open but no smile
//         else
//             mood = "Pensive"; // Default for unclear or partial detection

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         // Colors for OpenCV drawing (BGR format)
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); // Yellow/Cyan
//             case "Neutral": return new Scalar(0, 255, 0);   // Green
//             case "Pensive": return new Scalar(255, 0, 0); // Blue
//             default: return new Scalar(255, 255, 255);        // White
//         }
//     }

//     // Utility to convert OpenCV Mat to JavaFX Image
//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         // Encode the Mat to a byte array (JPEG format)
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             // Read the byte array into a BufferedImage
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             e.printStackTrace();
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }











// //history


// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.ListView; // New Import
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList; // New Import

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     // --- OpenCV Configuration Paths (Requires these files in the execution directory) ---
//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;
    
//     // --- Application State and Components ---
//     private Stage primaryStage;
//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = false;
//     private Thread cameraThread; 
//     private Mat lastCapturedFrame = null;
    
//     // --- NEW: Mood Tracking Components ---
//     // Record to hold mood data
//     private static record MoodEntry(String mood, LocalDateTime timestamp) {
//         @Override
//         public String toString() {
//             return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | Mood: " + mood;
//         }
//     }
    
//     // ObservableList for JavaFX ListView
//     private ObservableList<MoodEntry> moodHistory = FXCollections.observableArrayList();
//     private ListView<MoodEntry> historyListView;
//     // ------------------------------------
    
//     /**
//      * Nested class for simple file-based user management (simulating a database).
//      * Users are stored in 'users.txt' as username,password
//      */
//     private static class UserAuthenticator {
//         private static final String USER_FILE = "users.txt";

//         public static void createUser(String username, String password) throws IOException {
//             String data = username + "," + password + "\n";
//             // Append the new user data to the file, creating it if it doesn't exist.
//             Files.write(Paths.get(USER_FILE), data.getBytes(), 
//                          java.nio.file.StandardOpenOption.CREATE, 
//                          java.nio.file.StandardOpenOption.APPEND);
//         }

//         public static boolean authenticate(String username, String password) {
//             try {
//                 // Initialize with a default user if file is missing
//                 if (!Files.exists(Paths.get(USER_FILE))) {
//                     createUser("admin", "password"); 
//                 }
                
//                 List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//                 String target = username + "," + password;
//                 return lines.contains(target);
//             } catch (IOException e) {
//                 System.err.println("Error accessing user file: " + e.getMessage());
//                 return false;
//             }
//         }
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage stage) {
//         this.primaryStage = stage;
        
//         try {
//             // Load the native OpenCV library
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library check passed.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Ensure the native libraries are correctly configured in your system path.");
//             // Stop application if the library can't be loaded
//             return; 
//         }

//         // Ensure resources are cleaned up when the user closes the window manually
//         primaryStage.setOnCloseRequest(event -> {
//             stopLiveFeedAndReleaseCamera();
//         });

//         // Application starts with the Login screen
//         showLoginScene();
//         primaryStage.show();
//     }
    
//     /**
//      * Gracefully stops the camera feed thread and releases the hardware resource.
//      * This is crucial to prevent camera locking errors when switching scenes or closing.
//      */
//     private void stopLiveFeedAndReleaseCamera() {
//         showingLiveFeed = false;
//         if (cameraThread != null && cameraThread.isAlive()) {
//             try {
//                 cameraThread.interrupt(); 
//                 cameraThread.join(100); // Wait briefly for thread cleanup
//             } catch (InterruptedException ignored) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//         if (camera != null && camera.isOpened()) {
//             camera.release();
//             camera = null; 
//         }
//     }

//     // -----------------------------------------------------------------
//     // ## 1. Login Scene
//     // -----------------------------------------------------------------
//     private void showLoginScene() {
//         // Stop camera if coming from the main app
//         stopLiveFeedAndReleaseCamera();

//         // UI Elements for Login
//         Label title = new Label("Mood Scanner Login 👤");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         Label userLabel = new Label("Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
//         userField.setPromptText("Enter Username");
        
//         Label passLabel = new Label("Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();
//         passField.setPromptText("Enter Password");
        
//         Button loginBtn = new Button("🔑 Login");
//         loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button signupBtn = new Button("✍️ Create Account");
//         signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("Welcome! (Default: admin/password)");
//         messageLabel.setStyle("-fx-text-fill: #00BCD4;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, loginBtn, signupBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400); 
        
//         primaryStage.setTitle("Login Required");
//         primaryStage.setScene(scene);
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();
        
//         // Login Logic
//         loginBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (UserAuthenticator.authenticate(username, password)) {
//                 messageLabel.setText("Login Successful! Starting scanner...");
//                 // Clear mood history on successful login for a new session
//                 moodHistory.clear(); 
//                 showMainApplicationScene(); // Switch to the main application scene
//             } else {
//                 messageLabel.setText("❌ Invalid Username or Password. Try again.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//             }
//         });

//         // Signup Action
//         signupBtn.setOnAction(e -> {
//             showSignupScene();
//         });
//     }

//     // -----------------------------------------------------------------
//     // ## 2. Sign Up Scene
//     // -----------------------------------------------------------------
//     private void showSignupScene() {
//         stopLiveFeedAndReleaseCamera();
        
//         Label title = new Label("Create New Account ✍️");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         Label userLabel = new Label("New Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
        
//         Label passLabel = new Label("New Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();

//         Button registerBtn = new Button("✅ Register");
//         registerBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button backBtn = new Button("⬅️ Back to Login");
//         backBtn.setStyle("-fx-background-color: #384A5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("");
//         messageLabel.setStyle("-fx-text-fill: white;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, registerBtn, backBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400);
//         primaryStage.setTitle("User Registration");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // Registration Logic
//         registerBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (username.isEmpty() || password.isEmpty()) {
//                 messageLabel.setText("❌ Username and Password cannot be empty.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 return;
//             }

//             try {
//                 // Simple check for existing username
//                 List<String> lines = Files.readAllLines(Paths.get(UserAuthenticator.USER_FILE));
//                 for (String line : lines) {
//                     if (line.split(",")[0].equals(username)) {
//                         messageLabel.setText("❌ Username already exists. Try logging in.");
//                         messageLabel.setStyle("-fx-text-fill: #F44336;");
//                         return;
//                     }
//                 }

//                 UserAuthenticator.createUser(username, password);
//                 messageLabel.setText("🎉 Registration successful for " + username + "! Returning to login...");
//                 messageLabel.setStyle("-fx-text-fill: #00BCD4;");
                
//                 // Go back to the login screen after a short delay
//                 new Thread(() -> {
//                     try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//                     javafx.application.Platform.runLater(this::showLoginScene);
//                 }).start();

//             } catch (IOException ex) {
//                 messageLabel.setText("❌ Error saving user data.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 ex.printStackTrace();
//             }
//         });

//         backBtn.setOnAction(e -> showLoginScene());
//     }

//     // -----------------------------------------------------------------
//     // ## 3. Main Application Scene (The Scanner)
//     // -----------------------------------------------------------------
//     private void showMainApplicationScene() {
        
//         // 1. Stop and release any existing camera resources cleanly
//         stopLiveFeedAndReleaseCamera();
        
//         // Check if all necessary cascade/model files are present
//         if (!checkRequiredFilesExist()) return;

//         try {
//             // Initialize DNN/Cascades if they weren't initialized before
//             if (faceNet == null) faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             if (smileDetector == null) smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//             if (eyeDetector == null) eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//             if (mouthDetector == null) mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);
            
//             System.out.println("Face detection models loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading face detection models: " + e.getMessage());
//             faceNet = null; // Set to null if DNN fails
//         }

//         // --- Camera Initialization with Retry Loop for Robustness ---
//         camera = new VideoCapture(0);
//         int maxRetries = 5;
//         int currentRetry = 0;
        
//         while (!camera.isOpened() && currentRetry < maxRetries) {
//             System.err.println("Camera failed to open. Retrying in 500ms... (Attempt " + (currentRetry + 1) + ")");
//             currentRetry++;
//             try {
//                 Thread.sleep(500); 
//             } catch (InterruptedException ignored) {}
            
//             if (camera != null && camera.isOpened()) camera.release();
//             camera = new VideoCapture(0); 
//         }
        
//         // --- Create a button to handle the navigation back to login
//         Button backToLoginBtn = new Button("Back to Login"); // Create button with text only
//         backToLoginBtn.setOnAction(e -> showLoginScene()); // Set action separately

//         if (!camera.isOpened()) {
//             System.err.println("Camera not found after multiple retries! Returning to login.");
//             Label errorLabel = new Label("❌ ERROR: Camera not found! Returning to login...");
//             errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            
//             // FIX APPLIED HERE: Use the correctly constructed button
//             VBox errorRoot = new VBox(50.0, errorLabel, backToLoginBtn);
//             errorRoot.setAlignment(Pos.CENTER);
//             errorRoot.setStyle("-fx-background-color: #1E2A38;");
//             primaryStage.setScene(new Scene(errorRoot, 450, 400));
//             primaryStage.sizeToScene();
//             primaryStage.centerOnScreen();
//             return;
//         }
//         // ----------------------------------------------------

//         // --- UI Element Setup (Scanner Palette) ---
        
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 

//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("🚪 Log Out");
        
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
//         exitBtn.setOnAction(e -> {
//             // Log out switches back to the login screen
//             showLoginScene(); 
//         });

//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         VBox imageContainer = new VBox(10, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         // --- NEW: Mood History UI ---
//         Label historyLabel = new Label("Mood History 📊");
//         historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         historyListView = new ListView<>(moodHistory);
//         historyListView.setPrefHeight(150); // Set a reasonable height
//         historyListView.setStyle("-fx-control-inner-background: #384A5C; -fx-font-family: 'Monospace'; -fx-text-fill: white;");
        
//         VBox historyBox = new VBox(10, historyLabel, historyListView);
//         historyBox.setPadding(new Insets(20));
//         historyBox.setPrefWidth(300);
//         // ------------------------------

//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);
//         root.setRight(historyBox); // Place the history to the right

//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         // Attempt to load background image (requires /bg.jpg resource)
//         try {
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                         bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                         BackgroundPosition.CENTER, new BackgroundSize(
//                         BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             // Fallback to solid color if image fails
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         Scene scene = new Scene(root, 1300, 650); // Increased width to accommodate history
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // 2. Start the new live feed
//         startLiveFeed();
//     }

//     // -----------------------------------------------------------------
//     // ## 4. OpenCV Helper Methods
//     // -----------------------------------------------------------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera != null && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         // Update UI on the JavaFX application thread
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { 
//                         Thread.sleep(33); // Approx 30 FPS
//                     } catch (InterruptedException ignored) {
//                         break; 
//                     } 
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         cameraThread = new Thread(cameraTask); 
//         cameraThread.setDaemon(true); // Allows the application to exit even if the thread is running
//         cameraThread.start();
//     }

//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (camera == null || !camera.isOpened()) return;
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
            
//             // Stop the live feed to freeze the captured frame
//             showingLiveFeed = false; 

//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             // Save snapshot
//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (camera == null || !camera.isOpened()) {
//              // If camera was closed, attempt to re-initialize by switching back to main scene
//              showMainApplicationScene(); 
//              return;
//         }
        
//         // Restart the live feed
//         if (!showingLiveFeed) {
//             startLiveFeed();
//         }
        
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
//         Mat frameCopy = lastCapturedFrame.clone();
//         String currentMood = detectAndAnalyzeMood(frameCopy); // Separate the detection and drawing logic
        
//         // --- NEW: Add the result to the history ---
//         MoodEntry newEntry = new MoodEntry(currentMood, LocalDateTime.now());
//         moodHistory.add(0, newEntry); // Add to the beginning of the list (most recent first)
        
//         // Update the ListView display immediately
//         historyListView.refresh(); 
//         // -----------------------------------------

//         Image processed = matToImage(frameCopy);
//         imageView.setImage(processed);
//         moodLabel.setText("🧠 Mood analysis complete! Detected: " + currentMood);
//         frameCopy.release();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     /**
//      * Performs face and feature detection, draws on the image, and returns the determined mood.
//      * This is an updated version of detectMood.
//      */
//     private static String detectAndAnalyzeMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;
//         String primaryMood = "No Face Detected"; // Default mood

//         // Use DNN (Caffe model) for more robust face detection
//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             // Filter detections by confidence
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { // Confidence threshold
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             // Fallback to Haar Cascade
//             // IMPORTANT: If you don't have a Haar Cascade XML for face, this will likely fail unless FACE_PROTO is actually a Haar file.
//             // Assuming for now that there is a Haar file (e.g., haarcascade_frontalface_alt.xml) available under FACE_PROTO path or a different one.
//             // If the DNN model files are missing, this block needs a proper face Haar XML.
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_PROTO); 
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         if (facesArray.length > 0) {
//             // Process only the first (largest/most confident) face for simplicity in determining the primary mood
//             Rect faceRect = facesArray[0]; 
//             Mat faceGray = grayImage.submat(faceRect);
//             primaryMood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(primaryMood);
            
//             // Draw rectangle and text on the main image
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                       new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                       color, 3);
            
//             Imgproc.putText(image, "Mood: " + primaryMood,
//                       new Point(faceRect.x, faceRect.y - 10),
//                       Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//         return primaryMood;
//     }

//     /**
//      * Simple heuristic analysis based on eye and smile detection counts.
//      */
//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         // Focus smile detection on the lower half of the face
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         // Focus eye detection on the upper half of the face
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         // Heuristic Logic
//         if (numSmiles > 0)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             mood = "Neutral"; // Assume neutral or slightly focused if eyes are open but no smile
//         else
//             mood = "Pensive"; // Default for unclear or partial detection

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         // Colors for OpenCV drawing (BGR format)
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); // Yellow/Cyan
//             case "Neutral": return new Scalar(0, 255, 0);   // Green
//             case "Pensive": return new Scalar(255, 0, 0); // Blue
//             default: return new Scalar(255, 255, 255);       // White
//         }
//     }

//     // Utility to convert OpenCV Mat to JavaFX Image
//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         // Encode the Mat to a byte array (JPEG format)
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             // Read the byte array into a BufferedImage
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             e.printStackTrace();
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }




















// //all most done


// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.ListView; 
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList; 
// import java.nio.file.Path; // New Import for Path

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     // --- OpenCV Configuration Paths (Requires these files in the execution directory) ---
//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;
    
//     // --- Application State and Components ---
//     private Stage primaryStage;
//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = false;
//     private Thread cameraThread; 
//     private Mat lastCapturedFrame = null;
    
//     // Non-static field to track current user
//     private String currentUsername = null; 
    
//     // --- Mood Tracking Components ---
//     // Record to hold mood data
//     private static record MoodEntry(String mood, LocalDateTime timestamp) {
//         @Override
//         public String toString() {
//             return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | Mood: " + mood;
//         }
//     }
    
//     // ObservableList for JavaFX ListView
//     private ObservableList<MoodEntry> moodHistory = FXCollections.observableArrayList();
//     private ListView<MoodEntry> historyListView;
//     // ------------------------------------
    
//     /**
//      * Helper class to manage user data loaded from file.
//      */
//     private static class UserData {
//         String username;
//         String password;
//         String lastMood;
//         String lastTimestamp;

//         public UserData(String username, String password, String lastMood, String lastTimestamp) {
//             this.username = username;
//             this.password = password;
//             this.lastMood = lastMood;
//             this.lastTimestamp = lastTimestamp;
//         }
//     }
    
//     /**
//      * Nested class for simple file-based user management (simulating a database).
//      * Users are stored in 'users.txt' as username,password,last_mood,last_timestamp
//      */
//     private static class UserAuthenticator {
//         private static final String USER_FILE = "users.txt";
//         private static final String DEFAULT_MOOD = "N/A";
//         private static final String DEFAULT_TIME = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 10); // Date only

//         // Modified createUser to handle initial (empty) mood state
//         public static void createUser(String username, String password) throws IOException {
//             // New user format: username,password,DEFAULT_MOOD,DEFAULT_TIME
//             String data = username + "," + password + "," + DEFAULT_MOOD + "," + DEFAULT_TIME + "\n";
//             // Append the new user data to the file, creating it if it doesn't exist.
//             Files.write(Paths.get(USER_FILE), data.getBytes(), 
//                          java.nio.file.StandardOpenOption.CREATE, 
//                          java.nio.file.StandardOpenOption.APPEND);
//         }

//         public static boolean authenticate(String username, String password) {
//             try {
//                 // Initialize with a default user if file is missing
//                 if (!Files.exists(Paths.get(USER_FILE))) {
//                     createUser("admin", "password"); 
//                 }
                
//                 List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//                 String target = username + "," + password;
                
//                 for (String line : lines) {
//                     // Check if the beginning of the line matches the target username,password
//                     if (line.startsWith(target + ",")) {
//                         return true;
//                     }
//                 }
//                 return false;
//             } catch (IOException e) {
//                 System.err.println("Error accessing user file: " + e.getMessage());
//                 return false;
//             }
//         }
        
//         // --- NEW: Method to retrieve the last mood data upon login ---
//         public static UserData getUserData(String username) throws IOException {
//              if (!Files.exists(Paths.get(USER_FILE))) return null;

//              List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//              for (String line : lines) {
//                  // Split by comma, limiting to 4 parts
//                  String[] parts = line.split(",", 4); 
//                  if (parts.length >= 1 && parts[0].equals(username)) {
//                      String password = parts.length > 1 ? parts[1] : "";
//                      String mood = parts.length > 2 ? parts[2] : DEFAULT_MOOD;
//                      String timestamp = parts.length > 3 ? parts[3] : DEFAULT_TIME;
//                      return new UserData(parts[0], password, mood, timestamp);
//                  }
//              }
//              return null; 
//         }
        
//         // --- NEW: Method to save the last mood data when logging out ---
//         public static void saveLastMood(String username, String mood, String timestamp) throws IOException {
//             Path filePath = Paths.get(USER_FILE);
//             if (!Files.exists(filePath)) return;

//             List<String> lines = Files.readAllLines(filePath);
//             List<String> newLines = new ArrayList<>();
            
//             boolean updated = false;
//             for (String line : lines) {
//                 if (line.startsWith(username + ",")) {
//                     // Extract existing password
//                     String[] parts = line.split(",", 2); // Split only up to password
//                     String password = parts.length > 1 ? parts[1].split(",", 2)[0] : ""; 
                    
//                     // Construct the updated line: username,password,mood,timestamp
//                     String updatedLine = parts[0] + "," + password + "," + mood + "," + timestamp;
//                     newLines.add(updatedLine);
//                     updated = true;
//                 } else {
//                     newLines.add(line);
//                 }
//             }
            
//             if (updated) {
//                  // Write all lines back, replacing the file content
//                  Files.write(filePath, newLines, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
//             }
//         }
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage stage) {
//         this.primaryStage = stage;
        
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library check passed.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Ensure the native libraries are correctly configured in your system path.");
//             return; 
//         }

//         primaryStage.setOnCloseRequest(event -> {
//             stopLiveFeedAndReleaseCamera();
//         });

//         showLoginScene();
//         primaryStage.show();
//     }
    
//     private void stopLiveFeedAndReleaseCamera() {
//         showingLiveFeed = false;
//         if (cameraThread != null && cameraThread.isAlive()) {
//             try {
//                 cameraThread.interrupt(); 
//                 cameraThread.join(100); 
//             } catch (InterruptedException ignored) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//         if (camera != null && camera.isOpened()) {
//             camera.release();
//             camera = null; 
//         }
//         // Note: Static models (faceNet, etc.) are NOT released here since they are static.
//     }

//     // -----------------------------------------------------------------
//     // ## 1. Login Scene
//     // -----------------------------------------------------------------
//     private void showLoginScene() {
//         stopLiveFeedAndReleaseCamera();

//         Label title = new Label("Mood Scanner Login 👤");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         Label userLabel = new Label("Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
//         userField.setPromptText("Enter Username");
        
//         Label passLabel = new Label("Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();
//         passField.setPromptText("Enter Password");
        
//         Button loginBtn = new Button("🔑 Login");
//         loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button signupBtn = new Button("✍️ Create Account");
//         signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("Welcome! (Default: admin/password)");
//         messageLabel.setStyle("-fx-text-fill: #00BCD4;");

//         // Layout omitted for brevity (unchanged)
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, loginBtn, signupBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400); 
        
//         primaryStage.setTitle("Login Required");
//         primaryStage.setScene(scene);
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();
        
//         // Login Logic
//         loginBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (UserAuthenticator.authenticate(username, password)) {
//                 messageLabel.setText("Login Successful! Starting scanner...");
                
//                 // Set the current user and clear mood history
//                 currentUsername = username;
//                 moodHistory.clear(); 
                
//                 // --- Load previous session data ---
//                 String lastMood = UserAuthenticator.DEFAULT_MOOD;
//                 String lastTime = UserAuthenticator.DEFAULT_TIME;
//                 try {
//                     UserData data = UserAuthenticator.getUserData(username);
//                     if (data != null) {
//                         lastMood = data.lastMood;
//                         lastTime = data.lastTimestamp;
//                     }
//                 } catch (IOException ex) {
//                     System.err.println("Failed to load last mood on login: " + ex.getMessage());
//                 }

//                 // Switch to the main application scene, passing the previous mood
//                 showMainApplicationScene(lastMood, lastTime); 
//             } else {
//                 messageLabel.setText("❌ Invalid Username or Password. Try again.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//             }
//         });

//         // Signup Action
//         signupBtn.setOnAction(e -> {
//             showSignupScene();
//         });
//     }

//     // -----------------------------------------------------------------
//     // ## 2. Sign Up Scene (unchanged)
//     // -----------------------------------------------------------------
//     private void showSignupScene() {
//         stopLiveFeedAndReleaseCamera();
        
//         Label title = new Label("Create New Account ✍️");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         Label userLabel = new Label("New Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
        
//         Label passLabel = new Label("New Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();

//         Button registerBtn = new Button("✅ Register");
//         registerBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button backBtn = new Button("⬅️ Back to Login");
//         backBtn.setStyle("-fx-background-color: #384A5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("");
//         messageLabel.setStyle("-fx-text-fill: white;");

//         // Layout omitted for brevity (unchanged)
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, registerBtn, backBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400);
//         primaryStage.setTitle("User Registration");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // Registration Logic
//         registerBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (username.isEmpty() || password.isEmpty()) {
//                 messageLabel.setText("❌ Username and Password cannot be empty.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 return;
//             }

//             try {
//                 // Check for existing username by attempting to get data
//                 if (UserAuthenticator.getUserData(username) != null) {
//                     messageLabel.setText("❌ Username already exists. Try logging in.");
//                     messageLabel.setStyle("-fx-text-fill: #F44336;");
//                     return;
//                 }

//                 UserAuthenticator.createUser(username, password);
//                 messageLabel.setText("🎉 Registration successful for " + username + "! Returning to login...");
//                 messageLabel.setStyle("-fx-text-fill: #00BCD4;");
                
//                 new Thread(() -> {
//                     try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//                     javafx.application.Platform.runLater(this::showLoginScene);
//                 }).start();

//             } catch (IOException ex) {
//                 messageLabel.setText("❌ Error saving user data.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 ex.printStackTrace();
//             }
//         });

//         backBtn.setOnAction(e -> showLoginScene());
//     }

//     // -----------------------------------------------------------------
//     // ## 3. Main Application Scene (The Scanner)
//     // -----------------------------------------------------------------
//     // MODIFIED: Accepts last session data
//     private void showMainApplicationScene(String lastSessionMood, String lastSessionTimestamp) {
        
//         // 1. Stop and release any existing camera resources cleanly
//         stopLiveFeedAndReleaseCamera();
        
//         // Check if all necessary cascade/model files are present
//         if (!checkRequiredFilesExist()) return;

//         try {
//             // Initialize DNN/Cascades if they weren't initialized before
//             if (faceNet == null) faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             if (smileDetector == null) smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//             if (eyeDetector == null) eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//             if (mouthDetector == null) mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);
            
//             if (faceNet.empty() || smileDetector.empty() || eyeDetector.empty() || mouthDetector.empty()) {
//                 System.err.println("CRITICAL ERROR: Failed to load one or more face detection models.");
//                 return;
//             }
//             System.out.println("Face detection models loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading face detection models: " + e.getMessage());
//         }

//         // --- Camera Initialization with Retry Loop for Robustness ---
//         camera = new VideoCapture(0);
//         int maxRetries = 5;
//         int currentRetry = 0;
        
//         while (!camera.isOpened() && currentRetry < maxRetries) {
//             System.err.println("Camera failed to open. Retrying in 500ms... (Attempt " + (currentRetry + 1) + ")");
//             currentRetry++;
//             try {
//                 Thread.sleep(500); 
//             } catch (InterruptedException ignored) {}
            
//             if (camera != null && camera.isOpened()) camera.release();
//             camera = new VideoCapture(0); 
//         }
        
//         Button backToLoginBtn = new Button("⬅️ Return to Login"); 
//         backToLoginBtn.setOnAction(e -> showLoginScene()); 
        
//         if (!camera.isOpened()) {
//             Label errorLabel = new Label("❌ ERROR: Camera not found! Check connection.");
//             errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
//             VBox errorRoot = new VBox(50.0, errorLabel, backToLoginBtn);
//             errorRoot.setAlignment(Pos.CENTER);
//             errorRoot.setStyle("-fx-background-color: #1E2A38;");
//             primaryStage.setScene(new Scene(errorRoot, 450, 400));
//             primaryStage.sizeToScene();
//             primaryStage.centerOnScreen();
//             return;
//         }
//         // ----------------------------------------------------

//         // --- UI Element Setup (Scanner Palette) ---
        
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 
        
//         // --- NEW: Display previous session mood on the main screen (Header) ---
//         Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
//         welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         String lastMoodDisplay = lastSessionMood.equals(UserAuthenticator.DEFAULT_MOOD) ? 
//                                  "No previous log found." : 
//                                  lastSessionMood + " (on " + lastSessionTimestamp.substring(0, 10) + ")";

//         Label prevMoodLabel = new Label("Last Logged Mood: " + lastMoodDisplay);
//         prevMoodLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFC107; -fx-font-style: italic;");
        
//         VBox headerBox = new VBox(5, welcomeLabel, prevMoodLabel);
//         headerBox.setAlignment(Pos.CENTER);
//         headerBox.setPadding(new Insets(10, 0, 10, 0));
//         // ----------------------------------------------------------------------


//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("🚪 Log Out");
        
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
        
//         exitBtn.setOnAction(e -> {
//              // 1. Save the last analyzed mood before exiting
//             String lastMood = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_MOOD : moodHistory.get(0).mood();
//             String lastTime = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_TIME : moodHistory.get(0).timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
//             try {
//                 UserAuthenticator.saveLastMood(currentUsername, lastMood, lastTime);
//             } catch (IOException ex) {
//                 System.err.println("Failed to save last mood data for " + currentUsername + ": " + ex.getMessage());
//             }
            
//             // 2. Log out switches back to the login screen
//             showLoginScene(); 
//         });

//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         // MODIFIED: Added headerBox to imageContainer
//         VBox imageContainer = new VBox(10, headerBox, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         // --- Mood History UI ---
//         Label historyLabel = new Label("Mood History 📊");
//         historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         historyListView = new ListView<>(moodHistory);
//         historyListView.setPrefHeight(450); // Set a reasonable height
//         historyListView.setPrefWidth(280);
//         historyListView.setStyle("-fx-control-inner-background: #384A5C; -fx-font-family: 'Monospace'; -fx-text-fill: white; -fx-background-insets: 0;");
        
//         VBox historyBox = new VBox(10, historyLabel, historyListView);
//         historyBox.setPadding(new Insets(20));
//         historyBox.setPrefWidth(300);
//         // ------------------------------

//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);
//         root.setRight(historyBox); // Place the history to the right

//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         // Background loading (unchanged)
//         try {
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                         bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                         BackgroundPosition.CENTER, new BackgroundSize(
//                         BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         Scene scene = new Scene(root, 1300, 650); 
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // 2. Start the new live feed
//         startLiveFeed();
//     }

//     // -----------------------------------------------------------------
//     // ## 4. OpenCV Helper Methods (Unchanged)
//     // -----------------------------------------------------------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera != null && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { 
//                         Thread.sleep(33); 
//                     } catch (InterruptedException ignored) {
//                         break; 
//                     } 
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         cameraThread = new Thread(cameraTask); 
//         cameraThread.setDaemon(true); 
//         cameraThread.start();
//     }

//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (camera == null || !camera.isOpened()) return;
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
//             showingLiveFeed = false; 

//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("snapshot_" + timestamp + ".jpg", frame);
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (camera == null || !camera.isOpened()) {
//              showMainApplicationScene(UserAuthenticator.DEFAULT_MOOD, UserAuthenticator.DEFAULT_TIME); 
//              return;
//         }
        
//         if (!showingLiveFeed) {
//             startLiveFeed();
//         }
        
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
        
//         // Run analysis in a non-blocking way (using Task)
//         Task<String> analysisTask = new Task<String>() {
//             @Override
//             protected String call() throws Exception {
//                 Mat frameCopy = lastCapturedFrame.clone();
//                 return detectAndAnalyzeMood(frameCopy); // This mutates frameCopy and returns mood
//             }

//             @Override
//             protected void succeeded() {
//                 String currentMood = getValue();
                
//                 // 1. Update mood history
//                 MoodEntry newEntry = new MoodEntry(currentMood, LocalDateTime.now());
//                 moodHistory.add(0, newEntry); 
//                 historyListView.refresh(); 

//                 // 2. Re-run analysis on a clone just for the UI update (since it draws rectangles)
//                 Mat frameForDisplay = lastCapturedFrame.clone();
//                 detectAndAnalyzeMood(frameForDisplay); 
//                 Image processed = matToImage(frameForDisplay);
//                 imageView.setImage(processed);
                
//                 moodLabel.setText("🧠 Mood analysis complete! Detected: " + currentMood);
//                 frameForDisplay.release();
//             }

//             @Override
//             protected void failed() {
//                 moodLabel.setText("❌ Analysis failed: " + getException().getMessage());
//             }
//         };
        
//         moodLabel.setText("⏳ Analyzing mood...");
//         new Thread(analysisTask).start();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     private static String detectAndAnalyzeMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;
//         String primaryMood = "No Face Detected"; 

//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { 
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             // Fallback (Only works if FACE_PROTO is actually a Haar cascade file)
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_PROTO); 
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         if (facesArray.length > 0) {
//             Rect faceRect = facesArray[0]; 
//             Mat faceGray = grayImage.submat(faceRect);
//             primaryMood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(primaryMood);
            
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                       new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                       color, 3);
            
//             Imgproc.putText(image, "Mood: " + primaryMood,
//                       new Point(faceRect.x, faceRect.y - 10),
//                       Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//         return primaryMood;
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         // Note: smileDetector must be static and initialized outside this method
//         if (smileDetector != null) smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         // Note: eyeDetector must be static and initialized outside this method
//         if (eyeDetector != null) eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         if (numSmiles > 0)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             mood = "Neutral"; 
//         else
//             mood = "Pensive"; 

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); 
//             case "Neutral": return new Scalar(0, 255, 0); 
//             case "Pensive": return new Scalar(255, 0, 0); 
//             default: return new Scalar(255, 255, 255); 
//         }
//     }

//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             e.printStackTrace();
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }










// final 0.1

// import javafx.application.Application;
// import javafx.embed.swing.SwingFXUtils;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.ListView; 
// import javafx.scene.control.PasswordField;
// import javafx.scene.control.TextField;
// import javafx.scene.image.ImageView;
// import javafx.scene.image.Image;
// import javafx.scene.layout.*;
// import javafx.stage.Stage;
// import javafx.concurrent.Task;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList; 
// import java.nio.file.Path; 

// import org.opencv.core.*;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.videoio.VideoCapture;
// import org.opencv.dnn.Dnn;
// import org.opencv.dnn.Net;

// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// public class AppLauncher extends Application {

//     // --- OpenCV Configuration Paths (Requires these files in the execution directory) ---
//     private static final String FACE_PROTO = "deploy.prototxt";
//     private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
//     private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
//     private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
//     private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

//     private static Net faceNet;
//     private static CascadeClassifier smileDetector;
//     private static CascadeClassifier eyeDetector;
//     private static CascadeClassifier mouthDetector;
    
//     // --- Application State and Components ---
//     private Stage primaryStage;
//     private VideoCapture camera;
//     private ImageView imageView;
//     private Label moodLabel;
//     private volatile boolean showingLiveFeed = false;
//     private Thread cameraThread; 
//     private Mat lastCapturedFrame = null;
    
//     // Non-static field to track current user
//     private String currentUsername = null; 
    
//     // --- Mood Tracking Components ---
//     // Record to hold mood data
//     private static record MoodEntry(String mood, LocalDateTime timestamp) {
//         @Override
//         public String toString() {
//             return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | Mood: " + mood;
//         }
//     }
    
//     // ObservableList for JavaFX ListView
//     private ObservableList<MoodEntry> moodHistory = FXCollections.observableArrayList();
//     private ListView<MoodEntry> historyListView;
//     // ------------------------------------
    
//     /**
//      * Helper class to manage user data loaded from file.
//      */
//     private static class UserData {
//         String username;
//         String password;
//         String lastMood;
//         String lastTimestamp;

//         public UserData(String username, String password, String lastMood, String lastTimestamp) {
//             this.username = username;
//             this.password = password;
//             this.lastMood = lastMood;
//             this.lastTimestamp = lastTimestamp;
//         }
//     }
    
//     /**
//      * Nested class for simple file-based user management (simulating a database).
//      * Users are stored in 'users.txt' as username,password,last_mood,last_timestamp
//      */
//     private static class UserAuthenticator {
//         private static final String USER_FILE = "users.txt";
//         private static final String DEFAULT_MOOD = "N/A";
//         private static final String DEFAULT_TIME = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 10); // Date only

//         // Modified createUser to handle initial (empty) mood state
//         public static void createUser(String username, String password) throws IOException {
//             // New user format: username,password,DEFAULT_MOOD,DEFAULT_TIME
//             String data = username + "," + password + "," + DEFAULT_MOOD + "," + DEFAULT_TIME + "\n";
//             // Append the new user data to the file, creating it if it doesn't exist.
//             Files.write(Paths.get(USER_FILE), data.getBytes(), 
//                          java.nio.file.StandardOpenOption.CREATE, 
//                          java.nio.file.StandardOpenOption.APPEND);
//         }

//         public static boolean authenticate(String username, String password) {
//             try {
//                 // Initialize with a default user if file is missing
//                 if (!Files.exists(Paths.get(USER_FILE))) {
//                     createUser("admin", "password"); 
//                 }
                
//                 List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//                 String target = username + "," + password;
                
//                 for (String line : lines) {
//                     // Check if the beginning of the line matches the target username,password
//                     if (line.startsWith(target + ",")) {
//                         return true;
//                     }
//                 }
//                 return false;
//             } catch (IOException e) {
//                 System.err.println("Error accessing user file: " + e.getMessage());
//                 return false;
//             }
//         }
        
//         // --- NEW: Method to retrieve the last mood data upon login ---
//         public static UserData getUserData(String username) throws IOException {
//              if (!Files.exists(Paths.get(USER_FILE))) return null;

//              List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
//              for (String line : lines) {
//                  // Split by comma, limiting to 4 parts
//                  String[] parts = line.split(",", 4); 
//                  if (parts.length >= 1 && parts[0].equals(username)) {
//                      String password = parts.length > 1 ? parts[1] : "";
//                      String mood = parts.length > 2 ? parts[2] : DEFAULT_MOOD;
//                      String timestamp = parts.length > 3 ? parts[3] : DEFAULT_TIME;
//                      return new UserData(parts[0], password, mood, timestamp);
//                  }
//              }
//              return null; 
//         }
        
//         // --- NEW: Method to save the last mood data when logging out ---
//         public static void saveLastMood(String username, String mood, String timestamp) throws IOException {
//             Path filePath = Paths.get(USER_FILE);
//             if (!Files.exists(filePath)) return;

//             List<String> lines = Files.readAllLines(filePath);
//             List<String> newLines = new ArrayList<>();
            
//             boolean updated = false;
//             for (String line : lines) {
//                 if (line.startsWith(username + ",")) {
//                     // Extract existing password
//                     String[] parts = line.split(",", 2); // Split only up to password
//                     String password = parts.length > 1 ? parts[1].split(",", 2)[0] : ""; 
                    
//                     // Construct the updated line: username,password,mood,timestamp
//                     String updatedLine = parts[0] + "," + password + "," + mood + "," + timestamp;
//                     newLines.add(updatedLine);
//                     updated = true;
//                 } else {
//                     newLines.add(line);
//                 }
//             }
            
//             if (updated) {
//                  // Write all lines back, replacing the file content
//                  Files.write(filePath, newLines, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
//             }
//         }
//     }

//     public static void main(String[] args) {
//         launch(args);
//     }

//     @Override
//     public void start(Stage stage) {
//         this.primaryStage = stage;
        
//         try {
//             System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//             System.out.println("OpenCV library check passed.");
//         } catch (UnsatisfiedLinkError e) {
//             System.err.println("Failed to load OpenCV library. Ensure the native libraries are correctly configured in your system path.");
//             return; 
//         }

//         primaryStage.setOnCloseRequest(event -> {
//             stopLiveFeedAndReleaseCamera();
//         });

//         showLoginScene();
//         primaryStage.show();
//     }
    
//     private void stopLiveFeedAndReleaseCamera() {
//         showingLiveFeed = false;
//         if (cameraThread != null && cameraThread.isAlive()) {
//             try {
//                 cameraThread.interrupt(); 
//                 cameraThread.join(100); 
//             } catch (InterruptedException ignored) {
//                 Thread.currentThread().interrupt();
//             }
//         }
//         if (camera != null && camera.isOpened()) {
//             camera.release();
//             camera = null; 
//         }
//     }

//     // -----------------------------------------------------------------
//     // ## 1. Login Scene
//     // -----------------------------------------------------------------
//     private void showLoginScene() {
//         stopLiveFeedAndReleaseCamera();

//         Label title = new Label("Mood Scanner Login 👤");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         Label userLabel = new Label("Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
//         userField.setPromptText("Enter Username");
        
//         Label passLabel = new Label("Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();
//         passField.setPromptText("Enter Password");
        
//         Button loginBtn = new Button("🔑 Login");
//         loginBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button signupBtn = new Button("✍️ Create Account");
//         signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("Welcome! (Default: admin/password)");
//         messageLabel.setStyle("-fx-text-fill: #00BCD4;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, loginBtn, signupBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400); 
        
//         primaryStage.setTitle("Login Required");
//         primaryStage.setScene(scene);
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();
        
//         // Login Logic
//         loginBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (UserAuthenticator.authenticate(username, password)) {
//                 messageLabel.setText("Login Successful! Starting scanner...");
                
//                 // Set the current user and clear mood history
//                 currentUsername = username;
//                 moodHistory.clear(); 
                
//                 // --- Load previous session data ---
//                 String lastMood = UserAuthenticator.DEFAULT_MOOD;
//                 String lastTime = UserAuthenticator.DEFAULT_TIME;
//                 try {
//                     UserData data = UserAuthenticator.getUserData(username);
//                     if (data != null) {
//                         lastMood = data.lastMood;
//                         lastTime = data.lastTimestamp;
//                     }
//                 } catch (IOException ex) {
//                     System.err.println("Failed to load last mood on login: " + ex.getMessage());
//                 }

//                 // Switch to the main application scene, passing the previous mood
//                 showMainApplicationScene(lastMood, lastTime); 
//             } else {
//                 messageLabel.setText("❌ Invalid Username or Password. Try again.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//             }
//         });

//         // Signup Action
//         signupBtn.setOnAction(e -> {
//             showSignupScene();
//         });
//     }

//     // -----------------------------------------------------------------
//     // ## 2. Sign Up Scene
//     // -----------------------------------------------------------------
//     private void showSignupScene() {
//         stopLiveFeedAndReleaseCamera();
        
//         Label title = new Label("Create New Account ✍️");
//         title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         Label userLabel = new Label("New Username:");
//         userLabel.setStyle("-fx-text-fill: white;");
//         TextField userField = new TextField();
        
//         Label passLabel = new Label("New Password:");
//         passLabel.setStyle("-fx-text-fill: white;");
//         PasswordField passField = new PasswordField();

//         Button registerBtn = new Button("✅ Register");
//         registerBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Button backBtn = new Button("⬅️ Back to Login");
//         backBtn.setStyle("-fx-background-color: #384A5C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 5;"); 
        
//         Label messageLabel = new Label("");
//         messageLabel.setStyle("-fx-text-fill: white;");

//         // Layout
//         GridPane grid = new GridPane();
//         grid.setAlignment(Pos.CENTER);
//         grid.setHgap(10);
//         grid.setVgap(15);
//         grid.setPadding(new Insets(25));
        
//         grid.add(userLabel, 0, 1);
//         grid.add(userField, 1, 1);
//         grid.add(passLabel, 0, 2);
//         grid.add(passField, 1, 2);

//         HBox buttonBar = new HBox(15, registerBtn, backBtn);
//         buttonBar.setAlignment(Pos.CENTER);

//         VBox root = new VBox(25, title, grid, buttonBar, messageLabel);
//         root.setAlignment(Pos.CENTER);
//         root.setStyle("-fx-background-color: #1E2A38; -fx-padding: 50;");
        
//         Scene scene = new Scene(root, 450, 400);
//         primaryStage.setTitle("User Registration");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // Registration Logic
//         registerBtn.setOnAction(e -> {
//             String username = userField.getText().trim();
//             String password = passField.getText().trim();

//             if (username.isEmpty() || password.isEmpty()) {
//                 messageLabel.setText("❌ Username and Password cannot be empty.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 return;
//             }

//             try {
//                 // Check for existing username by attempting to get data
//                 if (UserAuthenticator.getUserData(username) != null) {
//                     messageLabel.setText("❌ Username already exists. Try logging in.");
//                     messageLabel.setStyle("-fx-text-fill: #F44336;");
//                     return;
//                 }

//                 UserAuthenticator.createUser(username, password);
//                 messageLabel.setText("🎉 Registration successful for " + username + "! Returning to login...");
//                 messageLabel.setStyle("-fx-text-fill: #00BCD4;");
                
//                 new Thread(() -> {
//                     try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//                     javafx.application.Platform.runLater(this::showLoginScene);
//                 }).start();

//             } catch (IOException ex) {
//                 messageLabel.setText("❌ Error saving user data.");
//                 messageLabel.setStyle("-fx-text-fill: #F44336;");
//                 ex.printStackTrace();
//             }
//         });

//         backBtn.setOnAction(e -> showLoginScene());
//     }

//     // -----------------------------------------------------------------
//     // ## 3. Main Application Scene (The Scanner)
//     // -----------------------------------------------------------------
//     private void showMainApplicationScene(String lastSessionMood, String lastSessionTimestamp) {
        
//         // 1. Stop and release any existing camera resources cleanly
//         stopLiveFeedAndReleaseCamera();
        
//         // Check if all necessary cascade/model files are present
//         if (!checkRequiredFilesExist()) return;

//         try {
//             // Initialize DNN/Cascades if they weren't initialized before
//             if (faceNet == null) faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
//             if (smileDetector == null) smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
//             if (eyeDetector == null) eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
//             if (mouthDetector == null) mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);
            
//             if (faceNet.empty() || smileDetector.empty() || eyeDetector.empty() || mouthDetector.empty()) {
//                 System.err.println("CRITICAL ERROR: Failed to load one or more face detection models.");
//                 return;
//             }
//             System.out.println("Face detection models loaded.");
//         } catch (Exception e) {
//             System.err.println("Error loading face detection models: " + e.getMessage());
//         }

//         // --- Camera Initialization with Retry Loop for Robustness ---
//         camera = new VideoCapture(0);
//         int maxRetries = 5;
//         int currentRetry = 0;
        
//         while (!camera.isOpened() && currentRetry < maxRetries) {
//             System.err.println("Camera failed to open. Retrying in 500ms... (Attempt " + (currentRetry + 1) + ")");
//             currentRetry++;
//             try {
//                 Thread.sleep(500); 
//             } catch (InterruptedException ignored) {}
            
//             if (camera != null && camera.isOpened()) camera.release();
//             camera = new VideoCapture(0); 
//         }
        
//         Button backToLoginBtn = new Button("⬅️ Return to Login"); 
//         backToLoginBtn.setOnAction(e -> showLoginScene()); 
        
//         if (!camera.isOpened()) {
//             Label errorLabel = new Label("❌ ERROR: Camera not found! Check connection.");
//             errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
//             VBox errorRoot = new VBox(50.0, errorLabel, backToLoginBtn);
//             errorRoot.setAlignment(Pos.CENTER);
//             errorRoot.setStyle("-fx-background-color: #1E2A38;");
//             primaryStage.setScene(new Scene(errorRoot, 450, 400));
//             primaryStage.sizeToScene();
//             primaryStage.centerOnScreen();
//             return;
//         }
//         // ----------------------------------------------------

//         // --- UI Element Setup (Scanner Palette) ---
        
//         imageView = new ImageView();
//         imageView.setFitWidth(550);
//         imageView.setFitHeight(450);
//         imageView.setPreserveRatio(true);
//         imageView.setStyle("-fx-border-color: #384A5C; -fx-border-width: 3; -fx-background-color: black;"); 

//         moodLabel = new Label("📷 Ready to capture");
//         moodLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;"); 
        
//         // --- Display previous session mood on the main screen (Header) ---
//         Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
//         welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        
//         String lastMoodDisplay = lastSessionMood.equals(UserAuthenticator.DEFAULT_MOOD) ? 
//                                  "No previous log found." : 
//                                  lastSessionMood + " (on " + lastSessionTimestamp.substring(0, 10) + ")";

//         Label prevMoodLabel = new Label("Last Logged Mood: " + lastMoodDisplay);
//         prevMoodLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFC107; -fx-font-style: italic;");
        
//         VBox headerBox = new VBox(5, welcomeLabel, prevMoodLabel);
//         headerBox.setAlignment(Pos.CENTER);
//         headerBox.setPadding(new Insets(10, 0, 10, 0));
//         // ----------------------------------------------------------------------


//         Button takePicBtn = new Button("📸 Capture Moment");
//         Button retakeBtn = new Button("🔁 Recompose");
//         Button analyzeBtn = new Button("🧠 Scan Mood");
//         Button exitBtn = new Button("🚪 Log Out");
        
//         String buttonStyleBase = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;";
//         takePicBtn.setStyle("-fx-background-color: #FF5722;" + buttonStyleBase); 
//         retakeBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #1E2A38; -fx-font-weight: bold; -fx-padding: 15 25; -fx-font-size: 14px; -fx-background-radius: 5;"); 
//         analyzeBtn.setStyle("-fx-background-color: #2196F3;" + buttonStyleBase); 
//         exitBtn.setStyle("-fx-background-color: #F44336;" + buttonStyleBase); 

//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);

//         takePicBtn.setOnAction(e -> takePicture(takePicBtn, retakeBtn, analyzeBtn));
//         retakeBtn.setOnAction(e -> retakePicture(takePicBtn, retakeBtn, analyzeBtn));
//         analyzeBtn.setOnAction(e -> analyzeMoodFromCapture());
        
//         exitBtn.setOnAction(e -> {
//              // 1. Save the last analyzed mood before exiting
//             String lastMood = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_MOOD : moodHistory.get(0).mood();
//             String lastTime = moodHistory.isEmpty() ? UserAuthenticator.DEFAULT_TIME : moodHistory.get(0).timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
//             try {
//                 UserAuthenticator.saveLastMood(currentUsername, lastMood, lastTime);
//             } catch (IOException ex) {
//                 System.err.println("Failed to save last mood data for " + currentUsername + ": " + ex.getMessage());
//             }
            
//             // 2. Log out switches back to the login screen
//             showLoginScene(); 
//         });

//         VBox buttonBox = new VBox(20, takePicBtn, retakeBtn, analyzeBtn, exitBtn);
//         buttonBox.setAlignment(Pos.CENTER);
//         buttonBox.setPadding(new Insets(50, 20, 20, 20));

//         VBox imageContainer = new VBox(10, headerBox, imageView, moodLabel);
//         imageContainer.setAlignment(Pos.CENTER);
//         imageContainer.setPadding(new Insets(20));

//         // --- Mood History UI ---
//         Label historyLabel = new Label("Mood History 📊");
//         historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
//         historyListView = new ListView<>(moodHistory);
//         historyListView.setPrefHeight(450); 
//         historyListView.setPrefWidth(280);
//         historyListView.setStyle("-fx-control-inner-background: #384A5C; -fx-font-family: 'Monospace'; -fx-text-fill: white; -fx-background-insets: 0;");
        
//         VBox historyBox = new VBox(10, historyLabel, historyListView);
//         historyBox.setPadding(new Insets(20));
//         historyBox.setPrefWidth(300);
//         // ------------------------------

//         HBox centerBox = new HBox(30, imageContainer, buttonBox);
//         centerBox.setAlignment(Pos.CENTER);

//         BorderPane root = new BorderPane();
//         root.setCenter(centerBox);
//         root.setRight(historyBox); // Place the history to the right

//         root.setStyle("-fx-background-color: #1E2A38;"); 

//         // Background loading 
//         try {
//             Image bgImage = new Image(getClass().getResource("/bg.jpg").toExternalForm());
//             BackgroundImage backgroundImage = new BackgroundImage(
//                         bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                         BackgroundPosition.CENTER, new BackgroundSize(
//                         BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
//             );
//             root.setBackground(new Background(backgroundImage));
//         } catch (Exception ex) {
//             System.err.println("⚠️ Could not load background image. Using solid color background.");
//         }
        
//         Scene scene = new Scene(root, 1300, 650); 
//         primaryStage.setTitle("Mood Scanner v2.0 | Harmonious Tech");
//         primaryStage.setScene(scene); 
//         primaryStage.sizeToScene(); 
//         primaryStage.centerOnScreen();

//         // 2. Start the new live feed
//         startLiveFeed();
//     }

//     // -----------------------------------------------------------------
//     // ## 4. OpenCV Helper Methods
//     // -----------------------------------------------------------------

//     private void startLiveFeed() {
//         showingLiveFeed = true;
//         Task<Void> cameraTask = new Task<>() {
//             @Override
//             protected Void call() {
//                 Mat frame = new Mat();
//                 while (showingLiveFeed && camera != null && camera.isOpened()) {
//                     if (camera.read(frame)) {
//                         Image image = matToImage(frame);
//                         javafx.application.Platform.runLater(() -> imageView.setImage(image));
//                     }
//                     try { 
//                         Thread.sleep(33); 
//                     } catch (InterruptedException ignored) {
//                         break; 
//                     } 
//                 }
//                 frame.release();
//                 return null;
//             }
//         };
//         cameraThread = new Thread(cameraTask); 
//         cameraThread.setDaemon(true); 
//         cameraThread.start();
//     }

//     /**
//      * Creates a directory if it does not exist.
//      */
//     private void createPhotoDirectory(String directoryName) {
//         try {
//             Path dirPath = Paths.get(directoryName);
//             if (!Files.exists(dirPath)) {
//                 Files.createDirectories(dirPath);
//                 System.out.println("Created directory: " + directoryName);
//             }
//         } catch (IOException e) {
//             System.err.println("Failed to create directory " + directoryName + ": " + e.getMessage());
//         }
//     }


//     private void takePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         if (camera == null || !camera.isOpened()) return;
        
//         // --- MODIFIED: Ensure the photos directory exists and save there ---
//         createPhotoDirectory("photo");
//         // ------------------------------------------------------------------
        
//         Mat frame = new Mat();
//         if (camera.read(frame)) {
//             lastCapturedFrame = frame.clone();
            
//             // Stop the live feed to freeze the captured frame
//             showingLiveFeed = false; 

//             Image image = matToImage(frame);
//             imageView.setImage(image);

//             // Save snapshot to the 'photo/' directory
//             String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//             Imgcodecs.imwrite("photo/snapshot_" + timestamp + ".jpg", frame); 
            
//             moodLabel.setText("✅ Picture captured! Ready for analysis.");
//         }
//         takePicBtn.setDisable(true);
//         retakeBtn.setDisable(false);
//         analyzeBtn.setDisable(false);
//     }

//     private void retakePicture(Button takePicBtn, Button retakeBtn, Button analyzeBtn) {
//         moodLabel.setText("📷 Ready to capture");
//         if (camera == null || !camera.isOpened()) {
//              showMainApplicationScene(UserAuthenticator.DEFAULT_MOOD, UserAuthenticator.DEFAULT_TIME); 
//              return;
//         }
        
//         if (!showingLiveFeed) {
//             startLiveFeed();
//         }
        
//         takePicBtn.setDisable(false);
//         retakeBtn.setDisable(true);
//         analyzeBtn.setDisable(true);
//     }

//     private void analyzeMoodFromCapture() {
//         if (lastCapturedFrame == null) {
//             moodLabel.setText("⚠️ No image captured yet!");
//             return;
//         }
        
//         // Run analysis in a non-blocking way (using Task)
//         Task<String> analysisTask = new Task<String>() {
//             @Override
//             protected String call() throws Exception {
//                 Mat frameCopy = lastCapturedFrame.clone();
//                 return detectAndAnalyzeMood(frameCopy); // This mutates frameCopy and returns mood
//             }

//             @Override
//             protected void succeeded() {
//                 String currentMood = getValue();
                
//                 // 1. Update mood history
//                 MoodEntry newEntry = new MoodEntry(currentMood, LocalDateTime.now());
//                 moodHistory.add(0, newEntry); 
//                 historyListView.refresh(); 

//                 // 2. Re-run analysis on a clone just for the UI update (since it draws rectangles)
//                 Mat frameForDisplay = lastCapturedFrame.clone();
//                 detectAndAnalyzeMood(frameForDisplay); 
//                 Image processed = matToImage(frameForDisplay);
//                 imageView.setImage(processed);
                
//                 moodLabel.setText("🧠 Mood analysis complete! Detected: " + currentMood);
//                 frameForDisplay.release();
//             }

//             @Override
//             protected void failed() {
//                 moodLabel.setText("❌ Analysis failed: " + getException().getMessage());
//             }
//         };
        
//         moodLabel.setText("⏳ Analyzing mood...");
//         new Thread(analysisTask).start();
//     }

//     private static boolean checkRequiredFilesExist() {
//         boolean allFound = true;
//         if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
//         if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
//         if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
//         if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
//         return allFound;
//     }

//     private static String detectAndAnalyzeMood(Mat image) {
//         Mat grayImage = new Mat();
//         Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
//         Rect[] facesArray;
//         String primaryMood = "No Face Detected"; 

//         if (faceNet != null) {
//             Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
//                     new Scalar(104.0, 177.0, 123.0), false, false);
//             faceNet.setInput(blob);
//             Mat detections = faceNet.forward();
//             int cols = image.cols();
//             int rows = image.rows();
//             Mat detectionMat = detections.reshape(1, (int) detections.size(2));
//             List<Rect> dnnFaces = new ArrayList<>();
            
//             for (int i = 0; i < detectionMat.rows(); i++) {
//                 double confidence = detectionMat.get(i, 2)[0];
//                 if (confidence > 0.5) { 
//                     int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
//                     int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
//                     int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
//                     int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
//                     dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
//                 }
//             }
//             facesArray = dnnFaces.toArray(new Rect[0]);
//             blob.release();
//             detections.release();
//         } else {
//             // Fallback (Only works if FACE_PROTO is actually a Haar cascade file)
//             CascadeClassifier faceDetector = new CascadeClassifier(FACE_PROTO); 
//             MatOfRect faceDetections = new MatOfRect();
//             faceDetector.detectMultiScale(grayImage, faceDetections);
//             facesArray = faceDetections.toArray();
//             faceDetections.release();
//         }

//         if (facesArray.length > 0) {
//             Rect faceRect = facesArray[0]; 
//             Mat faceGray = grayImage.submat(faceRect);
//             primaryMood = analyzeMoodHeuristic(faceGray, faceRect.width);
//             faceGray.release();
//             Scalar color = getMoodColor(primaryMood);
            
//             Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
//                       new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
//                       color, 3);
            
//             Imgproc.putText(image, "Mood: " + primaryMood,
//                       new Point(faceRect.x, faceRect.y - 10),
//                       Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
//         }
//         grayImage.release();
//         return primaryMood;
//     }

//     private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
//         MatOfRect smileDetections = new MatOfRect();
//         Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat smileRegion = faceRegionGray.submat(lowerFace);
//         // Note: smileDetector must be static and initialized outside this method
//         if (smileDetector != null) smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

//         MatOfRect eyeDetections = new MatOfRect();
//         Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
//         Mat eyeRegion = faceRegionGray.submat(upperFace);
//         // Note: eyeDetector must be static and initialized outside this method
//         if (eyeDetector != null) eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

//         String mood;
//         int numSmiles = smileDetections.toArray().length;
//         int numEyes = eyeDetections.toArray().length;
        
//         if (numSmiles > 0)
//             mood = "Happy"; 
//         else if (numEyes >= 2 && numSmiles == 0)
//             mood = "Neutral"; 
//         else
//             mood = "Pensive"; 

//         smileRegion.release();
//         eyeRegion.release();
//         smileDetections.release();
//         eyeDetections.release();
//         return mood;
//     }

//     private static Scalar getMoodColor(String mood) {
//         switch (mood) {
//             case "Happy": return new Scalar(0, 255, 255); 
//             case "Neutral": return new Scalar(0, 255, 0); 
//             case "Pensive": return new Scalar(255, 0, 0); 
//             default: return new Scalar(255, 255, 255); 
//         }
//     }

//     private static BufferedImage matToBufferedImage(Mat original) {
//         MatOfByte mob = new MatOfByte();
//         Imgcodecs.imencode(".jpg", original, mob);
//         try {
//             return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//         } catch (IOException e) {
//             e.printStackTrace();
//             return null;
//         }
//     }

//     private static Image matToImage(Mat mat) {
//         BufferedImage bufferedImage = matToBufferedImage(mat);
//         return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
//     }
// }
