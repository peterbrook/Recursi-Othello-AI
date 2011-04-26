package dilemma;
import gamePlayer.Action;
import gamePlayer.InvalidActionException;

/**
 * This is an Action in the game Tic Tac Toe.
 * It translates from one TicTacToeState to another.
 * @author Peter Brook
 * @since Mon April 18 2011
 * @version CSE 473
 */
public class DilemmaAction implements Action<DilemmaState> {
	
	// The move choice
	private int val;
	
	/**
	 * Instantiate this DilemmaAction.
	 * @param player The player making the move.
	 * @param x      The x-coordinate of the move.
	 * @param y      The y-coordinate of the move.
	 */
	public DilemmaAction(int value) {
		this.val = value;
	}
	
	/** {@inheritDoc} */
	@Override
	public DilemmaState applyTo(DilemmaState input) throws InvalidActionException {
		DilemmaState next = (DilemmaState)input.clone();
		next.parent = input;
		// left side
		for (int i=next.t; i < next.b; i++) {
			for (int j=next.l; j < next.r; j++) {
				if (val == 0) {
					if (j >= next.l + (next.r - next.l) / 2) {
						next.board[i][j] = 0;			
					}
				}
				else if (val == 1) {
					if (j < next.l + (next.r - next.l) / 2) {
						next.board[i][j] = 0;
					}
				}
				else if (val == 2) {
					if (i >= next.t + (next.b - next.t) / 2) {
						next.board[i][j] = 0;
					}
				}
				else if (val == 3) {
					if (i < next.t + (next.b - next.t) / 2) {
						next.board[i][j] = 0;
					}
				}
			}
		}
		
		if (val == 0) {
			next.r -= (next.r - next.l) / 2;
		} else if (val == 1) {
			next.l += (next.r - next.l) / 2;
		} else if (val == 2) {
			next.b -= (next.b - next.t) / 2;
		} else {
			next.t += (next.b - next.t) / 2;
		}
		
		next.nextPlayer *= -1;
		return next;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean validOn(DilemmaState input) {
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Action "+val;
	}

}