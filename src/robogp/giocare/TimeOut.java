public class TimeOut extends TimerTask {
	private Connection conn;
	private Mesage msg;

	public TimeOut(Connection conn, Message msg) {
		super();
		this.conn = conn;
		this.msg = msg;
	}

	public void run () {
		conn.sendMessage(msg);
	}
}