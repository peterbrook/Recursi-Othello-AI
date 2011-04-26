package ticTacToe;
import gamePlayer.Game;
import gamePlayer.MiniMaxDecider;
import gamePlayer.algorithms.MTDDecider;

public class TicTacToe {
	
	/**
	 * Run the game! DO IT!!!
	 * @param args Trash! Just play the game already!!
	 */
	public static void main(String[] args) {
		int dimension = 3;
		// Run the game and time it
		long totalTime = 0;
		for (int i = 0; i < 1; i++) {
			Game game = new Game(new MTDDecider(true, 6),
								 new MiniMaxDecider(false, 6),
								 new TicTacToeState(dimension));
			final long startTime = System.nanoTime();
			try {
				game.run();
			} finally {
				totalTime += System.nanoTime() - startTime;
			}
		}
		System.out.println("Took " + totalTime / 1000000000.0 + " seconds.");
	}

}