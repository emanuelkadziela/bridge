package com.kadziela.games.bridge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.kadziela.games.bridge.model.Bid;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ContractService 
{
	private static final Logger logger = LogManager.getLogger(ContractService.class);
	
	
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
			Bid b = new Bid(seatedPlayer,bid);
			
			return b;
		}
		return null;
	}
}