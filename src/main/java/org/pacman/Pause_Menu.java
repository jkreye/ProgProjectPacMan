package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Pause_Menu extends JPanel {
    private JButton menubtn = new JButton();
    private JLabel pausedLabel = new JLabel("Paused", JLabel.CENTER);
    Pause_Menu(){
        // Load the custom font
        Font customFont = FontLoader.loadFont("font/ArcadeClassic.ttf", 20);


        setLayout(null);
        // setBackground(new Color(255, 183, 0, 170));
        setOpaque(false); // Wichtig für die Transparenz

        pausedLabel.setFont(customFont.deriveFont(40f)); // Größere Schriftgröße für den Titel
        pausedLabel.setForeground(Color.WHITE); // Weiße Farbe für den Text
        pausedLabel.setBounds(0, 0, 200, 60); // Vorläufige Position und Größe


        menubtn.setText("Hauptmenu");
        menubtn.setBounds(0, 0, 150, 50);  // Vorläufige Position und Größe
        menubtn.setVisible(true);
        Dimension buttonSize = new Dimension(150, 50);
        menubtn.setPreferredSize(buttonSize);
        menubtn.setSize(buttonSize);

        menubtn.setFont(customFont); // Set the custom font

        menubtn.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.MENU));

        add(pausedLabel);
        add(menubtn);

        // Initialer Aufruf, um den Button zu zentrieren
        centerComponents();

        // ComponentListener hinzufügen, um auf Größenänderungen zu reagieren
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Zentrieren Sie den Button jedes Mal, wenn das Panel geändert wird
                centerComponents();
            }
        });

    }

    private void centerComponents() {
        // Zentrieren des Buttons
        Dimension buttonSize = menubtn.getPreferredSize();
        int buttonX = (getWidth() - buttonSize.width) / 2;
        int buttonY = (getHeight() - buttonSize.height) / 2 + 30; // Etwas unterhalb der Mitte
        menubtn.setBounds(buttonX, buttonY, buttonSize.width, buttonSize.height);

        // Zentrieren des "Paused"-Labels
        Dimension labelSize = pausedLabel.getPreferredSize();
        int labelX = (getWidth() - labelSize.width) / 2;
        int labelY = buttonY - labelSize.height - 10; // 10 Pixel über dem Button
        pausedLabel.setBounds(labelX, labelY, labelSize.width, labelSize.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setPaint(new Color(0, 72, 255, 84)); // Halbtransparente Farbe
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
