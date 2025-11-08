import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MoodDetector {
    // --- OpenCV Configuration Paths ---
    private static final String FACE_PROTO = "deploy.prototxt";
    private static final String FACE_MODEL = "res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String SMILE_CASCADE_PATH = "haarcascade_smile.xml";
    private static final String EYE_CASCADE_PATH = "haarcascade_eye.xml";
    private static final String MOUTH_CASCADE_PATH = "haarcascade_mcs_mouth.xml";

    private static Net faceNet;
    private static CascadeClassifier smileDetector;
    private static CascadeClassifier eyeDetector;
    private static CascadeClassifier mouthDetector;

    public static boolean checkRequiredFilesExist() {
        boolean allFound = true;
        if (!Files.exists(Paths.get(FACE_PROTO))) { System.err.println("Missing: " + FACE_PROTO); allFound = false; }
        if (!Files.exists(Paths.get(FACE_MODEL))) { System.err.println("Missing: " + FACE_MODEL); allFound = false; }
        if (!Files.exists(Paths.get(SMILE_CASCADE_PATH))) { System.err.println("Missing: " + SMILE_CASCADE_PATH); allFound = false; }
        if (!Files.exists(Paths.get(EYE_CASCADE_PATH))) { System.err.println("Missing: " + EYE_CASCADE_PATH); allFound = false; }
        if (!Files.exists(Paths.get(MOUTH_CASCADE_PATH))) { System.err.println("Missing: " + MOUTH_CASCADE_PATH); allFound = false; }
        return allFound;
    }
    
    public static boolean isInitialized() {
        return faceNet != null;
    }

    public static void initOpenCV() throws Exception {
        if (!checkRequiredFilesExist()) {
             throw new Exception("One or more required model files are missing.");
        }
        faceNet = Dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL);
        smileDetector = new CascadeClassifier(SMILE_CASCADE_PATH);
        eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
        mouthDetector = new CascadeClassifier(MOUTH_CASCADE_PATH);

        if (faceNet.empty() || smileDetector.empty() || eyeDetector.empty() || mouthDetector.empty()) {
             throw new Exception("Failed to load one or more face detection models (Empty objects).");
        }
    }
    
    public static void saveImage(String filePath, Mat image) {
        Imgcodecs.imwrite(filePath, image);
    }

    public static String detectAndAnalyzeMood(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Rect[] facesArray;
        String primaryMood = "No Face Detected"; 

        if (faceNet != null) {
            Mat blob = Dnn.blobFromImage(image, 1.0, new Size(300, 300),
                    new Scalar(104.0, 177.0, 123.0), false, false);
            faceNet.setInput(blob);
            Mat detections = faceNet.forward();
            int cols = image.cols();
            int rows = image.rows();
            Mat detectionMat = detections.reshape(1, (int) detections.size(2));
            List<Rect> dnnFaces = new ArrayList<>();
            
            for (int i = 0; i < detectionMat.rows(); i++) {
                double confidence = detectionMat.get(i, 2)[0];
                if (confidence > 0.5) { 
                    int x1 = (int) (detectionMat.get(i, 3)[0] * cols);
                    int y1 = (int) (detectionMat.get(i, 4)[0] * rows);
                    int x2 = (int) (detectionMat.get(i, 5)[0] * cols);
                    int y2 = (int) (detectionMat.get(i, 6)[0] * rows);
                    dnnFaces.add(new Rect(x1, y1, x2 - x1, y2 - y1));
                }
            }
            facesArray = dnnFaces.toArray(new Rect[0]);
            blob.release();
            detections.release();
        } else {
            // Fallback: If faceNet is null (shouldn't happen if initialized properly)
            // Use a dummy detector or throw error
            facesArray = new Rect[0];
            primaryMood = "Detector Not Initialized";
        }

        if (facesArray.length > 0) {
            Rect faceRect = facesArray[0]; 
            Mat faceGray = grayImage.submat(faceRect);
            primaryMood = analyzeMoodHeuristic(faceGray, faceRect.width);
            faceGray.release();
            Scalar color = getMoodColor(primaryMood);
            
            // Draw rectangle and text on the original image (Mat 'image')
            Imgproc.rectangle(image, new Point(faceRect.x, faceRect.y),
                      new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height),
                      color, 3);
            
            Imgproc.putText(image, "Mood: " + primaryMood,
                      new Point(faceRect.x, faceRect.y - 10),
                      Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
        }
        grayImage.release();
        return primaryMood;
    }

    private static String analyzeMoodHeuristic(Mat faceRegionGray, int faceWidth) {
        MatOfRect smileDetections = new MatOfRect();
        Rect lowerFace = new Rect(0, faceRegionGray.height() / 2, faceRegionGray.width(), faceRegionGray.height() / 2);
        Mat smileRegion = faceRegionGray.submat(lowerFace);
        
        // Use the initialized static detectors
        if (smileDetector != null) smileDetector.detectMultiScale(smileRegion, smileDetections, 1.7, 20, 0, new Size(25, 25), new Size());

        MatOfRect eyeDetections = new MatOfRect();
        Rect upperFace = new Rect(0, 0, faceRegionGray.width(), faceRegionGray.height() / 2);
        Mat eyeRegion = faceRegionGray.submat(upperFace);
        
        if (eyeDetector != null) eyeDetector.detectMultiScale(eyeRegion, eyeDetections, 1.1, 2, 0, new Size(20, 20), new Size());

        String mood;
        int numSmiles = smileDetections.toArray().length;
        int numEyes = eyeDetections.toArray().length;
        
        //test
        System.out.println(numSmiles);
        System.out.println(numEyes);
        System.out.println();
        //test



        if (numSmiles >= 1 && numEyes==0)
            mood = "Happy"; 
        else if (numSmiles==0 && numEyes >= 2) // Changed from 2 to 1 for more robustness
            mood = "Angry"; 
        else if(numSmiles==1 &&numEyes==1)
            mood = "Pensive"; 
        else 
            mood="Neutral"    ;

        smileRegion.release();
        eyeRegion.release();
        smileDetections.release();
        eyeDetections.release();
        return mood;
    }

    private static Scalar getMoodColor(String mood) {
        switch (mood) {
            case "Happy": return new Scalar(0, 255, 255); // Yellow
            case "Neutral": return new Scalar(0, 255, 0); // Green
            case "Pensive": return new Scalar(255, 0, 0); // Blue (BGR format)
            default: return new Scalar(255, 255, 255); // White
        }
    }
}


