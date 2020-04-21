package com.kadziela.games.bridge.model.enumeration;

public enum Rank 
{
	TWO(0), THREE(0), FOUR(0), FIVE(0), SIX(0), SEVEN(0), EIGHT(0), NINE(0), TEN(0), JACK(1), QUEEN(2), KING(3), ACE(4); 	
	public final int points;	
	private Rank(int pts) {points = pts;}
	
	public static Rank getFromLetter(String letter)
	{
		try
		{
			Integer i = Integer.valueOf(letter);
			if (i<2 || i>10) return null;
			return Rank.values()[i-2];
		}
		catch(NumberFormatException nfe)
		{
			switch(letter)
			{
				case "J": return JACK;
				case "Q": return QUEEN;
				case "K": return KING;
				case "A": return ACE;
				default : return null;
			}
		}
	}
	public static String getSymbol(Rank rank)
	{
		if (rank.ordinal() < Rank.JACK.ordinal()) return String.valueOf(rank.ordinal()+2);
		return String.valueOf(rank.toString().charAt(0));
	}
}