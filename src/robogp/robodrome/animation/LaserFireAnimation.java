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
public class LaserFireAnimation extends Animation {

    /**
     * @return the hitRobot
     */
    public boolean shouldHitRobot() {
        return hitRobot;
    }

    /**
     * @return the hitEndWall
     */
    public boolean shouldHitEndWall() {
        return hitEndWall;
    }

    /**
     * @return the dir
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * @return the startCell
     */
    public int getStartCell() {
        return startCell;
    }

    /**
     * @return the endCell
     */
    public int getEndCell() {
        return endCell;
    }

    /**
     * @return the heatRadMax
     */
    public int getMaxHeatRadius() {
        return heatRadMax;
    }

    /**
     * @return the startX
     */
    public int getStartX() {
        return startX;
    }

    /**
     * @return the startY
     */
    public int getStartY() {
        return startY;
    }

    /**
     * @return the endX
     */
    public int getEndX() {
        return endX;
    }

    /**
     * @return the endY
     */
    public int getEndY() {
        return endY;
    }

    public static class LaserState {

        private int circleX;
        private int circleY;
        private int circleRad;
        private int currentX;
        private int currentY;
        private boolean visible;
        private boolean fired;
        
        public void setCirclePosition(int x, int y) {
            circleX = x;
            circleY = y;
        }
        
        public void setCurrentPosition(int x, int y) {
            currentX = x;
            currentY = y;
        }

        /**
         * @return the circleX
         */
        public int getCircleX() {
            return circleX;
        }

        /**
         * @return the circleY
         */
        public int getCircleY() {
            return circleY;
        }

        /**
         * @return the circleRad
         */
        public int getCircleRadius() {
            return circleRad;
        }

        /**
         * @param circleRad the circleRad to set
         */
        public void setCircleRadius(int circleRad) {
            this.circleRad = circleRad;
        }

        /**
         * @return the currentX
         */
        public int getCurrentX() {
            return currentX;
        }

        /**
         * @return the currentY
         */
        public int getCurrentY() {
            return currentY;
        }

        /**
         * @return the visible
         */
        public boolean isVisible() {
            return visible;
        }

        /**
         * @param visible the visible to set
         */
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        /**
         * @return the fired
         */
        public boolean isFired() {
            return fired;
        }

        /**
         * @param fired the fired to set
         */
        public void setFired(boolean fired) {
            this.fired = fired;
        }
    }

    private Direction dir;
    private int startCell;
    private int endCell;
    private boolean hitRobot;
    private boolean hitEndWall;

    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public static int HEATTIME = 300;
    private int heatRadMax;

    private LaserFireAnimation() {
        this.type = Animation.Type.LASER_FIRE;
    }

    public LaserFireAnimation(int robotNum, Direction dir, int start, int end,
            boolean hitRobot, boolean hitEndWall, int cellSize) {
        this();
        which = robotNum;
        duration = (int) (Animation.TIMEUNIT * Math.abs(end - start) * (cellSize / 300.0) + LaserFireAnimation.HEATTIME);
        this.dir = dir;
        startCell = start;
        endCell = end;
        this.hitRobot = hitRobot;
        this.hitEndWall = hitEndWall;
        heatRadMax = 10;
    }
    
    public void setStartPosition(int x, int y) {
        this.startX = x;
        this.startY = y;
    }
    
    public void setEndPosition(int x, int y) {
        this.endX = x;
        this.endY = y;
    }
}
