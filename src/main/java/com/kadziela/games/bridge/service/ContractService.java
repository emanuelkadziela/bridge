package com.kadziela.games.bridge.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.kadziela.games.bridge.NeedsCleanup;
import com.kadziela.games.bridge.model.Bid;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Contract;
import com.kadziela.games.bridge.model.ContractScore;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.Trick;
import com.kadziela.games.bridge.model.enumeration.ScoreLineItem;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.util.MapUtil;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ContractService implements NeedsCleanup
{
	private static final Logger logger = LogManager.getLogger(ContractService.class);
	
	private final Map<Long,List<ContractScore>> scores = new ConcurrentHashMap<Long, List<ContractScore>>();
	
	@Autowired private TableService tableService;

	public synchronized Map<String,Object> bid(SeatPosition position, ValidBidOption bid, Long tableId) throws IllegalArgumentException,IllegalStateException
	{
		Assert.notNull(position, "position cannot be null");
		Assert.notNull(bid, "bid cannot be null");
		Assert.notNull(tableId, "tableId cannot be null");
		Table table = tableService.findById(Long.valueOf(tableId));
		SeatedPlayer seatedPlayer = table.getPlayerAtPosition(position);
		if(!ValidBidOption.validBid(bid, table.getCurrentBidOptions())) throw new IllegalArgumentException("Invalid bid");
		SeatPosition shouldBid = table.getCurrentBids().isEmpty() ? table.getCurrentDealer().getPosition() : 
			SeatPosition.nextPlayer(table.getCurrentBids().get(table.getCurrentBids().size()-1).getSeatedPlayer().getPosition());
		if (!(position.equals(shouldBid))) throw new IllegalStateException(String.format("This bid was expected to come from %s , not %s",shouldBid,position));
		Bid b = new Bid(seatedPlayer,bid);
		table.addValidatedBid(b);
		Map<String,Object> response = new HashMap<String,Object>();
		response.put("message", new StringBuilder("Valid Bid Made. "));
		response.put("position", position);
		response.put("bid", bid);
		response.put("nextPosition", SeatPosition.nextPlayer(position));
		if (contractMade(table.getCurrentBidOptions()))
		{
			Contract contract = table.createNewContract();
			((StringBuilder) response.get("message")).append("The bid was a third pass, the contract has been made");
			response.put("contract",contract);
			response.put("bids",contract.getBidsWithoutHands());
			response.put("nextPosition", SeatPosition.nextPlayer(contract.getDeclarer().getPosition()));
			response.put("dummy", SeatPosition.getPartner(contract.getDeclarer().getPosition()));
			response.put("dummyHand", table.getPlayerAtPosition(SeatPosition.getPartner(contract.getDeclarer().getPosition())).getHandCopy());
			return response;
		}
		if (redeal(table.getCurrentBidOptions()))
		{
			return MapUtil.mappifyMessage("The last bid was a fourth pass, redeal");
		}		
		return response;			
	}
	/**
	 * Returns a map containing a breakdown of the scores by team, game, and over/under
	 * @param contract - the current contract
	 * @param tricks actual tricks from the game that just ended
	 * @param hands actual hands that the players were dealt (originally)
	 * @param table the table where play occurred
	 * @return a map of string keys and integer values representing the score
	 */
	public Map<String,String> calculateScore(Contract contract, List<Trick> tricks, Map<SeatPosition,Collection<Card>> hands,Table table) throws IllegalArgumentException,IllegalStateException
	{
		Assert.notNull(table, "table cannot be null");
		Assert.notNull(contract, "contract cannot be null");
		Assert.notNull(contract, "tricks cannot be null");
		Assert.notNull(contract, "hands cannot be null");
		ContractScore current = new ContractScore(contract, tricks, hands);
		List<ContractScore> scoreList = scores.get(table.getId());		
		if (scoreList == null) 
		{
			scoreList = new CopyOnWriteArrayList<ContractScore>();
			scores.put(table.getId(), scoreList);
		}
		scoreList.add(current);
		SeatPosition game = game(table);
		boolean rubber = false;
		if (game != null)
		{
			rubber = rubberOrVul(table,current,game);
			table.cleanupAfterGame(null);
		}		
		Map<String,String> scoreBreakdown = convertScore(table.getId());
		table.cleanupAfterPlay();		
		if (rubber)
		{
			table.cleanupAfterRubber(null);
			cleanupAfterRubber(null);			
		}
		return scoreBreakdown;
	}
	public void cleanupAfterPlay() 
	{
		// nothing to clean up locally, only the table needs cleanup after each play
	}	
	public void cleanupAfterGame(Long tableId) 
	{
		for (ContractScore score : scores.get(tableId))
		{
			if (score.isClosed()) continue;
			score.setClosed(true);
		}
	}
	public void cleanupAfterRubber(Long tableId) {scores.get(tableId).clear();}
	private SeatPosition game(Table table)
	{
		//check if game has been reached by either team (>=100 points under)
		//if so, close all scores (new game begins, so points under the line start at 0 for both teams)
		//cleanup hands, tricks, and other state tracking elements
		//set the winning team to vulnerable
		
		int eastUnder = 0;
		int northUnder = 0;
		for (ContractScore score : scores.get(table.getId()))
		{
			if (score.isClosed()) continue;
			northUnder += sumUnder(score.getNorthLedger());
			eastUnder += sumUnder(score.getEastLedger());
		}
		if (northUnder >= 100)
		{
			logger.info("north south won the game");
			return SeatPosition.NORTH;
		}
		if (eastUnder >= 100)
		{
			logger.info("east west won the game");
			return SeatPosition.EAST;
		}
		return null;		
	}
	private boolean rubberOrVul(Table table, ContractScore currentScore,SeatPosition gameWinner)
	{
		//check if rubber has been reached (game made by a vulnerable team)
		//cleanup more state (i.e. scores)
		//award rubber points
		if (gameWinner.equals(SeatPosition.NORTH))
		{
			if (table.getPlayerAtPosition(SeatPosition.NORTH).isVulnerable())
			{
				logger.info("north south won the rubber");				
				addRubberBonus(currentScore.getNorthLedger(),!table.getPlayerAtPosition(SeatPosition.EAST).isVulnerable());
				return true;
			}			
			table.getPlayerAtPosition(SeatPosition.NORTH).setVulnerable(true);
			table.getPlayerAtPosition(SeatPosition.SOUTH).setVulnerable(true);			
		}
		else
		{
			if (table.getPlayerAtPosition(SeatPosition.EAST).isVulnerable())
			{
				logger.info("east west won the rubber");
				addRubberBonus(currentScore.getEastLedger(),!table.getPlayerAtPosition(SeatPosition.NORTH).isVulnerable());
				return true;
			}			
			table.getPlayerAtPosition(SeatPosition.EAST).setVulnerable(true);
			table.getPlayerAtPosition(SeatPosition.WEST).setVulnerable(true);
		}
		return false;
	}
	private int sumUnder(Map<ScoreLineItem,Integer> ledger)
	{
		int result = 0;
		for (int i = 0; i <= ScoreLineItem.CONTRACT_MINOR_REDOUBLED.ordinal(); i++)
		{
			ScoreLineItem item = ScoreLineItem.values()[i];
			Integer count = ledger.get(item);
			if (count != null && count > 0) result += item.points * count;
		}
		return result;
	}
	private int sumOver(Map<ScoreLineItem,Integer> ledger)
	{
		int result = 0;
		for (int i = ScoreLineItem.CONTRACT_MINOR_REDOUBLED.ordinal()+1; i <= ScoreLineItem.values().length-1; i++)
		{
			ScoreLineItem item = ScoreLineItem.values()[i];
			Integer count = ledger.get(item);
			if (count != null && count > 0) result += item.points * count;
		}
		return result;		
	}
	private void addRubberBonus (Map<ScoreLineItem,Integer> ledger, boolean fast)
	{
		if (fast) ledger.put(ScoreLineItem.RUBBER_FAST, 1);
		else ledger.put(ScoreLineItem.RUBBER_SLOW, 1);
	}
	private Map<String,String> convertScore(Long tableId) throws IllegalStateException
	{
		Map<String,String> scoreBreakdown = new ConcurrentHashMap<String, String>();		
		List<ContractScore> tableScores = scores.get(tableId);
		int eastUnder = 0;
		int northUnder = 0;
		int eastOver = 0;
		int northOver = 0;
		int gameCounter = 0;
		for (ContractScore score : tableScores)
		{
			eastUnder += sumUnder(score.getEastLedger());
			northUnder += sumUnder(score.getNorthLedger());
			eastOver += sumOver(score.getEastLedger());
			northOver += sumOver(score.getNorthLedger());
			if (eastUnder >= 100 || northUnder >= 100)
			{
				scoreBreakdown.put(String.format("game%sEastWestUnder", gameCounter),String.valueOf(eastUnder));
				scoreBreakdown.put(String.format("game%sNorthSouthUnder", gameCounter),String.valueOf(northUnder));
				scoreBreakdown.put(String.format("game%sEastWestOver", gameCounter),String.valueOf(eastOver));
				scoreBreakdown.put(String.format("game%sNorthSouthOver", gameCounter),String.valueOf(northOver));
				eastUnder = 0;
				northUnder = 0;
				eastOver = 0;
				northOver = 0;
				gameCounter++;
				
			}
			if (gameCounter > 3) throw new IllegalStateException("more than three games were either played or scored, so something is wrong"); 				
		}
		return scoreBreakdown;
	}
	private boolean contractMade(List<ValidBidOption> bids)
	{
		if (bids.size() > 3 &&
			bids.get(bids.size()-1).equals(ValidBidOption.PASS) && 
			bids.get(bids.size()-2).equals(ValidBidOption.PASS) && 
			bids.get(bids.size()-3).equals(ValidBidOption.PASS)) 		
			return true;		
		return false;
	}
	private boolean redeal(List<ValidBidOption> bids)
	{
		if (bids.size() == 4 &&
			bids.get(bids.size()-1).equals(ValidBidOption.PASS) && 
			bids.get(bids.size()-2).equals(ValidBidOption.PASS) && 
			bids.get(bids.size()-3).equals(ValidBidOption.PASS) &&
			bids.get(bids.size()-4).equals(ValidBidOption.PASS)) 		
			return true;		
		return false;
	}
}