package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameOver_Panel extends JPanel {
    Font customFont = FontLoader.loadFont("font/ArcadeClassic.ttf", 20);
    private JButton menuBtn = new JButton("Hauptmenu");
    private JButton restartBtn = new JButton("Neustart");
    private JLabel gameOverLabel = new JLabel("Game Over", SwingConstants.CENTER);

    GameOver_Panel() {
        setLayout(null);
        setOpaque(false); // Wichtig für Transparenz

        // Hauptmenü-Button
        setupButton(menuBtn, 150, 50);
        menuBtn.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.MENU));

        // Neustart-Button
        setupButton(restartBtn, 150, 50);
        restartBtn.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.START));

        // Game Over-Label
        gameOverLabel.setFont(customFont.deriveFont(48f)); // Größere Schriftgröße
        gameOverLabel.setForeground(new Color(255, 215, 0)); // Helle Farbe
        gameOverLabel.setOpaque(false);
        add(gameOverLabel);

        // Component Listener für Größenänderung
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerComponents();
            }
        });
    }

    private void setupButton(JButton button, int width, int height) {
        button.setPreferredSize(new Dimension(width, height));
        button.setSize(width, height);
        button.setFont(customFont);
        button.setBackground(new Color(0, 0, 0, 128)); // Halbtransparenter Hintergrund
        button.setForeground(Color.WHITE); // Weiße Schrift
        button.setFocusPainted(false); // Kein Fokus-Rahmen
        button.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2)); // Farbiger Rand
        add(button);
    }

    private void centerComponents() {
        // Zentriere das Game Over-Label
        gameOverLabel.setSize(getWidth(), 60); // Setze die Größe des Labels
        int labelY = (getHeight() - gameOverLabel.getHeight() - 140) / 2; // Berücksichtige die Höhe der Buttons
        gameOverLabel.setLocation(0, labelY);

        // Zentriere die Buttons unter dem Label
        int buttonY = labelY + gameOverLabel.getHeight() + 20;
        centerButton(menuBtn, buttonY);
        centerButton(restartBtn, buttonY + 70); // Positioniere den Neustart-Button unter dem Hauptmenü-Button
    }

    private void centerButton(JButton button, int y) {
        int x = (getWidth() - button.getWidth()) / 2;
        button.setLocation(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setPaint(new Color(0, 0, 0, 128)); // Dunkler, halbtransparenter Hintergrund
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        revalidate();
        repaint();
    }
}
