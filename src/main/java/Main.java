import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("Version: " + Core.VERSION);
    }

    static double B = Math.random() * 256.0;
    static double R = Math.random() * 256.0;
    static double G = Math.random() * 256.0;

    public static void main(String[] args) {
        JFrame window = new JFrame("Window");
        JLabel screen = new JLabel();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        VideoCapture cap = new VideoCapture(0);

        Mat frame = new Mat();
        MatOfByte buf = new MatOfByte();
        ImageIcon ic;

        while (true) {
            cap.read(frame);
            Imgcodecs.imencode(".png", obr(frame), buf);
            ic = new ImageIcon(buf.toArray());
            screen.setIcon(ic);
            screen.repaint();
            window.setContentPane(screen);
            window.pack();
        }
    }

    public static Mat obr(Mat img) {
        String path = "src\\main\\resources\\";

        CascadeClassifier face_detector = new CascadeClassifier();
        CascadeClassifier eye_detector = new CascadeClassifier();
        CascadeClassifier smile_detector = new CascadeClassifier();
        String face = "haarcascade_frontalface_alt2.xml";
        String eye = "haarcascade_eye.xml";

        if (!face_detector.load(path + face)) {
            System.out.println("Error face");
        }
        if (!eye_detector.load(path + eye)) {
            System.out.println("Error eye");
        }

        MatOfRect faces = new MatOfRect();
        face_detector.detectMultiScale(img, faces);
        for (Rect r : faces.toList()) {
            Imgproc.rectangle(img, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height),
                    new Scalar(B, R, G), 2);
            Imgproc.putText(img, "Face", new Point(r.x, r.y - 20),
                    4, 1,new Scalar(255,255,255));

            Mat faceMat = img.submat(r);

            MatOfRect eyes = new MatOfRect();
            eye_detector.detectMultiScale(faceMat, eyes);
            for (Rect r2 : eyes.toList()) {
                Imgproc.rectangle(
                        faceMat,
                        new Point(r2.x, r2.y),
                        new Point(r2.x + r2.width, r2.y + r2.height),
                        new Scalar(B, G, R),
                        2
                );
                Imgproc.putText(faceMat, "Eye", new Point(r2.x, r2.y - 20),
                        4, 1,new Scalar(255,255,255));
            }

        }
        return img;
    }

    public static void showImage(Mat img, String title) {
        BufferedImage im = MatToBufferedImage(img);
        if (im == null) return;
        int w = 1000, h = 600;
        JFrame window = new JFrame(title);
        window.setSize(w, h);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon imageIcon = new ImageIcon(im);
        JLabel label = new JLabel(imageIcon);
        JScrollPane pane = new JScrollPane(label);
        window.setContentPane(pane);
        if (im.getWidth() < w && im.getHeight() < h) {
            window.pack();
        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static BufferedImage MatToBufferedImage(Mat m) {
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U) {
        } else if (m.depth() == CvType.CV_16U) { // CV_16U => CV_8U
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            m = m_16;
        } else if (m.depth() == CvType.CV_32F) { // CV_32F => CV_8U
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        } else
            return null;
        int type = 0;
        if (m.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else if (m.channels() == 3)
            type = BufferedImage.TYPE_3BYTE_BGR;
        else if (m.channels() == 4)
            type = BufferedImage.TYPE_4BYTE_ABGR;
        else
            return null;
        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        byte tmp = 0;
        if (m.channels() == 4) { // BGRA => ABGR
            for (int i = 0; i < buf.length; i += 4) {
                tmp = buf[i + 3];
                buf[i + 3] = buf[i + 2];
                buf[i + 2] = buf[i + 1];
                buf[i + 1] = buf[i];
                buf[i] = tmp;
            }
        }
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] data =
                ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, data, 0, buf.length);
        return image;
    }
}
