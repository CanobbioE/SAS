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
public class LaserHideAnimation extends Animation {

    public LaserHideAnimation() {
        this.type = Animation.Type.LASER_HIDE;
        this.which = -1;
    }

    @Override
    public boolean hasFinished(long elapsed) {
        return true;
    }
}
