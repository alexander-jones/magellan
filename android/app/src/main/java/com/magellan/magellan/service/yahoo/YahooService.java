package com.magellan.magellan.service.yahoo;

import android.util.Log;

import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.equity.IEquityService;
import com.magellan.magellan.equity.EquityQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YahooService implements IEquityService {

    public List<Equity> execute(EquityQuery equityQuery) {

        String url_select = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + equityQuery.symbolTemplate + "&region=1&lang=en";

        List<Equity> ret = null;
        try {
            URL url = new URL(url_select);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                try {
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                    StringBuilder sBuilder = new StringBuilder();

                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        sBuilder.append(line + "\n");
                    }

                    inputStream.close();
                    try {
                        String response = sBuilder.toString();
                        JSONObject rootObject = new JSONObject(response);
                        JSONArray resultArray = rootObject.getJSONObject("ResultSet").getJSONArray("Result");
                        ret = new ArrayList<Equity>();
                        for(int i=0; i < resultArray.length(); ++i) {
                            JSONObject result = resultArray.getJSONObject(i);

                            if (result.getString("symbol").contains(".")) // make sure this is domestic as international equities aren't yet supported via barchart integration yet.
                                continue;

                            // For now, we just support stocks and ETFs
                            String type = result.getString("type");
                            if (!type.contentEquals("S") && !type.contentEquals("E"))
                                continue;

                            if (!equityQuery.mRestrictToExchanges.isEmpty())
                            {
                                boolean storeValue = false;
                                String exchange = result.getString("exchDisp");
                                for(String str: equityQuery.mRestrictToExchanges) {
                                    if(str.contentEquals(exchange)) {
                                        storeValue = true;
                                        break;
                                    }
                                }
                                if (!storeValue)
                                    continue;
                            }

                            ret.add(new Equity(result.getString("symbol"), result.getString("name"),result.getString("exchDisp"),result.getString("typeDisp")));
                        }
                    } catch (JSONException e) {
                        Log.e("Magellan", "Error parsing json from yahoo finance: " + e.toString());
                    }

                } catch (Exception e) {
                    Log.e("Magellan", "Error unpacking json from yahoo finance:" + e.getMessage());
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e("Magellan", "Could not connect to yahoo finance" + e.toString());

        }
        return ret;
    }
}
