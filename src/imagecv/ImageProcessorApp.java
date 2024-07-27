package imagecv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static csv.LeerCSV.leerRespuestas;
import static imagecv.ProcesadorImagen.detectarColumnas;

public class ImageProcessorApp extends JFrame {
    private JLabel imageView;
    private JLabel smallImageView;
    private JPanel sidePanel;
    private JScrollPane sideScrollPane;
    private final JFileChooser fileChooser;
    private Mat src;
    private String[] respuestas;
    private String dniNie;
    private String numeroExamen;
    private List<String> rutasImagenesGeneradas;
    private JLabel respuestasLabel;
    private JLabel dniLabel;
    private JLabel numeroExamenLabel;
    private JLabel resultadoLabel;
    private String rutaImagen;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ImageProcessorApp() {
        setTitle("Image Processor");
        setSize(1450, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Estilo moderno
        setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 14));
        getContentPane().setBackground(new Color(245, 245, 245));

        // Vista principal
        imageView = new JLabel();
        smallImageView = new JLabel();
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.add(smallImageView);
        mainPanel.add(imageView);
        imageView.setVisible(false); // Ocultar la imagen procesada inicialmente
        add(mainPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4, 1, 10, 10));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(new Color(245, 245, 245));
        respuestasLabel = new JLabel("Respuestas: ");
        dniLabel = new JLabel("DNI/NIE: ");
        numeroExamenLabel = new JLabel("Número de Examen: ");
        resultadoLabel = new JLabel("Resultado: ");
        infoPanel.add(respuestasLabel);
        infoPanel.add(dniLabel);
        infoPanel.add(numeroExamenLabel);
        infoPanel.add(resultadoLabel);
        add(infoPanel, BorderLayout.NORTH);

        // Panel lateral para mostrar imágenes generadas (oculto inicialmente)
        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sideScrollPane = new JScrollPane(sidePanel);
        sideScrollPane.setVisible(false);
        add(sideScrollPane, BorderLayout.WEST);

        // Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        JButton selectImageButton = createStyledButton("Seleccionar Imagen");
        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectImage();
            }
        });

        JButton processImageButton = createStyledButton("Procesar Imagen");
        processImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processImage();
            }
        });

        JButton calculateScoreButton = createStyledButton("Calcular Nota");
        calculateScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateNota();
            }
        });

        buttonPanel.add(selectImageButton);
        buttonPanel.add(processImageButton);
        buttonPanel.add(calculateScoreButton);
        add(buttonPanel, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();

        // MenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem verImagenesRecortadas = new JMenuItem("Ver Imágenes Recortadas");
        JMenuItem volver = new JMenuItem("Volver");

        verImagenesRecortadas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sideScrollPane.setVisible(true);
                displayGeneratedImages();
                revalidate();
                repaint();
            }
        });

        volver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sideScrollPane.setVisible(false);
                revalidate();
                repaint();
            }
        });

        menuArchivo.add(verImagenesRecortadas);
        menuArchivo.add(volver);
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(100, 150, 255));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void selectImage() {
        Sesion.getInstance().limpiarSesion();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            src = Imgcodecs.imread(selectedFile.getAbsolutePath());
            if (!src.empty()) {
                displayImage(src, smallImageView, 0.2);  // Mostrar imagen pequeña
                rutaImagen = selectedFile.getAbsolutePath();
            }
        }
    }

    private void processImage() {
        if (src != null && !src.empty()) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            // Crear un Mat con la imagen rutaImagen
            Mat src = Imgcodecs.imread(rutaImagen);

            Mat dni = ExtraerDNI.init(rutaImagen);
            List<Mat> columnas = ExtraerColumnas.init(rutaImagen);

            // Procesar cada columna y el DNI
            for (int i = 0; i < columnas.size(); i++) {
                List<Rect> coordenadasColumnas = Sesion.getInstance().getCoordenadasColumnas();
                detectarColumnas(columnas.get(i), 0, i, src, coordenadasColumnas.get(i));
            }
            detectarColumnas(dni, 1, columnas.size(), src, Sesion.getInstance().getDnicoordenadas());

            List<String> lista = Sesion.getInstance().getRepuestas_Examen();
            String[] resultado = new String[]{String.join(",", lista)};
            List<Integer> codigo = Sesion.getInstance().getCodigoExamen();
            List<String> numerodni = Sesion.getInstance().getDniNie();
            String dniNie = String.join(", ", numerodni);
            String codigoExamen = String.join(", ", codigo.toString());

            // Configurar las respuestas, DNI/NIE y número de examen
            setRespuestas(resultado);
            setDniNie(dniNie);
            setNumeroExamen(codigoExamen);

            // Guardar src como una imagen en la ruta src/img/examenAnalizadoCompletoPrueba1.png
            Imgcodecs.imwrite("src/img/examenAnalizadoCompletoPrueba1.png", src);
            displayFullImage();  // Mostrar imagen completa procesada
        }
    }

    /***
     * Muestra las imágenes generadas en el panel lateral.
     */
    private void displayGeneratedImages() {
        sidePanel.removeAll();
        List<String> rutasImagenesGeneradas = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            rutasImagenesGeneradas.add("src/img/result" + i + ".png");
        }
        for (String filePath : rutasImagenesGeneradas) {
            File file = new File(filePath);
            if (file.exists()) {
                System.out.println("El archivo " + filePath + " existe.");
            } else {
                System.out.println("El archivo " + filePath + " no existe.");
            }
        }

        for (String imagePath : rutasImagenesGeneradas) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image image = icon.getImage(); // Transformar
            Image newimg = image.getScaledInstance(image.getWidth(null) / 5, image.getHeight(null) / 5, Image.SCALE_SMOOTH); // Escalar suavemente
            icon = new ImageIcon(newimg);  // Transformar de nuevo
            JLabel label = new JLabel(icon);
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    displayImage(imagePath);
                }
            });
            sidePanel.add(label);
        }
        revalidate();
        repaint();
    }

    private void displayFullImage() {
        String fullImagePath = "src/img/examenAnalizadoCompletoPrueba1.png";
        ImageIcon icon = new ImageIcon(fullImagePath);
        Image image = icon.getImage();
        Image newimg = image.getScaledInstance(image.getWidth(null) / 5, image.getHeight(null) / 5, Image.SCALE_SMOOTH); // Escalar a la mitad del tamaño
        icon = new ImageIcon(newimg);
        imageView.setIcon(icon);
        imageView.setHorizontalAlignment(JLabel.CENTER);
        imageView.setVerticalAlignment(JLabel.CENTER);
        imageView.setVisible(true); // Mostrar la imagen procesada
        imageView.revalidate();
    }

    private void displayImage(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage(); // Transformar
        Image newimg = image.getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2, Image.SCALE_SMOOTH); // Escalar suavemente
        icon = new ImageIcon(newimg);
        imageView.setIcon(icon);
        imageView.setHorizontalAlignment(JLabel.CENTER);
        imageView.setVerticalAlignment(JLabel.CENTER);
        imageView.revalidate();
    }

    private void displayImage(Mat img, JLabel container, double scale) {
        ImageIcon icon = new ImageIcon(Mat2BufferedImage(img));
        Image image = icon.getImage(); // Inicio de transformación
        Image newimg = image.getScaledInstance((int)(image.getWidth(null) * scale), (int)(image.getHeight(null) * scale), Image.SCALE_SMOOTH); // Escalar suavemente
        icon = new ImageIcon(newimg);  // Creación de icono con la imagen escalada
        container.setIcon(icon);
        container.setHorizontalAlignment(JLabel.CENTER);
        container.setVerticalAlignment(JLabel.CENTER);
    }

    private Image Mat2BufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private void calculateNota() {
        double notaExamen = 0;
        List<Integer> codExamenes = Sesion.getInstance().getCodigoExamen();
        String codigoArchivo = codExamenes.stream().map(String::valueOf).collect(Collectors.joining(""));
        List<String> respuestasCorrectas = leerRespuestas(codigoArchivo);
        List<String> respuestasAlumno = Sesion.getInstance().getRepuestas_Examen();

        for (int i = 0; i < respuestasAlumno.size(); i++) {
            String respuestaAlumno = respuestasAlumno.get(i);
            String respuestaCorrecta = respuestasCorrectas.get(i);

            if (!respuestaAlumno.equals("N")) {
                if (respuestaAlumno.equals(respuestaCorrecta)) {
                    // Respuesta correcta, suma 0.25 puntos
                    notaExamen += 0.25;
                    System.out.println("pregunta " + i + ": correcta");
                } else {
                    // Respuesta incorrecta, resta 0.08 puntos
                    notaExamen -= 0.08;
                    System.out.println("pregunta " + i + ": incorrecta");
                }
            } else {
                System.out.println("pregunta " + i + ": no contestada");
            }
        }

        resultadoLabel.setText("Resultado: " + notaExamen);
    }

    public void setRespuestas(String[] respuestas) {
        this.respuestas = respuestas;
        respuestasLabel.setText("Respuestas: " + String.join(", ", respuestas));
    }

    public void setDniNie(String dniNie) {
        this.dniNie = dniNie;
        dniLabel.setText("DNI/NIE: " + dniNie);
    }

    public void setNumeroExamen(String numeroExamen) {
        this.numeroExamen = numeroExamen;
        numeroExamenLabel.setText("Número de Examen: " + numeroExamen);
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

}
