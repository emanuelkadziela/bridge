package com.kadziela.games.bridge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.ContractService;
import com.kadziela.games.bridge.service.TableService;

@SpringBootTest
public class TableTest 
{	
	@Autowired ContractService contractService; 
	@Autowired TableService tableService; 
	
	@Test
	void testPlayCard()
	{
		Long id = System.currentTimeMillis();
		Table table = tableService.create(id);
		tableService.sitDown(new Player("North"), table.getId(), SeatPosition.NORTH);
		tableService.sitDown(new Player("South"), table.getId(), SeatPosition.SOUTH);
		tableService.sitDown(new Player("East"), table.getId(), SeatPosition.EAST);
		tableService.sitDown(new Player("West"), table.getId(), SeatPosition.WEST);

		table.setCurrentDealer(table.getPlayerAtPosition(SeatPosition.NORTH));
		
		contractService.bid(SeatPosition.NORTH, ValidBidOption.ONE_CLUBS, table.getId());
		contractService.bid(SeatPosition.EAST, ValidBidOption.PASS, table.getId());
		contractService.bid(SeatPosition.SOUTH, ValidBidOption.PASS, table.getId());
		contractService.bid(SeatPosition.WEST, ValidBidOption.PASS, table.getId());
				
		List<Card> cards = new ArrayList<Card>();
		cards.add(new Card(Rank.ACE,Suit.CLUBS));
		cards.add(new Card(Rank.KING,Suit.CLUBS));		
		table.getPlayerAtPosition(SeatPosition.NORTH).takeNewCards(cards);
		
		cards.clear();
		
		cards.add(new Card(Rank.QUEEN,Suit.CLUBS));
		cards.add(new Card(Rank.JACK,Suit.CLUBS));		
		table.getPlayerAtPosition(SeatPosition.EAST).takeNewCards(cards);
		
		cards.clear();
		
		cards.add(new Card(Rank.TEN,Suit.CLUBS));
		cards.add(new Card(Rank.NINE,Suit.CLUBS));		
		table.getPlayerAtPosition(SeatPosition.SOUTH).takeNewCards(cards);
		
		cards.clear();

		cards.add(new Card(Rank.EIGHT,Suit.CLUBS));
		cards.add(new Card(Rank.SEVEN,Suit.CLUBS));		
		table.getPlayerAtPosition(SeatPosition.WEST).takeNewCards(cards);
		
		cards.clear();
		
		table.createNewContract();
		
		table.playCard(new PlayedCard(new Card(Rank.JACK,Suit.CLUBS), SeatPosition.EAST));
		table.playCard(new PlayedCard(new Card(Rank.NINE,Suit.CLUBS), SeatPosition.SOUTH));
		table.playCard(new PlayedCard(new Card(Rank.SEVEN,Suit.CLUBS), SeatPosition.WEST));
		table.playCard(new PlayedCard(new Card(Rank.KING,Suit.CLUBS), SeatPosition.NORTH));
		
		assertNotNull(table.getTricks());
		assertEquals(1,table.getTricks().size());
		Trick trick = table.getTricks().get(0);
		assertEquals(new PlayedCard(new Card(Rank.KING,Suit.CLUBS), SeatPosition.NORTH), trick.getWinner());
		
		table.playCard(new PlayedCard(new Card(Rank.ACE,Suit.CLUBS), SeatPosition.NORTH));
		table.playCard(new PlayedCard(new Card(Rank.QUEEN,Suit.CLUBS), SeatPosition.EAST));
		table.playCard(new PlayedCard(new Card(Rank.TEN,Suit.CLUBS), SeatPosition.SOUTH));
		table.playCard(new PlayedCard(new Card(Rank.EIGHT,Suit.CLUBS), SeatPosition.WEST));
				
		assertEquals(2,table.getTricks().size());
		trick = table.getTricks().get(1);
		assertEquals(new PlayedCard(new Card(Rank.ACE,Suit.CLUBS), SeatPosition.NORTH), trick.getWinner());
		
		/*
		 * 		Assert.state(tricks.size() < 13, "13 (or more?) tricks have already been played");
		Assert.state(partialTrick.size() < 4, "more than 4 cards have been played for one trick");
		Assert.isTrue((players.get(card.getPosition()).hasCard(card.getCard())), String.format("player %s is not holding card %s",card.getPosition(),card.getCard()));			
		SeatPosition leader = SeatPosition.nextPlayer(currentContract.getDeclarer().getPosition());
		if (!tricks.isEmpty())
		{
			leader = tricks.get(tricks.size()-1).getWinner().getPosition();
		}
		if (partialTrick.isEmpty())
		{
			Assert.isTrue(card.getPosition().equals(leader),String.format("the card should be played by %s, not %s",leader,card.getPosition()));
			players.get(leader).playCard(card.getCard());
			partialTrick.add(card);
			return null;
		}
		PlayedCard previous = partialTrick.get(partialTrick.size()-1);
		SeatPosition throwCard = SeatPosition.nextPlayer(previous.getPosition());
		Assert.isTrue(card.getPosition().equals(throwCard),String.format("the card should be played by %s, not %s",throwCard,card.getPosition()));
		Suit led = partialTrick.get(0).getCard().getSuit();
		if(!(led.equals(card.getCard().getSuit())))			
		{
			Assert.isTrue((!players.get(card.getPosition()).hasSuit(led)), String.format("%s was led and %s has cards in that suit, must follow suit",led,card.getPosition()));			
		}
		players.get(card.getPosition()).playCard(card.getCard());
		if (partialTrick.size() == 3)
		{
			logger.debug("fourth card completes the trick");
			Trick trick = new Trick(partialTrick.get(0),partialTrick.get(1),partialTrick.get(2),card,currentContract.getSuit());
			partialTrick.clear();
			return trick;
		}
		partialTrick.add(card);
		return null;

		 */
		
	}
}