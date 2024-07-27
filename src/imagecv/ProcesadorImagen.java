package imagecv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static imagecv.auxiliar.*;
import static imagecv.verificacionFormas.*;

public class ProcesadorImagen {
    /***
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String rutaImagen = "src/img/fotoAlumnoDNI8.png";

        Mat dni = ExtraerDNI.init(rutaImagen);
        List<Mat> columnas = ExtraerColumnas.init(rutaImagen);

        // Procesar cada columna y el DNI
        for (int i = 0; i < columnas.size(); i++) {
            detectarColumnas(columnas.get(i), 0, i);
        }
        detectarColumnas(dni, 1, columnas.size());
    }
     **/

    /**
     * Detecta columnas en la imagen especificada.
     *
     * @param imagen La imagen a procesar
     * @param tipo   El tipo de procesamiento (0 para columnas, 1 para DNI)
     * @param contador El contador de imágenes procesadas
     */
    public static void detectarColumnas(Mat imagen, int tipo, int contador, Mat orignalImagen, Rect cordenadas) {
        if (imagen.empty()) {
            System.out.println("No se pudo abrir la imagen.");
            return;
        }

        Mat imagenProcesada = auxiliar.preprocesarImagen(imagen);

        // Guardar la imagen preprocesada
        Imgcodecs.imwrite("src/img/preprocesada" + contador + ".png", imagenProcesada);


        Rect rect = new Rect(0, 0, imagen.cols(), imagen.rows());

        if (tipo == 0) {
            detectarRectangulos(imagenProcesada, imagen, rect, contador,orignalImagen, cordenadas);
        } else {
            detectarRectangulosDNI(imagenProcesada, imagen, rect, contador,orignalImagen);
        }

        // Guardar la imagen resultante
        Imgcodecs.imwrite("src/img/result" + contador + ".png", imagen);

    }

    private static void detectarRectangulosDNI(Mat bordes, Mat src, Rect rect, int contador,Mat OriginalImagen) {
        Rect RecuadroDniRecorteimagenOriginal  = Sesion.getInstance().getDnicoordenadas();
        int contadorRectangulos = 0;
        Mat imagenColumna = new Mat(bordes, rect);
        List<MatOfPoint> contornosInternos = new ArrayList<>();
        Imgproc.findContours(imagenColumna, contornosInternos, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> rectangulosDetectadosCasiCuadrados = new ArrayList<>();

        for (MatOfPoint contornoInterno : contornosInternos) {
            Rect rectInterno = Imgproc.boundingRect(contornoInterno);
            if (esCasiCuadrado(rectInterno, imagenColumna)) {
                // Ajustar las coordenadas del rectángulo
                rectInterno.x += rect.x;
                rectInterno.y += rect.y;

                // Verificar si el rectángulo está duplicado
                boolean duplicado = false;
                for (int i = 0; i < rectangulosDetectadosCasiCuadrados.size(); i++) {
                    Rect rectExistente = rectangulosDetectadosCasiCuadrados.get(i);
                    if (Math.abs(rectExistente.x - rectInterno.x) < 10 && Math.abs(rectExistente.y - rectInterno.y) < 10 &&
                            Math.abs(rectExistente.width - rectInterno.width) < 10 && Math.abs(rectExistente.height - rectInterno.height) < 10) {
                        double aspectRatioInterno = rectInterno.width / (double) rectInterno.height;
                        double aspectRatioExistente = rectExistente.width / (double) rectExistente.height;

                        // Si el nuevo rectángulo es más cuadrado, reemplaza el rectángulo existente
                        if (Math.abs(aspectRatioInterno - 1) < Math.abs(aspectRatioExistente - 1)) {
                            rectangulosDetectadosCasiCuadrados.set(i, rectInterno);
                        }
                        duplicado = true;
                        break;
                    }
                }

                if (!duplicado) {
                    rectangulosDetectadosCasiCuadrados.add(rectInterno);
                }
            }
        }

        // Ordenar los rectángulos casi cuadrados de izquierda a derecha
        rectangulosDetectadosCasiCuadrados.sort(Comparator.comparingInt(r -> r.x));

        for (Rect rectInterno : rectangulosDetectadosCasiCuadrados) {
            // Dibujar en la imagen procesada (como ya estás haciendo)
            Imgproc.rectangle(src, rectInterno, new Scalar(0, 0, 255), 2);
            Imgproc.putText(src, "Rect " + contador + "." + contadorRectangulos, new Point(rectInterno.x, rectInterno.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);

            // Dibujar en la imagen original
            // Asegúrate de que las coordenadas del rectángulo sean relativas a la imagen original
            Rect rectOriginal = new Rect(rectInterno.x + RecuadroDniRecorteimagenOriginal.x, rectInterno.y + RecuadroDniRecorteimagenOriginal.y, rectInterno.width, rectInterno.height);
            Imgproc.rectangle(OriginalImagen, rectOriginal, new Scalar(0, 0, 255), 2);
            Imgproc.putText(OriginalImagen, "Rect " + contador + "." + contadorRectangulos, new Point(rectOriginal.x, rectOriginal.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);

            detectarCirculosDniletra(bordes, src, rectInterno, contadorRectangulos, OriginalImagen);
            contadorRectangulos++;
        }
        System.out.println("Cantidad de rectángulos DNI " + rectangulosDetectadosCasiCuadrados.size());
    }


    private static void detectarRectangulos(Mat bordes, Mat src, Rect rect, int contador,Mat originalImagen, Rect coordenadasOriginales) {
        int contadorRectangulos = 0;

        Mat imagenColumna = new Mat(bordes, rect);
        List<MatOfPoint> contornosInternos = new ArrayList<>();
        Imgproc.findContours(imagenColumna, contornosInternos, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        List <Rect> rectangulosNumeroExamen = new ArrayList<>();
        List<Rect> rectangulosDetectados = new ArrayList<>();

        for (MatOfPoint contornoInterno : contornosInternos) {
            Rect rectInterno = Imgproc.boundingRect(contornoInterno);
            if (esRectangulo(rectInterno, imagenColumna) || esRectanguloTorcido(rectInterno, imagenColumna)) {
                // Ajustar las coordenadas del rectángulo
                rectInterno.x += rect.x;
                rectInterno.y += rect.y;

                // Verificar si el rectángulo está duplicado
                boolean duplicado = false;
                for (Rect rectExistente : rectangulosDetectados) {
                    if (Math.abs(rectExistente.x - rectInterno.x) < 5 && Math.abs(rectExistente.y - rectInterno.y) < 5 &&
                            Math.abs(rectExistente.width - rectInterno.width) < 5 && Math.abs(rectExistente.height - rectInterno.height) < 5) {
                        duplicado = true;
                        break;
                    }
                }

                if (!duplicado) {
                    rectangulosDetectados.add(rectInterno);
                }
            }
            //columna de codigos de examen
            if(contador == 4 && esCasiCuadrado(rectInterno, imagenColumna)){
                rectangulosNumeroExamen.add(rectInterno);

            }
        }
        //la numero 4 en la lista de columnas
        if (contador == 4){
             Rect rectInternoNExamen = rectangulosNumeroExamen.getFirst();
            Imgproc.rectangle(src, rectInternoNExamen, new Scalar(0, 0, 255), 2);
            Imgproc.putText(src, "Rect " + contador + "." + contadorRectangulos, new Point(rectInternoNExamen.x, rectInternoNExamen.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
            detectarCirculosCodExamen(bordes, src, rectInternoNExamen, originalImagen, coordenadasOriginales);
            Rect adjustedRect = new Rect(rectInternoNExamen.x + coordenadasOriginales.x, rectInternoNExamen.y + coordenadasOriginales.y, rectInternoNExamen.width, rectInternoNExamen.height);
            Imgproc.rectangle(originalImagen, adjustedRect, new Scalar(0, 0, 255), 2);
            Imgproc.putText(originalImagen, "Rect " + contador + "." + contadorRectangulos, new Point(rectInternoNExamen.x + coordenadasOriginales.x, rectInternoNExamen.y - 10 + coordenadasOriginales.y), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
        }else{
        // Ordenar los rectángulos de arriba a abajo
            rectangulosDetectados.sort(Comparator.comparingInt(r -> r.y));
            for (Rect rectInterno : rectangulosDetectados) {
                contadorRectangulos++;
                Imgproc.rectangle(src, rectInterno, new Scalar(0, 0, 255), 2);
                Imgproc.putText(src, "Rect " + contador + "." + contadorRectangulos, new Point(rectInterno.x, rectInterno.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
                detectarCirculosRespuestas(bordes, src, rectInterno, originalImagen, coordenadasOriginales);
                Rect adjustedRect = new Rect(rectInterno.x + coordenadasOriginales.x, rectInterno.y + coordenadasOriginales.y, rectInterno.width, rectInterno.height);
                Imgproc.rectangle(originalImagen, adjustedRect, new Scalar(0, 0, 255), 2);
                Imgproc.putText(originalImagen, "Rect " + contador + "." + contadorRectangulos, new Point(rectInterno.x + coordenadasOriginales.x, rectInterno.y - 10 + coordenadasOriginales.y), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);

            }
            }

        System.out.println("Cantidad de rectángulos columna " + contador + ": " + rectangulosDetectados.size());
    }

    /***
     * Detecta los círculos en la imagen especificada.
     * @param bordes
     * @param src
     * @param rectInterno
     */
    private static void detectarCirculosRespuestas(Mat bordes, Mat src, Rect rectInterno,Mat originalImagen, Rect coordenadasOriginales) {
        Rect rect = new Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height);

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0);
        rect.y = Math.max(rect.y, 0);
        rect.width = Math.min(rect.width, bordes.cols() - rect.x);
        rect.height = Math.min(rect.height, bordes.rows() - rect.y);

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return;
        }

        Mat imagenRectangulo = new Mat(bordes, rect);

        // Escalar la imagen si es necesario
        double scale = 1.0; // Ajustar este valor según sea necesario
        Size size = new Size(imagenRectangulo.cols() * scale, imagenRectangulo.rows() * scale);
        Imgproc.resize(imagenRectangulo, imagenRectangulo, size);

        Mat circulos = new Mat();
        int minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8;
        if (minDist < 1) {
            minDist = 1;
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1, minDist, 100, 30, 21, 37);

        List<Point> centrosCirculos = new ArrayList<>();
        List<Integer> radiosCirculos = new ArrayList<>();
        for (int i = 0; i < circulos.cols(); i++) {
            double[] datosCirculo = circulos.get(0, i);
            if (datosCirculo == null) {
                continue;
            }
            Point centro = new Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y);
            int radio = (int) Math.round(datosCirculo[2]);

            boolean solapado = false;
            for (int j = 0; j < centrosCirculos.size(); j++) {
                if (distancia(centro, centrosCirculos.get(j)) < radio + radiosCirculos.get(j)) {
                    solapado = true;
                    break;
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro);
                radiosCirculos.add(radio);
            }
        }

        // Ordenar los círculos de izquierda a derecha
        centrosCirculos.sort(Comparator.comparingDouble(p -> p.x));
        int respuestasEncontradas = 0;
        char posicionAlfabetica = 'N'; // Inicializamos con 'N' por defecto
        for (int i = 0; i < centrosCirculos.size(); i++) {
            Point centro = centrosCirculos.get(i);
            int radio = radiosCirculos.get(i);
            // Dibujar en la imagen original y en la imagen procesada
            Point centroOriginal = new Point(centro.x + coordenadasOriginales.x, centro.y + coordenadasOriginales.y);
            // Si el círculo no está relleno, pintarlo con el borde de color amarillo
            Imgproc.circle(originalImagen, centroOriginal, radio, new Scalar(0, 255, 255), 2);
            Imgproc.circle(src, centro, radio, new Scalar(0, 255, 255), 2);
            if (verificarRelleno(src, centro, radio)) {
                // Si el círculo está relleno, pintarlo de verde
                Imgproc.circle(originalImagen, centroOriginal, radio, new Scalar(0, 255, 0), -1); // -1 indica relleno completo
                Imgproc.circle(src, centro, radio, new Scalar(0, 255, 0), -1); // -1 indica relleno completo
                posicionAlfabetica = asignarPosicionAlfabetica(i);
                respuestasEncontradas++;
            }


        }

// Si no se encontró exactamente una respuesta, marcamos como 'N'
        if (respuestasEncontradas != 1) {
            posicionAlfabetica = 'N';
        }

        System.out.println("Respuesta " + posicionAlfabetica);
        Sesion.getInstance().addRespuesta(String.valueOf(posicionAlfabetica));
    }

    /**
     * Detecta los círculos en la imagen especificada.
     * @param bordes
     * @param src
     * @param rectInterno
     * @param caso
     */
    private static void detectarCirculosDniletra(Mat bordes, Mat src, Rect rectInterno, int caso, Mat OriginalImagen) {
        Rect imagenOriginal = Sesion.getInstance().getDnicoordenadas();
        int radio = 0;
        int contadorCirculos = 0;
        int contadorCirculosDNI;
        List<String> respuestas = new ArrayList<>();
        List<String> respuestasDni = new ArrayList<>();
        Rect rect = new Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height);

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0);
        rect.y = Math.max(rect.y, 0);
        rect.width = Math.min(rect.width, bordes.cols() - rect.x);
        rect.height = Math.min(rect.height, bordes.rows() - rect.y);

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return;
        }

        Mat imagenRectangulo = new Mat(bordes, rect);

        Mat circulos = new Mat();
        int minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8;
        if (minDist < 1) {
            minDist = 1;
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1, 2, 50, 20, 20, 35);

        List<Point> centrosCirculos = new ArrayList<>();
        List<Integer> radiosCirculos = new ArrayList<>();
        for (int i = 0; i < circulos.cols(); i++) {
            double[] datosCirculo = circulos.get(0, i);
            if (datosCirculo == null) {
                continue;
            }

            // Crear el centro del círculo y el radio con los datos recuperados
            Point centro = new Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y);
            radio = (int) Math.round(datosCirculo[2]);

            boolean solapado = false;
            for (int j = 0; j < centrosCirculos.size(); j++) {
                if (distancia(centro, centrosCirculos.get(j)) < radio + radiosCirculos.get(j)) {
                    solapado = true;
                    break;
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro);
                radiosCirculos.add(radio);
            }
        }

        if (caso == 0 || caso == 2) {
            // Ordenar los círculos de izquierda a derecha y pasar a la línea de abajo si se llega al final
            List<List<Point>> filasCirculos = new ArrayList<>();
            for (Point centro : centrosCirculos) {
                boolean agregado = false;
                for (List<Point> fila : filasCirculos) {
                    if (Math.abs(fila.get(0).y - centro.y) < radio) {
                        fila.add(centro);
                        agregado = true;
                        break;
                    }
                }
                if (!agregado) {
                    List<Point> nuevaFila = new ArrayList<>();
                    nuevaFila.add(centro);
                    filasCirculos.add(nuevaFila);
                }
            }
            filasCirculos.sort(Comparator.comparingDouble(f -> f.get(0).y));
            for (List<Point> fila : filasCirculos) {
                fila.sort(Comparator.comparingDouble(p -> p.x));
                for (Point centro : fila) {
                    int radioCirculosOrdenados = radiosCirculos.get(centrosCirculos.indexOf(centro));

                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centro, radioCirculosOrdenados, new Scalar(0, 255, 255), 2);

                    // Ajustar las coordenadas del centro para la imagen original
                    Point centroOriginal = new Point(centro.x + imagenOriginal.x, centro.y + imagenOriginal.y);

                    // Dibujar círculos en la imagen original
                    Imgproc.circle(OriginalImagen, centroOriginal, radio, new Scalar(0, 255, 255), 2);

                    // Verificar si el círculo está relleno
                    if (verificarRelleno(src, centro, radioCirculosOrdenados)) {
                        Imgproc.circle(src, centro, radioCirculosOrdenados, new Scalar(0, 255, 0), 2);
                        // Dibujar círculos en la imagen original
                        Imgproc.circle(OriginalImagen, centroOriginal, radio, new Scalar(0, 255, 0), -1);
                        // Asignar una posición alfabética al círculo
                        char posicionAlfabetica = asignarPosicionAlfabetica(contadorCirculos);
                        System.out.println("Letra " + posicionAlfabetica);

                        // Agregar la posición alfabética a la sesión
                        Sesion.getInstance().addDniNie(String.valueOf(posicionAlfabetica));
                    }
                    contadorCirculos++;
                }
            }
        } else {

            // Ordenar los círculos de arriba a abajo
            List<List<Point>> columnasCirculos = new ArrayList<>();
            for (Point centro : centrosCirculos) {
                boolean agregado = false;
                for (List<Point> columna : columnasCirculos) {
                    if (Math.abs(columna.get(0).x - centro.x) < radio) {
                        columna.add(centro);
                        agregado = true;
                        break;
                    }
                }
                if (!agregado) {
                    List<Point> nuevaColumna = new ArrayList<>();
                    nuevaColumna.add(centro);
                    columnasCirculos.add(nuevaColumna);
                }
            }
            columnasCirculos.sort(Comparator.comparingDouble(c -> c.get(0).x));
            for (List<Point> columna : columnasCirculos) {
                contadorCirculosDNI = 0;
                columna.sort(Comparator.comparingDouble(p -> p.y));
                for (Point centro : columna) {
                    int radioCirculosOrdenados = radiosCirculos.get(centrosCirculos.indexOf(centro));

                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centro, radioCirculosOrdenados, new Scalar(0, 255, 255), 2);

                    // Ajustar las coordenadas del centro para la imagen original
                    Point centroOriginal = new Point(centro.x + imagenOriginal.x, centro.y + imagenOriginal.y);

                    // Dibujar círculos en la imagen original
                    Imgproc.circle(OriginalImagen, centroOriginal, radio, new Scalar(0, 255, 255), 2);

                    // Verificar si el círculo está relleno
                    if (verificarRelleno(src, centro, radioCirculosOrdenados)) {
                        Imgproc.circle(src, centro, radioCirculosOrdenados, new Scalar(0, 255, 0), 2);
                        // Dibujar círculos en la imagen original
                        Imgproc.circle(OriginalImagen, centroOriginal, radio, new Scalar(0, 255, 0), -1);
                        // Asignar un número DNI al círculo
                        System.out.println("Numero Dni " + contadorCirculosDNI);
                        Sesion.getInstance().addDniNie(String.valueOf(contadorCirculosDNI));
                        contadorCirculosDNI = 0;
                    }
                    contadorCirculosDNI++;
                }
            }
        }
    }
    private static void detectarCirculosCodExamen(Mat bordes, Mat src, Rect rectInterno, Mat originalImagen, Rect coordenadasOriginales) {
        int radio = 0;
        int contadorCirculosDNI;
        List<String> respuestas = new ArrayList<>();
        Rect rect = new Rect(rectInterno.x, rectInterno.y, rectInterno.width, rectInterno.height);

        // Asegurarse de que los índices están dentro de los límites
        rect.x = Math.max(rect.x, 0);
        rect.y = Math.max(rect.y, 0);
        rect.width = Math.min(rect.width, bordes.cols() - rect.x);
        rect.height = Math.min(rect.height, bordes.rows() - rect.y);

        // Asegurarse de que el tamaño del rectángulo es positivo
        if (rect.width <= 0 || rect.height <= 0) {
            return;
        }

        Mat imagenRectangulo = new Mat(bordes, rect);

        // Escalar la imagen si es necesario añadir casos de resoluciones
        double scale = 1.0;
        Size size = new Size(imagenRectangulo.cols() * scale, imagenRectangulo.rows() * scale);
        Imgproc.resize(imagenRectangulo, imagenRectangulo, size);

        Mat circulos = new Mat();
        int minDist = Math.min(imagenRectangulo.rows(), imagenRectangulo.cols()) / 8;
        if (minDist < 1) {
            minDist = 1;
        }

        // Ajustar los parámetros según sea necesario 100, 30, 13, 21 valores clave
        Imgproc.HoughCircles(imagenRectangulo, circulos, Imgproc.CV_HOUGH_GRADIENT, 1, 2, 50, 20, 20, 35);

        List<Point> centrosCirculos = new ArrayList<>();
        List<Integer> radiosCirculos = new ArrayList<>();
        for (int i = 0; i < circulos.cols(); i++) {
            double[] datosCirculo = circulos.get(0, i);
            if (datosCirculo == null) {
                continue;
            }
            // Crear el centro del círculo y el radio con los datos recuperados
            Point centro = new Point(datosCirculo[0] + rectInterno.x, datosCirculo[1] + rectInterno.y);
            radio = (int) Math.round(datosCirculo[2]);

            boolean solapado = false;
            for (int j = 0; j < centrosCirculos.size(); j++) {
                if (distancia(centro, centrosCirculos.get(j)) < radio + radiosCirculos.get(j)) {
                    solapado = true;
                    break;
                }
            }

            if (!solapado) {
                centrosCirculos.add(centro);
                radiosCirculos.add(radio);
            }
        }
        // Ordenar los círculos de arriba a abajo
            List<List<Point>> columnasCirculos = new ArrayList<>();
            for (Point centro : centrosCirculos) {
                boolean agregado = false;
                for (List<Point> columna : columnasCirculos) {
                    if (Math.abs(columna.get(0).x - centro.x) < radio) {
                        columna.add(centro);
                        agregado = true;
                        break;
                    }
                }
                if (!agregado) {
                    List<Point> nuevaColumna = new ArrayList<>();
                    nuevaColumna.add(centro);
                    columnasCirculos.add(nuevaColumna);
                }
            }
            columnasCirculos.sort(Comparator.comparingDouble(c -> c.get(0).x));
            for (List<Point> columna : columnasCirculos) {
                contadorCirculosDNI = 0;
                columna.sort(Comparator.comparingDouble(p -> p.y));
                for (Point centroCirculosColumnas : columna) {
                    int radioCirculosOrdenados = radiosCirculos.get(centrosCirculos.indexOf(centroCirculosColumnas));

                    // Dibujar círculos en la imagen procesada
                    Imgproc.circle(src, centroCirculosColumnas, radioCirculosOrdenados, new Scalar(0, 255, 255), 2);

                    // Ajustar las coordenadas del centro para la imagen original
                    Point centroOriginal = new Point(centroCirculosColumnas.x + coordenadasOriginales.x, centroCirculosColumnas.y + coordenadasOriginales.y);

                    // Dibujar círculos en la imagen original
                    Imgproc.circle(originalImagen, centroOriginal, radio, new Scalar(0, 255, 255), 2);

                    // Verificar si el círculo está relleno
                    if (verificarRelleno(src, centroCirculosColumnas, radioCirculosOrdenados)) {
                        // Dibujar círculos en la imagen procesada
                        Imgproc.circle(src, centroCirculosColumnas, radioCirculosOrdenados, new Scalar(0, 255, 0 ), 2);
                        // Dibujar círculos en la imagen original
                        Imgproc.circle(originalImagen, centroOriginal, radio, new Scalar(0, 255, 0), -1);
                        // Asignar un número de examen al círculo
                        System.out.println("Numero Examen " + contadorCirculosDNI);
                        Sesion.getInstance().addCodigoExamen(contadorCirculosDNI);
                        contadorCirculosDNI = 0;
                    }
                    contadorCirculosDNI++;
                }
            }
        }
    }


