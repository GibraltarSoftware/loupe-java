package com.onloupe.core.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * A (sorted) collection of Application User objects.
 */
public final class ApplicationUserCollection {
	
	/** The application user by guid. */
	private final Map<UUID, ApplicationUser> applicationUserByGuid = new HashMap<UUID, ApplicationUser>();
	
	/** The application user by key. */
	private final Map<String, ApplicationUser> applicationUserByKey = new HashMap<String, ApplicationUser>();
	
	/** The application user by user name. */
	private final Map<String, ApplicationUser> applicationUserByUserName = new HashMap<String, ApplicationUser>();
	
	/** The sorted application user. */
	private final List<ApplicationUser> sortedApplicationUser = new ArrayList<ApplicationUser>();
	
	/** The lock. */
	private final Object lock = new Object();

	/** The sort needed. */
	private boolean sortNeeded;
	
	/** The cached application user. */
	private ApplicationUser cachedApplicationUser; // this is a tetchy little performance optimization to save us
													// thread info lookup time

	/**
	 * Create a new empty ApplicationUserCollection.
	 */
	public ApplicationUserCollection() {
		this.sortNeeded = false; // We start empty, so there's nothing to sort.
	}

	/**
	 * Makes sure any new ApplicationUser items added to the collection have been
	 * re-sorted.
	 */
	private void ensureSorted() {
		synchronized (this.lock) {
			if (this.sortNeeded) {
				Collections.sort(this.sortedApplicationUser);
				this.sortNeeded = false;
			}
		}
	}

	/**
	 * Adds an item to the ApplicationUserCollection.
	 *
	 * @param item The ApplicationUser item to add.
	 * @return true, if successful
	 */
	public boolean add(ApplicationUser item) {
		if (item == null) {
			throw new NullPointerException("A null ApplicationUser can not be added to the collection.");
		}

		if (TypeUtils.isBlank(item.getFullyQualifiedUserName())) {
			throw new NullPointerException(
					"An ApplicationUser with a null username can not be added to the collection.");
		}

		synchronized (this.lock) {
			if (this.applicationUserByGuid.containsKey(item.getId())) {
				throw new IllegalStateException(
						"The collection already contains the ApplicationUser item being added.");
			}

			if (TypeUtils.isBlank(item.getKey())) {
				if (this.applicationUserByUserName.containsKey(item.getFullyQualifiedUserName())) {
					throw new IllegalStateException(
							"The collection already contains the ApplicationUser item being added.");
				}

				this.applicationUserByUserName.put(item.getFullyQualifiedUserName(), item);
			} else {
				if (this.applicationUserByKey.containsKey(item.getKey())) {
					throw new IllegalStateException(
							"The collection already contains the ApplicationUser item being added.");
				}

				this.applicationUserByKey.put(item.getKey(), item);
				this.applicationUserByUserName.put(item.getFullyQualifiedUserName(), item); // we will overwrite
																								// whatever's
				// there because we're a better
				// match.
			}

			this.applicationUserByGuid.put(item.getId(), item);
			this.sortedApplicationUser.add(item);
			this.sortNeeded = true; // Mark that we've added a new item which isn't yet sorted.
		}
		return true;
	}

	/**
	 * Clear the ApplicationUserCollection.
	 */
	public void clear() {
		synchronized (this.lock) {
			this.applicationUserByKey.clear();
			this.applicationUserByUserName.clear();
			this.applicationUserByGuid.clear();
			this.sortedApplicationUser.clear();
			this.sortNeeded = false; // We cleared them all, so there's nothing left to sort.
		}
	}

	/**
	 * Determines whether a given ApplicationUser item is already present in the
	 * ApplicationUserCollection.
	 *
	 * @param objectValue the object value
	 * @return True if present, false if not.
	 */
	public boolean contains(Object objectValue) {
		ApplicationUser item = (ApplicationUser) objectValue;
		if (item == null) {
			throw new NullPointerException("A null ApplicationUser can not be queried in the collection.");
		}

		synchronized (this.lock) {
			return this.applicationUserByGuid.containsKey(item.getId());
		}
	}

	/**
	 * Determines whether the ApplicationUserCollection contains a ApplicationUser
	 * with a specified Guid ID.
	 * 
	 * @param id The Guid ID of the ApplicationUser of interest.
	 * @return True if present, false if not.
	 */
	public boolean containsKey(UUID id) {
		synchronized (this.lock) {
			return this.applicationUserByGuid.containsKey(id);
		}
	}

	/**
	 * Determines whether the ApplicationUserCollection contains a ApplicationUser
	 * with a specified Key.
	 * 
	 * @param key The unique key of the ApplicationUser of interest.
	 * @return True if present, false if not.
	 */
	public boolean containsKey(String key) {
		synchronized (this.lock) {
			return this.applicationUserByKey.containsKey(key);
		}
	}

	/**
	 * Determines whether the ApplicationUserCollection contains a ApplicationUser
	 * with a specified user name.
	 * 
	 * @param userName The fully qualified user name of the ApplicationUser of
	 *                 interest.
	 * @return True if present, false if not.
	 */
	public boolean containsUserName(String userName) {
		synchronized (this.lock) {
			return this.applicationUserByUserName.containsKey(userName);
		}
	}

	/**
	 * Copy the collected ApplicationUser objects to a target array, in sorted
	 * order.
	 * 
	 * @param array      The target array (must be large enough to hold the Count of
	 *                   items starting at arrayIndex).
	 * @param arrayIndex The starting index in the target array at which to begin
	 *                   copying.
	 */
	public void copyTo(ApplicationUser[] array, int arrayIndex) {
		if (array == null) {
			throw new NullPointerException("Can not CopyTo a null array");
		}

		synchronized (this.lock) {
			ensureSorted();
			ApplicationUser[] applicationUser = this.sortedApplicationUser
					.toArray(new ApplicationUser[this.sortedApplicationUser.size()]);
			System.arraycopy(applicationUser, 0, array, arrayIndex, applicationUser.length);
		}
	}

	/**
	 * Gets the number of ApplicationUser items in the ApplicationUserCollection.
	 *
	 * @return the int
	 */
	public int size() {
		synchronized (this.lock) {
			return this.sortedApplicationUser.size();
		}
	}

	/**
	 * Gets a value indicating whether the ApplicationUserCollection is read-only.
	 * 
	 * @return False because a ApplicationUserCollection is never read-only.
	 * 
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Removes a specified ApplicationUser item from the ApplicationUserCollection.
	 *
	 * @param objectValue the object value
	 * @return True if item was found and removed from the
	 *         ApplicationUserCollection, false if not found.
	 */
	public boolean remove(Object objectValue) {
		ApplicationUser item = (ApplicationUser) objectValue;
		if (item == null) {
			throw new NullPointerException("A null ApplicationUser can not be removed from the collection.");
		}

		synchronized (this.lock) {
			if (this.applicationUserByGuid.containsKey(item.getId())) {
				this.sortedApplicationUser.remove(item); // We don't need to re-sort after a removal (unless already
															// needed).
				this.applicationUserByGuid.remove(item.getId());
				this.applicationUserByUserName.remove(item.getFullyQualifiedUserName());

				if (!TypeUtils.isBlank(item.getKey())) {
					this.applicationUserByKey.remove(item.getKey());
				}

				return true;
			}
			return false;
		}
	}

	/**
	 * Determines the index of a specific ApplicationUser in the
	 * ApplicationUserCollection (in sorted order).
	 *
	 * @param objectValue the object value
	 * @return The index of the ApplicationUser item if found in the list;
	 *         otherwise, -1.
	 */
	public int indexOf(Object objectValue) {
		ApplicationUser item = (ApplicationUser) objectValue;
		synchronized (this.lock) {
			ensureSorted();
			return this.sortedApplicationUser.indexOf(item);
		}
	}

	/**
	 * ApplicationUserCollection is sorted and does not support direct modification.
	 *
	 * @param index the index
	 * @param item the item
	 */
	public void add(int index, ApplicationUser item) {
		throw new UnsupportedOperationException(
				"ApplicationUserCollection is sorted and does not support direct modification.");
	}

	/**
	 * Remove the ApplicationUser item found at a specified index in the
	 * ApplicationUserCollection (in sorted order). (Supported but not recommended.)
	 *
	 * @param index The index (in the sorted order) of a ApplicationUser item to
	 *              remove.
	 * @return the application user
	 */
	public ApplicationUser remove(int index) {
		synchronized (this.lock) {
			ensureSorted();
			ApplicationUser victim = this.sortedApplicationUser.get(index);
			remove(victim);
			return victim;
		}
	}

	/**
	 * Sets the.
	 *
	 * @param sortIndex the sort index
	 * @param value the value
	 * @return the application user
	 */
	public ApplicationUser set(int sortIndex, ApplicationUser value) {
		throw new UnsupportedOperationException(
				"ApplicationUserCollection is sorted and does not support direct modification.");
	}

	/**
	 * Gets a ApplicationUser item at a specified index (in the sorted order). (NOT
	 * BY ThreadId or ThreadIndex! Use TryGetValue to lookup by ThreadIndex or
	 * TryFindThreadId to lookup by ThreadId.)
	 * 
	 * @param sortIndex The index (in the sorted order) of a ApplicationUser item to
	 *                  extract.
	 * @return The ApplicationUser item at that index in the sorted order of this
	 *         ApplicationUserCollection.
	 */
	public ApplicationUser get(int sortIndex) {
		synchronized (this.lock) {
			if (sortIndex < 0 || sortIndex >= this.sortedApplicationUser.size()) {
				throw new IndexOutOfBoundsException("Selected index is outside the range of the collection");
			}

			ensureSorted();
			return this.sortedApplicationUser.get(sortIndex);
		}
	}

	/**
	 * Gets a ApplicationUser item with a specified Guid ID.
	 * 
	 * @param id The Guid ID of the desired ApplicationUser.
	 * @return The ApplicationUser item with the specified Guid ID.
	 */
	public ApplicationUser get(UUID id) {
		synchronized (this.lock) {
			return this.applicationUserByGuid.get(id);
		}
	}

	/**
	 * Get the ApplicationUser with a specified Guid ID.
	 *
	 * @param id              The Guid ID of the desired ApplicationUser.
	 * @return True if found, false if not found.
	 */
	public ApplicationUser tryGetValue(UUID id) {
		synchronized (this.lock) {
			return (this.applicationUserByGuid.get(id));
		}
	}

	/**
	 * Get the ApplicationUser with a specified Key. (Use TryFindUserName() to look
	 * up by fully qualified user name.)
	 *
	 * @param key             The unique key of the desired ApplicationUser.
	 * @return True if found, false if not found.
	 */
	public ApplicationUser tryGetValue(String key) {
		if (TypeUtils.isBlank(key)) {
			throw new NullPointerException("key");
		}

		// this method gets *hammered* so we do a cheap one element cache.
		ApplicationUser applicationUser = this.cachedApplicationUser; // yep, outside the lock - because we're going to
																		// verify it in a second and we don't care what
																		// value it had.

		if (applicationUser != null && (key.equals(applicationUser.getKey()))) // if it's actually what they wanted then
																				// hooray! no need to go into the lock.
		{
			return applicationUser;
		}

		synchronized (this.lock) {
			this.cachedApplicationUser = applicationUser;
			return this.applicationUserByKey.get(key);
		}
	}

	/**
	 * Get the ApplicationUser with a specified fully qualified user name.
	 *
	 * @param userName        The fully qualified user name of the desired
	 *                        ApplicationUser.
	 * @return True if found, false if not found.
	 */
	public ApplicationUser tryFindUserName(String userName) {
		if (TypeUtils.isBlank(userName)) {
			throw new NullPointerException("userName");
		}

		synchronized (this.lock) {
			return this.applicationUserByUserName.get(userName);
		}
	}

	/**
	 * Set the specified value as a cached user if that user isn't present,
	 * returning the correct user from the collection.
	 *
	 * @param user the user
	 * @return the application user
	 */
	public ApplicationUser trySetValue(ApplicationUser user) {
		if (user == null) {
			throw new NullPointerException("user");
		}

		if (TypeUtils.isBlank(user.getFullyQualifiedUserName())) {
			throw new IllegalStateException("The provided user has no fully qualified user name");
		}

		synchronized (this.lock) {
			ApplicationUser existingUser;
			if (TypeUtils.isNotBlank(user.getKey())) {
				// see if it exists already by key; if so we return that.
				existingUser = this.applicationUserByKey.get(user.getKey());
				if (existingUser != null) {
					return existingUser;
				}
			}

			// see if it exists already by user name; if so we return that.
			existingUser = this.applicationUserByUserName.get(user.getFullyQualifiedUserName());
			if (existingUser != null) {
				return existingUser;
			}

			// If we got this far then it's not in our collection..
			add(user);
			return user;
		}
	}

	/**
	 * Returns an enumerator that iterates through the ApplicationUserCollection (in
	 * sorted order).
	 * 
	 * @return A <see cref="T:System.Collections.Generic.IEnumerator`1"/> that can
	 *         be used to iterate through the collection.
	 * 
	 *         <filterpriority>1</filterpriority>
	 */
	public Iterator<ApplicationUser> iterator() {
		synchronized (this.lock) {
			ensureSorted();
			return this.sortedApplicationUser.iterator();
		}
	}
}