package ticTacToe;
import gamePlayer.Action;
import gamePlayer.InvalidActionException;

/**
 * This is an Action in the game Tic Tac Toe.
 * It translates from one TicTacToeState to another.
 * @author Ashoat Tevosyan
 * @since Mon April 18 2011
 * @version CSE 473
 */
public class TicTacToeAction implements Action<TicTacToeState> {
	
	// The player making the move
	private boolean player;
	// The x-coordinate of the move
	private int x;
	// The y-coordinate of the move
	private int y;
	
	/**
	 * Instantiate this TicTacToeAction.
	 * @param player The player making the move.
	 * @param x      The x-coordinate of the move.
	 * @param y      The y-coordinate of the move.
	 */
	public TicTacToeAction(boolean player, int x, int y) {
		this.player = player;
		this.x = x;
		this.y = y;
	}
	
	/** {@inheritDoc} */
	@Override
	public TicTacToeState applyTo(TicTacToeState input) throws InvalidActionException {
		TicTacToeState output = (TicTacToeState)input.clone();
		output.parent = input;
		if (output.getValue(this.x, this.y) != 0) throw new InvalidActionException();
		output.executeMove(this.player ? (byte)1 : (byte)-1, this.x, this.y);
		return output;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean validOn(TicTacToeState input) {
		return input.getValue(this.x, this.y) == 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Player ").append(this.player ? "1" : "2");
		builder.append(" moving to (" + this.x + "," + this.y + ").");
		return builder.toString();
	}

}