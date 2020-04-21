package com.kadziela.games.bridge.model;

import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.util.ComparisonUtils;

public class Card implements Comparable<Card>
{
	private Rank rank;
	private Suit suit;

	public Card(Rank rank, Suit suit) {this.rank = rank;this.suit = suit;}
	public Card(String rnk, String st) throws IllegalArgumentException 
	{
		rank = Rank.getFromLetter(rnk);
		suit = Suit.getFromLetter(st);
		if (rank == null || suit == null) throw new IllegalArgumentException("rank and suit cannot be null");
	}
	public Card(String symbol) throws IllegalArgumentException 
	{
		if (symbol.length() == 2)
		{
			rank = Rank.getFromLetter(symbol.split("")[0]);
			suit = Suit.getFromLetter(symbol.split("")[1]);
		}
		if (symbol.length() == 3)
		{
			rank = Rank.getFromLetter(symbol.split("")[0]+symbol.split("")[1]);
			suit = Suit.getFromLetter(symbol.split("")[2]);
		}
		if (rank == null || suit == null) throw new IllegalArgumentException("rank and suit cannot be null");
	}
	
	public Rank getRank() {return rank;}
	public void setRank(Rank rank) {this.rank = rank;}
	public Suit getSuit() {return suit;}
	public void setSuite(Suit suit) {this.suit = suit;}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rank == null) ? 0 : rank.hashCode());
		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Card)) return false;
		Card other = (Card) obj;
		if (rank != other.rank) return false;
		if (suit != other.suit) return false;
		return true;
	}
	
	@Override public String toString() {return new Gson().toJson(this);}
	/**
	 * compares (sorts) by rank first then suit, finally id
	 */
	@Override
	public int compareTo(Card o) 
	{
		if (o == null) return 1;		
		return ComparisonUtils.nullSafeCompare(rank, o.getRank()) != 0 ? ComparisonUtils.nullSafeCompare(rank, o.getRank()) : 
			ComparisonUtils.nullSafeCompare(suit, o.getSuit());
	}
}