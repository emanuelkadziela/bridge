package com.kadziela.games.bridge.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;

public class Table 
{	
	private final Long id = System.currentTimeMillis();
	private final Map<SeatPosition,SeatedPlayer> players = new ConcurrentHashMap<SeatPosition, SeatedPlayer>();
	private final Deck deck = new Deck();
	private SeatedPlayer currentDealer;
	
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
	public Collection<SeatedPlayer> getAllSeatedPlayers(){return new HashSet<SeatedPlayer> (players.values());}
	public void deal()
	{
		List<Card> shuffledDeck = getDeck().getShuffled();
		
		players.get(SeatPosition.NORTH).takeNewCards(shuffledDeck.subList(0, 13));
		players.get(SeatPosition.EAST).takeNewCards(shuffledDeck.subList(13, 26));
		players.get(SeatPosition.SOUTH).takeNewCards(shuffledDeck.subList(26, 39));
		players.get(SeatPosition.WEST).takeNewCards(shuffledDeck.subList(39, 52));
	}
}