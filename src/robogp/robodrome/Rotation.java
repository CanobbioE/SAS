/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome;

import robogp.robodrome.Direction;

/**
 *
 * @author claudia
 */
public enum Rotation {
    NO(0, 0), CW90(90, +1), CW180(180, +1), CW270(270, +1), CCW90(90, -1), CCW180(180, -1), CCW270(270, -1);
    public float degrees;
    public int wise;

    private Rotation(int deg, int w) {
        degrees = deg;
        wise = w;
    }

    public float getDegrees() {
        return degrees;
    }

    public int getWise() {
        return wise;
    }

    public static Direction changeDirection(Direction startDir, Rotation rot) {
        int turn = 0;
        switch (rot) {
            case CW90:
            case CCW270:
                turn = 1;
                break;
            case CW180:
            case CCW180:
                turn = 2;
                break;
            case CCW90:
            case CW270:
                turn = 3;
                break;
            case NO:
        }
        int newVal = (startDir.ordinal() + turn) % 4;
        return Direction.values()[newVal];
    }
}
