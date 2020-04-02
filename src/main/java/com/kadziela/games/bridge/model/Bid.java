package com.kadziela.games.bridge.model;

import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

public class Bid 
{
	private final Long id = System.currentTimeMillis();
	private SeatedPlayer seatedPlayer;
	private ValidBidOption bid;
	
	public Bid(SeatedPlayer sp, ValidBidOption vbo) 
	{
		seatedPlayer = sp;
		bid = vbo;
	}
	public Long getId() {return id;}
	public SeatedPlayer getSeatedPlayer() {return seatedPlayer;}
	public ValidBidOption getBid() {return bid;}
	
}