package com.kadziela.games.bridge.model.enumeration;

public enum Rank 
{
	TWO(0), THREE(0), FOUR(0), FIVE(0), SIX(0), SEVEN(0), EIGHT(0), NINE(0), TEN(0), JACK(1), QUEEN(2), KING(3), ACE(4); 	
	public final int points;	
	private Rank(int pts) {points = pts;}
}