package org.openremote.manager.client.assets;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface AssetDetailView extends IsWidget {

    interface Presenter {
        void goTo(Place place);

        void sendMessage();
    }

    void setPresenter(Presenter presenter);

    void setMessageText(String text);
}
