package com.kadziela.games.bridge.model.enumeration;

public enum SeatPosition 
{
	NORTH, EAST, SOUTH, WEST;
	
	public static SeatPosition nextPlayer(SeatPosition currentPlayer)
	{
		return SeatPosition.values()[(currentPlayer.ordinal()+1) % 4];
	}
	public static SeatPosition getPartner(SeatPosition position)
	{
		return SeatPosition.values()[(position.ordinal() + 2) % 4];
	}
}