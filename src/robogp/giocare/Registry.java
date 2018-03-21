public class Registry {
	private int number;
	private boolean blocked;
	private InstructionCard instruction;

	public Registry(int n, boolean b) {
		this.number = n;
		this.blocked = b;
		this.instruction = null;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public setBlocked(boolean b) {
		this.blocked = b;
	}

	public void clear() {
		this.instruction = null;
	}

	public void setInstruction(InstructionCard istr) {
		this.instruction = istr;
	}

	public InstructionCard getInstruction() {
		return this.instruction;
	}

	public boolean isEmpty() {
		return this.instruction == null;
	}

	public int getnumber() {
		return this.number;
	}
}