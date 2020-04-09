package com.kadziela.games.bridge.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.kadziela.games.bridge.NeedsCleanup;
import com.kadziela.games.bridge.controllers.ContractController;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

public class Table implements NeedsCleanup
{	
	private static final Logger logger = LogManager.getLogger(Table.class);

	private final Long id = System.currentTimeMillis();
	private final Map<SeatPosition,SeatedPlayer> players = new ConcurrentHashMap<>();
	private final List<Bid> bids = new CopyOnWriteArrayList<>();
	private final Deck deck = new Deck();
	private SeatedPlayer currentDealer;
	private Contract currentContract;
	private final List<Trick> tricks = new CopyOnWriteArrayList<>();
	private final List<PlayedCard> partialTrick = new CopyOnWriteArrayList<>();
	
	public Long getId() {return id;}	
	public Deck getDeck() {return deck;}
	public SeatedPlayer getCurrentDealer() {return currentDealer;}
	public SeatedPlayer getPlayerAtPosition(SeatPosition sp) {return players.get(sp);}
	public Collection<SeatedPlayer> getAllSeatedPlayers(){return new HashSet<> (players.values());}
	public List<Bid> getCurrentBids() {return new ArrayList<>(bids);}
	public List<Trick> getTricks() {return new ArrayList<>(tricks);}
	public synchronized List<ValidBidOption> getCurrentBidOptions()
	{
		List<ValidBidOption> options = new ArrayList<ValidBidOption>();
		for (Bid b:bids) options.add(b.getBid());
		return options;
	}
	public void setCurrentDealer(SeatedPlayer cd) {currentDealer = cd;}	
	public void addValidatedBid(Bid validatedBid) {bids.add(validatedBid);}
	
	public void sitDown(SeatPosition sp, Player p) throws IllegalStateException
	{
		SeatedPlayer extantOccupant = players.get(sp); 
		if (extantOccupant != null)
		{
			throw new IllegalStateException("someone ("+extantOccupant+") is already seated in position "+sp+" so player "+p+" cannot sit down there");
		}
		players.put(sp, new SeatedPlayer(sp, p));		
	}
	public void standUp(SeatPosition sp) throws IllegalStateException
	{
		if (players.get(sp) == null)
		{
			throw new IllegalStateException("nobody is sitting at position "+sp);
		}
		SeatedPlayer p = players.remove(sp);
	}
	public void deal()
	{
		List<Card> shuffledDeck = getDeck().getShuffled();
		
		players.get(SeatPosition.NORTH).takeNewCards(shuffledDeck.subList(0, 13));
		players.get(SeatPosition.EAST).takeNewCards(shuffledDeck.subList(13, 26));
		players.get(SeatPosition.SOUTH).takeNewCards(shuffledDeck.subList(26, 39));
		players.get(SeatPosition.WEST).takeNewCards(shuffledDeck.subList(39, 52));
	}
	public Contract createNewContract()
	{
		currentContract = new Contract(getCurrentBids());
		return currentContract;
	}
	public Contract getCurrentContract() {return currentContract;}
	/**
	 * Plays the given card (which includes the position). If four cards have been played, returns the resulting trick. 
	 * @param card the card with the position
	 * @return a trick if four legal (unequal and from four distinct positions) cards have been played
	 * @throws IllegalArgumentException if there are any problems with inputs to this method 
	 * 	(for example a card is played from the same position more than once, or the same card is played more than once, or someone doesn't follow suit, etc.) 
	 * @throws IllegalStateException If there are any systemic issues, like an attempt to play after 13 tricks have been collected
	 */
	public Trick playCard(PlayedCard card) throws IllegalArgumentException,IllegalStateException
	{
		Assert.state(tricks.size() < 13, "13 (or more?) tricks have already been played");
		Assert.state(partialTrick.size() < 4, "more than 4 cards have been played for one trick");
		SeatPosition leader = SeatPosition.nextPlayer(currentContract.getDeclarer().getPosition());
		if (!tricks.isEmpty())
		{
			leader = tricks.get(tricks.size()-1).getWinner().getPosition();
		}
		if (partialTrick.isEmpty())
		{
			Assert.isTrue(card.getPosition().equals(leader),String.format("the card should be played by %s, not %s",leader,card.getPosition()));
			players.get(leader).playCard(card.getCard());
			partialTrick.add(card);
			return null;
		}
		PlayedCard previous = partialTrick.get(partialTrick.size()-1);
		SeatPosition throwCard = SeatPosition.nextPlayer(previous.getPosition());
		Assert.isTrue(card.getPosition().equals(throwCard),String.format("the card should be played by %s, not %s",throwCard,card.getPosition()));
		Suit led = partialTrick.get(0).getCard().getSuit();
		if(!(led.equals(card.getCard().getSuit())))			
		{
			Assert.isTrue((!players.get(card.getPosition()).hasSuit(led)), String.format("%s was led and %s has cards in that suit, must follow suit",led,card.getPosition()));			
		}
		players.get(card.getPosition()).playCard(card.getCard());
		if (partialTrick.size() == 3)
		{
			logger.debug("fourth card completes the trick");
			Trick trick = new Trick(partialTrick.get(0),partialTrick.get(1),partialTrick.get(2),card,currentContract.getSuit());
			partialTrick.clear();
			return trick;
		}
		partialTrick.add(card);
		return null;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bids == null) ? 0 : bids.hashCode());
		result = prime * result + ((currentContract == null) ? 0 : currentContract.hashCode());
		result = prime * result + ((currentDealer == null) ? 0 : currentDealer.hashCode());
		result = prime * result + ((deck == null) ? 0 : deck.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((players == null) ? 0 : players.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		if (bids == null) {
			if (other.bids != null)
				return false;
		} else if (!bids.equals(other.bids))
			return false;
		if (currentContract == null) {
			if (other.currentContract != null)
				return false;
		} else if (!currentContract.equals(other.currentContract))
			return false;
		if (currentDealer == null) {
			if (other.currentDealer != null)
				return false;
		} else if (!currentDealer.equals(other.currentDealer))
			return false;
		if (deck == null) {
			if (other.deck != null)
				return false;
		} else if (!deck.equals(other.deck))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (players == null) {
			if (other.players != null)
				return false;
		} else if (!players.equals(other.players))
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
	@Override public void cleanupAfterGame(Long tableId) 
	{
		bids.clear();
		currentDealer = null;
		currentContract = null;
		tricks.clear();
		partialTrick.clear();		
		for (SeatedPlayer player : players.values()) player.cleanupAfterGame(tableId);		
	}
	@Override public void cleanupAfterRubber(Long tableId) 
	{
		players.clear();
		bids.clear();
		currentDealer = null;
		currentContract = null;
		tricks.clear();
		partialTrick.clear();		
		for (SeatedPlayer player : players.values()) player.cleanupAfterRubber(tableId);		
	}
	@Override
	public void cleanupAfterPlay() 
	{
		bids.clear();
		currentDealer = null;
		currentContract = null;
		tricks.clear();
		partialTrick.clear();
		players.values().forEach(SeatedPlayer::cleanupAfterPlay);		 		
	}
}