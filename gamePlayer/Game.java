package gamePlayer;

import gamePlayer.State.Status;

/**
 * This class runs a turn-based deterministic game between two players.
 * @author Ashoat Tevosyan
 * @since Mon April 18 2011
 * @version CSE 473
 */
public class Game {
	
	// The two players
	private Decider playerOne;
	private Decider playerTwo;
	// The current state of the game we are on
	private State currentState;
	// Whose turn is it?
	private boolean turn;
	
	/**
	 * Instantiate this Game object.
	 * @param playerOne  The first player.
	 * @param playerTwo  The second player.
	 * @param startState The start State.
	 */
	public Game(Decider playerOne, Decider playerTwo, State startState) {
		this.playerOne = playerOne;
		this.playerTwo = playerTwo;
		this.currentState = startState;
		this.turn = true; // First person's turn
	}
	
	/**
	 * Make a single move.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void move() {
		// Get the action
		Action action = this.turn ? this.playerOne.decide(this.currentState) : 
			this.playerTwo.decide(this.currentState);
		// Print it to the trace
		System.out.println(action);
		// Try to apply it
		try {
			currentState = action.applyTo(currentState);
		} catch (InvalidActionException e) {
			throw new RuntimeException("Invalid action!");
		}
		// Next person's turn
		this.turn = !this.turn;
	}
	
	/**
	 * Run the Game.
	 */
	public void run() {
		while (this.currentState.getStatus() == Status.Ongoing) this.move();
		System.out.print(this.currentState);
		System.out.println("Game result: " + this.currentState.getStatus() + ".");
	}
	
}