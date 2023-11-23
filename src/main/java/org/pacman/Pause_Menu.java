package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Pause_Menu extends JPanel {

    // Load the custom font
    Font customFont = FontLoader.loadFont("ArcadeClassic.ttf", 20);
    private JButton menubtn = new JButton();
    Pause_Menu(){
        // Load the custom font
        Font customFont = FontLoader.loadFont("ArcadeClassic.ttf", 20);

        setLayout(null);
        // setBackground(new Color(255, 183, 0, 170));
        setOpaque(false); // Wichtig für die Transparenz
        menubtn.setText("Hauptmenu");
        menubtn.setBounds(0, 0, 150, 50);  // Vorläufige Position und Größe
        menubtn.setVisible(true);
        Dimension buttonSize = new Dimension(150, 50);
        menubtn.setPreferredSize(buttonSize);
        menubtn.setSize(buttonSize);

        menubtn.setFont(customFont); // Set the custom font

        menubtn.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.MENU));
        add(menubtn);

        // Initialer Aufruf, um den Button zu zentrieren
        centerButton();

        // ComponentListener hinzufügen, um auf Größenänderungen zu reagieren
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Zentrieren Sie den Button jedes Mal, wenn das Panel geändert wird
                centerButton();
            }
        });

    }

    private void centerButton() {
        Dimension size = menubtn.getPreferredSize();
        int x = (getWidth() - size.width) / 2;
        int y = (getHeight() - size.height) / 2;
        menubtn.setBounds(x, y, size.width, size.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setPaint(new Color(255, 183, 0, 128)); // Halbtransparente Farbe
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    // Stellen Sie sicher, dass Sie revalidate und repaint aufrufen, wenn die Fenstergröße geändert wird.
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        revalidate();
        repaint();
    }

}
