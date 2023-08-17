package APIs;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FloatAPI {

    private final String apiKey;
    private static final int MAX_RETRIES = 10;

    public FloatAPI() throws ParserConfigurationException, IOException, SAXException {
        apiKey = loadCsgoFloatApiToken();
        if(apiKey == null) {
            throw new RuntimeException("CSGOFloat API key not found");
        }
    }

    private static String loadCsgoFloatApiToken() throws ParserConfigurationException, IOException, SAXException {
        InputStream in = FloatAPI.class.getClassLoader().getResourceAsStream("apikeys.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);
        document.getDocumentElement().normalize();

        NodeList apiKeyNodes = document.getElementsByTagName("string");

        for (int i = 0; i < apiKeyNodes.getLength(); i++) {
            Node apiKeyNode = apiKeyNodes.item(i);
            if (apiKeyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element apiKeyElement = (Element) apiKeyNode;
                String apiKeyName = apiKeyElement.getAttribute("name");
                if ("csgofloatapi".equals(apiKeyName)) {
                    return apiKeyElement.getTextContent().trim();
                }
            }
        }

        return null;
    }

    public JSONArray getBestDeals() throws IOException, InterruptedException {
        JSONArray listings = null;
        int MAX_PRICE = 50000000;
        int MIN_PRICE = 1000;
        String baseUrl = "https://csfloat.com/api/v1/listings?min_price=" + MIN_PRICE +"&max_price=" + MAX_PRICE +"&type=buy_now";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .headers("Authorization", apiKey)
                .build();

        for (int i = 0; i < MAX_RETRIES; i++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try {
                    listings = new JSONArray(response.body());
                    break;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return listings;
    }
}
