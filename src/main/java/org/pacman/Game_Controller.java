/* Game_Controller.java */
package org.pacman;

import java.awt.*;

public class Game_Controller implements Runnable{
    private static GUI_Controller  mGUI_C_Ref;
    private static Game_Controller  mGame_C_Ref;
    private Game_Panel gamePanel;
    private Thread gameThread;
    private Thread movementThread;

    private Pacman pacman;
    private ACTION lastDirection = ACTION.MOVE_NONE;
    private ACTION nextDirection = ACTION.MOVE_NONE;
    private Maze maze;
    private boolean mRunning = true;
    public static final int mTickrate = 1000 / 60; // Tickrate in 60 pro Sekunde
    public static final int mTickrateMovement = 1000 / 20; // Tickrate in 60 pro Sekunde



    private void continuousMovement() {
        while (mRunning) {
            if (lastDirection != ACTION.MOVE_NONE && state != GAMESTATE.PAUSED) {
                if (nextDirection != ACTION.MOVE_NONE && !isCollision(nextDirection)) {
                    lastDirection = nextDirection;
                    nextDirection = ACTION.MOVE_NONE;
                }
                movePacman(lastDirection);
            }

            try {
                Thread.sleep(mTickrateMovement);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * Gameloop für Spielelogik
     */
    @Override
    public void run() {
        long startTime, endTime, deltaTime;
        while(mRunning){
            startTime = System.currentTimeMillis();
            //Game Logic
            if (state != GAMESTATE.PAUSED){
                gamePanel.repaint();
                //System.out.println(pacman.getX()+" "+ pacman.getY());
                //System.out.println(maze.getCellSize());

            }
            //
            endTime = System.currentTimeMillis();
            deltaTime = endTime - startTime;
            if (mTickrate - deltaTime > 0){
                try {
                    Thread.sleep(mTickrate - deltaTime);
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted");
                    throw new RuntimeException(e);
                }
            }
        }
        mGUI_C_Ref.quit();
        /*
            Code für Speichern etc.
         */
    }


    // in welchen Zuständen kann das Programm sein
    public enum GAMESTATE{
        MENU,
        RUNNING,
        PAUSED,
        GAMEOVER
    }


    private GAMESTATE state; // aktueller Zustand

    //welche Aktionen können auftreten
    public enum ACTION{
        QUIT,
        START,
        PAUSE_TOGGLE,
        MENU,
        HIGH_SCORES,
        SETTINGS,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        MOVE_NONE
    }
    Game_Controller(){
        // Initialisieren des Labyrinths
        this.maze = new Maze();

        mGame_C_Ref = this;
        mGUI_C_Ref = new GUI_Controller(this);
        this.gamePanel = GUI_Controller.getGamePanel();


        initializeGame();

        // Initialisieren von Pacman

        //starten des Gameloop
        gameThread = new Thread(this);
        gameThread.start();

        // Initialisieren des Bewegungs-Threads
        movementThread = new Thread(this::continuousMovement);
        movementThread.start();


        //wechseln zum Menü
        changeState(GAMESTATE.MENU);
    }

    public void initializeGame() {
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        Point PacManStart = maze.findPacmanStart();
        System.out.println("start "+PacManStart.x + " " + PacManStart.y);
        pacman = new Pacman(PacManStart.x, PacManStart.y);
    }

    /**
     * der aktuelle Zustand des Spieles wird geändert
     * ruft changeWindowConfig() auf
     * @param newState
     */
    public void changeState(GAMESTATE newState){
        state = newState;
        mGUI_C_Ref.changeWindowConfig(newState);
    }

    public static GUI_Controller getGUI_C_Ref() {
        return mGUI_C_Ref;
    }
    public static Game_Controller getGame_C_Ref(){
        return mGame_C_Ref;
    }

    /**
     * ein Event wird ausgelöst
     * @param a
     */
    public void fireEvent(ACTION a){
        switch (a){
            case START -> {
                initializeGame();
                changeState(GAMESTATE.RUNNING);
            }
            case QUIT -> mRunning = false;
            case PAUSE_TOGGLE -> {
                if (state == GAMESTATE.PAUSED){
                    changeState(GAMESTATE.RUNNING);
                }else if (state == GAMESTATE.RUNNING){
                    changeState(GAMESTATE.PAUSED);
                }
            }
            case MENU -> changeState(GAMESTATE.MENU);

            case MOVE_UP -> {
                nextDirection = ACTION.MOVE_UP;
                if (!isCollision(ACTION.MOVE_UP)) {
                    setLastDirection(ACTION.MOVE_UP);
                }
            }
            case MOVE_DOWN -> {
                nextDirection = ACTION.MOVE_DOWN;
                if (!isCollision(ACTION.MOVE_DOWN)) {
                    setLastDirection(ACTION.MOVE_DOWN);
                }
            }
            case MOVE_LEFT -> {
                nextDirection = ACTION.MOVE_LEFT;
                if (!isCollision(ACTION.MOVE_LEFT)) {
                    setLastDirection(ACTION.MOVE_LEFT);
                }
            }
            case MOVE_RIGHT -> {
                nextDirection = ACTION.MOVE_RIGHT;
                if (!isCollision(ACTION.MOVE_RIGHT)) {
                    setLastDirection(ACTION.MOVE_RIGHT);
                }
            }

        }
    }


    private boolean isCollision(ACTION action) {
        int cellSize = maze.getCellSize();
        int speed = pacman.getSpeed();
        int offset = cellSize / 4; // Ein Viertel der Zellengröße als Versatz

        // Bestimmen der zukünftigen Position basierend auf der Aktion
        int futureX = pacman.getX();
        int futureY = pacman.getY();

        switch (action) {
            case MOVE_UP:
                futureY -= speed;
                break;
            case MOVE_DOWN:
                futureY += speed;
                break;
            case MOVE_LEFT:
                futureX -= speed;
                break;
            case MOVE_RIGHT:
                futureX += speed;
                break;
        }

        // Berechnen der vier Ecken um Pac-Man
        Point[] corners = new Point[] {
                new Point(futureX + offset, futureY + offset), // Oben links
                new Point(futureX + cellSize - offset, futureY + offset), // Oben rechts
                new Point(futureX + offset, futureY + cellSize - offset), // Unten links
                new Point(futureX + cellSize - offset, futureY + cellSize - offset) // Unten rechts
        };

        for (Point corner : corners) {
            int gridX = corner.x / cellSize;
            int gridY = corner.y / cellSize;
            // System.out.println("corn: "+gridX+" "+gridY);

            if (maze.isWall(gridX, gridY)) {
                //System.out.println("kol: "+gridX+" "+gridY);
                return true; // Kollision erkannt
            }
        }

        return false; // Keine Kollision
    }

    public Point getPacmanGridPosition() {
        int cellSize = maze.getCellSize();
        int adjustedX = (pacman.getX()) / cellSize;
        int adjustedY = (pacman.getY()) / cellSize;
        // System.out.println(adjustedX+" "+adjustedY);

        return new Point(adjustedX, adjustedY);
    }

    private synchronized void movePacman(ACTION action) {
        switch (action) {
            case MOVE_UP:
                movePacmanUp();
                break;
            case MOVE_DOWN:
                movePacmanDown();
                break;
            case MOVE_LEFT:
                movePacmanLeft();
                break;
            case MOVE_RIGHT:
                movePacmanRight();
                break;
        }
    }

    private void movePacmanUp() {
        // Logik für Bewegung nach oben
        if (!isCollision(ACTION.MOVE_UP)) {
            pacman.moveUp();
        }
    }

    private void movePacmanDown() {
        // Logik für Bewegung nach unten
        if (!isCollision(ACTION.MOVE_DOWN)) {
            pacman.moveDown();
        }
    }

    private void movePacmanLeft() {
        // Logik für Bewegung nach links
        if (!isCollision(ACTION.MOVE_LEFT)) {
            pacman.moveLeft();
        }
    }

    private void movePacmanRight() {
        // Logik für Bewegung nach rechts
        if (!isCollision(ACTION.MOVE_RIGHT)) {
            pacman.moveRight();
        }
    }

    public Pacman getPacman() {
        return pacman;
    }

    public Maze getMaze() {
        return maze;
    }

    public void setMazeStartPositions(int x, int y) {
        maze.setMazeStartX(x);
        maze.setMazeStartY(y);
    }

    public void setLastDirection(ACTION direction) {
        this.lastDirection = direction;
    }
}
