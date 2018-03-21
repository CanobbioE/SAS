package robogp.matchmanager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import robogp.robodrome.image.ImageUtil;
import robogp.giocare.*;
import java.util.*;

/**
 *
 * @author claudia
 */
public class RobotMarker implements Serializable {

    public static final int MAX_HP = 10;
    public static final int MAX_LIVES = 3;

    private transient BufferedImage robotImage;
    private final String name;
    private final String color;
    private String owner;
    private int dockNumber;
    private int hitPoints;
    private final Registry[] registries;
    private final boolean asleep;
    private boolean toBeAsleep;
    private UpgradePack activeUpgrade;
    private Position position;
    private Position savedPosition;
    private int lifes;
    private int[] touchedCheckPoints

    public RobotMarker(String name, String color) {
        this.name = name;
        this.color = color;
        this.dockNumber = 0;
        this.hitPoints = MAX_HP;
        this.registries = new Registry[4];
        this.asleep = false;
        this.toBeAsleep = false;
        this.activeUpgrade = null;
        this.lives = MAX_LIVES
        this.touchedCheckPoints = new int[3];
    }

    public BufferedImage getImage(int size) {
        if (this.robotImage == null) {
            String imgFile = "robots/" + name + ".png";
            try {
                robotImage = ImageIO.read(new File(imgFile));
            } catch (IOException ex) {
                Logger.getLogger(RobotMarker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ImageUtil.scale(ImageUtil.superImpose(null, this.robotImage),size, size);
    }

    public void assign(String nickname, int dock) {
        this.owner = nickname;
        this.dockNumber = dock;
    }

    public void free() {
        this.owner = null;
        this.dockNumber = 0;
    }

    public boolean isAssigned() {
        return (this.dockNumber > 0);
    }

    public String getOwner() {
        return this.owner;
    }

    public int getDock() {
        return this.dockNumber;
    }
    
    public String getName() {
        return name;
    }

    public int getHitPoints() {
        return this.hitPoints;
    }

    public Registry getRegistry (int n) {
        if (n < registries.length()) {
            return this.registries[n];
        }
        return null;
    }

    public InstructionCard getRegistryInstr(int n) {
        if (n < registries.length()) {
            return this.registries[n].getInstruction();
        }
        return null;
    }

    public void setRegistryInstr(int n, InstructionCard istr) {
        if (n < registries.length()) {
            this.registries[n].setInstruction(istr);
        }
    }
    public ArrayList<Registry> getRegistries () {
        return this.registries;
    }

    public void addRegistry(Registry reg) {
        this.registries[reg.getNumber()] = reg;
    }

    public boolean isAsleep() {
        return this.asleep;
    }

    public void setToBeAsleep(boolean b) {
        this.toBeAsleep = b;
    }

    public int updateHp(int val) {
        if (this.hitPoints < RobotMarker.MAX_HP) {
            this.hitPoints += val;
        }
        return this.hitPoints;
    }

    public UpgradePack getActiveUpgrade() {
        return this.activeUpgrade;
    }

    public Position getPosition() {
        return this.postition;
    }

    public void setPosition(Position pos) {
        this.position = pos;
    }

    public void setPosition(int row, int col) {
        this.position.setRow(row);
        this.position.setCol(col);
    }

    public int destroy() {
        this.position = savedPosition;
        this.hitPoints = 10;
        this.asleep = false;
        this.toBeAsleep = false;
        this.activeUpgrade = null;
        return --this.lives;
    }

    public int[] getTouchedCheckPoints() {
        return this.touchedCheckPoints;
    }

    public void savePosition(Position pos) {
        this.savedPosition = pos;
    }

    public int[] addTouchedCheckpoint(int nCheck) {
        this.touchedCheckPoints[nCheck-1] = nCheck;
    }

    public void blockRegistry(int numReg) {
        this.registries[numReg].setBlocked(true);
    }

    public void unlockRegistry(int numReg) {
        this.registries[numReg].setBlocked(false)
        this.registries[numReg].cleat();
    }

    public boolean isToBeAsleep() {
        return this.toBeAsleep;
    }

    public void randomizeReg() {
        for (int k = 0; k < 5; k++) {
            this.registries[k].setInstruction(registries[Math.random()*4].getInstruction());
        }
    }
}
