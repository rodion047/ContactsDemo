package com.test.rodion.androidcodetestrodionmourakhtanov.db;

import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.EntityFactory;

/**
 * Factory implementation to be used with SugarORMDataProvider.
 */
class SugarORMEntityFactory implements EntityFactory {

    @Override
    public Contact createPerson() {
        return new SugarORMContact();
    }
}
