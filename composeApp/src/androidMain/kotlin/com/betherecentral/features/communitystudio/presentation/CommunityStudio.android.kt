package com.betherecentral.features.communitystudio.presentation

import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

actual val supportsCommunityStudio: Boolean = true

@Composable
actual fun CommunityStudioBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

@Composable
actual fun CommunityStudioHost(modifier: Modifier) {
    val context = LocalContext.current
    val importCallback = remember { ImportChooserCallback() }
    val latestImportCallback = rememberUpdatedState(importCallback)
    val exportState = remember { ExportState() }
    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> exportState.finish(context, uri) }
    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> latestImportCallback.value.deliver(uri) }
    val latestCreateDocument = rememberUpdatedState(createDocument)
    val latestOpenDocument = rememberUpdatedState(openDocument)
    val bridge = remember {
        StudioBridge(
            requestExport = { payload ->
                if (!exportState.queue(payload)) false
                else {
                    Handler(Looper.getMainLooper()).post {
                        if (exportState.isActive()) {
                            latestCreateDocument.value.launch("community-room.json")
                        }
                    }
                    true
                }
            },
        )
    }
    val chromeClient = remember {
        object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?,
            ): Boolean {
                latestImportCallback.value.replace(filePathCallback)
                latestOpenDocument.value.launch(arrayOf("application/json"))
                return true
            }
        }
    }
    val trustedHost = "studio.betherecentral.local"
    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .setDomain(trustedHost)
            .addPathHandler("/", WebViewAssetLoader.PathHandler { path ->
                val assets = WebViewAssetLoader.AssetsPathHandler(context)
                assets.handle("community-studio/" + path.ifBlank { "200.html" })
            })
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            WebView(viewContext).apply {
                exportState.deliverStatus = { status ->
                    evaluateJavascript(
                        "window.dispatchEvent(new CustomEvent('betherecentral-export-result',{detail:'$status'}))",
                        null,
                    )
                }
                setBackgroundColor(AndroidColor.rgb(12, 17, 20))
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                addJavascriptInterface(bridge, "BeThereCentralStudio")
                webChromeClient = chromeClient
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        val uri = request.url
                        if (!uri.isTrustedStudioUri(trustedHost)) return blockedResponse()
                        return assetLoader.shouldInterceptRequest(uri) ?: blockedResponse()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val uri = request.url
                        return !uri.isTrustedStudioUri(trustedHost)
                    }
                }
                loadUrl("https://$trustedHost/")
            }
        },
        update = { },
        onRelease = { webView ->
            importCallback.cancel()
            exportState.dispose()
            webView.stopLoading()
            webView.removeJavascriptInterface("BeThereCentralStudio")
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.loadUrl("about:blank")
            webView.destroy()
        },
    )
}

private class ImportChooserCallback {
    private var callback: ValueCallback<Array<Uri>>? = null

    fun replace(next: ValueCallback<Array<Uri>>?) {
        callback?.onReceiveValue(null)
        callback = next
    }

    fun deliver(uri: Uri?) {
        callback?.onReceiveValue(uri?.let { arrayOf(it) })
        callback = null
    }

    fun cancel() {
        callback?.onReceiveValue(null)
        callback = null
    }
}

private class ExportState {
    private var pendingPayload: ByteArray? = null
    private var disposed = false
    var deliverStatus: ((String) -> Unit)? = null
        @Synchronized set
        @Synchronized get

    @Synchronized
    fun queue(payload: String): Boolean {
        if (disposed || pendingPayload != null) return false
        val bytes = payload.toByteArray(StandardCharsets.UTF_8)
        if (!isValidCommunityStudioExportPayload(payload)) return false
        pendingPayload = bytes
        return true
    }

    fun finish(context: android.content.Context, uri: Uri?) {
        val bytes = synchronized(this) {
            if (disposed) return
            pendingPayload.also { pendingPayload = null }
        } ?: return
        val status = if (uri == null) {
            "cancelled"
        } else {
            runCatching {
                context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                    ?: error("Could not open the selected file")
            }.fold(onSuccess = { "success" }, onFailure = { "error" })
        }
        val callback = synchronized(this) { if (!disposed) deliverStatus else null }
        callback?.invoke(status)
    }

    @Synchronized
    fun isActive(): Boolean = !disposed

    @Synchronized
    fun dispose() {
        disposed = true
        pendingPayload = null
        deliverStatus = null
    }
}

private class StudioBridge(private val requestExport: (String) -> Boolean) {
    @JavascriptInterface
    fun exportPackage(payload: String): Boolean = requestExport(payload)
}

private fun Uri.isTrustedStudioUri(trustedHost: String): Boolean =
    scheme == "https" && encodedAuthority == trustedHost &&
        pathSegments.none { it == "." || it == ".." }

private fun blockedResponse() = WebResourceResponse(
    "text/plain", "utf-8", 403, "Forbidden", emptyMap(), ByteArrayInputStream(ByteArray(0)),
)
