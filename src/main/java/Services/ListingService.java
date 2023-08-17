package Services;

import APIs.BuffAPI;
import APIs.FloatAPI;
import Models.Item;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ListingService {
    private final FloatAPI floatAPI;
    private final BuffAPI buffScraper;
    private static ListingService instance;
    private final List<Item> oldItems = new ArrayList<>();

    private ListingService() throws ParserConfigurationException, IOException, SAXException {
        floatAPI = new FloatAPI();
        buffScraper = new BuffAPI();
    }

    public static ListingService getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(instance == null) {
            instance = new ListingService();
        }
        return instance;
    }

    public List<Item> getBestDeals() {
        JSONArray listings;
        try {
            listings = floatAPI.getBestDeals();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(listings == null) {
            return null;
        }

        List<Item> items = null;
        items = getItemsFromListings(listings);

        if(items == null || items.size() == 0) {
            return items;
        }

        List<Item> newItems = new ArrayList<>();
        for (Item item : items) {
            boolean found = false;
            for (Item oldItem : oldItems) {
                if (item.getFloatId().equals(oldItem.getFloatId())) {
                    if (item.getPriceDifference() != oldItem.getPriceDifference()) {
                        if (item.getPriceDifference() - oldItem.getPriceDifference() > 10) {
                            newItems.add(item);
                        }
                        Iterator<Item> iterator = oldItems.iterator();
                        while (iterator.hasNext()) {
                            Item currentOldItem = iterator.next();
                            if (currentOldItem.getFloatId().equals(oldItem.getFloatId())) {
                                iterator.remove();
                                break;
                            }
                        }
                        oldItems.add(item);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                newItems.add(item);
                oldItems.add(item);
            }
        }
        return newItems;
    }

    private List<Item> getItemsFromListings(JSONArray listings) {
        List<Item> items = new ArrayList<>();

        if (listings != null && listings.length() > 0) {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Create a thread pool

            List<Future<Item>> futures = new ArrayList<>(); // Store the results of submitted tasks

            for (int i = 0; i < listings.length(); i++) {
                JSONObject listing = listings.getJSONObject(i);
                Future<Item> future = executor.submit(() -> {
                    try {
                        return processListing(listing);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                futures.add(future);
            }

            for (Future<Item> future : futures) {
                try {
                    Item item = future.get(); // Get the result of the task
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown(); // Shutdown the executor
        }

        return items;
    }

    private Item processListing(JSONObject listing) throws IOException {
        String itemName = listing.getJSONObject("item").getString("market_hash_name");

        String floatPriceString = listing.get("price").toString();
        if (floatPriceString.length() >= 3) {
            floatPriceString = floatPriceString.substring(0, floatPriceString.length() - 2) + "." + floatPriceString.substring(floatPriceString.length() - 2);
        }
        double floatPrice = Double.parseDouble(floatPriceString);

        String floatSteamPriceString = listing.getJSONObject("item").getJSONObject("scm").get("price").toString();
        if (floatSteamPriceString.length() >= 3) {
            floatSteamPriceString = floatSteamPriceString.substring(0, floatSteamPriceString.length() - 2) + "." + floatSteamPriceString.substring(floatSteamPriceString.length() - 2);
        }
        double floatSteamPrice = Double.parseDouble(floatSteamPriceString);

        if (floatSteamPrice > floatPrice) {
            return null;
        }

        double buffPrice;

        try {
            buffPrice = buffScraper.getBuffPrice(itemName);
        } catch (IOException | InterruptedException | RuntimeException e) {
            System.out.println("Error getting buff price for " + itemName);
            e.printStackTrace();
            return null; // Return null if there's an error
        }

        if (floatPrice == 0 || buffPrice == 0) {
            System.out.println("There was an error getting the price for " + itemName);
            return null; // Return null if there's an error
        }

        // Calculate percentages
        double percentageDifference = ((buffPrice - floatPrice) / floatPrice) * 100.0;

        if (percentageDifference >= 1.0) {
            String id = listing.getString("id");
            String floatUrl = "https://csfloat.com/item/" + id;
            String buffUrl = "https://buff.163.com/goods/" + buffScraper.getBuffId(itemName);
            return new Item(itemName, id, floatPrice, floatUrl, buffPrice, buffUrl);
        }

        return null; // Return null if the item doesn't meet the condition
    }
}
