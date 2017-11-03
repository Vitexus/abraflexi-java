package com.adleritech.flexibee.core.api;

import com.adleritech.flexibee.core.api.domain.AddressBook;
import com.adleritech.flexibee.core.api.domain.InternalDocument;
import com.adleritech.flexibee.core.api.domain.IssuedInvoice;
import com.adleritech.flexibee.core.api.domain.IssuedInvoiceItem;
import com.adleritech.flexibee.core.api.domain.Order;
import com.adleritech.flexibee.core.api.domain.WinstromRequest;
import com.adleritech.flexibee.core.api.domain.WinstromResponse;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FlexibeeClientTest {

    @Test
    public void createInvoice() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .issuedInvoice(IssuedInvoice.builder()
                        .company("code:ABCFIRM1#")
                        .documentType("code:faktura")
                        .withoutItems(true)
                        .sumWithoutVat(BigDecimal.valueOf(1000))
                        .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInvoice(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        Assertions.assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void createDummyInvoice() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .issuedInvoice(IssuedInvoice.builder()
                        .company("code:ABCFIRM1#")
                        .documentType("code:faktura")
                        .items(Collections.singletonList(
                                IssuedInvoiceItem.builder()
                                        .name("Bla bla jizdne")
                                        .amount(1)
                                        .sumVat(BigDecimal.valueOf(1500))
                                        .unitPrice(BigDecimal.valueOf(7500))
                                        .sumTotal(BigDecimal.valueOf(9000))
                                        .vatRate(BigDecimal.valueOf(21)).build()
                        ))
                        .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInvoice(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void createInvoiceWithAddressBook() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .issuedInvoice(IssuedInvoice.builder()
                        .company("code:ABCFIRM1#")
                        .documentType("code:faktura")
                        .items(Collections.singletonList(
                                IssuedInvoiceItem.builder()
                                        .name("Invoice line")
                                        .amount(1)
                                        .unitPrice(BigDecimal.valueOf(128_140.96))
                                        .vatRate(BigDecimal.valueOf(21)).build()
                        ))
                        .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInvoice(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void updateAddressBook() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .addressBook(
                    AddressBook.builder()
                        .name("test")
                        .id(Collections.singletonList(String.format("ext:%s", new Random().nextInt())))
                        .build()
                ).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.updateAddressBook("1569", request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test(expected = FlexibeeClient.FlexibeeException.class)
    public void updateCompanyWithDuplicatedId() throws Exception {
        String alreadyExistingId = "-1207125871";
        WinstromRequest request = WinstromRequest.builder().addressBook(AddressBook.builder().name("test").id(Collections.singletonList(Helpers.externalId(alreadyExistingId))).build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        flexibeeClient.updateAddressBook("1568", request);
    }

    @Test
    public void createOrder() throws Exception {
        String alreadyExistingId = "-1207125871";
        WinstromRequest request = WinstromRequest.builder().order(
                Order.builder().name("test").id(Collections.singletonList(Helpers.externalId(alreadyExistingId))).build()
        ).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse order = flexibeeClient.createOrder(request);

        assertThat(order).isNotNull();
    }

    @Test
    public void createInvoiceWithOrderButNoCompany() throws Exception {
        String alreadyExistingId = String.valueOf(Math.random());
        WinstromRequest request = WinstromRequest.builder()
                .order(Order.builder().name("test").id(Collections.singletonList(Helpers.externalId(alreadyExistingId))).build())
                .issuedInvoice(
                        IssuedInvoice.builder()
                                .company("code:ABCFIRM1#")
                                .documentType("code:faktura")
                                .items(Collections.singletonList(
                                        IssuedInvoiceItem.builder()
                                                .name("Invoice line")
                                                .amount(1)
                                                .unitPrice(BigDecimal.valueOf(128_140.96))
                                                .vatRate(BigDecimal.valueOf(21)).build()
                                ))
                                .order(Helpers.externalId(alreadyExistingId))
                                .build())
                .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse order = flexibeeClient.createOrder(request);

        assertThat(order).isNotNull();
    }

    @Test
    public void canUpdateSameRecordWithSameExternalId() throws Exception {
        String ext = String.format("ext:%s", new Random().nextInt());
        WinstromRequest request = WinstromRequest.builder()
                .addressBook(
                        AddressBook.builder()
                                .id(Collections.singletonList(ext))
                                .build()
                ).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.updateAddressBook("1569", request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        WinstromResponse bla = flexibeeClient.updateAddressBook(response.getResults().get(0).getId(), request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
    }

    @Test
    public void createInternalDocument() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
            .internalDocument(
                InternalDocument.builder()
                    .company("code:PBENDA")
                    .documentType("code:ID")
                    .issued(LocalDate.parse("2017-10-03"))
                    .variableSymbol("123")
                    .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInternalDocument(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }
}
