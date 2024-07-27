package imagecv;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class auxiliar {
    /**
     * Calcula la distancia entre dos puntos.
     *
     * @param p1 Punto 1
     * @param p2 Punto 2
     * @return La distancia entre los puntos
     */
    static double distancia(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Preprocesa la imagen para preparar su análisis.
     *
     * @param src La imagen original
     * @return La imagen preprocesada
     */
    static Mat preprocesarImagen(Mat src) {
        // Convertir la imagen a escala de grises
        Mat gris = new Mat();
        Imgproc.cvtColor(src, gris, Imgproc.COLOR_BGR2GRAY);

        // Aplicar un filtro de suavizado para reducir el ruido
        Mat suavizado = new Mat();
        Imgproc.GaussianBlur(gris, suavizado, new Size(5, 5), 0);

        // Aplicar un filtro bilateral para preservar los bordes mientras se reduce el ruido
        Mat bilateral = new Mat();
        Imgproc.bilateralFilter(suavizado, bilateral, 9, 75, 75);


        // Ecualizar el histograma para mejorar el contraste
        Mat binaria = new Mat();
        Imgproc.adaptiveThreshold(bilateral, binaria, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 1);
        // Invierte la imagen binaria
        Core.bitwise_not(binaria, binaria);
        //Dilatación y erosion
        Imgproc.dilate(binaria, binaria, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(binaria, binaria, new Mat(), new Point(-1, -1), 1);

        // Aplicar la detección de bordes utilizando el algoritmo de Canny
        Mat bordes = new Mat();
        Imgproc.Canny(binaria, bordes, 50, 150);

        return bordes;
    }

    /**
     * Aplica la corrección gamma a la imagen.
     *
     * @param src La imagen de origen
     * @param gamma El valor de gamma
     * @return La imagen con corrección gamma aplicada
     */
    private static Mat aplicarCorreccionGamma(Mat src, double gamma) {
        Mat lut = new Mat(1, 256, CvType.CV_8U);
        lut.setTo(new Scalar(0));
        for (int i = 0; i < 256; i++) {
            lut.put(0, i, Math.pow(i / 255.0, gamma) * 255.0);
        }
        Mat corregida = new Mat();
        Core.LUT(src, lut, corregida);
        return corregida;
    }

    /**
     * Detecta bordes en la imagen utilizando el algoritmo de Canny.
     *
     * @param src La imagen de origen
     * @return La imagen con los bordes detectados
     */
    static Mat detectarBordes(Mat src) {
        Mat bordes = new Mat();
        Imgproc.Canny(src, bordes, 100, 150);
        return bordes;
    }

    /**
     * Verifica si un rectángulo está solapando con algún rectángulo existente.
     *
     * @param rect1 El rectángulo a verificar
     * @param rectangulos La lista de rectángulos existentes
     * @return Verdadero si hay solapamiento, falso de lo contrario
     */
    static boolean estaSolapando(Rect rect1, List<Rect> rectangulos) {
        double margen = 1; // Ajusta este valor según sea necesario

        for (Rect rect2 : rectangulos) {
            if (rect1.x < rect2.x + rect2.width + margen &&
                    rect1.x + rect1.width + margen > rect2.x &&
                    rect1.y < rect2.y + rect2.height + margen &&
                    rect1.y + rect1.height + margen > rect2.y) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si dos rectángulos se superponen.
     *
     * @param r1 Rectángulo 1
     * @param r2 Rectángulo 2
     * @return Verdadero si se superponen, falso de lo contrario
     */
    private static boolean superponen(Rect r1, Rect r2) {
        return r1.x < r2.x + r2.width &&
                r1.x + r1.width > r2.x &&
                r1.y < r2.y + r2.height &&
                r1.y + r1.height > r2.y;
    }
}
