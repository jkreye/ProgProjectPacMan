package org.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GUI_Controller implements Runnable {

    private JFrame window; // Das Fenster in dem alles passiert

    // einzelne Fenster/Komponenten des Spiels
    private Main_Menu mMainMenu;
    private Game_Panel mGame_Panel;
    private Pause_Menu mPauseMenu;

    private Game_Controller mGame_C_Ref; // Referenz zum Gamecontroller

    private boolean mGraphicsRunning = true; // läuft der Gameloop für repaint
    private long mFramerate = Game_Controller.mTickrate;


    GUI_Controller(Game_Controller gameController) {
        mGame_C_Ref = gameController;

        window = new JFrame("Mein tolles Spiel");
        //Beim Schließen des fensters wird die Anwendung beendet
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLayout(null);
        //window.setExtendedState(Frame.MAXIMIZED_BOTH); // Fenster füllt den Bildschirm
        // window.setBounds(100, 100, 1920, 1080); // Setzt Position und Größe des Fensters
        window.setBounds(100, 100, 1280, 720); // Setzt Position und Größe des Fensters

        // erlaube resize
        window.setResizable(true);

        //obere Fensterleiste ja/nein
        window.setUndecorated(false);

        //Das Fenster wird sichtbar
        window.setVisible(true);

        // Objekte der Pannels werden angelegt
        mMainMenu = new Main_Menu();
        mGame_Panel = new Game_Panel();
        mPauseMenu = new Pause_Menu();

        //Panels werden hinzugefügt und skaliert
        Container cp = window.getContentPane();
        cp.add(mMainMenu);
        mMainMenu.setBounds(0, 0, window.getWidth(), window.getHeight());
        cp.add(mGame_Panel);
        mGame_Panel.setBounds(0, 0, window.getWidth(), window.getHeight());
        cp.add(mPauseMenu,0);
        mPauseMenu.setBounds(0, 0, window.getWidth(), window.getHeight());

        //Tastatureingaben
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE -> mGame_C_Ref.fireEvent(Game_Controller.ACTION.PAUSE_TOGGLE);
                    default -> {
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
            }
        });
        window.setFocusable(true);
        window.setAutoRequestFocus(true);
    }

    /**
     * Veränderung des dargestellten Fensters z.B Hauptmenü -> Spiel
     * zum wechsel changeState() des Game_Controllers verwenden
     * @param state
     */
    public void changeWindowConfig(Game_Controller.GAMESTATE state) {
        if (state != Game_Controller.GAMESTATE.PAUSED) {
            for (Component element : window.getContentPane().getComponents()) {
                element.setVisible(false);
            }
        }

        switch (state) {
            case MENU -> mMainMenu.setVisible(true);
            case RUNNING -> mGame_Panel.setVisible(true);
            case PAUSED -> mPauseMenu.setVisible(true);
        }
        window.repaint();
    }

    /**
     * Gameloop für Grafik (noch nicht verwendet)
     */
    @Override
    public void run() {
        // GameLoop für Grafik
        long startTime, endTime, deltaTime;
        while(mGraphicsRunning){
            startTime = System.currentTimeMillis();
            window.repaint();
            endTime = System.currentTimeMillis();
            deltaTime = endTime - startTime;
            if (mFramerate - deltaTime > 0){
                try {
                    Thread.sleep(mFramerate - deltaTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * das Fenster wird geschlossen
     */
    public void quit() {
        mGraphicsRunning = false;
        window.dispose();
    }
}
