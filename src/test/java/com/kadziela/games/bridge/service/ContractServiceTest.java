package com.kadziela.games.bridge.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Bid;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Contract;
import com.kadziela.games.bridge.model.PlayedCard;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.Trick;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.util.CardUtils;

@SpringBootTest
public class ContractServiceTest 
{
	private static final Logger logger = LogManager.getLogger(ContractServiceTest.class);

	@Autowired RoomService roomService;
	@Autowired TableService tableService;
	@Autowired ContractService contractService;
	
	@Test
	void testCalculateScore()
	{
		Table table = tableService.create();
		Player north = roomService.addPlayer("northC");
		Player east = roomService.addPlayer("eastC");
		Player south = roomService.addPlayer("southC");
		Player west = roomService.addPlayer("westC");
		tableService.sitDown(north, table.getId(), SeatPosition.NORTH);
		tableService.sitDown(east, table.getId(), SeatPosition.EAST);
		tableService.sitDown(south, table.getId(), SeatPosition.SOUTH);
		tableService.sitDown(west, table.getId(), SeatPosition.WEST);		
		Map<Card,SeatPosition> map = tableService.chooseFirstDealer(table);		 
		assertNotNull(map);
		assertNotNull(table.getCurrentDealer());
		Map<String,Object> score = contractService.calculateScore(buildContract(table), buid13Tricks(), buildHands(), table);
		logger.info("score = {}", score);
		score = contractService.calculateScore(buildContract(table), buid13Tricks(), buildHands(), table);
		logger.info("score = {}", score);
	}
	private Contract buildContract(Table table)
	{
		return new Contract(Arrays.asList(
				new Bid(table.getPlayerAtPosition(SeatPosition.NORTH), ValidBidOption.FIVE_CLUBS),
				new Bid(table.getPlayerAtPosition(SeatPosition.EAST), ValidBidOption.PASS),
				new Bid(table.getPlayerAtPosition(SeatPosition.SOUTH), ValidBidOption.PASS),
				new Bid(table.getPlayerAtPosition(SeatPosition.WEST), ValidBidOption.PASS)));		
	}
	private List<Trick> buid13Tricks()
	{
		List<Trick> result = new ArrayList<>();
		result.add(new Trick(new PlayedCard(new Card("2H"), SeatPosition.EAST), new PlayedCard(new Card("2D"), SeatPosition.SOUTH), 
			new PlayedCard(new Card("2S"), SeatPosition.WEST), new PlayedCard(new Card("2C"), SeatPosition.NORTH), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("3C"), SeatPosition.NORTH), new PlayedCard(new Card("3H"), SeatPosition.EAST), 
			new PlayedCard(new Card("3D"), SeatPosition.SOUTH), new PlayedCard(new Card("3S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("4C"), SeatPosition.NORTH), new PlayedCard(new Card("4H"), SeatPosition.EAST), 
				new PlayedCard(new Card("4D"), SeatPosition.SOUTH), new PlayedCard(new Card("4S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("5C"), SeatPosition.NORTH), new PlayedCard(new Card("5H"), SeatPosition.EAST), 
				new PlayedCard(new Card("5D"), SeatPosition.SOUTH), new PlayedCard(new Card("5S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("6C"), SeatPosition.NORTH), new PlayedCard(new Card("6H"), SeatPosition.EAST), 
				new PlayedCard(new Card("6D"), SeatPosition.SOUTH), new PlayedCard(new Card("6S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("7C"), SeatPosition.NORTH), new PlayedCard(new Card("7H"), SeatPosition.EAST), 
				new PlayedCard(new Card("7D"), SeatPosition.SOUTH), new PlayedCard(new Card("7S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("8C"), SeatPosition.NORTH), new PlayedCard(new Card("8H"), SeatPosition.EAST), 
				new PlayedCard(new Card("8D"), SeatPosition.SOUTH), new PlayedCard(new Card("8S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("9C"), SeatPosition.NORTH), new PlayedCard(new Card("9H"), SeatPosition.EAST), 
				new PlayedCard(new Card("9D"), SeatPosition.SOUTH), new PlayedCard(new Card("9S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("10C"), SeatPosition.NORTH), new PlayedCard(new Card("10H"), SeatPosition.EAST), 
				new PlayedCard(new Card("10D"), SeatPosition.SOUTH), new PlayedCard(new Card("10S"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("JC"), SeatPosition.NORTH), new PlayedCard(new Card("JH"), SeatPosition.EAST), 
				new PlayedCard(new Card("JD"), SeatPosition.SOUTH), new PlayedCard(new Card("JS"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("QC"), SeatPosition.NORTH), new PlayedCard(new Card("QH"), SeatPosition.EAST), 
				new PlayedCard(new Card("QD"), SeatPosition.SOUTH), new PlayedCard(new Card("QS"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("KC"), SeatPosition.NORTH), new PlayedCard(new Card("KH"), SeatPosition.EAST), 
				new PlayedCard(new Card("KD"), SeatPosition.SOUTH), new PlayedCard(new Card("KS"), SeatPosition.WEST), BidSuit.CLUBS));
		result.add(new Trick(new PlayedCard(new Card("AC"), SeatPosition.NORTH), new PlayedCard(new Card("AH"), SeatPosition.EAST), 
				new PlayedCard(new Card("AD"), SeatPosition.SOUTH), new PlayedCard(new Card("AS"), SeatPosition.WEST), BidSuit.CLUBS));
		return result;
	}
	private Map<SeatPosition,Collection<Card>> buildHands()
	{
		Map<SeatPosition,Collection<Card>> result = new HashMap<>();
		result.put(SeatPosition.NORTH, CardUtils.convertFromStringsToEnums(Arrays.asList("2C","3C","4C","5C","6C","7C","8C","9C","10C","JC","QC","KC","AC")));
		result.put(SeatPosition.EAST,CardUtils.convertFromStringsToEnums(Arrays.asList("2H","3H","4H","5H","6H","7H","8H","9H","10H","JH","QH","KH","AH")));
		result.put(SeatPosition.SOUTH,CardUtils.convertFromStringsToEnums(Arrays.asList("2D","3D","4D","5D","6D","7D","8D","9D","10D","JD","QD","KD","AD")));
		result.put(SeatPosition.WEST,CardUtils.convertFromStringsToEnums(Arrays.asList("2S","3S","4S","5S","6S","7S","8S","9S","10S","JS","QS","KS","AS")));								
		return result;
	}
}