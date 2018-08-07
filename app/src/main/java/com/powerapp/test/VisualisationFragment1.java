package com.powerapp.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Arrays;

/**
 * Created by lukas on 21.04.2018.
 */

public class VisualisationFragment1 extends Fragment {
    private WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View statisticsView  =  inflater.inflate(R.layout.visualisation_web, container, false);
        webView = statisticsView.findViewById(R.id.web1);
        WebSettings webSettings =
                webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webView.setWebChromeClient(
                new WebChromeClient());


        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(
                    WebView view,
                    String url)
            {

                // after the HTML page loads,
                // load the pie chart
                loadPieChart();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());

                return super.onConsoleMessage(consoleMessage);
            }
        });


        // note the mapping from  file:///android_asset
        // to Android-D3jsPieChart/assets or
        // Android-D3jsPieChart/app/src/main/assets
        webView.loadUrl("file:///android_asset/" +
                "html/progressBarAndroid.html");

//
        return statisticsView;
    }



    public void loadPieChart()
    {
        String text = Utils.readCSVtext();
        // pass the array to the JavaScript function
        webView.loadUrl("javascript:mainFunction(" +text + ")");

    }
}


