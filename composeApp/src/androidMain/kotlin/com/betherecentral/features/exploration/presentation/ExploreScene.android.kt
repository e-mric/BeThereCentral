package com.betherecentral.features.exploration.presentation

import android.content.pm.ApplicationInfo
import android.graphics.Color as AndroidColor
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import org.json.JSONTokener

actual val supportsExploreScene: Boolean = true

@Composable
actual fun ExploreScene(modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()
            WebView(context).apply {
                setBackgroundColor(AndroidColor.rgb(12, 17, 20))
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    private var reportedPageLayout = false

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        if (reportedPageLayout) return
                        reportedPageLayout = true

                        view.postVisualStateCallback(0L, object : WebView.VisualStateCallback() {
                            override fun onComplete(requestId: Long) { view.invalidate() }
                        })
                        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return

                        val layoutScript = """(() => {
                            const size = (element) => element ? {
                                width: Math.round(element.getBoundingClientRect().width),
                                height: Math.round(element.getBoundingClientRect().height)
                            } : null;
                            return JSON.stringify({
                                viewport: { width: window.innerWidth, height: window.innerHeight },
                                body: size(document.body),
                                canvas: size(document.querySelector('canvas')),
                                header: size(document.querySelector('header')),
                                footer: size(document.querySelector('footer'))
                            });
                        })()""".trimIndent()
                        view.evaluateJavascript(layoutScript) { result ->
                            val layout = runCatching { JSONTokener(result).nextValue() as? String }
                                .getOrNull() ?: "unavailable"
                            Log.i(
                                "ExploreWebView",
                                "page layout native=${view.width}x${view.height} js=$layout",
                            )
                        }
                    }

                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        val url = request.url
                        if (url.scheme != "https" || url.host != "appassets.androidplatform.net") {
                            return blockedResponse()
                        }
                        return assetLoader.shouldInterceptRequest(url) ?: blockedResponse()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        request.url.scheme != "https" || request.url.host != "appassets.androidplatform.net"
                }
                loadUrl("https://appassets.androidplatform.net/assets/index.html")
            }
        },
        update = { },
        onRelease = { it.destroy() },
    )
}

private fun blockedResponse() = WebResourceResponse(
    "text/plain",
    "utf-8",
    403,
    "Forbidden",
    emptyMap(),
    ByteArrayInputStream(ByteArray(0)),
)
