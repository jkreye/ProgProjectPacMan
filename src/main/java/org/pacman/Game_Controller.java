package org.pacman;

public class Game_Controller implements Runnable{
    private static GUI_Controller  mGUI_C_Ref;
    private static Game_Controller  mGame_C_Ref;
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

            }
            //
            endTime = System.currentTimeMillis();
            deltaTime = endTime - startTime;
            if (mTickrate - deltaTime > 0){
                try {
                    Thread.sleep(mTickrate - deltaTime);
                } catch (InterruptedException e) {
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
        MENU
    }
    Game_Controller(){
        mGame_C_Ref = this;
        mGUI_C_Ref = new GUI_Controller(this);
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
        }
    }
}
