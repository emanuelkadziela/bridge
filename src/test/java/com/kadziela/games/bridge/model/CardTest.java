package com.kadziela.games.bridge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}