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
public class TransitionAnimation extends Animation {

    /**
     * @return the boundary
     */
    public BoundingRect getBoundary() {
        return boundary;
    }

    /**
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @param cellSize the speed to set
     */
    public void setSpeed(int cellSize) {
        this.speed = (float)cellSize*3/TIMEUNIT;
    }

    /**
     * @return the targetOriginX
     */
    public int getTargetOriginX() {
        return targetOriginX;
    }

    /**
     * @return the targetOriginY
     */
    public int getTargetOriginY() {
        return targetOriginY;
    }

    /**
     * @return the currentOriginX
     */
    public int getCurrentOriginX() {
        return currentOriginX;
    }

    /**
     * @return the currentOriginY
     */
    public int getCurrentOriginY() {
        return currentOriginY;
    }

    public static class BoundingRect {

        private final int top;
        private final int bottom;
        private final int left;
        private final int right;

        public BoundingRect(int xCenter, int yCenter, int viewW, int viewH, int maxX, int maxY, int cellSize) {
            float spanH = 1.5f, spanV = 1.5f;
            if (viewW < cellSize * 3) {
                spanH = 1;
            }
            if (viewW < cellSize * 2) {
                spanH = 0.5f;
            }
            if (viewW < cellSize) {
                spanH = viewW / (cellSize * 2);
            }
            if (viewH < cellSize * 3) {
                spanV = 1;
            }
            if (viewH < cellSize * 2) {
                spanV = 0.5f;
            }
            if (viewH < cellSize) {
                spanV = viewH / (cellSize * 2);
            }

            top = Math.max(0, (int) (yCenter - spanV * cellSize));
            bottom = Math.min(maxY, (int) (yCenter + spanV * cellSize));
            left = Math.max(0, (int) (xCenter - spanH * cellSize));
            right = Math.min(maxX, (int) (xCenter + spanH * cellSize));
        }

        /**
         * @return the top
         */
        public int getTop() {
            return top;
        }

        /**
         * @return the bottom
         */
        public int getBottom() {
            return bottom;
        }

        /**
         * @return the left
         */
        public int getLeft() {
            return left;
        }

        /**
         * @return the right
         */
        public int getRight() {
            return right;
        }
    }

    private boolean commanded;
    private BoundingRect boundary;
    private Direction dir;

    private int targetOriginX;
    private int targetOriginY;
    private int currentOriginX;
    private int currentOriginY;
    /*private int startOriginX;
    private int startOriginY;*/
    
    private int toX;
    private int toY;
    
    private float speed;

    private TransitionAnimation() {
        this.type = Animation.Type.TRANSITION;
    }

    public TransitionAnimation(int x, int y, boolean commanded) {
        this();
        this.toX = x;
        this.toY = y;
        this.commanded = commanded;
    }
    
    public boolean isCommanded() {
        return commanded;
    }
    
    public void setBoundary(int viewW, int viewH, int maxW, int maxH, int cellsize) {
        this.boundary = new BoundingRect(this.toX, this.toY, viewW, viewH, maxW, maxH, cellsize);
    }
    
    public void setTargetOrigin(int x, int y) {
        this.targetOriginX = x;
        this.targetOriginY = y;
    }
    
    public void setCurrentOrigin(int x, int y) {
        this.currentOriginX = x;
        this.currentOriginY = y;
    }
    
    @Override
    public boolean hasFinished(long elapsed) {
        boolean finished = ((this.getCurrentOriginX() == this.getTargetOriginX()) 
                && (this.getCurrentOriginY() == this.getTargetOriginY()));
        return finished;
    } 
}
