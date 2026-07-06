package com.example.app.utils.api.soap.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Gson @SerializedName 範本
 * JSON snake_case -> Java camelCase 映射
 */
public class SampleResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("data")
    private SampleData data;

    public int getStatus() { return status; }
    public SampleData getData() { return data; }

    public static class SampleData {

        @SerializedName("conversation_id")
        private String conversationId;

        @SerializedName("user_message")
        private String userMessage;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("item_list")
        private List<Item> itemList;

        public String getConversationId() { return conversationId; }
        public String getUserMessage() { return userMessage; }
        public String getCreatedAt() { return createdAt; }
        public List<Item> getItemList() { return itemList; }
    }

    public static class Item {

        @SerializedName("item_name")
        private String itemName;

        @SerializedName("item_price")
        private double itemPrice;

        public String getItemName() { return itemName; }
        public double getItemPrice() { return itemPrice; }
    }
}
