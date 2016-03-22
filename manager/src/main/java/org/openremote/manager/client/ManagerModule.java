package org.openremote.manager.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.openremote.manager.client.event.EventMapper;
import org.openremote.manager.client.event.bus.EventBus;
import org.openremote.manager.client.i18n.ManagerConstants;
import org.openremote.manager.client.i18n.ManagerMessages;
import org.openremote.manager.client.interop.keycloak.Keycloak;
import org.openremote.manager.client.app.*;
import org.openremote.manager.client.mvp.AppActivityManager;
import org.openremote.manager.client.mvp.AppPlaceController;
import org.openremote.manager.client.service.*;
import org.openremote.manager.client.map.*;
import org.openremote.manager.client.assets.*;
import org.openremote.manager.client.service.EventService;
import org.openremote.manager.server.event.*;
import org.openremote.manager.shared.map.MapResource;

public class ManagerModule extends AbstractGinModule {

    @Override
    protected void configure() {
        // App Wiring
        bind(com.google.web.bindery.event.shared.EventBus.class).to(com.google.web.bindery.event.shared.SimpleEventBus.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(PlaceHistoryMapper.class).to(ManagerHistoryMapper.class).in(Singleton.class);
        bind(AppController.class).to(AppControllerImpl.class).in(Singleton.class);

        // Views
        bind(HeaderView.class).to(HeaderViewImpl.class).in(Singleton.class);
        bind(AppLayout.class).to(AppLayoutImpl.class).in(Singleton.class);
        bind(MapView.class).to(MapViewImpl.class).in(Singleton.class);
        bind(AssetListView.class).to(AssetListViewImpl.class).in(Singleton.class);
        bind(AssetDetailView.class).to(AssetDetailViewImpl.class).in(Singleton.class);
        bind(LeftSideView.class).to(LeftSideViewImpl.class).in(Singleton.class);

        // Activities
        bind(AssetDetailActivity.class);
        bind(MapActivity.class);

        // Services
        bind(CookieService.class).to(CookieServiceImpl.class).in(Singleton.class);
        bind(ValidatorService.class).to(ValidatorServiceImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public EventService getEventBus(SecurityService securityService, EventBus eventBus, EventMapper eventMapper) {
        EventService eventService = EventServiceImpl.create(securityService, eventBus, eventMapper);
        eventService.connect();
        return eventService;
    }

    @Provides
    @Singleton
    public SecurityService getSecurityService(
            CookieService cookieService, EventBus eventBus) {
        return new SecurityServiceImpl(getKeyCloak(), cookieService, eventBus);
    }

    @Provides
    @Singleton
    public RequestService getRequestService(SecurityService securityService) {
        RequestServiceImpl.Configuration.setDefaults(securityService.getRealm());
        return new RequestServiceImpl(securityService);
    }

    @Provides
    @Singleton
    @Named("MainContentManager")
    public AppActivityManager getMainContentActivityMapper(MainContentActivityMapper activityMapper, EventBus eventBus) {
        return new AppActivityManager("MainContentManager", activityMapper, eventBus);
    }

    @Provides
    @Singleton
    @Named("LeftSideManager")
    public AppActivityManager getLeftSideActivityMapper(LeftSideActivityMapper activityMapper, EventBus eventBus) {
        return new AppActivityManager("LeftSideManager", activityMapper, eventBus);
    }

    @Provides
    @Singleton
    public ManagerConstants getConstants() {
        return GWT.create(ManagerConstants.class);
    }

    @Provides
    @Singleton
    public ManagerMessages getMessages() {
        return GWT.create(ManagerMessages.class);
    }

    public static native Keycloak getKeyCloak() /*-{
        return $wnd.keycloak;
    }-*/;

    @Provides
    @Singleton
    public MapResource getMapResource() {
        MapResource mapRestService = getNativeMapResource();
        return mapRestService;
    }

    public static native MapResource getNativeMapResource() /*-{
        return $wnd.MapResource;
    }-*/;

    @Provides
    @Singleton
    public PlaceController getPlaceController(SecurityService securityService,
                                              EventService eventService,
                                              EventBus eventBus,
                                              com.google.web.bindery.event.shared.EventBus legacyEventBus) {
        return new AppPlaceController(securityService, eventBus, legacyEventBus);
    }

    @Provides
    @Singleton
    public PlaceHistoryHandler getHistoryHandler(PlaceController placeController,
                                                 PlaceHistoryMapper historyMapper,
                                                 com.google.web.bindery.event.shared.EventBus legacyEventBus) {
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, legacyEventBus, new MapPlace());
        return historyHandler;
    }
}
