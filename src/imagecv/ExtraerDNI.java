package imagecv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ExtraerDNI {
    public static Mat init(String image) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Cargar la imagen
        String imagePath = image;
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            System.out.println("No se pudo abrir la imagen.");
            return src;
        }

        // Convertir la imagen a escala de grises
        Mat gris = new Mat();
        Imgproc.cvtColor(src, gris, Imgproc.COLOR_BGR2GRAY);

        // Aplicar un filtro de suavizado para reducir el ruido
        Imgproc.GaussianBlur(gris, gris, new Size(5, 5), 0);

        // Detectar los bordes usando el detector de bordes de Canny
        Mat bordes = new Mat();
        Imgproc.Canny(gris, bordes, 75, 200);

        // Encontrar contornos
        List<MatOfPoint> contornos = new ArrayList<>();
        Mat jerarquia = new Mat();
        Imgproc.findContours(bordes, contornos, jerarquia, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Calcular el 50% del área total de la imagen
        double halfTotalArea = 0.5 * src.rows() * src.cols();

        // Seleccionar el contorno del dni
        Rect rectFolio = null;
        double maxArea = 0;
        for (MatOfPoint contorno : contornos) {
            Rect rect = Imgproc.boundingRect(contorno);
            double area = rect.area();
            if (area > maxArea && area < halfTotalArea) {
                maxArea = area;
                rectFolio = rect;
            }
        }
        Mat Dni = null;
        if (rectFolio != null) {
            // Recortar y guardar la región del folio
            Dni = new Mat(src, rectFolio);
            Sesion.getInstance().setDnicoordenadas(rectFolio);
            Imgcodecs.imwrite("src/img/Dni.png", Dni);
            System.out.println("Folio detectado y guardado como folio.png");
            return Dni;
        } else {
            System.out.println("No se detectó el folio.");
        }
        try {
            return Dni;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


