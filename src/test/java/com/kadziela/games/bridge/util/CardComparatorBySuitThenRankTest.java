package com.kadziela.games.bridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;

@SpringBootTest
public class CardComparatorBySuitThenRankTest 
{
	@Test
	void testSortingSameSuit()
	{
		List<Card> cards = new ArrayList<>();
		cards.add(new Card(Rank.KING, Suit.CLUBS));
		cards.add(new Card(Rank.ACE, Suit.CLUBS));
		cards.add(new Card(Rank.TWO, Suit.CLUBS));
		cards.add(new Card(Rank.FIVE, Suit.CLUBS));
		cards.add(new Card(Rank.SIX, Suit.CLUBS));
		cards.add(new Card(Rank.TEN, Suit.CLUBS));
		cards.add(new Card(Rank.JACK, Suit.CLUBS));
		cards.add(new Card(Rank.QUEEN, Suit.CLUBS));
		cards.add(new Card(Rank.FOUR, Suit.CLUBS));
		Collections.sort(cards, new CardComparatorBySuitThenRank());
		
		assertEquals(new Card(Rank.TWO, Suit.CLUBS) , cards.get(0));
		assertEquals(new Card(Rank.FOUR, Suit.CLUBS) , cards.get(1));
		assertEquals(new Card(Rank.FIVE, Suit.CLUBS) , cards.get(2));
		assertEquals(new Card(Rank.SIX, Suit.CLUBS) , cards.get(3));
		assertEquals(new Card(Rank.TEN, Suit.CLUBS) , cards.get(4));
		assertEquals(new Card(Rank.JACK, Suit.CLUBS) , cards.get(5));
		assertEquals(new Card(Rank.QUEEN, Suit.CLUBS) , cards.get(6));
		assertEquals(new Card(Rank.KING, Suit.CLUBS) , cards.get(7));
		assertEquals(new Card(Rank.ACE, Suit.CLUBS) , cards.get(8));
	}
	@Test
	void testSortingDifferentSuits()
	{
		List<Card> cards = new ArrayList<>();
		cards.add(new Card(Rank.TWO, Suit.CLUBS));
		cards.add(new Card(Rank.FIVE, Suit.CLUBS));
		cards.add(new Card(Rank.KING, Suit.CLUBS));
		cards.add(new Card(Rank.KING, Suit.DIAMONDS));
		cards.add(new Card(Rank.KING, Suit.HEARTS));
		cards.add(new Card(Rank.KING, Suit.SPADES));
		cards.add(new Card(Rank.SIX, Suit.CLUBS));
		cards.add(new Card(Rank.TEN, Suit.CLUBS));
		cards.add(new Card(Rank.JACK, Suit.CLUBS));
		cards.add(new Card(Rank.QUEEN, Suit.CLUBS));
		cards.add(new Card(Rank.FOUR, Suit.CLUBS));
		cards.add(new Card(Rank.ACE, Suit.CLUBS));
		cards.add(new Card(Rank.ACE, Suit.DIAMONDS));
		cards.add(new Card(Rank.ACE, Suit.HEARTS));
		cards.add(new Card(Rank.ACE, Suit.SPADES));
		cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS));
		cards.add(new Card(Rank.QUEEN, Suit.HEARTS));
		cards.add(new Card(Rank.QUEEN, Suit.SPADES));
		Collections.sort(cards, new CardComparatorBySuitThenRank());
		
		assertEquals(new Card(Rank.TWO, Suit.CLUBS) , cards.get(0));
		assertEquals(new Card(Rank.FOUR, Suit.CLUBS) , cards.get(1));
		assertEquals(new Card(Rank.FIVE, Suit.CLUBS) , cards.get(2));
		assertEquals(new Card(Rank.SIX, Suit.CLUBS) , cards.get(3));
		assertEquals(new Card(Rank.TEN, Suit.CLUBS) , cards.get(4));
		assertEquals(new Card(Rank.JACK, Suit.CLUBS) , cards.get(5));
		assertEquals(new Card(Rank.QUEEN, Suit.CLUBS) , cards.get(6));
		assertEquals(new Card(Rank.KING, Suit.CLUBS) , cards.get(7));
		assertEquals(new Card(Rank.ACE, Suit.CLUBS) , cards.get(8));
		assertEquals(new Card(Rank.QUEEN, Suit.DIAMONDS) , cards.get(9));
		assertEquals(new Card(Rank.KING, Suit.DIAMONDS) , cards.get(10));
		assertEquals(new Card(Rank.ACE, Suit.DIAMONDS) , cards.get(11));
		assertEquals(new Card(Rank.QUEEN, Suit.HEARTS) , cards.get(12));
		assertEquals(new Card(Rank.KING, Suit.HEARTS) , cards.get(13));
		assertEquals(new Card(Rank.ACE, Suit.HEARTS) , cards.get(14));
		assertEquals(new Card(Rank.QUEEN, Suit.SPADES) , cards.get(15));
		assertEquals(new Card(Rank.KING, Suit.SPADES) , cards.get(16));
		assertEquals(new Card(Rank.ACE, Suit.SPADES) , cards.get(17));
	}
}