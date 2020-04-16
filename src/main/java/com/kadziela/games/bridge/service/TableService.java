package com.kadziela.games.bridge.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.kadziela.games.bridge.NeedsCleanup;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.PlayedCard;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.Trick;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TableService
{
	private static final Logger logger = LogManager.getLogger(TableService.class);

	private final Map<Long,Table> tables = new ConcurrentHashMap<Long,Table>();
	private final Map<Long,Table> tablesByExternalId = new ConcurrentHashMap<Long,Table>();

	/**
	 * Creates a new, empty table and adds it to the internal collection
	 * @return the newly created table
	 */

	public Table create()
	{
		Table table = new Table();
		tables.put(table.getId(), table);
		return table;
	}
	public Table create(Long externalId)
	{
		Table table = new Table(externalId);
		tables.put(table.getId(), table);
		tablesByExternalId.put(externalId, table);
		return table;
	}
	public Collection<Long> getAllTableIds() 
	{
		return new HashSet<Long>(tables.keySet());
	}
	/**
	 * Seats a player at a table in an available seat. If there are four player sitting, will return false. 
	 * @param player the player to seat
	 * @param tableId the id of the table
	 * @param position the position where this player wants to sit
	 * @return true if there are more open seats, false otherwise
	 * @throws IllegalArgumentException - if the table cannot be found, or a player already sits in the position requested, etc.
	 * @throws IllegalStateException if the table is already full and more players are trying to sit, etc.
	 */
	public synchronized boolean sitDown(Player player,Long tableId, SeatPosition position) throws IllegalArgumentException, IllegalStateException
	{
		Table table = tables.get(tableId);
		Assert.notNull(table, String.format("%s does not match any extant table ids ",tableId));
		logger.info("trying to seat player: {} at table: {} and position: {}. So far {} players are sitting at this table.",player,tableId,position,table.playersSitting());
		if (table.playersSitting() >= 4) throw new IllegalStateException(String.format("this table (%s) already has 4 players sitting",tableId));
		table.sitDown(position, player);
		if (table.playersSitting() == 4) return false;			
		return true;
	}
	public Table standUp(Player player,Long tableId, SeatPosition position) throws IllegalArgumentException, IllegalStateException
	{
		Table table = tables.get(tableId);
		Assert.notNull(table, String.format("%s does not match any extant table ids ",tableId));
		table.standUp(position);
		return table;
	}
	public Table findById(Long tableId) {return tables.get(tableId);}
	public Table findByExternalId(Long externalId) {return tablesByExternalId.get(externalId);}
	public void deal(Table table)
	{
		Assert.notNull(table, "Cannot deal on a null table");
		logger.debug(String.format("dealing on table %s",table));
		table.deal();		
	}
	public Map<Card,SeatPosition> chooseFirstDealer(Table table) throws IllegalArgumentException
	{
		Assert.notNull(table, "Cannot choose a dealer on a null table");
		List<Card> shuffledDeck = table.getDeck().getShuffled();
		Card north = shuffledDeck.get(0);
		logger.debug(String.format("NORTH card is %s, ", north));
		Card east = shuffledDeck.get(1);
		logger.debug(String.format("EAST card is %s", east));
		Card south = shuffledDeck.get(2);
		logger.debug(String.format("SOUTH card is %s", south));
		Card west = shuffledDeck.get(3);
		logger.debug(String.format("WEST card is %s", west));
		logger.debug("sorting ... ");
		NavigableMap<Card,SeatPosition> map = new TreeMap<Card,SeatPosition>();
		map.put(north,SeatPosition.NORTH);
		map.put(east,SeatPosition.EAST);
		map.put(south,SeatPosition.SOUTH);
		map.put(west,SeatPosition.WEST);
		Map.Entry<Card, SeatPosition> lastEntry = map.lastEntry();
		logger.debug(String.format("the best card is %s, so the dealer is %s",lastEntry.getKey(),lastEntry.getValue()));
		table.setCurrentDealer(table.getPlayerAtPosition(lastEntry.getValue()));
		return map;
	}
	public Trick playCard(Card card, Long tableId, SeatPosition position) throws IllegalArgumentException
	{
		Table table = tables.get(tableId);
		Assert.notNull(table, String.format("%s does not match any extant table ids ",tableId));
		return table.playCard(new PlayedCard(card,position));
	}
	public Map<SeatPosition,Collection<Card>> getCurrentHands(Long tableId) throws IllegalArgumentException
	{
		Table table = tables.get(tableId);
		Assert.notNull(table, String.format("%s does not match any extant table ids ",tableId));
		Map<SeatPosition,Collection<Card>> result = new ConcurrentHashMap<SeatPosition, Collection<Card>>();
		for (SeatedPlayer player : table.getAllSeatedPlayers())
		{
			result.put(player.getPosition(), player.getHandCopy());
		}
		return result;
	}
	public Map<SeatPosition,Collection<Card>> getPersistentHands(Long tableId) throws IllegalArgumentException
	{
		Table table = tables.get(tableId);
		Assert.notNull(table, String.format("%s does not match any extant table ids ",tableId));
		Map<SeatPosition,Collection<Card>> result = new ConcurrentHashMap<SeatPosition, Collection<Card>>();
		for (SeatedPlayer player : table.getAllSeatedPlayers())
		{
			result.put(player.getPosition(), player.getPersistentHandCopy());			
		}
		return result;
	}
}