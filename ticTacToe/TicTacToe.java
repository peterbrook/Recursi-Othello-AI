package ticTacToe;
import gamePlayer.Game;
import gamePlayer.State.Status;
import gamePlayer.algorithms.MTDDecider;
import gamePlayer.algorithms.MiniMaxDecider;

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
			TicTacToeState start = new TicTacToeState(dimension);
			/*start.executeMove((byte)1, 0, 0);
			start.executeMove((byte)-1, 1, 1);
			start.executeMove((byte)1, 0, 1);
			start.executeMove((byte)-1, 1, 0);
			start.executeMove((byte)1, 2, 0);
			start.executeMove((byte)-1, 0, 2);*/
			Game game = new Game(new MTDDecider(true, 500, 9),
								 new MTDDecider(false, 500, 9),
								 start);
			
			final long startTime = System.nanoTime();
			try {
				game.run();
			} finally {
				totalTime += System.nanoTime() - startTime;
				switch (game.getStatus()) {
				case Draw:
					System.out.println("DRAW");
					break;
				case PlayerOneWon:
					System.out.println("P1 WIN");
					return;
				case PlayerTwoWon:
					System.out.println("P2 WIN");
					return;
				}
			}
		}
		System.out.println("Took " + totalTime / 1000000000.0 + " seconds.");
	}

}