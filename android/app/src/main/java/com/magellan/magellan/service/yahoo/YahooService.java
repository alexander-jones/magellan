package com.magellan.magellan.service.yahoo;

import android.util.Log;

import com.magellan.magellan.stock.IStock;
import com.magellan.magellan.stock.IStockService;
import com.magellan.magellan.stock.StockQuery;

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

public class YahooService implements IStockService{

    public List<IStock> execute(StockQuery stockQuery) {

        String url_select = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + stockQuery.symbolTemplate + "&region=1&lang=en&callback=YAHOO.Finance.SymbolSuggest.ssCallback";

        List<IStock> ret = null;
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
                        String jsonText = response.substring(response.indexOf('(') + 1,response.lastIndexOf(')'));
                        JSONObject rootObject = new JSONObject(jsonText);
                        JSONArray resultArray = rootObject.getJSONObject("ResultSet").getJSONArray("Result");
                        ret = new ArrayList<IStock>();
                        for(int i=0; i < resultArray.length(); ++i) {
                            JSONObject result = resultArray.getJSONObject(i);
                            if (!stockQuery.mRestrictToExchanges.isEmpty())
                            {
                                boolean storeValue = false;
                                String exchange = result.getString("exchDisp");
                                for(String str: stockQuery.mRestrictToExchanges) {
                                    if(str.contentEquals(exchange)) {
                                        storeValue = true;
                                        break;
                                    }
                                }
                                if (!storeValue)
                                    continue;
                            }
                            ret.add(new YahooStock(result.getString("symbol"), result.getString("name"),result.getString("exchDisp"),result.getString("typeDisp")));
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
