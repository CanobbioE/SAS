package connection;

import java.io.Serializable;

/**
 * I messaggi scambiati fra client e server sono di tipo Message.
 * Ogni Message ha un nome e dei parametri, rappresentati con 
 * un'array di Object. Due messaggi particolari, Hello e Bye, possono
 * essere creati tramite factory methods. Hello viene usato nell'handshake
 * iniziale di una connessione, mentre Bye viene usato per chiudere la 
 * connessione stessa.
 */
public class Message implements Serializable {
	private final static String BYE_MSG = "___BYE___";
	private final static String HELLO_MSG = "___HELLO___";
	protected String name;
	private Object[] parameters;
	private Connection senderConnection;
	
	private Message() {
	}
	
	public void setSenderConnection(Connection conn) {
		senderConnection = conn;
	}
	
	public Connection getSenderConnection() {
		return senderConnection;
	}
	
	/**
	 * Costruisce un nuovo messaggio del tipo specificato.
	 * @param name Il nome (o tipo) del messaggio.
	 */
	public Message(String name) {
		this.name = name;
		parameters = new Object[0];
	}
	
	/** Imposta i parametri del messaggio.
	 * @param pars Un array contenente i parametri del messaggio.
	 */
	public void setParameters(Object[] pars) {
		this.parameters = pars;
	}

	/** Stabilisce se il messaggio attuale &egrave; un messaggio Bye.
	 * @return true se si tratta di un messaggio Bye, false altrimenti.
	 */
	public boolean isByeMessage() {
		return this.getName().equals(BYE_MSG);
	}

	/** Stabilisce se il messaggio attuale &egrave; un messaggio Hello.
	 * @return true se si tratta di un messaggio Hello, false altrimenti.
	 */	
	public boolean isHelloMessage() {
		return this.getName().equals(Message.HELLO_MSG);
	}
	
	/** Factory method per creare un messaggio Bye.
	 * 
	 * @return il messaggio creato
	 */
	public static Message createByeMessage() {
		Message msg = new Message();
		msg.name = BYE_MSG;
		return msg;
	}

	/** Factory method per creare un messaggio Hello.
	 * 
	 * @return il messaggio creato
	 */
	public static Message createHelloMessage() {
		Message msg = new Message();
		msg.name = Message.HELLO_MSG;
		return msg;
	}	
	
	/**
	 * @return l'i-esimo parametro di questo messaggio
	 */
	
	public Object getParameter(int i) {
		if (i < 0 || i >= parameters.length) return null;
		return  parameters[i];
	}
	
	/**
	 * @return il numero di parametri di questo messaggio
	 */
	public int getParametersCount() {
		return parameters.length;
	}

	/**
	 * @return nome/tipo di questo messaggio
	 */
	public String getName() {
		return name;
	}
}
