package com.kadziela.games.bridge.model;

import com.google.gson.Gson;

public class Player extends User 
{
	
	public Player(String name) {id = System.currentTimeMillis();setName(name);}
	private Long id;
	public Long getId() {return id;}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
}