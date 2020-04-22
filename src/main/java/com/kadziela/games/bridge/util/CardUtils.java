package com.kadziela.games.bridge.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;

public class CardUtils 
{
	public static List<String> convertFromEnumsToStrings(Collection<Card> cards)
	{
		return cards.stream().map(card -> Rank.getSymbol(card.getRank())+Suit.getSymbol(card.getSuit())).collect(Collectors.toList());
	}
	public static List<Card> convertFromStringsToEnums(Collection<String> symbols)
	{
		return symbols.stream().map(symbol -> new Card(symbol)).collect(Collectors.toList());
	}
}