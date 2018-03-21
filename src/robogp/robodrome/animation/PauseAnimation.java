/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.animation;

/**
 *
 * @author claudia
 */
public class PauseAnimation extends Animation {
    public PauseAnimation(long millisec) {
        this.type = Animation.Type.PAUSE;
        this.which = -1;
        this.duration = millisec;
    }

    @Override
    public boolean hasFinished(long elapsed) {
        return (elapsed >= duration);
    }    
}
