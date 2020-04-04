package com.kadziela.games.bridge.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapUtil 
{
	public static Map<String, String> mappifyStringMessage(String message)
	{
		Map<String,String> map = new HashMap<String,String>();
		map.put("message", message);
		return map;
	}
}