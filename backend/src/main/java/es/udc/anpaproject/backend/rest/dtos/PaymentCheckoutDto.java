package es.udc.anpaproject.backend.rest.dtos;

public class PaymentCheckoutDto {

    private String checkoutUrl;

    public PaymentCheckoutDto() {}

    public PaymentCheckoutDto(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}