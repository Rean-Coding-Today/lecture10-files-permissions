package edu.uw.filedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private static final String FILE_NAME = "myFile.txt";
    
    private EditText textEntry; //save reference for quick access
    private RadioButton externalButton; //save reference for quick access

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        externalButton = (RadioButton)findViewById(R.id.radio_external);
        textEntry = (EditText)findViewById(R.id.textEntry); //what we're going to save
    }

    public void saveFile(View v){
        Log.v(TAG, "Saving file...");

        if(externalButton.isChecked()){ //external storage

            saveToExternalFile(); //helper method

        }
        else { //internal storage


        }
    }

    //actually write to the file
    private void saveToExternalFile(){
        try {
            //saving in public Documents directory
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!dir.exists()) { dir.mkdirs(); } //make dir if doesn't otherwise exist
            File file = new File(dir, FILE_NAME);
            Log.v(TAG, "Saving to  " + file.getAbsolutePath());

            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.println(textEntry.getText().toString());
            out.close();
        } catch (IOException ioe) {
            Log.d(TAG, Log.getStackTraceString(ioe));
        }
    }



    public void loadFile(View v){
        Log.v(TAG, "Loading file...");
        TextView textDisplay = (TextView)findViewById(R.id.txtDisplay); //what we're going to save
        textDisplay.setText(""); //clear initially

        if(externalButton.isChecked()){ //external storage

        }
        else { //internal storage

        }
    }


    public void shareFile(View v) {
        Log.v(TAG, "Sharing file...");

        Uri fileUri = null;
        if(externalButton.isChecked()){ //external storage

        }
        else { //internal storage

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_photo:
                startActivity(new Intent(MainActivity.this, PhotoActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
