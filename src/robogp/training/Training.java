package robogp.training;

import robogp.robodrome.*;
import java.util.*;
        
public class Training implements Runnable {
	
	private ExecutionState executionState;
	private final HashMap<InstructionCard, Position> prevPositions;
	private final Position position;
	private final TrainingRobot trainRobot;
	private final Robodrome theRobodrome;
	private final Program program;
	private int turn;


	// Pattern Singleton
	private static Training singleInstance;

	private Training(String rbdName, int row, int col, Direction dir) {
		this.position = new Position(row, col, dir);
		String rbdFileName = "robodromes/" + rbdName + ".txt";
		this.theRobodrome = new Robodrome(rbdFileName);
		this.trainRobot = new TrainingRobot("Tester", "RED");
		prevPositions = new HashMap<>();
		this.executionState = ExecutionState.FERMA;
                program = new Program(0, null);
	}

	public static Training getInstance(String rbdname, int row, int col, Direction dir) {
		if (Training.singleInstance == null) {
			Training.singleInstance = new Training(rbdname, row, col, dir);
		}
		return Training.singleInstance;
	}

	public static Training getInstance() {
		if (Training.singleInstance == null) {
			return null;
		}
		return Training.singleInstance;
	}
	// Fine singleton

	public void setExecutionState(ExecutionState state) {
		this.executionState = state;
	}

	public void setExecutionState(ExecutionState state, InstructionCard istr) {
		this.executionState = state;
		if (prevPositions.get(istr) != null) {
			this.program.setOrder(istr);
			this.position.setPosition(prevPositions.get(istr));
			prevPositions.clear();
		}
	}

	public ExecutionState getExecutionState() {
		return this.executionState;
	}

	public void editProgram(Operation op, InstructionCard istr) {
		switch(op) {
			case ADD:
				this.program.addInstruction(istr);
				break;
			case REMOVE:
				this.program.removeIstruction(istr);
				break;
			default:
				break;
		}
	}

	public void editProgram(Operation op, InstructionCard istr, int index) {
		if (op == Operation.MOVE) {
			this.program.moveInstruction(istr, index);
		}
	}

        @Override
	public void run() {
		this.program.setOrder(0);
		this.turn = 0;
		while (this.executionState == ExecutionState.AVVIATA) {
			InstructionCard istr = this.program.getNextInstruction();
			if (istr != null) { 
				switch (istr.getInstruction()) {
					case MOVE:
						moveRobot(istr);
						break;
					case TURN:
						turnRobot(istr);
						break;
					case BACK:
						backRobot(istr);
						break;
					default:
						break;
				}
				moveRobodrome();
			} else {
				this.setExecutionState(ExecutionState.FERMA);
			}
			turn++;
		}
		while(this.executionState == ExecutionState.IN_PAUSA) {
                    try {
			this.wait();
                    }catch (InterruptedException ex) {}
		}
	}

	private void moveRobot(InstructionCard istr) {
		int offset = istr.getOffset();
		Direction dir = this.position.getDirection();
		int row = this.position.getRow();
		int col = this.position.getCol();

		while (offset > 0) {
			BoardCell cell = this.theRobodrome.getCell(row,col);
			if (cell instanceof FloorCell && ((FloorCell)cell).hasWall(dir) == false) {
				
				if (dir.ordinal() % 2 == 0) { col = col + (dir.ordinal()-1);} 
				else { row = row + (dir.ordinal()-2); }

				cell = this.theRobodrome.getCell(row, col);
				Direction oppDir = Direction.values()[(dir.ordinal()+2)%4];

				if (cell.hasWall(oppDir) == false) {
					this.position.setPosition(row,col);
					offset = offset - 1; 
				} else {
					offset = 0;
				}
			}
		}
		prevPositions.put(istr, this.position);
	}

	private void turnRobot(InstructionCard istr) {
		this.position.turnPosition(istr.getRotation());
		prevPositions.put(istr, this.position);
	}

	private void backRobot(InstructionCard istr) {
		int offset = istr.getOffset();
		Direction dir = this.position.getDirection();
		int row = this.position.getRow();
		int col = this.position.getCol();

		while (offset < 0) {
			BoardCell cell = this.theRobodrome.getCell(row, col);

			Direction oppDir = Direction.values()[(dir.ordinal()+2)%4];
			if (cell.hasWall(oppDir) == false) {
				if (dir.ordinal() % 2 == 0) { col = col - (dir.ordinal()-1); }
				else { row = row - (dir.ordinal()-2); }

				cell = this.theRobodrome.getCell(row, col);

				if (cell.hasWall(dir) == false) {
					this.position.setPosition(row, col);
					offset = offset + 1;
				} else {
					offset = 0;
				}
			}
		}
		prevPositions.put(istr, this.position);
	}

	private void moveRobodrome() {
		int row = this.position.getRow();
		int col = this.position.getCol();

		BoardCell cell = this.theRobodrome.getCell(row, col);
		Direction dir = this.position.getDirection();

		if (cell.getType() == 'B' || cell.getType() == 'E') {
			Direction outDir = theRobodrome.getBeltOutput(cell);
			
			if (dir.ordinal() % 2 == 0) { col = col + (dir.ordinal()-1);} 
			else { row = row + (dir.ordinal()-2); }

			cell = this.theRobodrome.getCell(row,col);
			Direction oppDir = Direction.values()[(outDir.ordinal()+2)%4];
			boolean hasInput = this.theRobodrome.hasBeltInput(cell, oppDir);

			if ((cell.getType() == 'B' || cell.getType() == 'E') && hasInput) {
				Direction aux = theRobodrome.getBeltOutput(cell);
				this.position.setDirection(aux);
			}
		} else if (cell.getType() == 'F') {
            boolean isRR = theRobodrome.isCellRR(cell);
            boolean isLR = theRobodrome.isCellLR(cell);
            boolean ePsh = theRobodrome.hasCellEP(cell);
            boolean oPsh = theRobodrome.hasCellOP(cell);
			if (ePsh && this.turn % 2 == 0) {
				// Pusher pari
				Direction d = theRobodrome.getCellPshWall(cell);
				if (d.ordinal() % 2 == 0) { col = col - (dir.ordinal()-1); }
				else { row = row - (dir.ordinal()-2); }
				this.position.setPosition(row, col);
			} else if (oPsh && this.turn % 2 == 0) {
				// Pusher dispari
				Direction d = theRobodrome.getCellPshWall(cell);
				if (d.ordinal() % 2 == 0) { col = col - (dir.ordinal()-1); }
				else { row = row - (dir.ordinal()-2); }
				this.position.setPosition(row, col);
			} else if (isRR) {
				// Rotatoria destra
				this.position.turnPosition(Rotation.CW90);
			} else if (isLR) {
				// Rotatoria Sinistra
				this.position.turnPosition(Rotation.CCW90);	
			}
		}
	}
}