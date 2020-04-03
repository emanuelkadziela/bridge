package com.kadziela.games.bridge.util;

import java.util.Comparator;
import com.kadziela.games.bridge.model.Card;

public class CardComparatorBySuitThenRank implements Comparator<Card> 	
{
	public int compare(Card c1, Card c2) 
	{
		if (ComparisonUtils.nullSafeCompare(c1, c2) == 0)
		{
			return 0;
		}
		return ComparisonUtils.nullSafeCompare(c1.getSuit(), c2.getSuit()) != 0 ? ComparisonUtils.nullSafeCompare(c1.getSuit(), c2.getSuit()) :
				ComparisonUtils.nullSafeCompare(c1.getRank(), c2.getRank());

	}
}