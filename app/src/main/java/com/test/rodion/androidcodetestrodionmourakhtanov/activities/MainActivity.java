package com.test.rodion.androidcodetestrodionmourakhtanov.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.test.rodion.androidcodetestrodionmourakhtanov.ContactSearchFilter;
import com.test.rodion.androidcodetestrodionmourakhtanov.R;
import com.test.rodion.androidcodetestrodionmourakhtanov.adapters.ContactsListAdapter;
import com.test.rodion.androidcodetestrodionmourakhtanov.adapters.ContactsListItemProvider;
import com.test.rodion.androidcodetestrodionmourakhtanov.db.DataProvider;
import com.test.rodion.androidcodetestrodionmourakhtanov.db.DataProviderFactory;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.EntityFactory;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CREATE_CONTACT = 200;
    private static final int REQUEST_VIEW_CONTACT = 201;
    private ContactsListAdapter adapter;
    private TextView favoritesButton;
    private TextView allButton;
    private PopupMenu contactContextPopupMenu;
    private ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create test db
        //createTestDb();

        // Init search bar
        EditText searchBar = (EditText) findViewById(R.id.contacts_search_edit_text);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing
            }

            @Override
            public void afterTextChanged(final Editable s) {
                searchContacts(s.toString());
            }
        });

        // Init "Favorites" button
        favoritesButton = (TextView) findViewById(R.id.contacts_button_favorites);
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoritesButton.setSelected(true);
                allButton.setSelected(false);
                filterFavoriteContacts();
            }
        });

        // Init "All" button
        allButton = (TextView) findViewById(R.id.contacts_button_all);
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoritesButton.setSelected(false);
                allButton.setSelected(true);
                filterAllContacts();
            }
        });
        allButton.setSelected(true);

        // Init contacts list view
        contactsListView = (ListView) findViewById(R.id.contacts_list_view);
        adapter = createContactListAdapter();
        contactsListView.setAdapter(adapter);
        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                contactsListView.setItemChecked(position, true);
                getContactContextPopupMenu(view).show();
                return true;
            }
        });
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onContactListItemClicked(adapter.getItem(position));
            }
        });

        // Init "Create contact" button
        FloatingActionButton buttonCreateContact = (FloatingActionButton) findViewById(R.id.button_create_contact);
        buttonCreateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCreateContactCliced();
            }
        });
    }

    private void onContactListItemClicked(Object item) {
        if (item instanceof Contact) {
            Contact contact = (Contact) item;
            Intent intent = new Intent(this, ContactEditActivity.class);
            intent.putExtra(ContactEditActivity.EXTRA_CONTACT_ID, contact.getId() == null ? 0 :
                    contact.getId());
            intent.putExtra(ContactEditActivity.EXTRA_VIEW_MODE, ContactEditActivity.Mode.VIEW);
            startActivityForResult(intent, REQUEST_VIEW_CONTACT);
        }
    }

    private PopupMenu getContactContextPopupMenu(View anchor) {
        if (contactContextPopupMenu == null) {
            contactContextPopupMenu = new PopupMenu(this, anchor);
            contactContextPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_delete:
                            deleteSelectedContact();
                            return true;
                    }
                    return false;
                }
            });
            contactContextPopupMenu.inflate(R.menu.contact_list_item_context_menu);
        }
        return contactContextPopupMenu;
    }

    private void deleteSelectedContact() {
        int pos = contactsListView.getCheckedItemPosition();
        if (pos == -1) {
            Toast.makeText(this, "Failed to delete contact...", Toast.LENGTH_LONG).show();
            return;
        }
        Contact contact = (Contact) adapter.getItem(pos);
        DataProvider provider = DataProviderFactory.getProviderInstance();
        provider.deleteContact(contact.getId());
        refreshContactListView();
    }

    private void searchContacts(String searchString) {
        adapter.search(searchString);
    }

    private void filterFavoriteContacts() {
        adapter.applyCustomFilter(ContactsListAdapter.FAVORITE_FILTER);
    }

    private void filterAllContacts() {
        adapter.filter(null);
    }

    private ContactsListAdapter createContactListAdapter() {
        ContactsListItemProvider provider = new ContactsListItemProvider(getLayoutInflater(), null, null);
        return new ContactsListAdapter(provider, loadAllContacts(), true,
                new ContactSearchFilter());
    }

    private List<Contact> loadAllContacts() {
        DataProvider provider = DataProviderFactory.getProviderInstance();
        return (List<Contact>) provider.getContacts();
    }

    private void refreshContactListView() {
        if (adapter != null) {
            adapter.setData(loadAllContacts());
        }
    }

    private void onButtonCreateContactCliced() {
        Intent intent = new Intent(this, ContactEditActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CREATE_CONTACT || requestCode == REQUEST_VIEW_CONTACT)
                && resultCode == RESULT_OK) {
            // Refresh contact list if new contact was created an existing one was updated
            refreshContactListView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createTestDb() {
        DataProvider provider = DataProviderFactory.getProviderInstance();

        // Create contact
        EntityFactory factory = provider.getEntityFactory();
        Contact contact = factory.createContact();
        contact.setFirstName("John");
        contact.setLastName("Zack");
        contact.setEmails(Arrays.asList("John-1@gmail.com", "John-2@gmail.com", "John-3@gmail.com"));
        contact.setPhoneNumbers(Arrays.asList("+1-123-343-1122", "+7-913-555-6789"));
        provider.addContact(contact);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
