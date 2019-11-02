package com.tony.collage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrinterId;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MAIN_ACTIVITY";
    private ImageButton mImageButton1, mImageButton2, mImageButton3, mImageButton4;

    private List<ImageButton> mImageButtons;
    private ArrayList<String> mImageFilePaths;

    private String mCurrentImagePath;

    private static final String BUNDLE_KEY_MOST_RECENT_FILE_PATH = "bundle key most recent path";
    private final static String BUNDLE_KEY_IMAGE_FILE_PATHS = "bundle key image file paths";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageButton1 = findViewById(R.id.imageButton1);
        mImageButton2 = findViewById(R.id.imageButton2);
        mImageButton3 = findViewById(R.id.imageButton3);
        mImageButton4 = findViewById(R.id.imageButton4);

        mImageButtons = new ArrayList<>(Arrays.asList(mImageButton1, mImageButton2, mImageButton3, mImageButton4));

        for (ImageButton button: mImageButtons) {
            button.setOnClickListener(this);
        }

        if (savedInstanceState != null) {
            mCurrentImagePath = savedInstanceState.getString(BUNDLE_KEY_MOST_RECENT_FILE_PATH);
            mImageFilePaths = savedInstanceState.getStringArrayList(BUNDLE_KEY_IMAGE_FILE_PATHS);

        }

        if (mCurrentImagePath == null) {
            mCurrentImagePath = "";
        }
        if (mImageFilePaths == null) {
            mImageFilePaths = new ArrayList<>(Arrays.asList("", "", "", ""));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        super.onSaveInstanceState(outBundle);
        outBundle.putString(BUNDLE_KEY_MOST_RECENT_FILE_PATH, mCurrentImagePath);
        outBundle.putStringArrayList(BUNDLE_KEY_IMAGE_FILE_PATHS, mImageFilePaths);
    }

    @Override
    public void onClick(View view) {

        int requestCodeButtonIndex = mImageButtons.indexOf(view);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null){ // if theres a camera
            try {
                File imageFile = createImageFile();
                if (imageFile != null) {
                    Uri imageURI = FileProvider.getUriForFile(this, "com.tony.collage.fileprovider", imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(takePictureIntent, requestCodeButtonIndex);
                }else{
                    Log.e(TAG, "Image file is null");
                }
            }catch (IOException e) {
                Log.e(TAG, "Error creating image file " + e);
            }
        }
    }

    private File createImageFile() throws IOException {
        //create unique filename with timestamp
        String imageFileName = "COLLAGE_" + new Date().getTime();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        //save file path globally, when the take picture intent returns
        //this location will be where the image is saved
        mCurrentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult for request code " + requestCode +
                    " and current path " + mCurrentImagePath
            );
            mImageFilePaths.set(requestCode, mCurrentImagePath);
        }
        else if (resultCode == RESULT_CANCELED) {
            mCurrentImagePath = "";
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        Log.d(TAG, "focus changed " + hasFocus);
        if (hasFocus) {
            for (int index = 0; index < mImageButtons.size() ; index ++) {
                loadImage(index);
            }
        }
    }
    private void loadImage(int index) {

        ImageButton imageButton = mImageButtons.get(index);
        String path = mImageFilePaths.get(index);

        if (mCurrentImagePath != null && !mCurrentImagePath.isEmpty()) {
            Picasso.get()
                    .load(new File(path))
                    .error(android.R.drawable.stat_notify_error) // built in error iron
                    .fit()
                    .centerCrop()
                    .into(imageButton);
        }

    }

}
