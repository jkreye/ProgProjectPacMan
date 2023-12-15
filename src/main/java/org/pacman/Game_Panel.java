/* Game_Panel.java */
package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game_Panel extends JPanel implements KeyListener {
    private static final int PADDING_TOP = 5;
    private JLabel label;
    private Game_Controller gameController;

    Game_Panel(Game_Controller controller){
        this.gameController = controller;

        setDoubleBuffered(true);

        //setBackground(new Color(70, 70, 70));
        setBackground(new Color(0,0,0));

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
        char[][] grid = gameController.getMaze().getGrid();
        Pacman pacman = gameController.getPacman();

        // Berechnen der Größe einer einzelnen Zelle basierend auf der Fenstergröße
        Maze.calculateCellSize(getWidth(), getHeight(), grid, PADDING_TOP);

        int cellSize = Maze.getCellSize();

        // Berechnen der Größe des gesamten Labyrinths
        int mazeWidth = grid[0].length * cellSize;
        int mazeHeight = (grid.length+PADDING_TOP) * cellSize;

        // Berechnen der Startposition, um das Labyrinth in der Mitte zu zeichnen
        int startX = (getWidth() - mazeWidth) / 2;

        // Definieren eines oberen Randes
        int paddingTop = 150;
        int startY = paddingTop + (getHeight() - mazeHeight - paddingTop) / 2;

        // Zeichnen des Labyrinths beginnend bei startX und startY
        gameController.setMazeStartPositions(startX, startY);

        drawMaze(g, startX, startY, cellSize);

        // pacman
        if (pacman != null) {
            pacman.setSpeed(cellSize/3);
            int pacmanX = startX + pacman.getX();
            int pacmanY = startY + pacman.getY();

            g.setColor(Color.YELLOW);
            g.fillOval(pacmanX, pacmanY, cellSize, cellSize);

            g.setColor(Color.RED); // Farbe für den Punkt
            g.fillOval(pacmanX, pacmanY, 5, 5);

        }



    }


    private void drawMaze(Graphics g, int startX, int startY, int cellSize) {
        char[][] grid = gameController.getMaze().getGrid();
        int coinSize = cellSize / 5; // Größe der Coins
        int killCoinSize = cellSize / 3; // Größe der KillCoins


        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == '#') {
                    g.setColor(Color.BLUE);
                    g.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                    g.setColor(Color.GREEN);
                    g.drawRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                }
                if (grid[row][col] == '-') {
                    g.setColor(Color.RED);
                    g.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                }
                if (grid[row][col] == '.') {
                    // Coins
                    // Zentrieren der Coins in jeder Zelle
                    int coinX = startX + col * cellSize + cellSize / 2 - coinSize / 2;
                    int coinY = startY + row * cellSize + cellSize / 2 - coinSize / 2;

                    g.setColor(Color.yellow);
                    g.fillOval(coinX, coinY, coinSize, coinSize);
                }
                if (grid[row][col] == 'o') {
                    // Killcoins
                    // Zentrieren der Killcoins in jeder Zelle
                    int killcoinX = startX + col * cellSize + cellSize / 2 - killCoinSize / 2;
                    int killcoinY = startY + row * cellSize + cellSize / 2 - killCoinSize / 2;

                    g.setColor(Color.green);
                    g.fillRect(killcoinX, killcoinY, killCoinSize, killCoinSize);
                }
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    // KeyListener-Methoden
    @Override
    public void keyPressed(KeyEvent e) {
        gameController.getPacmanGridPosition();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_UP);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_LEFT);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                gameController.fireEvent(Game_Controller.ACTION.MOVE_DOWN);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
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
