package imagecv;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class verificacionFormas {


    /**
     * Verifica si un rectángulo cumple con los criterios para ser considerado un rectángulo válido.
     *
     * @param rect El rectángulo a verificar
     * @return Verdadero si cumple con los criterios, falso de lo contrario
     */
    static boolean esRectangulo(Rect rect, Mat img) {
        double minWidthPercent = 0.1; // 1% del ancho total
        double minAreaPercent = 5.5; // 2.5% del área total

        double totalWidth = img.cols();
        double totalArea = img.rows() * img.cols();

        double rectWidthPercent = (rect.width / totalWidth) * 100;
        double rectAreaPercent = (rect.area() / totalArea) * 100;


        return rect.width > rect.height * 3 && rectWidthPercent > minWidthPercent && rectAreaPercent > minAreaPercent;
    }
    /**
     * Verifica si un rectángulo cumple con los criterios para ser considerado un rectángulo válido.
     */
    static boolean esRectanguloTorcido(Rect rect, Mat img) {
        double minWidthPercent = 0.1; // 1% del ancho total
        double minAreaPercent = 3; // 3% del área total

        double totalWidth = img.cols();
        double totalArea = img.rows() * img.cols();

        double rectWidthPercent = (rect.width / totalWidth) * 100;
        double rectAreaPercent = (rect.area() / totalArea) * 100;

        // Ajustar los límites de la relación de aspecto
        double aspectRatio = (double) rect.width / rect.height;
        double lowerBound = 2.5; // mínimo relación de aspecto (más ancho que alto)
        double upperBound = 3.5; // máximo relación de aspecto (para permitir cierta inclinación)

        return aspectRatio > lowerBound && aspectRatio < upperBound && rectWidthPercent > minWidthPercent && rectAreaPercent > minAreaPercent;
    }

    /**
     * Verifica si el círculo está relleno o no.
     *
     * @param src La imagen original
     * @param centro El centro del círculo
     * @param radio El radio del círculo
     * @return Verdadero si el círculo está relleno, falso de lo contrario
     */
    static boolean verificarRelleno(Mat src, Point centro, int radio) {
        // Calcular la intensidad media de los píxeles dentro del círculo
        int sumaIntensidades = 0;
        int contadorPixeles = 0;
        for (int y = (int) (centro.y - radio); y <= (int) (centro.y + radio); y++) {
            for (int x = (int) (centro.x - radio); x <= (int) (centro.x + radio); x++) {
                if (Math.pow(x - centro.x, 2) + Math.pow(y - centro.y, 2) <= Math.pow(radio, 2)) {
                    if (y >= 0 && y < src.rows() && x >= 0 && x < src.cols()) {
                        double[] pixel = src.get(y, x);
                        int intensidad = (int) pixel[0];
                        sumaIntensidades += intensidad;
                        contadorPixeles++;
                    }
                }
            }
        }
        // Calcular la intensidad media
        double intensidadMedia = (double) sumaIntensidades / contadorPixeles;


        return intensidadMedia < 100;
    }

    /**
     * Asigna una posición alfabética basada en el índice del círculo.
     *
     * @param indiceCirculo El índice del círculo
     * @return La posición alfabética
     */
    static char asignarPosicionAlfabetica(int indiceCirculo) {
        // Convertir el índice del círculo a una posición alfabética
        return (char) ('A' + indiceCirculo);
    }
    static boolean esCasiCuadrado(Rect rect, Mat img) {
        double minHeightPercent = 3; // 5% de la altura total
        double minAreaPercent = 16; // 0.5% del área total

        double totalHeight = img.rows();
        double totalArea = img.rows() * img.cols();

        double rectHeightPercent = (rect.height / totalHeight) * 100;
        double rectAreaPercent = (rect.area() / totalArea) * 100;

        return rect.height > rect.width * 1.18 && rectHeightPercent > minHeightPercent && rectAreaPercent > minAreaPercent;
    }
}
