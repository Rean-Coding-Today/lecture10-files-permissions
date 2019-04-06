package edu.uw.filedemo;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private static final String FILE_NAME = "myFileForLecturing190406.txt";
    
    private EditText textEntry; //save reference for quick access
    private RadioButton externalButton; //save reference for quick access
    private TextView textDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        externalButton = (RadioButton)findViewById(R.id.radio_external);
        textEntry = (EditText)findViewById(R.id.textEntry); //what we're going to save
        textDisplay = findViewById(R.id.txtDisplay);
    }

    public void saveFile(View v){
        Log.v(TAG, "Saving file...");

        if(externalButton.isChecked()){ //external storage
            saveToExternalFile(); //helper method
        }
        else { //internal storage
            saveToInternalFile();
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

    private void saveToInternalFile() {
        FileOutputStream outputStream;
        String fileContents = textEntry.getText().toString();

        if (fileContents != null && fileContents != "") {
            try {
                outputStream = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.d(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
            }
        }
    }



    public void loadFile(View v){
        Log.v(TAG, "Loading file...");
        TextView textDisplay = (TextView)findViewById(R.id.txtDisplay); //what we're going to save
        textDisplay.setText(""); //clear initially

        if(externalButton.isChecked()){ //external storage
            readFileFromExternal();

        }
        else { //internal storage
            readFileFromInternal();
        }
    }

    private void readFileFromExternal() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(FILE_NAME);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer text = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                text.append(line + "\n");
                line = reader.readLine();
            }

            textDisplay.setText(text);
            reader.close();
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    private void readFileFromInternal() {
        try {
            String filePath = this.getFilesDir() + "/" + FILE_NAME ;
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer text = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                text.append(line + "\n");
                line = reader.readLine();
            }

            textDisplay.setText(text);
            reader.close();
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
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
