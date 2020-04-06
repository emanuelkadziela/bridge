package com.kadziela.games.bridge.model;

import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;

public final class PlayedCard implements Comparable<PlayedCard> 
{
	private Card card;
	private SeatPosition postion;
	public PlayedCard(Card card, SeatPosition postion) {this.card = card;this.postion = postion;}
	public Card getCard() {return card;}
	public SeatPosition getPostion() {return postion;}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((card == null) ? 0 : card.hashCode());
		result = prime * result + ((postion == null) ? 0 : postion.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayedCard other = (PlayedCard) obj;
		if (card == null) {
			if (other.card != null)
				return false;
		} else if (!card.equals(other.card))
			return false;
		if (postion != other.postion)
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
	public int compareTo(PlayedCard pc) {return card.compareTo(pc.getCard());}
}