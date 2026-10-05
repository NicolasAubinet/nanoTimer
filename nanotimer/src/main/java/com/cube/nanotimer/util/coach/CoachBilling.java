package com.cube.nanotimer.util.coach;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.cube.nanotimer.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The coach subscription on Play: what this install has bought, read at every start of the app,
 * and the purchase of a base plan. What a purchase buys is the coach service's to say, from the
 * token {@link CoachAccess} keeps for {@link CoachClient} to send.
 *
 * <p>A purchase is acknowledged here as well as by the service. The service only looks at a
 * purchase when nothing else lets the install in and a request reaches it, and Play refunds one
 * nobody acknowledged within three days.
 */
public final class CoachBilling {

  /** The subscription's product id in Play Console, and its two base plans. */
  public static final String PRODUCT = "coach";
  public static final String MONTHLY = "monthly";
  public static final String YEARLY = "yearly";

  public interface Listener {
    /** Called on the main thread when {@link #getStatus} has something new to say. */
    void onBillingChanged();
  }

  private static final Handler MAIN = new Handler(Looper.getMainLooper());
  private static CoachBilling instance;

  private final Context context;
  private final CoachAccess access;
  private final BillingClient billing;
  private String status = "";
  private Listener listener;

  private CoachBilling(Context context) {
    this.context = context;
    this.access = CoachAccess.of(context);
    this.billing = BillingClient.newBuilder(context)
        .setListener(new PurchasesUpdatedListener() {
          @Override
          public void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {
            if (result.getResponseCode()
                == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
              restore(); // bought before, and for some reason not read at the start
            } else if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
              say("Purchase: " + describe(result));
            } else if (!take(purchases, true)) {
              say("Purchase: not bought yet (pending, or not the coach)");
            }
          }
        })
        // The library refuses to be built without it, though the coach sells no one-time product.
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build();
  }

  /** The billing of a build with a coach, or null in one without: no connection to Play is made. */
  public static synchronized CoachBilling of(Context context) {
    if (CoachClient.configured(context) == null) {
      return null;
    }
    if (instance == null) {
      instance = new CoachBilling(context.getApplicationContext());
    }
    return instance;
  }

  /** Reads what Play says this install has bought, so a subscriber is known without doing anything. */
  public static void onAppStart(Context context) {
    CoachBilling billing = of(context);
    if (billing != null) {
      billing.restore();
    }
  }

  /** How the last thing asked of Play went, in words for the debug row only. Main thread. */
  public String getStatus() {
    return status;
  }

  /** Main thread. */
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /** Lets go of a screen on its way out, unless a newer one has taken its place. */
  public void removeListener(Listener listener) {
    if (this.listener == listener) {
      this.listener = null;
    }
  }

  public void restore() {
    connected("Restore", new Runnable() {
      @Override
      public void run() {
        billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS).build(),
            new PurchasesResponseListener() {
              @Override
              public void onQueryPurchasesResponse(BillingResult result, List<Purchase> purchases) {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                  say("Restore: " + describe(result));
                } else if (!take(purchases, false)) {
                  // Kept as it was: the service says when a purchase has ended, not this list.
                  say("Restore: Play lists no coach purchase");
                }
              }
            });
      }
    });
  }

  /**
   * Opens Play's purchase of a base plan, with the offer {@link CoachOffers} picks for it. The
   * outcome arrives later, at the purchases listener.
   */
  public void buy(final Activity activity, final String basePlan) {
    connected("Buy " + basePlan, new Runnable() {
      @Override
      public void run() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT).setProductType(BillingClient.ProductType.SUBS).build();
        billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder()
            .setProductList(Collections.singletonList(product)).build(),
            new ProductDetailsResponseListener() {
              @Override
              public void onProductDetailsResponse(BillingResult result,
                  final QueryProductDetailsResult found) {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                  say("Buy " + basePlan + ": " + describe(result));
                  return;
                }
                MAIN.post(new Runnable() {
                  @Override
                  public void run() {
                    launch(activity, basePlan, found.getProductDetailsList());
                  }
                });
              }
            });
      }
    });
  }

  private void launch(Activity activity, String basePlan, List<ProductDetails> found) {
    if (found.isEmpty()) {
      say("Buy " + basePlan + ": Play does not sell \"" + PRODUCT + "\" to this app");
      return;
    }
    ProductDetails details = found.get(0);
    List<CoachOffers.Offer> offers = new ArrayList<CoachOffers.Offer>();
    List<ProductDetails.SubscriptionOfferDetails> listed = details.getSubscriptionOfferDetails();
    if (listed != null) {
      for (ProductDetails.SubscriptionOfferDetails offer : listed) {
        offers.add(new CoachOffers.Offer(offer.getBasePlanId(), offer.getOfferId(),
            offer.getOfferTags(), offer.getOfferToken()));
      }
    }
    CoachOffers.Offer offer = CoachOffers.pick(offers, basePlan);
    if (offer == null) {
      say("Buy " + basePlan + ": Play lists no such base plan");
      return;
    }
    if (activity.isFinishing() || activity.isDestroyed()) {
      return;
    }
    say("Buy " + basePlan + ": "
        + (offer.getOfferId() == null ? "no offer, the plain base plan" : "offer " + offer.getOfferId()));
    BillingResult result = billing.launchBillingFlow(activity, BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(Collections.singletonList(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details).setOfferToken(offer.getOfferToken()).build()))
        .build());
    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
      say("Buy " + basePlan + ": " + describe(result));
    }
  }

  /**
   * Keeps the coach purchase among those Play lists, if one is bought. A pending one is not sent:
   * the service would answer that it buys nothing, which reads as a subscription that ended.
   *
   * @return whether one was kept
   */
  private boolean take(List<Purchase> purchases, boolean justBought) {
    if (purchases == null) {
      return false;
    }
    for (Purchase purchase : purchases) {
      if (!purchase.getProducts().contains(PRODUCT)
          || purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
        continue;
      }
      boolean back = access.bought(purchase.getPurchaseToken(), justBought,
          System.currentTimeMillis());
      say((justBought ? "Purchase" : "Restore") + ": coach bought"
          + (purchase.isAcknowledged() ? "" : ", not acknowledged yet")
          + (back ? ", access is back" : ""));
      if (back) {
        // The refusal that ended it also held the weekly job back for a day.
        Options.INSTANCE.setCoachAskAgainAt(0);
        CoachPlanWorker.onAppStart(context);
      }
      if (!purchase.isAcknowledged()) {
        acknowledge(purchase);
      }
      return true;
    }
    return false;
  }

  private void acknowledge(Purchase purchase) {
    billing.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.getPurchaseToken()).build(),
        new AcknowledgePurchaseResponseListener() {
          @Override
          public void onAcknowledgePurchaseResponse(BillingResult result) {
            // A failure is tried again at the next start, the purchase still reading unacknowledged.
            say(result.getResponseCode() == BillingClient.BillingResponseCode.OK
                ? "Coach bought and acknowledged" : "Acknowledge: " + describe(result));
          }
        });
  }

  private void connected(final String what, final Runnable then) {
    if (billing.isReady()) {
      then.run();
      return;
    }
    billing.startConnection(new BillingClientStateListener() {
      @Override
      public void onBillingSetupFinished(BillingResult result) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          then.run();
        } else {
          say(what + ": " + describe(result));
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        // The client connects again by itself at the next thing asked of it.
      }
    });
  }

  private static String describe(BillingResult result) {
    String message = result.getDebugMessage();
    return "Play answered " + result.getResponseCode()
        + (message == null || message.isEmpty() ? "" : " (" + message + ")");
  }

  /** Play calls back on threads of its own. */
  private void say(final String words) {
    MAIN.post(new Runnable() {
      @Override
      public void run() {
        status = words;
        if (listener != null) {
          listener.onBillingChanged();
        }
      }
    });
  }
}
