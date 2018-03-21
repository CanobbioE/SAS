/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.robodrome;

/**
 *
 * @author claudia
 */
public class RobodromeException extends RuntimeException {

    /**
     * Creates a new instance of <code>RobodromeException</code> without detail
     * message.
     */
    public RobodromeException() {
    }

    /**
     * Constructs an instance of <code>RobodromeException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public RobodromeException(String msg) {
        super(msg);
    }
}
