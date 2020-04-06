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
		assertEquals(SeatPosition.NORTH, SeatPosition.nextPlayer(SeatPosition.WEST));
		assertEquals(SeatPosition.EAST, SeatPosition.nextPlayer(SeatPosition.NORTH));
		assertEquals(SeatPosition.SOUTH, SeatPosition.nextPlayer(SeatPosition.EAST));
		assertEquals(SeatPosition.WEST, SeatPosition.nextPlayer(SeatPosition.SOUTH));
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