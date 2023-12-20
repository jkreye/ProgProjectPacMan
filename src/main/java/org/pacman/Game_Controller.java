/* Game_Controller.java */
package org.pacman;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
/**
 * Haupt-Controller für das Pac-Man-Spiel.
 * Verwaltet die Spiellogik, den Zustand und die Interaktionen zwischen den Spielobjekten.
 */
public class Game_Controller implements Runnable{
    private static GUI_Controller  mGUI_C_Ref;
    private static Game_Controller  mGame_C_Ref;
    private final Game_Panel gamePanel;
    private Thread gameThread, movementThread, movementGhostsThread;
    private final Maze maze;

    private Pacman pacman;
    private List<Ghost> ghosts;
    private ACTION lastDirection = ACTION.MOVE_NONE;
    private ACTION nextDirection = ACTION.MOVE_NONE;
    private int score = 0; // Punktestand
    private int lives = 3; // Anzahl der Leben

    private long vulnerabilityDuration = 0;
    private static final long VULNERABILITY_TIME = 5000;

    private long nextGhostReleaseTime;
    private static final long GHOST_RELEASE_INTERVAL = 5000; // 5 Sekunden in Millisekunden
    private static final long INITIAL_RELEASE_DELAY = 1000; // 1 Sekunde in Millisekunden
    private static final long BLINK_THRESHOLD = 2000; // Blinken beginnt 2 Sekunden vor Ende der Verwundbarkeit


    private long pauseStartTime = 0;

    private Random random = new Random();;

    private boolean mRunning = true;
    public static final int mTickrate = 1000 / 60; // Tickrate in 60 pro Sekunde
    public static int mTickrateMovement = 1000 / 20; // Tickrate PacMan
    public static int mTickrateMovementGhosts = 1000 / 16; // Tickrate Ghosts

    private int totalDots; // Total number of dots in the level
    private int collectedDots = 0; // Number of collected dots
    private int totalPpillscoins; // Total number of bitcoins in the level
    private int collectedPpills = 0;


    /**
     * Führt die kontinuierliche Bewegung von Pac-Man aus.
     * Diese Methode läuft in einer Schleife, die so lange aktiv bleibt, wie das Spiel läuft (mRunning ist true).
     * In jedem Durchlauf der Schleife wird überprüft, ob eine Bewegungsrichtung festgelegt ist und ob eine Kollision vorliegt.
     * Wenn eine gültige Bewegungsrichtung vorhanden ist, wird Pac-Man bewegt. Anschließend pausiert der Thread für eine kurze Zeit,
     * basierend auf der definierten Tickrate für Pac-Mans Bewegung (mTickrateMovement).
     * Bei einer Unterbrechung des Threads wird eine Fehlermeldung ausgegeben und die Schleife beendet.
     */
    private void continuousMovement() {
        while (mRunning) {
            try {
                processMovement();
                Thread.sleep(mTickrateMovement);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Movement thread interrupted: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * Verarbeitet die Bewegungslogik von Pac-Man.
     * Überprüft die aktuelle und nächste Richtung und bewegt Pac-Man entsprechend,
     * wenn das Spiel nicht pausiert ist und keine Kollision vorliegt.
     */
    private void processMovement() {
        if (lastDirection != ACTION.MOVE_NONE && state != GAMESTATE.PAUSED) {
            if (nextDirection != ACTION.MOVE_NONE && !isCollision(nextDirection)) {
                lastDirection = nextDirection;
                nextDirection = ACTION.MOVE_NONE;
            }
            movePacman(lastDirection);
        }
    }

    /**
     * Führt die kontinuierliche Bewegung der Geister aus.
     * In dieser Methode wird in einer Schleife, die läuft, solange das Spiel aktiv ist (mRunning ist true), die Position der Geister aktualisiert.
     * Die Aktualisierung erfolgt nur, wenn das Spiel nicht pausiert ist (state != GAMESTATE.PAUSED).
     * Nach jeder Aktualisierung der Geisterpositionen pausiert der Thread für eine festgelegte Zeit,
     * basierend auf der Tickrate für die Bewegung der Geister (mTickrateMovementGhosts).
     * Bei einer Unterbrechung des Threads wird der Thread-Status auf 'interrupted' gesetzt und eine Fehlermeldung ausgegeben.
     */
    private void continuousMovementGhosts() {
        while (mRunning) {
            try {
                if (state != GAMESTATE.PAUSED) {
                    updateGhostPositions();
                }
                Thread.sleep(mTickrateMovementGhosts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Ghost movement thread interrupted: " + e.getMessage());
                break;
            }
        }
    }


    /**
     * Wählt eine zufällige Richtung für einen Geist aus, unter Berücksichtigung möglicher Kollisionen und der aktuellen Richtung.
     * Richtungen, die zu einer Kollision führen oder direkt entgegengesetzt zur aktuellen Richtung sind, werden ausgeschlossen.
     * Falls keine Richtung ohne Kollision möglich ist, wird die entgegengesetzte Richtung zur aktuellen Richtung gewählt.
     *
     * @param currentDirection Die aktuelle Bewegungsrichtung des Geistes.
     * @param lghost Der Geist, für den die Richtung bestimmt wird.
     * @return Die gewählte ACTION, die die neue Richtung des Geistes angibt.
     */
    private ACTION getRandomDirection(ACTION currentDirection, Ghost lghost) {
        List<ACTION> possibleDirections = new ArrayList<>(Arrays.asList(
                ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT));

        removeInvalidDirections(possibleDirections, currentDirection, lghost);

        if (!possibleDirections.isEmpty()) {
            return possibleDirections.get(random.nextInt(possibleDirections.size()));
        }

        return getOppositeDirection(currentDirection);
    }

    /**
     * Entfernt ungültige Bewegungsrichtungen aus der Liste der möglichen Richtungen für einen Geist.
     * Eine Richtung gilt als ungültig, wenn sie zu einer Kollision mit einem Hindernis führen würde
     * oder wenn sie direkt entgegengesetzt zur aktuellen Bewegungsrichtung des Geistes ist.
     *
     * @param possibleDirections Die Liste der möglichen Bewegungsrichtungen.
     * @param currentDirection Die aktuelle Bewegungsrichtung des Geistes.
     * @param lghost Der Geist, für den die Bewegungsrichtungen überprüft werden.
     */
    private void removeInvalidDirections(List<ACTION> possibleDirections, ACTION currentDirection, Ghost lghost) {
        if (possibleDirections == null || lghost == null) {
            throw new IllegalArgumentException("Die Liste der möglichen Richtungen und der Geist dürfen nicht null sein.");
        }

        possibleDirections.removeIf(direction ->
                isCollisionForGhost(lghost, direction) || direction == getOppositeDirection(currentDirection)
        );
    }


    /**
     * Ermittelt die entgegengesetzte Bewegungsrichtung zu einer gegebenen Richtung.
     * Diese Methode wird verwendet, um die Richtung zu bestimmen, in die sich ein Charakter umdrehen sollte.
     * Zum Beispiel ist die entgegengesetzte Richtung zu MOVE_UP MOVE_DOWN.
     *
     * @param direction Die aktuelle Bewegungsrichtung.
     * @return Die entgegengesetzte Bewegungsrichtung. Gibt MOVE_NONE zurück, falls die übergebene Richtung ungültig ist.
     */
    private ACTION getOppositeDirection(ACTION direction) {
        return switch (direction) {
            case MOVE_UP -> ACTION.MOVE_DOWN;
            case MOVE_DOWN -> ACTION.MOVE_UP;
            case MOVE_LEFT -> ACTION.MOVE_RIGHT;
            case MOVE_RIGHT -> ACTION.MOVE_LEFT;
            default -> ACTION.MOVE_NONE;
        };
    }


    /**
     * Aktualisiert die Positionen aller Geister im Spiel.
     * Für jeden Geist, der sich nicht im Gefängnis befindet, wird überprüft, ob er sich an einer Kreuzung befindet,
     * sich umdrehen soll oder auf eine Kollision zusteuert.
     * Basierend auf diesen Prüfungen wird die Bewegungsrichtung des Geistes festgelegt und der Geist entsprechend bewegt.
     * Die Methode berücksichtigt, ob der Geist in eine neue Richtung gehen, sich umdrehen oder bei einer Kollision
     * eine zufällige neue Richtung wählen soll.
     */
    private void updateGhostPositions() {
        for (Ghost ghost : ghosts) {
            if (!ghost.isInJail()) {
                if (isAtIntersection(ghost)) {
                    ghost.setDirection(chooseDirectionAtIntersection(ghost));
                } else if (shouldTurnAround()) {
                    ghost.setDirection(getOppositeDirection(ghost.getDirection()));
                } else if (isCollisionForGhost(ghost, ghost.getDirection())) {
                    ghost.setDirection(getRandomDirection(ghost.getDirection(), ghost));
                }
                moveGhost(ghost, ghost.getDirection());
            }
        }
    }

    /**
     * Überprüft Kollisionen zwischen Pac-Man und allen Geistern im Spiel.
     * Wenn Pac-Man einen Geist berührt, wird abhängig vom Zustand des Geistes eine entsprechende Aktion ausgeführt.
     * Bei Berührung mit einem verwundbaren Geist wird dieser zurück zum Startpunkt gesetzt und ist nicht mehr verwundbar.
     * Bei einer Kollision mit einem nicht-verwundbaren Geist verliert Pac-Man ein Leben, und das Spiel wird zurückgesetzt.
     */
    private void checkPacmanGhostCollision() {
        if (pacman != null) {
            for (Ghost ghost : ghosts) {
                if (Math.abs(pacman.getX() - ghost.getX()) < Maze.getCellSize() &&
                        Math.abs(pacman.getY() - ghost.getY()) < Maze.getCellSize()) {

                    if (ghost.getVulnerable()) {
                        resetVulnerableGhost(ghost);
                    } else {
                        handleCollisionWithInvulnerableGhost();
                        break; // Unterbrechen der Schleife, da die Kollision bereits behandelt wird
                    }
                }
            }
        }
    }

    /**
     * Setzt einen verwundbaren Geist nach einer Kollision mit Pac-Man zurück.
     * Der Geist wird zum Startpunkt zurückgesetzt, ist nicht mehr verwundbar und wird ins "Gefängnis" geschickt.
     * Zusätzlich wird der Timer für die nächste Geisterfreilassung zurückgesetzt.
     *
     * @param ghost Der verwundbare Geist, der zurückgesetzt werden soll.
     */
    private void resetVulnerableGhost(Ghost ghost) {
        Point ghostStart = maze.findGhostStart(ghost.getLetter());
        ghost.setX(ghostStart.x);
        ghost.setY(ghostStart.y);
        ghost.setIsInJail(true);
        ghost.setVulnerable(false);
        nextGhostReleaseTime = System.currentTimeMillis() + GHOST_RELEASE_INTERVAL;
    }

    /**
     * Behandelt die Kollision zwischen Pac-Man und einem nicht-verwundbaren Geist.
     * In der Regel verliert Pac-Man dabei ein Leben. Das Spiel wird daraufhin zurückgesetzt,
     * und alle Bewegungsrichtungen werden auf MOVE_NONE gesetzt.
     */
    private void handleCollisionWithInvulnerableGhost() {
        loseLife();
        resetPacmanAndGhosts();
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
    }


    /**
     * Aktiviert den Effekt einer Power-Pille.
     * Alle Geister im Spiel werden für eine festgelegte Zeitdauer verwundbar.
     * Während dieser Zeit können die Geister von Pac-Man gefangen werden.
     * Setzt auch die Dauer der Verwundbarkeit der Geister auf einen festgelegten Wert.
     */
    private void activatePowerPillEffect() {
        if (ghosts != null && !ghosts.isEmpty()) {
            for (Ghost ghost : ghosts) {
                ghost.setVulnerable(true);
                ghost.setBlinking(false);
            }
            vulnerabilityDuration = VULNERABILITY_TIME;
        } else {
            System.err.println("Warnung: Keine Geister vorhanden, um den Power-Pill-Effekt zu aktivieren.");
        }
    }


    /**
     * Setzt Pac-Man und alle Geister auf ihre jeweiligen Startpositionen zurück.
     * Diese Methode wird aufgerufen, wenn eine Kollision zwischen Pac-Man und einem nicht-verwundbaren Geist auftritt.
     * Sie setzt die Position von Pac-Man zurück, initialisiert seine Bewegungsrichtung neu und
     * setzt jeden Geist auf seine Anfangsposition, macht ihn nicht mehr verwundbar und setzt ihn ins "Gefängnis".
     */
    private void resetPacmanAndGhosts() {
        // Setze Pac-Man zurück
        Point pacmanStart = maze.findPacmanStart();
        pacman.setX(pacmanStart.x);
        pacman.setY(pacmanStart.y);
        setLastDirection(ACTION.MOVE_NONE);
        setNextDirection(ACTION.MOVE_NONE);

        // Setze jeden Geist zurück
        for (Ghost ghost : ghosts) {
            Point ghostStart = maze.findGhostStart(ghost.getLetter());
            ghost.setX(ghostStart.x);
            ghost.setY(ghostStart.y);
            ghost.setIsInJail(true);
            ghost.setVulnerable(false);
        }
    }

    private boolean isAtIntersection(Ghost ghost) {
        int availableDirections = 0;

        // Liste der gültigen Bewegungsrichtungen
        List<ACTION> movementDirections = Arrays.asList(ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT);

        // Prüfe jede Richtung, außer der entgegengesetzten Richtung des Geistes
        for (ACTION direction : movementDirections) {
            // Überspringe die entgegengesetzte Richtung
            if (direction == getOppositeDirection(ghost.getDirection())) {
                continue;
            }
            // Simuliere die Bewegung in dieser Richtung
            if (!isCollisionForGhost(ghost, direction)) {
                // Zähle die Richtungen, die keine Kollision verursachen
                availableDirections++;
            }
        }
        // Eine Kreuzung liegt vor, wenn es mehr als eine verfügbare Richtung gibt
        return availableDirections > 1;
    }

    /**
     * Wählt an einer Kreuzung eine neue Richtung für den Geist aus.
     * Zuerst wird überprüft, ob die Fortsetzung in der aktuellen Richtung möglich ist, ohne eine Kollision zu riskieren.
     * Es besteht eine 30%ige Chance, dass der Geist in seiner aktuellen Richtung weitergeht. Andernfalls
     * werden andere mögliche Richtungen, die keine Kollision verursachen, in Betracht gezogen und eine davon zufällig gewählt.
     *
     * @param ghost Der Geist, für den die Richtung bestimmt wird.
     * @return Die gewählte Richtung, in die der Geist gehen soll.
     */
    private ACTION chooseDirectionAtIntersection(Ghost ghost) {
        List<ACTION> possibleDirections = new ArrayList<>();
        List<ACTION> movementDirections = Arrays.asList(
                ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT);

        // Füge die aktuelle Richtung als Option hinzu, wenn keine Kollision vorliegt
        if (!isCollisionForGhost(ghost, ghost.getDirection())) {
            possibleDirections.add(ghost.getDirection());
        }

        for (ACTION direction : movementDirections) {
            if (direction != getOppositeDirection(ghost.getDirection()) &&
                    !isCollisionForGhost(ghost, direction)) {
                possibleDirections.add(direction);
            }
        }

        if (!possibleDirections.isEmpty()) {
            // Entscheiden, ob der Geist in der aktuellen Richtung weitergeht oder eine neue Richtung wählt
            return random.nextInt(100) < 30 && possibleDirections.contains(ghost.getDirection()) ?
                    ghost.getDirection() :
                    possibleDirections.get(random.nextInt(possibleDirections.size()));
        }

        return ghost.getDirection();
    }

    /**
     * Entscheidet zufällig, ob ein Geist sich umdrehen soll.
     * Die Entscheidung basiert auf einem Zufallswert: Es gibt eine 0,4% Chance (4 in 1000)
     *
     * @return true, wenn der Geist sich umdrehen soll, sonst false.
     */
    private boolean shouldTurnAround() {
        return new Random().nextInt(1000) < 4;
    }


    /**
     * Bewegt einen Geist in die angegebene Richtung.
     * Diese Methode ändert die Position des Geistes basierend auf der übergebenen Richtung.
     *
     * @param ghost Der Geist, der bewegt werden soll.
     * @param direction Die Richtung, in die der Geist bewegt werden soll.
     */
    private void moveGhost(Ghost ghost, ACTION direction) {
        if (ghost == null || direction == null) {
            throw new IllegalArgumentException("Geist und Richtung dürfen nicht null sein.");
        }
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
        long lastUpdateTime = System.currentTimeMillis();

        while (mRunning) {
            long startTime = System.currentTimeMillis();
            long deltaTime = startTime - lastUpdateTime;

            if (state != GAMESTATE.PAUSED) {
                updateGame(deltaTime); // Extrahiert die Spiellogik in eine separate Methode
            }

            lastUpdateTime = startTime;
            long frameTime = System.currentTimeMillis() - startTime;
            long sleepTime = mTickrate - frameTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted");
                    break; // Verlassen Sie die Schleife im Falle einer Unterbrechung
                }
            }
        }
        mGUI_C_Ref.quit();
        // Code für Speichern etc.
    }

    private void updateGame(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        gamePanel.repaint();
        checkAndTeleportPacman();
        checkPacmanGhostCollision();
        checkForPowerPill();
        if (currentTime >= nextGhostReleaseTime) {
            releaseGhostFromJail();
        }

        if (vulnerabilityDuration > 0) {
            vulnerabilityDuration -= deltaTime;
            if (vulnerabilityDuration <= BLINK_THRESHOLD) {
                for (Ghost ghost : ghosts) {
                    ghost.setBlinking(true);
                }
            }
            if (vulnerabilityDuration <= 0) {
                for (Ghost ghost : ghosts) {
                    ghost.setVulnerable(false);
                    ghost.setBlinking(false);
                }
            }
        }

        for (Ghost ghost : ghosts) {
            if (ghost.isMovingToReleasePoint()) {
                ghost.moveToReleasePoint(maze.findReleasePoint());
            }
        }
    }

    private void checkForPowerPill() {
        Point pacmanPosition = getPacmanGridPosition();
        char[][] grid = maze.getGrid();

        if (grid[pacmanPosition.y][pacmanPosition.x] == 'o') {
            grid[pacmanPosition.y][pacmanPosition.x] = ' '; // Power-Pille entfernen
            activatePowerPillEffect(); // Aktivieren Sie den Effekt der Power-Pille
            increaseScore(50); // Punktestand erhöhen
            collectedPpills++;
            //checkLevelCompletion();

        }
    }

    private void checkLevelCompletion() {
        //System.out.println(collectedDots+"/"+totalDots);
        if (getProgress() == 1.0f) {
            onLevelComplete();
        }
    }
    // Method to calculate and return the progress as a float
    public float getProgress() {
        int totalItems = totalDots + totalPpillscoins;
        int collectedItems = collectedDots + collectedPpills;

        if (totalItems == 0) return 0; // Avoid division by zero
        return (float) collectedItems / totalItems;
    }

    private void onLevelComplete() {
        // nächstes level!
        // wenn 3. Level geschafft, dann ende = win
        int nextLevel = (maze.getCurrentLevel())+1;
        maze.changeLevel(nextLevel);
        gamePanel.showLevelOverlay(nextLevel+1);
        totalDots = maze.getTotalDots();
        totalPpillscoins = maze.getTotalPpills();
        resetPacmanAndGhosts();
        collectedDots=0;
        collectedPpills=0;
        updateTickratesForLevel(nextLevel);

    }

    private void updateTickratesForLevel(int nextLevel) {
        mTickrateMovement = 1000 / (18+nextLevel);
        mTickrateMovementGhosts = 1000 / (16+nextLevel*2);
    }

    private void releaseGhostFromJail() {
        for (Ghost ghost : ghosts) {
            if (ghost.isInJail() && !ghost.isMovingToReleasePoint()) {
                ghost.setIsMovingToReleasePoint(true);
                break; // Nur einen Geist zur Zeit freilassen
            }
        }
        nextGhostReleaseTime = System.currentTimeMillis() + GHOST_RELEASE_INTERVAL;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public boolean isAnyGhostMovingToReleasePoint() {
        for (Ghost ghost : ghosts) {
            if (ghost.isMovingToReleasePoint()) {
                return true;
            }
        }
        return false;
    }

    public ACTION getLastDirection() {
        return lastDirection;
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
        updateTickratesForLevel(1);
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        lives=3;
        score=0;
        nextGhostReleaseTime = System.currentTimeMillis() + INITIAL_RELEASE_DELAY;

        totalDots = maze.getTotalDots();
        totalPpillscoins = maze.getTotalPpills();
        collectedDots = 0;
        collectedPpills = 0;

        Point PacManStart = maze.findPacmanStart();
        if (PacManStart != null) {
            this.pacman = new Pacman(PacManStart.x, PacManStart.y);
        }

        // Initialisieren der Geisterliste
        Point shadowStart = maze.findGhostStart('B'); // BLINKY
        Point bashfulStart = maze.findGhostStart('I'); // INKY
        Point speedyStart = maze.findGhostStart('S'); // Speedy, PINKY
        Point pokeyStart = maze.findGhostStart('C'); // CLYDE
        this.ghosts = new ArrayList<>();
        // Konstanten für die Startpositionen und Geschwindigkeiten der Geister
        // Geschwindigkeit der Geister
        int GHOST_SPEED = 0;
        this.ghosts.add(new Ghost(shadowStart.x, shadowStart.y, GhostType.SHADOW, GHOST_SPEED, 'B'));
        this.ghosts.add(new Ghost(speedyStart.x, speedyStart.y, GhostType.SPEEDY, GHOST_SPEED, 'S'));
        this.ghosts.add(new Ghost(pokeyStart.x, pokeyStart.y, GhostType.POKEY, GHOST_SPEED, 'C'));
        this.ghosts.add(new Ghost(bashfulStart.x, bashfulStart.y, GhostType.BASHFUL, GHOST_SPEED, 'I'));

    }



    void resetGame() {
        // Spiel zurücksetzen
        score = 0;
        lives = 3;
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        nextGhostReleaseTime = System.currentTimeMillis() + INITIAL_RELEASE_DELAY;

        // Setzen Sie das Labyrinth auf Level 0 und zurücksetzen Sie es
        maze.changeLevel(0); // Ändern Sie das Labyrinth auf Level 0
        maze.resetMaze(); // Setzen Sie das Labyrinth zurück, um alle Münzen wiederherzustellen

        // Setzen Sie Pac-Man und die Geister auf ihre Startpositionen zurück
        resetPacmanAndGhosts();
        gamePanel.showLevelOverlay(0);
    }

    private void handleGameOver() {
        // Game Over Logik
        // Zurücksetzen des Spiels oder andere erforderliche Aktionen
        resetGame();
        // Wechseln zum Hauptmenü
        changeState(GAMESTATE.GAMEOVER);
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
                gamePanel.showLevelOverlay(1);
            }
            case QUIT -> mRunning = false;
            case PAUSE_TOGGLE -> {
                if (state == GAMESTATE.PAUSED){
                    changeState(GAMESTATE.RUNNING);
                    nextGhostReleaseTime += (System.currentTimeMillis() - pauseStartTime); // Zeit während der Pause hinzufügen
                }else if (state == GAMESTATE.RUNNING){
                    changeState(GAMESTATE.PAUSED);
                    pauseStartTime = System.currentTimeMillis(); // Startzeit der Pause speichern
                }
            }
            case MENU -> {
                changeState(GAMESTATE.MENU);
                resetGame();
            }
            case LOST -> {
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
                //loseLife();
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

        collectCoin();
    }

    private void collectCoin() {
        Point pacmanGridPosition = getPacmanGridPosition();
        char[][] grid = maze.getGrid();
        if (grid[pacmanGridPosition.y][pacmanGridPosition.x] == '.') {
            grid[pacmanGridPosition.y][pacmanGridPosition.x] = ' '; // Münze entfernen
            increaseScore(20); // Punktestand erhöhen

            collectedDots++; // Increment collected dots
            checkLevelCompletion();
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
    public void setNextDirection(ACTION direction) {
        this.nextDirection = direction;
    }

    // Methoden zum Aktualisieren von Score und Lives
    public void increaseScore(int amount) {
        score += amount;
    }

    public void loseLife() {
        lives--;
        if (lives <= 0) {
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
