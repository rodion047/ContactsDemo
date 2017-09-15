package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.util.Locale;

/**
 * Utility object using for contacts grouping.
 */
class ContactHeaderData extends HeaderData<Contact> {

    private String headerTitle;

    ContactHeaderData(Contact contact) {
        // Try to get first name's first letter
        String fname = contact.getFirstName();
        headerTitle = (fname == null || fname.isEmpty()) ? null
                : fname.substring(0, 1).toUpperCase(Locale.getDefault());

        // If there is no first name, try to get the first letter of the last name
        if (headerTitle == null) {
            String lname = contact.getLastName();
            headerTitle = (lname == null || lname.isEmpty()) ? ""
                    : lname.substring(0, 1).toUpperCase(Locale.getDefault());
        }
    }

    String getSortingValue() {
        return headerTitle;
    }

    String getHeaderTitle() {
        return headerTitle;
    }

    @Override
    Object getGroupingKey() {
        return headerTitle;
    }

    @Override
    public int hashCode() {
        return headerTitle.hashCode();
    }
}
