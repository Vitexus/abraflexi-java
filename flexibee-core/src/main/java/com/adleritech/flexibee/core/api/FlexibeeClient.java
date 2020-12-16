package com.adleritech.flexibee.core.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;

import com.adleritech.flexibee.core.api.domain.AddressBookResponse;
import com.adleritech.flexibee.core.api.domain.BankResponse;
import com.adleritech.flexibee.core.api.domain.InternalDocumentResponse;
import com.adleritech.flexibee.core.api.domain.IssuedInvoiceResponse;
import com.adleritech.flexibee.core.api.domain.ObligationResponse;
import com.adleritech.flexibee.core.api.domain.ReceivableResponse;
import com.adleritech.flexibee.core.api.domain.WinstromRequest;
import com.adleritech.flexibee.core.api.domain.WinstromResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class FlexibeeClient {

    private final String company;

    @Getter
    private final Api client;

    private final Converter<ResponseBody, WinstromResponse> errorConverter;


    protected FlexibeeClient(String company, Api retrofitApi, Converter<ResponseBody, WinstromResponse> errorConverter) {
        this.company = company;
        this.client = retrofitApi;
        this.errorConverter = errorConverter;
    }

    public WinstromResponse createInvoice(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.issueInvoice(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public WinstromResponse createInternalDocument(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createInternalDocument(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public WinstromResponse createAddressBook(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createAddressBook(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public WinstromResponse createOrder(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createOrder(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }


    public WinstromResponse createBank(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createBank(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    private void handleErrorResponse(Response response) throws FlexibeeException {
        handleErrorResponse(response, null);
    }

    private void handleErrorResponse(Response response, WinstromRequest winstromRequest) throws FlexibeeException {
        if (!response.isSuccessful()) {
            if (response.code() == 404) {
                throw new NotFound(getErrorBody(response));
            } else {
                String rawErrorResponse = readRawResponse(response.errorBody());
                FlexibeeException flexibeeException;
                try {
                    String message = "Error while creating document in Flexibee";
                    WinstromResponse errorResponse = errorConverter.convert(response.errorBody());
                    flexibeeException = new FlexibeeException(message, winstromRequest, errorResponse, rawErrorResponse);

                } catch (Exception e) {
                    String message = "Error while creating document in Flexibee, " + e.getMessage();
                    flexibeeException = new FlexibeeException(message, winstromRequest, null, rawErrorResponse);
                }

                throw flexibeeException;
            }
        }
    }

    /**
     * Reads raw response body from cloned buffer so responseBody might be used again
     */
    private String readRawResponse(ResponseBody responseBody) throws FlexibeeException {
        try {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // request the entire body.
            Buffer buffer = source.buffer();
            return buffer.clone().readString(Charset.forName("UTF-8"));
        } catch (IOException ioe) {
            throw new FlexibeeException("Cannot parse flexibee errorResponse: " + ioe.getMessage());
        }
    }

    private String getErrorBody(Response response) {
        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return "--Can not read error body--";
        }
    }

    public AddressBookResponse findAddressBookByRegNo(String regNo) throws IOException, FlexibeeException {
        Response<AddressBookResponse> response = client.findAddressBookByRegNo(company, regNo).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public AddressBookResponse findAddressBookfindAddressBookByExternalId(String externalId) throws IOException, FlexibeeException {
        Response<AddressBookResponse> response = client.findAddressBookByExternalId(company, externalId).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public ResponseBody downloadIssuedInvoiceAsPdf(String id) throws IOException, FlexibeeException {
        Response<ResponseBody> response = client.downloadIssuedInvoiceAsPdf(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public IssuedInvoiceResponse getIssuedInvoice(String id) throws IOException, FlexibeeException {
        Response<IssuedInvoiceResponse> response = client.getIssuedInvoice(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public InternalDocumentResponse getInternalDocument(String id) throws IOException, FlexibeeException {
        Response<InternalDocumentResponse> response = client.getInternalDocument(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public BankResponse getBank(String id) throws IOException, FlexibeeException {
        Response<BankResponse> response = client.getBank(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public AddressBookResponse findAddressBookByCode(String code) throws IOException, FlexibeeException {
        Response<AddressBookResponse> response = client.findAddressBookByCode(company, code).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public WinstromResponse updateAddressBook(String id, WinstromRequest request) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.updateAddressBook(company, id, request).execute();
        if (!response.isSuccessful()) {
            String errorBody = getErrorBody(response);
            throw new FlexibeeException("Address book update failed for " +
                    "company=" + id + ", " +
                    "httpStatusCode=" + response.code() + "," +
                    "responseBody=" + errorBody + "," +
                    "requestBody=" + request.toString());
        }
        return response.body();
    }

    public AddressBookResponse searchInAddressBook(String q) throws IOException, FlexibeeException {
        Response<AddressBookResponse> response = client.searchInAddressBook(company, q).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public WinstromResponse createReceivable(WinstromRequest winstromRequest)  throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createReceivable(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public ReceivableResponse getReceivable(String id) throws IOException, FlexibeeException {
        Response<ReceivableResponse> response = client.getReceivable(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public WinstromResponse createObligation(WinstromRequest winstromRequest)  throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.createObligation(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public ObligationResponse getObligation(String id) throws IOException, FlexibeeException {
        Response<ObligationResponse> response = client.getObligation(company, id).execute();
        handleErrorResponse(response);
        return response.body();
    }

    public WinstromResponse createReceivedInvoice(WinstromRequest winstromRequest) throws IOException, FlexibeeException {
        Response<WinstromResponse> response = client.receivedInvoice(company, winstromRequest).execute();
        handleErrorResponse(response, winstromRequest);
        return response.body();
    }

    public void removeInvoice(String invoiceId) throws FlexibeeException, IOException {
        Response<Void> response = client.removeInvoice(company, invoiceId).execute();
        handleErrorResponse(response, null);
    }

    interface Api {

        @PUT("/c/{company}/faktura-prijata.xml")
        Call<WinstromResponse> receivedInvoice(@Path("company") String company, @Body WinstromRequest request);

        @PUT("/c/{company}/faktura-vydana.xml")
        Call<WinstromResponse> issueInvoice(@Path("company") String company, @Body WinstromRequest request);

        @GET("/c/{company}/faktura-vydana/{id}.pdf")
        Call<ResponseBody> downloadIssuedInvoiceAsPdf(@Path("company") String company, @Path("id") String id);

        @GET("/c/{company}/faktura-vydana/{id}.xml")
        Call<IssuedInvoiceResponse> getIssuedInvoice(@Path("company") String company, @Path("id") String id);

        @DELETE("/c/{company}/faktura-vydana/{id}.pdf")
        Call<Void> removeInvoice(@Path("company") String company, @Path("id") String id);

        @PUT("/c/{company}/interni-doklad.xml")
        Call<WinstromResponse> createInternalDocument(@Path("company") String company, @Body WinstromRequest request);

        @GET("/c/{company}/interni-doklad/{id}.xml")
        Call<InternalDocumentResponse> getInternalDocument(@Path("company") String company, @Path("id") String id);

        @PUT("/c/{company}/pohledavka.xml")
        Call<WinstromResponse> createReceivable(@Path("company") String company, @Body WinstromRequest request);

        @GET("/c/{company}/pohledavka/{id}.xml")
        Call<ReceivableResponse> getReceivable(@Path("company") String company, @Path("id") String id);

        @GET("c/{company}/adresar/in:{regNo}.xml")
        Call<AddressBookResponse> findAddressBookByRegNo(@Path("company") String company, @Path("regNo") String regNo);

        @GET("c/{company}/adresar/ext:{regNo}.xml")
        Call<AddressBookResponse> findAddressBookByExternalId(@Path("company") String company, @Path("regNo") String externalId);

        @GET("c/{company}/adresar/(kod='{kod}').xml")
        Call<AddressBookResponse> findAddressBookByCode(@Path("company") String company, @Path("kod") String kod);

        @GET("/c/{company}/adresar.xml")
        Call<AddressBookResponse> searchInAddressBook(@Path("company") String company, @Query("q") String q);

        @PUT("/c/{company}/adresar/{id}.xml")
        Call<WinstromResponse> updateAddressBook(@Path("company") String company, @Path("id") String id, @Body WinstromRequest request);

        @PUT("/c/{company}/adresar.xml")
        Call<WinstromResponse> createAddressBook(@Path("company") String company, @Body WinstromRequest request);

        @PUT("/c/{company}/zakazka.xml")
        Call<WinstromResponse> createOrder(@Path("company") String company, @Body WinstromRequest request);

        @PUT("/c/{company}/banka.xml")
        Call<WinstromResponse> createBank(@Path("company") String company, @Body WinstromRequest request);

        @GET("/c/{company}/banka/{id}.xml")
        Call<BankResponse> getBank(@Path("company") String company, @Path("id") String id);

        @PUT("/c/{company}/zavazek.xml")
        Call<WinstromResponse> createObligation(@Path("company") String company, @Body WinstromRequest request);

        @GET("/c/{company}/zavazek/{id}.xml")
        Call<ObligationResponse> getObligation(@Path("company") String company, @Path("id") String id);
    }

    public static class NotFound extends FlexibeeException {
        public NotFound(String s) {
            super(s);
        }
    }

    public static class FlexibeeException extends Exception {
        @Getter
        private WinstromRequest request;

        @Getter
        private WinstromResponse errorResponse;

        @Getter
        private String rawErrorResponse;

        private FlexibeeException(String s) {
            super(s);
        }

        private FlexibeeException(String message, WinstromRequest request, WinstromResponse errorResponse, String rawErrorResponse) {
            super(message);
            this.request = request;
            this.errorResponse = errorResponse;
            this.rawErrorResponse = rawErrorResponse;
        }
    }

    /**
     * Support for custom SSL configuration
     */
    @AllArgsConstructor
    @Getter
    public static class SSLConfig {
        @NonNull
        private final KeyStore keyStore;

        private final HostnameVerifier hostnameVerifier;
    }
}
