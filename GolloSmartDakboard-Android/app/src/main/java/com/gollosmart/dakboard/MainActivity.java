package com.gollosmart.dakboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Abre GolloSmartDakboard a pantalla completa dentro de un WebView.
 * - Mantiene la pantalla encendida.
 * - Si no hay internet, muestra un aviso y reintenta cada 15 segundos.
 * - Menú (tecla Menú, o mantener pulsado Atrás): recargar, cambiar dirección,
 *   borrar datos guardados y salir.
 * - Atrás dos veces seguidas: salir.
 */
public class MainActivity extends Activity {

    private static final String PREFS = "gollosmartdakboard";
    private static final String KEY_URL = "url";
    private static final long RETRY_MS = 15000;

    private WebView web;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastBack = 0;
    private boolean offlineShown = false;

    private final Runnable retry = new Runnable() {
        @Override
        public void run() {
            offlineShown = false;
            if (web != null) {
                web.loadUrl(currentUrl());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        setContentView(web, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        // Permite que la página sepa que corre dentro de la app de TV.
        s.setUserAgentString(s.getUserAgentString() + " GolloSmartTV");

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                showOffline();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url != null && url.startsWith("http")) {
                    offlineShown = false;
                    handler.removeCallbacks(retry);
                }
            }
        });

        web.loadUrl(currentUrl());
        web.requestFocus();
    }

    private String currentUrl() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        return p.getString(KEY_URL, getString(R.string.default_url));
    }

    private void showOffline() {
        if (offlineShown) {
            return;
        }
        offlineShown = true;
        String html = "<html><body style=\"margin:0;background:#0e1514;color:#e8efec;"
                + "font-family:sans-serif;display:flex;align-items:center;justify-content:center;"
                + "height:100vh;text-align:center\"><div><h1>GolloSmartDakboard</h1>"
                + "<p>Sin conexi\u00f3n. Reintentando\u2026</p>"
                + "<p style=\"opacity:.6\">Mant\u00e9n pulsado Atr\u00e1s para abrir el men\u00fa</p></div></body></html>";
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        handler.removeCallbacks(retry);
        handler.postDelayed(retry, RETRY_MS);
    }

    private void showMenu() {
        final String[] items = {
                "Recargar",
                "Cambiar direcci\u00f3n",
                "Borrar datos guardados",
                "Salir de la app"
        };
        new AlertDialog.Builder(this)
                .setTitle("GolloSmart Dakboard")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                offlineShown = false;
                                web.loadUrl(currentUrl());
                                break;
                            case 1:
                                changeUrl();
                                break;
                            case 2:
                                web.clearCache(true);
                                WebStorage.getInstance().deleteAllData();
                                CookieManager.getInstance().removeAllCookies(null);
                                Toast.makeText(MainActivity.this, "Datos borrados", Toast.LENGTH_SHORT).show();
                                offlineShown = false;
                                web.loadUrl(currentUrl());
                                break;
                            default:
                                finish();
                        }
                    }
                })
                .show();
    }

    private void changeUrl() {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(currentUrl());
        new AlertDialog.Builder(this)
                .setTitle("Direcci\u00f3n de la pantalla")
                .setView(input)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String u = input.getText().toString().trim();
                        if (u.length() == 0) {
                            return;
                        }
                        if (!u.startsWith("http://") && !u.startsWith("https://")) {
                            u = "https://" + u;
                        }
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URL, u).apply();
                        offlineShown = false;
                        web.loadUrl(u);
                    }
                })
                .setNeutralButton("Restablecer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_URL).apply();
                        offlineShown = false;
                        web.loadUrl(currentUrl());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            showMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showMenu();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
            long now = System.currentTimeMillis();
            if (now - lastBack < 2000) {
                finish();
            } else {
                lastBack = now;
                Toast.makeText(this, "Pulsa Atr\u00e1s otra vez para salir. Mant\u00e9n Atr\u00e1s para el men\u00fa.",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(retry);
        if (web != null) {
            ViewGroup parent = (ViewGroup) web.getParent();
            if (parent != null) {
                parent.removeView(web);
            }
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
