package com.example.wordapp

import android.content.Context
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false).apply()

        // 获取 WebView 对象
        val webView: WebView = findViewById(R.id.webview)
        val BackButton: ImageButton =findViewById(R.id.back)
        BackButton.setOnClickListener { finish() }

        // 配置 WebView 设置
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true  // 启用 JavaScript

        // 设置 WebViewClient 使得点击链接时不跳转到外部浏览器
        webView.webViewClient = WebViewClient()

        // 加载 URL
        webView.loadUrl("https://www.doubao.com/")
    }

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webview)
        // 如果 WebView 有历史记录，返回上一页
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}