/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robogp.matchmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import connection.Connection;

public class Server implements Runnable {

    private InetAddress address;
    private int port;
    private ServerSocket serverSock;
    private final ArrayList<Connection> connections;
    private Match currentMatch;

    /* variabili per gestione della chiusura del server
     */
    private boolean shouldClose;
    private boolean closed;
    private final Object closeDownMonitor;


    /* Gestione del pattern Singleton */
    private static Server singleInstance;

    private Server() {
        closed = false;
        closeDownMonitor = new Object();
        connections = new ArrayList<>();
    }

    /**
     * @param port La porta su cui eseguire il Server, se non &grave; stato
     * ancora creato
     * @return L'unica istanza di Server esistente
     * @throws ServerCreationException se non trova 'localhost'
     */
    public static Server getInstance(int port) throws ServerCreationException {
        if (Server.singleInstance == null) {
            Server.singleInstance = new Server();
            InetAddress localhost = null;
            try {
                localhost = InetAddress.getLocalHost();
            } catch (UnknownHostException exc) {
                throw new ServerCreationException("Cannot find localhost. Server creation impossible.");
            }
            Server.singleInstance.address = localhost;
            Server.singleInstance.port = port;
        }
        return Server.singleInstance;
    }

    public void startAcceptingRequests(Match m) {
        this.currentMatch = m;
        (new Thread(this)).start();
    }

    /**
     *
     */
    @Override
    public void run() {
        if (closed) {
            return;
        }
        try {
            serverSock = new ServerSocket(port);
            justStarted();
            System.out.println("Server set up.");
            while (!shouldIClose()) {
                Connection clientConnection = Connection.acceptConnectionRequest(serverSock);
                synchronized (connections) {
                    clientConnection.addMessageObserver(this.currentMatch);
                    connections.add(clientConnection);
                }
            }
            System.out.println("Server is closing down");
            ArrayList<Connection> disc = new ArrayList<>();
            disc.addAll(connections);
            disc.stream().forEach((conn) -> {
                conn.disconnect();
            });
            boolean canClose = false;
            while (!canClose) {
                canClose = true;
                for (Connection conn : connections) {
                    if (!conn.isClosed()) {
                        canClose = false;
                    }
                }
            }
            System.out.println("All helpers stopped");
            setClosed(true);
            serverSock.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*  I metodi qui sotto si occupano tutti di gestire la chiusura del 
        server in modo "graceful", se possibile.
     */
    public void waitOnClose(long timeout) throws InterruptedException {
        synchronized (closeDownMonitor) {
            if ((closeDownMonitor != null) && (!isClosed())) {
                closeDownMonitor.wait(timeout);
            }
        }
    }

    public void shouldClose() {
        boolean ok;
        synchronized (closeDownMonitor) {
            shouldClose = true;
            ok = isClosed();
        }
        // Sveglia se stesso dall'attesa di connessioni
        // stabilendo una P2PConnection con se stesso
        if (ok) {
            return;
        }

        Connection conn = null;
        try {
            conn = Connection.connectToHost(address, port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void justStarted() {
        synchronized (closeDownMonitor) {
            shouldClose = false;
        }
    }

    private boolean shouldIClose() {
        synchronized (closeDownMonitor) {
            return shouldClose;
        }
    }

    private void setClosed(boolean b) {
        synchronized (closeDownMonitor) {
            boolean prev = isClosed();
            closed = b;
            if (!prev) {
                closeDownMonitor.notifyAll();
            }
        }
    }

    public boolean isClosed() {
        boolean ret;
        synchronized (closeDownMonitor) {
            ret = closed;
        }
        return ret;
    }

    public void forceClose() {
        synchronized (closeDownMonitor) {
            if (isClosed()) {
                return;
            }
        }
        this.connections.stream().filter((conn) -> (!conn.isClosed())).forEach((conn) -> {
            conn.forceClose();
        });
        setClosed(true);
    }

    public void stop() {
        this.shouldClose();
        try {
            this.waitOnClose(3000);
        } catch (InterruptedException ex) {

        } finally {
            if (!this.isClosed()) {
                this.forceClose();
            }
        }
    }
}
