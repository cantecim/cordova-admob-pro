package com.rjfun.cordova.admob;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;
import com.rjfun.cordova.ad.GenericAdPlugin;

public class AdMobPlugin extends GenericAdPlugin {
    private static final String LOGTAG = "AdMobPlugin";
    
    // options
    private static final String OPT_ADCOLONY = "AdColony";
    private static final String OPT_FLURRY = "Flurry";
    private static final String OPT_MMEDIA = "mMedia";
    private static final String OPT_INMOBI = "InMobi";
    private static final String OPT_FACEBOOK = "Facebook";
    private static final String OPT_MOBFOX = "MobFox";

    private static final String TEST_BANNER_ID = "ca-app-pub-6869992474017983/4748283957";
    private static final String TEST_INTERSTITIAL_ID = "ca-app-pub-6869992474017983/6225017153";

    private AdSize adSize = AdSize.SMART_BANNER;
    
    public static final String OPT_AD_EXTRAS = "adExtras";
	private JSONObject adExtras = null;

	public static final String OPT_LOCATION = "location";
    private Location mLocation = null;

	private HashMap<String, AdMobMediation> mediations = new HashMap<String, AdMobMediation>();
	
    @Override
    protected void pluginInitialize() {
    	super.pluginInitialize();
    	
    	// TODO: any init code
	}

	@Override
	protected String __getProductShortName() {
		return "AdMob";
	}
	
	@Override
	protected String __getTestBannerId() {
		return TEST_BANNER_ID;
	}

	@Override
	protected String __getTestInterstitialId() {
		return TEST_INTERSTITIAL_ID;
	}

	@Override
	public void setOptions(JSONObject options) {
		super.setOptions(options);
		
		if(options.has(OPT_AD_SIZE)) adSize = adSizeFromString(options.optString(OPT_AD_SIZE));
		if(adSize == null) {
			adSize = new AdSize(adWidth, adHeight);
		}
		
		if(options.has(OPT_AD_EXTRAS)) adExtras = options.optJSONObject(OPT_AD_EXTRAS);

		if(options.has(OPT_LOCATION)) {
			JSONArray location = options.optJSONArray(OPT_LOCATION);
			if(location != null) {
				mLocation = new Location("dummyprovider");
				mLocation.setLatitude( location.optDouble(0, 0.0) );
				mLocation.setLongitude( location.optDouble(1, 0) );
			}
		}
	}
	
	@Override
	protected View __createAdView(String adId) {
    	// Tip: The format for the DFP ad unit ID is: /networkCode/adUnitName
    	// example: "/6253334/dfp_example_ad"
    	if(adId.charAt(0) == '/') {
    		PublisherAdView ad = new PublisherAdView(getActivity());
    		ad.setAdUnitId(adId);
    		ad.setAdSizes(adSize);
    		ad.setAdListener(new BannerListener());
    		return ad;
    	} else {
            AdView ad = new AdView(getActivity());
            ad.setAdUnitId(adId);
            ad.setAdSize(adSize);
            ad.setAdListener(new BannerListener());
            return ad;
    	}
	}
	
	@Override
	protected void __loadAdView(View view) {
		if(view instanceof PublisherAdView) {
			PublisherAdView ad = (PublisherAdView) view;
        	ad.loadAd(new PublisherAdRequest.Builder().build());
		} else {
			AdView ad = (AdView) view;
        	ad.loadAd(buildAdRequest());
		}
	}
	
	protected AdSize getAdViewSize(View view) {
		if(view instanceof PublisherAdView) {
			PublisherAdView dfpView = (PublisherAdView) view;
        	return dfpView.getAdSize();
		} else {
			AdView admobView = (AdView) view;
        	return admobView.getAdSize();
		}
	}

	@Override
	protected int __getAdViewWidth(View view) {
        AdSize sz = getAdViewSize(view);
        return sz.getWidthInPixels(getActivity());
	}

	@Override
	protected int __getAdViewHeight(View view) {
        AdSize sz = getAdViewSize(view);
        return sz.getHeightInPixels(getActivity());
	}

	@Override
	protected void __pauseAdView(View view) {
		if(view == null) return;
		
		if(view instanceof PublisherAdView) {
			PublisherAdView dfpView = (PublisherAdView)view;
			dfpView.pause();
		} else {
			AdView admobView = (AdView)view;
			admobView.pause();
		}
	}

	@Override
	protected void __resumeAdView(View view) {
		if(view == null) return;
		
		if(view instanceof PublisherAdView) {
			PublisherAdView dfpView = (PublisherAdView)view;
			dfpView.resume();
		} else {
			AdView admobView = (AdView)view;
			admobView.resume();
		}
	}

	@Override
	protected void __destroyAdView(View view) {
		if(view == null) return;
		
		if(view instanceof PublisherAdView) {
			PublisherAdView dfpView = (PublisherAdView)view;
			dfpView.setAdListener(null);
			dfpView.destroy();
		} else {
			AdView admobView = (AdView)view;
			admobView.setAdListener(null);
			admobView.destroy();
		}
	}
	
	@Override
	protected Object __createInterstitial(String adId) {
		InterstitialAd ad = new InterstitialAd(getActivity());
        ad.setAdUnitId(adId);
        ad.setAdListener(new InterstitialListener());
        return ad;
	}
	
	@Override
	protected void __loadInterstitial(Object interstitial) {
		if(interstitial == null) return;
		
		if(interstitial instanceof InterstitialAd) {
			InterstitialAd ad = (InterstitialAd) interstitial;
			ad.loadAd(buildAdRequest());
		}
	}
	
	@Override
	protected void __showInterstitial(Object interstitial) {
		if(interstitial == null) return;
		
		if(interstitial instanceof InterstitialAd) {
			InterstitialAd ad = (InterstitialAd) interstitial;
			if(ad.isLoaded()) {
				ad.show();
			}
		}
	}

	@Override
	protected void __destroyInterstitial(Object interstitial) {
		if(interstitial == null) return;

		if(interstitial instanceof InterstitialAd) {
			InterstitialAd ad = (InterstitialAd) interstitial;
			ad.setAdListener(null);
		}
	}

	@Override
	public void showBanner(final int argPos, final int argX, final int argY) {
		Log.d("GenericAdPlugin", "showBanner");
		if(this.adView == null) {
			Log.e("GenericAdPlugin", "banner is null, call createBanner() first.");
		} else {
			final Activity activity = this.getActivity();
			activity.runOnUiThread(new Runnable() {
				public void run() {
					View mainView = AdMobPlugin.this.getView();
					ViewGroup adParent = (ViewGroup) AdMobPlugin.this.adView.getParent();
					if (adParent != null) {
						adParent.removeView(AdMobPlugin.this.adView);
					}

					int bw = AdMobPlugin.this.__getAdViewWidth(AdMobPlugin.this.adView);
					int bh = AdMobPlugin.this.__getAdViewHeight(AdMobPlugin.this.adView);
					ViewGroup rootView = (ViewGroup) mainView.getRootView();
					int rw = rootView.getWidth();
					int rh = rootView.getHeight();
					Log.w("GenericAdPlugin", "show banner, overlap:" + AdMobPlugin.this.overlap + ", position: " + argPos);
					if (AdMobPlugin.this.overlap) {
						int v = AdMobPlugin.this.posX;
						int parentView = AdMobPlugin.this.posY;
						int ww = mainView.getWidth();
						int wh = mainView.getHeight();
						if (argPos >= 1 && argPos <= 9) {
							switch ((argPos - 1) % 3) {
								case 0:
									v = 0;
									break;
								case 1:
									v = (ww - bw) / 2;
									break;
								case 2:
									v = ww - bw;
							}

							switch ((argPos - 1) / 3) {
								case 0:
									parentView = 0;
									break;
								case 1:
									parentView = (wh - bh) / 2;
									break;
								case 2:
									parentView = wh - bh;
							}
						} else if (argPos == 10) {
							v = argX;
							parentView = argY;
						}

						int[] offsetRootView = new int[2];
						int[] offsetWebView = new int[2];
						rootView.getLocationOnScreen(offsetRootView);
						mainView.getLocationOnScreen(offsetWebView);
						v += offsetWebView[0] - offsetRootView[0];
						parentView += offsetWebView[1] - offsetRootView[1];
						android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(bw, bh);
						params.leftMargin = v;
						params.topMargin = parentView;

                        float scale = AdMobPlugin.this.getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
                        params.leftMargin = Math.round(argX * scale) + offsetWebView[0] + offsetRootView[0];
                        params.topMargin = Math.round(argY * scale) + offsetWebView[1] + offsetWebView[0];
						if (AdMobPlugin.this.overlapLayout == null) {
							AdMobPlugin.this.overlapLayout = new RelativeLayout(activity);
							rootView.addView(AdMobPlugin.this.overlapLayout, new android.widget.RelativeLayout.LayoutParams(-1, -1));
							AdMobPlugin.this.overlapLayout.bringToFront();
						} else {
							AdMobPlugin.this.overlapLayout.bringToFront();
						}
                        Log.d(LOGTAG, "Ad X : " + params.leftMargin);
                        Log.d(LOGTAG, "Ad Y : " + params.topMargin);
                        Log.d(LOGTAG, "Ad W : " + params.width);
                        Log.d(LOGTAG, "Ad H : " + params.height);

						AdMobPlugin.this.overlapLayout.addView(AdMobPlugin.this.adView, params);
					} else {
						FrameLayout v1 = new FrameLayout(AdMobPlugin.this.getActivity());
						v1.addView(AdMobPlugin.this.adView, new android.widget.FrameLayout.LayoutParams(-2, -2));
						if (!AdMobPlugin.this.isWebViewInLinearLayout) {
							if (AdMobPlugin.this.originalParent == null) {
								AdMobPlugin.this.originalParent = (ViewGroup) mainView.getParent();
								AdMobPlugin.this.originalLayoutParams = mainView.getLayoutParams();
							}

							if (AdMobPlugin.this.splitLayout == null) {
								AdMobPlugin.this.splitLayout = new LinearLayout(activity);
								AdMobPlugin.this.splitLayout.setOrientation(LinearLayout.VERTICAL);
								AdMobPlugin.this.originalParent.addView(AdMobPlugin.this.splitLayout, new android.widget.FrameLayout.LayoutParams(-1, -1));
								AdMobPlugin.this.splitLayout.bringToFront();
							}

							AdMobPlugin.this.splitLayout.removeAllViews();
							AdMobPlugin.this.originalParent.removeView(mainView);
							AdMobPlugin.this.splitLayout.addView(mainView, new android.widget.LinearLayout.LayoutParams(-1, AdMobPlugin.this.originalParent.getHeight() - bh));
						}

						ViewGroup parentView1 = (ViewGroup) mainView.getParent();
						if (argPos <= 3) {
							parentView1.addView(v1, 0);
						} else {
							parentView1.addView(v1);
						}
					}

					AdMobPlugin.this.adView.setVisibility(View.VISIBLE);
					AdMobPlugin.this.adView.bringToFront();
					AdMobPlugin.this.__resumeAdView(AdMobPlugin.this.adView);
					AdMobPlugin.this.bannerVisible = true;
				}
			});
		}
	}

    @SuppressLint("DefaultLocale")
	private AdRequest buildAdRequest() {
	    final Activity activity = getActivity();
        AdRequest.Builder builder = new AdRequest.Builder();
        
        if (isTesting) {
            // This will request test ads on the emulator and deviceby passing this hashed device ID.
        	String ANDROID_ID = Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            String deviceId = md5(ANDROID_ID).toUpperCase();
            builder = builder.addTestDevice(deviceId).addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }

        if(adExtras != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("cordova", 1);
            Iterator<String> it = adExtras.keys(); 
            while (it.hasNext()) {
                String key = it.next();
                try {
                    bundle.putString(key, adExtras.get(key).toString());
                } catch (JSONException exception) {
                    Log.w(LOGTAG, String.format("Caught JSON Exception: %s", exception.getMessage()));
                }
            }
            builder = builder.addNetworkExtras( new AdMobExtras(bundle) );
        }
        
        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
        	String key = it.next();
        	AdMobMediation m = mediations.get(key);
        	if(m != null) {
        		builder = m.joinAdRequest(builder);
        	}
        }

        if(mLocation != null) builder.setLocation(mLocation);

        return builder.build();
    }

    @Override
    public void onPause(boolean multitasking) {
        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
        	String key = it.next();
        	AdMobMediation m = mediations.get(key);
        	if(m != null) m.onPause();
        }

        super.onPause(multitasking);
    }
    
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
        	String key = it.next();
        	AdMobMediation m = mediations.get(key);
        	if(m != null) m.onResume();
        }
    }
    
    @Override
    public void onDestroy() {
        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
        	String key = it.next();
        	AdMobMediation m = mediations.get(key);
        	if(m != null) m.onDestroy();
        }
        super.onDestroy();
    }
    
    /**
     * Gets an AdSize object from the string size passed in from JavaScript.
     * Returns null if an improper string is provided.
     *
     * @param size The string size representing an ad format constant.
     * @return An AdSize object used to create a banner.
     */
    public static AdSize adSizeFromString(String size) {
        if ("BANNER".equals(size)) {
            return AdSize.BANNER;
        } else if ("SMART_BANNER".equals(size)) {
            return AdSize.SMART_BANNER;
        } else if ("MEDIUM_RECTANGLE".equals(size)) {
            return AdSize.MEDIUM_RECTANGLE;
        } else if ("FULL_BANNER".equals(size)) {
            return AdSize.FULL_BANNER;
        } else if ("LEADERBOARD".equals(size)) {
            return AdSize.LEADERBOARD;
        } else if ("SKYSCRAPER".equals(size)) {
            return AdSize.WIDE_SKYSCRAPER;
        } else {
            return null;
        }
    }
    
    /**
     * document.addEventListener('onAdLoaded', function(data));
     * document.addEventListener('onAdFailLoad', function(data));
     * document.addEventListener('onAdPresent', function(data));
     * document.addEventListener('onAdDismiss', function(data));
     * document.addEventListener('onAdLeaveApp', function(data));
     */
    private class BannerListener extends AdListener {
        @SuppressLint("DefaultLocale")
		@Override
        public void onAdFailedToLoad(int errorCode) {
        	fireAdErrorEvent(EVENT_AD_FAILLOAD, errorCode, getErrorReason(errorCode), ADTYPE_BANNER);
        }
        
        @Override
        public void onAdLeftApplication() {
        	fireAdEvent(EVENT_AD_LEAVEAPP, ADTYPE_BANNER);
        }
        
        @Override
        public void onAdLoaded() {
            if((! bannerVisible) && autoShowBanner) {
            	showBanner(adPosition, posX, posY);
            }
        	fireAdEvent(EVENT_AD_LOADED, ADTYPE_BANNER);
        }

        @Override
        public void onAdOpened() {
        	fireAdEvent(EVENT_AD_PRESENT, ADTYPE_BANNER);
        }
        
        @Override
        public void onAdClosed() {
        	fireAdEvent(EVENT_AD_DISMISS, ADTYPE_BANNER);
        }
        
    }
    
    /**
     * document.addEventListener('onAdLoaded', function(data));
     * document.addEventListener('onAdFailLoad', function(data));
     * document.addEventListener('onAdPresent', function(data));
     * document.addEventListener('onAdDismiss', function(data));
     * document.addEventListener('onAdLeaveApp', function(data));
     */
   private class InterstitialListener extends AdListener {
        @SuppressLint("DefaultLocale")
		@Override
        public void onAdFailedToLoad(int errorCode) {
        	fireAdErrorEvent(EVENT_AD_FAILLOAD, errorCode, getErrorReason(errorCode), ADTYPE_INTERSTITIAL);
        }
        
        @Override
        public void onAdLeftApplication() {
        	fireAdEvent(EVENT_AD_LEAVEAPP, ADTYPE_INTERSTITIAL);
        }
        
        @Override
        public void onAdLoaded() {
            if(autoShowInterstitial) {
            	showInterstitial();
            }
        	
        	fireAdEvent(EVENT_AD_LOADED, ADTYPE_INTERSTITIAL);
        }

        @Override
        public void onAdOpened() {
        	fireAdEvent(EVENT_AD_PRESENT, ADTYPE_INTERSTITIAL);
        }
        
        @Override
        public void onAdClosed() {
        	fireAdEvent(EVENT_AD_DISMISS, ADTYPE_INTERSTITIAL);

        	removeInterstitial();
        }
        
    }
   
   /** Gets a string error reason from an error code. */
   public String getErrorReason(int errorCode) {
     String errorReason = "";
     switch(errorCode) {
       case AdRequest.ERROR_CODE_INTERNAL_ERROR:
         errorReason = "Internal error";
         break;
       case AdRequest.ERROR_CODE_INVALID_REQUEST:
         errorReason = "Invalid request";
         break;
       case AdRequest.ERROR_CODE_NETWORK_ERROR:
         errorReason = "Network Error";
         break;
       case AdRequest.ERROR_CODE_NO_FILL:
         errorReason = "No fill";
         break;
     }
     return errorReason;
   }
   

}
