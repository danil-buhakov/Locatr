package com.book.dan.locatr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.List;

public class LocatrFragment extends Fragment implements ExplanationDialogFragment.iAccept {
    private static final String TAG = "LocatrFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_LOCATE_PERMISSIONS=0;

    private ImageView mImageView;
    private GoogleApiClient mClient;

    private ProgressDialog mProgressDialog;

    public static LocatrFragment newInstance(){
        return new LocatrFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("Wait while loading");
        mProgressDialog.setIndeterminate(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr,menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_LOCATE_PERMISSIONS:
                if(hasLocatePermissions())
                    findImage();
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_locate:
                if(hasLocatePermissions())
                    findImage();
                else
                    if(shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS[0])){
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        ExplanationDialogFragment fragment = new ExplanationDialogFragment();
                        fragment.setIAccept(this);
                        fragment.show(fm,TAG);
                    }
                    else
                        requestPermissions(LOCATION_PERMISSIONS,REQUEST_LOCATE_PERMISSIONS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_locatr,container, false);
        mImageView = v.findViewById(R.id.image);
        return v;
    }

    @SuppressLint("MissingPermission")
    private void findImage(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient,locationRequest,new LocationListener(){
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG,"Got a fix: "+location);
                        new SearchTask().execute(location);
                        mProgressDialog.show();
                    }
                });
    }

    private boolean hasLocatePermissions(){
        int result = ContextCompat.checkSelfPermission(getActivity(),LOCATION_PERMISSIONS[0]);
        return result== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void accept() {
        requestPermissions(LOCATION_PERMISSIONS,REQUEST_LOCATE_PERMISSIONS);
    }

    private class SearchTask extends AsyncTask<Location,Void,Void>{
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;

        @Override
        protected Void doInBackground(Location... locations) {
            FlickrFetchr flickrFetchr = new FlickrFetchr();
            List<GalleryItem> items = flickrFetchr.searchPhotos(locations[0]);

            if(items.size()==0){
                return null;
            }

            mGalleryItem = items.get(0);

            try {
                byte[] bytes = flickrFetchr.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            } catch (IOException ioe) {
                Log.e(TAG,"Failed downloading bitmap",ioe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mImageView.setImageBitmap(mBitmap);
        }
    }
}
