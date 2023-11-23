package org.pacman;

import javax.swing.*;

public class Main_Menu extends JPanel {
    private JButton start = new JButton();
    private JButton quit = new JButton();
    Main_Menu(){
        setLayout(null);
        add(start);
        add(quit);
        setFocusable(false);


        start.setBounds(50,50,150,50);
        start.setText("Start");
        start.setVisible(true);
        start.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.START));

        quit.setBounds(50,110,150,50);
        quit.setText("Quit");
        quit.setVisible(true);
        quit.addActionListener(e -> Game_Controller.getGame_C_Ref().fireEvent(Game_Controller.ACTION.QUIT));
    }
}
