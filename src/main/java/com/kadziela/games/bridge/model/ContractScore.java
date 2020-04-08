package com.kadziela.games.bridge.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.ScoreLineItem;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;

public final class ContractScore 
{
	private static final Logger logger = LogManager.getLogger(ContractScore.class);

	private final Contract contract;
	private final List<Trick> tricks;
	private final Map<SeatPosition,Collection<Card>> hands;
	private AtomicInteger north = new AtomicInteger(-6);
	private AtomicInteger east = new AtomicInteger(-6);
	private final Map<ScoreLineItem,Integer> northLedger = new ConcurrentHashMap<>();
	private final Map<ScoreLineItem,Integer> eastLedger = new ConcurrentHashMap<>();
	
	
	
	
	public ContractScore(Contract cont, List<Trick> trks, Map<SeatPosition,Collection<Card>> hnds) 
	{
		contract = cont;
		tricks = trks;
		hands = hnds;
		countTricks();
		assess();
	}
	private void countTricks()
	{
		for (Trick trick: tricks)
		{
			if (trick.getWinner().getPosition().equals(SeatPosition.NORTH) || trick.getWinner().getPosition().equals(SeatPosition.SOUTH)) north.getAndIncrement();
			else east.getAndIncrement();
		}
		
	}
	private void assess()
	{
		SeatedPlayer declarer = contract.getDeclarer();
		int level = contract.getLevel();
		int over = -6;
		if (declarer.getPosition().equals(SeatPosition.NORTH) || declarer.getPosition().equals(SeatPosition.SOUTH)) over = north.addAndGet(-level);		
		else over = east.addAndGet(-level);
		if (over < 0) calculateContractMissed();
		else calculateContractMade(over, level, declarer);
		addHonors(contract.getSuit());
		
	}
	private void calculateContractMade(int over,int level, SeatedPlayer declarer) 
	{
		logger.info(String.format("%s made the contract with %s overtricks ",contract.getDeclarer(),over));
		Map<ScoreLineItem,Integer> ledger = declarer.getPosition().equals(SeatPosition.NORTH) || declarer.getPosition().equals(SeatPosition.SOUTH) ? northLedger : eastLedger; 
		switch (contract.getSuit()) 
		{
			case NO_TRUMP:
				if(contract.isRedoubled()) 
				{
					ledger.put(ScoreLineItem.CONTRACT_NT_FIRST_REDOUBLED, 1);
					if (level > 1) ledger.put(ScoreLineItem.CONTRACT_NT_SUBSEQUENT_REDOUBLED, (level - 1));
				}
				else if(contract.isDoubled()) 
				{
					ledger.put(ScoreLineItem.CONTRACT_NT_FIRST_DOUBLED, 1);
					if (level > 1) ledger.put(ScoreLineItem.CONTRACT_NT_SUBSEQUENT_DOUBLED, (level - 1));
				}
				else 
				{
					ledger.put(ScoreLineItem.CONTRACT_NT_FIRST, 1);
					if (level > 1) ledger.put(ScoreLineItem.CONTRACT_NT_SUBSEQUENT, (level - 1));
				}
				break;
			case SPADES:
			case HEARTS:
				if(contract.isRedoubled()) ledger.put(ScoreLineItem.CONTRACT_MAJOR_REDOUBLED, level);
				else if(contract.isDoubled()) ledger.put(ScoreLineItem.CONTRACT_MAJOR_DOUBLED, level);
				else ledger.put(ScoreLineItem.CONTRACT_MAJOR, level);
				break;
			case DIAMONDS:
			case CLUBS:
				if(contract.isRedoubled()) ledger.put(ScoreLineItem.CONTRACT_MINOR_REDOUBLED, level);
				else if(contract.isDoubled()) ledger.put(ScoreLineItem.CONTRACT_MINOR_DOUBLED, level);
				else ledger.put(ScoreLineItem.CONTRACT_MINOR, level);
				break;
		}		
		if (over > 0)
		{
			addOvertricks(ledger, over);
		}
		if (contract.isRedoubled()) ledger.put(ScoreLineItem.REDOUBLE_BONUS, 1);
		if (contract.isDoubled()) ledger.put(ScoreLineItem.DOUBLE_BONUS, 1);
		if (level == 6) 
		{
			if (contract.isVulnerable()) ledger.put(ScoreLineItem.SLAM_SMALL_V, 1);
			else ledger.put(ScoreLineItem.SLAM_SMALL_NV, 1);
		}
		if (level == 7) 
		{
			if (contract.isVulnerable()) ledger.put(ScoreLineItem.SLAM_GRAND_V, 1);
			else ledger.put(ScoreLineItem.SLAM_GRAND_NV, 1);
		}
	}
	private void addOvertricks(Map<ScoreLineItem,Integer> ledger, int over)
	{
		if (contract.isVulnerable() && contract.isRedoubled()) ledger.put(ScoreLineItem.OVERTRICK_REDOUBLED_V, over);
		else if (contract.isVulnerable() && contract.isDoubled()) ledger.put(ScoreLineItem.OVERTRICK_DOUBLED_V, over);
		else if (contract.isRedoubled()) ledger.put(ScoreLineItem.OVERTRICK_REDOUBLED_NV, over);
		else if (contract.isDoubled()) ledger.put(ScoreLineItem.OVERTRICK_DOUBLED_NV, over);
		else if (contract.getSuit().equals(BidSuit.CLUBS) || contract.getSuit().equals(BidSuit.DIAMONDS)) ledger.put(ScoreLineItem.OVERTRICK_MINOR, over);
		else ledger.put(ScoreLineItem.OVERTRICK_NT_OR_MAJOR, over);
	}
	private void calculateContractMissed() {}
	private void addHonors(BidSuit suit) 
	{
		if (suit.equals(BidSuit.NO_TRUMP))
		{
			for (SeatPosition key: hands.keySet())
			{
				int aces = 0;
				for (Card card:hands.get(key))
				{
					if(card.getRank().equals(Rank.ACE))
					{
						aces++;
					}
				}
				if (aces == 4)
				{
					if (key.equals(SeatPosition.NORTH) || key.equals(SeatPosition.SOUTH)) northLedger.put(ScoreLineItem.HONORS_HIGH, 1);
					else eastLedger.put(ScoreLineItem.HONORS_HIGH, 1);
				}
			}
		}
		else 
		{
			for (SeatPosition key: hands.keySet())
			{
				int highCards = 0;
				for (Card card:hands.get(key))
				{
					if(Suit.valueOf(suit.toString()).equals(card.getSuit()) && card.getRank().ordinal() > Rank.NINE.ordinal())
					{
						highCards++;
					}
				}
				if (highCards == 5)
				{
					if (key.equals(SeatPosition.NORTH) || key.equals(SeatPosition.SOUTH)) northLedger.put(ScoreLineItem.HONORS_HIGH, 1);
					else eastLedger.put(ScoreLineItem.HONORS_HIGH, 1);
				}
				else if (highCards == 4)
				{
					if (key.equals(SeatPosition.NORTH) || key.equals(SeatPosition.SOUTH)) northLedger.put(ScoreLineItem.HONORS, 1);
					else eastLedger.put(ScoreLineItem.HONORS, 1);		
				}
			}
		}
	}
	private int getNorthSouthTotal() {return 0;}
	private int getEastWestTotal() {return 0;}	
	private int getNorthSouthUnder() {return 0;}
	private int getEastWestUnder() {return 0;}	
	private int getNorthSouthOver() {return 0;}
	private int getEastWestOver() {return 0;}	
}