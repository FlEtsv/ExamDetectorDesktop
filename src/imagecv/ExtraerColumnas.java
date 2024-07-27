package imagecv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ExtraerColumnas {

    public static List<Mat> init(String image) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Ruta de la imagen
        String imagePath = image;
        List<Mat> columnas = extraerColumnas(imagePath);

        // Procesar cada columna para encontrar cosas dentro de ellas
        int columnaNumero = 1;
        for (Mat columna : columnas) {
            String outputPath = "src/img/columna_" + columnaNumero + ".png";
            Imgcodecs.imwrite(outputPath, columna);
            System.out.println("Columna " + columnaNumero + " guardada como " + outputPath);
            columnaNumero++;
        }
        return columnas;
    }

    public static List<Mat> extraerColumnas(String imagePath) {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            System.out.println("No se pudo abrir la imagen.");
            return new ArrayList<>();
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

        // Ordenar los contornos por su posición en el eje X (de izquierda a derecha)
        contornos.sort(Comparator.comparingInt(c -> Imgproc.boundingRect(c).x));

        // Calcular el área total de la imagen
        double totalArea = src.rows() * src.cols();
        double minArea = totalArea * 0.024; // 5% del área total

        // Seleccionar las 5 columnas más grandes que cumplan con la relación de aspecto, área mínima y la distancia mínima entre centros
        List<Rect> columnas = new ArrayList<>();
        int distanciaMinima = 50; // distancia mínima en píxeles entre los centros de las columnas
        for (MatOfPoint contorno : contornos) {
            Rect rect = Imgproc.boundingRect(contorno);
            double aspectRatio = (double) rect.height / rect.width;
            if (aspectRatio >= 2.5 && rect.area() >= minArea) {
                boolean solapado = false;
                Point centroRect = new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
                for (Rect columnaExistente : columnas) {
                    Point centroExistente = new Point(columnaExistente.x + columnaExistente.width / 2.0, columnaExistente.y + columnaExistente.height / 2.0);
                    double distancia = Math.sqrt(Math.pow(centroRect.x - centroExistente.x, 2) + Math.pow(centroRect.y - centroExistente.y, 2));
                    if (distancia < distanciaMinima) {
                        solapado = true;
                        break;
                    }
                }
                if (!solapado) {
                    columnas.add(rect);
                    Sesion.getInstance().coordenadasColumnas.add(rect);
                    if (columnas.size() == 5) break;
                }
            }
        }

        // Recortar y guardar cada columna como una nueva imagen
        List<Mat> imagenesColumnas = new ArrayList<>();
        for (Rect columna : columnas) {
            Mat columnaImg = new Mat(src, columna);
            imagenesColumnas.add(columnaImg);
        }

        return imagenesColumnas;
    }
}
