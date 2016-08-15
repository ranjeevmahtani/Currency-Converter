package com.ranjeevmahtani.currencyconverter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * LatestRate is a model class used by Jackson to map a JSON response received from the Fixer API into a POJO.
 * Nested "Rates" class below is used to map the exchange rate for a specific target currency.
 * The corresponding API call that uses these classes is in the FixerApi interface.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "base",
        "date",
        "rates"
})
public class LatestRate {

    @JsonProperty("base")
    private String base;
    @JsonProperty("date")
    private String date;
    @JsonProperty("rates")
    private Rates rates;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The base
     */
    @JsonProperty("base")
    public String getBase() {
        return base;
    }

    /**
     *
     * @param base
     * The base
     */
    @JsonProperty("base")
    public void setBase(String base) {
        this.base = base;
    }

    /**
     *
     * @return
     * The date
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date
     * The date
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     * The rates
     */
    @JsonProperty("rates")
    public Rates getRates() {
        return rates;
    }

    /**
     *
     * @param rates
     * The rates
     */
    @JsonProperty("rates")
    public void setRates(Rates rates) {
        this.rates = rates;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }



    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({

    })
    public class Rates {

        @JsonIgnore
        private Map<String, Double> additionalProperties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Double> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Double value) {
            this.additionalProperties.put(name, value);
        }
    }

}
