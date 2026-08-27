package com.yapayzekapolat1.bankakur;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.getcapacitor.BridgeActivity;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;

/**
 * iOZ Bank Tycoon - Unity Ads koprusu.
 *
 * Oyunun web (JS) tarafi zaten su fonksiyonlari cagirmaya hazir sekilde yazildi:
 *   - window.AndroidAds.showRewardedAd()        -> bu siniftan JS'e acilan arayuz
 *   - window.onAdRewardEarned()                 -> odul kazanildiginda JS'e bildirim
 *   - window.onAdFailedOrClosed()                -> reklam basarisiz/iptal oldugunda JS'e bildirim
 *
 * Native tarafta yapilmasi gereken tek sey Unity Ads SDK'sini baslatmak ve
 * bu iki JS fonksiyonunu dogru zamanda cagirmak. Asagidaki sinif bunu yapiyor.
 */
public class MainActivity extends BridgeActivity {

    private static final String TAG = "UnityAdsBridge";

    // cloud.unity.com Organization -> Game panelinden alinan degerler
    private static final String GAME_ID = "800362657";
    private static final String PLACEMENT_ID = "Rewarded_Android";

    // Yayina cikmadan once mutlaka false yapin. true iken sadece test reklamlari gosterilir.
    private static final boolean TEST_MODE = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UnityAds.initialize(getApplicationContext(), GAME_ID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                Log.d(TAG, "Unity Ads basariyla baslatildi.");
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                Log.e(TAG, "Unity Ads baslatilamadi: " + error + " - " + message);
            }
        });

        this.bridge.getWebView().addJavascriptInterface(new AndroidAdsBridge(), "AndroidAds");
    }

    /** WebView icindeki JS'in "window.AndroidAds" olarak gordugu obje. */
    private class AndroidAdsBridge {

        @JavascriptInterface
        public void showRewardedAd() {
            runOnUiThread(() -> {
                UnityAds.load(PLACEMENT_ID, new IUnityAdsLoadListener() {
                    @Override
                    public void onUnityAdsAdLoaded(String placementId) {
                        UnityAds.show(
                            MainActivity.this,
                            placementId,
                            new UnityAdsShowOptions(),
                            new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                    Log.e(TAG, "Reklam gosterilemedi: " + message);
                                    notifyFail();
                                }

                                @Override
                                public void onUnityAdsShowStart(String placementId) {
                                    // Reklam ekrana geldi, ekstra islem gerekmiyor
                                }

                                @Override
                                public void onUnityAdsShowClick(String placementId) {
                                    // Reklama tiklandi, ekstra islem gerekmiyor
                                }

                                @Override
                                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                        notifyReward();
                                    } else {
                                        // Oyuncu reklami sonuna kadar izlemeden kapattiysa odul verilmiyor
                                        notifyFail();
                                    }
                                }
                            }
                        );
                    }

                    @Override
                    public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                        Log.e(TAG, "Reklam yuklenemedi: " + message);
                        notifyFail();
                    }
                });
            });
        }
    }

    private void notifyReward() {
        runOnUiThread(() ->
            this.bridge.getWebView().evaluateJavascript(
                "window.onAdRewardEarned && window.onAdRewardEarned();", null)
        );
    }

    private void notifyFail() {
        runOnUiThread(() ->
            this.bridge.getWebView().evaluateJavascript(
                "window.onAdFailedOrClosed && window.onAdFailedOrClosed();", null)
        );
    }
                              }
