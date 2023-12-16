/* Pacman.java */

package org.pacman;

public class Pacman {
    private int x, y;

    private int speed = 0; // Geschwindigkeit von Pac-Man

    public Pacman(int startX, int startY) {
        x = startX;
        y = startY;
    }


    // Methoden zum Abrufen der Position
    public int getX() { return x; }
    public int getY() { return y; }

    public int getSpeed() {return speed;}

    public void setSpeed(int newSpeed) {speed = newSpeed;}

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
    public void setY(int y) {
        this.y = y;
    }
    public void setX(int x) {
        this.x = x;
    }
}
