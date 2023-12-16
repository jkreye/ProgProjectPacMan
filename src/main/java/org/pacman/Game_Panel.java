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
    private Maze maze;

    // Portal Blink
    private boolean portalBlinkState = false;
    private final long BLINK_DURATION = 100; // Dauer des Aufblinkens in Millisekunden
    private long blinkStartTime = -1;


    Game_Panel(Game_Controller controller){
        this.gameController = controller;
        this.maze = gameController.getMaze();


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
        int calcSpeed = cellSize/3;

        // Berechnen der Größe des gesamten Labyrinths
        int mazeWidth = grid[0].length * cellSize;
        int mazeHeight = (grid.length+PADDING_TOP) * cellSize;

        // Berechnen der Startposition, um das Labyrinth in der Mitte zu zeichnen
        int startX = (getWidth() - mazeWidth) / 2;

        // Definieren eines oberen Randes
        int paddingTop = 150;
        int startY = paddingTop + (getHeight() - mazeHeight - paddingTop) / 2;



        // pacman
        if (pacman != null) {
            // Zeichnen des Labyrinths beginnend bei startX und startY
            gameController.setMazeStartPositions(startX, startY);

            drawMaze(g, startX, startY, cellSize);


            pacman.setSpeed(calcSpeed);
            int pacmanX = startX + pacman.getX();
            int pacmanY = startY + pacman.getY();

            g.setColor(Color.YELLOW);
            g.fillOval(pacmanX, pacmanY, cellSize, cellSize);

        }

        // Zeichnen der Geister
        for (Ghost ghost : gameController.getGhosts()) {
            ghost.setSpeed(calcSpeed);

        }
        drawGhosts(g, startX, startY, cellSize);

        // Score und Lives oberhalb des Labyrinths zeichnen
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16)); // Setzen Sie die Schriftart und -größe
        int scoreY = 30; // Y-Position für Score und Lives
        g.drawString("Score: " + gameController.getScore(), startX, scoreY);
        // Zeichnen der Leben als Kreise
        int livesX = startX + 100; // X-Position für die Leben
        int livesY = scoreY - 10;  // Y-Position für die Leben
        int circleDiameter = cellSize/2;   // Durchmesser der Kreise
        for (int i = 0; i < gameController.getLives(); i++) {
            g.setColor(Color.YELLOW);
            g.fillOval(livesX + i * (circleDiameter + 5), livesY, circleDiameter, circleDiameter);
        }
    }

    private void drawGhosts(Graphics g, int startX, int startY, int cellSize) {

        for (Ghost ghost : gameController.getGhosts()) {
            // Umrechnen der Geisterposition in Bildschirmkoordinaten
            int ghostX = ghost.getX() +startX;
            int ghostY = ghost.getY() +startY;

            // Farbe basierend auf dem Geistertyp setzen
            switch (ghost.getType()) {
                case SHADOW:
                    g.setColor(Color.RED);
                    break;
                case SPEEDY:
                    g.setColor(Color.PINK);
                    break;
                case POKEY:
                    g.setColor(Color.ORANGE);
                    break;
                case BASHFUL:
                    g.setColor(Color.CYAN);
                    break;
                default:
                    g.setColor(Color.GRAY);
            }

            // Zeichnen des Geists
            g.fillOval(ghostX, ghostY, cellSize, cellSize);
            g.drawRect(ghostX, ghostY, cellSize, cellSize);

        }
    }

    private void drawMaze(Graphics g, int startX, int startY, int cellSize) {
        char[][] grid = gameController.getMaze().getGrid();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        // Einstellen der Randdicke
        float borderWidth = 2.0f;
        g2d.setStroke(new BasicStroke(borderWidth));


        int coinSize = cellSize / 5; // Größe der Coins
        int killCoinSize = cellSize / 3; // Größe der KillCoins


        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == '#') {
                    g.setColor(Color.BLUE);
                    g.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);
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
                if (grid[row][col] == 'T') {
                    // Zeichne Teleport-Punkt
                    int telPointSize = (int) Math.round(cellSize*.8);
                    int telX = startX + col * cellSize + cellSize / 2 - telPointSize / 2;
                    int telY = startY + row * cellSize + cellSize / 2 - telPointSize / 2;
                    //g.setColor(Color.MAGENTA);
                    //g.drawOval(telX, telY, telPointSize, telPointSize);
                    drawPortal(g, telX, telY, telPointSize);
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

    private void drawPortal(Graphics g, int x, int y, int cellSize) {
        if (portalBlinkState) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - blinkStartTime < BLINK_DURATION) {
                g.setColor(Color.MAGENTA); // Blinkfarbe
            } else {
                g.setColor(Color.MAGENTA); // Ursprüngliche Farbe
                portalBlinkState = false;
            }
            g.fillOval(x, y, cellSize, cellSize);
        } else {
            g.setColor(Color.MAGENTA); // Ursprüngliche Farbe
            g.drawOval(x, y, cellSize, cellSize);
        }

    }

    private void togglePortalBlink() {
        portalBlinkState = true;
        blinkStartTime = System.currentTimeMillis();
    }

    public void triggerPortalBlink() {
        togglePortalBlink();
    }

}
