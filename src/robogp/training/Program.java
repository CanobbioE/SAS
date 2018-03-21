package robogp.training;

import java.util.*;

public class Program {
	private int order;
	private ArrayList<InstructionCard> instructions;

	public Program (int o, ArrayList<InstructionCard> i) {
		this.order = o;
		this.instructions = i;
	}

	public InstructionCard getNextInstruction() {
		if (order < instructions.size()) {
			return instructions.get(order++);
		} else {
			return null;
		}
	}

	public void setOrder(int o) {
		this.order = o;
	}

	public void setOrder(InstructionCard istr) {
		this.order = instructions.indexOf(istr);
	}

	public void addInstruction(InstructionCard istr) {
		this.instructions.add(istr);
	}

	public void removeIstruction(InstructionCard istr) {
		this.instructions.remove(istr);
	}

	public void moveInstruction(InstructionCard istr, int index) {
		if (index < instructions.size() && index > 0) {
			int oldIndex = instructions.indexOf(istr);
			InstructionCard swapCard = instructions.get(index);
			instructions.set(oldIndex, swapCard);
			instructions.set(index, istr);
		}
	}



}