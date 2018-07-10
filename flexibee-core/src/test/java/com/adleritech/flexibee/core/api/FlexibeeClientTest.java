package com.adleritech.flexibee.core.api;

import com.adleritech.flexibee.core.api.domain.AccountMovementType;
import com.adleritech.flexibee.core.api.domain.AddressBook;
import com.adleritech.flexibee.core.api.domain.Bank;
import com.adleritech.flexibee.core.api.domain.BankItem;
import com.adleritech.flexibee.core.api.domain.InternalDocument;
import com.adleritech.flexibee.core.api.domain.IssuedInvoice;
import com.adleritech.flexibee.core.api.domain.IssuedInvoiceItem;
import com.adleritech.flexibee.core.api.domain.IssuedInvoiceItems;
import com.adleritech.flexibee.core.api.domain.IssuedInvoiceResponse;
import com.adleritech.flexibee.core.api.domain.ItemType;
import com.adleritech.flexibee.core.api.domain.Obligation;
import com.adleritech.flexibee.core.api.domain.ObligationItem;
import com.adleritech.flexibee.core.api.domain.ObligationItems;
import com.adleritech.flexibee.core.api.domain.Order;
import com.adleritech.flexibee.core.api.domain.PaymentStatus;
import com.adleritech.flexibee.core.api.domain.Receivable;
import com.adleritech.flexibee.core.api.domain.ReceivedInvoice;
import com.adleritech.flexibee.core.api.domain.RoundingPrecision;
import com.adleritech.flexibee.core.api.domain.VatRateKind;
import com.adleritech.flexibee.core.api.domain.WinstromRequest;
import com.adleritech.flexibee.core.api.domain.WinstromResponse;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

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
        String invoiceId = response.getResults().get(0).getId();
        assertThat(invoiceId).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        IssuedInvoiceResponse issuedInvoice = flexibeeClient.getIssuedInvoice(invoiceId);
        assertThat(issuedInvoice).isNotNull();

        flexibeeClient.removeInvoice(invoiceId);
    }

    @Test
    public void createDeposit() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
            .issuedInvoice(IssuedInvoice.builder()
                .documentType("code:ZÁLOHA")
                .paymentStatus(PaymentStatus.MANUALLY)
                .roundingPrecision(RoundingPrecision.UNITS)
                .items(new IssuedInvoiceItems(
                    IssuedInvoiceItem.builder().unitPrice(BigDecimal.valueOf(124.20)).vatRateKind(VatRateKind.FREE).build())
                )
                .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInvoice(request);
        String invoiceId = response.getResults().get(0).getId();
        assertThat(invoiceId).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        IssuedInvoiceResponse issuedInvoice = flexibeeClient.getIssuedInvoice(invoiceId);
        assertThat(issuedInvoice).isNotNull();
        assertThat(issuedInvoice.getIssuedInvoice().getSumTotal()).isEqualByComparingTo("125");

        flexibeeClient.removeInvoice(invoiceId);
    }

    @Test
    public void createDummyInvoice() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .issuedInvoice(IssuedInvoice.builder()
                        .company("code:ABCFIRM1#")
                        .documentType("code:faktura")
                        .paymentForm("code:KARTA")
                        .items(new IssuedInvoiceItems(
                                IssuedInvoiceItem.builder()
                                        .name("Bla bla jizdne")
                                        .amount(ONE)
                                        .sumVat(BigDecimal.valueOf(1500))
                                        .vatRateKind(VatRateKind.BASIC)
                                        .unitPrice(BigDecimal.valueOf(7500))
                                        .sumTotal(BigDecimal.valueOf(9000))
                                        .vatRate(BigDecimal.valueOf(21)).build()
                        ))
                        .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInvoice(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        IssuedInvoiceResponse issuedInvoice = flexibeeClient.getIssuedInvoice(response.getResults().get(0).getId());
        assertThat(issuedInvoice).isNotNull();
        assertThat(issuedInvoice.getIssuedInvoice().getRegNo()).isNotNull();
    }

    @Test
    public void createInvoiceWithAddressBook() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
                .issuedInvoice(IssuedInvoice.builder()
                        .company("code:ABCFIRM1#")
                        .documentType("code:faktura")
                        .items(new IssuedInvoiceItems(
                                IssuedInvoiceItem.builder()
                                        .name("Invoice line")
                                        .amount(ONE)
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
                        .id(singletonList(String.format("ext:%s", new Random().nextInt())))
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
        WinstromRequest request = WinstromRequest.builder().addressBook(AddressBook.builder().name("test").id(singletonList(Helpers.externalId(alreadyExistingId))).build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        flexibeeClient.updateAddressBook("1568", request);
    }

    @Test
    public void createOrder() throws Exception {
        String alreadyExistingId = "-1207125871";
        WinstromRequest request = WinstromRequest.builder().order(
                Order.builder().name("test").id(singletonList(Helpers.externalId(alreadyExistingId))).build()
        ).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse order = flexibeeClient.createOrder(request);

        assertThat(order).isNotNull();
    }

    @Test
    public void createInvoiceWithOrderButNoCompany() throws Exception {
        String alreadyExistingId = String.valueOf(Math.random());
        WinstromRequest request = WinstromRequest.builder()
                .order(Order.builder().name("test").id(singletonList(Helpers.externalId(alreadyExistingId))).build())
                .issuedInvoice(
                        IssuedInvoice.builder()
                                .company("code:ABCFIRM1#")
                                .documentType("code:faktura")
                                .items(new IssuedInvoiceItems(
                                        IssuedInvoiceItem.builder()
                                                .name("Invoice line")
                                                .amount(ONE)
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
                                .id(singletonList(ext))
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
        String extId = "ext:123:456";

        String varSymbol = "132456";
        LocalDate rideFinishedAt = LocalDate.now();
        BigDecimal amount = BigDecimal.TEN;
        WinstromRequest request = WinstromRequest.builder()
            .internalDocument(InternalDocument
                .builder()
                .id(singletonList(extId))
                .documentType("code:ID")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("Liftago Kredit")
                .timeOfSupply(rideFinishedAt)
                .vatFreeSum(amount)
                .noLines(true)
                .currency("code:CZK")
                .primaryAccount("101")
                .contraAccount("102")
                .vatRow("code:000P")
                .vatReportRow("code:0.0.")
                .company("code:PBENDA")
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createInternalDocument(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        InternalDocument internalDocument = flexibeeClient.getInternalDocument(extId).getInternalDocument();
        assertThat(internalDocument.getId()).contains(response.getResults().get(0).getId());
    }

    @Test
    public void createReceivable() throws Exception {
        String extId = "ext:123:456";

        String varSymbol = "132456";
        LocalDate rideFinishedAt = LocalDate.now();
        BigDecimal amount = BigDecimal.TEN;
        WinstromRequest request = WinstromRequest.builder()
            .receivable(Receivable
                .builder()
                .id(singletonList(extId))
                .documentType("code:OST. POHLEDÁVKY")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("Liftago Kredit")
                .timeOfSupply(rideFinishedAt)
                .vatFreeSum(amount)
                .noLines(true)
                .currency("code:CZK")
                .primaryAccount("101")
                .contraAccount("102")
                .vatRow("code:000P")
                .vatReportRow("code:0.0.")
                .company("code:PBENDA")
                .bankAccount("code:BANKOVNÍ ÚČET")
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createReceivable(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        Receivable receivable = flexibeeClient.getReceivable(extId).getReceivable();
        assertThat(receivable.getId()).contains(response.getResults().get(0).getId());
    }

    @Test
    public void createObligation() throws Exception {
        String extId = "ext:liftago:1235";

        String varSymbol = "142456";
        LocalDate rideFinishedAt = LocalDate.now();
        BigDecimal amount = BigDecimal.TEN;
        WinstromRequest request = WinstromRequest.builder()
            .obligation(Obligation
                .builder()
                .id(singletonList(extId))
                .documentType("code:OZSK")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("závazkové zrcadlo OPSK pro výplatu do LSK")
                .timeOfSupply(rideFinishedAt)
                .dueDate(rideFinishedAt)
                .vatFreeSum(amount)
                .noLines(true)
                .currency("code:CZK")
                .primaryAccount("101")
                .contraAccount("102")
                .vatRow("code:40-41")
                .vatReportRow("code:0.0.")
                .company("code:400219161")
                .bankAccount("code:BANKOVNÍ ÚČET")
                .company("code:PBENDA")
                .sequence("code:OZSK")
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");

        WinstromResponse response = flexibeeClient.createObligation(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        Obligation receivable = flexibeeClient.getObligation(extId).getObligation();
        assertThat(receivable.getId()).contains(response.getResults().get(0).getId());
    }

    @Test
    public void createObligationWithItems() throws Exception {
        String extId = "ext:liftago:1236";

        BigDecimal amount = BigDecimal.TEN;
        ObligationItem item = ObligationItem.builder()
            .name("Item")
            .itemType(ItemType.ACCOUNTING)
            .currency("code:CZK")
            .vatRateKind(VatRateKind.FREE)
            .vatRow("code:000P")
            .vatRowCopy(false)
            .sumTotal(amount)
            .creditSideCopy(false)
            .creditSide("103")
            .debitSideCopy(false)
            .debitSide("104")
            .build();

        String varSymbol = "142456";
        LocalDate rideFinishedAt = LocalDate.now();
        WinstromRequest request = WinstromRequest.builder()
            .obligation(Obligation
                .builder()
                .id(singletonList(extId))
                .documentType("code:OZSK")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("závazkové zrcadlo OPSK pro výplatu do LSK")
                .timeOfSupply(rideFinishedAt)
                .taxableFulfillment(rideFinishedAt)
                .dueDate(rideFinishedAt)
                .items(new ObligationItems(item))
                .currency("code:CZK")
                .primaryAccount("101")
                .contraAccount("102")
                .vatRow("code:40-41")
                .vatReportRow("code:0.0.")
                .company("code:400219161")
                .bankAccount("code:BANKOVNÍ ÚČET")
                .company("code:PBENDA")
                .sequence("code:OZSK")
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");

        WinstromResponse response = flexibeeClient.createObligation(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        Obligation receivable = flexibeeClient.getObligation(extId).getObligation();
        assertThat(receivable.getId()).contains(response.getResults().get(0).getId());
    }

    @Test
    public void createBank() throws Exception {
        String extId = "ext:bank:123:456";

        String varSymbol = "132456";
        LocalDate rideFinishedAt = LocalDate.now();
        BigDecimal amount = BigDecimal.TEN;
        WinstromRequest request = WinstromRequest.builder()
            .bank(Bank
                .builder()
                .id(singletonList(extId))
                .documentType("code:STANDARD")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("Liftago Kredit")
                .timeOfSupply(rideFinishedAt)
                .vatFreeSum(amount)
                .noLines(true)
                .currency("code:CZK")
                .primaryAccount("101")
                .contraAccount("102")
                .vatRow("code:000P")
                .vatReportRow("code:0.0.")
                .company("code:PBENDA")
                .accountMovementType(AccountMovementType.DEBIT)
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createBank(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        Bank bank = flexibeeClient.getBank(extId).getBank();
        assertThat(bank.getId()).contains(response.getResults().get(0).getId());
    }

    @Test
    public void createBankWithItems() throws Exception {
        String extId = "ext:bank:123:458";

        String varSymbol = "132456";
        LocalDate rideFinishedAt = LocalDate.now();
        WinstromRequest request = WinstromRequest.builder()
            .bank(Bank
                .builder()
                .id(singletonList(extId))
                .documentType("code:STANDARD")
                .variableSymbol(varSymbol)
                .incomingNumber(varSymbol)
                .description("Liftago Kredit")
                .timeOfSupply(rideFinishedAt)
                .items(singletonList(
                    BankItem.builder()
                        .currency("code:CZK")
                        .name("Item")
                        .sumWithoutVat(valueOf(100))
                        .vatRate(valueOf(21))
                        .vatRateKind(VatRateKind.BASIC)
                        .itemType(ItemType.ACCOUNTING)
                        .build()
                ))
                .primaryAccount("101")
                .contraAccount("102")
                .company("code:PBENDA")
                .accountMovementType(AccountMovementType.DEBIT)
                .build())
            .build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createBank(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();

        Bank bank = flexibeeClient.getBank(extId).getBank();
        assertThat(bank.getId()).contains(response.getResults().get(0).getId());
    }

    @Test(expected = FlexibeeClient.NotFound.class)
    public void getNonexistingInternalDocument() throws Exception {
        String extId = "ext:123:unknown";

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        flexibeeClient.getInternalDocument(extId).getInternalDocument();
    }

    @Test
    public void createReceivedInvoice() throws Exception {
        WinstromRequest request = WinstromRequest.builder()
            .receivedInvoice(ReceivedInvoice.builder()
                .company("code:ABCFIRM1#")
                .documentType("code:faktura")
                .incomingNumber("112233")
                .withoutItems(true)
                .baseTotalSum(BigDecimal.valueOf(1000))
                .build()).build();

        FlexibeeClient flexibeeClient = new FlexibeeClient("winstrom", "winstrom", "demo");
        WinstromResponse response = flexibeeClient.createReceivedInvoice(request);
        assertThat(response.getResults().get(0).getId()).isNotEmpty();
        assertThat(response.isSuccess()).isTrue();
    }

}
