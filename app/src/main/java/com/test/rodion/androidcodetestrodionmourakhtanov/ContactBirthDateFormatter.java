package com.test.rodion.androidcodetestrodionmourakhtanov;

import android.support.annotation.Nullable;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class that provides unified date formatting methods.
 */
public class ContactBirthDateFormatter {

    private static Format formatter = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());

    public static String toDateString(@Nullable Date date) {
        return date == null ? null : formatter.format(date);
    }
}
