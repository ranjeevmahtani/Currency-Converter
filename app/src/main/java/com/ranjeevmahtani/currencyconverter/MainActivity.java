package com.ranjeevmahtani.currencyconverter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements Callback<LatestRate>, TextWatcher {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final double INVALID_CONVERSION_RATE = Double.MIN_VALUE;
    private final String CURRENCY_NOT_SET = "valueNotSet";

    private final String KEY_BASE_CURRENCY = "baseCurrencyBundleKey";
    private final String KEY_TARGET_CURRENCY = "targetCurrencyBundleKey";
    private final String KEY_CONVERSION_RATE = "conversionRateBundleKey";

    private final List<String> mCurrencies = new ArrayList<>(Arrays.asList(Currencies.sCurrencies));

    private EditText mBaseAmountEditText;
    private TextView mConvertedAmountView;

    private ProgressBar mProgressBar;

    private String mBaseCurrency = CURRENCY_NOT_SET;
    private String mTargetCurrency = CURRENCY_NOT_SET;

    private double mConversionRate = INVALID_CONVERSION_RATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        AppCompatSpinner baseCurrencySpinner = (AppCompatSpinner) findViewById(R.id.spinner_base_currency);
        AppCompatSpinner targetCurrencySpinner = (AppCompatSpinner) findViewById(R.id.spinner_target_currency);
        mBaseAmountEditText = (EditText) findViewById(R.id.edittext_base_amount);
        mConvertedAmountView = (TextView) findViewById(R.id.textview_converted_amount);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Set up adapters for the two currency spinners
        baseCurrencySpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.support_simple_spinner_dropdown_item,
                        mCurrencies));

        targetCurrencySpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.support_simple_spinner_dropdown_item,
                        mCurrencies));

        // Restore data from savedInstanceState if present
        if (savedInstanceState!= null) {
            if (savedInstanceState.containsKey(KEY_BASE_CURRENCY)) {
                mBaseCurrency = savedInstanceState.getString(KEY_BASE_CURRENCY);
                if (!mBaseCurrency.equals(INVALID_CONVERSION_RATE)){
                    baseCurrencySpinner.setSelection(mCurrencies.indexOf(mBaseCurrency));
                }
            }
            if (savedInstanceState.containsKey(KEY_TARGET_CURRENCY)) {
                mTargetCurrency = savedInstanceState.getString(KEY_TARGET_CURRENCY);
                if (!mTargetCurrency.equals(INVALID_CONVERSION_RATE)) {
                    targetCurrencySpinner.setSelection(mCurrencies.indexOf(mTargetCurrency));
                }
            }
            if (savedInstanceState.containsKey(KEY_CONVERSION_RATE)) {
                mConversionRate = savedInstanceState.getDouble(KEY_CONVERSION_RATE);
            }
        }

        // Set up listeners and watchers to automatically update the converted currency amount
        // when the user changes either of the currencies or the amount of money being converted
        mBaseAmountEditText.addTextChangedListener(this);

        // Using a custom onTouchListener/onItemSelectedListener in order to isolate and act on
        // onItemSelected calls that are only the result of a user tap. Device rotation events thus
        // don't result in new network calls. The class is defined at the bottom of this one.
        TouchItemSelectedListener touchItemSelectedListener = new TouchItemSelectedListener();

        baseCurrencySpinner.setOnItemSelectedListener(touchItemSelectedListener);
        baseCurrencySpinner.setOnTouchListener(touchItemSelectedListener);

        targetCurrencySpinner.setOnItemSelectedListener(touchItemSelectedListener);
        targetCurrencySpinner.setOnTouchListener(touchItemSelectedListener);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_BASE_CURRENCY, mBaseCurrency);
        outState.putString(KEY_TARGET_CURRENCY, mTargetCurrency);
        outState.putDouble(KEY_CONVERSION_RATE, mConversionRate);
    }


    // Interface methods for the TextWatcher that updates the converted value upon user input
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateDisplay();
    }

    // Success callback method for the Retrofit call.
    @Override
    public void onResponse(Call<LatestRate> call, Response<LatestRate> response) {
        // check for null all along the chain to ensure robust behavior in case the API ever
        // changes its response schema or removes a presently supported currency
        if (response != null
                && response.body() != null
                && response.body().getRates() != null
                && response.body().getRates().getAdditionalProperties() != null
                && response.body().getRates().getAdditionalProperties().containsKey(mTargetCurrency)){
            mConversionRate = response.body()
                    .getRates()
                    .getAdditionalProperties()
                    .get(mTargetCurrency);
            updateDisplay();
        } else {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            mConversionRate = INVALID_CONVERSION_RATE;
            mConvertedAmountView.setText(null);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    // Failed callback method for the Retrofit call.
    @Override
    public void onFailure(Call<LatestRate> call, Throwable t) {
        mConversionRate = INVALID_CONVERSION_RATE;
        mConvertedAmountView.setText(null);
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_LONG);
        Log.e(LOG_TAG, "call failed", t);

    }

    private void getConversionRate() {
        // Check for an internet connection. If not connected, invalidate the conversion rate and
        // the displayed value and let the user know he/she's disconnected.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            Toast.makeText(this, getString(R.string.toast_offline), Toast.LENGTH_SHORT).show();
            mConversionRate = INVALID_CONVERSION_RATE;
            mConvertedAmountView.setText(null);
            return;
        }
        // We're connected, so make the API call over the network.
        mProgressBar.setVisibility(View.VISIBLE);
        JacksonConverterFactory jacksonConverter = JacksonConverterFactory.create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fixer.io")
                .addConverterFactory(jacksonConverter)
                .build();
        FixerApi fixerApi = retrofit.create(FixerApi.class);
        Call<LatestRate> call = fixerApi.loadRate(mBaseCurrency, mTargetCurrency);
        call.enqueue(this);
    }

    // This method is called when the user changes the base amount, and also when a new conversion
    // rate is received in the API call response
    private void updateDisplay() {

        if (mBaseAmountEditText.getText().length() == 0) {
            // No input from the user, ensure there is no output.
            mConvertedAmountView.setText(null);
        } else if (mConversionRate == INVALID_CONVERSION_RATE
                && !mBaseCurrency.equals(mTargetCurrency)
                && !mBaseCurrency.equals(CURRENCY_NOT_SET)
                && !mTargetCurrency.equals(CURRENCY_NOT_SET)) {
            // We have what we need to update the conversion rate. Try to do so.
            getConversionRate();
        } else if (mConversionRate != INVALID_CONVERSION_RATE) {
            // We have a conversion rate and an amount to be converted. Show the user the converted value
            double baseAmount = Double.valueOf(mBaseAmountEditText.getText().toString());
            double convertedAmount = baseAmount * mConversionRate;
            mConvertedAmountView.setText(String.format(Locale.getDefault(),"%.2f",convertedAmount));
        }
    }

    // Custom listener that merges functionality of an OnTouchListener and an OnItemSelectedListener
    // This counteracts the repeat calls to onItemSelected on Spinners when the activity is (re)created.
    private class TouchItemSelectedListener implements
            AdapterView.OnItemSelectedListener,
            View.OnTouchListener {

        // a flag to indicate whether or not the event is the result of a user touch or not.
        // this is flipped to true in onTouch, checked in onItemSelected, and flipped back to false
        // at the end of onItemSelected.
        boolean userTouched = false;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getId() == R.id.spinner_base_currency) {
                mBaseCurrency = mCurrencies.get(position);
            } else if (parent.getId() == R.id.spinner_target_currency) {
                mTargetCurrency = mCurrencies.get(position);
            }
            if (userTouched) {
                if (!mBaseCurrency.equals(mTargetCurrency)
                        && !mBaseCurrency.equals(CURRENCY_NOT_SET)
                        && !mTargetCurrency.equals(CURRENCY_NOT_SET)) {
                    // We have two distinct currencies, make an asynchronous network call to set the appropriate
                    // conversion rate. updateDisplay() gets called in subsequent callback.
                    getConversionRate();
                } else {
                    // Just clear up values and the display
                    mConversionRate = INVALID_CONVERSION_RATE;
                    mConvertedAmountView.setText(null);
                }
            }
            userTouched = false;
        }

        // Dictates behavior when nothing is selected in the spinner
        // In that case, reset the appropriate spinner and the conversion rate
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (parent.getId() == R.id.spinner_base_currency) {
                mBaseCurrency = CURRENCY_NOT_SET;
            } else if (parent.getId() == R.id.spinner_target_currency) {
                mTargetCurrency = CURRENCY_NOT_SET;
            }
            mConversionRate = INVALID_CONVERSION_RATE;
            mConvertedAmountView.setText(null);
            userTouched = false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userTouched = true;
            return false;
        }
    }
}
