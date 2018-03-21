/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import robogp.matchmanager.RobotMarker;
import robogp.robodrome.BoardCell;
import robogp.robodrome.Direction;
import robogp.robodrome.MovableElement;
import robogp.robodrome.Robodrome;
import robogp.robodrome.RobodromeException;
import robogp.robodrome.Rotation;
import robogp.robodrome.animation.Animation;
import robogp.robodrome.animation.LaserFireAnimation;
import robogp.robodrome.animation.LaserHideAnimation;
import robogp.robodrome.animation.PauseAnimation;
import robogp.robodrome.animation.RobotFallAnimation;
import robogp.robodrome.animation.RobotHitAnimation;
import robogp.robodrome.animation.RobotMoveAnimation;
import robogp.robodrome.animation.TransitionAnimation;
import robogp.robodrome.image.ImageUtil;
import robogp.robodrome.image.TileProvider;

/**
 * Questa classe permette di visualizzare un Robodromo e mostrare con semplici
 * animazioni le azioni di gioco.
 *
 * @author claudia
 */
public class RobodromeView extends JComponent {

    private class Play extends Thread {

        private int waitTime;

        @Override
        public void run() {
            waitTime = (int) (1000 / FRAMERATE);
            while (isPlayingAnimation()) {
                try {
                    evolveAnimation();
                    repaint();
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RobodromeView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static final int BORDER = 15;
    private static final int FRAMERATE = 60;

    private final Robodrome drome;
    private final int cellSize;
    private BufferedImage boardImage;

    private int originX;
    private int originY;

    private boolean followingAction = false;
    private boolean dragging = false;
    private boolean playingAnimation = false;
    private Play player;

    private int lastMousePressedX;
    private int lastMousePressedY;
    private int dragOriginX;
    private int dragOriginY;

    private LinkedList<Animation> animationsQueue;
    private boolean transitioning;
    private Animation currentAnimation;

    private final MovableElement[] robotMarkers;
    private MovableElement hitMarker;
    private LaserFireAnimation.LaserState laser;

    private Color laserLightColor = Color.decode("#00C1CF");
    private Color laserDarkColor = Color.decode("#3200FF");
    private Stroke laserExternalStroke = new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private Stroke laserInternalStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private ArrayList<RobodromeAnimationObserver> observers;

    private void startDragging() {
        /*if (followingAction && isPlayingAnimation()) {
            stopFollowingAction();
        }*/
        dragging = true;
        this.dragOriginX = originX;
        this.dragOriginY = originY;
    }

    private void stopDragging() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    /**
     * Imposta la visualizzazione del robodromo in modo che segua
     * automaticamente l'elemento in movimento durante le animazioni, nel caso
     * il robodromo non sia interamente inquadrato.
     */
    public void startFollowingAction() {
        //if (!isDragging()) {
        followingAction = true;
        //}
    }

    /**
     * @return true se la vista &egrave; impostata per seguire automaticamente
     * l'elemento in movimento durante le animazioni.
     */
    public boolean isFollowingAction() {
        return followingAction;
    }

    /**
     * Imposta la visualizzazione in modo che non segua pi&ugrave; l'elemento in
     * movimento durante le animazioni.
     */
    public void stopFollowingAction() {
        if (isFollowingAction() && currentAnimation != null && currentAnimation.isTransition()
                && !((TransitionAnimation) currentAnimation).isCommanded()) {
            currentAnimation = null;
        }
        followingAction = false;
    }

    /**
     * @return true se c'&egrave; un'animazione in riproduzione.
     */
    public boolean isPlayingAnimation() {
        return this.playingAnimation;
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre il movimento (traslazione
     * e/o rotazione) di un robot. Attenzione: non vi &egrave; alcuna verifica
     * sulla "logica" del movimento (ad esempio il robot potrebbe attraversare
     * le pareti).
     *
     * @param robotNum il robot da muovere
     * @param movement il numero di caselle di cui si muove
     * @param dir la direzione del movimento
     * @param rot l'entit&agrave; e la direzione di rotazione
     */
    public void addRobotMove(int robotNum, int movement, Direction dir, Rotation rot) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new RobotMoveAnimation(robotNum, movement, dir, rot));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre il movimento (traslazione
     * e/o rotazione) di un robot, che ne spinge altri con s&eacute;.
     * Attenzione: la "spinta" in questo caso significa semplicemente che gli
     * altri robot si muovono in modo identico a quello principale per quanto
     * riguarda l'aspetto di traslazione. Non vi &egrave; alcuna verifica sulla
     * "logica" del movimento (ad esempio il robot spinto qui specificato
     * potrebbe essere lontano dal robot principale, e si muoverebbe lo stesso).
     *
     * @param robotNum il robot da muovere
     * @param movement il numero di caselle di cui si muove
     * @param dir la direzione del movimento
     * @param rot l'entit&agrave; e la direzione di rotazione
     */
    public void addRobotMove(int robotNum, int movement, Direction dir, Rotation rot, int[] push) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new RobotMoveAnimation(robotNum, movement, dir, rot, push));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre la "caduta" di un robot
     * in un buco nero. Attenzione: non ci sono verifiche sulla "logica" del
     * movimento: il robot precipita anche se non si trova davvero su un buco.
     *
     * @param robotNum Il robot che precipita
     */
    public void addRobotFall(int robotNum) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new RobotFallAnimation(robotNum));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre il lampeggiare di un
     * colpo che indica un danno subito dal robot. Attenzione: non ci sono
     * verifiche sulla "logica" dell'animazione: il robot potrebbe non avere
     * affatto subito danni.
     *
     * @param robotNum il robot che viene colpito
     * @param dir il lato da cui deve essere visualizzato il colpo
     */
    public void addRobotHit(int robotNum, Direction dir) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new RobotHitAnimation(robotNum, dir));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre lo sparo di un laser,
     * dalla casella di partenza a quella di destinazione. L'ultimo laser
     * sparato rimane visibile sino a che non viene attivata un'animazione di
     * tipo "hideLaserFire". Attenzione: non ci sono verifiche sulla "logica"
     * dell'animazione, ossia che lo sparo parta davvero dal robot e arrivi ad
     * una effettiva destinazione.
     *
     * @param robotNum il robot che spara
     * @param dir la direzione in cui spara
     * @param start la casella di partenza dello sparo (la colonna, se lo sparo
     * &egrave; orizzontale; la riga, se lo sparo &egrave; verticale)
     * @param end la casella di arrivo dello sparo (la colonna, se lo sparo
     * &egrave; orizzontale; la riga, se lo sparo &egrave; verticale)
     * @param hitRobot true se lo sparo colpisce un robot nella casella di
     * arrivo
     * @param hitEndWall true se lo sparo colpisce una parete nella casella di
     * arrivo (attenzione: se lo sparo colpisce una parete nella casella
     * successiva a quella di arrivo, questo parametro deve essere false)
     * @see hideLaserFire#addHideLaser
     */
    public void addLaserFire(int robotNum, Direction dir, int start, int end,
            boolean hitRobot, boolean hitEndWall) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new LaserFireAnimation(robotNum, dir, start, end, hitRobot, hitEndWall, cellSize));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre la scomparsa dallo
     * schermo degli spari laser.
     */
    public void addHideLaser() {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new LaserHideAnimation());
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge una pausa all'elenco di animazioni da riprodurre.
     *
     * @param millisec la durata della pausa.
     */
    public void addPause(long millisec) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new PauseAnimation(millisec));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Aggiunge all'elenco di animazioni da riprodurre lo scorrimento della
     * vista per visualizzare un dato robot.
     *
     * @param robotNum il robot da visualizzare
     */
    public void addFocusMove(int robotNum) {
        if (!isPlayingAnimation()) {
            this.animationsQueue.add(new TransitionAnimation(robotMarkers[robotNum].getPosX(),
                    robotMarkers[robotNum].getPosY(), true));
        } else {
            throw new RobodromeException("Cannot add animation steps while playing animation.");
        }
    }

    /**
     * Mette un segnalino robot sul robodromo, nella posizione indicata.
     * Attenzione: non pu&ograve; essere invocato quando c'&egrave;
     * un'animazione in corso.
     *
     * @param robotNum il numero assegnato al robot da inserire (da 0 ad N-1,
     * dove N &egrave; il numero di robot specificati alla creazione del
     * robodromo
     * @param name il nome del robot, scelto fra "red", "blue", "yellow",
     * green", "emerald", "orange", "violet", "turquoise"; il nome identifica
     * quale segnalino verr&agrave; usato
     * @param dir la direzione in cui guarda il robot inizialmente
     * @param row la riga a cui si posiziona il robot
     * @param col la colonna a cui si posiziona il robot
     * @param visible true se il robot &egrave; inizialmente visibile
     */
    public void addRobot(RobotMarker robot, Direction dir, int row, int col, boolean visible) {
        if (!this.isPlayingAnimation()) {
            robotMarkers[robot.getDock()] = new MovableElement(robot.getImage(cellSize));
            robotMarkers[robot.getDock()].setBoardPosition(row, col);
            robotMarkers[robot.getDock()].setPosX(col * cellSize + cellSize / 2 + BORDER);
            robotMarkers[robot.getDock()].setPosY(row * cellSize + cellSize / 2 + BORDER);
            robotMarkers[robot.getDock()].setDirection(dir);
            robotMarkers[robot.getDock()].setVisible(visible);
            repaint();
        } else {
            throw new RobodromeException("Cannot add robot during animation.");
        }
    }

    /**
     * Posiziona sul robodromo un robot gi&agrave; precedentemente inserito con
     * addRobot. Attenzione: non pu&ograve; essere invocato quando c'&egrave;
     * un'animazione in corso.
     *
     * @param robotNum il robot da posizionare
     * @param dir la direzione in cui guarda il robot inizialmente
     * @param row la riga a cui si posiziona il robot
     * @param col la colonna a cui si posiziona il robot
     * @param visible true se il robot &egrave; visibile
     * @see addRobot
     */
    public void placeRobot(RobotMarker robot, Direction dir, int row, int col, boolean visible) {
        if (!this.isPlayingAnimation()) {
            int robotNum = robot.getDock();
            if (robotNum >= 0 && robotNum < robotMarkers.length && robotMarkers[robotNum] != null) {
                robotMarkers[robotNum].setDirection(dir);
                robotMarkers[robotNum].setBoardPosition(row, col);
                robotMarkers[robotNum].setPosX(col * cellSize + cellSize / 2 + BORDER);
                robotMarkers[robotNum].setPosY(row * cellSize + cellSize / 2 + BORDER);
                robotMarkers[robotNum].setVisible(visible);
                robotMarkers[robotNum].resetImageSize(cellSize, cellSize);
                repaint();
            } else {
                throw new RobodromeException("Invalid robot number.");
            }
        } else {
            throw new RobodromeException("Cannot place robot during animation.");
        }
    }

    /**
     * Crea una vista per un robodromo
     *
     * @param rd il robodromo da visualizzare
     * @param cellSize la dimensione del lato di una cella, in pixel
     * @param totRobots il numero di robot che il robodromo accetta
     */
    public RobodromeView(Robodrome rd, int cellSize, int totRobots) {
        this.drome = rd;
        this.cellSize = cellSize;
        robotMarkers = new MovableElement[totRobots];

        buildBoardImage();

        originX = 0;
        originY = 0;

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                onResize(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        this.setMinimumSize(new Dimension(cellSize, cellSize));
        //this.setMaximumSize(new Dimension(boardImage.getWidth(), boardImage.getHeight()));
        this.setBackground(Color.black);

        this.animationsQueue = new LinkedList<>();

        hitMarker = new MovableElement(TileProvider.getTileProvider().getTile("H", Direction.W));
        hitMarker.setVisible(false);
        laser = new LaserFireAnimation.LaserState();
        laser.setVisible(false);

        observers = new ArrayList<>();
    }

    private void buildBoardImage() {
        int contentW = this.drome.getColumnCount() * cellSize + BORDER * 2;
        int contentH = this.drome.getRowCount() * cellSize + BORDER * 2;
        boardImage = new BufferedImage(contentW, contentH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgG2 = (Graphics2D) boardImage.getGraphics();

        for (int r = 0; r < this.drome.getRowCount(); r++) {
            for (int c = 0; c < this.drome.getColumnCount(); c++) {
                BoardCell bc = this.drome.getCell(r, c);
                BufferedImage cellImg = bc.getImage();
                BufferedImage baseCell = ImageUtil.scale(cellImg, cellSize, cellSize);
                imgG2.drawImage(baseCell, BORDER + c * cellSize, BORDER + r * cellSize, this);
            }
        }

    }

    /**
     * D&agrave; inizio alla riproduzione delle animazioni inserite in coda.
     */
    public void play() {
        if (!isPlayingAnimation() && !this.animationsQueue.isEmpty()) {
            this.currentAnimation = null;
            playingAnimation = true;
            /*if (this.isFollowingAction()) {
                int nextRobot = this.animationsQueue.peek().getWhich();
                TransitionAnimation ani = new TransitionAnimation(robotMarkers[nextRobot].getPosX(),
                        robotMarkers[nextRobot].getPosY(), false);
                this.animationsQueue.push(ani);
            }*/
            this.player = new Play();
            for (RobodromeAnimationObserver obs : observers) {
                obs.animationStarted();
            }
            this.player.start();
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.black);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(boardImage, -originX, -originY, this);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (laser.isVisible()) {
            g2.setPaint(this.laserLightColor);
            float circleR = laser.getCircleRadius() * 1.5f;
            g2.fillOval((int) (laser.getCircleX() - originX - circleR),
                    (int) (laser.getCircleY() - originY - circleR),
                    (int) circleR * 2, (int) circleR * 2);
            if (laser.isFired()) {
                g2.setStroke(this.laserExternalStroke);
                g2.drawLine(laser.getCircleX() - originX, laser.getCircleY() - originY,
                        laser.getCurrentX() - originX, laser.getCurrentY() - originY);
            }
            g2.setPaint(this.laserDarkColor);
            circleR = laser.getCircleRadius();
            g2.fillOval((int) (laser.getCircleX() - originX - circleR),
                    (int) (laser.getCircleY() - originY - circleR), (int) circleR * 2, (int) circleR * 2);
            if (laser.isFired()) {
                g2.setStroke(this.laserInternalStroke);
                g2.drawLine(laser.getCircleX() - originX, laser.getCircleY() - originY,
                        laser.getCurrentX() - originX, laser.getCurrentY() - originY);
            }
        }
        for (int i = 0; i < robotMarkers.length; i++) {
            MovableElement m = robotMarkers[i];
            if (m != null && m.isVisible()) {
                if (m.getRotation() != 0) {
                    AffineTransform at = new AffineTransform();
                    at.translate(m.getPosX() - originX, m.getPosY() - originY);
                    at.rotate(m.getRotation());
                    at.translate(-m.getImage().getWidth() / 2,
                            -m.getImage().getHeight() / 2);
                    g2.drawImage(m.getImage(), at, this);
                } else {
                    g2.drawImage(m.getImage(), m.getPosX() - m.getImage().getWidth() / 2 - originX,
                            m.getPosY() - m.getImage().getHeight() / 2 - originY, this);
                }
            }
        }

        if (hitMarker.isVisible()) {
            g2.drawImage(hitMarker.getImage(), hitMarker.getPosX() - originX - hitMarker.getImage().getWidth() / 2,
                    hitMarker.getPosY() - originY - hitMarker.getImage().getHeight() / 2, this);
        }

        drawBorder(g2);
    }

    private void drawBorder(Graphics2D g2) { // DA SISTEMARE
        g2.setPaint(Color.black);
        g2.fillRect(-originX, -originY, BORDER, boardImage.getHeight());
        g2.fillRect(-originX, -originY, boardImage.getWidth(), BORDER);
        g2.fillRect(-originX, boardImage.getHeight() - BORDER - originY, boardImage.getWidth(), BORDER);
        g2.fillRect(boardImage.getWidth() - BORDER - originX, -originY, BORDER, boardImage.getHeight());
        int emptyX = this.getWidth() - (boardImage.getWidth() - originX);
        int emptyY = this.getHeight() - (boardImage.getHeight() - originY);
        g2.fillRect(boardImage.getWidth() - originX, 0, emptyX, this.getHeight());
        g2.fillRect(0, boardImage.getHeight() - originY, this.getWidth(), emptyY);
    }

    private void onResize(ComponentEvent e) {
        int gapX = this.getWidth() - (boardImage.getWidth() - originX);
        int gapY = this.getHeight() - (boardImage.getHeight() - originY);
        if (gapX > 0) {
            originX = Math.max(0, originX - gapX);
        }
        if (gapY > 0) {
            originY = Math.max(0, originY - gapY);
        }
        this.repaint();
    }

    private void onMouseDragged(MouseEvent e) {
        if (!isDragging()) {
            startDragging();
        }
        int shiftX = e.getX() - this.lastMousePressedX;
        int shiftY = e.getY() - this.lastMousePressedY;
        originX = Math.max(0, Math.min(this.dragOriginX - shiftX, boardImage.getWidth() - this.getWidth()));
        originY = Math.max(0, Math.min(this.dragOriginY - shiftY, boardImage.getHeight() - this.getHeight()));
        repaint();
    }

    private void onMousePressed(MouseEvent e) {
        this.lastMousePressedX = e.getX();
        this.lastMousePressedY = e.getY();
    }

    private void onMouseReleased(MouseEvent e) {
        if (isDragging()) {
            stopDragging();
        }
    }

    private void evolveAnimation() {
        if (isPlayingAnimation()) {
            if (currentAnimation == null) { // step to next animation
                if (animationsQueue.isEmpty()) {
                    for (RobodromeAnimationObserver obs : observers) {
                        obs.animationFinished();
                    }
                    this.playingAnimation = false;
                } else { // queue not empty 
                    if (this.isFollowingAction() && !transitioning) {
                        int watch = animationsQueue.peek().getWhich();
                        if (watch >= 0) {
                            currentAnimation = new TransitionAnimation(robotMarkers[watch].getPosX(),
                                    robotMarkers[watch].getPosY(), false);
                            transitioning = true;
                        } else {
                            currentAnimation = animationsQueue.poll();
                            transitioning = false;
                        }
                    } else {
                        currentAnimation = animationsQueue.poll();
                        transitioning = false;
                    }
                    currentAnimation.setStartTime(System.currentTimeMillis());
                    currentAnimation.setLastTime(currentAnimation.getStartTime());
                    startAnimation();
                }
            }

            if (currentAnimation != null) { // continue animation
                long currtime = System.currentTimeMillis();
                long elapsed = currtime - currentAnimation.getStartTime();
                long step = currtime - currentAnimation.getLastTime();
                if (currentAnimation.hasFinished(elapsed)) {
                    finishAnimation(elapsed, step);
                    currentAnimation = null;
                } else {
                    stepAnimation(elapsed, step);
                    currentAnimation.setLastTime(currtime);
                }
            }
        }

    }

    private void startAnimation() {
        System.out.println("STARTING " + currentAnimation);
        if (currentAnimation.isRobotMove()) {
            RobotMoveAnimation ani = (RobotMoveAnimation) currentAnimation;
            ani.setStartPosition(robotMarkers[ani.getWhich()].getPosX(),
                    robotMarkers[ani.getWhich()].getPosY());
        } else if (currentAnimation.isRobotFall()) {
            RobotFallAnimation ani = (RobotFallAnimation) currentAnimation;
            ani.setStartSize(cellSize);
        } else if (currentAnimation.isRobotHit()) {
            RobotHitAnimation ani = (RobotHitAnimation) currentAnimation;
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            this.hitMarker = new MovableElement(ImageUtil.scale(
                    TileProvider.getTileProvider().getTile("H", ani.getDirection()), cellSize, cellSize));
            this.hitMarker.setBoardPosition(theRobot.getRowPos(), theRobot.getColPos());
            this.hitMarker.setPosX(theRobot.getPosX() + (int) (cellSize * ani.getShiftX()));
            this.hitMarker.setPosY(theRobot.getPosY() + (int) (cellSize * ani.getShiftY()));
            this.hitMarker.setDirection(ani.getDirection());
            this.hitMarker.setVisible(true);
            ani.adjustPhase();
        } else if (currentAnimation.isLaserFire()) {
            LaserFireAnimation ani = (LaserFireAnimation) currentAnimation;
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            int endLen = (int) (cellSize * (ani.shouldHitRobot() ? 0.5 : (ani.shouldHitEndWall() ? 0.92 : 1)));
            switch (ani.getDirection()) {
                case E:
                    laser.setCirclePosition(BORDER + ani.getStartCell() * cellSize + cellSize,
                            BORDER + theRobot.getRowPos() * cellSize + cellSize / 2);
                    ani.setStartPosition(laser.getCircleX(), laser.getCircleY());
                    ani.setEndPosition(BORDER + ani.getEndCell() * cellSize + endLen,
                            laser.getCircleY());
                    break;
                case W:
                    laser.setCirclePosition(BORDER + ani.getStartCell() * cellSize,
                            BORDER + theRobot.getRowPos() * cellSize + cellSize / 2);
                    ani.setStartPosition(laser.getCircleX(), laser.getCircleY());
                    ani.setEndPosition(BORDER + ani.getEndCell() * cellSize + (cellSize - endLen),
                            laser.getCircleY());
                    break;
                case N:
                    laser.setCirclePosition(BORDER + theRobot.getColPos() * cellSize + cellSize / 2,
                            BORDER + ani.getStartCell() * cellSize);
                    ani.setStartPosition(laser.getCircleX(), laser.getCircleY());
                    ani.setEndPosition(laser.getCircleX(),
                            BORDER + ani.getEndCell() * cellSize + (cellSize - endLen));
                    break;
                case S:
                    laser.setCirclePosition(BORDER + theRobot.getColPos() * cellSize + cellSize / 2,
                            BORDER + ani.getStartCell() * cellSize + cellSize);
                    ani.setStartPosition(laser.getCircleX(), laser.getCircleY());
                    ani.setEndPosition(laser.getCircleX(),
                            BORDER + ani.getEndCell() * cellSize + endLen);
                    break;
            }
            laser.setCircleRadius(0);
            laser.setCurrentPosition(laser.getCircleX(), laser.getCircleY());
            laser.setFired(false);
            laser.setVisible(true);
        } else if (currentAnimation.isTransition()) { // TO DO START TRANSITION
            TransitionAnimation ani = (TransitionAnimation) currentAnimation;
            ani.setBoundary(this.getWidth(), this.getHeight(), boardImage.getWidth(), boardImage.getHeight(), cellSize);
            /*ani.startOriginX = originX;
            ani.startOriginY = originY;*/
            int tX, tY;
            if (ani.getBoundary().getLeft() < originX) {
                tX = ani.getBoundary().getLeft();
            } else if (ani.getBoundary().getRight() > (originX + this.getWidth())) {
                tX = ani.getBoundary().getRight() - this.getWidth();
            } else {
                tX = originX;
            }
            if (ani.getBoundary().getTop() < originY) {
                tY = ani.getBoundary().getTop();
            } else if (ani.getBoundary().getBottom() > (originY + this.getHeight())) {
                tY = ani.getBoundary().getBottom() - this.getHeight();
            } else {
                tY = originY;
            }
            ani.setCurrentOrigin(originX, originY);
            ani.setTargetOrigin(tX, tY);
            /*int diffX = ani.targetOriginX - ani.startOriginX;
            int diffY = ani.targetOriginY - ani.startOriginY;
            double distance = Math.sqrt(diffX * diffX + diffY * diffY) / cellSize;
            ani.duration = (int) (distance * cellMovementTime) / 2;*/
            ani.setSpeed(cellSize);
        } else if (currentAnimation.isLaserHide()) {
            laser.setVisible(false);
        }
    }

    private void finishAnimation(long elapsed, long step) {
        if (currentAnimation.isRobotMove()) {
            RobotMoveAnimation ani = (RobotMoveAnimation) currentAnimation;
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            if (ani.getMovement() > 0) {
                int diffRow = 0;
                int diffCol = 0;
                switch (ani.getDirection()) {
                    case W:
                        diffCol = -ani.getMovement();
                        break;
                    case E:
                        diffCol = ani.getMovement();
                        break;
                    case N:
                        diffRow = -ani.getMovement();
                        break;
                    case S:
                        diffRow = ani.getMovement();
                        break;
                }
                theRobot.setBoardPosition(theRobot.getRowPos() + diffRow, theRobot.getColPos() + diffCol);
                for (int r : ani.getPushRobots()) {
                    robotMarkers[r].setBoardPosition(robotMarkers[r].getRowPos() + diffRow,
                            robotMarkers[r].getColPos() + diffCol);
                }
                theRobot.setPosX(theRobot.getColPos() * cellSize + cellSize / 2 + BORDER);
                theRobot.setPosY(theRobot.getRowPos() * cellSize + cellSize / 2 + BORDER);
                for (int r : ani.getPushRobots()) {
                    robotMarkers[r].setPosX(robotMarkers[r].getColPos() * cellSize + cellSize / 2 + BORDER);
                    robotMarkers[r].setPosY(robotMarkers[r].getRowPos() * cellSize + cellSize / 2 + BORDER);
                }
                if (this.isFollowingAction()) { // camera needs to follow movement
                    cameraOn(theRobot.getPosX(), theRobot.getPosY());
                }
            }
            if (ani.getRotation() != Rotation.NO) {
                theRobot.setRotation(0);
                theRobot.setDirection(Rotation.changeDirection(theRobot.getDirection(), ani.getRotation()));
                theRobot.resetImageSize(cellSize, cellSize);
            }
        } else if (currentAnimation.isRobotFall()) {
            RobotFallAnimation ani = (RobotFallAnimation) currentAnimation;
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            theRobot.setVisible(false);
            theRobot.resetImageSize(cellSize,cellSize);
        } else if (currentAnimation.isRobotHit()) {
            hitMarker.setVisible(false);
        } else if (currentAnimation.isLaserFire()) {
            LaserFireAnimation ani = (LaserFireAnimation) currentAnimation;
            laser.setCurrentPosition(ani.getEndX(), ani.getEndY());
        } else if (currentAnimation.isTransition()) {
            TransitionAnimation ani = (TransitionAnimation) currentAnimation;
            originX = ani.getTargetOriginX();
            originY = ani.getTargetOriginY();
            ani.setCurrentOrigin(originX, originY);
        }
    }

    private void stepAnimation(long elapsed, long step) {
        if (currentAnimation.isRobotMove()) {
            RobotMoveAnimation ani = (RobotMoveAnimation) currentAnimation;
            float timeFraction = elapsed / (float) currentAnimation.getDuration();
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            if (ani.getMovement() > 0) {
                int realmove = (int) (cellSize * ani.getMovement() * timeFraction);
                int oldPosX = theRobot.getPosX();
                int oldPosY = theRobot.getPosY();
                switch (ani.getDirection()) {
                    case W:
                        theRobot.setPosX(ani.getStartX() - realmove);
                        break;
                    case E:
                        theRobot.setPosX(ani.getStartX() + realmove);
                        break;
                    case N:
                        theRobot.setPosY(ani.getStartY() - realmove);
                        break;
                    case S:
                        theRobot.setPosY(ani.getStartY() + realmove);
                        break;
                }
                int diffX = theRobot.getPosX() - oldPosX;
                int diffY = theRobot.getPosY() - oldPosY;
                for (int r : ani.getPushRobots()) {
                    robotMarkers[r].setPosX(robotMarkers[r].getPosX() + diffX);
                    robotMarkers[r].setPosY(robotMarkers[r].getPosY() + diffY);
                }
                if (this.isFollowingAction()) { // camera needs to follow movement
                    cameraOn(theRobot.getPosX(), theRobot.getPosY());
                }
            }
            if (ani.getRotation() != Rotation.NO) {
                theRobot.setRotation((float) Math.toRadians(
                        ani.getRotation().getDegrees() * timeFraction) * ani.getRotation().getWise());
            }
        } else if (currentAnimation.isRobotFall()) {
            RobotFallAnimation ani = (RobotFallAnimation) currentAnimation;
            float timeFraction = (float) elapsed / (float) currentAnimation.getDuration();
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            float currdeg = (RobotFallAnimation.DEGREES * timeFraction) % 360;
            float rad = (float) Math.toRadians(currdeg);
            theRobot.setRotation(rad);
            int currSize = (int) (ani.getStartSize() * (1 - timeFraction));
            if (currSize > 0) {
                theRobot.resizeImage(currSize);
            } else {
                theRobot.setVisible(false);
            }

        } else if (currentAnimation.isRobotHit()) {
            RobotHitAnimation ani = (RobotHitAnimation) currentAnimation;
            int phaseNum = (int) (elapsed / ani.getPhase() + 1);
            boolean on = (phaseNum % 2) == 1;
            hitMarker.setVisible(on);
        } else if (currentAnimation.isLaserFire()) {
            LaserFireAnimation ani = (LaserFireAnimation) currentAnimation;
            MovableElement theRobot = robotMarkers[ani.getWhich()];
            boolean isHeating = (elapsed < LaserFireAnimation.HEATTIME);
            if (isHeating) {
                float heatFraction = elapsed / (float) LaserFireAnimation.HEATTIME;
                laser.setFired(false);
                laser.setCircleRadius((int) (ani.getMaxHeatRadius() * heatFraction));
                if (this.isFollowingAction()) {
                    // move center of bounding box from firing robot to laser start point
                    int bbx = theRobot.getPosX() + (int) ((laser.getCircleX() - theRobot.getPosX()) * heatFraction);
                    int bby = theRobot.getPosY() + (int) ((laser.getCircleY() - theRobot.getPosY()) * heatFraction);
                    this.cameraOn(bbx, bby);
                }
            } else {
                float fireFraction = (elapsed - LaserFireAnimation.HEATTIME) / (float) (ani.getDuration() - LaserFireAnimation.HEATTIME);
                laser.setFired(true);
                int x = laser.getCircleX() + (int) ((ani.getEndX() - ani.getStartX()) * fireFraction);
                int y = laser.getCircleY() + (int) ((ani.getEndY() - ani.getStartY()) * fireFraction);
                laser.setCurrentPosition(x, y);
                if (this.isFollowingAction()) {
                    // move center of bounding box following laser end point
                    this.cameraOn(laser.getCurrentX(), laser.getCurrentY());
                }
            }
        } else if (currentAnimation.isTransition()) {
            TransitionAnimation ani = (TransitionAnimation) currentAnimation;
            int diffX = ani.getTargetOriginX() - originX;
            int diffY = ani.getTargetOriginY() - originY;
            float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);
            float covered = step * ani.getSpeed();
            float perc = Math.min(covered / distance, 1.0f);
            originX = ani.getCurrentOriginX() + (int) ((ani.getTargetOriginX() - ani.getCurrentOriginX()) * perc);
            originY = ani.getCurrentOriginY() + (int) ((ani.getTargetOriginY() - ani.getCurrentOriginY()) * perc);
            ani.setCurrentOrigin(originX, originY);
        }
    }

    private void cameraOn(int x, int y) {
        TransitionAnimation.BoundingRect bound = new TransitionAnimation.BoundingRect(
                x, y, this.getWidth(), this.getHeight(),
                boardImage.getWidth(), boardImage.getHeight(), cellSize);
        if (bound.getLeft() < originX) {
            originX = bound.getLeft();
        } else if (bound.getRight() > (originX + this.getWidth())) {
            originX = bound.getRight() - this.getWidth();
        }
        if (bound.getTop() < originY) {
            originY = bound.getTop();
        } else if (bound.getBottom() > (originY + this.getHeight())) {
            originY = bound.getBottom() - this.getHeight();
        }
    }

    /**
     * Aggiunge un Animation Observer al robodromo, che ricever&agrave;
     * notifiche sull'inizio e la fine dell'animazione.
     *
     * @param obs l'observer da aggiungere
     */
    public void addObserver(RobodromeAnimationObserver obs) {
        observers.add(obs);
    }

    /**
     * Rimuove un Animation Observer dal robodromo, in modo che non riceva
     * pi&ugrave; notifiche.
     *
     * @param obs l'observer da rimuovere
     */
    public void removeObserver(RobodromeAnimationObserver obs) {
        observers.remove(obs);
    }
}
