/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.animation;

/**
 * Una generica animazione del robodromo
 * @author claudia
 */
public abstract class Animation {

    /**
     * @return il numero identificativo del robot protagonista dell'animazione, -1 se l'animazione non riguarda 
     * alcun robot in particolare.
     */
    public int getWhich() {
        return which;
    }

    /**
     * @return il tempo di inizio dell'animazione (espresso in millisecondi)
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Imposta il tempo di inizio dell'animazione
     * @param startTime il tempo da impostare
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return la durata dell'animazione, se possibile stabilirla a priori
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return il tempo a cui l'animazione &egrave; stata aggiornata l'ultima volta
     */
    public long getLastTime() {
        return lastTime;
    }

    /**
     * @param lastTime imposta il tempo dell'ultimo aggiornamento
     */
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * Tipo enumerativo che identifica i tipi di animazioni disponibili
     */
    public static enum Type {
        ROBOT_MOVE, LASER_FIRE, ROBOT_HIT, ROBOT_FALL, TRANSITION, LASER_HIDE, PAUSE
    };
    
    /**
     * Costante che identifica la velocit&agrave; di riproduzione delle animazioni; tempo impiegato
     * dal robot per spostarsi di una casella.
     */
    public static int TIMEUNIT = 800;
    
    protected Animation.Type type;
    protected long startTime;
    private long lastTime;
    protected long duration;
    protected int which;

    /**
     * 
     * @return il tipo di questa animazione
     */
    public Animation.Type getType() {
        return type;
    }

    /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Robot Move"
     */
    public boolean isRobotMove() {
        return type == Animation.Type.ROBOT_MOVE;
    }

     /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Laser Fire"
     */
    public boolean isLaserFire() {
        return type == Animation.Type.LASER_FIRE;
    }

    /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Robot Hit"
     */    
    public boolean isRobotHit() {
        return type == Animation.Type.ROBOT_HIT;
    }

    /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Robot Fall"
     */    
    public boolean isRobotFall() {
        return type == Animation.Type.ROBOT_FALL;
    }

        /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Transition"
     */
    public boolean isTransition() {
        return type == Animation.Type.TRANSITION;
    }
    
    /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Laser Hide"
     */    
    public boolean isLaserHide() {
        return type == Animation.Type.LASER_HIDE;
    }
    
    /**
     * 
     * @return true, se questa &egrave; un'animazione di tipo "Pause"
     */    
    public boolean isPause() {
        return type == Animation.Type.PAUSE;
    }

    /**
     * Stabilisce se l'animazione &egrave; terminata.
     * @param elapsed il tempo passato dall'inizio dell'animazione
     * @return true se l'animazione &egrave; terminata.
     */
    public boolean hasFinished(long elapsed) {
        return (elapsed >= duration);
    }
    
    public String toString() {
        return type.toString();
    }
};
