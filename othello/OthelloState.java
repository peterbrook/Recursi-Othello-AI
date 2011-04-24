package othello;

import java.util.BitSet;
import java.util.List;
import java.util.LinkedList;

import gamePlayer.Action;
import gamePlayer.State;

public class OthelloState implements State {
	
	// The dimension of this board
	private int dimension;
	// The actual data structure storing the board
	private BitSet board;
	
	/**
	 * Initialize this OthelloState.
	 * @param dimension The dimension of this board.
	 */
	public OthelloState(int dimension) {
		this.dimension = dimension;
		this.board = new BitSet(dimension * dimension * 2);
	}
	
	/** {@inheritDoc} */
	@Override
	public int compareTo(State state) {
		float heuristic = this.heuristic() - state.heuristic();
		return heuristic >= 0 ? (int)Math.ceil(heuristic) : (int)Math.floor(heuristic);
	}
	
	/**
	 * Get the value of a coordinate on the board.
	 * @param x The x-coordinate I am looking for.
	 * @param y The y-coordinate I am looking for.
	 * @return The value as a byte. -1 is black; 0 is empty; 1 is white.
	 */
	private byte getSquare(byte x, byte y) {
		if (!board.get(2 * (x * this.dimension + y))) return 0;
		return board.get(2 * (x * this.dimension + y) + 1) ? (byte)1 : (byte)-1;
	}
	
	/**
	 * Set the value of a coordinate on the board.
	 * @param x     The x-coordinate I am looking for.
	 * @param y     The y-coordinate I am looking for.
	 * @param value The value at that square. -1 is black; 0 is empty; 1 is white.
	 */
	private void setSquare(byte x, byte y, byte value) {
		if (value == 0) {
			board.set(2 * (x * this.dimension + y), false);
			return;
		}
		board.set(2 * (x * this.dimension + y));
		if (value > 1) board.set(2 * (x * this.dimension + y) + 1);
		else board.set(2 * (x * this.dimension + y) + 1, false);
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<Action> getActions() {
		List<Action> actions = new LinkedList<Action>();
		
	}
	
	private float pieceDifferential() {
		return this.board.cardinality();
	}

	@Override
	public float heuristic() {
		return this.pieceDifferential();
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

}
