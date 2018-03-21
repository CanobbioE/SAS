package connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Questa classe rappresenta una connessione, indipendentemente dal ruolo di 
 * Client o Server. Solo inizialmente
 * serve sapere chi &egrave; il Client (colui che richiede la connessione) e chi
 * il Server (colui che la concede) per gestire l'handshake iniziale.
 *
 * @author picardi
 */
public class Connection {

	private class ConnectionHelper implements Runnable {

		@Override
		public void run() {
			Message lastMsg = null;
			do {
				try {
					lastMsg = (Message) sockIn.readObject();
					System.out.println("Thread: " + Thread.currentThread() + ", Received Message: " + lastMsg.getName());
					//System.out.println((serverSide ? "Server: " : "Client: ") + "Received message: " + lastMsg.getName());
					if (lastMsg.isByeMessage() && !isClosing()) {
						//System.out.println("Current thread writing message: " + Thread.currentThread().toString());
						//System.out.println("Current output stream: " + sockOut.toString());
						try {
						sendMessage(Message.createByeMessage());
						} catch (PartnerShutDownException ex) {
							// Non fa nulla
							// Tanto si stava chiudendo
						}
						setClosing(true);
						//Thread.sleep(10000);
					} else {
						updateObservers(lastMsg);
					}
				} catch (IOException | ClassNotFoundException ex) {
					//System.out.println("Exception in " + (serverSide ? "Server: " : "Client: "));
					ex.printStackTrace();
				}
				// TODO in Step3: DO SOMETHING WITH MESSAGE
			} while (!isClosing());
			doClose();
		}
	}

	private final Socket mySocket;
	private final boolean serverSide;
	private ObjectInputStream sockIn;
	private ObjectOutputStream sockOut;
	private ConnectionHelper myHelper;
	private boolean closed;
	private boolean closing;
	private final ArrayList<MessageObserver> messageObservers;
	private final ArrayList<Message> unreadMessages;

	private Connection(Socket sock, boolean serverside) {
		mySocket = sock;
		this.serverSide = serverside;
		closed = false;
		messageObservers = new ArrayList<>();
		unreadMessages = new ArrayList<>();
	}

	private void doClose() {
		try {
			//System.out.println("Current thread closing: " + Thread.currentThread().toString());
			//System.out.println("Closing output stream: " + sockOut.toString());
			if (serverSide) {
				sockOut.close();
				sockIn.close();
			} else {
				sockIn.close();
				sockOut.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				mySocket.close();
			} catch (IOException ex) {
				Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
			}
			setClosed();
		}
	}

	public synchronized void sendMessage(Message msg) throws PartnerShutDownException {
		try {
			sockOut.writeObject(msg);
		} catch (SocketException ex) {
			throw new PartnerShutDownException("Connection closed");
		} catch (IOException ex) {
			Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void connect() throws IOException, ServerConnectionException {
		if (serverSide) {
			sockIn = new ObjectInputStream(mySocket.getInputStream());
			sockOut = new ObjectOutputStream(mySocket.getOutputStream());
			boolean ok = handshake();
			if (!ok) {
				throw new ServerConnectionException("Could not handshake");
			}
		} else {// clientside 
			sockOut = new ObjectOutputStream(mySocket.getOutputStream());
			sockIn = new ObjectInputStream(mySocket.getInputStream());
			boolean ok = handshake();
			if (!ok) {
				throw new ClientConnectionException("Could not handshake");
			}
		}
		myHelper = new ConnectionHelper();
		(new Thread(myHelper)).start();
	}

	private boolean handshake() throws IOException {
		boolean ok = true;
		if (serverSide) {
			try {
				Message helloMsg = (Message) sockIn.readObject();
				if (helloMsg.isHelloMessage()) {
					Message reply = Message.createHelloMessage();
					sockOut.writeObject(reply);
				} else {
					ok = false;
				}
			} catch (ClassNotFoundException ex) {
				ok = false;
			}

			if (!ok) {
				System.out.println("Handshake did not work out.");
				doClose();
			}
		} else {
			try {
				// clientside
				Message helloMsg = Message.createHelloMessage();
				sockOut.writeObject(helloMsg);
				Message rep = (Message) sockIn.readObject();
				if (!rep.isHelloMessage()) {
					ok = false;
				}
			} catch (ClassNotFoundException ex) {
				ok = false;
			}
			if (!ok) {
				System.out.println("Handshake did not work out.");
				doClose();
			}
		}
		return ok;
	}

	/**
	 * Accept a new connection from a client. In order for the connection to
	 * work, the client should create an output and an input stream (in this
	 * order).
	 *
	 * @param serverSock the server Socket
	 * @return the Connection with the new client
	 * @throws IOException, ServerConnectionException
	 */
	public static Connection acceptConnectionRequest(ServerSocket serverSock) throws IOException, ServerConnectionException {
		Socket clientsock = serverSock.accept();
		Connection p2p = new Connection(clientsock, true);
		p2p.connect();
		return p2p;
	}

	/**
	 * TODO
	 *
	 * @param address
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public static Connection connectToHost(InetAddress address, int port) throws IOException {
		Socket clientsock = new Socket(address, port);
		Connection p2p = new Connection(clientsock, false);
		p2p.connect();
		return p2p;
	}

	/**
	 * Chiede alla connessione di chiudersi. Essa manda un Bye message sul
	 * canale (vedasi Message). Ci&ograve; che effettivamente chiude
	 * per&ograve; &egrave; la ricezione di una risposta al Bye. Se qualcosa va
	 * storto la connessione viene chiusa bruscamente richiamando forceClose.
	 */
	public synchronized void disconnect() {
		try {
			setClosing(true);
			sockOut.writeObject(Message.createByeMessage());
		} catch (IOException ex) {
			System.out.println("Could not close down gracefully.");
			forceClose();
		}
	}

	/**
	 * Dice se la connessione &egrave; chiusa.
	 *
	 * @return true se la connessione &egrave; chiusa, false altrimenti.
	 */
	public synchronized boolean isClosed() {
		return closed;
	}

	private synchronized void setClosed() {
		closed = true;
	}

	private synchronized boolean isClosing() {
		return closing;
	}

	private synchronized void setClosing(boolean b) {
		closing = b;
	}

	/**
	 * Forza la chiusura brusca e improvvisa della connessione.
	 *
	 */
	public synchronized void forceClose() {
		if (closed) {
			return;
		}
		doClose();
		closed = true;
	}

	/**
	 * Aggiunge un osservatore della classe MessageObserver che verr&agrave;
	 * notificato quando arriva un messaggio del tipo richiesto.
	 *
	 * @param obs l'oggetto che vuole essere notificato dell'arrivo del
	 * messaggio
	 */
	public synchronized void addMessageObserver(MessageObserver obs) {
		messageObservers.add(obs);
		if (!unreadMessages.isEmpty()) {
                    unreadMessages.stream().forEach((msg) -> {
                        obs.notifyMessageReceived(msg);
                    });
			unreadMessages.clear();
		}
	}

	/**
	 * Rimuove un'istanza della classe MessageReceiver dalla lista degli
	 * osservatori dei messaggi con nome <em>messageName</em>. Tale istanza non
	 * verr&agrave; pi&ugrave; notificata.
	 *
	 * @param obs l'oggetto che vuole essere rimosso dalla lista degli
	 * osservatori
	 */
	public synchronized void removeMessageObserver(MessageObserver obs) {
		messageObservers.remove(obs);
	}

	private synchronized void updateObservers(Message msg) {
		msg.setSenderConnection(this);
		if (messageObservers.isEmpty()) {
			unreadMessages.add(msg);
			return;
		}
                messageObservers.stream().forEach((rec) -> {
                    rec.notifyMessageReceived(msg);
            });
	}
}
