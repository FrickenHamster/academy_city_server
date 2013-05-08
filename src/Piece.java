import objectdraw.*;
import java.awt.*;

public class Piece extends ActiveObject {
	// constants indicating what kind of move is pending
	private static final int MOVELEFT = -1;
	private static final int MOVERIGHT = 1;
	private static final int ROTATERIGHT = 1;
	private static final int ROTATELEFT = -1;

	private static final int NUM_PIECES = 4; // number of squares in a piece
	private static final int START_X = 5; // which column piece starts in

	private static final int PAUSE_TIME = 200;

	// array holding the positions of all squares in the piece
	private Position[] position = new Position[NUM_PIECES];

	// How many columns the piece should move on its next move
	private int chgeCol = 0;

	// if 1 then rotate right, if -1 rotate left
	private int rotate = 0;

	private boolean falling, overlapping;

	private Color currentColor;

	private LineTrisField aField;

	private LineTris aController;

	private int colorNumber;

	public Piece(LineTrisField field, int colorNum, LineTris controller) {
		aField = field;
		aController = controller;
		colorNumber = colorNum;

		for (int pieceNumber = 0; pieceNumber < 4; pieceNumber++)
			if (!field.isOccupied(position[pieceNumber])) {
				start();
			}

	}

	public void run() {
		for (int pieceNumber = 0; pieceNumber < 4; pieceNumber++) {
			aField.addItem(position[pieceNumber], currentColor);
		}
		while (!overlapping) {
			this.moveToNewPositions();
			if (!falling) {
				pause(PAUSE_TIME);
			}
			aController.startPiece();
		}
	}

	// Figure out next possible positions for piece and move
	// there if legal. Set flag to indicate if blocked.
	private void moveToNewPositions() {

		// remove all squares in the piece from the field
		for (int pieceNumber = 0; pieceNumber < 4; pieceNumber++)
			aField.removeItem(position[pieceNumber]);

		// rotate the piece if needed and possible
		if (rotate != 0) {
			tryRotate();
		}

		// move sideways if needed and possible
		if (chgeCol != 0) {
			trySideways();
		}

		// move down screen one place if possible
		this.tryMoveDown();
		// redraw piece in new position
		for(int pieceNumber = 0; pieceNumber < 4; pieceNumber++)
			aField.addItem(position[pieceNumber], currentColor);
	}

	// find out if new positions are legal -- unoccupied and inside field
	private boolean isLegalPosition(Position[] tempPosition) {
		for(int pieceNumber = 0; pieceNumber < 4; pieceNumber++) {
			if(aField.outOfBounds(tempPosition[pieceNumber]) || aField.isOccupied(tempPosition[pieceNumber])){
				return false;
			}
		}
		return true;
	}

	// Rotate piece in direction desired if possible.
	// Doesn't rotate if there is a piece in the way or would go out of bounds.
	// When done reset value of rotate to 0 so won't attempt to rotate again.
	// You should not need to modify this method (even if you do the real
	// "Tetris
	// pieces" so long as it is OK to rotate about the block at position 1)
	private void tryRotate() {
		int pivotRow = position[1].getRow();
		int pivotCol = position[1].getCol();

		Position[] tempPosition = new Position[NUM_PIECES];

		// find new rotated positions
		for (int pieceNumber = 0; pieceNumber < NUM_PIECES; pieceNumber++) {
			int newRow = pivotRow + rotate
					* (position[pieceNumber].getCol() - pivotCol);
			int newCol = pivotCol + rotate
					* (pivotRow - position[pieceNumber].getRow());

			tempPosition[pieceNumber] = new Position(newRow, newCol);
		}

		// if positions are legal, move there
		if (isLegalPosition(tempPosition)) {
			position = tempPosition;
		}

		rotate = 0;
	}

	private void trySideways() {
		Position tempPosition[] = new Position[NUM_PIECES];
		for (int pieceNumber = 0; pieceNumber < 4; pieceNumber++) {
			int nextCol = position[pieceNumber].getCol() + chgeCol;
			int nextRow = position[pieceNumber].getRow();
			tempPosition[pieceNumber] = new Position(nextRow, nextCol);
		}

		if (isLegalPosition(tempPosition))
			position = tempPosition;
		chgeCol = 0;
	}

	private void tryMoveDown() {
		Position nextPosition[] = new Position[4];
		for (int pieceNumber = 0; pieceNumber < 4; pieceNumber++) {
			nextPosition[pieceNumber] = new Position(
					position[pieceNumber].getRow() + 1,
					position[pieceNumber].getCol());

			if (isLegalPosition(nextPosition)) {
				position = nextPosition;
			} else {
				overlapping = true;
			}
		}
	}

	// Remember want to move left as part of next move
	public void moveLeft() {
		chgeCol = MOVELEFT;
	}

	// Remember want to move right as part of next move
	public void moveRight() {
		chgeCol = MOVERIGHT;
	}

	// Remember want to rotate to the left as part of next move
	public void rotateLeft() {
		rotate = ROTATELEFT;
	}

	// Remember want to rotate to the right as part of next move
	public void rotateRight() {
		rotate = ROTATERIGHT;
	}

	// Drop piece immediately to its final resting position without any pauses
	public void fall() {
		this.tryMoveDown();
	}
}




