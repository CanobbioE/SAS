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
 *
 * @author claudia
 */
class PitCell extends BoardCell {
  private final boolean[] walls;
  
  PitCell(String[] comps) {
    super('P');
    boolean wset = false;
    walls = new boolean[4];
    java.util.Arrays.fill(walls, false);
    for (int i=0; i < comps.length && !wset; i++) {
      if (comps[i].length() >= 5 && comps[i].charAt(0) == 'W' ) {
        wset = true;
        for (int j=0; j < walls.length; j++) {
          walls[j] = (comps[i].charAt(j+1) == 'y');
        }
      }
    }
  }
  
  @Override
  public boolean hasWall(Direction d) {
    return walls[d.ordinal()];
  }
  
  @Override
  public BufferedImage getBaseImage() {
    BufferedImage res = ImageUtil.superImpose(null, TileProvider.getTileProvider().getTile("P"));
    return res;
  }
  
  @Override
  public BufferedImage getTopImage() {
     return getWalls(walls);
  }  
}
