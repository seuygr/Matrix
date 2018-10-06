package com.laioffer.matrix;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback {
    private MapView mMapView;
    private View mView;
    private GoogleMap mMap;
    private LocationTracker locationTracker;
    private FloatingActionButton fab_report;
    private FloatingActionButton fab_focus;
    private Dialog dialog;
    private RecyclerView mRecyclerView;
    private ReportRecyclerViewAdapter mRecyclerViewAdapter;
    private ViewSwitcher mViewSwitcher;
    private String event_type = null;
    //Event specs
    private ImageView mImageCamera;
    private Button mBackButton;
    private Button mSendButton;
    private EditText mCommentEditText;
    private ImageView mEventTypeImg;
    private TextView mTypeTextView;
    private DatabaseReference database;

    private void setUpEventSpecs(final View dialogView) {
        mImageCamera = (ImageView) dialogView.findViewById(R.id.event_camera_img);
        mBackButton = (Button) dialogView.findViewById(R.id.event_back_button);
        mSendButton = (Button) dialogView.findViewById(R.id.event_send_button);
        mCommentEditText = (EditText) dialogView.findViewById(R.id.event_comment);
        mEventTypeImg = (ImageView)dialogView.findViewById(R.id.event_type_img);
        mTypeTextView = (TextView)dialogView.findViewById(R.id.event_type);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadEvent(Config.username);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewSwitcher.showPrevious();
            }
        });
    }

    public MainFragment() {
        // Required empty public constructor
    }
    // newInstance constructor for creating fragment with arguments
    public static MainFragment newInstance() {
        MainFragment mainFragment = new MainFragment();
        return mainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_main, container,
                false);
        database = FirebaseDatabase.getInstance().getReference();

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final MainFragment fragment = this;
        mMapView = (MapView) mView.findViewById(R.id.event_map_view);
        fab_focus = (FloatingActionButton) mView.findViewById(R.id.focus);
        fab_focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.getMapAsync(fragment);
            }
        });
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();// needed to get the map to display immediately
            mMapView.getMapAsync(this);
        }
        fab_report = (FloatingActionButton)mView.findViewById(R.id.fab);
        fab_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                showDiag();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mMap = googleMap;
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        getActivity(), R.raw.style_json));

        locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        locationTracker.getLatitude();
        LatLng latLng = new LatLng(locationTracker.getLatitude(), locationTracker.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(16)// Sets the zoom
                .bearing(90)           // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        MarkerOptions marker = new MarkerOptions().position(latLng).
                title("You");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.boy));

        // adding marker
        Marker mker = googleMap.addMarker(marker);

    }
    //Animation show dialog
    private void showDiag() {
        final View dialogView = View.inflate(getActivity(),R.layout.dialog,null);
        mViewSwitcher = (ViewSwitcher)dialogView.findViewById(R.id.viewSwitcher);
        dialog = new Dialog(getActivity(),R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                animateDialog(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK){
                    animateDialog(dialogView, false, dialog);
                    return true;
                }
                return false;
            }
        });
        Animation slide_in_left = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_out_right);
        mViewSwitcher.setInAnimation(slide_in_left);
        mViewSwitcher.setOutAnimation(slide_out_right);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setupRecyclerView(dialogView);
        setUpEventSpecs(dialogView);
        dialog.show();
    }

    //Add animation to Floating Action Button
    private void animateDialog(View dialogView, boolean open, final Dialog dialog) {
        final View view = dialogView.findViewById(R.id.dialog);
        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) (fab_report.getX() + (fab_report.getWidth()/2));
        int cy = (int) (fab_report.getY())+ fab_report.getHeight() + 80;

        if(open){
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx,cy, 0, endRadius);
            // view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(500);
            revealAnimator.start();

        } else {
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    // view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(500);
            anim.start();
        }

    }
    //Set up type items
    private void setupRecyclerView(View dialogView) {
        mRecyclerView = dialogView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        List<Item> listItems = new ArrayList<Item>();
        listItems.add(new Item(Config.POLICE, R.drawable.policeman));
        listItems.add(new Item(Config.TRAFFIC, R.drawable.traffic));
        listItems.add(new Item(Config.NO_ENTRY, R.drawable.no_entry));
        listItems.add(new Item(Config.NO_PARKING, R.drawable.no_parking));
        listItems.add(new Item(Config.SECURITY_CAMERA, R.drawable.security_camera));
        listItems.add(new Item(Config.HEADLIGHT, R.drawable.lights));
        listItems.add(new Item(Config.SPEEDING, R.drawable.speeding));
        listItems.add(new Item(Config.CONSTRUCTION, R.drawable.construction));
        listItems.add(new Item(Config.SLIPPERY, R.drawable.slippery));
        mRecyclerViewAdapter = new ReportRecyclerViewAdapter(getActivity(), listItems);
        mRecyclerViewAdapter.setClickListener(new ReportRecyclerViewAdapter
                .OnClickListener() {
            @Override
            public void setItem(String item) {
                event_type = item;
                if(mViewSwitcher != null) {
                    mViewSwitcher.showNext();
                    mTypeTextView.setText(event_type);
                    mEventTypeImg.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), Config.trafficMap.get(event_type)));
                }
            }
        });

        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    //Upload event
    private String uploadEvent(String user_id) {
        TrafficEvent event = new TrafficEvent();

        event.setEvent_type(event_type);
        event.setEvent_description(mCommentEditText.getText().toString());
        event.setEvent_reporter_id(user_id);
        event.setEvent_timestamp(System.currentTimeMillis());
        event.setEvent_latitude(locationTracker.getLatitude());
        event.setEvent_longitude(locationTracker.getLongitude());
        event.setEvent_like_number(0);
        event.setEvent_comment_number(0);

        String key = database.child("events").push().getKey();
        event.setId(key);
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast toast = Toast.makeText(getContext(),
                            "The event is failed, please check your network status.", Toast.LENGTH_SHORT);
                    toast.show();
                    dialog.dismiss();
                } else {
                    Toast toast = Toast.makeText(getContext(), "The event is reported", Toast.LENGTH_SHORT);
                    toast.show();
                    //TODO: update map fragment
                }
            }
        });

        return key;
    }

}
