package com.kadziela.games.bridge.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
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
}