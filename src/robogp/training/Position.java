package robogp.training;

import robogp.robodrome.*;
public class Position {

	private Direction direction;
	private int row;
	private int col;

	public Position(int r, int c, Direction d) {
		this.row = r;
		this.col = c;
		this.direction = d;
	}

	public void setPosition(int r, int c) {
		this.row = r;
		this.col = c;
	}

	public void setPosition(Position pos) {
		this.row = pos.getRow();
		this.col = pos.getCol();
		this.direction = pos.getDirection();
	}
        
    public void setDirection(Direction d) {
        this.direction = d;
    }

	public Direction getDirection() {
		return this.direction;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public void turnPosition(Rotation r) {
		this.direction = Rotation.changeDirection(this.direction, r);
	}

}