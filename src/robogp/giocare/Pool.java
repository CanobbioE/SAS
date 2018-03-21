public class Pool {
	private RobotMarker refRobot;
	private ArrayList<InstructionCard> hand;

	public pool(RobotMarker rb) {
		this.refRobot = rb;
	}

	public RobotMarker getRefRobot() {
		return this.refRobot;
	}

	public void clear() {
		this.hand.clear();
	}

	public void add(InstructionCard istr) {
		this.hand.add(istr);
	}

	public boolean remove(InstructionCard istr) {
		if (this.hand.contains(istr)) { 
			this.hand.remove(istr);
			return true;
		}
		return false;
	}
}