package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * >>>> NOTE <<<<
 * This adapter class is used in my other project so to save time I'm reusing it.
 * Some functionality of this adapter (like filtering by tag ids or sub-filtering) is not
 * used in this test project.
 * <p>
 * Base class for list adapters that allows filtering, sub-filtering, search and items grouping.
 *
 * @param <T> list item type.
 */
abstract class FilteredItemListAdapter<T> extends BaseAdapter {

    class ListHeader {
        private HeaderData<T> headerData;
        boolean collapsed;

        ListHeader(HeaderData<T> headerData) {
            if (headerData == null) {
                throw new IllegalArgumentException(
                        "parameter headerData is null");
            }
            this.headerData = headerData;
        }

        HeaderData<T> getHeaderData() {
            return headerData;
        }

        boolean isCollapsed() {
            return collapsed;
        }

        void setCollapsed(boolean value) {
            collapsed = true;
        }

        @Override
        public int hashCode() {
            return headerData.getGroupingKey().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o.getClass() == this.getClass() && o.hashCode() == this.hashCode();
        }
    }

    private class ListHeaderComparator implements Comparator<ListHeader> {

        private Comparator<HeaderData<T>> headerDataComparator;

        ListHeaderComparator(
                Comparator<HeaderData<T>> headerDataComparator) {
            if (headerDataComparator == null) {
                throw new IllegalArgumentException(
                        "headerDataComparator is null");
            }
            this.headerDataComparator = headerDataComparator;
        }

        @Override
        public int compare(ListHeader lhs, ListHeader rhs) {
            return headerDataComparator.compare(lhs.getHeaderData(),
                    rhs.getHeaderData());
        }
    }

    private static final int VIEW_TYPE_LIST_ITEM = 0;
    private static final int VIEW_TYPE_LIST_HEADER = 1;

    private ListItemProvider<T> itemProvider;
    private List<T> allItems = new ArrayList<>();
    private List<T> filteredItems = new ArrayList<>();
    private List<T> subFilteredItems = new ArrayList<>();
    private List<T> currentItems = new ArrayList<>();
    private LinkedHashMap<ListHeader, List<T>> headersMap = new LinkedHashMap<>();
    private boolean filterUpdated;
    private Object customFilter;
    private boolean groupItems;
    private boolean subFilterUpdated;
    private String lastSearchString;
    private long[] lastFilterTags;
    private long[] lastSubFilterTags;
    private Comparator<T> itemComparator;
    private ListHeaderComparator listHeaderComparator;

    /**
     * Creates new adapter instance.
     *
     * @param itemProvider the item provider implementation that will be creating list
     *                     views based on data the adapter will send it.
     * @throws IllegalArgumentException if itemProvider is <code>null</code>
     */
    FilteredItemListAdapter(ListItemProvider<T> itemProvider) {
        if (itemProvider == null) {
            throw new IllegalArgumentException("parameter itemProvider is null");
        }
        this.itemProvider = itemProvider;

        filterUpdated = true;
        subFilterUpdated = true;
        customFilter = null;
    }

    /**
     * Sets adapter data.
     *
     * @param data data source.
     */
    public void setData(List<T> data) {
        allItems = (data == null ? new ArrayList<T>() : new ArrayList<>(data));
        filterUpdated = true;
        subFilterUpdated = true;
        refresh();
    }

    /**
     * Sets adapter data and a filter.
     *
     * @param data       data source.
     * @param filterTags filter criteria.
     */
    public void setData(List<T> data, long[] filterTags) {
        allItems = (data == null ? new ArrayList<T>() : new ArrayList<>(data));

        // Set filter
        lastFilterTags = filterTags;
        filterUpdated = true;

        // Reset sub-filter
        lastSubFilterTags = null;
        subFilterUpdated = true;

        // Reset favorite filter
        customFilter = null;

        refresh();
    }

    void setGroupItems(boolean value) {
        groupItems = value;
        refresh();
    }

    void setHeaderDataComparator(
            Comparator<HeaderData<T>> headerComparator) {
        if (headerComparator != null) {
            listHeaderComparator = new ListHeaderComparator(headerComparator);
        } else {
            listHeaderComparator = null;
        }
        refresh();
    }

    void setItemComparator(Comparator<T> itemComparator) {
        this.itemComparator = itemComparator;
        refresh();
    }

    public void refresh() {
        if (customFilter != null) {
            applyCustomFilter();
        } else {
            if (filterUpdated) {
                applyFilter(lastFilterTags);
                filterUpdated = false;
            }
            if (subFilterUpdated) {
                applySubFilter(lastSubFilterTags);
                subFilterUpdated = false;
            }
        }

        // Apply search filter
        applySearch(lastSearchString);

        // Group items or clear groups
        if (groupItems) {
            groupItems();
        } else {
            headersMap.clear();
            if (itemComparator != null) {
                Collections.sort(currentItems, itemComparator);
            }
        }

        notifyDataSetChanged();
    }

    private void groupItems() {
        // Split items
        headersMap.clear();
        for (T item : currentItems) {
            HeaderData<T> itemHeader = getItemHeaderData(item);
            ListHeader listHeader = new ListHeader(itemHeader);
            List<T> itemList = headersMap.get(listHeader);
            if (itemList == null) {
                itemList = new ArrayList<>();
                headersMap.put(listHeader, itemList);
            }
            itemList.add(item);
        }

        // Sort child lists
        if (itemComparator != null) {
            for (Entry<ListHeader, List<T>> entry : headersMap.entrySet()) {
                Collections.sort(entry.getValue(), itemComparator);
            }
        }

        // Sort headers
        if (listHeaderComparator != null) {
            // Put headers into a list and sort it
            List<ListHeader> headerList = new ArrayList<>(
                    headersMap.keySet());
            Collections.sort(headerList, listHeaderComparator);

            // Copy one map into another using sorted lit of headers
            LinkedHashMap<ListHeader, List<T>> tmpMap = new LinkedHashMap<>();
            for (ListHeader listHeader : headerList) {
                tmpMap.put(listHeader, headersMap.get(listHeader));
            }

            headersMap = tmpMap;
        }
    }

    static final Object FILTER_FAVORITE = new Object();

    /**
     * Filter items marked as Favorites.
     */
    public void filterFavorites() {
        applyCustomFilter(FILTER_FAVORITE);
    }

    /**
     * Applies specified custom filter.
     *
     * @param filterTag specifies filter to apply.
     */
    public void applyCustomFilter(Object filterTag) {
        customFilter = filterTag;
        refresh();
    }

    /**
     * Selects items matching the custom filter from the allItems list and puts
     * them into subFilteredItems list.
     */
    private void applyCustomFilter() {
        subFilteredItems = new ArrayList<>();
        for (T item : allItems) {
            if (isItemMatchesCustomFilter(item, customFilter)) {
                subFilteredItems.add(item);
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified item matches the filter.
     *
     * @param item         item to check.
     * @param customFilter Custom filter instance. Can be any object that the adapter knows how to
     *                     use for filtering.
     * @return <code>true</code> if the specified item matches the filter;
     * otherwise <code>false</code>.
     */
    protected abstract boolean isItemMatchesCustomFilter(T item,
                                                         Object customFilter);

    /**
     * Applies search on top of the filtered and sub-filtered item list.
     *
     * @param searchString search string. Pass <code>null</code> or an empty string to cancel search.
     */
    public void search(String searchString) {
        lastSearchString = (searchString == null ? null : searchString.trim());
        refresh();
    }

    private void applySearch(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            currentItems = subFilteredItems;
            return;
        }

        currentItems = new ArrayList<>();
        for (T item : subFilteredItems) {
            if (itemMatchesSearch(item, searchString)) {
                currentItems.add(item);
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified item matches specified search
     * string.
     *
     * @param item         item to check.
     * @param searchString search string.
     * @return <code>true</code> if the specified item matches specified search
     * string; otherwise <code>false</code>.
     */
    protected abstract boolean itemMatchesSearch(T item, String searchString);

    /**
     * Applies filter to the item list.
     *
     * @param filterTags filter criteria. <code>null</code> cancels filtering and lets
     *                   all items through, empty array will filter out all items
     *                   resulting in an empty set.
     */
    public void filter(long[] filterTags) {
        // Set filter
        lastFilterTags = filterTags;
        filterUpdated = true;

        // Reset sub-filter
        lastSubFilterTags = null;
        subFilterUpdated = true;

        // Reset favorite filter
        customFilter = null;

        refresh();
    }

    /**
     * Applies specified criteria on top of the allItems list. Puts result in
     * the filteredItems list.
     *
     * @param filterTags filter criteria. <code>null</code> cancels filtering and lets
     *                   all items through, empty array will filter out all items
     *                   resulting in an empty set.
     */
    private void applyFilter(long[] filterTags) {
        if (filterTags == null) {
            filteredItems = allItems;
            return;
        }

        filteredItems = new ArrayList<>();
        for (T item : allItems) {
            if (itemMatchesFilter(item, filterTags)) {
                filteredItems.add(item);
            }
        }
    }

    /**
     * Applies sub-filter to the filtered item list.
     *
     * @param filterTags filter criteria.
     */
    public void subFilter(long[] filterTags) {
        // Set sub-filter
        lastSubFilterTags = filterTags;
        subFilterUpdated = true;

        // Reset favorite filter
        customFilter = null;

        refresh();
    }

    /**
     * Applies specified criteria on top of the filteredItems list. Puts result
     * in the subFilteredItems list.
     *
     * @param filterTags filter criteria.
     */
    private void applySubFilter(long[] filterTags) {
        if (filterTags == null) {
            subFilteredItems = filteredItems;
            return;
        }

        subFilteredItems = new ArrayList<>();
        for (T item : filteredItems) {
            if (itemMatchesSubFilter(item, filterTags)) {
                subFilteredItems.add(item);
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified item matches specified filter
     * criteria.
     * <p/>
     * In case of Event or Page item this method may just check if an item id
     * equals to any of the provided filter tags.
     *
     * @param item       item to check.
     * @param filterTags filter criteria.
     * @return <code>true</code> if the specified item matches specified filter
     * criteria; otherwise <code>false</code>.
     */
    protected abstract boolean itemMatchesFilter(T item, long[] filterTags);

    /**
     * Returns <code>true</code> if the specified item matches specified
     * sub-filter criteria.
     * <p/>
     * In case of Event or Page item this method may just check if an item id
     * equals to any of the provided filter tags.
     *
     * @param item       item to check.
     * @param filterTags filter criteria.
     * @return <code>true</code> if the specified item matches specified
     * sub-filter criteria; otherwise <code>false</code>.
     */
    protected abstract boolean itemMatchesSubFilter(T item, long[] filterTags);

    /**
     * Get header data for the specified item. Items that are expected to appear
     * in the same group should return identical header data. The adapter will
     * use grouping key to split items into groups. This method should never
     * return <code>null</code>.
     *
     * @param item the item
     * @return header data for the specified item.
     */
    @NonNull
    protected abstract HeaderData<T> getItemHeaderData(T item);

    @Override
    public int getCount() {
        if (headersMap == null || headersMap.isEmpty()) {
            // If grouping is disabled return current item list size
            return currentItems.size();
        }

        // If grouping is enabled sum up each group size and all headers
        int count = 0;
        for (Entry<ListHeader, List<T>> entry : headersMap
                .entrySet()) {
            FilteredItemListAdapter<T>.ListHeader header = entry.getKey();
            if (header.isCollapsed()) {
                // Do not include collapsed header children
                continue;
            }
            List<T> list = entry.getValue();
            if (list != null) {
                count += list.size();
            }
        }
        count += headersMap.size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (headersMap == null || headersMap.isEmpty()) {
            return currentItems.get(position);
        }

        int count = 0;
        for (Entry<ListHeader, List<T>> entry : headersMap
                .entrySet()) {
            FilteredItemListAdapter<T>.ListHeader header = entry.getKey();

            if (count == position) {
                return header;
            }
            count++;

            if (header.isCollapsed()) {
                // Skip collapsed header children
                continue;
            }

            List<T> list = entry.getValue();

            // The max index of an item that can be returned from the current
            // list
            int maxIdx = count + list.size() - 1;

            if (maxIdx < position) {
                // Our item isn't in the current list
                count += list.size();
                continue;
            }

            // Our item is in the current list
            return list.get(position - count);
        }

        // We should never get here
        throw new IllegalStateException("");
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);

        // Try to convert an item at the specified position to type <T> and
        // create an item view
        try {
            T listItem = (T) item;
            View itemView = convertView == null ? itemProvider
                    .createItemView(parent) : convertView;
            itemProvider.updateItemView(parent, itemView, listItem);
            return itemView;
        } catch (ClassCastException e) {
            // do nothing, it's expected to happen
        }

        // Try to convert an item at the specified position to ListHeader and
        // create a header view
        try {
            ListHeader header = (ListHeader) item;
            View headerView = convertView == null ? itemProvider
                    .createHeaderView(parent) : convertView;
            itemProvider.updateHeaderView(parent, headerView,
                    header.getHeaderData());
            return headerView;
        } catch (ClassCastException e) {
            // do nothing, it's expected to happen
        }

        // We should never get here
        throw new IllegalStateException(
                "Could not cast an item neither to an item type <T> nor to a ListHeader type");
    }

    @Override
    public int getViewTypeCount() {
        if (headersMap == null || headersMap.isEmpty()) {
            // If grouping is disabled we have only item views
            return 1;
        } else {
            // If grouping is enabled we have list headers and list items
            return 2;
        }
    }

    /**
     * The adapter supports maximum 2 view types: list items and optionally list
     * headers.
     */
    @Override
    public int getItemViewType(int position) {
        if (headersMap == null || headersMap.isEmpty()) {
            // If grouping is disabled we have only item views
            return VIEW_TYPE_LIST_ITEM;
        }
        int res;
        Object item = getItem(position);
        res = (convertInstanceOfObject(item, ListHeader.class) == null ? VIEW_TYPE_LIST_ITEM
                : VIEW_TYPE_LIST_HEADER);
        return res;
    }

    /**
     * Casts the given object to the type represented by specified class. If the
     * object is null then the result is also null.
     *
     * @param o     object to cast.
     * @param clazz cast target class.
     * @return instance of class T or <code>null</code> if cast failed or if the
     * object is <code>null</code>.
     */
    private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
