package imagecv;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class Sesion {
    List<String> repuestas_Examen = new ArrayList<String>();
    List<Integer> codigoExamen = new ArrayList<Integer>();
    List<String> dniNie = new ArrayList<String>();


    Rect Dnicoordenadas = new Rect();

    List<Rect> coordenadasColumnas = new ArrayList<Rect>();
    //Singleton
    private static Sesion instance = null;

    //Singleton
    public static Sesion getInstance() {
        if (instance == null) {
            instance = new Sesion();
        }
        return instance;
    }


    public List<Integer> getCodigoExamen() {
        return codigoExamen;
    }

    public void setCodigoExamen(List<Integer> codigoExamen) {
        this.codigoExamen = codigoExamen;
    }

    public List<String> getDniNie() {
        return dniNie;
    }

    public void setDniNie(List<String> dniNie) {
        this.dniNie = dniNie;
    }

    public List<String> getRepuestas_Examen() {
        return repuestas_Examen;
    }

    public void setRepuestas_Examen(List<String> repuestas_Examen) {
        this.repuestas_Examen = repuestas_Examen;
    }
    //metodo para recibir una respuesta y añadirla a la lista de respuestas_Examen
    public void addRespuesta(String respuesta) {
        repuestas_Examen.add(respuesta);
    }
    //metodo para recibir un codigo de examen y añadirlo a la lista de codigoExamen
    public void addCodigoExamen(int codigo) {
        codigoExamen.add(codigo);
    }
    //metodo para recibir un dni/nie y añadirlo a la lista de dniNie
    public void addDniNie(String dni) {
        dniNie.add(dni);
    }

    public Rect getDnicoordenadas() {
        return Dnicoordenadas;
    }

    public void setDnicoordenadas(Rect dnicoordenadas) {
        Dnicoordenadas = dnicoordenadas;
    }

    public List<Rect> getCoordenadasColumnas() {
        return coordenadasColumnas;
    }

    public void setCoordenadasColumnas(List<Rect> coordenadasColumnas) {
        this.coordenadasColumnas = coordenadasColumnas;
    }

    //metodo para limpiar las listas de respuestas, codigo de examen y dni/nie
    public void limpiarSesion() {
        repuestas_Examen.clear();
        codigoExamen.clear();
        dniNie.clear();
        coordenadasColumnas.clear();
        Dnicoordenadas.x=0;
        Dnicoordenadas.y=0;

    }

}
