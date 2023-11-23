package org.pacman;

import javax.swing.*;
import java.awt.*;

public class Game_Panel extends JPanel {
    private JLabel label = new JLabel("Game (ESC: Pause Menü) 123");
    Game_Panel(){
        setDoubleBuffered(true);
        setBackground(new Color(70, 70, 70));
        add(label);
        label.setBounds(100,100,100,50);
        label.setVisible(true);
        label.setForeground(Color.orange);
        setFocusable(false);
    }
}
