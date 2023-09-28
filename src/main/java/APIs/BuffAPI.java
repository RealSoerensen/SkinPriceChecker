package APIs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BuffAPI {

    private static final String BUFF_ID_FILE = "buffids.txt";
    private static final String USD_CNY = "USDCNY";
    private static final String BUFF_API_URL = "https://buff.163.com/api/market/goods/sell_order?game=csgo&page_num=1&goods_id=";
    private static final int MAX_RETRIES = 10;

    public double getBuffPrice(String itemName) throws IOException, InterruptedException {
        String id = getBuffId(itemName);

        if (id == null) {
            throw new RuntimeException("Buff ID not found");
        }

        String url = BUFF_API_URL + id;
        JSONObject item = fetchItemFromBuffAPI(url);

        double price = item.getDouble("price");
        double newPrice = currencyConverter(price);
        String newPriceString = String.format("%.2f", newPrice).replace(",", ".");
        return Double.parseDouble(newPriceString);
    }

    public String getBuffId(String itemName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(BUFF_ID_FILE);

        if (is == null) {
            throw new RuntimeException("Buff ID file not found");
        }

        String buffIds = new String(is.readAllBytes());
        String[] buffIdsArray = buffIds.split("\n");

        for (String id : buffIdsArray) {
            String[] idArray = id.split(";");
            if (idArray[1].equals(itemName)) {
                return idArray[0];
            }
        }

        return null;
    }

    private double currencyConverter(double value) throws IOException, InterruptedException {
        JSONObject usdCnyExchangeRates = fetchExchangeRates();

        JSONObject usdCnyRates = usdCnyExchangeRates.getJSONObject("rates");
        JSONObject usdCnyRate = usdCnyRates.getJSONObject(USD_CNY);
        double usdCnyRateDouble = usdCnyRate.getDouble("rate");
        return value / usdCnyRateDouble;
    }

    private JSONObject fetchItemFromBuffAPI(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        for (int i = 0; i < MAX_RETRIES; i++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray items = data.getJSONArray("items");
                return getFirstItem(items);
            }

            Thread.sleep(5000);
        }

        throw new RuntimeException("Error fetching item from Buff API");
    }

    private JSONObject fetchExchangeRates() throws IOException, InterruptedException {
        for (int i = 0; i < MAX_RETRIES; i++) {
            HttpResponse<String> response = sendHttpRequest();

            if (response.statusCode() == 200) {
                try {
                    return new JSONObject(response.body());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            Thread.sleep(1000);
        }
        throw new RuntimeException("Error fetching exchange rates");
    }

    private JSONObject getFirstItem(JSONArray items) {
        if (items.length() == 0) {
            return null;
        }
        try {
            return items.getJSONObject(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> sendHttpRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.freeforexapi.com/api/live?pairs=USDCNY"))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
