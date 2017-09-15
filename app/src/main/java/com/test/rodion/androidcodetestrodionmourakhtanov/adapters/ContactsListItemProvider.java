package com.test.rodion.androidcodetestrodionmourakhtanov.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.rodion.androidcodetestrodionmourakhtanov.R;
import com.test.rodion.androidcodetestrodionmourakhtanov.model.Contact;
import com.test.rodion.androidcodetestrodionmourakhtanov.db.DataProvider;
import com.test.rodion.androidcodetestrodionmourakhtanov.db.DataProviderFactory;

import java.util.List;

/**
 * Creates and updates contacts list item views.
 */
public class ContactsListItemProvider implements ListItemProvider<Contact> {

    private final LayoutInflater inflater;
    private final Integer headerTextColor;
    private final Integer headerBackgroundColor;

    public ContactsListItemProvider(@NonNull LayoutInflater inflater,
                                    @Nullable Integer headerTextColor,
                                    @Nullable Integer headerBackgroundColor) {
        this.inflater = inflater;
        this.headerTextColor = headerTextColor;
        this.headerBackgroundColor = headerBackgroundColor;
    }

    /**
     * Creates new item view.
     *
     * @param parent item parent view.
     * @return newly created item view.
     */
    @Override
    public View createItemView(ViewGroup parent) {
        return inflater.inflate(R.layout.contact_list_item, parent, false);
    }

    /**
     * Creates new group header view.
     *
     * @param parent item parent view.
     * @return newly created header view.
     */
    @Override
    public View createHeaderView(ViewGroup parent) {
        return inflater.inflate(R.layout.section_list_header, parent, false);
    }

    /**
     * Updates item view with new data.
     *
     * @param parent      item parent view.
     * @param convertView the item view to update.
     * @param contact     data object for the view.
     */
    @Override
    public void updateItemView(ViewGroup parent, View convertView, final Contact contact) {
        convertView.setTag(contact);

        // Set first and last name
        StringBuilder sb = new StringBuilder();
        String firstName = contact.getFirstName();
        if (firstName != null && !firstName.isEmpty()) sb.append(firstName);
        String lastName = contact.getLastName();
        if (lastName != null && !lastName.isEmpty()) sb.append(" ").append(lastName);
        TextView nameView = convertView.findViewById(R.id.contact_name);
        nameView.setText(sb.toString());

        // Set phone number view to the first number (if any)
        TextView phoneView = convertView.findViewById(R.id.contact_phone);
        List<String> phoneNumbers = contact.getPhoneNumbers();
        String firstNumber = (phoneNumbers != null && phoneNumbers.size() > 0) ?
                phoneNumbers.get(0) : "";
        phoneView.setText(firstNumber);

        // Set email view to the first email (if any)
        TextView emailView = convertView.findViewById(R.id.contact_email);
        List<String> emailList = contact.getEmails();
        String firstEmail = (emailList != null && emailList.size() > 0) ? emailList.get(0) : "";
        emailView.setText(firstEmail);

        // Set favorite icon
        ImageView starView = convertView.findViewById(R.id.contact_favourite_star);
        updateFavouriteStar(starView, contact.isFavorite());
        starView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFavoriteStarClicked(view);
            }
        });
    }

    private void updateFavouriteStar(ImageView starView, boolean favourite) {
        starView.setImageResource(favourite ? R.drawable.button_star_yellow
                : R.drawable.button_star_white);
    }

    private void onFavoriteStarClicked(View view) {
        View parent = (View) view.getParent();
        Contact contact = (Contact) parent.getTag();
        boolean currentFavoriteState = contact.isFavorite();
        contact.setFavorite(!currentFavoriteState);

        // Update person in the data base
        DataProvider provider = DataProviderFactory.getProviderInstance();
        provider.updateContact(contact);

        // Update star icon
        updateFavouriteStar((ImageView) view, contact.isFavorite());
    }

    /**
     * Updates header view with new data.
     *
     * @param parent     item parent view.
     * @param headerView the item view to update.
     * @param headerData data for the header view.
     */
    @Override
    public void updateHeaderView(ViewGroup parent, View headerView, HeaderData<Contact> headerData) {
        String text = "";
        if (headerData instanceof ContactHeaderData) {
            text = ((ContactHeaderData) headerData).getHeaderTitle();
        }

        final TextView textView = headerView.findViewById(R.id.header_text);
        textView.setText(text);

        // Set header text color
        if (headerTextColor != null) {
            textView.setTextColor(headerTextColor);
        }

        // Set header background color
        if (headerBackgroundColor != null) {
            headerView.setBackgroundColor(headerBackgroundColor);
        }
    }
}
