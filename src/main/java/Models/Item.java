package Models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private String itemName;
    private String floatId;
    private double csfloatPrice;
    private String csFloatUrl;
    private double buffPrice;
    private String buffUrl;
    private double priceDifference;

    public Item(String itemName, String floatId, double csfloatPrice, String csFloatUrl, double buffPrice, String buffUrl) {
        this.itemName = itemName;
        this.floatId = floatId;
        this.csfloatPrice = csfloatPrice;
        this.csFloatUrl = csFloatUrl;
        this.buffPrice = buffPrice;
        this.buffUrl = buffUrl;
        this.priceDifference = calculatePriceDifference();
    }

    private double calculatePriceDifference() {
        double result;
        if(csfloatPrice > buffPrice) {
            result = csfloatPrice - buffPrice;
        } else {
            result = buffPrice - csfloatPrice;
        }
        return Math.round(result * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Item: " + itemName + "\n" +
                "CSFloat Price: $" + csfloatPrice + "\n" +
                "CSFloat URL: " + csFloatUrl + "\n" +
                "Buff Price: $" + buffPrice + "\n" +
                "Buff URL: " + buffUrl + "\n" +
                "Price Difference: $" + priceDifference + "\n";
    }
}
