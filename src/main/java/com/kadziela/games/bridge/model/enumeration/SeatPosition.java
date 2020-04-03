package com.kadziela.games.bridge.model.enumeration;

public enum SeatPosition 
{
	NORTH, EAST, SOUTH, WEST;
	
	public static SeatPosition nextBidder(SeatPosition currentBidder)
	{
		if(currentBidder.equals(WEST)) return NORTH;
		return SeatPosition.values()[currentBidder.ordinal()+1];
	}
	public static SeatPosition getPartner(SeatPosition position)
	{
		return SeatPosition.values()[(position.ordinal() + 2) % 4];
	}
}