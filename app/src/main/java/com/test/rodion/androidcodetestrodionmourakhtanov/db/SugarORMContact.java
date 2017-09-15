package com.test.rodion.androidcodetestrodionmourakhtanov.db;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contact implementation to be used with SugarORMDataProvider. Objects of this class are stored in
 * the SQLite data base.
 * <p>
 * Note that I've chosen to keep data base "flat" and store everything in one table. Emails,
 * phone numbers and addresses are sored as JSON array of strings so that each list takes one
 * column in a data base table. The pro of this approach is the search performance as there is no
 * need to query related tables (there could be 3 of them!).
 * <p>
 * IMPORTANT! Class access MUST be public for Sugar ORM to be able to access the corresponding
 * SQLite table.
 */
public class SugarORMContact extends SugarRecord implements Contact {

    //
    // Persisted fields
    //
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String addresses;
    private String emails;
    private String phoneNumbers;
    private boolean favorite;

    //
    // Helper fields
    //
    @Ignore
    private List<String> addressList;
    @Ignore
    private List<String> emailList;
    @Ignore
    private List<String> phoneNumberList;
    @Ignore
    private static Gson gson = new Gson();
    @Ignore
    private static Type stringListType = new TypeToken<ArrayList<String>>() {
    }.getType();

    /**
     * Copy constructor.
     *
     * @param contact IPerson instance.
     */
    SugarORMContact(@Nullable Contact contact) {
        if (contact == null) return;
        this.firstName = contact.getFirstName();
        this.lastName = contact.getLastName();
        this.birthDate = contact.getBirthDate();
        setAddresses(contact.getAddresses());
        setEmails(contact.getEmails());
        setPhoneNumbers(contact.getPhoneNumbers());
    }

    /**
     * Default constructor. MUST be public for sugar ORM to be able to create a SQLite table for it.
     */
    public SugarORMContact() {
        // NOP
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public Date getBirthDate() {
        return birthDate;
    }

    @Override
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    @Nullable
    public List<String> getAddresses() {
        if (addressList != null) return addressList;
        if (addresses == null) return null;
        addressList = gson.fromJson(addresses, stringListType);
        return addressList;
    }

    @Override
    public void setAddresses(@Nullable List<String> addressList) {
        if (addressList == null) {
            addresses = null;
        } else {
            addresses = gson.toJson(addressList, stringListType);
        }
        this.addressList = addressList;
    }

    @Override
    @Nullable
    public List<String> getEmails() {
        if (emailList != null) return emailList;
        if (emails == null) return null;
        emailList = gson.fromJson(emails, stringListType);
        return emailList;
    }

    @Override
    public void setEmails(@Nullable List<String> emailList) {
        if (emailList == null) {
            emails = null;
        } else {
            emails = gson.toJson(emailList, stringListType);
        }
        this.emailList = emailList;
    }

    @Override
    @Nullable
    public List<String> getPhoneNumbers() {
        if (phoneNumberList != null) return phoneNumberList;
        if (phoneNumbers == null) return null;
        phoneNumberList = gson.fromJson(phoneNumbers, stringListType);
        return phoneNumberList;
    }

    @Override
    public void setPhoneNumbers(@Nullable List<String> phoneNumberList) {
        if (phoneNumberList == null) {
            phoneNumbers = null;
        } else {
            phoneNumbers = gson.toJson(phoneNumberList, stringListType);
        }
        this.phoneNumberList = phoneNumberList;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public void setFavorite(boolean value) {
        this.favorite = value;
    }
}
