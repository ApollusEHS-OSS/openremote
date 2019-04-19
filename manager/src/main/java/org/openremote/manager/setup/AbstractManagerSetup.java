/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.setup;

import org.openremote.agent.protocol.macro.MacroAction;
import org.openremote.agent.protocol.macro.MacroProtocol;
import org.openremote.agent.protocol.timer.TimerValue;
import org.openremote.container.Container;
import org.openremote.manager.asset.AssetProcessingService;
import org.openremote.manager.asset.AssetStorageService;
import org.openremote.manager.concurrent.ManagerExecutorService;
import org.openremote.manager.datapoint.AssetDatapointService;
import org.openremote.manager.persistence.ManagerPersistenceService;
import org.openremote.manager.rules.RulesetStorageService;
import org.openremote.manager.security.ManagerIdentityService;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetAttribute;
import org.openremote.model.attribute.*;
import org.openremote.model.value.Values;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.openremote.agent.protocol.macro.MacroProtocol.META_MACRO_ACTION_INDEX;
import static org.openremote.agent.protocol.timer.TimerConfiguration.initTimerConfiguration;
import static org.openremote.agent.protocol.timer.TimerProtocol.META_TIMER_VALUE_LINK;
import static org.openremote.model.attribute.MetaItemType.*;
import static org.openremote.model.asset.AssetType.*;
import static org.openremote.model.asset.agent.ProtocolConfiguration.initProtocolConfiguration;
import static org.openremote.model.attribute.AttributeValueType.*;

public abstract class AbstractManagerSetup implements Setup {

    final protected ManagerExecutorService executorService;
    final protected ManagerPersistenceService persistenceService;
    final protected ManagerIdentityService identityService;
    final protected AssetStorageService assetStorageService;
    final protected AssetProcessingService assetProcessingService;
    final protected AssetDatapointService assetDatapointService;
    final protected RulesetStorageService rulesetStorageService;
    final protected SetupService setupService;

    public AbstractManagerSetup(Container container) {
        this.executorService = container.getService(ManagerExecutorService.class);
        this.persistenceService = container.getService(ManagerPersistenceService.class);
        this.identityService = container.getService(ManagerIdentityService.class);
        this.assetStorageService = container.getService(AssetStorageService.class);
        this.assetProcessingService = container.getService(AssetProcessingService.class);
        this.assetDatapointService = container.getService(AssetDatapointService.class);
        this.rulesetStorageService = container.getService(RulesetStorageService.class);
        this.setupService = container.getService(SetupService.class);
    }

    // ################################ Demo apartment with complex scenes ###################################

    protected Asset createDemoApartment(Asset parent, String name) {
        Asset apartment = new Asset(name, RESIDENCE, parent);
        apartment.setAttributes(
            new AssetAttribute("alarmEnabled", AttributeValueType.BOOLEAN)
                .setMeta(new Meta(
                    new MetaItem(LABEL, Values.create("Alarm enabled")),
                    new MetaItem(DESCRIPTION, Values.create("Send notifications when presence is detected")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true))
                )),
            new AssetAttribute("presenceDetected", AttributeValueType.BOOLEAN)
                .setMeta(new Meta(
                    new MetaItem(LABEL, Values.create("Presence detected")),
                    new MetaItem(DESCRIPTION, Values.create("Presence detected in any room")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(STORE_DATA_POINTS, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true))
                )),
            new AssetAttribute("vacationUntil", TIMESTAMP_MILLIS)
                .setMeta(new Meta(
                    new MetaItem(LABEL, Values.create("Vacation until")),
                    new MetaItem(DESCRIPTION, Values.create("Vacation mode enabled until")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                )),
            new AssetAttribute("lastExecutedScene", AttributeValueType.STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Last executed scene")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true))
                )
            /* TODO Unused, can be removed? Port schedule prediction from DRL...
            new AssetAttribute("autoSceneSchedule", AttributeValueType.BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Automatic scene schedule")),
                    new MetaItem(DESCRIPTION, Values.create("Predict presence and automatically adjust scene schedule")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lastDetectedScene", AttributeValueType.STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Last detected scene by rules")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
            */
        );
        return apartment;
    }

    protected Asset createDemoApartmentRoom(Asset apartment, String name) {
        Asset room = new Asset(name, ROOM, apartment);
        return room;
    }

    protected void addDemoApartmentRoomMotionSensor(Asset room, boolean shouldBeLinked, Supplier<MetaItem[]> agentLinker) {
        room.addAttributes(
            new AssetAttribute("motionSensor", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Motion sensor")),
                    new MetaItem(DESCRIPTION, Values.create("Greater than zero when motion is sensed")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null),
            new AssetAttribute("presenceDetected", AttributeValueType.BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Presence detected")),
                    new MetaItem(DESCRIPTION, Values.create("Someone is moving or resting in the room")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(STORE_DATA_POINTS, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true))
                ),
            new AssetAttribute("firstPresenceDetected", TIMESTAMP_MILLIS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("First time movement was detected")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lastPresenceDetected", TIMESTAMP_MILLIS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Last time movement was detected")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );
    }

    protected void addDemoApartmentRoomCO2Sensor(Asset room, boolean shouldBeLinked, Supplier<MetaItem[]> agentLinker) {
        room.addAttributes(
            new AssetAttribute("co2Level", CO2_PPM)
                .setMeta(
                    new MetaItem(LABEL, Values.create("CO2 level")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(RULE_EVENT, Values.create(true)),
                    new MetaItem(RULE_EVENT_EXPIRES, Values.create("45m")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(STORE_DATA_POINTS, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true)),
                    new MetaItem(FORMAT, Values.create("%4d ppm"))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null)
        );
    }

    protected void addDemoApartmentRoomHumiditySensor(Asset room, boolean shouldBeLinked, Supplier<MetaItem[]> agentLinker) {
        room.addAttributes(
            new AssetAttribute("humidity", HUMIDITY_PERCENTAGE)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Humidity")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(RULE_EVENT, Values.create(true)),
                    new MetaItem(RULE_EVENT_EXPIRES, Values.create("45m")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(STORE_DATA_POINTS, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true)),
                    new MetaItem(FORMAT, Values.create("%3d %%"))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null)
        );
    }

    protected void addDemoApartmentRoomThermometer(Asset room,
                                                   boolean shouldBeLinked,
                                                   Supplier<MetaItem[]> agentLinker) {
        room.addAttributes(
            new AssetAttribute("currentTemperature", TEMPERATURE_CELCIUS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Current temperature")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(STORE_DATA_POINTS, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true)),
                    new MetaItem(FORMAT, Values.create("%0.1f° C"))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null)
        );
    }

    protected void addDemoApartmentTemperatureControl(Asset room,
                                                      boolean shouldBeLinked,
                                                      Supplier<MetaItem[]> agentLinker) {
        room.addAttributes(
            new AssetAttribute("targetTemperature", TEMPERATURE_CELCIUS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Target temperature")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true)),
                    new MetaItem(FORMAT, Values.create("%0f° C"))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null)
        );
    }

    protected void addDemoApartmentSmartSwitch(Asset room,
                                               String switchName,
                                               boolean shouldBeLinked,
                                               // Integer represents attribute:
                                               // 0 = Mode
                                               // 1 = Time
                                               // 2 = StartTime
                                               // 3 = StopTime
                                               // 4 = Enabled
                                               Function<Integer, MetaItem[]> agentLinker) {

        room.addAttributes(
            // Mode
            new AssetAttribute("smartSwitchMode" + switchName, STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Smart Switch mode " + switchName)),
                    new MetaItem(DESCRIPTION, Values.create("NOW_ON (default when empty) or ON_AT or READY_AT")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(RULE_EVENT, Values.create(true)),
                    new MetaItem(RULE_EVENT_EXPIRES, Values.create("48h"))
                ).addMeta(shouldBeLinked ? agentLinker.apply(0) : null),
            // Time
            new AssetAttribute("smartSwitchBeginEnd" + switchName, TIMESTAMP_MILLIS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Smart Switch begin/end cycle " + switchName)),
                    new MetaItem(DESCRIPTION, Values.create("User-provided begin/end time of appliance cycle")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(shouldBeLinked ? agentLinker.apply(1) : null),
            // StartTime
            new AssetAttribute("smartSwitchStartTime" + switchName, TIMESTAMP_SECONDS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Smart Switch actuator earliest start time " + switchName)),
                    new MetaItem(DESCRIPTION, Values.create("Earliest computed start time sent to actuator")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(shouldBeLinked ? agentLinker.apply(2) : null),
            // StopTime
            new AssetAttribute("smartSwitchStopTime" + switchName, TIMESTAMP_SECONDS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Smart Switch actuator latest stop time " + switchName)),
                    new MetaItem(DESCRIPTION, Values.create("Latest computed stop time sent to actuator")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(shouldBeLinked ? agentLinker.apply(3) : null),
            // Enabled
            new AssetAttribute("smartSwitchEnabled" + switchName, NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Smart Switch actuator enabled " + switchName)),
                    new MetaItem(DESCRIPTION, Values.create("1 if actuator only provides power at ideal time between start/stop")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(shouldBeLinked ? agentLinker.apply(4) : null)
        );
    }

    protected void addDemoApartmentVentilation(Asset apartment,
                                               boolean shouldBeLinked,
                                               Supplier<MetaItem[]> agentLinker) {
        apartment.addAttributes(
            new AssetAttribute("ventilationLevel", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Ventilation level")),
                    new MetaItem(RANGE_MIN, Values.create(0)),
                    new MetaItem(RANGE_MAX, Values.create(255)),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(FORMAT, Values.create("%d"))
                ).addMeta(shouldBeLinked ? agentLinker.get() : null),
            new AssetAttribute("ventilationAuto", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Ventilation auto")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true))
                )
        );
    }

    public class Scene {

        final String attributeName;
        final String attributeLabel;
        final String internalName;
        final String startTime;
        final boolean alarmEnabled;
        final double targetTemperature;

        public Scene(String attributeName,
                     String attributeLabel,
                     String internalName,
                     String startTime,
                     boolean alarmEnabled,
                     double targetTemperature) {
            this.attributeName = attributeName;
            this.attributeLabel = attributeLabel;
            this.internalName = internalName;
            this.startTime = startTime;
            this.alarmEnabled = alarmEnabled;
            this.targetTemperature = targetTemperature;
        }

        AssetAttribute createMacroAttribute(Asset apartment, Asset... rooms) {
            AssetAttribute attribute = initProtocolConfiguration(new AssetAttribute(attributeName), MacroProtocol.PROTOCOL_NAME)
                .addMeta(new MetaItem(LABEL, Values.create(attributeLabel)));
            attribute.getMeta().add(
                new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "alarmEnabled"), Values.create(alarmEnabled))).toMetaItem()
            );
            for (Asset room : rooms) {
                if (room.hasAttribute("targetTemperature")) {
                    attribute.getMeta().add(
                        new MacroAction(new AttributeState(new AttributeRef(room.getId(), "targetTemperature"), Values.create(targetTemperature))).toMetaItem()
                    );
                }
            }
            attribute.getMeta().add(
                new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "lastExecutedScene"), Values.create(internalName))).toMetaItem()
            );
            return attribute;
        }

        AssetAttribute[] createTimerAttributes(Asset apartment) {
            List<AssetAttribute> attributes = new ArrayList<>();
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                // "MONDAY" => "Monday"
                String dayOfWeekLabel = dayOfWeek.name().substring(0, 1) + dayOfWeek.name().substring(1).toLowerCase(Locale.ROOT);
                // "0 0 7 ? *" => "0 0 7 ? * MON *"
                String timePattern = startTime + " " + dayOfWeek.name().substring(0, 3).toUpperCase(Locale.ROOT) + " *";
                attributes.add(
                    initTimerConfiguration(new AssetAttribute(attributeName + dayOfWeek.name()), timePattern,
                        new AttributeState(apartment.getId(), attributeName, Values.create("REQUEST_START")))
                        .addMeta(new MetaItem(LABEL, Values.create(attributeLabel + " trigger " + dayOfWeekLabel)))
                );
            }
            return attributes.toArray(new AssetAttribute[attributes.size()]);
        }
    }

    protected Asset createDemoApartmentSceneAgent(Asset apartment, Scene[] scenes, Asset... rooms) {
        Asset agent = new Asset("Scene Agent", AGENT, apartment);
        for (Scene scene : scenes) {
            agent.addAttributes(scene.createMacroAttribute(apartment, rooms));
        }
        for (Scene scene : scenes) {
            agent.addAttributes(scene.createTimerAttributes(apartment));
        }

        addDemoApartmentSceneEnableDisableTimer(apartment, agent, scenes);

        return agent;
    }

    protected void addDemoApartmentSceneEnableDisableTimer(Asset apartment, Asset agent, Scene[] scenes) {
        AssetAttribute enableAllMacro = initProtocolConfiguration(new AssetAttribute("enableSceneTimer"), MacroProtocol.PROTOCOL_NAME)
            .addMeta(new MetaItem(LABEL, Values.create("Enable scene timer")));
        for (Scene scene : scenes) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String sceneAttributeName = scene.attributeName + "Enabled" + dayOfWeek;
                enableAllMacro.getMeta().add(
                    new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), sceneAttributeName), Values.create(true))).toMetaItem()
                );
            }
        }
        enableAllMacro.getMeta().add(
            new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "sceneTimerEnabled"), Values.create(true))).toMetaItem()
        );
        agent.addAttributes(enableAllMacro);

        AssetAttribute disableAllMacro = initProtocolConfiguration(new AssetAttribute("disableSceneTimer"), MacroProtocol.PROTOCOL_NAME)
            .addMeta(new MetaItem(LABEL, Values.create("Disable scene timer")));
        for (Scene scene : scenes) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String sceneAttributeName = scene.attributeName + "Enabled" + dayOfWeek;
                disableAllMacro.getMeta().add(
                    new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), sceneAttributeName), Values.create(false))).toMetaItem()
                );
            }
        }
        disableAllMacro.getMeta().add(
            new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "sceneTimerEnabled"), Values.create(false))).toMetaItem()
        );
        agent.addAttributes(disableAllMacro);
    }

    protected void linkDemoApartmentWithSceneAgent(Asset apartment, Asset agent, Scene[] scenes) {
        for (Scene scene : scenes) {
            apartment.addAttributes(
                new AssetAttribute(scene.attributeName, AttributeValueType.STRING, Values.create(AttributeExecuteStatus.READY.name()))
                    .setMeta(
                        new MetaItem(LABEL, Values.create(scene.attributeLabel)),
                        new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                        new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                        new MetaItem(EXECUTABLE, Values.create(true)),
                        new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), scene.attributeName).toArrayValue())
                    ),
                new AssetAttribute(scene.attributeName + "AlarmEnabled", AttributeValueType.BOOLEAN)
                    .setMeta(
                        new MetaItem(LABEL, Values.create(scene.attributeLabel + " alarm enabled")),
                        new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                        new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                        new MetaItem(META_MACRO_ACTION_INDEX, Values.create(0)),
                        new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), scene.attributeName).toArrayValue())
                    ),
                new AssetAttribute(scene.attributeName + "TargetTemperature", AttributeValueType.NUMBER)
                    .setMeta(
                        new MetaItem(LABEL, Values.create(scene.attributeLabel + " target temperature")),
                        new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                        new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                        new MetaItem(META_MACRO_ACTION_INDEX, Values.create(1)),
                        new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), scene.attributeName).toArrayValue())
                    )
            );
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                // "MONDAY" => "Monday"
                String dayOfWeekLabel = dayOfWeek.name().substring(0, 1) + dayOfWeek.name().substring(1).toLowerCase(Locale.ROOT);
                apartment.addAttributes(
                    new AssetAttribute(scene.attributeName + "Time" + dayOfWeek.name(), AttributeValueType.STRING)
                        .setMeta(
                            new MetaItem(LABEL, Values.create(scene.attributeLabel + " time " + dayOfWeekLabel)),
                            new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                            new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                            new MetaItem(RULE_STATE, Values.create(true)),
                            new MetaItem(META_TIMER_VALUE_LINK, Values.create(TimerValue.TIME.toString())),
                            new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), scene.attributeName + dayOfWeek.name()).toArrayValue())
                        ),
                    new AssetAttribute(scene.attributeName + "Enabled" + dayOfWeek.name(), AttributeValueType.BOOLEAN)
                        .setMeta(
                            new MetaItem(LABEL, Values.create(scene.attributeLabel + " enabled " + dayOfWeekLabel)),
                            new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                            new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                            new MetaItem(META_TIMER_VALUE_LINK, Values.create(TimerValue.ENABLED.toString())),
                            new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), scene.attributeName + dayOfWeek.name()).toArrayValue())
                        )
                );
            }
        }
        apartment.addAttributes(
            new AssetAttribute("sceneTimerEnabled", AttributeValueType.BOOLEAN, Values.create(true)) // The scene timer is enabled when the timer protocol starts
                .setMeta(
                    new MetaItem(LABEL, Values.create("Scene timer enabled")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(SHOW_ON_DASHBOARD, Values.create(true)),
                    new MetaItem(READ_ONLY, Values.create(true))
                ),
            new AssetAttribute("enableSceneTimer", AttributeValueType.STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Enable scene timer")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(EXECUTABLE, Values.create(true)),
                    new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), "enableSceneTimer").toArrayValue())
                ),
            new AssetAttribute("disableSceneTimer", AttributeValueType.STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Disable scene timer")),
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)),
                    new MetaItem(EXECUTABLE, Values.create(true)),
                    new MetaItem(AGENT_LINK, new AttributeRef(agent.getId(), "disableSceneTimer").toArrayValue())
                )
        );
    }

    protected Asset createDemoCameraAsset(String name, Asset area, Supplier<MetaItem[]> agentLinker) {
        Asset cameraAsset = new Asset(name, THING, area);
        cameraAsset.addAttributes(
            new AssetAttribute("cameraCountIn", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count In")),
                    new MetaItem(DESCRIPTION, Values.create("Cumulative number of people going into area")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("cameraCountOut", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Out")),
                    new MetaItem(DESCRIPTION, Values.create("Cumulative number of people leaving area")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("cameraCountInMinute", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count In Minute")),
                    new MetaItem(DESCRIPTION, Values.create("Number of people going into area per minute")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("cameraCountOutMinute", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Out Minute")),
                    new MetaItem(DESCRIPTION, Values.create("Number of people leaving area per minute")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("cameraCountTotal", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Total")),
                    new MetaItem(DESCRIPTION, Values.create("cameraCountIn - cameraCountOut")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("cameraCountGrowth", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Growth")),
                    new MetaItem(DESCRIPTION, Values.create("cameraCountIn - cameraCountOut")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("cameraCountTotalAlertLevel", NUMBER, Values.create(100))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Total Alert Level")),
                    new MetaItem(DESCRIPTION, Values.create("When Camera Count Total equals or passes this value, an alert will be sent")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("cameraCountTotalAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Total Alert")),
                    new MetaItem(DESCRIPTION, Values.create("True when alert rule is triggered")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("cameraCountGrowthAlertLevel", NUMBER, Values.create(200))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Growth Alert Level")),
                    new MetaItem(DESCRIPTION, Values.create("When Camera Count Growth equals or passes this value, an alert will be sent")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("cameraCountGrowthAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Camera Count Growth Alert")),
                    new MetaItem(DESCRIPTION, Values.create("True when alert rule is triggered")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );

        return cameraAsset;
    }

    protected Asset createDemoMicrophoneAsset(String name, Asset area, Supplier<MetaItem[]> agentLinker) {
        Asset microphoneAsset = new Asset(name, THING, area);
        microphoneAsset.addAttributes(
            new AssetAttribute("microphoneLevel", SOUND_DB)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Level")),
                    new MetaItem(DESCRIPTION, Values.create("dB")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("microphoneLevelAlertLevel", SOUND_DB, Values.create(80))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Level Alert Level")),
                    new MetaItem(DESCRIPTION, Values.create("Level of dB when alert will be send")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("microphoneLevelAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Level Alert")),
                    new MetaItem(DESCRIPTION, Values.create("True when alert rule is triggered")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("microphoneGlassAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Glass Alert")),
                    new MetaItem(DESCRIPTION, Values.create("Input from microphone")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("microphoneGunshotAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Gunshot Alert")),
                    new MetaItem(DESCRIPTION, Values.create("Input from microphone")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("microphoneScreamAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Scream Alert")),
                    new MetaItem(DESCRIPTION, Values.create("Input from microphone")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("microphoneSoundTypeAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Microphone Sound Type Alert")),
                    new MetaItem(DESCRIPTION, Values.create("True when alert rule is triggered")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );
        return microphoneAsset;
    }

    protected Asset createDemoEnviromentAsset(String name, Asset area, Supplier<MetaItem[]> agentLinker) {
        Asset enviromentAsset = new Asset(name, THING, area);
        enviromentAsset.addAttributes(
            new AssetAttribute("temperature", TEMPERATURE_CELCIUS)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Tempature Level")),
                    new MetaItem(DESCRIPTION, Values.create("oC")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("nO2", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Nitrogen Level")),
                    new MetaItem(DESCRIPTION, Values.create("µg/m3")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("relHumidity", PERCENTAGE)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Humidity")),
                    new MetaItem(DESCRIPTION, Values.create("Humidity in area")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("ozon", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Ozon Level")),
                    new MetaItem(DESCRIPTION, Values.create("µg/m3")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("ozonAlertLevel", NUMBER, Values.create(300))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Ozon Alert Level")),
                    new MetaItem(DESCRIPTION, Values.create("µg/m3")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("ozonAlert", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Ozon Alert")),
                    new MetaItem(DESCRIPTION, Values.create("True when alert rule is triggered")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("particlesPM1", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Particles PM 1")),
                    new MetaItem(DESCRIPTION, Values.create("Particles PM 1")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("particlesPM2_5", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Particles PM 2.5")),
                    new MetaItem(DESCRIPTION, Values.create("Particles PM 2.5")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get()),
            new AssetAttribute("particlesPM10", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Particles PM 10")),
                    new MetaItem(DESCRIPTION, Values.create("Particles PM 10")),
                    new MetaItem(READ_ONLY, Values.create(true)),
                    new MetaItem(RULE_STATE, Values.create(true))
                ).addMeta(agentLinker.get())
        );

        return enviromentAsset;
    }

    protected Asset createDemoLightAsset(String name, Asset area) {
        Asset lightAsset = new Asset(name, THING, area);
        lightAsset.addAttributes(
            new AssetAttribute("lightStatus", BOOLEAN)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Light Status")),
                    new MetaItem(DESCRIPTION, Values.create("On or off")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lightDimLevel", PERCENTAGE)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Light Dim Level")),
                    new MetaItem(DESCRIPTION, Values.create("The level of dimming of the light")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("colorRGB", COLOR_RGB)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Color RGB")),
                    new MetaItem(DESCRIPTION, Values.create("The RGB color of the light")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("groupNumber", NUMBER)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Group number")),
                    new MetaItem(DESCRIPTION, Values.create("Which group this lights belongs to")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("scene", STRING)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Scene")),
                    new MetaItem(DESCRIPTION, Values.create("The scene this light is setup to")),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );

        return lightAsset;
    }
}
