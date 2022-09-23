package com.transifex.common;

import com.google.gson.Gson;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Helper class that contains boilerplate code for setting up and destroying a mock web server, as
 * well as static methods for configuring it for various unit tests.
 */
public class CDSMockHelper {

    public static final String elBody = "{\"data\":{\"test_key\":{\"string\":\"Καλημέρα\"},\"another_key\":{\"string\":\"Καλό απόγευμα\"},\"key3\":{\"string\":\"\"}}}";
    public static final String esBody = "{\"data\":{\"test_key\":{\"string\":\"Buenos días\"},\"another_key\":{\"string\":\"Buenas tardes\"},\"key3\":{\"string\":\"\"}}}";
    public static final String elBodyBadFormatting = "not a JSON format";

    //region Interface

    private MockWebServer server = null;
    private String baseUrl = null;

    public void setUpServer() {
        server = new MockWebServer();
        baseUrl = server.url("").toString();
    }

    public void teardownServer() {
        if (server != null) {
            try {
                server.shutdown();
            } catch (IOException ignored) {}
        }
    }

    public MockWebServer getServer() {
        return server;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    //endregion Interface

    //region Static methods

    public static Dispatcher getElEsDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es":
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    public static Dispatcher getElDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    public static Dispatcher getElEsWithTagsDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    // "/content/el?filter[tags]=tag a,tag b"
                    case "/content/el?filter%5Btags%5D=tag%20a%2Ctag%20b":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es?filter%5Btags%5D=tag%20a%2Ctag%20b":
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    public static Dispatcher getElDispatcherBadJsonFormatting() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBodyBadFormatting);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    public static Dispatcher getElEs202OnceDispatcher(final int maxRetryTimes) {

        Dispatcher dispatcher = new Dispatcher() {

            int elCounter = 0;
            int eSCounter = 0;

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        if (elCounter < maxRetryTimes) {
                            elCounter++;
                            return new MockResponse().setResponseCode(202);
                        }
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es":
                        if (eSCounter < maxRetryTimes) {
                            eSCounter++;
                            return new MockResponse().setResponseCode(202);
                        }
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    // A dispatcher that emulates the "push content" and "job status" CDS endpoints. The "job status"
    // endpoint responds with "processing", followed by a "completed" job status response.
    public static Dispatcher getPostDispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            int counter = 0;

            final int processingCount = 1;
            final String jobId = "abcd";
            final String jobLink = "/jobs/content/" + jobId;
            final Gson gson = new Gson();

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
                        responseData.data = new LocaleData.TxPostResponseData.Data();
                        responseData.data.id = jobId;
                        responseData.data.links = new LocaleData.TxPostResponseData.Data.Links();
                        responseData.data.links.job = jobLink;
                        return new MockResponse().setResponseCode(202).setBody(gson.toJson(responseData));
                    case jobLink:
                        if (counter < processingCount) {
                            counter++;
                            LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                            jobStatus.data = new LocaleData.TxJobStatus.Data();
                            jobStatus.data.status = "processing";
                            return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                        }
                        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                        jobStatus.data = new LocaleData.TxJobStatus.Data();
                        jobStatus.data.status = "completed";
                        jobStatus.data.details = new LocaleData.TxJobStatus.Data.Details();
                        jobStatus.data.details.created = 2;
                        return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    // A dispatcher that emulates the push content and Job status CDS endpoints. The job status
    // returned is "failed" and contains errors.
    public static Dispatcher getPostWithFailedJobDispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            final String jobId = "abcd";
            final String jobLink = "/jobs/content/" + jobId;
            final Gson gson = new Gson();

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
                        responseData.data = new LocaleData.TxPostResponseData.Data();
                        responseData.data.id = jobId;
                        responseData.data.links = new LocaleData.TxPostResponseData.Data.Links();
                        responseData.data.links.job = jobLink;
                        return new MockResponse().setResponseCode(202).setBody(gson.toJson(responseData));
                    case jobLink:
                        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                        jobStatus.data = new LocaleData.TxJobStatus.Data();
                        jobStatus.data.status = "failed";
                        jobStatus.data.details = new LocaleData.TxJobStatus.Data.Details();
                        LocaleData.TxJobStatus.Data.Error error = new LocaleData.TxJobStatus.Data.Error();
                        error.status = 409;
                        error.code = "conflict";
                        error.title = "Conflict error";
                        error.detail = "Expected plural rules '['one', 'other']' instead got '['few', 'many', 'one', 'other', 'two', 'zero']' for new resource string on source language 'en'";
                        jobStatus.data.errors = new LocaleData.TxJobStatus.Data.Error[]{error};
                        return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    // A dispatcher that emulates the push content endpoints that returns 409.
    public static Dispatcher getPostWith409Dispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        return new MockResponse().setResponseCode(409);
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    //endregion Static methods
}