package com.test.rodion.androidcodetestrodionmourakhtanov;

import android.support.annotation.Nullable;

import com.test.rodion.androidcodetestrodionmourakhtanov.adapters.ContactsListAdapter;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.util.Date;
import java.util.List;

/**
 * Performs search in a Person instance.
 */
public class ContactSearchFilter implements ContactsListAdapter.SearchFilter {

    /**
     * Searches for the provided string in all person's fields.
     *
     * @param contact       person instance to search in.
     * @param searchString search string.
     * @return <code>true</code> if any person's field contains the search string
     */
    @Override
    public boolean search(@Nullable Contact contact, @Nullable String searchString) {
        if (contact == null || searchString == null || searchString.isEmpty()) return false;

        // Search in first name
        boolean res = contact.getFirstName() != null && contact.getFirstName().toLowerCase()
                .contains(searchString);

        // Search in last name
        if (!res) {
            res = contact.getLastName() != null && contact.getLastName().toLowerCase()
                    .contains(searchString);
        }

        // Search in emails
        if (!res) {
            res = searchInList(contact.getEmails(), searchString);
        }

        // Search in phone numbers
        if (!res) {
            res = searchInList(contact.getPhoneNumbers(), searchString);
        }

        // Search in addresses
        if (!res) {
            res = searchInList(contact.getAddresses(), searchString);
        }

        // Search in birth date string
        if (!res) {
            Date birthDate = contact.getBirthDate();
            if (birthDate != null) {
                res = ContactBirthDateFormatter.toDateString(birthDate).toLowerCase().contains(searchString);
            }
        }
        return res;
    }

    private boolean searchInList(@Nullable List<String> list, @Nullable String searchString) {
        if (list == null || searchString == null || searchString.isEmpty()) return false;
        for (String value : list) {
            if (value != null && value.toLowerCase().contains(searchString)) return true;
        }
        return false;
    }
}
