package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.WebCommand
import com.example.ui.WebViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    viewModel: WebViewModel,
    command: WebCommand?,
    modifier: Modifier = Modifier,
    onExitRequested: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    // Intercept back button: if can go back in WebView history, go back; otherwise trigger exit popup
    BackHandler(enabled = true) {
        webViewInstance?.let { wv ->
            if (wv.canGoBack()) {
                wv.goBack()
            } else {
                onExitRequested?.invoke()
            }
        } ?: run {
            onExitRequested?.invoke()
        }
    }

    // Execute incoming commands
    LaunchedEffect(command) {
        val wv = webViewInstance ?: return@LaunchedEffect
        when (command) {
            is WebCommand.LoadUrl -> {
                wv.loadUrl(command.url)
                viewModel.clearWebCommand()
            }
            is WebCommand.Reload -> {
                wv.reload()
                viewModel.clearWebCommand()
            }
            is WebCommand.StopLoading -> {
                wv.stopLoading()
                viewModel.clearWebCommand()
            }
            is WebCommand.GoBack -> {
                if (wv.canGoBack()) {
                    wv.goBack()
                }
                viewModel.clearWebCommand()
            }
            is WebCommand.GoForward -> {
                if (wv.canGoForward()) {
                    wv.goForward()
                }
                viewModel.clearWebCommand()
            }
            null -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    // Ensure standard mobile user agent and viewport rendering
                    userAgentString = userAgentString.replace("; wv", "") // Ensure full mobile browser capability
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return false
                        val urlStr = uri.toString()

                        // Let HTTP / HTTPS stay in the WebView
                        if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                            return false
                        }

                        // Open external schemes (tel:, mailto:, geo:, etc.)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignored if no app to handle
                        }
                        return true
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { viewModel.onPageStarted(it) }
                        view?.let {
                            canGoBack = it.canGoBack()
                            viewModel.onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val title = view?.title
                        url?.let { viewModel.onPageFinished(it, title) }
                        view?.let {
                            canGoBack = it.canGoBack()
                            viewModel.onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        // Only report for main frame requests
                        if (request?.isForMainFrame == true) {
                            val desc = error?.description?.toString() ?: "Network error"
                            viewModel.onReceivedError(desc)
                        }
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?
                    ): Boolean {
                        // Return true to indicate the app has handled the renderer termination,
                        // preventing an application-level crash (fatal signal / SIGKILL).
                        view?.let { wv ->
                            val currentUrl = wv.url ?: viewModel.uiState.value.currentUrl
                            wv.post {
                                try {
                                    wv.destroy()
                                } catch (ignored: Exception) {
                                }
                                viewModel.onReceivedError("Web page renderer reloaded. Tap retry to reload.")
                            }
                        }
                        return true
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        viewModel.onProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let { t ->
                            val currentUrl = view?.url ?: ""
                            viewModel.onPageFinished(currentUrl, t)
                        }
                    }
                }

                // Initial load
                loadUrl(viewModel.uiState.value.currentUrl)
                webViewInstance = this
            }
        },
        update = { wv ->
            webViewInstance = wv
        }
    )
}
