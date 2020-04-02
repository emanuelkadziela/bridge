package com.kadziela.games.bridge.model.enumeration;

import java.util.List;

public enum ValidBidOption 
{
	ONE_CLUBS, ONE_DIAMONDS, ONE_HEARTS, ONE_SPADES, ONE_NO_TRUMP,
	TWO_CLUBS, TWO_DIAMONDS, TWO_HEARTS, TWO_SPADES, TWO_NO_TRUMP,
	THREE_CLUBS, THREE_DIAMONDS, THREE_HEARTS, THREE_SPADES, THREE_NO_TRUMP,
	FOUR_CLUBS, FOUR_DIAMONDS, FOUR_HEARTS, FOUR_SPADES, FOUR_NO_TRUMP,
	FIVE_CLUBS, FIVE_DIAMONDS, FIVE_HEARTS, FIVE_SPADES, FIVE_NO_TRUMP,
	SIX_CLUBS, SIX_DIAMONDS, SIX_HEARTS, SIX_SPADES, SIX_NO_TRUMP,
	SEVEN_CLUBS, SEVEN_DIAMONDS, SEVEN_HEARTS, SEVEN_SPADES, SEVEN_NO_TRUMP,
	DOUBLE, REDOUBLE, PASS;
	
	/**
	 * Determines if the given bid is valid in the context of previous bids. 
	 * The rules are as follows (more detailed in the comments throughout the code):
	 * 
	 * 1. A pass is always valid
	 * 2. The first non-pass bid is always valid, unless it is a double or a redouble
	 * 3. A normal (not dbl, rdbl, or pass) bid that is higher than the last normal bid (regardless of what happened in between) is also valid
	 * 4. A double can only follow a normal bid from an opponent. If it is the left-hand opponent, a double is only valid if my partner passed.
	 * 5. A redouble can only follow a double from from one of my opponents. If it is the left-hand opponent, a redouble is only valid if my partner passed.
	 * 
	 * @param candidate - the bid in question
	 * @param prior a list of prior bids (can be null or empty)
	 * @return true if the candidate bid is valid and false otherwise
	 */
	public static boolean validBid(ValidBidOption candidate, List<ValidBidOption> prior)
	{
		//pass is always valid
		if (candidate.equals(ValidBidOption.PASS)) return true;
		//the first non-pass bid is always valid, unless it is a double or a redouble
		if (prior == null || prior.isEmpty() || lastNormal(prior) == null)
		{
			if (candidate.equals(DOUBLE) || candidate.equals(REDOUBLE)) return false;
			return true;
		}
		//a normal (not dbl, rdbl, or pass) bid that is higher than the last normal bid (regardless of what happened in between) is also valid
		//we already checked that the candidate is not a pass and not a first non-pass bid
		if (!candidate.equals(DOUBLE) && !candidate.equals(REDOUBLE))
		{
			if (candidate.ordinal() > lastNormal(prior).ordinal()) return true;			
		}
		//A double can only follow a normal bid from an opponent. If it is the left-hand opponent, a double is only valid if my partner passed.
		if (candidate.equals(DOUBLE))
		{
			//normal bid from my right-hand opponent, double is valid
			if (prior.get(0).ordinal() < DOUBLE.ordinal()) return true;
			//not enough prior bids to have a normal one from the left-hand opponent, so double is invalid (this would happen in the case when only my partner and right-hand opponent bid so far)
			if (prior.size() < 3) return false;
			//normal bid from my left-hand opponent
			if (prior.get(2).ordinal() < DOUBLE.ordinal())
			{
				//my partner passed, so double is valid
				if (prior.get(1).equals(PASS)) return true;
			}
		}
		//A redouble can only follow a double from from one of my opponents. If it is the left-hand opponent, a redouble is only valid if my partner passed.
		if (candidate.equals(REDOUBLE))
		{
			//double from my right-hand opponent, double is valid
			if (prior.get(0).equals(DOUBLE)) return true;
			//not enough prior bids to have a double from the left-hand opponent, so redouble is invalid (this would happen in the case when only my partner and right-hand opponent bid so far)
			if (prior.size() < 3) return false;
			//double from my left-hand opponent
			if (prior.get(2).equals(DOUBLE))
			{
				//my partner passed, so redouble is valid
				if (prior.get(1).equals(PASS)) return true;
			}
		}
		//anything else should be invalid
		return false;
	}
	private static ValidBidOption lastNormal(List<ValidBidOption> prior)
	{
		if(prior == null || prior.isEmpty()) return null;
		for(ValidBidOption vbo:prior)
		{
			if (vbo.ordinal() < DOUBLE.ordinal()) return vbo;
		}
		return null;
	}
}