package com.test.rodion.androidcodetestrodionmourakhtanov.db;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.EntityFactory;

import java.util.List;

/**
 * Defines data access provider interface.
 */
public interface DataProvider {

    List<? extends Contact> getContacts();

    Contact getContact(long personId);

    void addContact(Contact contact);

    boolean deleteContact(long personId);

    boolean updateContact(Contact contact);

    EntityFactory getEntityFactory();
}
