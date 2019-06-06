package com.onloupe.core.serialization.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.model.data.ISessionSummaryCollection;
import com.onloupe.model.session.ISessionSummary;


/**
 * The session summary collection implementation for the local collection
 * repository.
 */
public class SessionSummaryCollection implements ISessionSummaryCollection {
	
	/** The lock. */
	private final Object lock = new Object();
	
	/** The repository. */
	private LocalRepository repository;
	
	/** The list. */
	private final List<ISessionSummary> list = new ArrayList<ISessionSummary>();
	
	/** The dictionary. */
	private final Map<UUID, ISessionSummary> dictionary = new HashMap<UUID, ISessionSummary>();

	/**
	 * Create an empty session summary collection.
	 *
	 * @param repository the repository
	 */
	public SessionSummaryCollection(LocalRepository repository) {
		this.repository = repository;
	}

	/**
	 * Create a new collection by loading the provided summaries.
	 *
	 * @param repository the repository
	 * @param sessions the sessions
	 */
	public SessionSummaryCollection(LocalRepository repository, List<ISessionSummary> sessions) {
		this.repository = repository;

		this.list.addAll(sessions);
		for (ISessionSummary sessionSummary : sessions) {
			this.dictionary.put(sessionSummary.getId(), sessionSummary);
		}
	}

	/**
	 * Returns an enumerator that iterates through the collection.
	 * 
	 * @return A  that can
	 *         be used to iterate through the collection.
	 * 
	 */
	public final Iterator<ISessionSummary> getEnumerator() {
		return this.list.iterator();
	}

	/**
	 * Adds the.
	 *
	 * @param item the item
	 */
	public void add(ISessionSummary item) {
		addItem(item);
	}

	/**
	 * Adds the item.
	 *
	 * @param item the item
	 */
	private void addItem(ISessionSummary item) {
		synchronized (this.lock) {
			this.list.add(item);
			this.dictionary.put(item.getId(), item);
		}
	}

	/**
	 * Removes all items from the
	 * .
	 */
	public final void clear() {
		synchronized (this.lock) {
			this.list.clear();
			this.dictionary.clear();
		}
	}

	/**
	 * Indicates if the collection contains the key.
	 *
	 * @param key the key
	 * @return True if a session summary with the key exists in the collection,
	 *         false otherwise.
	 */
	@Override
	public final boolean contains(UUID key) {
		throw new UnsupportedOperationException("The Contains method has not been implemented.");
	}

	/**
	 * Determines whether the
	 *  contains a specific
	 * value.
	 *
	 * @param item The object to locate in the
	 *             .
	 * @return true if  is found in the
	 *         ; otherwise,
	 *         false.
	 */
	public final boolean contains(ISessionSummary item) {
		if (item == null) {
			throw new NullPointerException("item");
		}

		synchronized (this.lock) {
			return this.dictionary.containsKey(item.getId());
		}
	}

	/**
	 * Copies the elements of the
	 *  to an
	 * , starting at a particular
	 *  index.
	 *
	 * @param array      The one-dimensional  that is
	 *                   the destination of the elements copied from
	 *                   .
	 *                   The  must have zero-based
	 *                   indexing.
	 * @param arrayIndex The zero-based index in  at which
	 *                   copying begins.
	 */
	public final void copyTo(ISessionSummary[] array, int arrayIndex) {
		synchronized (this.lock) {
			System.arraycopy(this.list, 0, array, arrayIndex, this.list.size());
		}
	}

	/**
	 * Removes the first occurrence of a specific object from the
	 * .
	 *
	 * @param item The object to remove from the
	 *             .
	 * @return true if  was successfully removed from the
	 *         ; otherwise,
	 *         false. This method also returns false if  is
	 *         not found in the original
	 *         .
	 */
	public final boolean remove(ISessionSummary item) {
		return removeItem(item);
	}

	/**
	 * Removes the item.
	 *
	 * @param item the item
	 * @return true, if successful
	 */
	private boolean removeItem(ISessionSummary item) {
		if (item == null) {
			throw new NullPointerException("item");
		}

		// we want to be sure we remove any item with this key, not just this exact
		// obect to preserve collection symmetry.
		if (this.dictionary.containsKey(item.getId())) {
			this.dictionary.remove(item.getId());
			this.list.remove(item);
			return true;
		}

		return false;
	}

	/**
	 * Gets the number of elements contained in the
	 * .
	 * 
	 * @return The number of elements contained in the
	 *         .
	 * 
	 */
	public final int getCount() {
		return this.list.size();
	}

	/**
	 * Gets a value indicating whether the
	 *  is read-only.
	 * 
	 * @return true if the 
	 *         is read-only; otherwise, false.
	 * 
	 */
	public final boolean isReadOnly() {
		return false;
	}

	/**
	 * Searches for an element that matches the conditions defined by the specified
	 * predicate, and returns the first occurrence within the entire List.
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
	 *              with the last element. Processing is stopped when a match is
	 *              found.
	 * 
	 * @return The first element that matches the conditions defined by the
	 *         specified predicate, if found; otherwise, null.
	 *         @throws NullPointerException match is a null reference (Nothing in
	 *         Visual Basic)
	 */
	@Override
	public final ISessionSummary find(java.util.function.Predicate<ISessionSummary> match) {
		if (match == null) {
			throw new NullPointerException("match");
		}

		synchronized (this.lock) {
			// we only care about the FIRST match.
			for (ISessionSummary sessionSummary : this.list) {
				if (match.test(sessionSummary)) {
					return sessionSummary;
				}
			}
		}

		return null;
	}

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
	 *         @throws NullPointerException match is a null reference (Nothing in
	 *         Visual Basic)
	 */
	@Override
	public final ISessionSummaryCollection findAll(java.util.function.Predicate<ISessionSummary> match) {
		if (match == null) {
			throw new NullPointerException("match");
		}

		SessionSummaryCollection resultsCollection = new SessionSummaryCollection(this.repository);

		synchronized (this.lock) {
			for (ISessionSummary sessionSummary : this.list) {
				if (match.test(sessionSummary)) {
					resultsCollection.add(sessionSummary);
				}
			}
		}

		return resultsCollection;
	}

	/**
	 * Removes the first occurrence of a specified object.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	@Override
	public final boolean remove(UUID key) {
		synchronized (this.lock) {
			ISessionSummary victim = this.dictionary.get(key);
			boolean foundItem = false;
			if (victim != null) {
				foundItem = true;
				this.dictionary.remove(key);
				this.list.remove(victim);
			}

			return foundItem;
		}
	}

	/**
	 * Attempt to get the item with the specified key, returning true if it could be
	 * found.
	 *
	 * @param key the key
	 * @param item the item
	 * @return True if the item could be found, false otherwise
	 */
	@Override
	public final boolean tryGetValue(UUID key, ISessionSummary item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Determines the index of a specific item in the
	 * .
	 *
	 * @param item The object to locate in the
	 *             .
	 * @return The index of  if found in the list; otherwise,
	 *         -1.
	 */
	public final int indexOf(ISessionSummary item) {
		synchronized (this.lock) {
			return this.list.indexOf(item);
		}
	}

	/**
	 * Inserts an item to the  at
	 * the specified index.
	 *
	 * @param index The zero-based index at which  should be
	 *              inserted.
	 * @param item  The object to insert into the
	 *              .
	 */
	public final void insert(int index, ISessionSummary item) {
		throw new UnsupportedOperationException(
				"Inserting an item into the collection at a specific location is not supported.");
	}

	/**
	 * Removes the  item at the
	 * specified index.
	 *
	 * @param index The zero-based index of the item to remove.
	 */
	public final void removeAt(int index) {
		synchronized (this.lock) {
			// find what the hell item they're talking about and remove that using our other
			// overlaod.
			ISessionSummary item = this.list.get(index);
			remove(item);
		}
	}

	/**
	 * Gets or sets the element at the specified index.
	 *
	 * @param index The zero-based index of the element to get or set.
	 * @return The element at the specified index.
	 */
	public final ISessionSummary get(int index) {
		return this.list.get(index);
	}

	/**
	 * Sets the.
	 *
	 * @param index the index
	 * @param value the value
	 * @return the i session summary
	 */
	public final ISessionSummary set(int index, ISessionSummary value) {
		throw new UnsupportedOperationException("Updated items by index is not supported.");
	}

	/**
	 * get the item with the specified key.
	 *
	 * @param key the key
	 * @return the i session summary
	 */
	@Override
	public final ISessionSummary get(UUID key) {
		return this.dictionary.get(key);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.data.ISessionSummaryCollection#size()
	 */
	@Override
	public int size() {
		return this.list.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ISessionSummary> iterator() {
		return this.list.iterator();
	}
}