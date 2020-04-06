package com.kadziela.games.bridge.model.enumeration;

public enum ScoreLineItem 
{
	CONTRACT_NO_TRUMP_FIRST (40),
	CONTRACT_NO_TRUMP_SUBSEQUENT (30),
	CONTRACT_MAJOR (30),
	CONTRACT_MINOR (20),
	OVERTRICK_NT_OR_MAJOR (30),
	OVERTRICK_MINOR (20),
	OVERTRICK_DOUBLED_NOT_VUL (100),
	OVERTRICK_DOUBLED_VUL (200),
	OVERTRICK_REDOUBLED_NOT_VUL (200),
	OVERTRICK_REDOUBLED_VUL (400);
	
	
	
	
	
	
	
	
	
	
	public final int points;
	
	private ScoreLineItem(int pts) {points = pts;}
}