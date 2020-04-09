package com.kadziela.games.bridge;

/**
 * Every object holding game related state (cards, tricks, players, etc.) 
 * needs to cleanup after a game ends and after a rubber ends, resetting state
 * in prepapration for a new deal.
 * 
 * @author Emanuel M. Kadziela
 *
 */
public interface NeedsCleanup 
{
	/**
	 * Cleanup state after each play
	 */
	void cleanupAfterPlay();
	/**
	 * Cleanup state after a game
	 */
	void cleanupAfterGame();
	/**
	 * Cleanup state after a rubber
	 */
	void cleanupAfterRubber();
}