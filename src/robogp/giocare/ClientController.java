public class ClientController implements MessageObserver{

	private static final String TurnOffRobotMsg = "turnRobotOffRequest";
	private static final String DoneProgrammingMsg = "doneProgramming";
	private static final String ClientUseUpgradeMsg = "useUpgrade";
	public static final String FFpgradeMsg = "forfaitUpgrade";


	private String nickname;
	private Connection connection;
	private ArrayList<Upgrade> upgrades;
	private ArrayList<RobotMarker> robots;
	private ArrayList<Pool> pools;
	private boolean hasUsedUpgrade;

	private void setHasUsedUpgrade(boolean b) {
		this.hasUsedUpgrade = b;
	}

	public void uniscitiAPartita(String nickname, InetAddress address, int port, String key) {
		this.connection = Connection.connectToHost(address, port);
		connection.addMessageObserver(this);
		Message msg = new Message("joinMatchRequest");
		msg.setParameters(nickname, key);
		connection.sendMessage(msg);
	}

	public void inserisciScheda(RobotMarker rb, InstructionCard istr, int nReg) {
		Registry reg = rb.getRegistry(nReg);
		boolean ret;
		if (!rb.isAsleep) {
			if (!reg.isBlocked()) {
				for (int i = 0; i < pools.size(); i++) { // Rimuovi istr dal pool
					if (pools.get(i).getRefRobot() == rb) {
						ret = pools.get(i).remove(istr);
					}
				}
				if (ret == true) { // Inserisci istr nel registro
					rb.setRegistryInstr(nReg, istr);
				}
			}
		}
	}

	public void rimuoviScheda(RobotMarker rb, int nReg) {
		Registry reg = rb.getRegistry(nReg);
		if (!reg.isBlocked()) {
			InstructionCard istr = reg.getInstruction();
			rb.setRegistryInstr(null);

			for ( int j = 0; j < pools.size(); j++) {
				if (pools.get(j).getRefRobot() == rb) {
					pools.get(j).add(istr);
				}
			}
		}
	}

	public void spegniRobot(RobotMarker rb) {
		rb.setToBeAsleep(true);
		Message msg = new Message(Client.TurnOffRobotMsg);
		msg.setParameters(rb);
		connection.sendMessage(msg);
	}

	public void terminaProgrammazione() {
		// Checking all robots registries are filled
		boolean allRbAllFilled = true;
		for (RobotMarker rb : robots ) {
			boolean allRegFilled = true;
			for (Registry rg : rb.getRegistries()) {
				if (rg.isEmpty()) {
					allRegFilled = false;
					break;
				}
			}
			if (!allRegFilled) {
				allRbAllFilled = false;
				break
			}
		}

		if (allRbAllFilled) {
			Messge msg = new Messge(Client.DoneProgrammingMsg);
			msg.setParameters(nickname);
			for (int i = 0; i < pools.size(); i++) {
				pools.get(i).clear();
			}
		} else {
			// TODO comunica con GUI
		}
	}

	public attivaUpgrade(RobotMarker rb, UpgradePack u) {
		if (hasUsedUpgrade == false && upgrades.contains(u)) {
			u.useOne();
			setHasUsedUpgrade(true);

			Message msg = new Message(Client.ClientUseUpgrade);
			msg.setParameters(rb, u);
			connection.sendMessage(msg);
		} 
	}

	public void sacrificaUpgrade(UpgradePack u, RobotMarker rb) {

		if (rb.isHit()) {
			if (upgrades.contains(u)) {}
				upgrades.remove(u);
				Message msg = new Message(Client.ForfaitUpgradeMsg);
				msg.setParameters(rb, u);
				connection.sendMessage(msg);
		}
	}

	@Override
	public void notifyMessageReceived(Message msg) {
		if (msg.getName().equals("joinMatchReply")) { // Richiesta accettata
			boolean reply = (boolean) msg.getParameters(0);
			if (reply == true) {
				this.robots = (ArrayList<RobotMarker>) msg.getParameters(1);
				// Creazione pool e 5 registri per ogni robot
				for (int i = 0; i < robots.length; i++) {
					for (int n = 0; n < 5; n++) {
						robots.get(i).addRegistry(new Registry(n, false))
					}
					this.pools.add(new Pool(robots.get(i)));
				}
			} else if (reply == false) {
				//TODO: comunica con GUI
			}

		} else if (msg.getName().equals("startMatch")) { // Match start
			for (RobotMarker rb : this.robots) {
				Message m = new Message("poolRequest");
				m.setParameters(rb);
				connection.sendMessage(m);
			}

		} else if (msg.getName().equals("poolReply")) { // Schede distribuite
			Pair<InstructionCard[], RobotMarker> pair = (Pair<InstructionCard[], RobotMarker>)msg.getParameters(0) 
			if (robots.contains(pair.getSecond())) {
				for (int i = 0; i<pools.size(); i++) {
					if (p.getRefRobot() == pair.getSecond()) {
						//Popola il pool
						for ( InstructionCard istr : pair.getFirst() {
							pools.get(i).add(istr);
						}
					}
				}
			}

		} else if (msg.getName().equals("robotDestroyed")) {
			RobotMarker rb = (RobotMarker)msg.getParameters(0);
			for(RobotMarker r : robots) {
				if (r.getName() == rb.getName()) {
					robots.set(robots.indexOf(r), rb);
				}
			}
			// Comunica alla GUI che il robot è stato distrutto

		} else if (msg.getName().equals("robotHit")) {
			RobotMarker rb = (RobotMarker)msg.getParameters(0);
			LaserType lt = (LaserType)msg.getParameters(1);
			for(RobotMarker r : robots) {
				if (r.getName() == rb.getName()) {
					robots.set(robots.indexOf(r), rb);
				}
			}
			// Comunica alla GUI "Rb è stato colpito da lt"

		} else if (msg.getName().equals("outOfLives")) {
			RobotMarker rb = (RobotMarker)msg.getParameters(0);
			for(RobotMarker r : robots) {
				if (r.getName() == rb.getName()) {
					robots.remove(robots.indexOf(r));
				}
			}
			if (robots.isEmpty()) {
				connection.disconnect();
			}

		} else if (msg.getName().equals("endMatch")) {
			ArrayList<RobotMarker>leaderboard = msg.getParameters();
			// Comunica alla GUI la classifica così da rappresentarla
			
		} else if (msg.getName().equals("recivedUpgrade")) {
			UpgradePack up = msg.getParameters(0);
			RobotMarker rb = msg.getParameters(1);
			boolean myUpgr = false;

			for(RobotMarker r : robots) {
				if (r.getName() == rb.getName()) {
					upgrades.add(up);
					myUpgr = true;
				}
			}
			if (!myUpgr) {
				// Comunica alla GUI che rb ha ottenuto up
			}

		} else if (msg.getName().equals("updatePosition")) {
			RobotMarker rb = msg.getParameters(1);
			// Comunica con la GUI che sposterà il segnalino

		} else if (msg.getName().equals("endManche")) {
			ArrayList<RobotMarker> rb = (ArrayList<RobotMarker>) msg.getParameters(0);

			for (RobotMarker r : robots) {
				for (RobotMarker b : rb) {
					if (r.getName() == rb.getName()) {
						robots.set(robots.indexOf(r), rb);
					}
				}
			}
			for (RobotMarker rb : this.robots) {
				Message m = new Message("poolRequest");
				m.setParameters(rb);
				connection.sendMessage(m);
			}
		}
	}
	
}