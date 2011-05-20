package othello;

import gamePlayer.Game;
import gamePlayer.State.Status;
import gamePlayer.algorithms.MTDDecider;
import gamePlayer.algorithms.MTDDecider2;

public class Othello {
	
	public static void main(String[] args) {
		int p1 = 0, p2=0;
		MTDDecider2 d1 = new MTDDecider2(true, 12500, 64);
		//d1.INTERMOVE_TRANSPOSITIONS = true;
		//d1.initializeTranspositionTable();
		MTDDecider2 d2 = new MTDDecider2(false, 12500, 64);
		//d2.INTERMOVE_TRANSPOSITIONS = false;
		for (int i=0; i < 10; i++) {
			OthelloState startState = new OthelloState();
			startState.setStandardStartState();/*
			startState.setValueOnBoards((byte)2, (byte)2, (byte)3);
			startState.setValueOnBoards((byte)2, (byte)3, (byte)3);
			startState.setValueOnBoards((byte)2, (byte)4, (byte)3);
			startState.setValueOnBoards((byte)3, (byte)2, (byte)2);
			startState.setValueOnBoards((byte)3, (byte)3, (byte)2);
			startState.setValueOnBoards((byte)3, (byte)4, (byte)3);
			startState.generateMoveBoards();*/
			//System.out.println("Start State:\n"+startState);
			Game g = new Game(d1, d2, startState);
			g.run();
			if (g.getStatus() == Status.PlayerOneWon) p1++;
			if (g.getStatus() == Status.PlayerTwoWon) p2++;
			
			System.out.println();
			System.out.println("==========================");
			System.out.println("P1 STATS:");
			d1.printSearchStatistics();
			
			System.out.println("P2 STATS:");
			d2.printSearchStatistics();
			System.out.println("==========================");
		}
		
		System.out.println("FINAL STATS:");
		System.out.println("==========================");
		System.out.println("P1 STATS:");
		d1.printSearchStatistics();
		
		System.out.println("P2 STATS:");
		d2.printSearchStatistics();
		System.out.println("==========================");
		
		System.out.println("P1: "+p1+" P2: "+p2);
	}
		
	
}
