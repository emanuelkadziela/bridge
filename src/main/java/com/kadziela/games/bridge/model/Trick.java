package com.kadziela.games.bridge.model;

import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.util.Assert;
import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;

public final class Trick 
{
	private PlayedCard leader;
	private PlayedCard second;
	private PlayedCard third;
	private PlayedCard fourth;
	private BidSuit trump;
	
	NavigableSet<PlayedCard> internal = new ConcurrentSkipListSet<>();

	public Trick(PlayedCard ldr, PlayedCard scd, PlayedCard thd, PlayedCard frt, BidSuit tmp) throws IllegalArgumentException 
	{
		Assert.notNull(ldr, "Cannot construct a valid trick with a null card");
		Assert.notNull(scd, "Cannot construct a valid trick with a null card");
		Assert.notNull(thd, "Cannot construct a valid trick with a null card");
		Assert.notNull(thd, "Cannot construct a valid trick with a null card");

		Set<SeatPosition> positions = new ConcurrentSkipListSet<>();
		positions.add(ldr.getPostion());
		positions.add(scd.getPostion());
		positions.add(thd.getPostion());
		positions.add(frt.getPostion());		
		Assert.isTrue(positions.size() == 4,"Each card in every trick must come from each of the four positions at the table");

		internal.add(ldr);
		internal.add(scd);
		internal.add(thd);
		internal.add(frt);		
		Assert.isTrue(internal.size() == 4, "A trick must have exactly four distinct played cards");
		
		leader = ldr;
		second = scd;
		third = thd;
		fourth = frt;
		trump = tmp;
	}
	public PlayedCard getLeader() {return leader;}
	public PlayedCard getSecond() {return second;}
	public PlayedCard getThird() {return third;}
	public PlayedCard getFourth() {return fourth;}
	public BidSuit getTrump() {return trump;}
	public PlayedCard getWinner() throws IllegalArgumentException,IllegalStateException
	{
		PlayedCard bestLed = getBestInSuit(leader.getCard().getSuit());
		//if we're playing no trump, return the best card in the suit that was led
		//if we're playing in a trump game, and trump was led, return the highest card in the led, trump suit
		if (trump.equals(BidSuit.NO_TRUMP) || leader.getCard().getSuit().equals(Suit.valueOf(trump.toString())))
		{
			return bestLed;
		}
		//we're playing a trump game, but trump wasn't led
		//so, let's see if trump was played, and if so, return the best one		
		PlayedCard bestTrump = getBestInSuit(Suit.valueOf(trump.toString()));
		if (bestTrump != null)
		{
			return bestTrump;
		}
		//trump was not played, so return the best card in the suit that was led
		return bestLed;
	}	
	private PlayedCard getBestInSuit(Suit suit)
	{
		PlayedCard result = null;
		for (PlayedCard card:internal)
		{
			if (card.getCard().getSuit().equals(suit))
			{
				if (result == null) result = card;
				else if (card.getCard().getRank().ordinal() > result.getCard().getRank().ordinal()) result = card;
			}
		}
		return result;
	}
	@Override public String toString() {return new Gson().toJson(this);}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
		result = prime * result + ((leader == null) ? 0 : leader.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		result = prime * result + ((trump == null) ? 0 : trump.hashCode());
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
		Trick other = (Trick) obj;
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		if (leader == null) {
			if (other.leader != null)
				return false;
		} else if (!leader.equals(other.leader))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		if (trump != other.trump)
			return false;
		return true;
	}	
}