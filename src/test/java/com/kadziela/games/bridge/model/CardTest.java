package com.kadziela.games.bridge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;

@SpringBootTest
public class CardTest 
{	
	@Test
	void testCardComparison()
	{
		Card lowerRank = new Card(Rank.FIVE, Suit.DIAMONDS);
		Card higherRank = new Card(Rank.SIX, Suit.DIAMONDS);
		assertEquals(1, higherRank.compareTo(lowerRank));
		assertEquals(-1, lowerRank.compareTo(higherRank));		
		assertEquals(0, lowerRank.compareTo(lowerRank));		
		Card lowerSuit = new Card(Rank.FIVE, Suit.HEARTS);
		Card higherSuit = new Card(Rank.FIVE, Suit.SPADES);
		assertEquals(1, higherSuit.compareTo(lowerSuit));
		assertEquals(-1, lowerSuit.compareTo(higherSuit));		
		assertEquals(0, lowerSuit.compareTo(lowerSuit));
		TreeSet<Card> set = new TreeSet<>(); 
		set.add(lowerRank);
		set.add(higherRank);
		set.add(lowerSuit);
		set.add(higherSuit);
		assertEquals(lowerRank, set.first());
		assertEquals(higherRank, set.last());
	}
	@Test
	void testCreateFromLetters()
	{
		Card card = new Card("10","H");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.TEN,Suit.HEARTS));
		card = new Card("9","D");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.NINE,Suit.DIAMONDS));		
		card = new Card("A","C");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.ACE,Suit.CLUBS));
		card = new Card("J","S");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.JACK,Suit.SPADES));
		assertThrows(IllegalArgumentException.class, () -> new Card("",""), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("11","H"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("Z","H"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("10","W"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("1","S"), "rank and suit cannot be null");
	}
	@Test
	void testCreateFromSymbols()
	{
		Card card = new Card("10H");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.TEN,Suit.HEARTS));
		card = new Card("9D");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.NINE,Suit.DIAMONDS));		
		card = new Card("AC");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.ACE,Suit.CLUBS));
		card = new Card("JS");
		assertNotNull(card);
		assertEquals(card, new Card(Rank.JACK,Suit.SPADES));
		assertThrows(IllegalArgumentException.class, () -> new Card(""), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("11H"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("ZH"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("10W"), "rank and suit cannot be null");
		assertThrows(IllegalArgumentException.class, () -> new Card("1S"), "rank and suit cannot be null");
	}
}