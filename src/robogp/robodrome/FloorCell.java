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
class FloorCell extends BoardCell {
  
  private final boolean[] walls;
  private final boolean[] lasers;
  private final java.util.ArrayList<CellLaser> allLasers;
  private int pusher;
  private Direction pusherDir;
  private int rotator;
  private boolean upgrade;
  private boolean repair;
  private int checkpoint;
  private int dockNumber;
  private Direction dockDir;
  
  FloorCell(String[] comps) {
    super('F');
    boolean wset = false;
    boolean xset = false;
    checkpoint = 0;
    dockNumber = 0;
    pusher = 0;
    rotator = 0;
    
    walls = new boolean[4];
    lasers = new boolean[4];
    java.util.Arrays.fill(walls, false);
    java.util.Arrays.fill(lasers, false);
    allLasers = new java.util.ArrayList<>();
    
    int wallnumber = 0;
    
    for (int i=0; i < comps.length; i++) {
      if (comps[i].length() >= 5 && comps[i].charAt(0) == 'W' ) {
        wset = true;
        for (int j=0; j < walls.length; j++) {
          walls[j] = (comps[i].charAt(j+1) == 'y');
          if (walls[j]) wallnumber++;
        }
      }
      else if (comps[i].length() >= 5 && comps[i].charAt(0) == 'L' && wset && wallnumber==1) {
        for (int j=0; j < lasers.length; j++) {
          lasers[j] = (comps[i].charAt(j+1) == 'y');
        }        
      }
      
      else if (comps[i].charAt(0) == 'P' && comps[i].length() >= 3 && wset && !xset) {
        int pwall = Integer.parseInt(""+comps[i].charAt(2));
        if (walls[pwall]) {
          pusher = Integer.parseInt(""+comps[i].charAt(1));
          pusherDir = Direction.values()[pwall];
          xset = true;
        }
      }
      else if (comps[i].charAt(0) == 'R'  && comps[i].length() >= 2 && !xset) {
          rotator = Integer.parseInt(""+comps[i].charAt(1));
          xset = true;
      }
      else if (comps[i].charAt(0) == 'A'  && comps[i].length() >= 2 && !xset) {
        if (comps[i].charAt(1) == 'u') {
          upgrade = true;
          xset = true;
        }
        else if (comps[i].charAt(1) == 'r') {
          repair = true; 
          xset = true;
        }
        else if (comps[i].charAt(1) == 'c' && comps[i].length() >= 3) {
          checkpoint = Integer.parseInt(""+comps[i].charAt(2));
          xset = true;
        }
      }
      else if (comps[i].charAt(0) == 'D'  && comps[i].length() >= 3 && !xset) {
        dockNumber = Integer.parseInt(""+comps[i].charAt(1));
        dockDir = Direction.values()[Integer.parseInt(""+comps[i].charAt(2))];
        xset = true;
      }
    }  
    
    for (int i = 0; i < Direction.values().length; i++) {
      if (lasers[i]) {
        CellLaser las = new CellLaser(Direction.values()[i]);
        allLasers.add(las);
      }
    }
  }
  
  public boolean isCheckpoint() {
    return checkpoint > 0;
  }
  
  public int getCheckpoint() {
    return checkpoint;
  }
  
  public boolean isDock() {
    return dockNumber > 0;
  }
  
  public int getDock() {
    return dockNumber;
  }
  
  public Direction getDockDirection() {
      return dockDir;
  }
  
  public boolean isLeftRotator() {
    return (rotator > 0) && (rotator % 2 == 1); 
  }

  public boolean isRightRotator() {
    return (rotator > 0) && (rotator % 2 == 0); 
  }
  
  public boolean isUpgrade() {
    return upgrade;
  }
  
  public boolean isRepair() {
    return repair;
  }

  public boolean hasEvenPusher() {
    return (pusher > 0) && (pusher % 2 == 0);
  }

  public boolean hasOddPusher() {
    return (pusher > 0) && (pusher % 2 == 1);
  }
  
  public Direction getPusherWall() {
    return pusherDir;
  }

  @Override
  public boolean hasWall(Direction d) {
    return walls[d.ordinal()];
  }
  
  public boolean hasLaser(Direction d) {
    return lasers[d.ordinal()];
  }
  
  @Override
  public java.util.ArrayList<CellLaser> getLasers() {
    java.util.ArrayList<CellLaser> ret = new java.util.ArrayList<>();
    ret.addAll(allLasers);
    return ret;
  }
  
  public BufferedImage getBaseImage() {
    BufferedImage res = ImageUtil.superImpose(null, TileProvider.getTileProvider().getTile("F"));
    if (isLeftRotator()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("Rl"));
    else if (isRightRotator()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("Rr"));
    else if (isUpgrade()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("Au"));
    else if (isRepair()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("Ar"));
    else if (isDock())  res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("D"+getDock()));
    else if (isCheckpoint()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("Ac"+getCheckpoint()));
    return res;
  }
  
  @Override
  public BufferedImage getTopImage() {
    BufferedImage res = null;
    for (int i=0; i < lasers.length; i++) {
      if (lasers[i]) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("L", Direction.values()[i]));
    }
    res = ImageUtil.superImpose(res, getWalls(walls));
    if (hasOddPusher()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("P1", getPusherWall()));
    if (hasEvenPusher()) res = ImageUtil.superImpose(res, TileProvider.getTileProvider().getTile("P2", getPusherWall()));
    return res;
  }
  
}
