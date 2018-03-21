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
public class RobotFallAnimation extends Animation {
  public static float DEGREES = 1080;
  private int startSize;
  
  private RobotFallAnimation() {
    this.type = Animation.Type.ROBOT_FALL;
  }
  
  public RobotFallAnimation(int robotNum) {
      this();
      which = robotNum;
      duration = Animation.TIMEUNIT;
  }

    /**
     * @return the startSize
     */
    public int getStartSize() {
        return startSize;
    }

    /**
     * @param startSize the startSize to set
     */
    public void setStartSize(int startSize) {
        this.startSize = startSize;
    }
}
