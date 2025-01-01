package bisq.desktop.components.paymentmethods;

import bisq.desktop.main.overlays.popups.Popup;

import bisq.core.locale.FiatCurrency;
import bisq.core.locale.Res;
import bisq.core.locale.TradeCurrency;

public class ArsBlueRatePopup {
    public static boolean isTradeCurrencyArgentinePesos(TradeCurrency tradeCurrency) {
        FiatCurrency arsCurrency = new FiatCurrency("ARS");
        return tradeCurrency.equals(arsCurrency);
    }

    public static void show() {
        new Popup()
                .headLine(Res.get("popup.arsBlueMarket.title"))
                .information(Res.get("popup.arsBlueMarket.info"))
                .actionButtonText(Res.get("shared.iUnderstand"))
                .hideCloseButton()
                .show();
    }
}
