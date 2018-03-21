/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.animation;

import robogp.robodrome.Direction;

/**
 *
 * @author claudia
 */
public class RobotHitAnimation extends Animation {
  private Direction dir;
  private static int BLINK = 3;
  private static float SHIFT = 0.3f;
  private float shiftX;
  private float shiftY;
  private int phase;
  
  private RobotHitAnimation() {
    this.type = Animation.Type.ROBOT_HIT;
  }
  
  public RobotHitAnimation(int robotNum, Direction dir) {
      this();
      which = robotNum;
      duration = (int)(Animation.TIMEUNIT*1.5);
      this.dir = dir;
      switch(dir) {
        case W: 
          shiftX = -SHIFT;
          break;
        case E:
          shiftX = SHIFT;
          break;
        case S:
          shiftY = SHIFT;
          break;
        case N:
          shiftY = -SHIFT;
          break;
      }      
  }

    /**
     * @return the direction
     */
    public Direction getDirection() {
        return dir;
    }
    
    public void adjustPhase() {
        this.phase = (int)(this.duration / (2 * BLINK - 1));        
    }

    /**
     * @return the shiftX
     */
    public float getShiftX() {
        return shiftX;
    }

    /**
     * @return the shiftY
     */
    public float getShiftY() {
        return shiftY;
    }

    /**
     * @return the phase
     */
    public int getPhase() {
        return phase;
    }
}
