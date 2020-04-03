package com.kadziela.games.bridge.util;

import java.util.Collections;
import java.util.Map;

public class MapUtil 
{
	public static Map<String, String> mappifyStringMessage(String message)
	{
		return Collections.singletonMap("message", message);
	}
}