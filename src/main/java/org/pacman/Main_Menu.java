package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main_Menu extends JPanel {
    private JButton start = new JButton("Start Game");
    private JButton highScores = new JButton("High Scores");
    private JButton quit = new JButton("Quit");


    Main_Menu() {
        // Load the custom font
        Font customFont = FontLoader.loadFont("font/ArcadeClassic.ttf", 20);
        Font customFontTitle = FontLoader.loadFont("font/ArcadeClassic.ttf", 50);


        // Set up BoxLayout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Customize button appearance
        customizeButton(start, customFont);
        customizeButton(highScores, customFont);
        customizeButton(quit, customFont);

        // Set the background color of the JPanel to black
        setBackground(Color.BLACK);

        // Erstellen des JLabel für den Schriftzug
        JLabel titleLabel = new JLabel("Cookie Collector", JLabel.CENTER);
        titleLabel.setFont(customFontTitle);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Zentrieren des Labels


        // Create a subpanel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false); // Make the subpanel transparent

        // Add buttons to the subpanel
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(start);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(highScores);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(quit);
        buttonPanel.add(Box.createVerticalGlue());

        // Add the subpanel to the main panel
        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 20))); // Optional: Abstand zwischen Titel und Buttons

        add(buttonPanel);

        // Add action listeners
        start.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.START));
        highScores.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.HIGH_SCORES));
        quit.addActionListener(e -> System.exit(0)); // Exit the application

        // Add mouse listeners to handle button hover effect
        addHoverEffect(start);
        addHoverEffect(highScores);
        addHoverEffect(quit);

        setFocusable(false);
    }

    private void addHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Set a border or change the background color when the mouse enters
                button.setBorderPainted(true); // Show the button border
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Remove the border or revert to the original state when the mouse exits
                button.setBorderPainted(false); // Hide the button border
            }
        });
    }
    private void customizeButton(JButton button, Font customFont) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 50)); // Set button size
        button.setForeground(new Color(255, 204, 0)); // Pac-Man yellow

        if (customFont != null) {
            button.setFont(customFont); // Set the custom font
        } else {
            // Handle the case where the font could not be loaded
            System.err.println("Custom font could not be loaded.");
        }

        // button.setFont(new Font("ArcadeClassic", Font.PLAIN, 18)); // Custom font
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
}
