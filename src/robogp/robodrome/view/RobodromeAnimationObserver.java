/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.view;

/**
 * Un RobodromeAnimationObserver riceve notifiche sull'inizio e la fine di una 
 * animazione nella RobodromeView.
 * @author claudia
 */
public interface RobodromeAnimationObserver {
    /**
     * Invocata quando l'animazione ha inizio (ossia viene invocato il metodo play
     * sulla RobodromeView).
     */
    public void animationStarted();
    
    /** Invocata quando l'animazione termina (ossia quando la coda di animazioni
     * da eseguire rimane vuota).
     */
    public void animationFinished();
}
