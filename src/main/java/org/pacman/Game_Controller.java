/* Game_Controller.java */
package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Game_Controller implements Runnable{
    private static GUI_Controller  mGUI_C_Ref;
    private static Game_Controller  mGame_C_Ref;
    private Game_Panel gamePanel;
    private Thread gameThread;
    private Thread movementThread, movementGhostsThread;

    private Pacman pacman;
    private Random random = new Random();
    private List<Ghost> ghosts;
    private ACTION lastDirection = ACTION.MOVE_NONE;
    private ACTION nextDirection = ACTION.MOVE_NONE;
    private int score = 0; // Punktestand
    private int lives = 3; // Anzahl der Leben

    private final Maze maze;
    private boolean mRunning = true;
    public static final int mTickrate = 1000 / 60; // Tickrate in 60 pro Sekunde
    public static final int mTickrateMovement = 1000 / 18; // Tickrate PacMan
    public static final int mTickrateMovementGhosts = 1000 / 14; // Tickrate Ghosts

    // Konstanten für die Startpositionen und Geschwindigkeiten der Geister
    private int GHOST_SPEED = 1; // Geschwindigkeit der Geister


    private void continuousMovement() {
        while (mRunning) {
            try {
                if (lastDirection != ACTION.MOVE_NONE && state != GAMESTATE.PAUSED) {
                    if (nextDirection != ACTION.MOVE_NONE && !isCollision(nextDirection)) {
                        lastDirection = nextDirection;
                        nextDirection = ACTION.MOVE_NONE;
                    }
                    movePacman(lastDirection);
                }


                Thread.sleep(mTickrateMovement);
            } catch (InterruptedException e) {
                // Unterbrechung behandeln, aber nicht neu auslösen
                System.err.println("Movement thread interrupted");
                break;
            }
        }
    }

    private void continuousMovementGhosts() {
        while (mRunning) {
            try {
                if (state != GAMESTATE.PAUSED) {
                    updateGhostPositions();
                }

                Thread.sleep(mTickrateMovementGhosts);
            } catch (InterruptedException e) {
                // Unterbrechung behandeln, aber nicht neu auslösen
                System.err.println("Movement thread interrupted");
                break;
            }
        }
    }

    // Methode zum Auswählen einer zufälligen Richtung
    private ACTION getRandomDirection(ACTION currentDirection, Ghost lghost) {
        List<ACTION> possibleDirections = new ArrayList<>(Arrays.asList(
                ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT));

        // Entfernen von Richtungen, die zu einer Kollision führen würden
        possibleDirections.removeIf(direction -> isCollisionForGhost(lghost, direction)
                || direction == getOppositeDirection(currentDirection));

        // Wählen einer zufälligen verbleibenden Richtung
        if (!possibleDirections.isEmpty()) {
            return possibleDirections.get(new Random().nextInt(possibleDirections.size()));
        }

        // Wenn keine andere Richtung möglich ist, umkehren
        return getOppositeDirection(currentDirection);
    }


    // Hilfsmethode, um zu überprüfen, ob zwei Richtungen gegensätzlich sind
    private boolean isOppositeDirection(ACTION dir1, ACTION dir2) {
        return (dir1 == ACTION.MOVE_UP && dir2 == ACTION.MOVE_DOWN) ||
                (dir1 == ACTION.MOVE_DOWN && dir2 == ACTION.MOVE_UP) ||
                (dir1 == ACTION.MOVE_LEFT && dir2 == ACTION.MOVE_RIGHT) ||
                (dir1 == ACTION.MOVE_RIGHT && dir2 == ACTION.MOVE_LEFT);
    }

    private ACTION getOppositeDirection(ACTION direction) {
        switch (direction) {
            case MOVE_UP: return ACTION.MOVE_DOWN;
            case MOVE_DOWN: return ACTION.MOVE_UP;
            case MOVE_LEFT: return ACTION.MOVE_RIGHT;
            case MOVE_RIGHT: return ACTION.MOVE_LEFT;
            default: return ACTION.MOVE_NONE;
        }
    }

    // Methode zum Aktualisieren der Position der Geister
    private void updateGhostPositions() {
        for (Ghost ghost : ghosts) {
            if (isAtIntersection(ghost)) {
                ghost.setDirection(chooseDirectionAtIntersection(ghost));
            } else if (shouldTurnAround()) {
                ghost.setDirection(getOppositeDirection(ghost.getDirection()));
            } else if (isCollisionForGhost(ghost, ghost.getDirection())) {
                // Bei Kollision, wähle eine neue zufällige Richtung
                ghost.setDirection(getRandomDirection(ghost.getDirection(), ghost));
            }
            // Bewege den Geist in die aktuelle Richtung
            moveGhost(ghost, ghost.getDirection());
        }
    }

    //--
    private boolean isAtIntersection(Ghost ghost) {
        int availableDirections = 0;
        List<ACTION> possibleDirections = new ArrayList<>();

        // Liste der gültigen Bewegungsrichtungen
        List<ACTION> movementDirections = Arrays.asList(ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT);

        // Prüfe jede Richtung, außer der entgegengesetzten Richtung des Geistes
        for (ACTION direction : movementDirections) {
            // Überspringe die entgegengesetzte Richtung und Nicht-Bewegung
            if (direction == getOppositeDirection(ghost.getDirection())) {
                continue;
            }

            // Simuliere die Bewegung in dieser Richtung

            // Zähle die Richtungen, die keine Kollision verursachen
            if (!isCollisionForGhost(ghost, direction)) {
                availableDirections++;
            }
        }
        // System.out.println(availableDirections);

        // Eine Kreuzung liegt vor, wenn es mehr als eine verfügbare Richtung gibt
        return availableDirections > 1;
    }


    private ACTION chooseDirectionAtIntersection(Ghost ghost) {
        List<ACTION> possibleDirections = new ArrayList<>();
        List<ACTION> movementDirections = Arrays.asList(ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT);
        Random random = new Random();

        // Überprüfen, ob eine Fortsetzung in der aktuellen Richtung möglich ist
        if (!isCollisionForGhost(ghost, ghost.getDirection())) {
            // Entscheiden, ob der Geist geradeaus weitergeht (30 % Chance)
            if (random.nextInt(100) < 30) {
                return ghost.getDirection();
            }
            // Füge die aktuelle Richtung trotzdem als Option hinzu
            possibleDirections.add(ghost.getDirection());
        }

        // Überprüfen Sie andere Richtungen, ohne die entgegengesetzte Richtung zu berücksichtigen
        for (ACTION direction : movementDirections) {
            if (direction == ACTION.MOVE_NONE || direction == getOppositeDirection(ghost.getDirection())) {
                continue;
            }

            // Simuliere die Bewegung in dieser Richtung
            if (!isCollisionForGhost(ghost, direction)) {
                possibleDirections.add(direction);
            }
        }

        // Wähle eine zufällige Richtung aus den verbleibenden möglichen Richtungen
        if (!possibleDirections.isEmpty()) {
            int randomIndex = random.nextInt(possibleDirections.size());
            return possibleDirections.get(randomIndex);
        }

        return ghost.getDirection(); // Behalte die aktuelle Richtung bei, falls keine andere möglich ist
    }



    private boolean shouldTurnAround() {
        // Bestimmen Sie einen zufälligen Wert, z.B. 1% Wahrscheinlichkeit
        return new Random().nextInt(1000) < 10;
        //return false;
    }
    //--

    // Methode zum Bewegen eines Geistes
    private void moveGhost(Ghost ghost, ACTION direction) {
        switch (direction) {
            case MOVE_UP:
                ghost.moveUp();
                break;
            case MOVE_DOWN:
                ghost.moveDown();
                break;
            case MOVE_LEFT:
                ghost.moveLeft();
                break;
            case MOVE_RIGHT:
                ghost.moveRight();
                break;
        }
    }

    private boolean isCollisionForGhost(Ghost ghost, ACTION direction) {
        int cellSize = maze.getCellSize();
        int speed = ghost.getSpeed(); // Angenommen, die Geister haben eine "speed"-Eigenschaft
        int offset = 4; // Ein Viertel der Zellengröße als Versatz

        // Bestimmen der zukünftigen Position basierend auf der Richtung
        int futureX = ghost.getX();
        int futureY = ghost.getY();

        switch (direction) {
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

        // Berechnen der vier Ecken um den Geist
        Point[] corners = new Point[] {
                new Point(futureX + offset, futureY + offset), // Oben links
                new Point(futureX + cellSize - offset, futureY + offset), // Oben rechts
                new Point(futureX + offset, futureY + cellSize - offset), // Unten links
                new Point(futureX + cellSize - offset, futureY + cellSize - offset) // Unten rechts
        };

        for (Point corner : corners) {
            int gridX = corner.x / cellSize;
            int gridY = corner.y / cellSize;
            //System.out.println(gridX+" "+gridY);

            if (maze.isWallOrTeleportForGhost(gridX, gridY)) {
                return true; // Kollision erkannt
            }
        }

        return false; // Keine Kollision
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
                checkAndTeleportPacman();


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

    public List<Ghost> getGhosts() {
        return ghosts;
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
        MOVE_NONE,
        LOST,
        WIN
    }

    // Ghost
    public enum GhostType {
        SHADOW, // Blinky
        SPEEDY, // Pinky
        BASHFUL, // Inky
        POKEY // Clyde
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE // kann verwendet werden, wenn der Geist nicht in Bewegung ist
    }

    Game_Controller(){
        // Initialisieren des Labyrinths
        this.maze = new Maze();
        initializeGame();

        mGame_C_Ref = this;
        mGUI_C_Ref = new GUI_Controller(this);
        this.gamePanel = GUI_Controller.getGamePanel();

        // Initialisieren von Pacman

        //starten des Gameloop
        gameThread = new Thread(this);
        gameThread.start();

        // Initialisieren des Bewegungs-Threads Pacman
        movementThread = new Thread(this::continuousMovement);
        movementThread.start();

        // Initialisieren des Bewegungs-Threads Ghosts
        movementGhostsThread = new Thread(this::continuousMovementGhosts);
        movementGhostsThread.start();


        //wechseln zum Menü
        changeState(GAMESTATE.MENU);
    }

    public void initializeGame() {
        System.out.println("GAME START");
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        lives=3;
        score=0;
        Point PacManStart = maze.findPacmanStart();
        if (PacManStart != null) {
            this.pacman = new Pacman(PacManStart.x, PacManStart.y);
            if (isCollision(ACTION.MOVE_RIGHT)){
                this.pacman = new Pacman(PacManStart.x, PacManStart.y);
            }
        }

        // Initialisieren der Geisterliste
        Point shadowStart = maze.findGhostStart('B'); // BLINKY
        Point bashfulStart = maze.findGhostStart('I'); // INKY
        Point speedyStart = maze.findGhostStart('S'); // Speedy, PINKY
        Point pokeyStart = maze.findGhostStart('C'); // CLYDE
        this.ghosts = new ArrayList<>();
        this.ghosts.add(new Ghost(shadowStart.x, shadowStart.y, GhostType.SHADOW, GHOST_SPEED));
        this.ghosts.add(new Ghost(speedyStart.x, speedyStart.y, GhostType.SPEEDY, GHOST_SPEED));
        this.ghosts.add(new Ghost(pokeyStart.x, pokeyStart.y, GhostType.POKEY, GHOST_SPEED));
        this.ghosts.add(new Ghost(bashfulStart.x, bashfulStart.y, GhostType.BASHFUL, GHOST_SPEED));

    }

    private void resetGame() {
        // Spiel zurücksetzen
        score = 0;
        lives = 3;
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        ghosts = new ArrayList<>();
        // Optional: Pac-Man und das Labyrinth zurücksetzen !!!
        // ...
    }

    private void handleGameOver() {
        // Game Over Logik
        // Zurücksetzen des Spiels oder andere erforderliche Aktionen
        resetGame();
        // Wechseln zum Hauptmenü
        changeState(GAMESTATE.MENU);
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
                changeState(GAMESTATE.RUNNING);
                initializeGame();
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
            case LOST -> {
                changeState(GAMESTATE.GAMEOVER);
            }
            case WIN -> {
                changeState(GAMESTATE.GAMEOVER);
            }

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


    public void checkAndTeleportPacman() {

        if (this.pacman != null) {
            Point pacmanPosition = getPacmanGridPosition();


            char[][] grid = maze.getGrid();
            int cellSize = maze.getCellSize();

            char cell = grid[pacmanPosition.y][pacmanPosition.x];
            if (cell == 'T') {
                // Teleportiere Pac-Man
                gamePanel.triggerPortalBlink();
                loseLife();
                if (pacmanPosition.x == 0) { // Linker Rand
                    pacman.setX((grid.length - 5) * cellSize); // Position am rechten Rand (minus 1 für Index, minus 1 für Rand)
                } else if (pacmanPosition.x == grid.length - 4) { // Rechter Rand
                    pacman.setX(cellSize); // Position am linken Rand
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

    // Methoden zum Aktualisieren von Score und Lives
    public void increaseScore(int amount) {
        score += amount;
    }

    public void loseLife() {
        lives--;
        if (lives <= 0) {
            changeState(GAMESTATE.GAMEOVER);
            handleGameOver();
        }
    }

    // Getter-Methoden
    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }
}
