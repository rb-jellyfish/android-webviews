package com.example.demoshop.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.example.demoshop.databinding.FragmentDashboardBinding
import com.example.demoshop.AnalyticsWebInterface
import com.google.firebase.analytics.FirebaseAnalytics

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        setupWebView()

        return root
    }

    private fun setupWebView() {
        val webView: WebView = binding.webviewDashboard
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }

        webView.clearCache(true)
        WebView.setWebContentsDebuggingEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                error?.let {
                    Log.e("WebView", "Error: ${it.description} (Error Code: ${it.errorCode})")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("WebView", "Page finished loading: $url")
                // Log an event when the page finishes loading
                val bundle = Bundle()
                bundle.putString("loaded_url", url)
                firebaseAnalytics.logEvent("web_page_loaded", bundle)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("WebView", "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}")
                return true
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.addJavascriptInterface(
                AnalyticsWebInterface(requireContext()), AnalyticsWebInterface.TAG
            )
        } else {
            Log.w("DashboardFragment", "Not adding JavaScriptInterface, API Version: ${android.os.Build.VERSION.SDK_INT}")
        }

        webView.loadUrl("https://firebase.google.com/docs/analytics/webview")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
