package csv;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerarArchivosCSV {/*** Clase para generar archivos CSV con respuestas aleatorias */
// Opciones de respuesta
    private static final String[] OPCIONES = {"A", "B", "C", "D"};
    // Número de respuestas por archivo
    private static final int NUM_RESPUESTAS = 40;
    // Número de archivos a generar
    private static final int NUM_ARCHIVOS = 3;
/***
    public static void main(String[] args) {
        Random rand = new Random();
            // Generar los archivos
        for (int i = 0; i < NUM_ARCHIVOS; i++) {
            // Nombre del archivo
            String fileName = String.format("%03d.csv", i);
            try (FileWriter writer = new FileWriter(fileName)) {
                for (int j = 0; j < NUM_RESPUESTAS; j++) {
                    String respuesta = OPCIONES[rand.nextInt(OPCIONES.length)];
                    writer.write(respuesta + "\n");
                }
                System.out.println("Archivo generado: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 */
}
