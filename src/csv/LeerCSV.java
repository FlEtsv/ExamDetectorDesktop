package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LeerCSV {
    public static List<String> leerRespuestas(String codigoArchivo) {
        String fileName = String.format("%03d.csv", Integer.parseInt(codigoArchivo));
        List<String> respuestas = new ArrayList<>();

        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                respuestas.add(data);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return respuestas;
    }


}