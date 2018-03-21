package robogp.matchmanager;

import connection.Connection;
import connection.Message;
import connection.MessageObserver;
import connection.PartnerShutDownException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import robogp.robodrome.Robodrome;
import robogp.giocare.*;
import robogp.training.*;
import java.util.*;

/**
 *
 * @author claudia
 */
public class Match implements MessageObserver {

    public static final int ROBOTSINGAME = 8;
    public static final int MAX_TIMER = 30000;
    public static final String PlayerRecivedUpgradeMsg "recivedUpgrade";
    public static final String MatchJoinRequestMsg = "joinMatchRequest";
    public static final String RobotRepairedMsg = "repairedRobotMsg";
    public static final String RobotUpdatePosition "updatePosition";
    public static final String MatchJoinReplyMsg = "joinMatchReply";
    public static final String MatchPoolRequestMsg = "poolRequest";
    public static final String RobotDestroyedMsg "robotDestroyed";
    public static final String MatchPoolReplyMsg = "poolReply";
    public static final String RobotOutOfLivesMsg "outOfLives";
    public static final String MatchCancelMsg = "cancelMatch";
    public static final String MatchTimerOutMsg = "timeOut";
    public static final String MatchStartMsg = "startMatch";
    public static final String CardRevealMsg "revealCard";
    public static final String RobotHitMessage "robotHit";
    public static final String MatchEndMessage "endMatch";
    public static final String MancheEndMsg "endManche";



    public enum EndGame {
        First, First3, AllButLast
    };

    public enum State {
        Created, Started, Canceled
    };

    private static final String[] ROBOT_COLORS = {"blue", "red", "yellow", "emerald", "violet", "orange", "turquoise", "green"};
    private static final String[] ROBOT_NAMES = {"robot-blue", "robot-red", "robot-yellow", "robot-emerald", "robot-violet", "robot-orange", "robot-turquoise", "robot-green"};
    private final Robodrome theRobodrome;
    private final RobotMarker[] robots;
    private final EndGame endGameCondition;
    private final int nMaxPlayers;
    private final int nRobotsXPlayer;
    private final boolean initUpgrades;
    private State status;
    private int turn;

    private final HashMap<String, Connection> readyQ;
    private final HashMap<String, Connection> waiting;
    private final HashMap<String, Connection> players;
    private final ArrayList<Pair> toBeActivated;
    private ArrayList<RobotMarker> leaderboard;

    private final HashMap<InstructionCard, Boolean> istrDeck;
    private final ArrayList<UpgradePack> upgrDeck;

    private final Timer timer;

    /* Gestione pattern singleton */
    private static Match singleInstance;

    private Match(String rbdName, int nMaxPlayers, int nRobotsXPlayer, EndGame endGameCond, boolean initUpg) {
        this.nMaxPlayers = nMaxPlayers;
        this.nRobotsXPlayer = nRobotsXPlayer;
        this.endGameCondition = endGameCond;
        this.initUpgrades = initUpg;
        String rbdFileName = "robodromes/" + rbdName + ".txt";
        this.robots = new RobotMarker[Match.ROBOT_NAMES.length];
        this.theRobodrome = new Robodrome(rbdFileName);
        for (int i = 0; i < Match.ROBOT_NAMES.length; i++) {
            this.robots[i] = new RobotMarker(Match.ROBOT_NAMES[i], Match.ROBOT_COLORS[i]);
        }
        toBeActivated = new ArrayList<>();
        istrDeck = new HashMap<>();
        upgrDeck = UpgradePack.generateDeck();
        waiting = new HashMap<>();
        players = new HashMap<>();
        readyQ = new HashMap<>();
        timer = new Timer();
        leaderboard = new ArrayList<>();
        this.status = State.Created;
        this.turn = 0;
    }

    public static Match getInstance(String rbdName, int nMaxPlayers,
            int nRobotsXPlayer, EndGame endGameCond, boolean initUpg) {
        if (Match.singleInstance == null || Match.singleInstance.status == Match.State.Canceled) {
            Match.singleInstance = new Match(rbdName, nMaxPlayers, nRobotsXPlayer, endGameCond, initUpg);
        }
        return Match.singleInstance;
    }

    public static Match getInstance() {
        if (Match.singleInstance == null || Match.singleInstance.status == Match.State.Canceled) {
            return null;
        }
        return Match.singleInstance;
    }

    @Override
    public void notifyMessageReceived(Message msg) {
        if (msg.getName().equals(Match.MatchJoinRequestMsg)) { // Richiesta di Join
            String nickName = (String) msg.getParameter(0);
            this.waiting.put(nickName, msg.getSenderConnection());
            MatchManagerApp.getAppInstance().getIniziarePartitaController().matchJoinRequestArrived(msg);

        } else if (msg.getName().equals(Match.MatchPoolRequestMsg)) { // Richiesta di schede
            RobotMarker rb = (RobotMarker) msg.getParameter(0);
            int hp = rb.getHitPoints();
            InstructionCard[] tmpPool = new InstructionCard[rb.getHitPoints()-1];
            InstructionCard randIstr;

            int available = Collections.frequency(istrDeck.values(), true);
            int needed = players.size() * nRobotsXPlayer;
            if ( available < needed || istrDeck.isEmpty() ) {
                resetIstrDeck();
            }

            for (int k = 0; k < rb.getHitPoints()-1; k++) {
                do {
                    randIstr = getRandomInstruction();
                } while (istrDeck.get(randIstr) == false);
                tmpPool[k] = randIstr;
            }

            Message m = new Message(MatchPoolReplyMsg);
            Object[] param = new Object[1];
            param[0] = new Pair(tmpPool, rb);
            m.setParameters(param);
            Connection conn = msg.getSenderConnection();
            try {
                conn.sendMessage(m);
            } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        }

        } else if (msg.getName().equals("turnRobotOffRequest")) { // Richiesta spegni robot
           RobotMarker rb = (RobotMarker) msg.getParameter(0);
           rb.setToBeAsleep(true);

        } else if (msg.getName().equals("doneProgramming")) { // Terminata programmazione
            String nickname = (String) msg.getParameter(0);
            readyQ.put(nickname, msg.getSenderConnection());

            if (readyQ.size() == players.size() - 1) {
                Connection conn = getNotReady();
                Message m = new Message(Match.MatchTimerOutMsg);
                TimerTask timerTask = new TimeOut(conn, m);
                this.timer.schedule(timerTask, Match.MAX_TIMER);
            }

            if (readyQ.size() == players.size()) {
                this.timer.cancel();
                this.timer.purge();
                readyQ.clear();
                startExecution();
                endManche();
            }

        } else if (msg.getName().equals("useUpgrade")) { // Usato un upgrade
            RobotMarker rb = (RobotMarker) msg.getParameter(0);
            UpgradePack up = (UpgradePack) msg.getParameter(1);
            this.toBeActivated.add(new Pair(rb, up));

        } else if (msg.getName().equals("forfaitUpgrade")) { // Sacrifica upgrade
            RobotMarker rb = (RobotMarker) msg.getParameter(0);
            UpgradePack up = (UpgradePack) msg.getParameter(1);
            rb.updateHp(1);

            Connection conn = msg.getSenderConnection();
            Message m = new Message(Match.RobotRepairedMsg);
            try{
                conn.sendMessage(m);
            } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        }

        }
    }

    public State getStatus() {
        return this.status;
    }

    public void cancel() {
        this.status = State.Canceled;

        Message msg = new Message(Match.MatchCancelMsg);
        waiting.keySet().stream().forEach((nickname) -> {
            this.refusePlayer(nickname);
        });

        players.values().stream().forEach((conn) -> {
            try {
                conn.sendMessage(msg);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public void start() {
        this.status = State.Started;

        Message msg = new Message(Match.MatchStartMsg);

        players.values().stream().forEach((conn) -> {
            try {
                conn.sendMessage(msg);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public void stop() {
        // PROBABILMENTE NON IMPLEMENTATO NEL CORSO DI QUESTO PROGETTO
    }

    public ArrayList<RobotMarker> getAvailableRobots() {
        ArrayList<RobotMarker> ret = new ArrayList<>();
        for (RobotMarker m : this.robots) {
            if (!m.isAssigned()) {
                ret.add(m);
            }
        }
        return ret;
    }

    public ArrayList<RobotMarker> getAllRobots() {
        ArrayList<RobotMarker> ret = new ArrayList<>();
        for (RobotMarker m : this.robots) { // TODO
            ret.add(m);
        }
        return ret;
    }

    public int getRobotsPerPlayer() {
        return this.nRobotsXPlayer;
    }

    public void refusePlayer(String nickname) {
        try {

            Connection conn = this.waiting.get(nickname);

            Message reply = new Message(Match.MatchJoinReplyMsg);
            Object[] parameters = new Object[1];
            parameters[0] = new Boolean(false);
            reply.setParameters(parameters);

            conn.sendMessage(reply);
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            waiting.remove(nickname);
        }
    }

    public boolean addPlayer(String nickname, List<RobotMarker> selection) {
        boolean added = false;
        try {
            for (RobotMarker rob : selection) {
                int dock = this.getFreeDock();
                rob.assign(nickname, dock);
            }

            Connection conn = this.waiting.get(nickname);

            Message reply = new Message(Match.MatchJoinReplyMsg);
            Object[] parameters = new Object[2];
            parameters[0] = new Boolean(true);
            parameters[1] = selection.toArray(new RobotMarker[selection.size()]);
            reply.setParameters(parameters);

            conn.sendMessage(reply);
            this.players.put(nickname, conn);
            added = true;
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            waiting.remove(nickname);
        }
        return added;

    }

    private int getFreeDock() {
        boolean[] docks = new boolean[this.theRobodrome.getDocksCount()];
        for (RobotMarker rob : this.robots) {
            if (rob.isAssigned()) {
                docks[rob.getDock() - 1] = true;
            }
        }
        int count = 0;
        while (docks[count]) {
            count++;
        }
        if (count < docks.length) {
            return count + 1;
        }
        return -1;
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.nMaxPlayers;
    }

    private void resetIstrDeck() {
        this.istrDeck.clear();
        InstructionCard[] deck = InstructionCard.generateDeck();

        for (InstructionCard i : deck) {
            this.istrDeck.put(i, true);
        }
    }

    private InstructionCard getRandomInstruction() {
        return (InstructionCard)((ArrayList)(istrDeck.keySet())).get((int)(Math.random()*83));
    }

    private UpgradePack getRandomUpgrade() {
        return upgrDeck.get((int)(Math.random()*(upgrDeck.size()-1)));
    }

    private Connection getNotReady() {
        ArrayList<String> allPlayers = (ArrayList)players.keySet();
        for (String nickName : allPlayers) {
            if (!readyQ.containsKey(nickName)) {
                return players.get(nickName);
            }
        }
        return null;
    }

    private int getMaxPriority(ArrayList<?> array) {
        int max = 0;
        for (InstructionCard istr : array) {
            if (istr.getPriority() >= max){
                max = istr.getPriority();
            }
        }
        return max;
    }

    private assignPosBasedOnDock() {
        // Assegna posizione in base al dock
        if (turn == 0) {
            for (int n = 0; n < robots.length; n++) {
                if (robots[n].isAssigned()) {
                    for (int i = 0; i < robodrome.getRowCount()) {
                        for (int j = 0; j < robodrome.getColCount()) {
                            cell = theRobodrome.getCell(i,j);
                            if (cell instanceof FloorCell  && cell.isDock()) {
                                dock++;
                                if (dock == robots[n].getDock()) {
                                    Direction dockDir = theRobodrome.getDockDir(i,j);
                                    aggiornaPosizione(rb,  new Position(i,j,getDock,dockDir));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //------------------------------------------ Parte principale di gioco

    private void startExecution() {
        for (int n; n < 5; n++ ) {
            for (int i = 0; i < robots.lenth()) { // Ripara robot spenti
                if (robots[i].isAsleep() && robots[i].isAssigned()) {
                    robots[i].repair();
                }
            }

            dichiarazione(n);
            clearAndInstallUpgrades();

            mossa(n);
            clearAndInstallUpgrades();

            attivazioneRobodromo();
            clearAndInstallUpgrades();
            
            laserAndArmi();
            clearAndInstallUpgrades();
            
            touchAndSave();
            clearAndInstallUpgrades();

        }
    }

    private void dichiarazione(int numReg) {
        for (int i = 0; i < robots.length; i++) {
            if (!robots[i].isAsleep() && robots[i].isAssigned()) {
                InstructionCard istr = robots[i].getRegistryInstr(n);
                UpgradePack up = robots[i].getActiveUpgrade();

                // Gestione upgrade
                if (up != null) {
                    if (up.getType() == 'A') {
                        if (name == "random switch") {
                            robots[i].setRegistryInstr(numReg+1, getRandomInstruction());
                        } else if (name == "copia e incolla") {
                            robots[i].setRegistryInstr(numReg+1, istr);
                        }
                    } else {
                        robots[i].clearUpgrades();
                    }
                }

                Message msg = new Message(Match.CardRevealMsg);
                setParameters(robots[i], istr)
                players.values().stream().forEach((conn) -> {
                    try {
                        conn.sendMessage(msg);
                    } catch (PartnerShutDownException ex) {
                        Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        }
    }
    private void mossa(int numReg) {
        ArrayList<InstructionCard> istrArray = new ArrayList<>();
        for (int i = 0; i < istrArray.length) {
            if (robots[i].isAssigned && !robots[i].isAsleep) {
                istrArray.add(robots[i].getRegistryInstr(numReg));
            }
        }

        for (int k = 0; !istrArray.isEmpty() && k < istrArray.size(); i++) {
            if (istrArray.get(k) == getMaxPriority(istrArray)) {
                InstructionCard istr = istrArray.get(k);
                istrArray.remove(istr);

                Position pos;
                switch (istr.getName()) {
                    case MOVE:
                        moveRobot(robots[k], istr);
                        break;
                    case TURN:
                        turnRobot(robots[k], istr);
                        break;
                    case BACK:
                        backRobot(robots[k], istr);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void attivazioneRobodromo() {
        for (int i = 0; i < robots.length; i++) {
            Position pos = robots[i].getPosition();
            UpgradePack up = robots[i].getActiveUpgrade();
            // Gestione upgrade
            boolean skipBelt = false;
            boolean skipRotation = false;
            if (up != null && up.getType() == 'C') {
                String name = up.getName();
                if (name == "diturbatore di frequenze") {
                    skipBelt = true;
                } else if (name == "giroscopio") {
                    skipRotation = true;
                }
            } else {
                robots[i].clearUpgrades();
            }

            int row = pos.getRow();
            int col = pos.getCol();
            Direction dir = pos.getDirection();

            BoardCell cell = theRobodrome.getCell(row, col);
            if (!skipBelt && cell instanceof BeltCell) {
                Direction outDir = theRobodrome.getBeltOutput(cell);
                if (outDir.ordinal()%2 == 0) {
                    col += (outDir.ordinal()-1);
                } else {
                    row += (outDir.ordinal()-2);
                }
                aggiornaPosizione(robots[i], pos);
                cell = theRobodrome.getCell(pos.getRow(), pos.getCol());

                if (!skipRotation && cell instanceof BeltCell) {
                    Direction oppDir = Direction.values()[(dir.ordinal()+2)%4];
                    if (theRobodrome.hasBeltInput(cell, outDir)) {
                        Direction d = theRobodrome.getBeltOutput(cell);
                        pos.setDirection(d);
                        aggiornaPosizione(robots[i], pos);
                        cell = theRobodrome.getCell(pos.getRow(), pos.getCol());
                    }
                    checkPitCell(robots[i], pos);
                }
            } else if (cell instanceof FloorCell) {
                boolean isRR = theRobodrome.isCellRR(cell);
                boolean isLR = theRobodrome.isCellLR(cell);
                boolean ePsh = theRobodrome.hasCellEP(cell);
                boolean oPsh = theRobodrome.hasCellOP(cell);
                // Pusher
                if ((ePsh && turn%2==0) || (oPsh && turn%2!=0)) {
                    Direction pshDir = getCellPshWall(cell);
                    if (pshDir.ordinal()%2 == 0) { //pusher Est o Ovest
                        col = col -(pshDir.ordinal()-1);
                    } else { // pusher nord o sud
                        row = row -(pshDir.ordinal()-2);
                    }
                } else if (!skipRotation && isRR) { // Rotatore dx
                    pos.turnPosition(Rotation.CW90); 
                } else if (!skipRotation && isLR) { // Rotatore sx
                    pos.turnPosition(Rotation.CCW90);
                }
            }
            aggiornaPosizione(robots[i], pos);
            cell = theRobodrome.getCell(pos.getRow(), pos.getCo());
            checkPitCell(robots[i], pos);
        }
    }

    private void laserAndArmi() {
        int max;
        boolean collided
        boolean shield = false;
        for (int i = 0; i < robots.length; i++) { // Robots sparano
            if ( !robots[i].isAssigned() && !robots[i].isAsleep()) {
                Position pos = robots[i].getPosition();
                int row = pos.getRow();
                int col = pos.getCol();
                Direcrtion faceDir = pos.getDirection();

                // Gestione Upgrades
                int mul = 1;
                int maxHit = 1;
                boolean bomb = false;
                boolean retro = false;
                boolean skipDmg = false;
                Direction upgrDir = null;
                ArrayList<registries> swapRegistries;;
                LaserType laserType = LaserType.NORMAL;
                manageLaserUpgrades(bomb, maxHit, mul, skipDmg, upgrDir, laserType, rb, swapRegistries, retro);

                theRobodrome.getCell(row, col);
                Direcrtion oppDir = Direction.values()[(faceDir.ordinal()+2)%4];
                if (!cell.hasWall(oppDir) && !bomb) {
                    if (faceDir.ordinal() % 2 == 0) { // laser spara su E/O
                        max = theRobodrome.getColumnCount();
                    } else { // laser spara su N/S
                        max = theRobodrome.getRowCount();
                    }

                    for (int j = 1; j < max && !collided; j++) {
                        int rowScaling = row+j*((faceDir.ordinal()%2)+(faceDir.ordinal()-2));
                        int colScaling = col+j*(((faceDir.ordinal()%2)-1)*(-faceDir.ordinal()+1));
                        BoardCell cell = theRobodrome.getCell(rowScaling, colScaling);

                        if (cell.hasWall(oppDir) || cell.hasWall(faceDir)) {
                            collided = true;
                        }

                        collisionDetection(cell, shield, collided, laserType, skipDmg, swapRegistries, upgrDir);

                        if (collided && maxHit > 1) { // Laser colpisce due ostacoli
                            collided = false;
                            maxHit--;
                        }
                    }
                    cell = theRobodrome.get(row, col);
                    if (retro && !cell.hasWall(faceDir)) { // Retro laser
                        for (int j = 0; j < max && !collided; j++) {
                            int rowScaling = row+j*((faceDir.ordinal()%2)*(-faceDir.ordinal()+2));
                            int colScaling = col+j*(((faceDir.ordinal()%2)-1)*(faceDir.ordinal()-1));
                            cell = theRobodrome.getCell(rowScaling, colScaling);

                            if (cell.hasWall(oppDir) || cell.hasWall(faceDir)) {
                                collided = true;
                            }
                            collisionDetection(collided, laserType, false, null);
                        }
                    }
                } else if (bomb) { // Bomba
                    bomb(row, col);
                }
            }
        }

        ArrayList<Pair<Pair<int,int>, Direction>> lasers = theRobodrome.getLasersDirection();

        for (Pair<Pair<int,int>, Direction> element : lasers) { // Robodromo spara
            row = element.getFirst().getFirst();
            col = element.getFirst().getSecond();
            Direction laserDir = element.getSecond();

            if (laserDir.ordinal()%2 ==0) {
               max = theRobodrome.getColumnCount();
            } else {
                max = theRobodrome.getRowCount();
            }

            for (int j = 0; j < max && !collided; j++) {
                int rowScaling = row+j*((laserDir.ordinal()%2)+(laserDir.ordinal()-2));
                int colScaling = col+j*(((laserDir.ordinal()%2)-1)*(-laserDir.ordinal()+1));
                BoardCell cell = theRobodrome.getCell(rowScaling, colScaling);

                Direcrtion oppDir = Direction.values()[(faceDir.ordinal()+2)%4];
                if (cell.hasWall(oppDir) || cell.hasWall(laserDir)) {
                    collided = true;
                }

                for (int i = 0; i < robots.length; i++) {
                    laserType = LaserType.WALL;
                    collisionDetection(collided, laserType, false, null);
                }
            }
        }
    }

    private void touchAndSave() {
        for (int i = 0; i < robots.length; i++) {
            Position pos = robot[i].getPosition;

            int row = pos.getRow();
            int col = pos.getCol();
            BoardCell cell = theRobodrome.getCell(row,col);

            if (theRobodrome.isCellCheckpoint(cell)) {
                robots[i].savePosition(pos);
                int numCheck = theRobodrome.getCellCheckpoint(cell);
                int[] checks = robots[i].getTouchedCheckpoints();

                if (numCheck == nextCheckpoint(checks)) {
                    checks = robots[i].addTouchedCheckpoint(numCheck);
                    if (checks[2] == 3) {
                        this.leaderboard.add(robots[i]);
                        if (checkWinningConditions() == true) {
                             Message m = new Message(Match.MatchEndMessage);
                            m.setParameters(leaderboard);
                            players.values().stream().forEach((conn) -> {
                                try {
                                    conn.sendMessage(msg);
                                } catch (PartnerShutDownException ex) {
                                    Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });

                            //stop(); !NOT IMPLEMENTED!
                        }
                    }
                } // Checkpoint
            } else if (theRobodrome.isCellRepair(cell)) { // Repair
                robots[i].savePosition(pos);
                robots[i].updateHp(1);
            } else if (theRobodrome.isCellUpgrade()) { // Upgrade and repair
                robots[i].savePosition();
                robots[i].updateHp(1);

                if (!upgrDeck.isEmpty()) {
                    UpgradePack randUpgr = getRandomUpgrade();
                    upgrDeck.remove(randUpgr);

                    String owner = robots[i].getOwner();
                    Message m = new Message(Match.PlayerRecivedUpgradeMsg);
                    m.setParameters(randUpgr, rb);
                    players.values().stream().forEach((conn) -> {
                        try {
                            conn.sendMessage(m);
                        } catch (PartnerShutDownException ex) {
                            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }

            }

        }
    }

    private void endManche() {
        for (int i = 0; i < robots.length; i++) {
            if (robots[i].isAssigned()) {
                int currHp = robots[i].getHitPoints();
                Registry[] registries = robots[i].getRegistries();
                // Blocco/sblocco i registri in base ai PV
                for (int j = 0; j < registries.length; j++) {
                    if ( j >= currHp -1) {
                        robots[i].blockRegistry(j);
                    } else {
                        robots[i].unlockRegistry(j);
                    }

                    robots[i].clearUpgrades();

                    if (robots[i].isToBeAsleep()) {
                        robots[i].setAsleep(true);
                    }
                    if (robots[i].isAsleep()) {
                        robots[i].setAsleep(false);
                    }

                    Message m = new Message(Match.MancheEndMsg)
                    m.setParameters(robots);
                    players.values().stream().forEach((conn) -> {
                        try {
                            conn.sendMessage(m);
                        } catch (PartnerShutDownException ex) {
                            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
            }
        }
        this.turn++;
    }

    private void clearAndInstallUpgrades() { 
        for(int i = 0; i < robots.length; i++) {  
            if (!robots[i].isAsleep() && robots[i].isAssigned()) {
                robots[i].clearUpgrades();
                for (Pair p : toBeActivated) { // il robot è in toBeActivated
                    if (p.getFirst() == rb) {
                        robots[i].installUpgrade(p.getSecond());
                    }
                }
            }
        }
    }

    private void moveRobot(RobotMarker rb, InstructionCard istr) {
        int offset = rb.getOffset();
        Position pos = rb.getPosition();
        UpgradePack up = rb.getActiveUpgrade();
        boolean skip = false;

        // Gestione upgrade
        if (up != null && up.getType() == 'B') {
            String name = up.getName();
            if (name == "freni") {
                skip = true;
            } else if (name == "acceleratore") {
                offset += 1;
            } else if (name = "dual core") {
                offset *= 2;
            }
        } else {
            rb.clearUpgrades();
        }

        int row = pos.getRow();
        int col = pos.getCol();
        Direction dir = pos.getDirection();

        while (skip == false && offset > 0) {
            BoardCell theRobodrome.getCell(row,col);
            // Controllo muri nella direzione in cui mi sto spostando
            if (cell instanceof FloorCell && !((FloorCell)cell).hasWall(dir)) {
                if (dir.ordinal() % 2 == 0) { col += dir.ordinal()-1; }
                else { row += dir.ordinal()-2; }

                cell = theRobodrome.getCell(row,col);
                Direction oppDir = Direction.values()[(dir.ordinal()+2)%4];
                // Controllo muri nella direzione da cui arrivo
                if (cell instanceof FloorCell && !((FloorCell)cell).hasWall(oppDir)) {
                    pos.setRow(row);
                    pos.setCol(col);
                    offset--;
                } else {
                    offset = 0;
                }

                aggiornaPosizione(rb, pos);
                cell = theRobodrome.getCell(pos.getRow(), pos.getCol());
                if (checkPitCell(rb, pos) == true) {
                    offset = 0;
                }
            }
        }
    }

    private void turnRobot(RobotMarker rb, InstructionCard istr) {
        Rotation rotation = istr.getRotation();
        UpgradePack up = rb.getActiveUpgrade();
        boolean skip = false;

        // Gestione upgrade
        if (up != null && up.getType() == 'B') {
            String name = up.getName();
            if (name == "freni") {
                skip = true;
            } else if (name == "shift") {
                shift = true;
            } else if (name == "dual core") {
                dual = true;
            }
        } else {
            rb.clearUpgrades();
        }

        Position pos = rb.getPosition();
        Direction dir = pos.getDirection();
        if (!skip && !shift) {
            Direction newDir = Rotation.changeDirection(dir, rotation);
            pos.setDirection(newDir);
            if (dual == true) {
                newDir = Rotation.changeDirection(newDir, rotation);
                pos.setDirection(newDir);

            }
        } else if (!skip && shift == true) {
            int row = pos.getRow();
            int col = pos.getCol();

            BoardCell cell = theRobodrome.getCell(row, col);
            Direction moveDir;
            if (dir.ordinal() % 2 == 0) { //Dir = Est o Ovest
                if (rotation == Rotation.CW90) { // turn left
                    moveDir = Direction.values()[(dir.ordinal()+1)];
                } else if (rotation = Rotation.CCW90) { // turn right
                    moveDir =  Direction.values()[(dir.ordinal()+3)%4];
                }
            } else { // Dir = Nord o Sud
                if (rotation == Rotation.CW90) { // turn left
                    moveDir = Direction.values()[dir.ordinal()-1];
                } else if (rotation = Rotation.CCW90) { // turn right
                    moveDir =  Direction.values()[(dir.ordinal()+1)%4];
                }
            }

            if (cell instanceof FloorCell && ((FloorCell)cell).hasWall(moveDir) == false) {
                if (moveDir.ordinal()%2 == 0) { col += (moveDir.ordinal()-1); }
                else { row += (moveDir.ordinal()-2); }

                cell = theRobodrome.getCell(row, col);
                Direction oppDir = Direction.values()[((dir.ordinal()+2)%4)];
                if (cell instanceof FloorCell && ((FloorCell)cell).hasWall(oppDir) == false) {
                    pos.setRow(row);
                    pos.setCol(col);
                }
            }
        }
        aggiornaPosizione(rb, pos);
        checkPitCell(rb, pos);
    }

    private void backRobot(RobotMarker rb, InstructionCard istr) {
        int offset = istr.getOffset();
        Position pos = rb.getPosition();
        UpgradePack up = rb.getActiveUpgrade();
        boolean skip = false;
        // Gestione upgrade
        if(up != null && up.getType() == 'B') {
            String name = up.getName();
            if (name == "freni") {
                skip = true;
            } else if (name == "retromarcia") {
                offset -= 1;
            } else if (name == "dual core") {
                offset *= 2;
            }
        } else {
            rb.clearUpgrades();
        }
        int row = pos.getRow();
        int col = pos.getCol();
        Direction dir = pos.getDirection();

        while (!skip && offset < 0) {
            BoardCell cell = theRobodrome.getCell(row, col);
            Direction oppDir = Direction.values()[(dir.ordinal()+2)%4];
            if (cell instanceof FloorCell && ((FloorCell)cell).hasWall(oppDir) == false) {
                if (dir.ordinal()%2 == 0) { // est ovest
                    col = col - (dir.ordinal()-1);
                } else { // nord sud
                    row = row - (dir.ordinal()-2);
                }
                cell = theRobodrome.getCell(row,col);
                if (cell instanceof FloorCell && ((FloorCell)cell).hasWall(dir) == false) {
                    pos.setRow(row);
                    pos.setCol(col);
                    offset +=1;
                } else {
                    offset = 0;
                }
                aggiornaPosizione(rb, pos);
                theRobodrome.getCell(pos.getRow(), pos.getCol());
                if (checkPitCell(rb, pos)) {
                    offset = 0;
                }
            }
        }
    }

    private void aggiornaPosizione(RobotMarker rb, Position pos) {
        rb.setPosition(pos);
        Message m = new Message(Match.RobotUpdatePosition);
        m.setParameters(rb);
        players.values().stream().forEach((conn) -> {
            try {
                conn.sendMessage(msg);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
  
    private boolean checkPitCell (RobotMarker rb, Position pos) {
        if (cell.getType() == 'P') {
            int livesLeft = rb.destroy();
            String owner = rb.getOwner();
            Connection conn = getPlayerConnection(owner)
            if (livesLeft <= 0) {
                Message m = new Message(Match.RobotOutOfLivesMsg);
                m.setParameters(rb);
                conn.sendMessage(m);
            }

            Message m = new Message(Match.RobotDestroyedMsg)
            m.setParameters(rb);
            conn.sendMessage(m);
            return true;
        }
        return false;
    }

    private int nextCheckpoint(int[] checkpoints) {
        max = 0;
        for (int c : checkpoints) {
            if (c != 0 && c >= max) {
                max = c;
            }
        }
        return max+1;
    }

    private boolean checkWinningConditions() {
        switch (endGameCondition) {
            case First:
                return !leaderboard.isEmpty();
                break;
            case First3:
                return leaderboard.size() == 3;
                break;
            case AllButLast:
                return leaderboard.size() == (players.size() * nRobotsXPlayer)-1;
                break;
            default:
                return false
                break;
        }
    }

    private void manageLaserUpgrades(boolean bomb, int maxHit, int mul, 
        boolean skipDmg, Direction upgrDir, LaserType laserType, RobotMarker rb,
        ArrayList<registries> swapRegistries, boolean retro) {
        
        Direcrtion faceDir = rb.getPosition().getDirection();
        UpgradePack up = rb.getActiveUpgrade();
        if (up != null && up.getType == 'A') {
            String name = up.getName();
            switch (name) {
                case "bomba":
                    bomb = true;
                    break;
                case "super laser":
                    maxHit = 2;
                    break;
                case "doppio laser":
                    mul = 2;
                    break;
                case "raggio respingente":
                    skipDmg = true;
                    upgrDir = faceDir;
                    laserType = LaserType.PUSH;
                    break;
                case "raggio attrattore":
                    skipDmg = true;
                    upgrDir = Direction.values()[(faceDir.ordinal()+2)%4];
                    laserType = LaserType.PULL;
                    break;
                case "raggio controllante":
                    skipDmg = true;
                    laserType = LaserType.CTRL;
                    swapRegistries = rb.getRegistries();
                    break;
                case "raggio disturbante":
                    skipDmg = true;
                    laserType = LaserType.MESS;
                    break;
                case "retro laser":
                    retro = true;
                    break;
                default:
                    break;
            } else {
                rb.clearUpgrades();
            }
        }
    }

    private void collisionDetection(BoardCell cell, boolean shield,
        boolean collided, LaserType laserType, boolean skipDmg, 
        ArrayList<Registry> swapRegistries, Direction upgrDir) {

        for (int i = 0; i < robots.length; i++) {
            if (robots[i].isAssigned()){
                Position pos = robots[i].getPosition();
                int row = pos.getRow();
                int col = pos.getCol();
                int currHp;

                UpgradePack up = rb.getActiveUpgrade();
                if (up.getName == "scudo") {
                    shield = true;
                }

                if (row == cell.getRow() && col =  cell.getCol()) { // Robot colpito!
                    if (laserType != LaserType.WALL) {
                        if (!shield && !skipDmg) { // Tutto regolare
                            currHp = robots[i].updateHp(-1*mul);
                        } else if (shield && !skipDmg) { // Scudo attivo
                            shield = false;
                        } else if (skipDmg) { // Il laser non danneggia
                            switch (laserType) {
                                case PUSH:
                                case PULL:
                                    cellWithRb = theRobodrome.getCell(row, col);
                                    if (upgrDir.ordinal()%2 == 0) {
                                        col += (upgrDir.ordinal()-1);
                                    } else {
                                        row += (upgrDir.ordinal()-2);
                                    }
                                    Direction oppDir = Direction.values()[(upgrDir.ordinal()+2)%4];
                                    if (!cell.hasWall(oppDir)) {
                                        pos.setRow(row);
                                        pos.setCol(col);
                                    }
                                    aggiornaPosizione(robots[i], pos);
                                    checkPitCell(robots[i], pos);
                                    break;
                                case MESS:
                                    robots[i].randomizeReg();
                                    break;
                                case CTRL:
                                    int j = 0;
                                    for (Registry reg : swapRegistries) {
                                        InstructionCard istr = reg.getInstruction();
                                        robots[i].setRegistryInstr(j, istr);
                                        j++;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    String nickName = robots[i].getOwner();
                    Connection conn = getPlayerConnection(nickName);
                    Message m = new Message(Match.RobotHitMessage);
                    m.setParameters(robots[i], laserType);
                    conn.sendMessage(m);

                    if (currHp <= 0) {
                        Message m2 = new Message(Match.RobotDestroyedMsg);
                        m2.setParameters(robots[i]);
                        conn.sendMessage(m2);
                    }
                    collided = true;
                }
            }
        }
    }

    private void bomb(int row, int col) {
        int startRow = row-2;
        int startCol = col-2;

        for (int c = 0; c<5; c++) {
            for (int r = 0; r<5; r++) {
                for (int i = 0; i<robots.length; i++) {
                    if (robots[i].isAssigned()) {
                        Position pos = robots[i].getPosition();
                        String nickName = robots[i].getOwner();
                        int posRow = pos.getRow();
                        int posCol = pos.getCol()

                        if (posRow == r && posCol == c) { // Robot colpito
                            robots[i].updateHp(-1);
                            Connection conn = getPlayerConnection(nickName);
                            Message m = new Message(Match.RobotHitMessage);
                            m.setParameters(robots[i], "bomba");

                            conn.sendMessage(m);

                            if (currHp <= 0) {
                                Message m2 = new Message(Match.RobotDestroyedMsg);
                                m2.setParameters(robots[i]);
                                conn.sendMessage(m2);
                            }
                        }
                    }
                }
            }
        }
    }
}