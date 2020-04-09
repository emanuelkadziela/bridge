package com.kadziela.games.bridge.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ContractService implements NeedsCleanup
{
	private static final Logger logger = LogManager.getLogger(ContractService.class);
	
	private final Map<Long,List<ContractScore>> scores = new ConcurrentHashMap<Long, List<ContractScore>>();
		
	public Bid bid(SeatedPlayer seatedPlayer, ValidBidOption bid, Table table) throws IllegalArgumentException,IllegalStateException
	{
		Assert.notNull(seatedPlayer, "player cannot be null");
		Assert.notNull(bid, "bid cannot be null");
		Assert.notNull(table, "table cannot be null");
		if (table.getCurrentBids().isEmpty())
		{
			if (!(seatedPlayer.equals(table.getCurrentDealer())))
			{
				throw new IllegalStateException(String.format("The first player to bid should be the dealer. The dealer is %s, and the player attempting to bid is %s",table.getCurrentDealer(),seatedPlayer));
			}
			if (!ValidBidOption.validBid(bid, null)) throw new IllegalArgumentException("Invalid bid");
			Bid b = new Bid(seatedPlayer,bid);
			table.addValidatedBid(b);
			return b;
		}
		SeatPosition lastBidder = table.getCurrentBids().get(table.getCurrentBids().size()-1).getSeatedPlayer().getPosition();
		if (!seatedPlayer.getPosition().equals(SeatPosition.nextPlayer(lastBidder)))
		{
			throw new IllegalStateException(String.format("The last position who bid was %s. The next should be %s, not %s",lastBidder,SeatPosition.nextPlayer(lastBidder),seatedPlayer.getPosition()));
		}
		if(!ValidBidOption.validBid(bid, table.getCurrentBidOptions())) throw new IllegalArgumentException("Invalid bid");
		Bid b = new Bid(seatedPlayer,bid);
		table.addValidatedBid(b);
		return b;
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
		gameRubber(table);
		Map<String,String> scoreBreakdown = new ConcurrentHashMap<String, String>(); 
		table.cleanupAfterPlay();
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
	private void gameRubber(Table table)
	{
		//check if game has been reached by either team (>=100 points under)
		//if so, close all scores (new game begins, so points under the line start at 0 for both teams)
		//cleanup hands, tricks, and other state tracking elements
		//set the winning team to vulnerable
		
		//check if rubber has been reached (game made by a vulnerable team)
		//cleanup more state (i.e. scores)
		//award rubber points
		
		
		
		
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
			logger.info("north south won the game, cleaning up after game");
			table.cleanupAfterGame(null);
			cleanupAfterGame(null);
			if (table.getPlayerAtPosition(SeatPosition.NORTH).isVulnerable())
			{
				logger.info("north south won the rubber, cleaning up everything");
				table.cleanupAfterRubber(null);
				cleanupAfterRubber(null);
			}
		}
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
}