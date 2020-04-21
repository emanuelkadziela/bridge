package com.kadziela.games.bridge.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;

public class HandGenerator 
{
	public static final List<Card> generateAShittyHand()
	{
		List<Card> result = new ArrayList<>();
		for (int i=0;i<13;i++)
		{
			result.add(new Card(Rank.values()[i],Suit.values()[i%4]));
		}
		return result;
	}
	public static final List<String> generateA1NTHand()
	{
		return Arrays.asList("AC","AD","AH","AS","JC","10D","10H","10S","9C","8C","9D","9H","9S");
	}
}