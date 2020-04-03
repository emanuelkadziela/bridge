package com.kadziela.games.bridge.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

public class Table 
{	
	private final Long id = System.currentTimeMillis();
	private final Map<SeatPosition,SeatedPlayer> players = new ConcurrentHashMap<>();
	private final List<Bid> bids = new CopyOnWriteArrayList<>();
	private final Deck deck = new Deck();
	private SeatedPlayer currentDealer;
	private Contract currentContract;
	
	public Long getId() {return id;}	
	public Deck getDeck() {return deck;}
	public SeatedPlayer getCurrentDealer() {return currentDealer;}
	public void setCurrentDealer(SeatedPlayer cd) {currentDealer = cd;}
	
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
	public SeatedPlayer getPlayerAtPosition(SeatPosition sp) {return players.get(sp);}
	public Collection<SeatedPlayer> getAllSeatedPlayers(){return new HashSet<> (players.values());}
	public List<Bid> getCurrentBids() {return new ArrayList<>(bids);}
	public void addValidatedBid(Bid validatedBid) {bids.add(validatedBid);}
	public synchronized List<ValidBidOption> getCurrentBidOptions()
	{
		List<ValidBidOption> options = new ArrayList<ValidBidOption>();
		for (Bid b:bids) options.add(b.getBid());
		return options;
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
}