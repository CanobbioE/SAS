/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome.image;

import robogp.robodrome.Rotation;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Una classe di utilities per le immagini del robodromo.
 * @author claudia
 */
public class ImageUtil {

    /**
     * Restituisce una versione ruotata dell'immagine di partenza.
     * @param src L'immagine di partenza
     * @param r La rotazione
     * @return L'immagine ruotata
     */
    public static BufferedImage rotate(BufferedImage src, Rotation r) {
        BufferedImage result = new BufferedImage(src.getHeight(), src.getWidth(), BufferedImage.TYPE_INT_ARGB);
        switch (r) {
            case CW90:
            case CCW270:
                for (int i = 0; i < src.getWidth(); i++) {
                    for (int j = 0; j < src.getHeight(); j++) {
                        result.setRGB(result.getWidth() - j - 1, i, src.getRGB(i, j));
                    }
                }
                break;
            case CW180:
            case CCW180:
                for (int i = 0; i < src.getWidth(); i++) {
                    for (int j = 0; j < src.getHeight(); j++) {
                        result.setRGB(result.getWidth() - i - 1, result.getHeight() - j - 1, src.getRGB(i, j));
                    }
                }
                break;
            case CW270:
            case CCW90:
                for (int i = 0; i < src.getWidth(); i++) {
                    for (int j = 0; j < src.getHeight(); j++) {
                        result.setRGB(j, result.getHeight() - i - 1, src.getRGB(i, j));
                    }
                }
                break;
        }
        return result;
    }

    /** Sovrappone un'immagine ad un altra.
     * @param img1 L'immagine che "riceve" la sovrapposizione. Se null, viene creata una nuova immagine a sfondo trasparente.
     * @param img2 L'immagine da sovapporre
     * @return La prima immagine con la seconda sovrapposta
     */
    public static BufferedImage superImpose(BufferedImage img1, BufferedImage img2) {
        if (img2 == null) {
            return img1;
        }
        if (img1 == null) {
            img1 = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g2 = (Graphics2D)img1.getGraphics();
        /*g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);*/
        g2.drawImage(img2, 0, 0, null);
        return img1;
    }
    
    /**
     * Restituisce una versione ridimensionata dell'immagine di partenza.
     * @param img L'immagine di partenza
     * @param width La nuova larghezza
     * @param height La nuova altezza
     * @return L'immagine ridimensionata
     */
    public static BufferedImage scale(BufferedImage img, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)scaled.getGraphics();
        g2.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH),0,0,null);
        return scaled;
    }
}
