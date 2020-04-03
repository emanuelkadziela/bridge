package com.kadziela.games.bridge.model.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SeatPositionTest 
{	
	@Test
	void testNextBidder()
	{
		assertEquals(SeatPosition.NORTH, SeatPosition.nextBidder(SeatPosition.WEST));
		assertEquals(SeatPosition.EAST, SeatPosition.nextBidder(SeatPosition.NORTH));
		assertEquals(SeatPosition.SOUTH, SeatPosition.nextBidder(SeatPosition.EAST));
		assertEquals(SeatPosition.WEST, SeatPosition.nextBidder(SeatPosition.SOUTH));
	}
	@Test
	void testGetPartner()
	{
		assertEquals(SeatPosition.NORTH, SeatPosition.getPartner(SeatPosition.SOUTH));
		assertEquals(SeatPosition.EAST, SeatPosition.getPartner(SeatPosition.WEST));
		assertEquals(SeatPosition.SOUTH, SeatPosition.getPartner(SeatPosition.NORTH));
		assertEquals(SeatPosition.WEST, SeatPosition.getPartner(SeatPosition.EAST));
	}	
}