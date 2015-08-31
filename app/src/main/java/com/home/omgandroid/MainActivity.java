package com.home.omgandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    JSONAdapter mJSONAdapter;
    ArrayList mNameList = new ArrayList();
    ShareActionProvider mShareActionProvider;
    private static final String PREFS = "prefs";
    private static final String PREF_NAME="name";
    private static final String QUERY_URL="http://openlibrary.org/search.json?q=";
    SharedPreferences mSharedPreferences;
    ProgressDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTextView = (TextView)findViewById(R.id.main_TextView);
        mainButton = (Button) findViewById(R.id.main_Button);
        mainButton.setOnClickListener(this);
        mainEditText = (EditText) findViewById(R.id.main_edittext);
        mainListView = (ListView) findViewById(R.id.main_list);
        //Create an Array Adapter for ListView
        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);
        mainListView.setOnItemClickListener(this);
        displayWelcome();
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching For Books");
        mDialog.setCancelable(false);
    }

    public void displayWelcome(){
        mSharedPreferences = getSharedPreferences(PREFS,MODE_PRIVATE);
        String name = mSharedPreferences.getString(PREF_NAME,"");
        if(name.length()>0){
            Toast.makeText(this, "Welcome back, " +name+"!", Toast.LENGTH_LONG).show();
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello");
            alert.setMessage("What is your name ?");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputName = input.getText().toString();
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();
                    Toast.makeText(getApplicationContext(), "Welcom " + inputName + "!", Toast.LENGTH_LONG).show();

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        if(shareItem!=null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setShareIntent();
        return true;
    }

    private void setShareIntent(){
        if(mShareActionProvider!=null){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Developemnt");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
    @Override
    public void onClick(View v) {
        /*mainTextView.setText(mainEditText.getText().toString()+"is learning Android dev");
        mNameList.add(mainEditText.getText().toString());
        mArrayAdapter.notifyDataSetChanged();
        setShareIntent();*/
        queryBook(mainEditText.getText().toString());
        mainEditText.setText("");
    }
    private void queryBook(String searchString){
        String urlString="";
        try{
            urlString= URLEncoder.encode(searchString,"UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            Toast.makeText(this,"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        mDialog.show();
        client.get(QUERY_URL+urlString,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject jsonObject){
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Success !",Toast.LENGTH_LONG).show();
                mJSONAdapter.updateDate(jsonObject.optJSONArray("docs"));
            }
            @Override
            public  void  onFailure(int statusCode, Throwable throwable, JSONObject error){
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                // Log error message
                // to help solve any problems
                Log.e("omg android", statusCode + " " + throwable.getMessage());
            }

        });
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Log the item position and contents
        JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
        String coverID = jsonObject.optString("cover_i","");
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra("coverID",coverID);
        startActivity(detailIntent);
    }
}
