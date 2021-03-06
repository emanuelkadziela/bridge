package com.kadziela.games.bridge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import com.google.gson.Gson;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

public final class Contract
{
	private final Long id = System.currentTimeMillis();;
	private List<Bid> bids;
	private SeatedPlayer declarer;
	private boolean vulnerable;
	private boolean doubled;
	private boolean redoubled;
	private BidSuit suit;
	private int level;
	
	public Contract(List<Bid> bidList) throws IllegalArgumentException
	{
		bids = bidList;		
		buildThis();
	}

	public Long getId() {return id;}
	public List<Bid> getBids() {return bids;}
	public SeatedPlayer getDeclarer() {return declarer;}
	public boolean isVulnerable() {return vulnerable;}
	public boolean isDoubled() {return doubled;}
	public boolean isRedoubled() {return redoubled;}
	public BidSuit getSuit() {return suit;}
	public int getLevel() {return level;}
	public Map<String,Object> getContractDetailsWithoutHands()
	{
		Map<String,Object> result = new HashMap<>();
		result.put("id", getId());
		result.put("declarer", declarer.getPosition());
		result.put("vulnerable", isVulnerable());
		result.put("doubled", isDoubled());
		result.put("redoubled", isRedoubled());
		result.put("suit", getSuit());
		result.put("level", getLevel());
		List<Map<String,String>> bidMaps = new ArrayList<>();
		for (Bid bid:bids)
		{
			Map<String,String> bidMap = new HashMap<>();
			bidMap.put("bid", bid.getBid().toString());
			bidMap.put("bidder", bid.getSeatedPlayer().getPosition().toString());
			bidMaps.add(bidMap);
		}
		result.put("bids", bidMaps);
		return result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bids == null) ? 0 : bids.hashCode());
		result = prime * result + ((declarer == null) ? 0 : declarer.hashCode());
		result = prime * result + (doubled ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + level;
		result = prime * result + (redoubled ? 1231 : 1237);
		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
		result = prime * result + (vulnerable ? 1231 : 1237);
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
		Contract other = (Contract) obj;
		if (bids == null) {
			if (other.bids != null)
				return false;
		} else if (!bids.equals(other.bids))
			return false;
		if (declarer == null) {
			if (other.declarer != null)
				return false;
		} else if (!declarer.equals(other.declarer))
			return false;
		if (doubled != other.doubled)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (level != other.level)
			return false;
		if (redoubled != other.redoubled)
			return false;
		if (suit != other.suit)
			return false;
		if (vulnerable != other.vulnerable)
			return false;
		return true;
	}
	@Override public String toString() {return new Gson().toJson(this);}
	private void buildThis() throws IllegalArgumentException
	{
		Assert.notNull(bids, "the list of bids cannot be null");
		Assert.notEmpty(bids,"The list of bids cannot be empty");
		//verify at least 4 bids were made
		if(bids.size() < 4) throw new IllegalArgumentException("The must be at least four bids to make a contract (last 3 must be PASS)");
		//verify there were not just 4 passes
		if (bids.size() == 4)
		{
			if (bids.get(0).getBid().equals(ValidBidOption.PASS) && bids.get(1).getBid().equals(ValidBidOption.PASS) && 
				bids.get(2).getBid().equals(ValidBidOption.PASS) && bids.get(3).getBid().equals(ValidBidOption.PASS))
				throw new IllegalArgumentException("Four passes is a redeal not a contract");
		}
		//verify the last 3 bids were passes
		if (bids.size() > 3)
		{
			if (!(bids.get(bids.size()-3).getBid().equals(ValidBidOption.PASS) && bids.get(bids.size()-2).getBid().equals(ValidBidOption.PASS) && bids.get(bids.size()-1).getBid().equals(ValidBidOption.PASS)))
				throw new IllegalArgumentException("The last three consecutive bids must be PASS");
		}
		//is the last bid before the 3 passes double or redouble
		Bid lastNonPass = bids.get(bids.size()-4);
		if (lastNonPass.getBid().equals(ValidBidOption.REDOUBLE)) 
		{
			doubled = true;
			redoubled = true;
		}
		if (lastNonPass.getBid().equals(ValidBidOption.DOUBLE)) doubled = true;
		//what's the last non-pas, non-dbl, non-rdbl bid
		Bid lastNormal = null;
		for (int i = 0; i<bids.size(); i++)
		{
			Bid bid = bids.get(i);			
			if(ValidBidOption.isNormal(bid.getBid())) lastNormal = bid;			
		}
		suit = ValidBidOption.getSuit(lastNormal.getBid());
		level = ValidBidOption.getLevel(lastNormal.getBid());
		declarer = lastNormal.getSeatedPlayer();
		//reset the declarer if the partner of the winning player was the first to introduce the bid suit
		List<Bid> winningSuitBids = new ArrayList<>();		
		winningSuitBids = bids.stream().filter(bid -> ValidBidOption.isNormal(bid.getBid())).filter(bid -> ValidBidOption.getSuit(bid.getBid()).equals(suit))
			.filter(bid -> (bid.getSeatedPlayer().getPosition().equals(declarer.getPosition()) || bid.getSeatedPlayer().getPosition().equals(SeatPosition.getPartner(declarer.getPosition()))))
			.collect(Collectors.toList());		
		//make the first person to bid the winning suit the declarer
		if (winningSuitBids.size() > 0) 
		{			
			declarer = winningSuitBids.get(0).getSeatedPlayer();
			vulnerable = declarer.isVulnerable();
		}
	}
}