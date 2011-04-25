package othello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ticTacToe.TicTacToeAction;
import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.State;

public class OthelloPlayer implements Decider {

	// Are we player one?
	private boolean player;
	
	/**
	 * Instantiate this player class.
	 * @param player If true, we are player one. If false, we are player two.
	 */
	public OthelloPlayer(boolean player) {
		this.player = player;
	}
	
	@Override
	public Action decide(State state) {
		// Get some input
		System.out.println("Enter a move like so: 0,0");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
			line = input.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Failed to get input!");
		}
        // Parse output
		if (line.length() != 3) throw new RuntimeException("Bad input!");
		byte x = -1, y = -1;
		try {
			x = Byte.parseByte(line.substring(0, 1));
			y = Byte.parseByte(line.substring(2, 3));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Bad input!");
		}
		// Set up and return Action
		return new OthelloAction(player, x, y);
		
	}

}
