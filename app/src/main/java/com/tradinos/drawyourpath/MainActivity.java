package com.tradinos.drawyourpath;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.tradinos.drawyourpath.Models.MyPath;
import com.tradinos.drawyourpath.Utils.contactManager.Contact;
import com.tradinos.drawyourpath.Utils.contactManager.ContactUtil;
import com.tradinos.drawyourpath.Sources.PathViewModel;
import com.tradinos.drawyourpath.Ui.paths.PathsAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity
        implements PathsAdapter.sendSmsCallback,
        PathsAdapter.deletePathFromDatabaseCallback,
        PathsAdapter.sharePathWithImageCallback{

    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior sheetBehavior;
    public ConstraintLayout bottom_sheet;
    private ImageView bottomSheetArrow;
    private ImageButton showNavigationDrawerButton;
    private DrawerLayout drawer;

    private final int PICK_CONTACT = 99;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 98;
    private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 97;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 96;
    private boolean HAS_PERMISSIONS_READ_CONTACTS = false;
    private boolean HAS_PERMISSIONS_WRITE_EXTERNAL_STORAGE = false;
    private boolean HAS_PERMISSIONS_SEND_SMS = false;

    //TODO: search for another way ...
    MyPath pathToSendInSms = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_paths)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        bottomSheetArrow = bottom_sheet.findViewById(R.id.bottom_sheet_arrow);
        showNavigationDrawerButton = findViewById(R.id.showNavigationDrawer_button);



        setupActions();

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        Contact contact = null;

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        Uri contactURI = data.getData();
                        contact = ContactUtil.fetchAndBuildContact(getApplicationContext(), contactURI);
                        Toast.makeText(this,contact.getGivenName(),Toast.LENGTH_SHORT).show();
                        if(pathToSendInSms!=null)
                            sendSMS(contact.getContactNumber(), pathToSendInSms.toString());
                    }
                }
            break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HAS_PERMISSIONS_READ_CONTACTS = true;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HAS_PERMISSIONS_SEND_SMS = true;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HAS_PERMISSIONS_WRITE_EXTERNAL_STORAGE = true;
                }
                return;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void deletePathAction(MyPath myPath) {
        PathViewModel mPathViewModel = new ViewModelProvider(this).get(PathViewModel.class);
        mPathViewModel.deletePath(myPath);

    }

    @Override
    public void sendSmsAction(MyPath myPath) {

        if(getPermissions()){

            Toast.makeText(this,"You have all permissions", Toast.LENGTH_SHORT).show();
            pathToSendInSms = myPath;
            callContactProvider();



        }

    }

    @Override
    public void sharePathWithImageAction(MyPath myPath) {

        getPermissions();
        if(!HAS_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
            return;

        if(myPath.getImageBase64() != null){
            byte[] decodedString = Base64.decode(myPath.getImageBase64(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            Intent shareIntent;
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Share.png";
            OutputStream out = null;
            File file=new File(path);
            try {
                out = new FileOutputStream(file);
                decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);

            shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            shareIntent.putExtra(Intent.EXTRA_TEXT,myPath.toString());
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent,"Share with"));
        }


    }

    private void setupActions() {

        showNavigationDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // callback for do something
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        bottomSheetArrow.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        bottomSheetArrow.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    private boolean getPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else{
            HAS_PERMISSIONS_READ_CONTACTS = true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            HAS_PERMISSIONS_SEND_SMS = true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            HAS_PERMISSIONS_WRITE_EXTERNAL_STORAGE = true;
        }



        if(HAS_PERMISSIONS_SEND_SMS && HAS_PERMISSIONS_READ_CONTACTS)
            return true;

        return false;
    }

    private void callContactProvider(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);

        } else{
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }

    }

    private void sendSMS(String phoneNo, String msg) {

        Log.d("Send SMS: ","Phone number: " + phoneNo + "\nmessage: " + msg);

        if(HAS_PERMISSIONS_SEND_SMS)
            try {

                new AlertDialog.Builder(this)
                        .setTitle("Send SMS")
                        .setMessage("Are you sure you want to send this path as sms?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                                Toast.makeText(getApplicationContext(), "Message Sent",
                                        Toast.LENGTH_LONG).show();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
    }

}
