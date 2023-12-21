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
    private static final int OFFSET = 4; // Ein Viertel der Zellengröße als Versatz

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
        List<Ghost> ghostsCopy = new ArrayList<>(ghosts);
        if (pacman != null) {
            for (Ghost ghost : ghostsCopy) {
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


    /**
     * Überprüft, ob sich ein Geist an einer Kreuzung befindet.
     * Eine Kreuzung wird als ein Punkt definiert, an dem der Geist mehr als eine mögliche Bewegungsrichtung hat,
     * ohne mit einem Hindernis zu kollidieren. Die entgegengesetzte Richtung zur aktuellen Bewegungsrichtung des Geistes
     * wird dabei nicht berücksichtigt, da ein Geist sich normalerweise nicht umdrehen sollte.
     *
     * @param ghost Der Geist, dessen Position überprüft wird.
     * @return true, wenn sich der Geist an einer Kreuzung befindet, sonst false.
     */
    private boolean isAtIntersection(Ghost ghost) {
        if (ghost == null) {
            throw new IllegalArgumentException("Geist darf nicht null sein.");
        }

        int availableDirections = 0;
        List<ACTION> movementDirections = Arrays.asList(
                ACTION.MOVE_UP, ACTION.MOVE_DOWN, ACTION.MOVE_LEFT, ACTION.MOVE_RIGHT);

        for (ACTION direction : movementDirections) {
            if (direction != getOppositeDirection(ghost.getDirection()) &&
                    !isCollisionForGhost(ghost, direction)) {
                availableDirections++;
                if (availableDirections > 1) {
                    return true; // Frühes Beenden, sobald mehr als eine Richtung verfügbar ist
                }
            }
        }

        return false; // Keine Kreuzung, wenn weniger als zwei Richtungen verfügbar sind
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

    /**
     * Prüft, ob ein Geist bei einer Bewegung in eine bestimmte Richtung mit einem Hindernis kollidieren würde.
     * Berechnet die zukünftige Position des Geistes basierend auf seiner aktuellen Geschwindigkeit und Richtung.
     * Überprüft, ob an dieser Position ein Hindernis oder ein Teleport ist.
     *
     * @param ghost Der Geist, dessen Kollision überprüft wird.
     * @param direction Die Richtung, in die der Geist sich bewegen soll.
     * @return true, wenn eine Kollision erkannt wird, sonst false.
     */
    private boolean isCollisionForGhost(Ghost ghost, ACTION direction) {
        if (ghost == null || direction == null) {
            throw new IllegalArgumentException("Geist und Richtung dürfen nicht null sein.");
        }

        int cellSize = maze.getCellSize();
        int speed = ghost.getSpeed();

        Point futurePosition = calculateFuturePosition(ghost.getX(), ghost.getY(), speed, direction);

        // Berechnen der vier Ecken um den Geist
        Point[] corners = new Point[] {
                new Point(futurePosition.x + OFFSET, futurePosition.y + OFFSET), // Oben links
                new Point(futurePosition.x + cellSize - OFFSET, futurePosition.y + OFFSET), // Oben rechts
                new Point(futurePosition.x + OFFSET, futurePosition.y + cellSize - OFFSET), // Unten links
                new Point(futurePosition.x + cellSize - OFFSET, futurePosition.y + cellSize - OFFSET) // Unten rechts
        };

        for (Point corner : corners) {
            int gridX = corner.x / cellSize;
            int gridY = corner.y / cellSize;

            if (maze.isWallOrTeleportForGhost(gridX, gridY)) {
                return true; // Kollision erkannt
            }
        }

        return false; // Keine Kollision
    }

    /**
     * Berechnet die zukünftige Position basierend auf der aktuellen Position, Geschwindigkeit und Bewegungsrichtung.
     *
     * @param x Die aktuelle X-Position.
     * @param y Die aktuelle Y-Position.
     * @param speed Die Geschwindigkeit des Geistes.
     * @param direction Die Bewegungsrichtung.
     * @return Die berechnete zukünftige Position.
     */
    private Point calculateFuturePosition(int x, int y, int speed, ACTION direction) {
        switch (direction) {
            case MOVE_UP:
                return new Point(x, y - speed);
            case MOVE_DOWN:
                return new Point(x, y + speed);
            case MOVE_LEFT:
                return new Point(x - speed, y);
            case MOVE_RIGHT:
                return new Point(x + speed, y);
            default:
                return new Point(x, y);
        }
    }


    /**
     * Haupt-Game-Loop des Spiels.
     * Diese Methode wird kontinuierlich ausgeführt, solange das Spiel läuft (mRunning ist true).
     * In jeder Iteration der Schleife wird überprüft, ob das Spiel pausiert ist.
     * Wenn nicht, wird die Spiellogik aktualisiert und die Zeit für den nächsten Frame berechnet.
     * Die Methode sorgt dafür, dass das Spiel mit einer konstanten Tickrate läuft.
     */
    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();

        while (mRunning) {
            long startTime = System.currentTimeMillis();
            long deltaTime = startTime - lastUpdateTime;

            if (state != GAMESTATE.PAUSED) {
                updateGame(deltaTime); // Spiellogik
            }

            lastUpdateTime = startTime;
            long frameTime = System.currentTimeMillis() - startTime;
            long sleepTime = mTickrate - frameTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Setzt den unterbrochenen Status des Threads
                    System.err.println("Game thread interrupted: " + e.getMessage()); // Logging statt System.err
                    break;
                }
            }
        }

        // Saubere Beendigung des Spiels
        mGUI_C_Ref.quit();
    }

    /**
     * Aktualisiert die Spiellogik für jeden Frame.
     * Diese Methode wird regelmäßig im Spiel-Loop aufgerufen und führt verschiedene Aktionen aus,
     * einschließlich Repaint des Spielfelds, Überprüfen von Kollisionen, Handhaben von Power-Pills und Teleportationen,
     * sowie Aktualisieren des Zustands der Geister.
     *
     * @param deltaTime Die Zeit seit dem letzten Update in Millisekunden.
     */
    private void updateGame(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        gamePanel.repaint();
        checkAndTeleportPacman();
        checkPacmanGhostCollision();
        checkForPowerPill();

        if (currentTime >= nextGhostReleaseTime) {
            releaseGhostFromJail();
        }

        if (ghosts != null) {
            updateGhostStates(deltaTime);
        }
    }

    private void updateGhostStates(long deltaTime) {
        if (vulnerabilityDuration > 0) {
            updateVulnerabilityState(deltaTime);
        }

        for (Ghost ghost : ghosts) {
            if (ghost.isMovingToReleasePoint()) {
                ghost.moveToReleasePoint(maze.findReleasePoint());
            }
        }
    }

    private void updateVulnerabilityState(long deltaTime) {
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

    /**
     * Überprüft, ob Pac-Man eine Power-Pille aufgenommen hat.
     * Wenn Pac-Man eine Power-Pille aufnimmt, wird der Effekt der Power-Pille aktiviert,
     * der Punktestand erhöht und die Anzahl der gesammelten Power-Pillen aktualisiert.
     */
    private void checkForPowerPill() {
        Point pacmanPosition = getPacmanGridPosition();
        char[][] grid = maze.getGrid();

        if (grid[pacmanPosition.y][pacmanPosition.x] == 'o') {
            grid[pacmanPosition.y][pacmanPosition.x] = ' '; // Power-Pille entfernen
            activatePowerPillEffect(); // Effekt der Power-Pille aktivieren
            increaseScore(50); // Punktestand erhöhen
            collectedPpills++;
            checkLevelCompletion(); // Überprüfen, ob das Level abgeschlossen ist
        }
    }

    /**
     * Überprüft, ob das aktuelle Level abgeschlossen ist.
     * Der Fortschritt wird anhand der Anzahl der gesammelten Punkte und der Gesamtzahl der Punkte im Level berechnet.
     * Wenn der Fortschritt 100% erreicht, wird die Methode onLevelComplete aufgerufen, um zum nächsten Level überzugehen oder das Spiel zu beenden.
     */
    private void checkLevelCompletion() {
        if (Math.abs(getProgress() - 1.0f) < 0.001f) {
            onLevelComplete();
        }
    }

    /**
     * Berechnet den Fortschritt im aktuellen Level des Spiels.
     * Der Fortschritt wird als Verhältnis der gesammelten Gegenstände (Dots und Power-Pills)
     * zur Gesamtzahl der Gegenstände im Level berechnet.
     *
     * @return Den Fortschritt als Gleitkommazahl zwischen 0 und 1,
     *         wobei 1 den vollständigen Abschluss des Levels bedeutet.
     */
    public float getProgress() {
        int totalItems = totalDots + totalPpillscoins;
        int collectedItems = collectedDots + collectedPpills;

        if (totalItems == 0) return 0; // Vermeidung einer Division durch Null
        return (float) collectedItems / totalItems;
    }

    /**
     * Wird aufgerufen, wenn ein Level im Spiel abgeschlossen ist.
     * Diese Methode setzt das Spiel für das nächste Level zurück,
     * einschließlich der Aktualisierung des Labyrinths, der Anzeige des Level-Overlays,
     * der Rücksetzung von Pac-Man und den Geistern sowie der Aktualisierung der Tickrate.
     * Wenn das dritte Level abgeschlossen ist, wird das Spiel als gewonnen markiert.
     */
    private void onLevelComplete() {
        // next level!
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

    /**
     * Aktualisiert die Tickraten für die Bewegungen von Pac-Man und den Geistern für das angegebene Level.
     * Die Tickraten beginnen bei einer Basisrate und erhöhen sich mit jedem Level, um die Schwierigkeit zu steigern.
     *
     * @param nextLevel Das nächste Level, für das die Tickraten aktualisiert werden sollen.
     */
    private void updateTickratesForLevel(int nextLevel) {
        final int baseTickRatePacman = 19;
        final int baseTickRateGhosts = 17;
        final int levelFactor = 1; // Erhöhung der Geschwindigkeit pro Level

        mTickrateMovement = 1000 / (baseTickRatePacman + levelFactor * (nextLevel - 1));
        mTickrateMovementGhosts = 1000 / (baseTickRateGhosts + levelFactor * (nextLevel - 1));
    }


    /**
     * Lässt einen Geist aus dem Gefängnis frei, falls vorhanden.
     * Geht die Liste der Geister durch und setzt den ersten Geist, der sich im Gefängnis befindet und noch nicht
     * auf dem Weg zum Freilassungspunkt ist, auf den Weg zur Freilassung.
     * Nachdem ein Geist freigelassen wurde, wird der Zeitpunkt für die nächste Geisterfreilassung aktualisiert.
     */
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

    /**
     * Überprüft, ob sich mindestens ein Geist auf dem Weg zum Freilassungspunkt befindet.
     * Durchläuft die Liste der Geister und prüft, ob irgendeiner von ihnen sich gerade zum Freilassungspunkt bewegt.
     *
     * @return true, wenn mindestens ein Geist auf dem Weg zum Freilassungspunkt ist, sonst false.
     */
    public boolean isAnyGhostMovingToReleasePoint() {
        if (ghosts == null) {
            return false;
        }
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

    public Game_Controller() {
        // Initialisieren des Labyrinths
        this.maze = new Maze();
        initializeGame();

        // Initialisieren der GUI und Zuweisung der Referenzen
        mGame_C_Ref = this;
        mGUI_C_Ref = new GUI_Controller(this);
        this.gamePanel = GUI_Controller.getGamePanel();

        // Starten des Hauptspiel-Loops
        gameThread = new Thread(this);
        gameThread.start();

        // Initialisieren und Starten der Bewegungs-Threads
        movementThread = new Thread(this::continuousMovement);
        movementThread.start();
        movementGhostsThread = new Thread(this::continuousMovementGhosts);
        movementGhostsThread.start();

        // Wechseln zum Menü
        changeState(GAMESTATE.MENU);
    }

    /**
     * Initialisiert Pac-Man mit Startwerten.
     * Setzt die Startposition, Geschwindigkeit und andere relevante Eigenschaften von Pac-Man.
     */
    private void initializePacman() {
        Point pacmanStart = maze.findPacmanStart();
        if (pacmanStart != null) {
            this.pacman = new Pacman(pacmanStart.x, pacmanStart.y);
        } else {
            // Fehlerbehandlung, falls keine Startposition gefunden wurde.
            System.err.println("Startposition für Pac-Man konnte nicht gefunden werden.");
        }
    }



    /**
     * Initialisiert das Spiel, indem es alle relevanten Spielkomponenten und -variablen zurücksetzt.
     * Setzt die Tickraten, Startpositionen von Pac-Man und den Geistern, die Spielvariablen wie Punktestand und Leben.
     */
    public void initializeGame() {
        updateTickratesForLevel(1);
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        lives = 3;
        score = 0;
        nextGhostReleaseTime = System.currentTimeMillis() + INITIAL_RELEASE_DELAY;

        totalDots = maze.getTotalDots();
        totalPpillscoins = maze.getTotalPpills();
        collectedDots = 0;
        collectedPpills = 0;

        initializePacmanAndGhosts();
    }

    private void initializePacmanAndGhosts() {
        initializePacman();
        initializeGhosts();
    }

    private void initializeGhosts() {
        this.ghosts = new ArrayList<>();
        int ghostSpeed = 0; // Konstante für die Geschwindigkeit der Geister

        // Initialisieren der Geister an ihren Startpositionen
        addGhostToGame('B', GhostType.SHADOW, ghostSpeed);
        addGhostToGame('I', GhostType.BASHFUL, ghostSpeed);
        addGhostToGame('S', GhostType.SPEEDY, ghostSpeed);
        addGhostToGame('C', GhostType.POKEY, ghostSpeed);
    }

    private void addGhostToGame(char ghostChar, GhostType type, int speed) {
        Point ghostStart = maze.findGhostStart(ghostChar);
        if (ghostStart != null) {
            this.ghosts.add(new Ghost(ghostStart.x, ghostStart.y, type, speed, ghostChar));
        } else {
            System.err.println("Startposition für Geist " + ghostChar + " nicht gefunden.");
        }
    }



    /**
     * Setzt das Spiel auf den Anfangszustand zurück.
     * Der Punktestand, die Anzahl der Leben, die Richtungen und der Zeitpunkt der nächsten Geisterfreilassung werden zurückgesetzt.
     * Das Labyrinth wird auf das Anfangslevel gesetzt und zurückgesetzt, und Pac-Man sowie die Geister werden auf ihre Startpositionen zurückgesetzt.
     */
    void resetGame() {
        score = 0;
        lives = 3;
        lastDirection = ACTION.MOVE_NONE;
        nextDirection = ACTION.MOVE_NONE;
        nextGhostReleaseTime = System.currentTimeMillis() + INITIAL_RELEASE_DELAY;

        // Labyrinth auf Level 0 setzen und zurücksetzen
        maze.changeLevel(0);
        maze.resetMaze();

        // Pac-Man und Geister zurücksetzen
        resetPacmanAndGhosts();
        gamePanel.showLevelOverlay(0); // Zeigt das Level-Overlay für das Anfangslevel an
    }

    /**
     * Behandelt das Ereignis des Spielendes (Game Over).
     * Setzt das Spiel zurück und wechselt zum Game-Over-Zustand oder zum Hauptmenü.
     */
    private void handleGameOver() {
        resetGame(); // Setzt das Spiel auf den Anfangszustand zurück

        // Wechseln zum Game-Over-Bildschirm oder Hauptmenü
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
                initializePacmanAndGhosts();
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

    /**
     * Überprüft, ob Pac-Man auf einem Teleportationsfeld steht und teleportiert ihn bei Bedarf.
     * Teleportiert Pac-Man zum gegenüberliegenden Rand des Labyrinths, wenn er sich auf einem Teleportationsfeld befindet.
     */
    public void checkAndTeleportPacman() {
        if (this.pacman != null) {
            Point pacmanPosition = getPacmanGridPosition();
            char[][] grid = maze.getGrid();
            int cellSize = maze.getCellSize();

            // Prüfen, ob die Position von Pac-Man innerhalb der Grenzen des Grids liegt
            if (pacmanPosition.y >= 0 && pacmanPosition.y < grid.length &&
                    pacmanPosition.x >= 0 && pacmanPosition.x < grid[pacmanPosition.y].length) {
                char cell = grid[pacmanPosition.y][pacmanPosition.x];

                if (cell == 'T') {
                    gamePanel.triggerPortalBlink(); // Auslösen des Blinkens am Portal
                    teleportPacman(pacmanPosition, grid, cellSize);
                }
            }
        }
    }

    /**
     * Teleportiert Pac-Man zum gegenüberliegenden Rand des Labyrinths.
     * @param pacmanPosition Die aktuelle Position von Pac-Man.
     * @param grid Das Labyrinth-Grid.
     * @param cellSize Die Größe einer Zelle im Labyrinth.
     */
    private void teleportPacman(Point pacmanPosition, char[][] grid, int cellSize) {
        if (pacmanPosition.x == 0) { // Linker Rand
            pacman.setX((grid[0].length - 2) * cellSize); // Teleportieren zum rechten Rand
        } else if (pacmanPosition.x == grid[0].length - 1) { // Rechter Rand
            pacman.setX(cellSize); // Teleportieren zum linken Rand
        }
    }

    /**
     * Überprüft, ob eine Kollision zwischen Pac-Man und einer Wand bei einer bestimmten Bewegungsrichtung auftritt.
     * Berechnet die zukünftige Position von Pac-Man basierend auf seiner aktuellen Geschwindigkeit und Richtung,
     * und prüft, ob an dieser Position eine Wand im Labyrinth ist.
     *
     * @param action Die geplante Bewegungsrichtung von Pac-Man.
     * @return true, wenn eine Kollision auftritt, sonst false.
     */
    private boolean isCollision(ACTION action) {
        int cellSize = maze.getCellSize();
        int speed = pacman.getSpeed();
        int offset = cellSize / 4; // Ein Viertel der Zellengröße als Versatz

        // Bestimmen der zukünftigen Position basierend auf der Aktion
        Point futurePosition = getFuturePosition(action, pacman.getX(), pacman.getY(), speed);

        // Berechnen der vier Ecken um Pac-Man
        Point[] corners = getCorners(futurePosition, cellSize, offset);

        // Überprüfen jeder Ecke auf Kollision
        return checkCornersForCollision(corners, cellSize);
    }

    private Point getFuturePosition(ACTION action, int x, int y, int speed) {
        switch (action) {
            case MOVE_UP:    return new Point(x, y - speed);
            case MOVE_DOWN:  return new Point(x, y + speed);
            case MOVE_LEFT:  return new Point(x - speed, y);
            case MOVE_RIGHT: return new Point(x + speed, y);
            default:         return new Point(x, y);
        }
    }

    private Point[] getCorners(Point position, int cellSize, int offset) {
        int x = position.x;
        int y = position.y;
        return new Point[]{
                new Point(x + offset, y + offset), // Oben links
                new Point(x + cellSize - offset, y + offset), // Oben rechts
                new Point(x + offset, y + cellSize - offset), // Unten links
                new Point(x + cellSize - offset, y + cellSize - offset) // Unten rechts
        };
    }

    private boolean checkCornersForCollision(Point[] corners, int cellSize) {
        for (Point corner : corners) {
            int gridX = corner.x / cellSize;
            int gridY = corner.y / cellSize;

            if (maze.isWall(gridX, gridY)) {
                return true; // Kollision erkannt
            }
        }
        return false; // Keine Kollision
    }


    /**
     * Berechnet die Position von Pac-Man im Labyrinth-Gitter basierend auf seiner aktuellen Pixelposition.
     * Diese Methode wandelt die Pixelkoordinaten von Pac-Man in Gitterkoordinaten um, indem sie die Pixelposition
     * durch die Größe der Zellen im Labyrinth teilt.
     *
     * @return Ein Point-Objekt, das die Gitterposition von Pac-Man repräsentiert.
     */
    public Point getPacmanGridPosition() {
        int cellSize = maze.getCellSize();

        // Stellen Sie sicher, dass cellSize nicht Null ist, um eine Division durch Null zu vermeiden
        if (cellSize == 0) {
            throw new IllegalStateException("Zellengröße des Labyrinths ist 0. Kann Gitterposition nicht berechnen.");
        }

        int adjustedX = pacman.getX() / cellSize;
        int adjustedY = pacman.getY() / cellSize;

        return new Point(adjustedX, adjustedY);
    }


    /**
     * Bewegt Pac-Man in die angegebene Richtung und sammelt Münzen, falls vorhanden.
     * Diese Methode aktualisiert die Position von Pac-Man basierend auf der angegebenen Aktion.
     * Anschließend wird überprüft, ob Pac-Man eine Münze sammelt.
     *
     * @param action Die Bewegungsrichtung, in die Pac-Man bewegt werden soll.
     */
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

    /**
     * Überprüft, ob Pac-Man eine Münze an seiner aktuellen Position eingesammelt hat und aktualisiert den Spielstand.
     * Entfernt die Münze aus dem Labyrinth und erhöht den Punktestand sowie die Anzahl der gesammelten Münzen.
     * Überprüft zudem, ob das aktuelle Level abgeschlossen ist.
     */
    private void collectCoin() {
        Point pacmanGridPosition = getPacmanGridPosition();
        char[][] grid = maze.getGrid();

        // Überprüfen, ob sich an der Position von Pac-Man eine Münze befindet
        if (pacmanGridPosition.y >= 0 && pacmanGridPosition.y < grid.length &&
                pacmanGridPosition.x >= 0 && pacmanGridPosition.x < grid[pacmanGridPosition.y].length &&
                grid[pacmanGridPosition.y][pacmanGridPosition.x] == '.') {

            grid[pacmanGridPosition.y][pacmanGridPosition.x] = ' '; // Münze entfernen
            increaseScore(20); // Punktestand um 20 Punkte erhöhen
            collectedDots++; // Anzahl der gesammelten Münzen erhöhen
            checkLevelCompletion(); // Überprüfen, ob das Level abgeschlossen ist
        }
    }


    /**
     * Bewegt Pac-Man nach oben, wenn keine Kollision auftritt.
     */
    private void movePacmanUp() {
        if (!isCollision(ACTION.MOVE_UP)) {
            pacman.moveUp();
        }
    }

    /**
     * Bewegt Pac-Man nach unten, wenn keine Kollision auftritt.
     */
    private void movePacmanDown() {
        if (!isCollision(ACTION.MOVE_DOWN)) {
            pacman.moveDown();
        }
    }

    /**
     * Bewegt Pac-Man nach links, wenn keine Kollision auftritt.
     */
    private void movePacmanLeft() {
        if (!isCollision(ACTION.MOVE_LEFT)) {
            pacman.moveLeft();
        }
    }

    /**
     * Bewegt Pac-Man nach rechts, wenn keine Kollision auftritt.
     */
    private void movePacmanRight() {
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
