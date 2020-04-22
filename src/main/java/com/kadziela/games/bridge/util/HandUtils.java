package com.kadziela.games.bridge.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

public class HandUtils 
{
	private static final Logger logger = LogManager.getLogger(HandUtils.class);

	public static final int getHighCountPoints(Collection<Card> hand)
	{
		return hand.stream().map(card -> card.getRank().points).reduce(0, Integer::sum);
	}
	public static final int getDistributionPoints(Collection<Card> hand)
	{
		int result = 0;
		Map<Suit, List<Card>> cardMap = mappifyCollection(hand);
		for (Suit suit:Suit.values())
		{
			if (cardMap.get(suit) == null) result += 3;
			else if(cardMap.get(suit).size() < 3) result += (3 - cardMap.get(suit).size());
		}
		return result;
	}
	public static final int getTotalPointsDeduped(Collection<Card> hand)
	{
		int result = 0;
		Map<Suit, List<Card>> cardMap = mappifyCollection(hand);
		for (Suit suit:Suit.values())
		{
			if (cardMap.get(suit) == null) result += 3;
			else 
			{
				int hcp = getHighCountPoints(cardMap.get(suit));
				if (hcp > 0) result += hcp;
				else if (cardMap.get(suit).size() < 3) result += (3 - cardMap.get(suit).size());
			}
		}
		return result;
	}
	public static final ValidBidOption suggestFirstOpeningBid5CMBM(Collection<Card> hand) throws IllegalArgumentException
	{
		Assert.isTrue(hand.size() == 13,"the hand must contain exactly 13 cards");
		int totalPoints = getTotalPointsDeduped(hand);
		logger.debug("{} points in the hand",totalPoints);
		Map<Suit, List<Card>> cardMap = mappifyCollection(hand);
		if (totalPoints < 13) 
		{
			if (totalPoints > 7) return weakThree(cardMap);
			return ValidBidOption.PASS;
		}
		if (isBalanced(cardMap))
		{
			logger.debug("balanced hand",totalPoints);
			if (getHighCountPoints(hand) > 22) return ValidBidOption.THREE_NO_TRUMP;
			if (getHighCountPoints(hand) > 19) return ValidBidOption.TWO_NO_TRUMP;
			if (getHighCountPoints(hand) > 15) return ValidBidOption.ONE_NO_TRUMP;
		}
		logger.debug("unbalanced hand",totalPoints);
		BidSuit suit = findSuitFor5CMBM(cardMap);
		if (totalPoints > 22)
		{
			return ValidBidOption.valueOf("TWO_"+suit.toString());
		}
		return ValidBidOption.valueOf("ONE_"+suit.toString());
	}
	public static final ValidBidOption suggestFirstOpeningBid5CMBM(List<String> hand) throws IllegalArgumentException
	{
		Assert.isTrue(hand.size() == 13,"the hand must contain exactly 13 cards");
		List<Card> cards = CardUtils.convertFromStringsToEnums(hand);
		return suggestFirstOpeningBid5CMBM(cards);
	}
	private static final Map<Suit, List<Card>> mappifyCollection(Collection<Card> input)
	{
		return input.stream().collect(Collectors.groupingBy(Card::getSuit));
	}
	private static final boolean isBalanced(Map<Suit, List<Card>> cardMap) 
	{
		int doubletonCount = 0;
		for (Suit suit:Suit.values())
		{
			if (cardMap.get(suit) == null) return false;//void
			if (cardMap.get(suit).size() < 2) return false;//singleton
			if (cardMap.get(suit).size() < 3) 
			{
				if (doubletonCount > 0) return false;//more than 1 doubleton
				doubletonCount++;
				continue;
			}
		}
		return true;
	}
	private static final ValidBidOption weakThree(Map<Suit, List<Card>> cardMap)
	{
		for (Suit suit:cardMap.keySet())
		{
			List<Card> suited = cardMap.get(suit);
			if (suited.size() > 6)
			{
				if (getHighCountPoints(suited) > 1) 
				{
					Collections.sort(suited);
					if (suited.get(suited.size()-1).getRank().ordinal() > Rank.JACK.ordinal() && suited.get(suited.size()-2).getRank().ordinal() > Rank.NINE.ordinal())
						return ValidBidOption.valueOf("THREE_"+suit.toString());
				}
			}
		}
		return ValidBidOption.PASS;
	}
	private static final BidSuit findSuitFor5CMBM(Map<Suit, List<Card>> cardMap)
	{
		//check for longest 5+ card major, return the longer one, if equal return spades (equal 5 CMs should be bid down the line, spades then hearts)
		if (cardMap.get(Suit.SPADES) != null && cardMap.get(Suit.SPADES).size() > 4) 
		{
			//I have at least 5 spades
			if (cardMap.get(Suit.HEARTS) != null && cardMap.get(Suit.HEARTS).size() > 4)
			{
				//I also have at least 5 hearts, so return the longer suit, and if equal, return spades
				if(cardMap.get(Suit.SPADES).size() >= cardMap.get(Suit.HEARTS).size()) return BidSuit.SPADES;
				return BidSuit.HEARTS;
			}
			//I do not have 5+ hearts, so just return spades
			return BidSuit.SPADES;
		}
		//I don't have 5 spades, but I have 5+ hearts, so return hearts
		if (cardMap.get(Suit.HEARTS) != null && cardMap.get(Suit.HEARTS).size() > 4) return BidSuit.HEARTS;
		//I don't have a 5CM, so return the longer minor, and if they are equal length, return the stronger one. 
		//If they are equal strength and length, return clubs
		if (cardMap.get(Suit.DIAMONDS) != null && cardMap.get(Suit.CLUBS) != null)
		{
			if (cardMap.get(Suit.DIAMONDS).size() > cardMap.get(Suit.CLUBS).size()) return BidSuit.DIAMONDS;
			if (cardMap.get(Suit.DIAMONDS).size() < cardMap.get(Suit.CLUBS).size()) return BidSuit.CLUBS;
			if (cardMap.get(Suit.DIAMONDS).size() == cardMap.get(Suit.CLUBS).size())
			{
				if (getHighCountPoints(cardMap.get(Suit.DIAMONDS)) > getHighCountPoints(cardMap.get(Suit.CLUBS))) return BidSuit.DIAMONDS;
				return BidSuit.CLUBS;
			}
		}
		//should never reach this point		
		return BidSuit.CLUBS;
	}
}