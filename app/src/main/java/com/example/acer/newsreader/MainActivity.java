package com.example.acer.newsreader;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    TextView title;
    ImageView img;
    Button next, prev;

    int flag = 0;

    ArrayList<String> storyTitle = new ArrayList<>(); // index : 1
    ArrayList<String> storyImage = new ArrayList<>(); // index : 2
    ArrayList<String> storyUrl = new ArrayList<>(); // index :
    ArrayList<Bitmap> images = new ArrayList<>();
    String page ="";
    String apiLink ="https://newsapi.org/v2/top-headlines?country=in&category=technology&apiKey=45387f797799484fa48ba05d9f86ef1a";

    JSONObject jsonObject;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.textView);
        img = findViewById(R.id.imageView);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);

        DownloadTask task = new DownloadTask();

        try {
            page = task.execute(apiLink).get();
            Log.i("Page source test", page);
        }
        catch (Exception e){
            e.printStackTrace();
            Log.i("Exception", "Something went wrong");
        }

        try {
            jsonObject = new JSONObject(page);
            jsonArray = jsonObject.getJSONArray("articles");
            Log.i("Test trial", jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GetTitleToList(); //adding titles to the list
        GetImageLinkToList(); //adding image links to list
        GetURLToList(); //adding redirect links to list

        title.setText(storyTitle.get(flag));
        SetThisImage(flag);

    }



    public static class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            String result = "";
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader read = new InputStreamReader(in);
                int data = read.read();
                while(data!=-1){
                    char current = (char)data;
                    result += current;
                    data = read.read();
                }
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            URL url ;
            Bitmap bitmap;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    void SetThisImage(int imgNo){
        DownloadImage downloadImage = new DownloadImage();
        if(storyImage.get(imgNo).equals("Invalid image link")){
            Bitmap bitmap = Bitmap.createBitmap(img.getWidth(),img.getHeight(), Bitmap.Config.RGB_565);
            img.setImageBitmap(bitmap);
            return;
        }
        try {
            Bitmap b = downloadImage.execute(storyImage.get(imgNo)).get();
            img.setImageBitmap(b);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    void GetImageLinkToList(){
        for(int i = 0; i<jsonArray.length(); i++){
            try {
                String s = jsonArray.getJSONObject(i).getString("urlToImage");
                if(s == null){
                    s = "Invalid image link";
                }
                storyImage.add(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Image link test", storyImage.toString());
    }

    void GetTitleToList(){
        for(int i = 0; i<jsonArray.length(); i++){
            try {
               String s = jsonArray.getJSONObject(i).getString("title");
               storyTitle.add(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Title test", storyTitle.toString());
    }


    void GetURLToList(){
        for(int i = 0; i<jsonArray.length(); i++){
            try {
                String s = jsonArray.getJSONObject(i).getString("url");
                storyUrl.add(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i("URL link test", storyUrl.toString());
    }


    public void GoNext(View view){
        flag++;
        if(flag < storyTitle.size()){
            title.setText(storyTitle.get(flag));
            SetThisImage(flag);
        }
        else{
            Toast.makeText(this, "All the top stories loaded", Toast.LENGTH_SHORT).show();
        }
    }

    public void GoPrevious(View view){
        flag--;
        if(flag>0){
            title.setText(storyTitle.get(flag));
            SetThisImage(flag);
        }
        else{
            Toast.makeText(this, "This is the start line", Toast.LENGTH_SHORT).show();
        }
    }

    public void RedirectToSite(View view){
        Intent i = new Intent(MainActivity.this, NextPage.class);
        i.putExtra("siteIndex",storyUrl.get(flag));
        startActivity(i);
    }





}
