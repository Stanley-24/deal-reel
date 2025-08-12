package com.dealonreels.amazonapi;

public class RequestBuilder {

    public static String buildGetItemsPayload(String[] itemIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ItemIds\": [");
        for (int i = 0; i < itemIds.length; i++) {
            sb.append("\"").append(itemIds[i]).append("\"");
            if (i != itemIds.length - 1) sb.append(",");
        }
        sb.append("],");
        sb.append("\"Resources\": [\"Images.Primary.Small\", \"ItemInfo.Title\", \"Offers.Listings.Price\"]");
        sb.append("}");
        return sb.toString();
    }
}
