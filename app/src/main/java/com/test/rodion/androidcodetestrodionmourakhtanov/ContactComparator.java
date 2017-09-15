package com.test.rodion.androidcodetestrodionmourakhtanov;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.util.Comparator;

/**
 * Comparator for ordering persons alphabetically by first and last names.
 */
public class ContactComparator implements Comparator<Contact> {

    private int ascending;

    /**
     * Creates new comparator instance.
     *
     * @param ascending specifies ordering direction.
     */
    public ContactComparator(boolean ascending) {
        this.ascending = ascending ? 1 : -1;
    }

    @Override
    public int compare(Contact contact1, Contact contact2) {
        int res;
        if (contact1 == contact2) {
            res = 0;
        } else if (contact1 == null) {
            res = -1;
        } else if (contact2 == null) {
            res = 1;
        } else {
            // Build combined first name and last name strings for each person and compare them
            String fullName1 = (contact1.getFirstName() == null ? "" : contact1.getFirstName())
                    + " "
                    + (contact1.getLastName() == null ? "" : contact1.getLastName());
            String fullName2 = (contact2.getFirstName() == null ? "" : contact2.getFirstName())
                    + " "
                    + (contact2.getLastName() == null ? "" : contact2.getLastName());

            res = fullName1.trim().toLowerCase().compareTo(fullName2.trim().toLowerCase());
        }
        return res * ascending;
    }
}
