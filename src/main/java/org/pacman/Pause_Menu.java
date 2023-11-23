package org.pacman;

import javax.swing.*;
import java.awt.*;

public class Pause_Menu extends JPanel {
    private JButton menu = new JButton();
    Pause_Menu(){
        setLayout(null);
        setBackground(new Color(255, 183, 0, 170));

        add(menu);
        menu.setText("Main Menu");
        menu.setBounds(200,100,150,50);
        menu.setVisible(true);
        menu.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.MENU));
    }
}
