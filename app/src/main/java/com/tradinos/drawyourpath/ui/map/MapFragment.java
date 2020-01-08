package com.tradinos.drawyourpath.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tradinos.drawyourpath.Models.MyPath;
import com.tradinos.drawyourpath.R;
import com.tradinos.drawyourpath.Utils.HaversineDistanceUtil;
import com.tradinos.drawyourpath.sources.PathViewModel;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static boolean IS_LOCATION_PERMISSION_EXIST = false;
    private static final int DEFAULT_ZOOM = 12;
    private boolean isDrawingNow = false;
    private Boolean isMapMovable = false; // to detect map is movable

    private List<LatLng> linePointsList = new ArrayList<>();

    private MyPath currentPath;

    private View bottom_sheet;
    private Button savePathButton;
    private FloatingActionButton newRoutButton;
    private TextView tvDistance;
    private TextView tvDuration;
    private FrameLayout FrameMap;

    private Polyline currentPolyline;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);


        if(checkLocationPermission())
            IS_LOCATION_PERMISSION_EXIST = true;


        setupUiElements(rootView);

        setupMapView(savedInstanceState);

        if(isMapMovable)
            newRoutButton.setImageResource(R.drawable.ic_open_with_black_24dp);
        else  newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);




        setupActions();

         return rootView;

    }

    private void setupUiElements(View rootView) {
        bottom_sheet = getActivity().findViewById(R.id.bottom_sheet);
        savePathButton = bottom_sheet.findViewById(R.id.save_path_button);
        newRoutButton = bottom_sheet.findViewById(R.id.draw_rout_button);
        tvDistance = bottom_sheet.findViewById(R.id.distance_textview);
        tvDuration = bottom_sheet.findViewById(R.id.duration_textview);
        FrameMap =  rootView.findViewById(R.id.fram_map);
        mMapView = rootView.findViewById(R.id.map);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupActions() {
        newRoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(googleMap!=null)
                    googleMap.getUiSettings().setScrollGesturesEnabled(isMapMovable);

                isMapMovable = !isMapMovable;

                if(isMapMovable)
                    newRoutButton.setImageResource(R.drawable.ic_open_with_black_24dp);
                else  newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);



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

        FrameMap.setOnTouchListener(new View.OnTouchListener() {
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
                        if(isMapMovable){
                            googleMap.clear();
                            linePointsList = new ArrayList<>();
                        }
                        linePointsList.add(new LatLng(latitude, longitude));
                        break;

                    case MotionEvent.ACTION_MOVE:
                        linePointsList.add(new LatLng(latitude, longitude));
                        DrawOnMap();
                        break;

                    case MotionEvent.ACTION_UP:
                        isDrawingNow = false;
                        DrawOnMap();
                        break;
                }
                return isMapMovable;
            }
        });

    }

    private void setupMapView(Bundle savedInstanceState) {

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

                googleMap.setPadding(0,16,0,200);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                //Add my location keys to th UI and enable it
                if(IS_LOCATION_PERMISSION_EXIST){
                    googleMap.setMyLocationEnabled(true);
                }

                //Add zoom control keys to th UI
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                //Add compass to th UI
                googleMap.getUiSettings().setCompassEnabled(true);

                // Add a marker in Damascus and move the camera
                LatLng Damascus = new LatLng(33.510414, 36.278336);
                googleMap.addMarker(new MarkerOptions().position(Damascus).title("Marker in Damascus"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Damascus.latitude,Damascus.longitude),DEFAULT_ZOOM));
            }
        });

    }

    private boolean checkLocationPermission() {
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

    private void DrawOnMap() {

        if(linePointsList.size() == 1){
            googleMap.addMarker(new MarkerOptions().position(linePointsList.get(0)));
        }else{

            if(!isDrawingNow){
                googleMap.clear();
                reduceLinePointsListSize();

                googleMap.addMarker(new MarkerOptions().position(linePointsList.get(0)));
                googleMap.addMarker(new MarkerOptions().position(linePointsList.get(linePointsList.size()-1)));


                double distance = 0.0;
                double duration = 0.0;
                for (int i = 0; i < linePointsList.size()-1; i++) {
                    distance += HaversineDistanceUtil.betwenToePoint(linePointsList.get(i), linePointsList.get(i+1));
                }
                duration = distance/40*60;

                DecimalFormat df = new DecimalFormat("0.00");
                tvDistance.setText(df.format(distance) + " km");


                newRoutButton.setImageResource(R.drawable.ic_edit_mode_black_24dp);

                if(googleMap!=null)
                    googleMap.getUiSettings().setScrollGesturesEnabled(isMapMovable);

                isMapMovable = !isMapMovable;


                final String[] encodedImage = new String[1];
                Double finalDistance = distance;


                googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {

                    @Override
                    public void onSnapshotReady(Bitmap snapshot)
                    {

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        snapshot.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bitmapdata = stream.toByteArray();

                        encodedImage[0] = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                        currentPath = new MyPath("start",
                                "End",
                                Double.valueOf(df.format(finalDistance)),
                                "----",
                                encodedImage[0]);
                    }
                });


                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            PolylineOptions rectOptions = new PolylineOptions();
            rectOptions.addAll(linePointsList);
            rectOptions.geodesic(false);
            currentPolyline = googleMap.addPolyline(rectOptions);

        }
    }

    private void reduceLinePointsListSize() {

        List<LatLng> reducedLinePointsList = new ArrayList<>();
        for (int i=0; i<linePointsList.size(); i+=6){
            reducedLinePointsList.add(linePointsList.get(i));
        }
        reducedLinePointsList.add(linePointsList.get(linePointsList.size()-1));
        linePointsList = reducedLinePointsList;
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
