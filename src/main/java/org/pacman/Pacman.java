/* Pacman.java */

package org.pacman;

public class Pacman {
    private int x, y;
    private int dx, dy;
    private int speed = 0; // Geschwindigkeit von Pac-Man

    public Pacman(int startX, int startY) {
        x = startX;
        y = startY;
        dx = 0;
        dy = 0;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public void setDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
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
}
