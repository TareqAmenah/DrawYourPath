package com.tradinos.drawyourpath.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tradinos.drawyourpath.CustomListViewDialog;
import com.tradinos.drawyourpath.HaversineDistanceUtil;
import com.tradinos.drawyourpath.MyPath;
import com.tradinos.drawyourpath.PathViewModel;
import com.tradinos.drawyourpath.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private static final String MAIN_FOLDER_NAME = "MyMap";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 98;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 97;
    public static final int PICK_IMAGE = 96;
    public static boolean IS_LOCATION_PERMISSION_EXIST = false;
    public static boolean IS_WRITE_EXTERNAL_STORAGE_PERMISSION_EXIST = false;
    public static boolean IS_READ_EXTERNAL_STORAGE_PERMISSION_EXIST = false;
    private static final int DEFAULT_ZOOM = 8;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private String recentScreenshotName = null;
    private boolean isShareScreenshotAvailable = false;
    private boolean isDrawingNow = false;
    CustomListViewDialog customDialog;

    private View bottom_sheet;
    private Button savePathButton;
    private FloatingActionButton newRoutButton;
    private TextView tvDistance;
    private TextView tvDuration;

    private Polyline currentPolyline;

    SearchView mSearchView;
    private PlacesClient placesClient;

    Boolean Is_MAP_Moveable = false; // to detect map is movable
    private List<LatLng> linePointsList = new ArrayList<>();

    private MyPath currentPath;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);


        if(checkLocationPermission())
            IS_LOCATION_PERMISSION_EXIST = true;


        bottom_sheet = getActivity().findViewById(R.id.bottom_sheet);
        savePathButton = bottom_sheet.findViewById(R.id.save_path_button);
        newRoutButton = bottom_sheet.findViewById(R.id.draw_rout_button);
        tvDistance = bottom_sheet.findViewById(R.id.distance_textview);
        tvDuration = bottom_sheet.findViewById(R.id.duration_textview);


        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                googleMap.setPadding(0,16,0,150);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                //Add my location keys to th UI and enable it
                if(IS_LOCATION_PERMISSION_EXIST){
                    googleMap.setMyLocationEnabled(true);

                }



                //Add zoom control keys to th UI
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                //Add compass to th UI
                googleMap.getUiSettings().setCompassEnabled(true);


                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(
                                new LatLng(-35.016, 143.321),
                                new LatLng(-34.747, 145.592),
                                new LatLng(-34.364, 147.891),
                                new LatLng(-33.501, 150.217),
                                new LatLng(-32.306, 149.248),
                                new LatLng(-32.491, 147.309)));
                polyline1.setTag("A");


                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(33.510414, 36.278336))
                        .radius(10000)
                        .strokeColor(Color.RED)
                        .fillColor(0x20000050));


                // Add a marker in Damascus and move the camera
                LatLng Damascus = new LatLng(33.510414, 36.278336);
                googleMap.addMarker(new MarkerOptions().position(Damascus).title("Marker in Damascus"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Damascus.latitude,Damascus.longitude),DEFAULT_ZOOM));
            }
        });

        FrameLayout fram_map =  rootView.findViewById(R.id.fram_map);

        if(Is_MAP_Moveable)
            newRoutButton.setImageResource(R.drawable.ic_open_with_black_24dp);
        else  newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);


        newRoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Is_MAP_Moveable)
                     newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);
                else newRoutButton.setImageResource(R.drawable.ic_open_with_black_24dp);

                if(googleMap!=null)
                    googleMap.getUiSettings().setScrollGesturesEnabled(Is_MAP_Moveable);

                Is_MAP_Moveable = !Is_MAP_Moveable;

            }
        });


        savePathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPath == null){
                    Toast.makeText(getActivity(),"No path found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                PathViewModel mPathViewModel = new ViewModelProvider(requireActivity()).get(PathViewModel.class);
                mPathViewModel.insertPath(currentPath);

                Toast.makeText(getActivity(),"New Path was Saved!", Toast.LENGTH_SHORT).show();



            }
        });


        fram_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(v.getId() == R.id.bottom_sheet_arrow)
                    return false;

                float x = event.getX();
                float y = event.getY();

                int x_co = Math.round(x);
                int y_co = Math.round(y);

                Projection projection = googleMap.getProjection();
                Point x_y_points = new Point(x_co, y_co);

                LatLng latLng = googleMap.getProjection().fromScreenLocation(x_y_points);
                double latitude = latLng.latitude;

                double longitude = latLng.longitude;

                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN:
                        isDrawingNow = true;
                        // finger touches the screen
                        if(Is_MAP_Moveable){
                            googleMap.clear();
                            linePointsList = new ArrayList<>();
                        }
                        linePointsList.add(new LatLng(latitude, longitude));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // finger moves on the screen
                        linePointsList.add(new LatLng(latitude, longitude));
                        DrawOnMap();
                        break;
                    case MotionEvent.ACTION_UP:
                        isDrawingNow = false;
                        DrawOnMap();
                        break;
                }

                return Is_MAP_Moveable;

            }
        });

        return rootView;

    }


    public boolean checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        } else {
            return true;
        }
    }
    public void takeScreenshot(View view) {

        Log.v("************","Screenshot button clicked");

        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        if(checkWriteExternalStoragePermission()){
            captureScreen();
            Toast.makeText(getActivity(),"Screenhot",Toast.LENGTH_LONG).show();

        }else
            Toast.makeText(getActivity() ,"There is no permission", Toast.LENGTH_SHORT).show();

    }

    public void captureScreen() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot)
            {
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                // image naming and path  to include sd card  appending name you choose for file
                String imageName = now + ".jpg";
                String directory = Environment.getExternalStorageDirectory().toString() +  "/" + MAIN_FOLDER_NAME;
                File directoryFile = new File(directory);
                if(!directoryFile.exists())
                    directoryFile.mkdirs();

                String mPath =   directory + "/" +imageName;

                OutputStream fout = null;

                try {
                    File imageFile = new File(mPath);

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    snapshot.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    recentScreenshotName = imageName;

                    Log.v("************",mPath);
                }
                catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "FileNotFoundException");
                    Log.d("ImageCapture", e.getMessage());
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "IOException");
                    Log.d("ImageCapture", e.getMessage());
                }

            }
        };

        googleMap.snapshot(callback);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    public void DrawOnMap() {




        if(!isDrawingNow){
            googleMap.clear();
            reduceLinePointsListSize();
            Toast.makeText(getActivity(), "Point number: " + linePointsList.size(), Toast.LENGTH_SHORT).show();

            for (LatLng latLng : linePointsList) {
                Log.d("lat: "+latLng.latitude, "lon: "+latLng.longitude);
            }

        }


        if(linePointsList.size() == 1){
            googleMap.addMarker(new MarkerOptions().position(linePointsList.get(0)));
        }else{



            PolylineOptions rectOptions = new PolylineOptions();
            rectOptions.addAll(linePointsList);
            rectOptions.geodesic(false);
            currentPolyline = googleMap.addPolyline(rectOptions);

            if(!isDrawingNow){
                googleMap.addMarker(new MarkerOptions().position(linePointsList.get(0)));
                googleMap.addMarker(new MarkerOptions().position(linePointsList.get(linePointsList.size()-1)));


                Double distance = 0.0;
                for (int i = 0; i < linePointsList.size()-1; i++) {
                    distance += HaversineDistanceUtil.betwenToePoint(linePointsList.get(i), linePointsList.get(i+1));
                }

                DecimalFormat df1 = new DecimalFormat("0.00000");
                Log.d("********", "Distance: " + df1.format(distance) + " km");
                DecimalFormat df = new DecimalFormat("0.00");


                tvDistance.setText(df.format(distance) + " km");

                Toast.makeText(getActivity(), "Distance:  " + df.format(distance) + " km", Toast.LENGTH_SHORT).show();

                newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);

                if(googleMap!=null)
                    googleMap.getUiSettings().setScrollGesturesEnabled(Is_MAP_Moveable);

                Is_MAP_Moveable = !Is_MAP_Moveable;


                //TODO: select start and end of path
                currentPath = new MyPath("start",
                        "End",
                        df.format(distance) + " km",
                        "----");

                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }





    }

    private void reduceLinePointsListSize() {

        List<LatLng> reducedLinePointsList = new ArrayList<>();

        for (int i=0; i<linePointsList.size(); i+=6){
            reducedLinePointsList.add(linePointsList.get(i));
        }
        reducedLinePointsList.add(linePointsList.get(linePointsList.size()-1));

        linePointsList = reducedLinePointsList;
        Log.d("*********", "linePointsList size after reducing = " + linePointsList.size());

    }





    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        bottom_sheet.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }




}
