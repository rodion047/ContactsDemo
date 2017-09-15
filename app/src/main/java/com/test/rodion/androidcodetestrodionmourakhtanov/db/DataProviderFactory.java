package com.test.rodion.androidcodetestrodionmourakhtanov.db;

/**
 * Factory class to access a data provider.
 */
public class DataProviderFactory {

    /**
     * Returns data provider that this app will use.
     *
     * @return data provider that this app will use.
     */
    public static DataProvider getProviderInstance() {
        return SugarORMDataProvider.getInstance();
    }
}
