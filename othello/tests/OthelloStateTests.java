package othello.tests;

import static org.junit.Assert.*;

import org.junit.*;

import othello.OthelloState;

public class OthelloStateTests {
	
	@Test
	public void testMoveIsValid() {
		// 2 is player 1 = O = true, 3 is player 2 = X = false
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)2);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.generateMoveBoards();
		System.out.println(state.toString());
		
		assertTrue(state.moveIsValid((byte)4, (byte)2, true));
		assertTrue(state.moveIsValid((byte)5, (byte)3, true));
		assertTrue(state.moveIsValid((byte)2, (byte)4, true));
		assertTrue(state.moveIsValid((byte)3, (byte)5, true));
		
		OthelloState s2 = new OthelloState();
		/*for (int i=0; i < 8; i++) {
			for (int j=0; j < 8; j++) {
				boolean val = state.moveIsValid((byte)i, (byte)j, true);
				s2.setValueOnBoards((byte)i, (byte)j, val ? (byte) 2 : (byte)3);
				
			}
		}*/
		assertTrue(state.moveIsValid((byte)2, (byte)3, false));
		assertTrue(state.moveIsValid((byte)3, (byte)2, false));
		assertTrue(state.moveIsValid((byte)4, (byte)5, false));
		assertTrue(state.moveIsValid((byte)5, (byte)4, false));
		
		/*for (int i=0; i < 8; i++) {
			for (int j=0; j < 8; j++) {
				boolean val = state.moveIsValid((byte)i, (byte)j, false);
				s2.setValueOnBoards((byte)i, (byte)j, val ? (byte) 2 : (byte)3);
			}
		}
		
		System.out.println(s2);*/
	}
	
	@Test
	public void testMoveIsValid2() {
		
		// NOTE: x is vertical, y is horizontal
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)3);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		state.setValueOnBoards((byte)2, (byte)3, (byte)3);
		state.generateMoveBoards();

		assertTrue(state.moveIsValid((byte)2, (byte)2, true));
		assertTrue(state.moveIsValid((byte)4, (byte)2, true));
		assertTrue(state.moveIsValid((byte)2, (byte)4, true));
		assertFalse(state.moveIsValid((byte)1, (byte)3, true));
		System.out.println(state);
	}
	
	@Test
	public void testCapture() {
		OthelloState state = new OthelloState();
		state.setValueOnBoards((byte)3, (byte)3, (byte)3);
		state.setValueOnBoards((byte)4, (byte)4, (byte)2);
		state.setValueOnBoards((byte)3, (byte)4, (byte)3);
		state.setValueOnBoards((byte)4, (byte)3, (byte)3);
		//state.setValueOnBoards((byte)2, (byte)3, (byte)3);
		state.generateMoveBoards();
		OthelloState res = state.childOnMove((byte)2, (byte)3);
		
		for (int i=0; i < 2; i++) {
			for (int j= 0; j < 8; j++) {
				assertFalse(res.moveIsValid((byte)i, (byte)j, true));
			}
		}
		
		for (int i=5; i < 8; i++) {
			for (int j= 0; j < 8; j++) {
				assertFalse(res.moveIsValid((byte)i, (byte)j, true));
			}
		}
		
		
		//System.out.println("res"+res);
		OthelloState res2 = res.childOnMove((byte)2, (byte)2);
		//System.out.println("res2"+res2);
		OthelloState res3 = res2.childOnMove((byte)3, (byte)2);
		//System.out.println("res3\n"+res3);
		OthelloState res4 = res3.childOnMove((byte)4, (byte)2);
		System.out.println("res4\n"+res4);
		
		OthelloState printState = new OthelloState();
		for (byte i=0; i < 8; i++) {
			for (byte j=0; j < 8; j++) {
				boolean val = res4.moveIsValid((byte)i, (byte)j, false);
				printState.setValueOnBoards(i, j, val ? (byte)3 : (byte)2);
			}
		}
		
		System.out.println(printState);
	}

}
