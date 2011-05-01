// NOTE: This is extra code, not a critical part of our Othello assignment
package gamePlayer;

import java.io.File;

/**
 * We use this class for debugging purposes.
 * It draws and maintains a graph of a search space.
 * @author Ashoat Tevosyan
 * @since Mon Apr 18 2011
 * @version CSE 473
 */
public class GraphVizPrinter {
	
	// The GraphViz object.
	private static GraphViz gv;
	// Which turn is this? Used so we don't overwrite the same file each turn.
	private static int turn = 0;
	// What type should we save our files in?
	public static String type = "svg";
	
	static int idx = 0;
	/**
	 * A static constructor to initialize GraphViz.
	 */
	static {
		gv = new GraphViz();
		gv.addln(gv.start_graph());
	}
	
	/**
	 * Add a State to the graph.
	 * @param state The State to add. 
	 */
	public static void setState(State s) {
		gv.addln(getId(s)+ " [label=\"" + s.toString().replaceAll("\n", "\\\\n") + "\", shape=box, fontname=Courier];");
	}
	
	/**
	 * Add a State to the graph.
	 * @param state The State to add. 
	 */
	public static void setCached(State s) {
		gv.addln(getId(s) + " [style=filled,fillcolor=green];");
	}
	
	/**
	 * Set a relationship between two States with the given weight.
	 * @param state The child State.
	 * @param value The weight (heuristic) value of this relationship.
	 * @param daddy The parent State.
	 */
	public static void setRelation(State state, float value, State daddy) {
		if (daddy != null) gv.addln(getId(daddy) + " -> " + getId(state) + " [label=\"" + value + "\"];");
	}
	
	/**
	 * Set a relationship between two States with the given weight.
	 * @param state The child State.
	 * @param value The weight (heuristic) value of this relationship.
	 * @param daddy The parent State.
	 * @param alpha the alpha value passed in to this search
	 * @param alpha the beta value passed in to this search
	 */
	public static void setRelation(State state, float value, State daddy, float alpha, float beta) {
		if (daddy != null) gv.addln(getId(daddy) + " -> " + getId(state) + " [label=\"" + value + " (" + alpha + ", " + beta +")\"];");
	}
	
	
	/**
	 * Highlight the final decision as red for easy viewing.
	 * @param state The final decision we settled on.
	 */
	public static void setDecision(State state) {
		gv.addln(getId(state) + " [color=red];");
	}
	
	/**
	 * Finally print the current graph to a file.
	 */
	public static void printGraphToFile() {
		gv.addln(gv.end_graph());
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), new File("turn" + turn++ + "." + type) );
		gv = new GraphViz();
		gv.addln(gv.start_graph());
	}
	
	/**
	 * Finally print the current graph to a file.
	 */
	public static void printGraphToFileWDeepening(int d) {
		gv.addln(gv.end_graph());
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), new File("turn" + turn + "." + +d+"."+type) );
		gv = new GraphViz();
		gv.addln(gv.start_graph());
	}
	
	private static String getId(State s) {
		StringBuilder sb = new StringBuilder();
		sb.append(s.identifier());
		if (s.getParentState() != null) {
			sb.append(s.getParentState().identifier());
		}
		return sb.toString();
	}
}