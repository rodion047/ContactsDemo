package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.ContactComparator;

import java.util.List;

/**
 * Adapter for the contacts list view.
 */
public class ContactsListAdapter extends FilteredItemListAdapter<Contact> {

    /**
     * Defines contact search interface.
     */
    public interface SearchFilter {

        /**
         * Returns <code>true</code> if implemented method finds the search string in the person.
         *
         * @param contact      person instance to search in.
         * @param searchString search string.
         * @return <code>true</code> if implemented method finds the search string in the person.
         */
        boolean search(Contact contact, String searchString);
    }

    private final SearchFilter searchFilter;
    public final static Object FAVORITE_FILTER = new Object();

    public ContactsListAdapter(@NonNull ContactsListItemProvider itemProvider,
                               @Nullable List<Contact> data, boolean isAscending,
                               @Nullable SearchFilter searchFilter) {
        super(itemProvider);
        setGroupItems(true);
        setItemComparator(new ContactComparator(isAscending));
        setHeaderDataComparator(new ContactHeaderDataComparator(isAscending));
        this.searchFilter = searchFilter;
        setData(data);
    }

    /**
     * NOTE: not used in this project.
     * Returns <code>true</code> if the specified item matches custom filter.
     *
     * @param item         item to check.
     * @param customFilter filter instance.
     * @return <code>true</code> if the specified item matches the filter;
     * otherwise <code>false</code>.
     */
    @Override
    protected boolean isItemMatchesCustomFilter(Contact item, Object customFilter) {
        return customFilter == FAVORITE_FILTER && item.isFavorite();
    }

    /**
     * Returns <code>true</code> if the specified item matches the search string.
     *
     * @param item         item to check.
     * @param searchString search string.
     * @return <code>true</code> if the specified item matches specified search
     * string; otherwise <code>false</code>.
     */
    @Override
    protected boolean itemMatchesSearch(Contact item, String searchString) {
        return searchFilter != null && searchFilter.search(item, searchString);
    }

    /**
     * NOTE: not used in this project.
     * Returns <code>true</code> if the specified item matches specified filter
     * criteria.
     *
     * @param item       item to check.
     * @param filterTags filter criteria.
     * @return <code>true</code> if the specified item matches specified filter
     * criteria; otherwise <code>false</code>.
     */
    @Override
    protected boolean itemMatchesFilter(Contact item, long[] filterTags) {
        return true;
    }

    /**
     * NOTE: not used in this project.
     * Returns <code>true</code> if the specified item matches specified
     * sub-filter criteria.
     *
     * @param item       item to check.
     * @param filterTags filter criteria.
     * @return <code>true</code> if the specified item matches specified
     * sub-filter criteria; otherwise <code>false</code>.
     */
    @Override
    protected boolean itemMatchesSubFilter(Contact item, long[] filterTags) {
        return true;
    }

    /**
     * Get header data for the specified item. Items that are expected to appear
     * in the same group should return identical header data. The adapter will
     * use grouping key to split items into groups.
     *
     * @param item the item
     * @return header data for the specified item.
     */
    @Override
    @NonNull
    protected HeaderData<Contact> getItemHeaderData(Contact item) {
        return new ContactHeaderData(item);
    }
}
