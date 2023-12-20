package org.pacman;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Die Klasse FontLoader dient zum Laden von Schriftarten.
 * Sie ermöglicht das Laden von Schriftarten aus Dateien und stellt eine Methode bereit,
 * um die geladene Schriftart abzurufen.
 */
public class FontLoader {
    private static Font customFont;

    /**
     * Lädt eine Schriftart aus einer Datei.
     *
     * @param fontFileName Der Name der Schriftartdatei, die geladen werden soll.
     * @param fontSize Die Größe der Schriftart.
     * @return Eine Font-Instanz der geladenen Schriftart oder null, wenn das Laden fehlschlägt.
     */
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

    /**
     * Gibt die zuletzt geladene benutzerdefinierte Schriftart zurück.
     *
     * @return Die benutzerdefinierte Schriftart oder null, wenn keine Schriftart geladen wurde.
     */
    public static Font getCustomFont() {
        return customFont;
    }
}
