package org.pacman;

import java.awt.*;

public class Ghost {
    private int x, y;
    private int speed;
    private Game_Controller.GhostType type;
    private char letter;
    private boolean vulnerable;
    private boolean isInJail;
    private boolean isMovingToReleasePoint;
    private boolean blinking;
    private static final int STEP_SIZE = 1; // Größe eines Schrittes
    private Game_Controller.ACTION direction = Game_Controller.ACTION.MOVE_UP;

    public Ghost(int startX, int startY, Game_Controller.GhostType type, int speed, char letter) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.speed = speed;
        this.letter = letter;
        this.vulnerable = false;
        this.isInJail = true; // Alle Geister starten im Jail
        this.blinking = false;
    }

    public void move() {
        // Bewegung basierend auf der aktuellen Richtung
        switch (this.direction) {
            case MOVE_UP:
                this.y -= this.speed;
                break;
            case MOVE_DOWN:
                this.y += this.speed;
                break;
            case MOVE_LEFT:
                this.x -= this.speed;
                break;
            case MOVE_RIGHT:
                this.x += this.speed;
                break;
        }
    }

    // Getter und Setter für Position, Richtung usw.
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Game_Controller.ACTION getDirection() { return direction; }
    public void setDirection(Game_Controller.ACTION direction) { this.direction = direction; }
    public Game_Controller.GhostType getType() { return type; }

    public void moveUp() {
        y -= speed;
    }

    public void moveDown() {
        y += speed;

    }

    public void moveLeft() {
        x -= speed;

    }

    public void moveRight() {
        x += speed;

    }

    public int getSpeed() {return speed;}

    public void setSpeed(int newSpeed) {speed = newSpeed;}

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public void setVulnerable(boolean b) {
        this.vulnerable = b;
    }
    public boolean getVulnerable() {
        return vulnerable;
    }

    public boolean isInJail() {
        return isInJail;
    }

    public void setIsInJail(boolean isInJail) {
        this.isInJail = isInJail;
    }

    public void moveToReleasePoint(Point releasePoint) {
        // Logik, um den Geist Schritt für Schritt zum Freigabepunkt zu bewegen
        // Bewegung auf der X-Achse
        if (x < releasePoint.x) {
            x += STEP_SIZE;
        } else if (x > releasePoint.x) {
            x -= STEP_SIZE;
        }

        // Bewegung auf der Y-Achse
        if (x == releasePoint.x) {
            if (y < releasePoint.y) {
                y += STEP_SIZE;
            } else if (y > releasePoint.y) {
                y -= STEP_SIZE;
            }
        }

        if (erreichtReleasePoint(releasePoint)) {
            isMovingToReleasePoint = false;
            isInJail = false; // Der Geist ist jetzt nicht mehr im Gefängnis
        }
    }

    private boolean erreichtReleasePoint(Point releasePoint) {
        // Überprüfen, ob der Geist den releasePoint erreicht hat
        return x == releasePoint.x && y == releasePoint.y;
    }

    public void setIsMovingToReleasePoint(boolean b) {
        isMovingToReleasePoint = b;
    }

    public boolean isMovingToReleasePoint() {
        return isMovingToReleasePoint;
    }

    public void setBlinking(boolean blinking) {
        this.blinking = blinking;
    }

    public boolean isBlinking() {
        return blinking;
    }

    // Methode, um die Farbe des Geistes basierend auf seinem Typ zu bestimmen
    public Color getColor() {
        switch (this.getType()) {
            case SHADOW:
                return Color.RED;
            case SPEEDY:
                return Color.PINK;
            case POKEY:
                return Color.ORANGE;
            case BASHFUL:
                return Color.CYAN;
            default:
                return Color.GRAY;
        }
    }
}
