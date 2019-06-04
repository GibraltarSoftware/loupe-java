package com.onloupe.model.data;

import java.util.UUID;
import java.util.function.Predicate;

import com.onloupe.model.session.ISessionSummary;

// TODO: Auto-generated Javadoc
/**
 * A standard collection for session summaries that provides indexing by session
 * id.
 */
public interface ISessionSummaryCollection extends Iterable<ISessionSummary> {


	/**
	 * Gets the.
	 *
	 * @param key the key
	 * @return the i session summary
	 */
	ISessionSummary get(UUID key);

	/**
	 * Indicates if the collection contains the key.
	 *
	 * @param key the key
	 * @return True if a session summary with the key exists in the collection,
	 *         false otherwise.
	 */
	boolean contains(UUID key);

	/**
	 * Searches for an element that matches the conditions defined by the specified
	 * predicate, and returns the first occurrence within the entire List.
	 * 
	 * @param match The Predicate delegate that defines the conditions of the
	 *              elements to search for.
	 * 
	 *              The Predicate is a delegate to a method that returns true if the
	 *              object passed to it matches the conditions defined in the
	 *              delegate. The elements of the current List are individually
	 *              passed to the Predicate
	 *              delegate, moving forward in the List, starting with the first
	 *              element and ending with the last element. Processing is stopped
	 *              when a match is found.
	 * 
	 * @return The first element that matches the conditions defined by the
	 *         specified predicate, if found; otherwise, null. match is a null
	 *         reference (Nothing in Visual Basic)
	 */
	ISessionSummary find(Predicate<ISessionSummary> match);

	/**
	 * Retrieves all the elements that match the conditions defined by the specified
	 * predicate.
	 * 
	 * @param match The Predicate delegate
	 *              that defines the conditions of the elements to search for.
	 * 
	 *              The Predicate is a
	 *              delegate to a method that returns true if the object passed to
	 *              it matches the conditions defined in the delegate. The elements
	 *              of the current List are individually passed to the
	 *              Predicate delegate, moving
	 *              forward in the List, starting with the first element and ending
	 *              with the last element.
	 * 
	 * @return A List containing all the elements that match the conditions defined
	 *         by the specified predicate, if found; otherwise, an empty List.
	 */
	ISessionSummaryCollection findAll(Predicate<ISessionSummary> match);

	/**
	 * Removes the first occurrence of a specified object.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	boolean remove(UUID key);

	/**
	 * Attempt to get the item with the specified key, returning true if it could be
	 * found.
	 *
	 * @param key  the key
	 * @param item the item
	 * @return True if the item could be found, false otherwise
	 */
	boolean tryGetValue(UUID key, ISessionSummary item);

	/**
	 * Size.
	 *
	 * @return the int
	 */
	int size();
}