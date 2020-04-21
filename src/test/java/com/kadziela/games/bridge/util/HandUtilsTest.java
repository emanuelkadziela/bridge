package com.kadziela.games.bridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.ContractService;

@SpringBootTest
public class HandUtilsTest 
{
	private static final Logger logger = LogManager.getLogger(HandUtilsTest.class);

	@Test
	void testCounts()
	{
		List<Card> cards = new ArrayList<>();
		cards.add(new Card(Rank.KING, Suit.CLUBS));//3
		cards.add(new Card(Rank.ACE, Suit.CLUBS));//4
		cards.add(new Card(Rank.TWO, Suit.CLUBS));//0
		cards.add(new Card(Rank.FIVE, Suit.CLUBS));//0
		cards.add(new Card(Rank.SIX, Suit.CLUBS));//0
		cards.add(new Card(Rank.TEN, Suit.CLUBS));//0
		cards.add(new Card(Rank.JACK, Suit.CLUBS));//1
		cards.add(new Card(Rank.QUEEN, Suit.CLUBS));//2
		cards.add(new Card(Rank.FOUR, Suit.CLUBS));//0
		
		assertEquals(10, HandUtils.getHighCountPoints(cards));
		assertEquals(9, HandUtils.getDistributionPoints(cards));
		assertEquals(19, HandUtils.getTotalPointsDeduped(cards));
	}
	@Test
	void testCounts2()
	{
		List<Card> cards = new ArrayList<>();
		cards.add(new Card(Rank.KING, Suit.CLUBS));//3
		cards.add(new Card(Rank.ACE, Suit.CLUBS));//4
		cards.add(new Card(Rank.TWO, Suit.CLUBS));//0
		cards.add(new Card(Rank.FIVE, Suit.DIAMONDS));//0
		cards.add(new Card(Rank.SIX, Suit.DIAMONDS));//0
		cards.add(new Card(Rank.KING, Suit.HEARTS));//3
		
		assertEquals(10, HandUtils.getHighCountPoints(cards));
		assertEquals(6, HandUtils.getDistributionPoints(cards));
		assertEquals(14, HandUtils.getTotalPointsDeduped(cards));
	}
	@Test
	void testBidPass()
	{
		List<Card> cards = HandUtils.generateAShittyHand();
		logger.debug("hand = {}", cards);
		assertEquals(10, HandUtils.getHighCountPoints(cards));
		assertEquals(0, HandUtils.getDistributionPoints(cards));
		assertEquals(10, HandUtils.getTotalPointsDeduped(cards));
		assertEquals(ValidBidOption.PASS, HandUtils.suggestFirstOpeningBid5CMBM(cards));		
	}
}