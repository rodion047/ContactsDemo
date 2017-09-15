package com.test.rodion.androidcodetestrodionmourakhtanov.db;

import android.support.annotation.Nullable;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.EntityFactory;

import java.util.List;

/**
 * Data provider implementation based on Sugar ORM framework. Data is stored in SQLLite data base.
 */
class SugarORMDataProvider implements DataProvider {

    private static SugarORMDataProvider instance;

    /**
     * Private constructor to enforce singleton.
     */
    private SugarORMDataProvider() {
    }

    /**
     * Returns provider instance.
     *
     * @return provider instance.
     */
    static SugarORMDataProvider getInstance() {
        if (instance == null) {
            instance = new SugarORMDataProvider();
        }
        return instance;
    }

    @Override
    public List<? extends Contact> getContacts() {
        return SugarORMContact.listAll(SugarORMContact.class);
    }

    @Override
    public Contact getContact(long personId) {
        return SugarORMContact.findById(SugarORMContact.class, personId);
    }

    @Override
    public void addContact(@Nullable Contact contact) {
        if (contact == null) return;
        if (contact instanceof SugarORMContact) {
            ((SugarORMContact) contact).save();
        } else {
            new SugarORMContact(contact).save();
        }
    }

    @Override
    public boolean deleteContact(long personId) {
        SugarORMContact person = SugarORMContact.findById(SugarORMContact.class, personId);
        if (person == null) return false;
        person.delete();
        return true;
    }

    @Override
    public boolean updateContact(@Nullable Contact contact) {
        if (contact == null) return false;
        SugarORMContact record = SugarORMContact.findById(SugarORMContact.class, contact.getId());
        if (record == null) return false;
        if (contact instanceof SugarORMContact) {
            ((SugarORMContact) contact).save();
        } else {
            new SugarORMContact(contact).save();
        }
        return true;
    }

    @Override
    public EntityFactory getEntityFactory() {
        return new SugarORMEntityFactory();
    }
}
