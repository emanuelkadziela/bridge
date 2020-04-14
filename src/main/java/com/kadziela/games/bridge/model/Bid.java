package com.kadziela.games.bridge.model;

import com.google.gson.Gson;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bid == null) ? 0 : bid.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((seatedPlayer == null) ? 0 : seatedPlayer.hashCode());
		return result;
	}
	public Bid getCopyWithoutHands()
	{
		return new Bid(seatedPlayer.getCopyWithoutHands(),bid);		
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bid other = (Bid) obj;
		if (bid != other.bid)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (seatedPlayer == null) {
			if (other.seatedPlayer != null)
				return false;
		} else if (!seatedPlayer.equals(other.seatedPlayer))
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
}