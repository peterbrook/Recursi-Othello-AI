// NOTE: This is extra code, not a critical part of our Othello assignment
package dilemma;

import java.util.Random;

import gamePlayer.Game;
import gamePlayer.algorithms.MTDDecider;
import gamePlayer.algorithms.MTDDecider2;
import gamePlayer.algorithms.MiniMaxDecider;

public class Dilemma {

	/**
	 * Run the game! DO IT!!!
	 * 
	 * @param args
	 *            Trash! Just play the game already!!
	 */
	public static void main(String[] args) {
		int dimension = 4;
		// Run the game and time it
		long totalTime = 0;
		// int[][] board = {{1,1},{8,2}};
		int[][] board = new int[dimension][dimension];/*{ { 2, 1, 1, 1 }, { 3, 5, 1, 1 }, { 3, 2, 3, 4 },
				{ 4, 3, 2, 1 } };*/
		Random r = new Random(999999);
		for (int i=0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				board[i][j] = r.nextInt(9) + 1;
			}
		}
		Game game = new Game(
				//new MiniMaxDecider(true, 6),
				new MTDDecider2(true, 120000, 6), 
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