package org.pacman;

public class Game_Controller implements Runnable{
    private Pacman pacman;
    private static GUI_Controller  mGUI_C_Ref;
    private static Game_Controller  mGame_C_Ref;
    private Game_Panel gamePanel;
    private Thread gameThread;
    private boolean mRunning = true;
    public static final int mTickrate = 1000 / 60; // Tickrate in 60 pro Sekunde

    /**
     * Gameloop für Spielelogik
     */
    @Override
    public void run() {
        long startTime, endTime, deltaTime;
        while(mRunning){
            startTime = System.currentTimeMillis();
            //Game Logic
            if (state != GAMESTATE.PAUSED){
                gamePanel.repaint();

            }
            //
            endTime = System.currentTimeMillis();
            deltaTime = endTime - startTime;
            if (mTickrate - deltaTime > 0){
                try {
                    Thread.sleep(mTickrate - deltaTime);
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted");
                    throw new RuntimeException(e);
                }
            }
        }
        mGUI_C_Ref.quit();
        /*
            Code für Speichern etc.
         */
    }

    // in welchen Zuständen kann das Programm sein
    public enum GAMESTATE{
        MENU,
        RUNNING,
        PAUSED,
        GAMEOVER
    }


    private GAMESTATE state; // aktueller Zustand

    //welche Aktionen können auftreten
    public enum ACTION{
        QUIT,
        START,
        PAUSE_TOGGLE,
        MENU,
        HIGH_SCORES,
        SETTINGS,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT
    }
    Game_Controller(){


        mGame_C_Ref = this;
        mGUI_C_Ref = new GUI_Controller(this);
        this.gamePanel = GUI_Controller.getGamePanel();
        pacman = new Pacman(300, 300);
        //starten des Gameloop
        gameThread = new Thread(this);
        gameThread.start();


        //wechseln zum Menü
        changeState(GAMESTATE.MENU);
    }

    /**
     * der aktuelle Zustand des Spieles wird geändert
     * ruft changeWindowConfig() auf
     * @param newState
     */
    public void changeState(GAMESTATE newState){
        state = newState;
        mGUI_C_Ref.changeWindowConfig(newState);
    }

    public static GUI_Controller getGUI_C_Ref() {
        return mGUI_C_Ref;
    }
    public static Game_Controller getGame_C_Ref(){
        return mGame_C_Ref;
    }

    /**
     * ein Event wird ausgelöst
     * @param a
     */
    public void fireEvent(ACTION a){
        switch (a){
            case START -> changeState(GAMESTATE.RUNNING);
            case QUIT -> mRunning = false;
            case PAUSE_TOGGLE -> {
                if (state == GAMESTATE.PAUSED){
                    changeState(GAMESTATE.RUNNING);
                }else if (state == GAMESTATE.RUNNING){
                    changeState(GAMESTATE.PAUSED);
                }
            }
            case MENU -> changeState(GAMESTATE.MENU);

            case MOVE_UP -> movePacmanUp();
            case MOVE_DOWN -> movePacmanDown();
            case MOVE_LEFT -> movePacmanLeft();
            case MOVE_RIGHT -> movePacmanRight();

        }
    }

    private void movePacmanUp() {
        // Logik für Bewegung nach oben
        pacman.moveUp();
    }

    private void movePacmanDown() {
        // Logik für Bewegung nach unten
        pacman.moveDown();
    }

    private void movePacmanLeft() {
        // Logik für Bewegung nach links
        pacman.moveLeft();
    }

    private void movePacmanRight() {
        // Logik für Bewegung nach rechts
        pacman.moveRight();

    }

    public Pacman getPacman() {
        return pacman;
    }
}
