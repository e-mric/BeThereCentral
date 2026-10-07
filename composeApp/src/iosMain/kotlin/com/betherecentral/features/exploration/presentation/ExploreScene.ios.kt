package com.betherecentral.features.exploration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol

actual val supportsExploreScene: Boolean = true

@Composable
@OptIn(ExperimentalForeignApi::class)
actual fun ExploreScene(modifier: Modifier) {
    val sceneDirectory = NSBundle.mainBundle.URLForResource("index", withExtension = "html", subdirectory = "exploration")
        ?.URLByDeletingLastPathComponent
    val allowedPath = sceneDirectory?.path
    val navigationDelegate = remember(allowedPath) {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit,
            ) {
                val url = decidePolicyForNavigationAction.request.URL
                val allowed = (url?.isFileURL() == true && allowedPath != null &&
                    url.path?.startsWith("$allowedPath/") == true) ||
                    (allowedPath == null && url?.absoluteString == "about:blank")
                decisionHandler(if (allowed) WKNavigationActionPolicy.WKNavigationActionPolicyAllow else WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            }
        }
    }
    UIKitView(
        modifier = modifier,
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true),
        factory = {
            WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = WKWebViewConfiguration()).apply {
                opaque = false
                backgroundColor = platform.UIKit.UIColor.colorWithRed(12.0 / 255.0, 17.0 / 255.0, 20.0 / 255.0, 1.0)
                scrollView.bounces = false
                this.navigationDelegate = navigationDelegate
                val htmlUrl = NSBundle.mainBundle.URLForResource("index", withExtension = "html", subdirectory = "exploration")
                if (htmlUrl != null && sceneDirectory != null) {
                    loadFileURL(htmlUrl, allowingReadAccessToURL = sceneDirectory)
                } else {
                    loadHTMLString("<html><body style='background:#0c1114;color:#f5f2eb;font:16px system-ui;padding:24px'><h1>Sample scene unavailable</h1><p>Use ‹ Map to continue.</p></body></html>", baseURL = null)
                }
            }
        },
        update = { },
        onRelease = { webView ->
            webView.stopLoading()
            webView.navigationDelegate = null
            webView.loadHTMLString("", baseURL = null)
        },
    )
}
