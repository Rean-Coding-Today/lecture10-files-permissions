package edu.uw.filedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "Photo";

    private static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        //action bar "back"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void takePicture(View v){
        Log.v(TAG, "Taking picture...");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            ImageView imageView = (ImageView)findViewById(R.id.imgThumbnail);
            imageView.setImageBitmap(imageBitmap);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sharePicture(View v){
        Log.v(TAG, "Sharing picture...");

//        Intent chooser = Intent.createChooser(shareIntent, "Share Picture");
//        //check that there is at least one option
//        if (shareIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(chooser);
//        }
    }
}
