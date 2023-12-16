package org.pacman;
public class Ghost {
    private int x, y;
    private int speed;
    private Game_Controller.GhostType type;
    private Game_Controller.ACTION direction = Game_Controller.ACTION.MOVE_UP;

    public Ghost(int startX, int startY, Game_Controller.GhostType type, int speed) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.speed = speed;
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
}
