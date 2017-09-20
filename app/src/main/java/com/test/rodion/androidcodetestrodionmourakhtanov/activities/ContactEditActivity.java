package com.test.rodion.androidcodetestrodionmourakhtanov.activities;

import android.app.DatePickerDialog;
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
import java.util.List;
import java.util.Locale;

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

    private static final int MAX_NUMOF_PHONE_NUMBERS = 10;
    private static final int MAX_NUMOF_EMAILS = 10;
    private static final int MAX_NUMOF_ADDRESSES = 10;

    private EditText birthDateView;
    private TextView birthDateLabel;
    private Mode mode;
    private Contact contact;
    private List<View> phoneNumberViewsList = new ArrayList<>();
    private ViewGroup phoneNumberListContainer;
    private List<View> emailViewsList = new ArrayList<>();
    private ViewGroup emailListContainer;
    private List<View> addressViewsList = new ArrayList<>();
    private ViewGroup addressListContainer;
    private Calendar birthDateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        long contactId;
        if (savedInstanceState != null) {
            contactId = savedInstanceState.getLong(EXTRA_CONTACT_ID, 0);
            mode = (Mode) savedInstanceState.getSerializable(EXTRA_VIEW_MODE);
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
        addPhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddPhoneClicked();
            }
        });

        // Init "Add email" button
        View addEmailButton = findViewById(R.id.button_add_email);
        addEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddEmailClicked();
            }
        });

        // Init "Add address" button
        View addAddressButton = findViewById(R.id.button_add_address);
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
//            firstNameEdit.setHint(mode == Mode.VIEW && (firstName == null || firstName.isEmpty()) ?
//                    "" : getString(R.string.contacts_edit_last_name_hint));

            // Set last name
            EditText lastNameEdit = (EditText) findViewById(R.id.last_name);
            lastNameEdit.setFocusable(mode == Mode.EDIT);
            lastNameEdit.setFocusableInTouchMode(mode == Mode.EDIT);
            final String lastName = contact.getLastName();
            if (lastName != null) {
                lastNameEdit.setText(contact.getLastName());
            }
            lastNameEdit.setEnabled(!(mode == Mode.VIEW && (lastName == null || lastName.isEmpty())));
//            lastNameEdit.setHint(mode == Mode.VIEW && (lastName == null || lastName.isEmpty()) ?
//                    "" : getString(R.string.contacts_edit_last_name_hint));

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
                    View phoneNumberView = addPhoneNumberLayout();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        EditText phoneNumberEdit = phoneNumberView.findViewById(R.id.phone_number);
                        phoneNumberEdit.setText(phoneNumber);
                        phoneNumberEdit.setFocusable(mode == Mode.EDIT);
                        phoneNumberEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            } else if (mode == Mode.EDIT) {
                // Add one empty phone number edit view
                addPhoneNumberLayout();
            }
            addPhoneNumberButton.setVisibility(mode == Mode.EDIT ? View.VISIBLE : View.GONE);

            // Set emails
            List<String> emails = contact.getEmails();
            if (emails != null && !emails.isEmpty()) {
                for (String email : emails) {
                    View emailView = addEmailLayout();
                    if (email != null && !email.isEmpty()) {
                        EditText emailEdit = emailView.findViewById(R.id.email);
                        emailEdit.setText(email);
                        emailEdit.setFocusable(mode == Mode.EDIT);
                        emailEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            } else if (mode == Mode.EDIT) {
                // Add one empty email edit view
                addEmailLayout();
            }
            addEmailButton.setVisibility(mode == Mode.EDIT ? View.VISIBLE : View.GONE);

            // Set addresses
            List<String> addresses = contact.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                for (String address : addresses) {
                    View addressView = addAddressLayout();
                    if (address != null && !address.isEmpty()) {
                        EditText addressEdit = addressView.findViewById(R.id.address);
                        addressEdit.setText(address);
                        addressEdit.setFocusable(mode == Mode.EDIT);
                        addressEdit.setFocusableInTouchMode(mode == Mode.EDIT);
                    }
                }
            } else if (mode == Mode.EDIT) {
                // Add one empty email edit view
                addAddressLayout();
            }
            addAddressButton.setVisibility(mode == Mode.EDIT ? View.VISIBLE : View.GONE);
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

    private View addPhoneNumberLayout() {
        ViewGroup parent = getPhoneNumberListContainer();
        View phoneNumberLayout = getLayoutInflater().inflate(R.layout.phone_number_list_item,
                parent, false);
        parent.addView(phoneNumberLayout);
        phoneNumberViewsList.add(phoneNumberLayout);
        TextView phoneNumberLabelView = phoneNumberLayout.findViewById(R.id.phone_number_label);
        phoneNumberLabelView.setText(getString(R.string.contacts_edit_phone_number_label,
                phoneNumberViewsList.size()));
        if (phoneNumberViewsList.size() >= MAX_NUMOF_PHONE_NUMBERS) {
            findViewById(R.id.button_add_phone_number).setVisibility(View.GONE);
        }
        phoneNumberLayout.findViewById(R.id.phone_number).requestFocus();

        return phoneNumberLayout;
    }

    private View addEmailLayout() {
        ViewGroup parent = getEmailListContainer();
        View emailLayout = getLayoutInflater().inflate(R.layout.email_list_item, parent, false);
        parent.addView(emailLayout);
        emailViewsList.add(emailLayout);
        TextView emailLabelView = emailLayout.findViewById(R.id.email_label);
        emailLabelView.setText(getString(R.string.contacts_edit_email_label, emailViewsList.size()));
        if (emailViewsList.size() >= MAX_NUMOF_EMAILS) {
            findViewById(R.id.button_add_email).setVisibility(View.GONE);
        }
        findViewById(R.id.email).requestFocus();

        return emailLayout;
    }

    private View addAddressLayout() {
        ViewGroup parent = getAddressListContainer();
        View addressLayout = getLayoutInflater().inflate(R.layout.address_list_item, parent, false);
        parent.addView(addressLayout);
        addressViewsList.add(addressLayout);
        TextView addressLabelView = addressLayout.findViewById(R.id.address_label);
        addressLabelView.setText(getString(R.string.contacts_edit_address_label, addressViewsList.size()));
        if (addressViewsList.size() >= MAX_NUMOF_ADDRESSES) {
            findViewById(R.id.button_add_address).setVisibility(View.GONE);
        }
        addressLayout.findViewById(R.id.address).requestFocus();

        return addressLayout;
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
        for (View view : phoneNumberViewsList) {
            String phoneString = ((TextView) view.findViewById(R.id.phone_number))
                    .getText().toString().trim();
            if (!phoneString.isEmpty()) {
                phoneList.add(phoneString);
            }
        }
        contact.setPhoneNumbers(phoneList);

        // Set emails
        List<String> emailList = new ArrayList<>();
        for (View view : emailViewsList) {
            String emailString = ((TextView) view.findViewById(R.id.email))
                    .getText().toString().trim();
            if (!emailString.isEmpty()) {
                emailList.add(emailString);
            }
        }
        contact.setEmails(emailList);

        // Set addresses
        List<String> addressList = new ArrayList<>();
        for (View view : addressViewsList) {
            String addressString = ((TextView) view.findViewById(R.id.address))
                    .getText().toString().trim();
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

    private void onButtonCancelClicked() {
        finish();
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
}
