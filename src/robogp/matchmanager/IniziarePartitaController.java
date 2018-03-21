package robogp.matchmanager;

import connection.Message;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 * GRASP Controller dell'UC Iniziare Partita
 *
 * @author claudia
 */
public class IniziarePartitaController {

    /*  Il controller mantiene una serie di informazioni necessarie
        a svolgere il suo compito.
     */
    private String serverAccessKey;
    private Server theServer;
    private Match theMatch;

    private DefaultListModel<String> requests;

    /* Gestione Pattern Singleton */
    private static IniziarePartitaController singleInstance;

    private IniziarePartitaController() {
    }

    /**
     * @return l'unica istanza esistente del GRASP Controller di IniziarePartita
     */
    public static IniziarePartitaController getInstance() {
        if (IniziarePartitaController.singleInstance == null) {
            IniziarePartitaController.singleInstance = new IniziarePartitaController();
        }
        return IniziarePartitaController.singleInstance;
    }

    public void iniziaCreazionePartita(int port, String key) {
        this.serverAccessKey = key;
        this.theServer = Server.getInstance(port);
    }

    public void creaPartita(String rbdName, int nMaxPlayers, int nRobotsXPlayer, Match.EndGame endGameCond, boolean initUpg) {
        this.theMatch = Match.getInstance(rbdName, nMaxPlayers, nRobotsXPlayer, endGameCond, initUpg);
        requests = new DefaultListModel<>();
        this.theServer.startAcceptingRequests(theMatch);
    }

    public DefaultListModel<String> getRequestList() {
        return this.requests;
    }

    void matchJoinRequestArrived(Message msg) {
        requests.addElement((String) msg.getParameter(0));
    }

    void chiudi() {
        if (this.theMatch != null) {
            if (this.theMatch.getStatus() == Match.State.Started) {
                this.theMatch.stop();
            } else if (this.theMatch.getStatus() == Match.State.Created) {
                this.theMatch.cancel();
            }
        }
        if (this.theServer != null) {
            this.theServer.stop();
        }
    }

    public ArrayList<RobotMarker> getAvailableRobots() {
        return this.theMatch.getAvailableRobots();
    }

    public boolean canAcceptMoreRequests() {
        int nPl = this.theMatch.getPlayerCount();
        int maxPl = this.theMatch.getMaxPlayers();
        int freeRob = this.theMatch.getAvailableRobots().size();
        int robPl = this.theMatch.getRobotsPerPlayer();
        return (freeRob >= robPl && nPl < maxPl);
    }

    public boolean canStartMatch() {
        return (this.theMatch.getPlayerCount() >= 2);
    }

    public void accettaRichiesta(String nickname, List<RobotMarker> selection) {
        if (this.canAcceptMoreRequests()) {
            this.theMatch.addPlayer(nickname, selection);
        }
    }

    public void rifiutaRichiesta(String nickname) {
        this.theMatch.refusePlayer(nickname);
    }

    public void avviaPartita() {
        if (this.canStartMatch()) {
            this.theMatch.start();
        }
    }

    public void annullaPartita() {
        this.theMatch.cancel();
        this.theServer.stop();
    }
}
