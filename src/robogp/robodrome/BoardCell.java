/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome;

import java.awt.image.BufferedImage;
import robogp.robodrome.image.ImageUtil;
import robogp.robodrome.image.TileProvider;

/**
 * Rappresenta una generica cella del robodromo
 * @author claudia
 */
public class BoardCell {

    /**
     * Crea una casella del robodromo a partire da un array di stringhe che la descrivono
     * @param comps le stringhe che descrivono la casella
     * @return la casella creata
     */
    public static BoardCell createBoardCell(String[] comps) {
        char t = comps[0].trim().charAt(0);
        String[] recomps = new String[comps.length - 1];
        for (int i = 0; i < recomps.length; i++) {
            recomps[i] = comps[i + 1].trim();
        }
        BoardCell b = null;
        switch (t) {
            case 'F':
                b = new FloorCell(recomps);
                break;
            case 'P':
                b = new PitCell(recomps);
                break;
            case 'B':
            case 'E':
                b = new BeltCell(recomps, t);
        }
        return b;
    }

    private final char type;
    private boolean hLaser;
    private boolean vLaser;

    public boolean hasHorizontalLaser() {
        return hLaser;
    }

    public boolean hasVerticalLaser() {
        return vLaser;
    }

    public void setHorizontalLaser(boolean v) {
        hLaser = v;
    }

    public void setVerticalLaser(boolean v) {
        vLaser = v;
    }

    BoardCell(char t) {
        this.type = t;
    }

    public char getType() {
        return type;
    }

    public BufferedImage getBaseImage() {
        return null;
    }

    public BufferedImage getTopImage() {
        return null;
    }

    public java.util.ArrayList<CellLaser> getLasers() {
        return new java.util.ArrayList<>();
    }

    public boolean hasWall(Direction d) {
        return false;
    }

    public BufferedImage getImage() {
        BufferedImage res = getBaseImage();
        BufferedImage limg = getLasersImage();
        if (limg != null) {
            res = ImageUtil.superImpose(res, limg);
        }
        BufferedImage topimg = getTopImage();
        if (topimg != null) {
            res = ImageUtil.superImpose(res, topimg);
        }
        return res;
    }

    BufferedImage getLasersImage() {
        BufferedImage result = null;
        if (hasHorizontalLaser()) {
            result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("Lh"));
        }
        if (hasVerticalLaser()) {
            result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("Lv"));
        }
        return result;
    }

    BufferedImage getWalls(boolean[] walls) {
        BufferedImage result = null;
        boolean[] wallset = java.util.Arrays.copyOf(walls, walls.length);
        if (wallset[0]) {
            wallset[0] = false;
            if (wallset[1]) {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("WW", Direction.W));
                wallset[1] = false;
            } else if (wallset[3]) {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("WW", Direction.S));
                wallset[3] = false;
            } else {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("W", Direction.W));
            }
        }

        if (wallset[1]) {
            wallset[1] = false;
            if (wallset[2]) {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("WW", Direction.N));
                wallset[2] = false;
            } else {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("W", Direction.N));
            }
        }
        if (wallset[2]) {
            wallset[2] = false;
            if (wallset[3]) {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("WW", Direction.E));
                wallset[3] = false;
            } else {
                result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("W", Direction.E));
            };
        }
        if (wallset[3]) {
            result = ImageUtil.superImpose(result, TileProvider.getTileProvider().getTile("W", Direction.S));
            wallset[3] = false;
        }
        return result;
    }
}
