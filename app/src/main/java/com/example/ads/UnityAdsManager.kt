package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsManager {
    private const val TAG = "UnityAdsManager"
    const val GAME_ID = "6157767"
    const val BANNER_PLACEMENT_ID = "Banner_Android"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"
    
    // Unity Ads test mode. Set to true for integration testing
    private const val TEST_MODE = true

    var isInitialized = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return

        UnityAds.initialize(context.applicationContext, GAME_ID, TEST_MODE, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isInitialized = true
                Log.d(TAG, "Unity Ads Initialization Complete.")
                loadRewardedAd()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError, message: String) {
                isInitialized = false
                Log.e(TAG, "Unity Ads Initialization Failed: [$error] $message")
            }
        })
    }

    // Pre-loads the Rewarded Ad so it is ready when clicked
    fun loadRewardedAd() {
        if (!isInitialized) return
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Rewarded Ad Loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Rewarded Ad Failed to Load: [$error] $message")
            }
        })
    }

    // Shows a Rewarded Ad, with callback to continue to player
    fun showRewardedAd(activity: Activity, onAdFinished: () -> Unit) {
        if (!isInitialized) {
            Log.e(TAG, "Unity Ads not initialized. Playing directly.")
            onAdFinished()
            return
        }

        // We attempt to show the rewarded ad. If it fails or is not ready, we proceed to watch the video
        UnityAds.show(activity, REWARDED_PLACEMENT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Rewarded Ad Show Failed: [$error] $message. Proceeding directly.")
                // Preload next
                loadRewardedAd()
                // Proceed to watch
                onAdFinished()
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Rewarded Ad Show Started.")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Rewarded Ad Clicked.")
            }

            override fun onUnityAdsShowComplete(
                placementId: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                Log.d(TAG, "Rewarded Ad Show Complete with state: $state")
                // Preload next
                loadRewardedAd()
                // Proceed to watch
                onAdFinished()
            }
        })
    }

    // Dynamically creates a Unity Ads Banner View
    fun createBannerView(activity: Activity): BannerView {
        val bannerView = BannerView(activity, BANNER_PLACEMENT_ID, UnityBannerSize(320, 50))
        bannerView.listener = object : BannerView.IListener {
            override fun onBannerLoaded(bannerView: BannerView?) {
                Log.d(TAG, "Banner Loaded successfully.")
            }

            override fun onBannerFailedToLoad(bannerView: BannerView?, errorInfo: BannerErrorInfo?) {
                Log.e(TAG, "Banner Failed to Load: ${errorInfo?.errorMessage}")
            }

            override fun onBannerClick(bannerView: BannerView?) {
                Log.d(TAG, "Banner Clicked.")
            }

            override fun onBannerShown(bannerView: BannerView?) {
                Log.d(TAG, "Banner Shown.")
            }

            override fun onBannerLeftApplication(bannerView: BannerView?) {
                Log.d(TAG, "Banner left application.")
            }
        }
        bannerView.load()
        return bannerView
    }
}
