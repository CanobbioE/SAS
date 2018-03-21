/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.image;

import robogp.robodrome.Rotation;
import robogp.robodrome.Direction;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * Fornisce la grafica per le caselle del robodromo e per i segnalini che si
 * muovono sul tabellone.
 *
 * @author claudia
 */
public class TileProvider {

    private static TileProvider instance;

    public static TileProvider getTileProvider() {
        if (instance == null) {
            instance = new TileProvider();
            instance.loadTiles();
        }
        return instance;
    }

    private final HashMap<String, BufferedImage> tiles;

    private TileProvider() {
        tiles = new HashMap<>();
    }

    private void loadTiles() {
        try {
            tiles.put("F", ImageIO.read(new File("tiles/F.png")));
            tiles.put("P", ImageIO.read(new File("tiles/P.png")));
            tiles.put("W!0", ImageIO.read(new File("tiles/W.png")));
            tiles.put("WW!0", ImageIO.read(new File("tiles/WW.png")));
            tiles.put("Lh", ImageIO.read(new File("tiles/lbeam.png")));
            tiles.put("Lv", ImageUtil.rotate(ImageIO.read(new File("tiles/lbeam.png")), Rotation.CW90));
            tiles.put("H!0", ImageIO.read(new File("tiles/blast.png")));
            tiles.put("L!0", ImageIO.read(new File("tiles/L.png")));
            tiles.put("P1!0", ImageIO.read(new File("tiles/P1.png")));
            tiles.put("P2!0", ImageIO.read(new File("tiles/P2.png")));
            tiles.put("B-ynn!0", ImageIO.read(new File("tiles/B-ynn.png")));
            tiles.put("B-nyn!0", ImageIO.read(new File("tiles/B-nyn.png")));
            tiles.put("B-nny!0", ImageIO.read(new File("tiles/B-nny.png")));
            tiles.put("B-yyn!0", ImageIO.read(new File("tiles/B-yyn.png")));
            tiles.put("B-yny!0", ImageIO.read(new File("tiles/B-yny.png")));
            tiles.put("B-nyy!0", ImageIO.read(new File("tiles/B-nyy.png")));
            tiles.put("B-yyy!0", ImageIO.read(new File("tiles/B-yyy.png")));
            tiles.put("E-ynn!0", ImageIO.read(new File("tiles/E-ynn.png")));
            tiles.put("E-nyn!0", ImageIO.read(new File("tiles/E-nyn.png")));
            tiles.put("E-nny!0", ImageIO.read(new File("tiles/E-nny.png")));
            tiles.put("E-yyn!0", ImageIO.read(new File("tiles/E-yyn.png")));
            tiles.put("E-yny!0", ImageIO.read(new File("tiles/E-yny.png")));
            tiles.put("E-nyy!0", ImageIO.read(new File("tiles/E-nyy.png")));
            tiles.put("E-yyy!0", ImageIO.read(new File("tiles/E-yyy.png")));
            tiles.put("Rl", ImageIO.read(new File("tiles/Rl.png")));
            tiles.put("Rr", ImageIO.read(new File("tiles/Rr.png")));
            tiles.put("Au", ImageIO.read(new File("tiles/Au.png")));
            tiles.put("Ar", ImageIO.read(new File("tiles/Ar.png")));
            for (int i = 1; i <= 4; i++) {
                tiles.put("Ac" + i, ImageIO.read(new File("tiles/Ac" + i + ".png")));
            }
            for (int i = 1; i <= 8; i++) {
                tiles.put("D" + i, ImageIO.read(new File("tiles/D" + i + ".png")));
            }
            /*tiles.put("robot-blue!0", ImageIO.read(new File("../tiles/robot-blue.png")));
            tiles.put("robot-emerald!0", ImageIO.read(new File("../tiles/robot-emerald.png")));
            tiles.put("robot-green!0", ImageIO.read(new File("../tiles/robot-green.png")));
            tiles.put("robot-orange!0", ImageIO.read(new File("../tiles/robot-orange.png")));
            tiles.put("robot-red!0", ImageIO.read(new File("../tiles/robot-red.png")));
            tiles.put("robot-turquoise!0", ImageIO.read(new File("../tiles/robot-turquoise.png")));
            tiles.put("robot-violet!0", ImageIO.read(new File("../tiles/robot-violet.png")));
            tiles.put("robot-yellow!0", ImageIO.read(new File("../tiles/robot-yellow.png")));*/
        } catch (IOException ex) {

        }
    }

    /**
     * Restituisce l'immagine per un certo tipo di casella.
     *
     * @param name Il nome dell'immagine, che ne identifica il tipo.
     * @return l'immagine corrispondente al nome, null se non esiste.
     */
    public BufferedImage getTile(String name) {
        BufferedImage t = tiles.get(name);
        if (t != null) {
            return t;
        }
        t = tiles.get(name + "!0");
        return t;
    }

    /**
     * Restituisce l'immagine per un certo tipo di casella, ruotata in modo
     * che guardi in una certa direzione.
     * @param name Il nome dell'immagine, che ne identifica il tipo.
     * @param d La direzione in cui deve guardare l'immagine
     * @return l'immagine corrispondente al nome e alla direzione, null se non esiste.
     */
    public BufferedImage getTile(String name, Direction d) {
        int count = 0;
        if (name.startsWith("E") || name.startsWith("B")) {
            switch (d) {
                case W:
                    count = 1;
                    break;
                case N:
                    count = 2;
                    break;
                case E:
                    count = 3;
                    break;
                case S:
                    count = 0;
            }
        } else if (name.startsWith("robot")) {
            switch (d) {
                case E:
                    count = 0;
                    break;
                case N:
                    count = 3;
                    break;
                case W:
                    count = 2;
                    break;
                case S:
                    count = 1;
                    break;
            }
        } else {
            switch (d) {
                case W:
                    count = 0;
                    break;
                case N:
                    count = 1;
                    break;
                case E:
                    count = 2;
                    break;
                case S:
                    count = 3;
            }
        }
        String fullName = name + "!" + count;
        BufferedImage t = tiles.get(fullName);
        if (t != null) {
            return t;
        }
        BufferedImage tmp = tiles.get(name + "!0");
        if (tmp == null) {
            tmp = tiles.get(name);
            return tmp;
        }

        switch (count) {
            case 0:
                t = tmp;
                break;
            case 1:
                t = ImageUtil.rotate(tmp, Rotation.CW90);
                tiles.put(fullName, t);
                break;
            case 2:
                t = ImageUtil.rotate(tmp, Rotation.CW180);
                tiles.put(fullName, t);
                break;
            case 3:
                t = ImageUtil.rotate(tmp, Rotation.CCW90);
                tiles.put(fullName, t);
                break;
        }
        return t;
    }
}
