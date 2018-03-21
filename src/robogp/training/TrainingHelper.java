package robogp.training;

import robogp.robodrome.*;

public class TrainingHelper {
	private Training theTraining;

	public void iniziaAllenamento(String rbdName, int row, int col, Direction dir) {
		this.theTraining = Training.getInstance(rbdName, row, col, dir);
	}

	public void avviaEsecuzione() {
		if (theTraining.getExecutionState() == ExecutionState.FERMA) {
			theTraining.setExecutionState(ExecutionState.AVVIATA);
			(new Thread(theTraining)).start();
		}
	}

	public void pausaEsecuzione() {
		if (this.theTraining.getExecutionState() == ExecutionState.AVVIATA) {
			this.theTraining.setExecutionState(ExecutionState.IN_PAUSA);
		}
	}

	public void interrompiEsecuzione() {
		if (this.theTraining.getExecutionState() == ExecutionState.IN_PAUSA) {
			this.theTraining.setExecutionState(ExecutionState.FERMA);
		}
	}

	public void ripristina() {
		if (this.theTraining.getExecutionState() == ExecutionState.IN_PAUSA) {
			this.theTraining.setExecutionState(ExecutionState.AVVIATA);
			this.theTraining.notify();
		}
	}

	public void ripristinaDa(InstructionCard istr) {
		if (this.theTraining.getExecutionState() == ExecutionState.IN_PAUSA) {
			this.theTraining.setExecutionState(ExecutionState.AVVIATA, istr);
			this.theTraining.notify();
		}
	}

	public void inserisciScheda(InstructionCard istr) {
		if (this.theTraining.getExecutionState() == ExecutionState.FERMA) {
			this.theTraining.editProgram(Operation.ADD, istr);
		}
	}

	public void rimuoviScheda(InstructionCard istr) {
		if (this.theTraining.getExecutionState() == ExecutionState.FERMA) {
			this.theTraining.editProgram(Operation.REMOVE, istr);
		}
	}

	public void spostaScheda(InstructionCard istr, int index) {
		if (this.theTraining.getExecutionState() == ExecutionState.FERMA) {
			this.theTraining.editProgram(Operation.MOVE, istr, index);
		}
	}

}