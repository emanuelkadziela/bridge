package com.kadziela.games.bridge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

@SpringBootTest
public class ContractTest 
{	
	SeatedPlayer north = new SeatedPlayer(SeatPosition.NORTH, new Player("north"));
	SeatedPlayer east = new SeatedPlayer(SeatPosition.EAST, new Player("east"));
	SeatedPlayer south = new SeatedPlayer(SeatPosition.SOUTH, new Player("south"));
	SeatedPlayer west = new SeatedPlayer(SeatPosition.WEST, new Player("west"));
	
	@Test
	void testContractFourPasses()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.PASS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		bids.add(new Bid(south, ValidBidOption.PASS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		assertThrows(IllegalArgumentException.class, () -> new Contract(bids), "Four passes is a redeal not a contract");
	}
	@Test
	void testContractNotLast3Passes()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.PASS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		bids.add(new Bid(south, ValidBidOption.PASS));
		bids.add(new Bid(west, ValidBidOption.FIVE_CLUBS));	
		assertThrows(IllegalArgumentException.class, () -> new Contract(bids), "The last three consecutive bids must be PASS");
	}
	@Test
	void testContractRegular()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.ONE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.ONE_NO_TRUMP));
		bids.add(new Bid(south, ValidBidOption.TWO_DIAMONDS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.FIVE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		bids.add(new Bid(south, ValidBidOption.PASS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		Contract contract = new Contract(bids);
		assertEquals(BidSuit.DIAMONDS, contract.getSuit());
		assertEquals(5, contract.getLevel());
		assertEquals(north, contract.getDeclarer());
		assertFalse(contract.isVulnerable());
		assertFalse(contract.isDoubled());
		assertFalse(contract.isRedoubled());		
	}
	@Test
	void testContractDoubled()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.ONE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.ONE_NO_TRUMP));
		bids.add(new Bid(south, ValidBidOption.TWO_DIAMONDS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.FIVE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.DOUBLE));
		bids.add(new Bid(south, ValidBidOption.PASS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.PASS));
		Contract contract = new Contract(bids);
		assertEquals(BidSuit.DIAMONDS, contract.getSuit());
		assertEquals(5, contract.getLevel());
		assertEquals(north, contract.getDeclarer());
		assertFalse(contract.isVulnerable());
		assertTrue(contract.isDoubled());
		assertFalse(contract.isRedoubled());		
	}
	@Test
	void testContractReDoubled()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.ONE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.ONE_NO_TRUMP));
		bids.add(new Bid(south, ValidBidOption.TWO_DIAMONDS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.FIVE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.DOUBLE));
		bids.add(new Bid(south, ValidBidOption.REDOUBLE));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.PASS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		Contract contract = new Contract(bids);
		assertEquals(BidSuit.DIAMONDS, contract.getSuit());
		assertEquals(5, contract.getLevel());
		assertEquals(north, contract.getDeclarer());
		assertFalse(contract.isVulnerable());
		assertTrue(contract.isDoubled());
		assertTrue(contract.isRedoubled());		
	}
	@Test
	void testContractDeclarer()
	{
		List<Bid> bids = new ArrayList<>();
		bids.add(new Bid(north, ValidBidOption.ONE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.ONE_NO_TRUMP));
		bids.add(new Bid(south, ValidBidOption.TWO_DIAMONDS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.THREE_DIAMONDS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		bids.add(new Bid(south, ValidBidOption.FIVE_DIAMONDS));
		bids.add(new Bid(west, ValidBidOption.PASS));	
		bids.add(new Bid(north, ValidBidOption.PASS));
		bids.add(new Bid(east, ValidBidOption.PASS));
		Contract contract = new Contract(bids);
		assertEquals(BidSuit.DIAMONDS, contract.getSuit());
		assertEquals(5, contract.getLevel());
		assertEquals(north, contract.getDeclarer());
		assertFalse(contract.isVulnerable());
		assertFalse(contract.isDoubled());
		assertFalse(contract.isRedoubled());		
	}
}