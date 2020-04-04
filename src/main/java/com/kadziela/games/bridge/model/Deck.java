package com.kadziela.games.bridge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.util.CardComparatorBySuitThenRank;
import com.kadziela.games.bridge.util.ComparisonUtils;

public class Deck 
{
	private Set<Card> byRankThenSuit = new TreeSet<Card>();
	
	public Deck() 
	{
		byRankThenSuit.add(new Card(Rank.ACE,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.ACE,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.ACE,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.ACE,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.KING,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.KING,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.KING,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.KING,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.QUEEN,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.QUEEN,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.QUEEN,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.QUEEN,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.JACK,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.JACK,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.JACK,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.JACK,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.TEN,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.TEN,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.TEN,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.TEN,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.NINE,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.NINE,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.NINE,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.NINE,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.EIGHT,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.EIGHT,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.EIGHT,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.EIGHT,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.SEVEN,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.SEVEN,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.SEVEN,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.SEVEN,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.SIX,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.SIX,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.SIX,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.SIX,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.FIVE,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.FIVE,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.FIVE,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.FIVE,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.FOUR,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.FOUR,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.FOUR,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.FOUR,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.THREE,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.THREE,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.THREE,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.THREE,Suit.CLUBS));
		byRankThenSuit.add(new Card(Rank.TWO,Suit.SPADES));
		byRankThenSuit.add(new Card(Rank.TWO,Suit.HEARTS));
		byRankThenSuit.add(new Card(Rank.TWO,Suit.DIAMONDS));
		byRankThenSuit.add(new Card(Rank.TWO,Suit.CLUBS));
	}
	
	public Set<Card> getByRankThenSuit() {return byRankThenSuit;}
	public Set<Card> getBySuitThenRank() 
	{
		Set<Card> bySuitThenRank = new TreeSet<Card>(new CardComparatorBySuitThenRank());
		bySuitThenRank.addAll(byRankThenSuit);
		return bySuitThenRank;
	}
	public List<Card> getShuffled()
	{
		List<Card> output = new ArrayList<Card>(byRankThenSuit);
		Collections.shuffle(output);
		return output;
	}
	
	public static void main (String[] args)
	{
		Deck d = new Deck();
		d.getByRankThenSuit().forEach(System.out::println);
		System.out.println("##############################################");
		d.getBySuitThenRank().forEach(System.out::println);
		System.out.println("##############################################");
		d.getShuffled().forEach(System.out::println);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((byRankThenSuit == null) ? 0 : byRankThenSuit.hashCode());
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
		Deck other = (Deck) obj;
		if (byRankThenSuit == null) {
			if (other.byRankThenSuit != null)
				return false;
		} else if (!byRankThenSuit.equals(other.byRankThenSuit))
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
}