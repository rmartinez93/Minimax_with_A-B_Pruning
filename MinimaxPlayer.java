// MinimaxPlayer.java

/**
 *
 * @author Sean Bridges
 * @version 1.0
 *
 *          The MinimaxPlayer uses the minimax algorithm to determine what move
 *          it should make. Looks ahead depth moves. The number of moves will be
 *          on the order of n^depth, where n is the number of moves possible,
 *          and depth is the number of moves the engine is searching. Because of
 *          this the minimax player periodically polls the thread that calls
 *          getMove(..) to see if it was interrupted. If the thread is
 *          interrupted, the player returns null after it checks.
 */
public class MinimaxPlayer extends DefaultPlayer {

	// ----------------------------------------------
	// instance variables

	// the number of levels minimax will look ahead
	private int depth = 1;
	private Player minPlayer;
		public int totalMoves = 0;
	public long totalTime = 0;
		public int moves = 0;


	// ----------------------------------------------
	// constructors

	/** Creates new MinimaxPlayer */
	public MinimaxPlayer(String name, int number, Player minPlayer) {
		super(name, number);

		this.minPlayer = minPlayer;

	}

	public void resetStats() {
			 this.totalMoves = 0;
	this.totalTime = 0;
	this.moves = 0;
	}

	// ----------------------------------------------
	// instance methods

	/**
	 * Get the number of levels that the Minimax Player is currently looking
	 * ahead.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Set the number of levels that the Minimax Player will look ahead when
	 * getMove is next called
	 */
	public void setDepth(int anInt) {
		depth = anInt;
	}

	/**
	 * Passed a copy of the board, asked what move it would like to make.
	 *
	 * The MinimaxPlayer periodically polls the thread that makes this call to
	 * see if it is interrupted. If it is the player returns null.
	 *
	 * Looks ahead depth moves.
	 */
	public Move getMove(Board b) {
		MinimaxCalculator calc = new MinimaxCalculator(b, this, minPlayer);
		Move value = calc.calculateMove(depth);
		totalMoves += calc.totalMoves;
		totalTime += calc.totalTime;
		moves++;
		
		//
		System.out.println("Total Moves: " + totalMoves + "  Total Time: "
				+ totalTime + " Moves Played: " + moves);
		return value ;
	}

}// end MinimaxPlayer

/**
 * The MinimaxCalculator does the actual work of finding the minimax move. A new
 * calculator should be created each time a move is to be made. A calculator
 * should only be used once.
 */
final class MinimaxCalculator {
		public int totalMoves = 0;
	public long totalTime = 0;
	public int moves = 0;

	// -------------------------------------------------------
	// instance variables

	// the number of moves we have tried
	private int moveCount = 0;
	private long startTime;

	private Player minPlayer;
	private Player maxPlayer;
	private Board board;

	private final int MAX_POSSIBLE_STRENGTH;
	private final int MIN_POSSIBLE_STRENGTH;
	
	private boolean pruning = true; //toggles a/b pruning
	private int alpha;
	private int beta;
	// -------------------------------------------------------
	// constructors
	MinimaxCalculator(Board b, Player max, Player min) {
		board = b;
		maxPlayer = max;
		minPlayer = min;

		MAX_POSSIBLE_STRENGTH = board.getBoardStats().getMaxStrength(); // Integer.MAX_VALUE
		MIN_POSSIBLE_STRENGTH = board.getBoardStats().getMinStrength(); // Integer.MIN_VALUE
	}

	// -------------------------------------------------------
	// instance methods

	/**
	 * Calculate the move to be made.
	 */
	public Move calculateMove(int depth) {
		startTime = System.currentTimeMillis();

		// we have a problem, Houston...
		if (depth == 0) {
			System.out.println("Error, 0 depth in minumax player");
			Thread.dumpStack();
			return null;
		}

		Move[] moves = board.getPossibleMoves(maxPlayer);

		//index and value of best move so far
		int maxIndex = -1;
		int maxValue = MIN_POSSIBLE_STRENGTH;

		if(pruning) {
			// begin a/b pruning
			alpha = Integer.MIN_VALUE;
			beta  = Integer.MAX_VALUE;
		}
			
		// explore each move in turn
		for (int i = 0; i < moves.length; i++) {
			if (board.move(moves[i])) // move was legal (column was not full)
			{
				moveCount++; // global variable}
				
				//minimax
				int value = expandMinNode(depth-1);
				if(value > maxValue) {
					maxValue = value;
					maxIndex = i;
				}
				

				board.undoLastMove(); // undo exploratory move
			} // end if move made

			// if the thread has been interrupted, return immediately.
			if (Thread.currentThread().isInterrupted()) {
				return null;
			}

		}// end for all moves

		long stopTime = System.currentTimeMillis();
	totalMoves =  totalMoves + moveCount;
		totalTime =  totalTime +(stopTime - startTime);
		System.out.println("Number of moves tried = " + moveCount + "\tTime = "
				+ (stopTime - startTime) + " milliseconds");
		/*System.out.println("Total Moves: " + totalMoves + "  Total Time: "
				+ totalTime + " milliseconds");*/

		// maxIndex is the index of the move to be made
		return moves[maxIndex];
	}

	/**
	 * A max node returns the maximum score of its descendents.
	 */
	private int expandMaxNode(int depth) {
		// if cutoff test is satisfied
		if (depth == 0 || board.isGameOver()) {
			return board.getBoardStats().getStrength(maxPlayer);
		}

		// if not
		Move[] moves = board.getPossibleMoves(maxPlayer);

		// set maxValue to -infinity
		int maxValue = MIN_POSSIBLE_STRENGTH;
		
		// explore each move in turn
		for (int i = 0; i < moves.length; i++) {
			if (board.move(moves[i])) // move was legal (column was not full)
			{
				moveCount++; // global variable

				//minimax
				maxValue = max(maxValue, expandMinNode(depth - 1));
				
				board.undoLastMove(); // undo exploratory move
				
				if(pruning) {
					// a/b pruning
					if(maxValue >= beta)
						return maxValue;
					alpha = max(alpha, maxValue);
				}
			} // end if move made

		}// end for all moves

		return maxValue;

	}// end expandMaxNode

	/**
	 * A max node returns the minimum score of its descendents.
	 */
	private int expandMinNode(int depth) {
		// if cutoff test is satisfied
		if (depth == 0 || board.isGameOver()) {
			return board.getBoardStats().getStrength(maxPlayer);
		}

		// if not
		Move[] moves = board.getPossibleMoves(minPlayer);

		// set minValue to infinity
		int minValue = MAX_POSSIBLE_STRENGTH;
		
		// explore each move in turn
		for (int i = 0; i < moves.length; i++) {
			if (board.move(moves[i])) // move was legal (column was not full)
			{
				moveCount++; // global variable

				//minimax
				minValue = min(minValue, expandMaxNode(depth - 1));

				board.undoLastMove(); // undo exploratory move
				
				if(pruning) {
					//a/b pruning
					if(minValue <= alpha)
						return minValue;
					beta = min(beta, minValue);
				}
			} // end if move made

		}// end for all moves

		return minValue;

	}// end expandMaxNode

	// returns maximum of two ints
	private int max(int a, int b) {
		return a > b ? a : b;
	}

	// returns minimum of two ints
	private int min(int a, int b) {
		return a < b ? a : b;
	}

}