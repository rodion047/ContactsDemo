package com.test.rodion.androidcodetestrodionmourakhtanov.model;

import java.util.Date;
import java.util.List;

/**
 * Defines Person entity interface.
 */
public interface Contact {

    Long getId();

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    Date getBirthDate();

    void setBirthDate(Date birthDate);

    List<String> getAddresses();

    void setAddresses(List<String> addressList);

    List<String> getEmails();

    void setEmails(List<String> emailList);

    List<String> getPhoneNumbers();

    void setPhoneNumbers(List<String> phoneNumbersList);

    boolean isFavorite();

    void setFavorite(boolean value);
}
