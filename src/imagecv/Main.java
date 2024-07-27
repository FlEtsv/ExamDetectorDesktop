package imagecv;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ImageProcessorApp app = new ImageProcessorApp();
                app.setVisible(true);

            }
        });
    }
}
