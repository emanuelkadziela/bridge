package com.kadziela.games.bridge.util;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestUtils 
{
	private TestUtils() {}
	
	 public static List<Map<String,Object>> queueToList(BlockingQueue<Map<String,Object>> queue) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		List<Map<String,Object>> messages = new ArrayList<Map<String,Object>>();
		while (true)
		{
			Map<String,Object> map2 = queue.poll(5, TimeUnit.SECONDS);
			if (map2 != null) 
			{
				messages.add(map2);
			}
			else break;
		}
		return messages;
	 }

}