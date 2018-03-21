package connection;

/**
 * L'interfaccia MessageObserver va a definire, insieme alla Connection, un
 * pattern Observer relativo alla ricezione di messaggi. Le classi che vogliono
 * osservare i messaggi in arrivo devono implementare questa interfaccia e le loro
 * istanze devono registrarsi presso la Connection tramite il metodo addMessageObserver.
 */
public interface MessageObserver {
	/** Questo metodo viene invocato dal soggetto osservato (un'istanza di Connection)
	 * quando arriva un messaggio.
	 * @param msg Il messaggio ricevuto.
	 */
	public void notifyMessageReceived(Message msg);
}
