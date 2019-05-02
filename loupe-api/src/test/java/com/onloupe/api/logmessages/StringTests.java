package com.onloupe.api.logmessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

public class StringTests
{

	@Test
	public final void testStringHashCodeCollision(TestReporter reporter)
	{
		final int maxCount = 262000;
		int tableCount = maxCount;
		int tableSize = 0;
		while (tableCount > 0)
		{
			tableCount >>= 1;
			tableSize++;
		}
		tableSize = 1 << tableSize;
		tableCount = tableSize * 4;

		Map<Integer, String> stringTable = new HashMap<Integer, String>(tableSize);
		Map<String, List<String>> collisionTable = new HashMap<String, List<String>>();
		UUID guid = UUID.randomUUID();
		String guidString = guid.toString();
		int totalCount = 0;
		while (stringTable.size() < maxCount && totalCount < tableCount)
		{
			totalCount++; // Make sure we don't run forever if guids don't fill up our table.
			String tableString;
			if (stringTable.containsKey(guidString.hashCode()) ? (tableString = stringTable.get(guidString.hashCode())).equals(tableString) : false)
			{
				if (!guidString.equals(tableString))
				{
					assert tableString.hashCode() == guidString.hashCode();
					reporter.publishEntry(String.format("\"%s\" and \"%s\" both have hash code: %s",
					        tableString, guidString, guidString.hashCode()));

					List<String> collisionList = collisionTable.get(tableString);
					if (collisionList == null)
					{
						collisionList = new ArrayList<String>();
						collisionTable.put(tableString, collisionList);
					}
					if (!collisionList.contains(guidString))
					{
						collisionList.add(guidString);
					}
				}
				// Otherwise we just found the exact same Guid string again!
			}
			else
			{
				stringTable.put(guidString.hashCode(), guidString);
			}

			guid = UUID.randomUUID();
			guidString = guid.toString();
		}

		int hashCollisionCount = collisionTable.size();
		reporter.publishEntry(String.format("Collision table found %d of %d hash codes with collisions (%d%%)",
				hashCollisionCount, stringTable.size(), (hashCollisionCount * 100) / stringTable.size()));

		if (hashCollisionCount > 0)
		{
			for (Map.Entry<String, List<String>> entry : collisionTable.entrySet())
			{
				String key = entry.getKey();
				List<String> valueList = entry.getValue();
				StringBuilder output = new StringBuilder();
				output.append(String.format("%08x : %2$s", key.hashCode(), key));
				for (String value : valueList)
				{
					output.append(String.format(", %1$s", value));
				}

				reporter.publishEntry(output.toString());
			}
		}
	}

	@Test
	public final void readOnlyValues()
	{
		StringContainer stringContainer = new StringContainer();
		Assertions.assertEquals(stringContainer.getReadOnlyExplicitProperty(), stringContainer.getExplicitProperty());
		Assertions.assertEquals(stringContainer.getReadOnlyExplicitProperty(), stringContainer.getImplicitProperty());
	}

	@Test
	public final void dictionaryStrings()
	{
		HashMap<String, String> dictionary = new HashMap<String, String>();

		for (int curTestLoop = 0;curTestLoop < 2000; curTestLoop++)
		{
			addDictionaryString(dictionary);
		}
	}

	@Test
	public final void listStrings()
	{
		ArrayList<String> list = new ArrayList<String>();

		for (int curTestLoop = 0; curTestLoop < 2000; curTestLoop++)
		{
			addListString(list);
		}
	}

	private static void addDictionaryString(HashMap<String, String> dictionary)
	{
		StringContainer stringContainer = new StringContainer(dictionary.size());

		dictionary.put(stringContainer.getReadOnlyExplicitProperty(), stringContainer.getReadOnlyExplicitProperty());

		String testArticle;
		testArticle = dictionary.get(stringContainer.getReadOnlyExplicitProperty()); //this is getting the value
		Assertions.assertEquals(testArticle, stringContainer.getReadOnlyExplicitProperty());

		testArticle = dictionary.get(stringContainer.getReadOnlyExplicitProperty());
		Assertions.assertEquals(testArticle, stringContainer.getReadOnlyExplicitProperty());

		for (Map.Entry<String, String> keyValuePair : dictionary.entrySet())
		{
			if (keyValuePair.getKey().equals(stringContainer.getReadOnlyExplicitProperty()))
			{
				Assertions.assertEquals(keyValuePair.getKey(), stringContainer.getReadOnlyExplicitProperty());
				Assertions.assertEquals(keyValuePair.getValue(), stringContainer.getReadOnlyExplicitProperty());
			}
		}
	}

	private static void addListString(ArrayList<String> list)
	{
		StringContainer stringContainer = new StringContainer(list.size());

		list.add(stringContainer.getReadOnlyExplicitProperty());

		String testArticle;
		testArticle = list.get(list.size() - 1); //this is getting the value
		Assertions.assertEquals(testArticle, stringContainer.getReadOnlyExplicitProperty());

		String[] array = list.toArray(new String[0]);
		testArticle = array[list.size() - 1];
		Assertions.assertEquals(testArticle, stringContainer.getReadOnlyExplicitProperty());
	}
}