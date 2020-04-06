package com.kadziela.games.bridge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;

@SpringBootTest
public class TrickTest 
{	
	@Test
	void testGetWinnerTrumpAllFollow()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.KING,Suit.SPADES),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.SPADES),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.SPADES);
		assertEquals(east, trick.getWinner());
	}
	@Test
	void testGetWinnerTrumpSomeFollow()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.KING,Suit.HEARTS),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.HEARTS),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.SPADES);
		assertEquals(west, trick.getWinner());
	}
	@Test
	void testGetWinnerTrumpedOnce()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.TWO,Suit.HEARTS),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.DIAMONDS),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.HEARTS);
		assertEquals(east, trick.getWinner());
	}
	@Test
	void testGetWinnerTrumpedTwice()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.TWO,Suit.HEARTS),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.HEARTS),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.HEARTS);
		assertEquals(south, trick.getWinner());
	}
	@Test
	void testGetWinnerNoTrump()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.TWO,Suit.HEARTS),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.HEARTS),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.NO_TRUMP);
		assertEquals(west, trick.getWinner());
	}
	@Test
	void testGetWinnerTrumpNotPlayed()
	{
		PlayedCard north = new PlayedCard(new Card(Rank.FOUR,Suit.SPADES),SeatPosition.NORTH);
		PlayedCard east = new PlayedCard(new Card(Rank.TWO,Suit.HEARTS),SeatPosition.EAST);
		PlayedCard south = new PlayedCard(new Card(Rank.NINE,Suit.HEARTS),SeatPosition.SOUTH);
		PlayedCard west = new PlayedCard(new Card(Rank.EIGHT,Suit.SPADES),SeatPosition.WEST);
		Trick trick = new Trick(north, east, south, west, BidSuit.DIAMONDS);
		assertEquals(west, trick.getWinner());
	}
}