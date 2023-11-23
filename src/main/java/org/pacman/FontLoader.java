package org.pacman;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {
    private static Font customFont;

    public static Font loadFont(String fontFileName, float fontSize) {
        try {

            InputStream fontStream = FontLoader.class.getResourceAsStream("/" + fontFileName);

            if (fontStream == null) {
                System.err.println("Font file not found: " + fontFileName);
                return null;
            }

            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            return customFont.deriveFont(fontSize);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            return null; // Handle the exception appropriately
        }
    }

    public static Font getCustomFont() {
        return customFont;
    }
}
