package com.kadziela.games.bridge.model.enumeration;

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
	
	public static boolean validBid(ValidBidOption candidate, ValidBidOption previous, ValidBidOption preprevious, ValidBidOption prepreprevious)
	{
		//pass is always valid
		if (candidate.equals(ValidBidOption.PASS)) return true;
		//the first bid is always valid, unless it is a double or a redouble
		if (previous == null)
		{
			if (candidate.equals(DOUBLE) || candidate.equals(REDOUBLE)) return false;
			return true;
		}
		//higher bid is always valid, and double is always valid (pass is higher ordinal in this enum, so double following a pass will get caught by that check), 
		//but a redouble can only follow a double
		if (candidate.ordinal() > previous.ordinal())
		{
			if(candidate.equals(REDOUBLE) && !previous.equals(DOUBLE)) return false;				
			return true;
		}
		
		
		//if we got to here, it means that the candidate bid is lower than the previous bid, 
		//so we just have to deal with a few edge cases around doubles, redoubles, and passes, and most likely the bid is invalid
		
		
		//if this is the second bid, it must just be higher than the previous one, which will be correctly resolved above (but I still have to avoid the NPE)
		if(preprevious == null) return true;
		//however, if it is the third bid or higher, and the previous bid is a double, the candidate cannot be a double and must be higher than the bid before previous
		if(previous.equals(DOUBLE))
		{
			//can't double a double
			if (candidate.equals(DOUBLE)) return false;
			//the candidate must be higher than the bid before the double
			return candidate.ordinal() > preprevious.ordinal();
		}
		//if this is the third bid, it must just be higher than the previous one, which will be correctly resolved above (but I still have to protect against accessing a null object)
		if (prepreprevious == null) return true;
		//if it is the fourth bid or higher, and the previous bid is a redouble, than this bid must be higher than the prepreprevious bid
		if (previous.equals(REDOUBLE))
		{
			//can't double or redouble a redouble
			if (candidate.equals(DOUBLE) || candidate.equals(REDOUBLE)) return false;
			//the candidate must be higher than the bid before the bid before the redouble
			return candidate.ordinal() > prepreprevious.ordinal();
		}
		return false;
	}
}