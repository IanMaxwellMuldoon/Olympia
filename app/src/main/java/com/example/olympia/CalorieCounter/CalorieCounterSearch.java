package com.example.olympia.CalorieCounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;


import com.example.olympia.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.AsynchronousChannelGroup;


public class CalorieCounterSearch extends AppCompatActivity{
    private static HttpURLConnection connection;
    private String nameSearch;
    private String input;
    private String[] autoList = new String[]{"Chicken", "Sandwich", "Burger"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_counter);

       // new foodSearchNetworkCall().execute();

        //Meal Type Text
        TextView mealType = (TextView) findViewById(R.id.mealType);
        mealType.setText("<Testing>");

        //Search Bar
        AutoCompleteTextView searchBar = (AutoCompleteTextView) findViewById(R.id.autoComplete);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = searchBar.getText().toString();
                new autoSearchNetworkCall().execute();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CalorieCounterSearch.this, android.R.layout.simple_list_item_1, autoList);
                searchBar.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }



    private class foodSearchNetworkCall extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            BufferedReader reader;
            String line;
            StringBuffer responseContent = new StringBuffer();

            try {
                final String URL_PREFIX = "https://api.edamam.com/api/food-database/v2/parser";
                final String API_ID = "?app_id=1ac33da3";
                final String API_KEY = "&app_key=8a5ff0e08e487166b798e56f3ab64627";
                final String INGR = "&ingr=";
                final String URL_SUFFIX = "&nutrition-type=logging";

                String urlstring = URL_PREFIX + API_ID + API_KEY + INGR + nameSearch + URL_SUFFIX;

                URL url = new URL(urlstring);
                connection = (HttpURLConnection) url.openConnection();

                //Request setup
                connection.setRequestMethod("GET");

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();

                if (status > 299) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                }
                parse(responseContent.toString());

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }


    }

    private class autoSearchNetworkCall extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedReader reader;
            String line;
            StringBuffer responseContent = new StringBuffer();
            try {
                final String URL_PREFIX = "https://api.edamam.com/auto-complete";
                final String API_ID = "?app_id=1ac33da3";
                final String API_KEY = "&app_key=8a5ff0e08e487166b798e56f3ab64627";
                final String INGR = "&q=";

                String urlstring = URL_PREFIX + API_ID + API_KEY + INGR + input;

                URL url = new URL(urlstring);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();

                if (status > 299) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                }
                parseAuto(responseContent.toString());
            } catch (
                    ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                connection.disconnect();
            }
            return null;
        }
    }


    public void parseAuto(String responseBody) {
        try {
            JSONArray responseArray = new JSONArray(responseBody);
            autoList = new String[responseArray.length()];
            for (int i = 0; i < responseArray.length(); i++) {
                String recommendation = responseArray.getString(i);
                Log.d("recommend", recommendation);
                autoList[i] = recommendation;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static String parse(String responseBody) {
        try {
            JSONObject responseObject = new JSONObject(responseBody);
            String searchtext = responseObject.getString("text");
            JSONArray foodlist = responseObject.getJSONArray("hints");
            for (int i = 15; i < foodlist.length(); i++) {
                int calories = 0;
                int protein = 0;
                int fat = 0;
                int fiber = 0;
                int cholesterol = 0;
                JSONObject listobject = foodlist.getJSONObject(i);
                JSONObject foodobject = listobject.getJSONObject("food");
                String label = foodobject.getString("label");
                JSONObject nutrients = foodobject.getJSONObject("nutrients");
                if (nutrients.has("ENERC_KCAL")) {
                    calories = nutrients.getInt("ENERC_KCAL");
                }
                if (nutrients.has("PROCNT")) {
                    protein = nutrients.getInt("PROCNT");
                }
                if (nutrients.has("FAT")) {
                    fat = nutrients.getInt("FAT");
                }
                if (nutrients.has("CHOCDF")) {
                    cholesterol = nutrients.getInt("CHOCDF");
                }
                if (nutrients.has("FIBTG")) {
                    fiber = nutrients.getInt("FIBTG");
                }
                Log.d("foodlist", "food: " + label + " Nutrients: calories = " + calories + " protien = " + protein + " fat = " + fat + " cholesterol = " + cholesterol + " fiber = " + fiber);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}





