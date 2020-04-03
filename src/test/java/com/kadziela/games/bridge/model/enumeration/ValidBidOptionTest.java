package com.kadziela.games.bridge.model.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ValidBidOptionTest 
{	
	@Test
	void testTestGetSuit()
	{
		assertEquals(BidSuit.CLUBS, ValidBidOption.getSuit(ValidBidOption.FIVE_CLUBS));
		assertEquals(BidSuit.NO_TRUMP, ValidBidOption.getSuit(ValidBidOption.FIVE_NO_TRUMP));
	}
	@Test
	void testGetLevel()
	{
		assertEquals(5, ValidBidOption.getLevel(ValidBidOption.FIVE_CLUBS));
		assertEquals(1, ValidBidOption.getLevel(ValidBidOption.ONE_NO_TRUMP));
	}	
	@Test
	void testValidBid()
	{
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, null));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SEVEN_DIAMONDS, null));
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, null));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, null));		
		List<ValidBidOption> prior = new ArrayList<ValidBidOption>();
		prior.add(ValidBidOption.FIVE_DIAMONDS);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.FIVE_SPADES);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.PASS);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.FIVE_NO_TRUMP);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.DOUBLE);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));		
		prior.add(ValidBidOption.SIX_HEARTS);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_CLUBS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FOUR_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_DIAMONDS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.FIVE_SPADES, prior));
		prior.add(ValidBidOption.DOUBLE);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		prior.add(ValidBidOption.REDOUBLE);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SEVEN_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SIX_SPADES, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));		
		prior.add(ValidBidOption.SIX_NO_TRUMP);
		assertTrue(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SEVEN_CLUBS, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.SEVEN_SPADES, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_HEARTS, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.SIX_DIAMONDS, prior));
		prior.add(ValidBidOption.DOUBLE);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertFalse(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));		
		prior.add(ValidBidOption.PASS);
		assertFalse(ValidBidOption.validBid(ValidBidOption.DOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.REDOUBLE, prior));
		assertTrue(ValidBidOption.validBid(ValidBidOption.PASS, prior));		
	}
}