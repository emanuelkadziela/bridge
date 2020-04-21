package com.kadziela.games.bridge.model.enumeration;

public enum Suit 
{
	CLUBS, DIAMONDS, HEARTS, SPADES;
	
	public static Suit getFromLetter(String suit)
	{
		switch(suit)
		{
			case "C": return CLUBS;
			case "D": return DIAMONDS;
			case "H": return HEARTS;
			case "S": return SPADES;
			default: return null;
		}
	}
	public static String getSymbol(Suit suit)
	{
		return String.valueOf(suit.toString().charAt(0));
	}
}