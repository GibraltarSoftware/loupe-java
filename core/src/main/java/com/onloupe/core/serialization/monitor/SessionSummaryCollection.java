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
 * repository
 */
public class SessionSummaryCollection implements ISessionSummaryCollection {
	private final Object lock = new Object();
	private LocalRepository repository;
	private final List<ISessionSummary> list = new ArrayList<ISessionSummary>();
	private final Map<UUID, ISessionSummary> dictionary = new HashMap<UUID, ISessionSummary>();

	/**
	 * Create an empty session summary collection
	 */
	public SessionSummaryCollection(LocalRepository repository) {
		this.repository = repository;
	}

	/**
	 * Create a new collection by loading the provided summaries.
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
	 * @return A <see cref="T:System.Collections.Generic.IEnumerator`1"/> that can
	 *         be used to iterate through the collection.
	 * 
	 *         <filterpriority>1</filterpriority>
	 */
	public final Iterator<ISessionSummary> getEnumerator() {
		return this.list.iterator();
	}

	public void add(ISessionSummary item) {
		addItem(item);
	}

	private void addItem(ISessionSummary item) {
		synchronized (this.lock) {
			this.list.add(item);
			this.dictionary.put(item.getId(), item);
		}
	}

	/**
	 * Removes all items from the
	 * <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
	 * @exception T:System.NotSupportedException The <see cref=
	 *                                           "T:System.Collections.Generic.ICollection`1"/>
	 *                                           is read-only.
	 * 
	 */
	public final void clear() {
		synchronized (this.lock) {
			this.list.clear();
			this.dictionary.clear();
		}
	}

	/**
	 * Indicates if the collection contains the key
	 * 
	 * @param key
	 * @return True if a session summary with the key exists in the collection,
	 *         false otherwise.
	 */
	@Override
	public final boolean contains(UUID key) {
		throw new UnsupportedOperationException("The Contains method has not been implemented.");
	}

	/**
	 * Determines whether the
	 * <see cref="T:System.Collections.Generic.ICollection`1"/> contains a specific
	 * value.
	 * 
	 * @return true if <paramref name="item"/> is found in the
	 *         <see cref="T:System.Collections.Generic.ICollection`1"/>; otherwise,
	 *         false.
	 * 
	 * @param item The object to locate in the
	 *             <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
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
	 * <see cref="T:System.Collections.Generic.ICollection`1"/> to an
	 * <see cref="T:System.Array"/>, starting at a particular
	 * <see cref="T:System.Array"/> index.
	 * 
	 * @param array      The one-dimensional <see cref="T:System.Array"/> that is
	 *                   the destination of the elements copied from
	 *                   <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 *                   The <see cref="T:System.Array"/> must have zero-based
	 *                   indexing.
	 * @param arrayIndex The zero-based index in <paramref name="array"/> at which
	 *                   copying begins.
	 * @exception T:System.ArgumentNullException       <paramref name="array"/> is
	 *                                                 null.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="arrayIndex"/>
	 *                                                 is less than 0.
	 * @exception T:System.ArgumentException           <paramref name="array"/> is
	 *                                                 multidimensional. -or-
	 *                                                 <paramref name="arrayIndex"/>
	 *                                                 is equal to or greater than
	 *                                                 the length of
	 *                                                 <paramref name="array"/>.
	 *                                                 -or- The number of elements
	 *                                                 in the source <see cref=
	 *                                                 "T:System.Collections.Generic.ICollection`1"/>
	 *                                                 is greater than the available
	 *                                                 space from
	 *                                                 <paramref name="arrayIndex"/>
	 *                                                 to the end of the destination
	 *                                                 <paramref name="array"/>.
	 * 
	 */
	public final void copyTo(ISessionSummary[] array, int arrayIndex) {
		synchronized (this.lock) {
			System.arraycopy(this.list, 0, array, arrayIndex, this.list.size());
		}
	}

	/**
	 * Removes the first occurrence of a specific object from the
	 * <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
	 * @return true if <paramref name="item"/> was successfully removed from the
	 *         <see cref="T:System.Collections.Generic.ICollection`1"/>; otherwise,
	 *         false. This method also returns false if <paramref name="item"/> is
	 *         not found in the original
	 *         <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
	 * @param item The object to remove from the
	 *             <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * @exception T:System.NotSupportedException The <see cref=
	 *                                           "T:System.Collections.Generic.ICollection`1"/>
	 *                                           is read-only.
	 * 
	 */
	public final boolean remove(ISessionSummary item) {
		return removeItem(item);
	}

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
	 * <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
	 * @return The number of elements contained in the
	 *         <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 * 
	 */
	public final int getCount() {
		return this.list.size();
	}

	/**
	 * Gets a value indicating whether the
	 * <see cref="T:System.Collections.Generic.ICollection`1"/> is read-only.
	 * 
	 * @return true if the <see cref="T:System.Collections.Generic.ICollection`1"/>
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
	 * @param match The <see cref="System.Predicate{T}">Predicate</see> delegate
	 *              that defines the conditions of the elements to search for.
	 * 
	 *              The <see cref="System.Predicate{T}">Predicate</see> is a
	 *              delegate to a method that returns true if the object passed to
	 *              it matches the conditions defined in the delegate. The elements
	 *              of the current List are individually passed to the
	 *              <see cref="System.Predicate{T}">Predicate</see> delegate, moving
	 *              forward in the List, starting with the first element and ending
	 *              with the last element. Processing is stopped when a match is
	 *              found.
	 * 
	 * @return The first element that matches the conditions defined by the
	 *         specified predicate, if found; otherwise, null.
	 *         <exception caption="Argument Null Exception" cref=
	 *         "System.ArgumentNullException">match is a null reference (Nothing in
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
	 * @param match The <see cref="System.Predicate{T}">Predicate</see> delegate
	 *              that defines the conditions of the elements to search for.
	 * 
	 *              The <see cref="System.Predicate{T}">Predicate</see> is a
	 *              delegate to a method that returns true if the object passed to
	 *              it matches the conditions defined in the delegate. The elements
	 *              of the current List are individually passed to the
	 *              <see cref="System.Predicate{T}">Predicate</see> delegate, moving
	 *              forward in the List, starting with the first element and ending
	 *              with the last element.
	 * 
	 * @return A List containing all the elements that match the conditions defined
	 *         by the specified predicate, if found; otherwise, an empty List.
	 *         <exception caption="Argument Null Exception" cref=
	 *         "System.ArgumentNullException">match is a null reference (Nothing in
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
	 * Removes the first occurrence of a specified object
	 * 
	 * @param key
	 * @return
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
	 * found
	 * 
	 * @return True if the item could be found, false otherwise
	 */
	@Override
	public final boolean tryGetValue(UUID key, ISessionSummary item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Determines the index of a specific item in the
	 * <see cref="T:System.Collections.Generic.IList`1"/>.
	 * 
	 * @return The index of <paramref name="item"/> if found in the list; otherwise,
	 *         -1.
	 * 
	 * @param item The object to locate in the
	 *             <see cref="T:System.Collections.Generic.IList`1"/>.
	 * 
	 */
	public final int indexOf(ISessionSummary item) {
		synchronized (this.lock) {
			return this.list.indexOf(item);
		}
	}

	/**
	 * Inserts an item to the <see cref="T:System.Collections.Generic.IList`1"/> at
	 * the specified index.
	 * 
	 * @param index The zero-based index at which <paramref name="item"/> should be
	 *              inserted.
	 * @param item  The object to insert into the
	 *              <see cref="T:System.Collections.Generic.IList`1"/>.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index"/> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>.
	 * @exception T:System.NotSupportedException       The <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>
	 *                                                 is read-only.
	 * 
	 */
	public final void insert(int index, ISessionSummary item) {
		throw new UnsupportedOperationException(
				"Inserting an item into the collection at a specific location is not supported.");
	}

	/**
	 * Removes the <see cref="T:System.Collections.Generic.IList`1"/> item at the
	 * specified index.
	 * 
	 * @param index The zero-based index of the item to remove.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index"/> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>.
	 * @exception T:System.NotSupportedException       The <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>
	 *                                                 is read-only.
	 * 
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
	 * @return The element at the specified index.
	 * 
	 * @param index The zero-based index of the element to get or set.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index"/> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>.
	 * @exception T:System.NotSupportedException       The property is set and the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"/>
	 *                                                 is read-only.
	 * 
	 */
	public final ISessionSummary get(int index) {
		return this.list.get(index);
	}

	public final ISessionSummary set(int index, ISessionSummary value) {
		throw new UnsupportedOperationException("Updated items by index is not supported.");
	}

	/**
	 * get the item with the specified key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public final ISessionSummary get(UUID key) {
		return this.dictionary.get(key);
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public Iterator<ISessionSummary> iterator() {
		return this.list.iterator();
	}
}