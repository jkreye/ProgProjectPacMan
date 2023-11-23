package org.pacman;

import javax.swing.*;
import java.awt.*;

public class Game_Panel extends JPanel {
    private JLabel label = new JLabel("Game (ESC: Pause Menü)");
    Game_Panel(){
        setDoubleBuffered(true);
        setBackground(new Color(70, 70, 70));
        add(label);
        label.setBounds(100,100,100,50);
        label.setVisible(true);
        label.setForeground(Color.orange);
        setFocusable(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Berechnen der Größe einer einzelnen Zelle basierend auf der Fenstergröße
        int cellSize = Math.min(getWidth() / maze[0].length, getHeight() / (maze.length+5));

        // Berechnen der Größe des gesamten Labyrinths
        int mazeWidth = maze[0].length * cellSize;
        int mazeHeight = (maze.length+5) * cellSize;

        // Berechnen der Startposition, um das Labyrinth in der Mitte zu zeichnen
        int startX = (getWidth() - mazeWidth) / 2;

        // Definieren eines oberen Randes
        int paddingTop = 100; // Zum Beispiel 50 Pixel
        int startY = paddingTop + (getHeight() - mazeHeight - paddingTop) / 2;

        // Zeichnen des Labyrinths beginnend bei startX und startY
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                if (maze[row][col] == '#') {
                    g.setColor(Color.BLUE);
                    g.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                }
                // Weitere Zeichenlogik für andere Elemente des Labyrinths hier hinzufügen
            }
        }
    }



    private final char[][] maze = {
            // Obere Grenze
            "############################".toCharArray(),
            "#............##............#".toCharArray(),
            "#.####.#####.##.#####.####.#".toCharArray(),
            "#o####.#####.##.#####.####o#".toCharArray(),
            "#.####.#####.##.#####.####.#".toCharArray(),
            "#..........................#".toCharArray(),
            "#.####.##.########.##.####.#".toCharArray(),
            "#.####.##.########.##.####.#".toCharArray(),
            "#......##....##....##......#".toCharArray(),
            "######.##### ## #####.######".toCharArray(),
            "     #.##### ## #####.#     ".toCharArray(),
            "     #.##          ##.#     ".toCharArray(),
            "     #.## ###--### ##.#     ".toCharArray(),
            "######.## #      # ##.######".toCharArray(),
            "      .   #      #   .      ".toCharArray(),
            "######.## #      # ##.######".toCharArray(),
            "     #.## ######## ##.#     ".toCharArray(),
            "     #.##          ##.#     ".toCharArray(),
            "     #.## ######## ##.#     ".toCharArray(),
            "######.## ######## ##.######".toCharArray(),
            "#............##............#".toCharArray(),
            "#.####.#####.##.#####.####.#".toCharArray(),
            "#.####.#####.##.#####.####.#".toCharArray(),
            "#o..##.......  .......##..o#".toCharArray(),
            "###.##.##.########.##.##.###".toCharArray(),
            "###.##.##.########.##.##.###".toCharArray(),
            "#......##....##....##......#".toCharArray(),
            "#.##########.##.##########.#".toCharArray(),
            "#.##########.##.##########.#".toCharArray(),
            "#..........................#".toCharArray(),
            // Untere Grenze
            "############################".toCharArray()
    };


    private void drawMaze(Graphics g) {
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                if (maze[row][col] == '#') {
                    g.setColor(Color.BLUE);
                    g.fillRect(col * 20, row * 20, 20, 20);
                }
            }
        }
    }
}
