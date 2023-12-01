package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game_Panel extends JPanel implements KeyListener {

    private JLabel label;
    private Game_Controller gameController;
    private Maze maze;

    Game_Panel(Game_Controller controller){
        maze = new Maze();

        this.gameController = controller;

        setDoubleBuffered(true);
        setBackground(new Color(70, 70, 70));

        label = new JLabel("Game (ESC: Pause Menü)");
        label.setBounds(100, 100, 100, 50);
        label.setVisible(true);
        label.setForeground(Color.orange);
        add(label);

        addKeyListener(this);
        setFocusable(true);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        char[][] grid = maze.getGrid();
        // Berechnen der Größe einer einzelnen Zelle basierend auf der Fenstergröße
        int cellSize = Math.min(getWidth() / grid[0].length, getHeight() / (grid.length+5));

        // Berechnen der Größe des gesamten Labyrinths
        int mazeWidth = grid[0].length * cellSize;
        int mazeHeight = (grid.length+5) * cellSize;

        // Berechnen der Startposition, um das Labyrinth in der Mitte zu zeichnen
        int startX = (getWidth() - mazeWidth) / 2;

        // Definieren eines oberen Randes
        int paddingTop = 100; // Zum Beispiel 50 Pixel
        int startY = paddingTop + (getHeight() - mazeHeight - paddingTop) / 2;

        // Zeichnen des Labyrinths beginnend bei startX und startY
        drawMaze(g, startX, startY, cellSize);

        Pacman pacman = gameController.getPacman();
        g.setColor(Color.YELLOW);
        g.fillOval(pacman.getX(), pacman.getY(), 20, 20); // Größe und Form von Pac-Man

    }


    private void drawMaze(Graphics g, int startX, int startY, int cellSize) {
        char[][] grid = maze.getGrid();

        int pointSize = cellSize / 3;
        int halfPointSize = pointSize / 2;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == '#') {
                    g.setColor(Color.BLUE);
                    g.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                }
                if (grid[row][col] == '.') {
                    // Berechnen der zentrierten Position für den Punkt
                    int x = startX + col * cellSize + (cellSize - pointSize) / 2;
                    int y = startY + row * cellSize + (cellSize - pointSize) / 2;

                    g.setColor(Color.yellow);
                    g.fillRect(x, y, cellSize/3, cellSize/3);
                }
                if (grid[row][col] == 'o') {
                    int x = startX + col * cellSize + (cellSize - pointSize) / 2;
                    int y = startY + row * cellSize + (cellSize - pointSize) / 2;

                    g.setColor(Color.green);
                    g.fillRect(x, y, cellSize/3, cellSize/3);                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    // KeyListener-Methoden
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_UP);
                System.out.println("up");
                break;
            case KeyEvent.VK_A:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_LEFT);
                break;
            case KeyEvent.VK_S:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_DOWN);
                break;
            case KeyEvent.VK_D:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_RIGHT);
                break;
            case KeyEvent.VK_ESCAPE:
                gameController.fireEvent(Game_Controller.ACTION.PAUSE_TOGGLE);
                break;
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        // Diese Methode kann leer bleiben, wenn keine Aktion erforderlich ist
    }

}
