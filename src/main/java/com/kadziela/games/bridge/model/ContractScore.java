package com.kadziela.games.bridge.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.ScoreLineItem;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;

public final class ContractScore 
{
	private static final Logger logger = LogManager.getLogger(ContractScore.class);

	private final Long id = System.currentTimeMillis();
	private final Contract contract;
	private final List<Trick> tricks;
	private final Map<SeatPosition,Collection<Card>> hands;
	private AtomicInteger north = new AtomicInteger(-6);
	private AtomicInteger east = new AtomicInteger(-6);
	private final Map<ScoreLineItem,Integer> northLedger = new ConcurrentHashMap<>();
	private final Map<ScoreLineItem,Integer> eastLedger = new ConcurrentHashMap<>();
	private boolean closed;
	
	public ContractScore(Contract cont, List<Trick> trks, Map<SeatPosition,Collection<Card>> hnds) 
	{
		closed = false;
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
		Map<ScoreLineItem,Integer> madeLedger = northLedger;
		Map<ScoreLineItem,Integer> missedLedger = eastLedger;
		if (declarer.getPosition().equals(SeatPosition.NORTH) || declarer.getPosition().equals(SeatPosition.SOUTH)) 
		{
			over = north.addAndGet(-level);		
		}
		else 
		{
			over = east.addAndGet(-level);
			madeLedger = eastLedger;
			missedLedger = northLedger;
		}
		if (over < 0) calculateContractMissed(-over,missedLedger);
		else calculateContractMade(over, level, madeLedger);
		addHonors(contract.getSuit());
	}
	private void calculateContractMade(int over,int level, Map<ScoreLineItem,Integer> ledger) 
	{
		logger.info(String.format("%s made the contract with %s overtricks ",contract.getDeclarer().getPosition(),over));
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
	private void calculateContractMissed(int under, Map<ScoreLineItem,Integer> ledger) 
	{
		logger.info(String.format("%s missed the contract with %s undertricks ",contract.getDeclarer().getPosition(),under));
		if (contract.isVulnerable() && contract.isRedoubled()) 
		{
			ledger.put(ScoreLineItem.UNDERTRICK_V_REDOUBLED_FIRST, 1);
			if (under > 1) ledger.put(ScoreLineItem.UNDERTRICK_V_REDOUBLED_SECOND_OR_MORE, (under - 1));
		}
		else if (contract.isVulnerable() && contract.isDoubled())
		{
			ledger.put(ScoreLineItem.UNDERTRICK_V_DOUBLED_FIRST, 1);
			if (under > 1) ledger.put(ScoreLineItem.UNDERTRICK_V_DOUBLED_SECOND_OR_MORE, (under - 1));
			
		}
		else if (contract.isRedoubled()) 
		{
			ledger.put(ScoreLineItem.UNDERTRICK_NV_REDOUBLED_FIRST, 1);
			if (under > 1) ledger.put(ScoreLineItem.UNDERTRICK_NV_REDOUBLED_SECOND_THIRD, 1);
			if (under > 2) ledger.put(ScoreLineItem.UNDERTRICK_NV_REDOUBLED_SECOND_THIRD, 1);
			if (under > 3) ledger.put(ScoreLineItem.UNDERTRICK_NV_REDOUBLED_FOURTH_OR_MORE, (under - 3));
		}
		else if (contract.isDoubled()) 
		{
			ledger.put(ScoreLineItem.UNDERTRICK_NV_DOUBLED_FIRST, 1);
			if (under > 1) ledger.put(ScoreLineItem.UNDERTRICK_NV_DOUBLED_SECOND_THIRD, 1);
			if (under > 2) ledger.put(ScoreLineItem.UNDERTRICK_NV_DOUBLED_SECOND_THIRD, 1);
			if (under > 3) ledger.put(ScoreLineItem.UNDERTRICK_NV_DOUBLED_FOURTH_OR_MORE, (under - 3));
		}
		else if (contract.isVulnerable())
		{
			ledger.put(ScoreLineItem.UNDERTRICK_V, under);
		}
		else ledger.put(ScoreLineItem.UNDERTRICK_NV, under);
	}
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
	public void addRubberBonus (SeatPosition position, boolean fast)
	{
		if (position.equals(SeatPosition.NORTH))
		{
			if (fast) northLedger.put(ScoreLineItem.RUBBER_FAST, 1);
			else northLedger.put(ScoreLineItem.RUBBER_SLOW, 1);
		}
		else
		{
			if (fast) eastLedger.put(ScoreLineItem.RUBBER_FAST, 1);
			else eastLedger.put(ScoreLineItem.RUBBER_SLOW, 1);			
		}
	}
	public Map<ScoreLineItem, Integer> getNorthLedger() {return new HashMap<ScoreLineItem,Integer>(northLedger);}
	public Map<ScoreLineItem, Integer> getEastLedger() {return new HashMap<ScoreLineItem,Integer>(eastLedger);}
	public Long getId() {return id;}	
	public boolean isClosed() {return closed;}
	public void setClosed(boolean clsd) {closed = clsd;}
	public Contract getContract() {return contract;}
	
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (closed ? 1231 : 1237);
		result = prime * result + ((contract == null) ? 0 : contract.hashCode());
		result = prime * result + ((eastLedger == null) ? 0 : eastLedger.hashCode());
		result = prime * result + ((hands == null) ? 0 : hands.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((northLedger == null) ? 0 : northLedger.hashCode());
		result = prime * result + ((tricks == null) ? 0 : tricks.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContractScore other = (ContractScore) obj;
		if (closed != other.closed)
			return false;
		if (contract == null) {
			if (other.contract != null)
				return false;
		} else if (!contract.equals(other.contract))
			return false;
		if (eastLedger == null) {
			if (other.eastLedger != null)
				return false;
		} else if (!eastLedger.equals(other.eastLedger))
			return false;
		if (hands == null) {
			if (other.hands != null)
				return false;
		} else if (!hands.equals(other.hands))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (northLedger == null) {
			if (other.northLedger != null)
				return false;
		} else if (!northLedger.equals(other.northLedger))
			return false;
		if (tricks == null) {
			if (other.tricks != null)
				return false;
		} else if (!tricks.equals(other.tricks))
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
}