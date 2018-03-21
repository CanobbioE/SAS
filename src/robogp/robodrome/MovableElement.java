/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome;

import java.awt.image.BufferedImage;
import robogp.robodrome.image.ImageUtil;

/**
 *
 * @author claudia
 */
public class MovableElement {

    private BufferedImage img;
    private BufferedImage resizedImg;
    private int rowPos;
    private int colPos;
    private Direction dir;
    private int posX;
    private int posY;
    private float rotation;
    private boolean visible;

    public MovableElement(BufferedImage img) {
        this.img = img;
        this.resizedImg = img;
    }
    
    public void setImage(BufferedImage img) {
        this.img = img;
        this.resizedImg = img;
    }
    
    public void resetImageSize(int w, int h) {
        this.resizedImg = ImageUtil.scale(this.img, w, h);
    }

    public void setBoardPosition(int row, int col) {
        this.rowPos = row;
        this.colPos = col;
    }
    /**
     * @return the posX
     */
    public int getPosX() {
        return posX;
    }

    /**
     * @return the posY
     */
    public int getPosY() {
        return posY;
    }

    /**
     * @return the img
     */
    public BufferedImage getImage() {
        return this.resizedImg;
    }

    /**
     * @param posX the posX to set
     */
    public void setPosX(int posX) {
        this.posX = posX;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @return the rowPos
     */
    public int getRowPos() {
        return rowPos;
    }

    /**
     * @return the colPos
     */
    public int getColPos() {
        return colPos;
    }

    /**
     * @param posY the posY to set
     */
    public void setPosY(int posY) {
        this.posY = posY;
    }

    /**
     * @param rotation the rotation to set
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * @return the dir
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * @param dir the direction to set
     */
    public void setDirection(Direction dir) {
        this.dir = dir;        
    }
    
    public void resizeImage(int size) {
        img = ImageUtil.scale(img, size, size);        
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
