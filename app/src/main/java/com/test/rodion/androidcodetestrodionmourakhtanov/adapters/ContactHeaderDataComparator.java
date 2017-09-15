package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.util.Comparator;

/**
 * Contacts list grouping header data comparator. Used to sort groups of contacts alphabetically.
 */
class ContactHeaderDataComparator implements Comparator<HeaderData<Contact>> {

    private final int ascending;

    /**
     * Creates new comparator instance.
     *
     * @param ascending specifies ordering direction.
     */
    ContactHeaderDataComparator(boolean ascending) {
        this.ascending = ascending ? 1 : -1;
    }

    @Override
    public int compare(HeaderData<Contact> left, HeaderData<Contact> right) {
        int res;
        ContactHeaderData lhs = (ContactHeaderData) left;
        ContactHeaderData rhs = (ContactHeaderData) right;
        if (lhs == null && rhs == null) {
            res = 0;
        } else if (lhs == null) {
            res = -1;
        } else if (rhs == null) {
            res = 1;
        } else {
            String lhsValue = lhs.getSortingValue();
            String rhsValue = rhs.getSortingValue();
            if (lhsValue == null && rhsValue == null) {
                res = 0;
            } else if (lhsValue == null) {
                res = -1;
            } else if (rhsValue == null) {
                res = 1;
            } else {
                res = lhsValue.toUpperCase().compareTo(
                        rhsValue.toUpperCase());
            }
        }

        return res * ascending;
    }
}
