package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

import java.util.*;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.*;


public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private ArrayList<ScotlandYardPlayer> players;
	private ArrayList<Colour> playersCol;
	private ArrayList<ScotlandYardPlayer> detectives;
	private ScotlandYardPlayer currentPlayer;
	private boolean mrXWins;
	private boolean detectivesWin;
	private int playerAmount;
    private int round;
	private int playerNum;
	private int mrXAccLoc;
	private Set<Spectator> spectators;

	/**
	 * Model for ScotlandYard game.
	 * @param rounds - list of rounds. Returns true on reveal round. Never null or empty.
	 * @param graph - game graph. Never null.
	 * @param mrX - configuration for Mr. X player. Never null.
	 * @param firstDetective - configuration for first detective. Never null.
	 * @param restOfTheDetectives - list of configurations for rest of detectives. Never null.
	 */
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph, PlayerConfiguration mrX,
							 PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		initRounds(rounds);
		initGraph(graph);
		initSpectators();
		checkMrXBlack(mrX);
		ArrayList<PlayerConfiguration> allConfigurations =
				initAllConfigsList(mrX, firstDetective, restOfTheDetectives);
		ArrayList<PlayerConfiguration> detectiveConfigurations =
				initDetectiveConfigsList(firstDetective, restOfTheDetectives);
		checkConfigurations(allConfigurations);
		checkTicket(allConfigurations, detectiveConfigurations);
		initPlayers(allConfigurations);
	}

	/**
	 * Initialises rounds list and ensures that it is not empty.
	 */
  private void initRounds(List<Boolean> rounds) {
		this.rounds = Objects.requireNonNull(rounds);
		if (rounds.isEmpty())
			throw new IllegalArgumentException("Empty rounds");
		this.round = NOT_STARTED;
	}

	/**
	 * Initialises the graph and ensures that it is not empty.
	 */
  private void initGraph(Graph<Integer, Transport> graph) {
		this.graph = Objects.requireNonNull(graph);
		if (graph.isEmpty())
			throw new IllegalArgumentException("Empty graph");
  }

	/**
	 * Initialises spectators and ensures the list is not null.
	 */
	private void initSpectators() {
		this.spectators = new HashSet<>();
		this.spectators = Objects.requireNonNull(this.spectators);
	}

	/**
	 * Initialises Mr. X and makes sure the player is black.
	 */
  private void checkMrXBlack(PlayerConfiguration mrX) {
		if (mrX.colour != Black)
			throw new IllegalArgumentException("MrX not Black");
	}

	/**
	 * Initialises list of configurations for all players.
	 * @return an ArrayList of configurations for all players.
	 */
  private ArrayList<PlayerConfiguration> initAllConfigsList(PlayerConfiguration mrX, PlayerConfiguration firstDetective,
															PlayerConfiguration... restOfTheDetectives) {
		ArrayList<PlayerConfiguration> allConfigurations = new ArrayList<>();
		allConfigurations.add(0, requireNonNull(firstDetective));
		allConfigurations.add(0, requireNonNull(mrX));
		for (PlayerConfiguration configuration : restOfTheDetectives)
			allConfigurations.add(requireNonNull(configuration));
		return allConfigurations;
	}

	/**
	 * Initialises list of configurations for detectives only.
	 * @return an ArrayList of detecctive configurations only.
	 */
  private ArrayList<PlayerConfiguration> initDetectiveConfigsList(PlayerConfiguration firstDetective,
																	PlayerConfiguration... restOfTheDetectives) {
		ArrayList<PlayerConfiguration> detectiveConfigurations = new ArrayList<>();
		detectiveConfigurations.add(0, firstDetective);
		for (PlayerConfiguration configuration : restOfTheDetectives)
			detectiveConfigurations.add(requireNonNull(configuration));
    return detectiveConfigurations;
	}

	/**
	 * Checks configurations don't have duplicate locations or colours.
	 * @param allConfigurations - all players' configurations.
	 */
  private void checkConfigurations(ArrayList<PlayerConfiguration> allConfigurations) {
		Set<Integer> startLoc = new HashSet<>();
		Set<Colour> startCol = new HashSet<>();
		for (PlayerConfiguration configuration : allConfigurations) {
			if (startLoc.contains(configuration.location))
				throw new IllegalArgumentException("Duplicate location");
			else startLoc.add(configuration.location);
			if (startCol.contains(configuration.colour))
				throw new IllegalArgumentException("Duplicate colour");
			else startCol.add(configuration.colour);
		}
	}

	/**
	 * Checks that detectives have no Double or Secret tickets.
	 * Checks that no player has 'null' tickets.
	 */
	private void checkTicket(ArrayList<PlayerConfiguration> allConfigurations,
							 ArrayList<PlayerConfiguration> detectiveConfigurations) {

		ArrayList<Ticket> tickets = new ArrayList<>();
		tickets.add(Double); tickets.add(Secret); tickets.add(Taxi); tickets.add(Bus); tickets.add(Underground);

		for (PlayerConfiguration configuration : allConfigurations) {
			for (Ticket ticket : tickets) {
				if (!configuration.tickets.containsKey(ticket))
					throw new IllegalArgumentException("Null tickets");
			}
		}

	  for (PlayerConfiguration configuration : detectiveConfigurations) {
			if (configuration.tickets.get(Double) != 0 ||
					configuration.tickets.get(Secret) != 0)
				throw new IllegalArgumentException("Invalid ticket");
		}
	}

	/**
	 * Converts configurations to mutable ScotlandYardPlayer type.
	 */
  private void initPlayers(ArrayList<PlayerConfiguration> allConfigurations) {
		this.players = new ArrayList<>();
		for (PlayerConfiguration configuration : allConfigurations)
			this.players.add(new ScotlandYardPlayer(configuration.player, configuration.colour, configuration.location,
					configuration.tickets));

		this.playerAmount = this.players.size();

		this.playersCol = new ArrayList<>();
		for (ScotlandYardPlayer player : players)
			this.playersCol.add(player.colour());

	  this.detectives = new ArrayList<>(this.players);
	  ScotlandYardPlayer mrX = null;
	  for (ScotlandYardPlayer player : this.players)
		  if (player.colour().equals(Black)) mrX = player;
	  this.detectives.remove(mrX);

	    this.currentPlayer = this.players.get(0);

		this.mrXAccLoc = this.players.get(0).location();

	    this.players.get(0).location(0);
  }

	@Override
	public void registerSpectator(Spectator spectator) {
		if (spectator == null)
			throw new NullPointerException("Spectator is null");
		boolean alreadySpectator = false;
		for (Spectator occupiedSpectator : this.spectators)
			if (spectator.equals(occupiedSpectator))
				alreadySpectator = true;
		if (alreadySpectator)
			throw new IllegalArgumentException("Spectator is already on list");
		else this.spectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		boolean alreadySpectator = false;
		if (spectator == null)
			throw new NullPointerException("Spectator is null");
		for (Spectator occupiedSpectator : this.spectators)
			if (spectator.equals(occupiedSpectator))
				alreadySpectator = true;
		if (!alreadySpectator)
			throw new IllegalArgumentException("Not a spectator");
		else this.spectators.remove(spectator);
	}

	@Override
	public void startRotate() {
		if (isGameOver()) throw new IllegalStateException("Game is already over");
		int location = this.currentPlayer.location();
		if (this.currentPlayer.isMrX()) location = mrXAccLoc;
		this.playerNum = 0;
		requestMove(location);
	}

	private void requestMove(int location) {
		this.currentPlayer.player().makeMove(this, location, validMoves(this.currentPlayer, location), this);
	}

	/**
	 * Accepts move from UI and apples logic based on parameter.
	 * @param move - move given by UI. Never null.
	 */
	public void accept(Move move) {
		int location = this.currentPlayer.location();
		if (currentPlayer.isMrX()) location = mrXAccLoc;
		checkValidMove(move, location);
		MoveVisitor moveLogic = new MoveVisitor() {
			public void visit (TicketMove move) {
				TicketMove shownMove = move;
				if (currentPlayer.isMrX()) {
					round += 1;
					for (Spectator spectator : spectators)
						spectator.onRoundStarted(ScotlandYardModel.this, round);
					if (isRevealRound()) {
						shownMove = new TicketMove(currentPlayer.colour(), move.ticket(), currentPlayer.location());
					}
				}
				else players.get(0).addTicket(move.ticket());
				for (Spectator spectator : spectators)
					spectator.onMoveMade(ScotlandYardModel.this, shownMove);
				updateLocation(move.destination());
				currentPlayer.removeTicket(move.ticket());
			}

            public void visit (DoubleMove move) {
			    DoubleMove shownDoubleMove;
			    TicketMove shownFirstMove = move.firstMove();
			    TicketMove shownSecondMove = move.secondMove();

			    if (!rounds.get(round))
			        shownFirstMove = new TicketMove(currentPlayer.colour(), move.firstMove().ticket(),
                            currentPlayer.location());

                if (!rounds.get(round + 1))
                    shownSecondMove = new TicketMove(currentPlayer.colour(), move.secondMove().ticket(),
                            shownFirstMove.destination());

                shownDoubleMove = new DoubleMove(currentPlayer.colour(), shownFirstMove, shownSecondMove);
				for (Spectator spectator : spectators)
					spectator.onMoveMade(ScotlandYardModel.this, shownDoubleMove);
				currentPlayer.removeTicket(Double);
				round += 1;
				for (Spectator spectator : spectators)
					spectator.onRoundStarted(ScotlandYardModel.this, round);
				for (Spectator spectator : spectators)
					spectator.onMoveMade(ScotlandYardModel.this, shownFirstMove);
				currentPlayer.removeTicket(move.firstMove().ticket());
                updateLocation(move.firstMove().destination());
				round += 1;
				for (Spectator spectator : spectators)
					spectator.onRoundStarted(ScotlandYardModel.this, round);
				for (Spectator spectator : spectators)
					spectator.onMoveMade(ScotlandYardModel.this, shownSecondMove);
				currentPlayer.removeTicket(move.secondMove().ticket());
				updateLocation(move.finalDestination());
			}

			public void visit (PassMove move) {
				if (currentPlayer.isMrX())
					round += 1;
				for (Spectator spectator : spectators)
                    spectator.onMoveMade(ScotlandYardModel.this, move);
			}
		};

		move.visit(moveLogic);
		updatePlayer();

		if (isGameOver()) {
			for (Spectator spectator : this.spectators)
				spectator.onGameOver(this, getWinningPlayers());
			return;
		}

		if (this.playerNum == 0) {
			for (Spectator spectator : this.spectators)
				spectator.onRotationComplete(this);
			return;
		}

		int location2 = this.currentPlayer.location();
		if (this.currentPlayer.isMrX()) location2 = mrXAccLoc;
		requestMove(location2);
	}

	/**
	 * Compiles list of possible TicketMoves and DoubleMoves and checks for PassMove.
	 * @param player - player whose valid moves are to be checked. Never null.
	 * @param location - location the player is at.
	 * @return - complete list of ValidMoves. Never null or empty.
	 */
	private Set<Move> validMoves(ScotlandYardPlayer player, int location) {
		Set<Move> validMoves = new HashSet<>();
		Graph<Integer,Transport> currentGraph = getGraph();
		Node<Integer> currentNode = currentGraph.getNode(location);
		Collection<Edge<Integer,Transport>> edgesFrom = currentGraph.getEdgesFrom(currentNode);

		validMoves.addAll(ticketMoves(player, currentGraph, edgesFrom));
		if (player.hasTickets(Double) && (this.rounds.size() - this.round >= 2))
			validMoves.addAll(doubleMoves(player, currentGraph, ticketMoves(player, currentGraph, edgesFrom)));
		if (validMoves.isEmpty())
			validMoves.add(new PassMove(player.colour()));
		return validMoves;
	}

	/**
	 * @param edgesFrom - the edges that move from the source node. Never null.
	 * @return set of possible TicketMoves. Never null.
	 */
	private Set<TicketMove> ticketMoves(ScotlandYardPlayer player, Graph<Integer, Transport> graph,
										Collection<Edge<Integer, Transport>> edgesFrom) {
		Set<TicketMove> ticketMoves = new HashSet<>();

		for (Edge<Integer, Transport> edge : edgesFrom) {
			Ticket ticket = Ticket.fromTransport((edge.data()));
			Node<Integer> destination = edge.destination();

			if (player.hasTickets(ticket) &&
					!occupiedNodes(graph).contains(destination)) {
				ticketMoves.add(new TicketMove(player.colour(), ticket, destination.value()));

				if (player.hasTickets(Secret)) {
					ticketMoves.add(new TicketMove(player.colour(), Secret, destination.value()));
				}
			}
		}
		return ticketMoves;
	}

	/**
	 * @param ticketMoves - all possible first moves. Never null.
	 * @return set of possible DoubleMoves. Never null.
	 */
	private Set<DoubleMove> doubleMoves(ScotlandYardPlayer player, Graph<Integer, Transport> graph,
										Set<TicketMove> ticketMoves) {
		Set<DoubleMove> doubleMoves = new HashSet<>();

		for (TicketMove ticketMove1: ticketMoves) {
			Node<Integer> destination1 = graph.getNode(ticketMove1.destination());
			Ticket ticket1 = ticketMove1.ticket();

			Collection<Edge<Integer,Transport>> edgesFrom = graph.getEdgesFrom(destination1);

			for (Edge<Integer, Transport> edge : edgesFrom) {
				Ticket ticket2 = Ticket.fromTransport((edge.data()));
				Node<Integer> destination2 = edge.destination();

				TicketMove ticketMove2 = new TicketMove(player.colour(), ticket2, destination2.value());

				TicketMove ticketMove1S = new TicketMove(player.colour(), Secret, destination1.value());
				TicketMove ticketMove2S = new TicketMove(player.colour(), Secret, destination2.value());

				if (!destination1.value().equals(destination2.value()) &&
						!occupiedNodes(graph).contains(destination1)
						&& !occupiedNodes(graph).contains(destination2)) {
					if (player.hasTickets(ticket2) &&
							ticket1 != ticket2 || player.hasTickets(ticket1, 2)) {
						doubleMoves.add(new DoubleMove(player.colour(), ticketMove1, ticketMove2));
					}

					if (player.hasTickets(Secret)) {
						doubleMoves.add(new DoubleMove(player.colour(), ticketMove1S, ticketMove2));
						doubleMoves.add(new DoubleMove(player.colour(), ticketMove1, ticketMove2S));
					}

					if (player.hasTickets(Secret, 2)) {
						doubleMoves.add(new DoubleMove(player.colour(), ticketMove1S, ticketMove2S));
					}
				}
			}
		}
		return doubleMoves;
	}

	/**
	 * Checks which nodes cannot be moved onto as detective is occupying them.
	 * @return set of nodes. Never null or empty.
	 */
	private Set<Node<Integer>> occupiedNodes(Graph<Integer,Transport> graph) {
    Set<Node<Integer>> occupiedNodes = new HashSet<>();
		for (ScotlandYardPlayer player : this.detectives) {
			Node<Integer> playerNode = graph.getNode(player.location());
			occupiedNodes.add(playerNode);
		}
		return occupiedNodes;
	}

	/**
	 * Checks whether move supplied is valid.
	 * @param move - move to be checked. Never null.
	 * @param location - current location of the player.
	 */
	private void checkValidMove(Move move, int location) {
		if (move == null)
			throw new NullPointerException("Callback is null");
		if (!validMoves(this.currentPlayer, location).contains(move))
		    throw new IllegalArgumentException("Invalid move");
	}

	/**
	 * Changes location of player to new destination. Changes Mr. X's hidden location every time but only changes
	 * the player's location on reveal round.
	 * @param destination - new destination.
	 */
	private void updateLocation(int destination) {
		if (currentPlayer.isMrX()) {
			this.mrXAccLoc = destination;
			if (isRevealRound()) {
				this.currentPlayer.location(destination);
			}
		}
		else this.currentPlayer.location(destination);
	}

	/**
	 *
	 */
	private void updatePlayer() {
		if (this.playerNum < (this.playerAmount - 1))
		    this.playerNum = this.playerNum + 1;
		else this.playerNum = 0;
		this.currentPlayer = this.players.get(this.playerNum);
	}


	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableCollection(this.spectators);
	}

	@Override
	public List<Colour> getPlayers() {
		return Collections.unmodifiableList(playersCol);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
    Set<Colour> winningPlayers = new HashSet<>();
		if (this.mrXWins) winningPlayers.add(Black);
		else if (this.detectivesWin) {
            winningPlayers.addAll(playersCol);
			winningPlayers.remove(Black);
		}
		if (!isGameOver()) return Collections.emptySet();
		else return Collections.unmodifiableSet(winningPlayers);
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		for (ScotlandYardPlayer player : players) {
			if (player.colour().equals(colour))
				return player.location();
		}
		throw new IllegalArgumentException("Colour does not match a player");
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
    for (ScotlandYardPlayer player : this.players) {
	    if (player.colour().equals(colour))
			    return player.tickets().get(ticket);
		}
		throw new IllegalArgumentException("Colour does not match a player");
	}

	/**
	 * Checks several end game conditions to see if the game is over.
	 * @return true if game is over. False otherwise.
	 */
	@Override
	public boolean isGameOver() {
    this.mrXWins = false;
    this.detectivesWin = false;
	if (roundsOver() || detectivesStuck()) this.mrXWins = true;
    if (mrXStuck() || mrXLanded()) this.detectivesWin = true;
    return (mrXWins || detectivesWin);
	}

	private boolean roundsOver() {
		if (this.playerNum == 0 && this.round == this.rounds.size() && !detectivesWin)
			return true;
		return false;
	}

	/**
	 * @return false if any detective has a move to make other than a PassMove. True otherwise.
	 */
	private boolean detectivesStuck() {
		boolean detectiveFree = false;
		for (ScotlandYardPlayer detective : this.detectives)
			if (!(validMoves(detective, detective.location()).contains(new PassMove(detective.colour()))))
				detectiveFree = true;
		return !detectiveFree;
	}

	/**
	 * @return false if Mr. X has a move to make other than a PassMove. True otherwise.
	 */
	private boolean mrXStuck() {
		if (this.playerNum == 0 && this.round > 0 && validMoves(this.players.get(0), this.mrXAccLoc).contains(new PassMove(Black)))
			return true;
		return false;
	}

	/**
	 * @return true if Mr. X has been landed on by any of the detectives. False otherwise.
	 */
	private boolean mrXLanded() {
		for (ScotlandYardPlayer detective : this.detectives) {
			if (detective.location() == this.mrXAccLoc)
				return true;
		}
		return false;
	}

	@Override
	public Colour getCurrentPlayer() {
		return this.currentPlayer.colour();
	}

	@Override
	public int getCurrentRound() {
		return this.round;
	}

	@Override
	public boolean isRevealRound() {
	    return this.rounds.get(this.round - 1);
	}

	@Override
	public List<Boolean> getRounds() {
	  return Collections.unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<>(graph);
	}

}
