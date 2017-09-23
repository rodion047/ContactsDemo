package com.test.rodion.androidcodetestrodionmourakhtanov.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.test.rodion.androidcodetestrodionmourakhtanov.R;
import com.test.rodion.androidcodetestrodionmourakhtanov.db.DataProviderFactory;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity for editing a contact or for creating a new one. If contact id is passed in this
 * activity it will look up a contact and edit it. If no id is passed it will create a new empty
 * contact and will allow and save it.
 */
public class ContactEditActivity extends AppCompatActivity {

    /**
     * Enumerates contact view modes.
     */
    enum Mode {
        /**
         * View contact in read only mode.
         */
        VIEW,

        /**
         * Edit contact.
         */
        EDIT
    }

    public static final String TAG = ContactEditActivity.class.getSimpleName();

    public static final String EXTRA_CONTACT_ID = "contactId";
    public static final String EXTRA_VIEW_MODE = "viewMode";

    private static final String STATE_PHONES_VIEW_LIST_SIZE = "phonesViewListSize";
    private static final String STATE_EMAILS_VIEW_LIST_SIZE = "emailsViewListSize";
    private static final String STATE_ADDRESSES_VIEW_LIST_SIZE = "addressesViewListSize";

    private static final int MAX_NUMOF_PHONE_NUMBERS = 10;
    private static final int MAX_NUMOF_EMAILS = 10;
    private static final int MAX_NUMOF_ADDRESSES = 10;
    private static final int PHONE_NUMBER_EDIT_START_ID = 500;
    private static final int EMAIL_EDIT_START_ID = 600;
    private static final int ADDRESS_EDIT_START_ID = 700;

    private EditText birthDateView;
    private TextView birthDateLabel;
    private Mode mode;
    private Contact contact;
    private List<EditText> phoneNumberViewsList = new ArrayList<>();
    private ViewGroup phoneNumberListContainer;
    private List<EditText> emailViewsList = new ArrayList<>();
    private ViewGroup emailListContainer;
    private List<EditText> addressViewsList = new ArrayList<>();
    private ViewGroup addressListContainer;
    private Calendar birthDateCalendar;
    private AlertDialog dialog;
    private int savedPhonesViewListSize;
    private int savedEmailsViewListSize;
    private int savedAddressesViewListSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        long contactId;
        if (savedInstanceState != null) {
            contactId = savedInstanceState.getLong(EXTRA_CONTACT_ID, 0);
            mode = (Mode) savedInstanceState.getSerializable(EXTRA_VIEW_MODE);
            savedPhonesViewListSize = savedInstanceState.getInt(STATE_PHONES_VIEW_LIST_SIZE, 0);
            savedEmailsViewListSize = savedInstanceState.getInt(STATE_EMAILS_VIEW_LIST_SIZE, 0);
            savedAddressesViewListSize = savedInstanceState.getInt(STATE_ADDRESSES_VIEW_LIST_SIZE, 0);
        } else {
            contactId = getIntent().getLongExtra(EXTRA_CONTACT_ID, 0);
            mode = (Mode) getIntent().getSerializableExtra(EXTRA_VIEW_MODE);
        }

        // Check that contact id is provided if the mode is "view" or "edit"
        if (mode != null && contactId == 0) {
            Log.e(TAG, "Contact id is required for mode: " + mode);
            Toast.makeText(this, "Contact id is required for mode: " + mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get contact
        if (mode != null) {
            contact = DataProviderFactory.getProviderInstance().getContact(contactId);
            if (contact == null) {
                Log.e(TAG, "Contact not found for id: " + contactId);
                Toast.makeText(this, "Contact not found for id: " + contactId, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        initViews(contact);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (contact != null) {
            outState.putLong(EXTRA_CONTACT_ID, contact.getId());
        }
        outState.putInt(STATE_PHONES_VIEW_LIST_SIZE, phoneNumberViewsList.size());
        outState.putInt(STATE_EMAILS_VIEW_LIST_SIZE, emailViewsList.size());
        outState.putInt(STATE_ADDRESSES_VIEW_LIST_SIZE, addressViewsList.size());
        outState.putSerializable(EXTRA_VIEW_MODE, mode);
        super.onSaveInstanceState(outState);
    }

    /**
     * Initializes screen views.
     *
     * @param contact contact instance to display or <code>null</code>
     *                if we intent to create a new contact.
     */
    private void initViews(@Nullable final Contact contact) {
        // Show top buttons bar if we creating a new contact
        findViewById(R.id.top_buttons_bar).setVisibility(contact == null || mode == Mode.EDIT
                ? View.VISIBLE : View.GONE);

        // Init birth date views
        birthDateView = (EditText) findViewById(R.id.birth_date);
        if (contact == null || mode == Mode.EDIT) {
            birthDateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showBirthDatePicker();
                }
            });
        }

        // Init "Add phone" button
        View addPhoneNumberButton = findViewById(R.id.button_add_phone_number);
        addPhoneNumberButton.setVisibility(contact == null || mode == Mode.EDIT ? View.VISIBLE : View.GONE);
        addPhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddPhoneClicked();
            }
        });

        // Init "Add email" button
        View addEmailButton = findViewById(R.id.button_add_email);
        addEmailButton.setVisibility(contact == null || mode == Mode.EDIT ? View.VISIBLE : View.GONE);
        addEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddEmailClicked();
            }
        });

        // Init "Add address" button
        View addAddressButton = findViewById(R.id.button_add_address);
        addAddressButton.setVisibility(contact == null || mode == Mode.EDIT ? View.VISIBLE : View.GONE);
        addAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddAddressClicked();
            }
        });

        // Set button "Cancel" click handler
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancelClicked();
            }
        });

        // Set button "OK" click handler
        findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonOKClicked();
            }
        });

        EditText firstNameEdit = (EditText) findViewById(R.id.first_name);

        if (contact == null) {
            // Add one empty phone number edit view
            addPhoneNumberLayout();

            // Add one empty email edit view
            addEmailLayout();
        } else {
            // Set first name
            firstNameEdit.setFocusable(mode == Mode.EDIT);
            firstNameEdit.setFocusableInTouchMode(mode == Mode.EDIT);
            final String firstName = contact.getFirstName();
            if (firstName != null) {
                firstNameEdit.setText(contact.getFirstName());
            }
            firstNameEdit.setEnabled(!(mode == Mode.VIEW && (firstName == null || firstName.isEmpty())));

            // Set last name
            EditText lastNameEdit = (EditText) findViewById(R.id.last_name);
            lastNameEdit.setFocusable(mode == Mode.EDIT);
            lastNameEdit.setFocusableInTouchMode(mode == Mode.EDIT);
            final String lastName = contact.getLastName();
            if (lastName != null) {
                lastNameEdit.setText(contact.getLastName());
            }
            lastNameEdit.setEnabled(!(mode == Mode.VIEW && (lastName == null || lastName.isEmpty())));

            // Set birth date
            if (contact.getBirthDate() != null) {
                // Set birth date data
                Calendar birthDate = Calendar.getInstance();
                birthDate.setTime(contact.getBirthDate());
                setBirthDateViewText(birthDate);
            }

            // Init "Edit contact" button
            FloatingActionButton buttonEditContact = (FloatingActionButton) findViewById(R.id.button_edit_contact);
            buttonEditContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mode = Mode.EDIT;
                    getPhoneNumberListContainer().removeAllViews();
                    phoneNumberViewsList.clear();
                    emailViewsList.clear();
                    getEmailListContainer().removeAllViews();
                    addressViewsList.clear();
                    getAddressListContainer().removeAllViews();
                    initViews(contact);
                }
            });
            buttonEditContact.setVisibility(mode == Mode.VIEW ? View.VISIBLE : View.GONE);

            // Set phone numbers
            List<String> phoneNumbers = contact.getPhoneNumbers();
            if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                for (String phoneNumber : phoneNumbers) {
                    EditText phoneNumberEdit = addPhoneNumberLayout();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        phoneNumberEdit.setText(phoneNumber);
                        phoneNumberEdit.setFocusable(mode == Mode.EDIT);
                        phoneNumberEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            } else if (mode == Mode.EDIT) {
                // Add one empty phone number edit view
                addPhoneNumberLayout();
            }

            // Set emails
            List<String> emails = contact.getEmails();
            if (emails != null && !emails.isEmpty()) {
                for (String email : emails) {
                    EditText emailEdit = addEmailLayout();
                    if (email != null && !email.isEmpty()) {
                        emailEdit.setText(email);
                        emailEdit.setFocusable(mode == Mode.EDIT);
                        emailEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            } else if (mode == Mode.EDIT) {
                // Add one empty email edit view
                addEmailLayout();
            }

            // Set addresses
            List<String> addresses = contact.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                for (String address : addresses) {
                    EditText addressEdit = addAddressLayout();
                    if (address != null && !address.isEmpty()) {
                        addressEdit.setText(address);
                        addressEdit.setFocusable(mode == Mode.EDIT);
                        addressEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            }
        }

        // IMPORTANT:
        // If this activity is being restored, we must add back in fields that user had added
        // (but had not saved yet) so that onRestoreInstanceState can restore those fields values.
        int numOfFields = savedPhonesViewListSize - phoneNumberViewsList.size();
        if (numOfFields > 0) {
            for (int i = 0; i < numOfFields; i++) {
                addPhoneNumberLayout();
            }
        }
        numOfFields = savedEmailsViewListSize - emailViewsList.size();
        if (numOfFields > 0) {
            for (int i = 0; i < numOfFields; i++) {
                addEmailLayout();
            }
        }
        numOfFields = savedAddressesViewListSize - addressViewsList.size();
        if (numOfFields > 0) {
            for (int i = 0; i < numOfFields; i++) {
                addAddressLayout();
            }
        }

        firstNameEdit.requestFocus();
    }

    private void onButtonAddEmailClicked() {
        addEmailLayout();
    }

    private void onButtonAddAddressClicked() {
        addAddressLayout();
    }

    private void onButtonAddPhoneClicked() {
        addPhoneNumberLayout();
    }

    private EditText addPhoneNumberLayout() {
        ViewGroup parent = getPhoneNumberListContainer();
        View phoneNumberLayout = getLayoutInflater().inflate(R.layout.phone_number_list_item, parent, false);
        parent.addView(phoneNumberLayout);

        final EditText phoneEditView = phoneNumberLayout.findViewById(R.id.phone_number);
        // Set UNIQUE id to the phone number EditText so that activity restore works correctly
        phoneEditView.setId(PHONE_NUMBER_EDIT_START_ID + phoneNumberViewsList.size());
        phoneNumberViewsList.add(phoneEditView);

        TextView phoneNumberLabelView = phoneNumberLayout.findViewById(R.id.phone_number_label);
        phoneNumberLabelView.setText(getString(R.string.contacts_edit_phone_number_label,
                phoneNumberViewsList.size()));

        // Hide "Add phone" button if we've reached the limit
        if (phoneNumberViewsList.size() >= MAX_NUMOF_PHONE_NUMBERS) {
            findViewById(R.id.button_add_phone_number).setVisibility(View.GONE);
        }

        phoneEditView.requestFocus();
        return phoneEditView;
    }

    private EditText addEmailLayout() {
        ViewGroup parent = getEmailListContainer();
        View emailLayout = getLayoutInflater().inflate(R.layout.email_list_item, parent, false);
        parent.addView(emailLayout);

        final EditText emailEditView = emailLayout.findViewById(R.id.email);
        // Set UNIQUE id to the EditText view so that activity restore works correctly
        emailEditView.setId(EMAIL_EDIT_START_ID + emailViewsList.size());
        emailViewsList.add(emailEditView);

        // Set label text
        TextView emailLabelView = emailLayout.findViewById(R.id.email_label);
        emailLabelView.setText(getString(R.string.contacts_edit_email_label, emailViewsList.size()));

        // Hide "Add email" button if we've reached the limit
        if (emailViewsList.size() >= MAX_NUMOF_EMAILS) {
            findViewById(R.id.button_add_email).setVisibility(View.GONE);
        }

        emailEditView.requestFocus();
        return emailEditView;
    }

    private EditText addAddressLayout() {
        ViewGroup parent = getAddressListContainer();
        View addressLayout = getLayoutInflater().inflate(R.layout.address_list_item, parent, false);
        parent.addView(addressLayout);

        final EditText addressEditView = addressLayout.findViewById(R.id.address);
        // Set UNIQUE id to the EditText view so that activity restore works correctly
        addressEditView.setId(ADDRESS_EDIT_START_ID + addressViewsList.size());
        addressViewsList.add(addressEditView);

        // Set label text
        TextView addressLabelView = addressLayout.findViewById(R.id.address_label);
        addressLabelView.setText(getString(R.string.contacts_edit_address_label, addressViewsList.size()));

        // Hide "Add address" button if we've reached the limit
        if (addressViewsList.size() >= MAX_NUMOF_ADDRESSES) {
            findViewById(R.id.button_add_address).setVisibility(View.GONE);
        }

        addressEditView.requestFocus();
        return addressEditView;
    }

    private void onButtonOKClicked() {
        createOrUpdateContact();
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Creates new contact or updates existing one and saves it.
     */
    private void createOrUpdateContact() {
        if (contact == null) {
            contact = DataProviderFactory.getProviderInstance().getEntityFactory().createContact();
        }

        contact.setFirstName(((TextView) findViewById(R.id.first_name)).getText().toString());
        contact.setLastName(((TextView) findViewById(R.id.last_name)).getText().toString());
        if (birthDateCalendar != null) {
            contact.setBirthDate(birthDateCalendar.getTime());
        }

        // Set phones
        List<String> phoneList = new ArrayList<>();
        for (EditText view : phoneNumberViewsList) {
            String phoneString = view.getText().toString().trim();
            if (!phoneString.isEmpty()) {
                phoneList.add(phoneString);
            }
        }
        contact.setPhoneNumbers(phoneList);

        // Set emails
        List<String> emailList = new ArrayList<>();
        for (EditText view : emailViewsList) {
            String emailString = view.getText().toString().trim();
            if (!emailString.isEmpty()) {
                emailList.add(emailString);
            }
        }
        contact.setEmails(emailList);

        // Set addresses
        List<String> addressList = new ArrayList<>();
        for (EditText view : addressViewsList) {
            String addressString = view.getText().toString().trim();
            if (!addressString.isEmpty()) {
                addressList.add(addressString);
            }
        }
        contact.setAddresses(addressList);

        // Save contact
        if (contact.getId() == null) {
            DataProviderFactory.getProviderInstance().addContact(contact);
        } else {
            DataProviderFactory.getProviderInstance().updateContact(contact);
        }
    }

    /**
     * Returns true if changes were made to the contact.
     *
     * @return true if changes were made to the contact.
     */
    private boolean isContactEdited() {
        // Check first name
        String firstName = ((EditText) findViewById(R.id.first_name)).getText().toString();
        if (contact == null) {
            if (!firstName.isEmpty()) return true;
        } else {
            if (!firstName.equals(contact.getFirstName())) return true;
        }

        // Check last name
        String lastName = ((EditText) findViewById(R.id.last_name)).getText().toString();
        if (contact == null) {
            if (!lastName.isEmpty()) return true;
        } else {
            if (!lastName.equals(contact.getLastName())) return true;
        }

        // Check birth date
        if (birthDateCalendar != null) {
            if (contact == null) return true;
            Date date1 = birthDateCalendar.getTime();
            Date date2 = contact.getBirthDate();
            if (date1 != null && date2 != null) {
                if (date1.compareTo(date2) != 0) return true;
            } else if (date1 != null || date2 != null) {
                return true;
            }
        }

        // Check phone numbers
        List<String> phoneList = new ArrayList<>();
        for (EditText view : phoneNumberViewsList) {
            String phoneString = view.getText().toString().trim();
            if (!phoneString.isEmpty()) {
                phoneList.add(phoneString);
            }
        }
        if (contact == null) {
            if (phoneList.size() > 0) return true;
        } else {
            if (!listsEqual(contact.getPhoneNumbers(), phoneList)) return true;
        }

        // Check emails
        List<String> emailList = new ArrayList<>();
        for (EditText view : emailViewsList) {
            String emailString = view.getText().toString().trim();
            if (!emailString.isEmpty()) {
                emailList.add(emailString);
            }
        }
        if (contact == null) {
            if (emailList.size() > 0) return true;
        } else {
            if (!listsEqual(contact.getEmails(), emailList)) return true;
        }

        // Check addresses
        List<String> addressList = new ArrayList<>();
        for (EditText view : addressViewsList) {
            String addressString = view.getText().toString().trim();
            if (!addressString.isEmpty()) {
                addressList.add(addressString);
            }
        }
        if (contact == null) {
            if (addressList.size() > 0) return true;
        } else {
            return !listsEqual(contact.getAddresses(), addressList);
        }

        return false;
    }

    private boolean listsEqual(List<String> list1, List<String> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!Objects.equals(list1.get(i), list2.get(i))) return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if ((mode == null || mode == Mode.EDIT) && isContactEdited()) {
            // If changes were made to the new or existing contact - show confirmation dialog
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.contacts_edit_discard_dialog_title)
                    .setPositiveButton(R.string.contacts_edit_discard_dialog_positive_button_title,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ContactEditActivity.super.onBackPressed();
                                }
                            })
                    .setNegativeButton(R.string.contacts_edit_discard_dialog_negative_button_title,
                            null)
                    .create();
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    private void onButtonCancelClicked() {
        onBackPressed();
    }

    /**
     * shows dae picker dialog and sets birth date edit view.
     */
    private void showBirthDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                birthDateCalendar = Calendar.getInstance();
                birthDateCalendar.set(Calendar.YEAR, year);
                birthDateCalendar.set(Calendar.MONTH, monthOfYear);
                birthDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                birthDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                birthDateCalendar.set(Calendar.MINUTE, 0);
                birthDateCalendar.set(Calendar.SECOND, 0);
                birthDateCalendar.set(Calendar.MILLISECOND, 0);
                setBirthDateViewText(birthDateCalendar);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Sets birth date edit view text.
     *
     * @param calendar calendar instance containing the data to set or null to clear the view text.
     */
    private void setBirthDateViewText(@Nullable Calendar calendar) {
        if (calendar == null) {
            birthDateView.setText("");
            getBirthDateLabel().setVisibility(View.GONE);
            return;
        }
        String format = "MMMM dd yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        birthDateView.setText(formatter.format(calendar.getTime()));
        getBirthDateLabel().setVisibility(View.VISIBLE);
    }

    private TextView getBirthDateLabel() {
        if (birthDateLabel == null) birthDateLabel = (TextView) findViewById(R.id.birth_date_label);
        return birthDateLabel;
    }

    private ViewGroup getPhoneNumberListContainer() {
        if (phoneNumberListContainer == null) {
            phoneNumberListContainer = (ViewGroup) findViewById(R.id.phone_number_list_container);
        }
        return phoneNumberListContainer;
    }

    private ViewGroup getEmailListContainer() {
        if (emailListContainer == null) {
            emailListContainer = (ViewGroup) findViewById(R.id.email_list_container);
        }
        return emailListContainer;
    }

    private ViewGroup getAddressListContainer() {
        if (addressListContainer == null) {
            addressListContainer = (ViewGroup) findViewById(R.id.address_list_container);
        }
        return addressListContainer;
    }

    @Override
    protected void onStop() {
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onStop();
    }
}
