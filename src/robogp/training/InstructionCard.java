package robogp.training;

import robogp.robodrome.Rotation;
import java.io.Serializable;

public class InstructionCard implements Serializable {
	private final InstructionName instruction;
	private final Rotation rotation;
	private final int offset;
	private int priority;

	public InstructionCard(InstructionName i, Rotation r, int o) {
		this.instruction = i;
		this.rotation = r;
		this.offset = o;
	}

	public InstructionCard(InstructionName i, Rotation r, int o, int p) {
		this.instruction = i;
		this.rotation = r;
		this.offset = o;
		this.priority = p;
	}

	public InstructionName getInstruction() {
		return this.instruction;
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public int getOffset() {
		return this.offset;
	}

	public static InstructionCard[] generateDeck() {
		InstructionCard[] deck = new InstructionCard[83];
		// 6 U-Turn
		for (int i = 0; i < 6; i++) {
			deck[i] = new InstructionCard(
				InstructionName.TURN, Rotation.CW180, 0, 10*(i+1));
		}
		// 18 Turn Left
		for (int i = 0; i < 18; i++) {
			deck[i+6] = new InstructionCard(
				InstructionName.TURN, Rotation.CCW90, 0, 10*(2*i+7));
		}
		//18 Turn Right
		for (int i = 0; i < 18; i++) {
			deck[i+24] = new InstructionCard(
				InstructionName.TURN, Rotation.CW90, 0, 10*(2*i+8));
		}
		//6 Back-up
		for (int i = 0; i < 6; i++) {
			deck[i+42] = new InstructionCard(
				InstructionName.BACK, Rotation.NO, -1, 10*(i+43));
		}
		//18 Move 1
		for (int i = 0; i < 18; i++) {
			deck[i+48] = new InstructionCard(
				InstructionName.MOVE, Rotation.NO, 1, 10*(i+49));
		}
		//12 Move 2
		for (int i = 0; i < 12; i++) {
			deck[i+66] = new InstructionCard (
				InstructionName.MOVE, Rotation.NO, 2, 10*(i+67));
		}
		//6 Move 3
		for (int i = 0; i < 6; i++) {
			deck[i+78] = new InstructionCard(
				InstructionName.MOVE, Rotation.NO, 3, 10*(i+79));
		}

		return deck;
	}
}