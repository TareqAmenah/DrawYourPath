package com.tradinos.drawyourpath.contactManager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactUtil {

    public static Contact fetchAndBuildContact(Context ctx, Uri uriContact){
        // Getting cursorLookUpKey because contacts ID may not be correct all the time.
        Cursor cursorLookUpKey = ctx.getContentResolver().query(uriContact, new String[]{ContactsContract.Data.LOOKUP_KEY}, null, null, null);
        Contact contact = null;
        String lookupKey = null;
        if (cursorLookUpKey.moveToFirst()) {
            lookupKey = cursorLookUpKey.getString(cursorLookUpKey.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
            if(null != lookupKey ){
                contact = new Contact();
                contact = buildConactPhoneDetails(lookupKey, ctx, contact);
                contact = buildEmailDetails(lookupKey, ctx, contact);
                contact = buildAddressDetails(lookupKey, ctx, contact);
            }
        }
        cursorLookUpKey.close();
        return contact;
    }


    private static Contact buildConactPhoneDetails(String lookupKey, Context ctx, final Contact contact) {
        ContentResolver contentResolver = ctx.getContentResolver();
        String contactWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] contactWhereParams = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        Cursor cursorPhone = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, contactWhere, contactWhereParams, null);
        if (cursorPhone.getCount() > 0) {
            if (cursorPhone.moveToNext()) {
                if (Integer.parseInt(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String givenName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String familyName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    String middleName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                    int contactType = cursorPhone.getInt(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    String phoneNo = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contact.contactNumber = phoneNo;
                    contact.givenName = givenName;
                    contact.familyName = familyName;
                    contact.middleName = middleName;
                    contact.contactType = contactType;
                }
            }
        }
        cursorPhone.close();
        return contact;
    }


    private static Contact buildEmailDetails(String lookupKey, Context ctx, final Contact contact) {
        ContentResolver contentResolver = ctx.getContentResolver();
        String emailWhere = ContactsContract.Data.LOOKUP_KEY+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] emailWhereParams = new String[]{lookupKey, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
        Cursor emailCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, emailWhere, emailWhereParams, null);
        if (emailCursor.moveToNext()) {
            String emailId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            contact.emailId = emailId;
        }
        emailCursor.close();
        return contact;
    }

    private static Contact buildAddressDetails(String lookupKey, Context ctx, final Contact contact) {
        ContentResolver contentResolver = ctx.getContentResolver();
        String addrWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor addrCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
        if (addrCursor.moveToNext()) {
            String poBox = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
            String street = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            String city = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            String state = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            String postalCode = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            String country = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            String neighborhood = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
            String formattedAddress = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));

            contact.poBox = poBox;
            contact.street = street;
            contact.city = city;
            contact.state = state;
            contact.zipcode = postalCode;
            contact.country = country;
            contact.street = street;
            contact.neighborhood = neighborhood;
            contact.poBox = poBox;
            contact.formattedAddress = formattedAddress;
        }
        addrCursor.close();
        return contact;
    }
}
