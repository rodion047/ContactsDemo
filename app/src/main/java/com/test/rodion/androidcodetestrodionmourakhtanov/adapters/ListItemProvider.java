package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import android.view.View;
import android.view.ViewGroup;

/**
 * Defines list item provider interface. Intended to be used with
 * {@link FilteredItemListAdapter}.
 */
public interface ListItemProvider<T> {

    /**
     * Creates new item view.
     *
     * @param parent item parent view.
     * @return newly created item view.
     */
    View createItemView(ViewGroup parent);

    /**
     * Creates new header view.
     *
     * @param parent item parent view.
     * @return newly created header view.
     */
    View createHeaderView(ViewGroup parent);

    /**
     * Updates item view with new data.
     *
     * @param parent   item parent view.
     * @param itemView the item view to update.
     * @param itemData data for the item view.
     */
    void updateItemView(ViewGroup parent, View itemView, T itemData);

    /**
     * Updates header view with new data.
     *
     * @param parent     item parent view.
     * @param headerView the item view to update.
     * @param headerData data for the header view.
     */
    void updateHeaderView(ViewGroup parent, View headerView,
                          HeaderData<T> headerData);
}
