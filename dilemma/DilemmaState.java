// NOTE: This is extra code, not a critical part of our Othello assignment
package dilemma;
import java.util.LinkedList;
import java.util.List;

import gamePlayer.Action;
import gamePlayer.State;

/**
 * This is a State of the game Dilemma.
 * @author Ashoat Tevosyan
 * @author Peter Brook
 * @since Mon April 26 2011
 * @version CSE 473
 */
public class DilemmaState implements State {
	
	// Whose turn is it?
	private boolean turn;
	
	int[][] board;
	int size;
	int l,r,t,b;
	int nextPlayer;

	DilemmaState parent;
	
	/**
	 * Instantiate this DilemmaState.
	 * @param size The dimension of the board.
	 */
	public DilemmaState(int[][] board) {
		this.board = board;
		this.size = board.length;
		l = 0;
		r = size;
		t = 0;
		b = size; 
		nextPlayer = 1;
	}
	
	public DilemmaState(DilemmaState other) {
		this.board = new int[other.size][other.size];
		this.size = other.size;
		this.nextPlayer = other.nextPlayer;
		l = other.l;
		r = other.r;
		t = other.t;
		b = other.b;
		for (int i=t; i < b; i++) {
			for (int j=l; j < r; j++) {
				board[i][j] = other.board[i][j];
			}
		}
	}

	public DilemmaState(int dimension) {
		this(new int[dimension][dimension]);
		
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Action> getActions() {		
		List<Action> actions = new LinkedList<Action>();
		if (nextPlayer == 1) {
			actions.add(new DilemmaAction(0));
			actions.add(new DilemmaAction(1));
		} else {
			actions.add(new DilemmaAction(2));
			actions.add(new DilemmaAction(3));
		}
		
		return actions;
	}
	
	/** {@inheritDoc} */
	@Override
	public float heuristic() {
		if (solved()) {
			float h = 0;
			for (int[] row: board) {
				for (int v: row) {
					h += v;
				}
			}
			return h;
		}
		return 0.0f;
	}
	
	private boolean solved() {
		int counter = 0;
		for (int[] row: board) {
			for (int v: row) {
				if (v != 0) {
					counter++;
				}
				if (counter > 1) {
					return false;
				}
			}
		}
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public Status getStatus() {
		if (solved()) return Status.PlayerOneWon;
		return Status.Ongoing;
	}
	
	/**
	 * We want to override since Actions might want to copy us to make a transition State.
	 * @return A deep copy of this State.
	 */
	@Override
	public Object clone() {
		DilemmaState state = new DilemmaState(this);
		return state;
	}
	
	/**
	 * Necessary for checking duplicate States.
	 * @param object An object to compare to this one.
	 * @return True if the parameter is a State of the same type that is deeply equivalent to us.
	 */
	@Override
	public boolean equals(Object object) {
		if (object != null && this.getClass() == object.getClass()) {
			DilemmaState state = (DilemmaState)object;
			return this.hashCode() == state.hashCode();
		} else {
			return false;
		}
	}
	
	/**
	 * We want to hash identical States into the same bucket.
	 * This way, we can use a HashSet/Map to avoid duplicate States in searches.
	 * @return A hash code for this State. 
	 */
	@Override
	public int hashCode() {
		int code = 0;
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				int additive = this.board[i][j] + 1;
				for (int k = 0; k < i * this.size + j; k++) additive *= 3;
				code += additive;
			}
		}
		return code;
	}
	
	/**
	 * We want to be able to compare States accurately.
	 * This way, we can use a TreeSet/Map to avoid duplicate States in searched.
	 * @param state A State to compare ourselves to.
	 * @return A positive integer if we are greater than the parameter; negative if we are lesser; zero otherwise. 
	 */
	@Override
	public int compareTo(State state) {
		float heuristic = this.heuristic() - state.heuristic();
		return heuristic >= 0 ? (int)Math.ceil(heuristic) : (int)Math.floor(heuristic); 
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int v;
		for (int i=0; i < size; i++) {
			for (int j=0; j < size; j++) {
				 v=board[i][j];
				 if (v==0) {
					 sb.append("X");
				 } else {
					 sb.append(v);
				 }
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public State getParentState() {
		return parent;
	}

	@Override
	public float heuristic2() {
		return heuristic();
	}

	@Override
	public String identifier() {
		
		return String.valueOf(hashCode());
	}
	
}