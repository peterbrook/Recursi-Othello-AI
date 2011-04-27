package dilemma;

import gamePlayer.Game;
import gamePlayer.MiniMaxDecider;
import gamePlayer.algorithms.MTDDecider;

public class Dilemma {

	/**
	 * Run the game! DO IT!!!
	 * 
	 * @param args
	 *            Trash! Just play the game already!!
	 */
	public static void main(String[] args) {
		int dimension = 2;
		// Run the game and time it
		long totalTime = 0;
		// int[][] board = {{1,1},{8,2}};
		int[][] board = { { 2, 1, 1, 1 }, { 3, 5, 1, 1 }, { 3, 2, 3, 4 },
				{ 4, 3, 2, 1 } };
		Game game = new Game(
				new MTDDecider(true, 120000, 9), 
				new MiniMaxDecider(false, 6), 
				new DilemmaState(board));
		final long startTime = System.nanoTime();
		try {
			game.run();
		} finally {
			totalTime += System.nanoTime() - startTime;
		}
		System.out.println("Took " + totalTime / 1000000000.0 + " seconds.");
	}

}