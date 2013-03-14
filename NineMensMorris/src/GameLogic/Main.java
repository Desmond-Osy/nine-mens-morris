package GameLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import GameLogic.IAPlayer.Move;

public class Main {
	public Game game;
	public BufferedReader input;
	
	public static void main(String []args) throws Exception {
		System.out.println("Nine Men's Morris starting...");
		Main maingame = new Main();
		maingame.input = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("(L)OCAL or (N)ETWORK?");
		String userInput = maingame.input.readLine();
		userInput = userInput.toUpperCase();
		
		if(userInput.compareTo("LOCAL") == 0 || userInput.compareTo("L") == 0) {
			maingame.createLocalGame();
		} else if(userInput.compareTo("NETWORK") == 0 || userInput.compareTo("N") == 0) {
			maingame.createNetworkGame();
		} else {
			System.out.println("UNKNOWN COMMAND");
			System.exit(-1);
		}
	}
	
	public void createLocalGame() throws IOException {
		game = new LocalGame();
		System.out.println("Player 1: (H)UMAN or (C)PU?");
		String userInput = input.readLine();
		userInput = userInput.toUpperCase();
		Player p1 = null, p2 = null;
		
		if(userInput.compareTo("HUMAN") == 0 || userInput.compareTo("H") == 0) {
			p1 = new HumanPlayer("Souto",Player.PLAYER_1);
		} else if(userInput.compareTo("CPU") == 0 || userInput.compareTo("C") == 0) {
			p1 = new MinimaxIAPlayer(Player.PLAYER_1,2);
		} else {
			System.out.println("Command unknown");
			System.exit(-1);
		}
		
		System.out.println("Player 2: (H)UMAN or (C)PU?");
		userInput = input.readLine();
		userInput = userInput.toUpperCase();
		
		if(userInput.compareTo("HUMAN") == 0 || userInput.compareTo("H") == 0) {
			p2 = new HumanPlayer("Miguel", Player.PLAYER_2);
		} else if(userInput.compareTo("CPU") == 0 || userInput.compareTo("C") == 0) {
			p2 = new MinimaxIAPlayer(Player.PLAYER_2,2);
		} else {
			System.out.println("Command unknown");
			System.exit(-1);
		}
		
		((LocalGame)game).setPlayers(p1, p2);
		while(game.getGamePhase() == Game.PLACING_PHASE) {
			while(true) {
				Player p = ((LocalGame)game).getCurrentTurnPlayer();
				int boardIndex;
				if(p.isIA()) {
					boardIndex = ((MinimaxIAPlayer)p).getIndexToPlacePiece(game.gameBoard);
					System.out.println(p.getName()+" placed piece on "+boardIndex);
				} else {
					game.printGameBoard();
					System.out.println(p.getName()+" place piece on: ");
					userInput = input.readLine();
					userInput = userInput.toUpperCase();
					boardIndex = Integer.parseInt(userInput);
				}
				if(game.setPiece(boardIndex, p.getPlayerId())) {
					if(game.madeAMill(boardIndex, p.getPlayerId())) {
						int otherPlayerId = (p.getPlayerId() == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
						while(true) {
							if(p.isIA()){
								boardIndex = ((MinimaxIAPlayer)p).getIndexToRemovePieceOfOpponent(game.gameBoard);
								System.out.println(p.getName()+" removes opponent piece on "+boardIndex);
							} else {
								System.out.println("You made a mill. You can remove a piece of your oponent: ");
								userInput = input.readLine();
								userInput = userInput.toUpperCase();
								boardIndex = Integer.parseInt(userInput);
							}
							if(game.removePiece(boardIndex, otherPlayerId)) {
								break;
							} else {
								System.out.println("You can't remove a piece from there. Try again");
							}
						}
					}
					((LocalGame)game).updateCurrentTurnPlayer();
					break;
				} else {
					System.out.println("You can't place a piece there. Try again");
				}
			}
		}
		
		System.out.println("The pieces are all placed. Starting the fun part...");
		int numMoves = 0;
		while(!game.gameIsOver()) {
			while(true) {
				Player p = ((LocalGame)game).getCurrentTurnPlayer();
				int initialIndex, finalIndex;
				if(p.isIA()) {
					Move move = ((IAPlayer)p).getPieceMove(game.gameBoard);
					initialIndex = move.src;
					finalIndex = move.dest;
					System.out.println(p.getName()+" moved piece from "+initialIndex+" to "+finalIndex);
				} else {
					game.printGameBoard();
					System.out.println(p.getName()+" it's your turn. Input PIECE_POS:PIECE_DEST");
					userInput = input.readLine();
					userInput = userInput.toUpperCase();
					String[] positions = userInput.split(":");
					initialIndex = Integer.parseInt(positions[0]);
					finalIndex = Integer.parseInt(positions[1]);
					System.out.println("Move piece from "+initialIndex+" to "+finalIndex);
				}
				if(game.positionHasPieceOfPlayer(initialIndex, p.getPlayerId())) {
					if(game.positionIsAvailable(finalIndex) && (game.validMove(initialIndex, finalIndex) || p.canItFly())) {
						game.movePieceFromTo(initialIndex, finalIndex, p.getPlayerId());
						numMoves++;
						if(game.madeAMill(finalIndex, p.getPlayerId())) {
							int otherPlayerId = (p.getPlayerId() == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
							int boardIndex;
							while(true) {
								if(p.isIA()){
									boardIndex = ((IAPlayer)p).getIndexToRemovePieceOfOpponent(game.gameBoard);
									System.out.println(p.getName()+" removes opponent piece on "+boardIndex);
								} else {
									System.out.println("You made a mill! You can remove a piece of your oponent: ");
									userInput = input.readLine();
									userInput = userInput.toUpperCase();
									boardIndex = Integer.parseInt(userInput);
								}
								if(game.removePiece(boardIndex, otherPlayerId)) {
									break;
								} else {
									System.out.println("It couldn't be done! Try again.");
								}
							}
						}
						game.checkGameIsOver();
						if(game.gameIsOver()) {
							game.printGameBoard();
							break;
						}
						((LocalGame)game).updateCurrentTurnPlayer();
					} else {
						System.out.println("That's not a valid move");
					}
				} else {
					System.out.println("No piece on that position or it isn't yours");
				}
			}
		}
		System.out.println("Num moves: "+numMoves);
	}
	
	public void createNetworkGame() throws IOException, InterruptedException {
		System.out.println("(S)ERVER or (C)LIENT?");
		String userInput = input.readLine();
		userInput = userInput.toUpperCase();
		NetworkGame game = null;
		
		if(userInput.compareTo("SERVER") == 0 || userInput.compareTo("S") == 0) {
			game = new ServerGame();
		} else if(userInput.compareTo("CLIENT") == 0 || userInput.compareTo("C") == 0) {
			game = new ClientGame();
		} else {
			System.out.println("UNKNOWN COMMAND");
			System.exit(-1);
		}
		
		System.out.println("Player: (H)UMAN or (C)PU?");
		userInput = input.readLine();
		userInput = userInput.toUpperCase();
		Player p = null;
		
		if(userInput.compareTo("HUMAN") == 0 || userInput.compareTo("H") == 0) {
			if(game instanceof ServerGame) {
				p = new HumanPlayer("Miguel",Player.PLAYER_1);
			} else {
				p = new HumanPlayer("Aida",Player.PLAYER_2);
			}
		} else if(userInput.compareTo("CPU") == 0 || userInput.compareTo("C") == 0) {
			if(game instanceof ServerGame) {
				p = new MinimaxIAPlayer(Player.PLAYER_1, 2);
			} else {
				p = new MinimaxIAPlayer(Player.PLAYER_2, 2);
			}
		} else {
			System.out.println("UNKNOWN COMMAND");
			System.exit(-1);
		}
		
		game.setPlayer(p);
		int numberTries = 3;			
	
		if(game instanceof ClientGame) {
			//((ClientGame)game).connectToServer(InetAddress.getLocalHost().getHostAddress());
			while(true) {
				try {
					System.out.println("Trying to connect to server...");
					((ClientGame)game).connectToServer("localhost");
					break;
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("NO SERVER DETECTED");
					if(--numberTries == 0) {
						System.exit(-1);
					}
					Thread.sleep(10000);
				}
			}
		} else {
			while(true) {
				if(game.hasConnection()) {
					break;
				}
				System.out.println("WAITING FOR CLIENT");
				Thread.sleep(10000);
			}
		}
		
		// TODO it can't be always the server player the first one, change that!
		if(game instanceof ServerGame) {
			game.setTurn(true);
		}
		
		while(game.getGamePhase() == Game.PLACING_PHASE) {
			while(true) {
				if(game.isThisPlayerTurn()) {
					Player player = game.getPlayer();
					int boardIndex;
					if(p.isIA()) {
						boardIndex = ((MinimaxIAPlayer)player).getIndexToPlacePiece(game.gameBoard);
						System.out.println(player.getName()+" placed piece on "+boardIndex);
					} else {
						game.printGameBoard();
						System.out.println(player.getName()+" place piece on: ");
						userInput = input.readLine();
						userInput = userInput.toUpperCase();
						boardIndex = Integer.parseInt(userInput);
					}
					
					if(game.setPiece(boardIndex)) {
						if(game.madeAMill(boardIndex, player.getPlayerId())) {
							int otherPlayerId = (player.getPlayerId() == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
							while(true) {
								if(player.isIA()){
									boardIndex = ((IAPlayer)player).getIndexToRemovePieceOfOpponent(game.gameBoard);
									System.out.println(player.getName()+" removes opponent piece on "+boardIndex);
								} else {
									System.out.println("You made a mill. You can remove a piece of your oponent: ");
									userInput = input.readLine();
									userInput = userInput.toUpperCase();
									boardIndex = Integer.parseInt(userInput);
								}
								if(game.removePiece(boardIndex)) {
									break;
								} else {
									System.out.println("You can't remove a piece from there. Try again");
								}
							}
						}
						game.checkGameIsOver();
						if(game.gameIsOver()) {
							game.printGameBoard();
							break;
						}
						game.setTurn(false);
					}
				}
				Thread.sleep(100);
			}
		}
		
	}
}
