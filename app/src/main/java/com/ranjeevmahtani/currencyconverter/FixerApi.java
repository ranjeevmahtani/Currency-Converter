package com.ranjeevmahtani.currencyconverter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface used by Retrofit to make the API call to Fixer.io
 */
public interface FixerApi {

    @GET("/latest")
    Call<LatestRate> loadRate(@Query("base") String baseCurrency, @Query("symbols") String toCurrency);

}
