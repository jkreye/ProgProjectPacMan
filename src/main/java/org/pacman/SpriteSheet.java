package org.pacman;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
public class SpriteSheet {

    private BufferedImage spriteSheet;
    private int tileSize;

    public SpriteSheet(String path, int tileSize) {
        InputStream is = this.getClass().getResourceAsStream(path);
        try {
            spriteSheet = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.tileSize = tileSize;
    }

    public static SpriteSheet getCookieSprite() {
        return new SpriteSheet("/img/cookie.png", 351);
    }

    public BufferedImage getSprite(int index) {
        int x = index * tileSize % spriteSheet.getWidth();
        int y = (index * tileSize / spriteSheet.getWidth()) * tileSize;
        return spriteSheet.getSubimage(x, y, tileSize, tileSize);
    }

    public static SpriteSheet getGhostSprite(Game_Controller.GhostType ghosttype) {
        SpriteSheet sheet = switch (ghosttype) {
            case SHADOW -> // Blinky
                    new SpriteSheet("/img/blueGhost.png", 16);
            case SPEEDY -> // Pinky
                    new SpriteSheet("/img/greenGhost.png", 16);
            case POKEY -> // Clyde
                    new SpriteSheet("/img/redGhost.png", 16);
            case BASHFUL -> // Inky
                    new SpriteSheet("/img/yellowGhost.png", 16);
        };
        // Laden des Sprite-Blatts basierend auf dem Geistertyp
        return sheet;
    }

    public static SpriteSheet getGhostSpritevulnerable() {
        return new SpriteSheet("/img/vulnerableGhost.png", 16);
    }
    public static SpriteSheet getCoinSprite() {
        return new SpriteSheet("/img/Coin.png", 16);
    }
}