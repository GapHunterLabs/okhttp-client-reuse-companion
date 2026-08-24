package com.acmecorp.orders;

public class PaymentApiClient {

    private final OkHttpClient sharedClient;

    // Built once, in the constructor -- not flagged.
    PaymentApiClient() {
        this.sharedClient = new OkHttpClient();
    }

    // Built again on every call inside a regular method -- flagged.
    void charge(String orderId) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        client.newCall(buildRequest(orderId)).execute();
    }
}
