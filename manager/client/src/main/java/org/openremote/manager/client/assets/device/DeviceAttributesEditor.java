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
package org.openremote.manager.client.assets.device;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.openremote.manager.client.Environment;
import org.openremote.manager.client.widget.*;
import org.openremote.manager.shared.attribute.Attribute;
import org.openremote.manager.shared.attribute.Attributes;
import org.openremote.manager.shared.device.DeviceAttributes;
import org.openremote.manager.shared.device.DeviceResource;

import java.util.logging.Logger;

public class DeviceAttributesEditor extends AttributesEditor<DeviceAttributesEditor.Style> {

    private static final Logger LOG = Logger.getLogger(DeviceAttributesEditor.class.getName());

    public interface Style extends AttributesEditor.Style {

        String readWriteInput();

        String readButton();

        String writeButton();
    }

    final protected FormGroup deviceActionsGroup = new FormGroup();
    final protected FormCheckBox enableLiveUpdatesCheckBox = new FormCheckBox();

    public DeviceAttributesEditor(Environment environment, Container<DeviceAttributesEditor.Style> container, Attributes attributes) {
        super(environment, container, attributes);

        FormLabel enableLiveUpdates = new FormLabel();
        enableLiveUpdates.addStyleName("larger");
        enableLiveUpdates.setText(environment.getMessages().enableLiveUpdates());
        FormField deviceActionsFormField = new FormField();
        deviceActionsFormField.add(enableLiveUpdatesCheckBox);
        deviceActionsGroup.addFormLabel(enableLiveUpdates);
        deviceActionsGroup.addFormField(deviceActionsFormField);

        enableLiveUpdatesCheckBox.addValueChangeHandler(event -> {
            LOG.info("### LIVE UPDATES: " + event.getValue());
        });
    }

    @Override
    public void render() {
        container.getPanel().add(deviceActionsGroup);
        super.render();
    }

    @Override
    public void setOpaque(boolean opaque) {
        super.setOpaque(opaque);
        deviceActionsGroup.setOpaque(opaque);
    }

    @Override
    protected FormLabel buildFormLabel(Attribute attribute) {
        FormLabel formLabel = super.buildFormLabel(attribute);

        if (DeviceResource.isDeviceResource(attribute)) {
            DeviceResource deviceResource = new DeviceResource(attribute);
            // TODO Anything
        } else if (attribute.getName().equals("key")) {
            formLabel.setText(environment.getMessages().deviceKey());
        }

        return formLabel;
    }

    @Override
    protected FormGroup createFormGroup(Attribute attribute) {
        FormGroup formGroup = super.createFormGroup(attribute);
        if (DeviceResource.isDeviceResource(attribute)) {
            formGroup.addInfolabel(
                new Label(environment.getMessages().resourceKey())
            );

            DeviceResource deviceResource = new DeviceResource(attribute);


            // TODO These actions don't do anything
            FlowPanel actionPanel = new FlowPanel();
            actionPanel.setStyleName("flex layout horizontal center");

            FormButton readButton = new FormButton();
            readButton.setPrimary(true);
            readButton.addStyleName(container.getStyle().readButton());
            readButton.setText(environment.getMessages().read());

            FormInputText readWriteInput = new FormInputText();
            readWriteInput.addStyleName(container.getStyle().readWriteInput());

            FormButton writeButton = new FormButton();
            writeButton.setDanger(true);
            writeButton.addStyleName(container.getStyle().writeButton());
            writeButton.setText(environment.getMessages().write());

            actionPanel.add(readButton);
            actionPanel.add(readWriteInput);
            actionPanel.add(writeButton);
            switch (deviceResource.getAccess()) {
                case R:
                    readWriteInput.setReadOnly(true);
                    readButton.setEnabled(true);
                    writeButton.setEnabled(false);
                    break;
                case W:
                    readWriteInput.setReadOnly(false);
                    readButton.setEnabled(false);
                    writeButton.setEnabled(true);
                    break;
                default:
                    readWriteInput.setReadOnly(false);
                    readButton.setEnabled(true);
                    writeButton.setEnabled(true);
            }
            formGroup.getFormField().add(actionPanel);
        }
        return formGroup;
    }

    @Override
    protected boolean isDefaultReadOnly(Attribute attribute) {
        return DeviceAttributes.isReadOnly(attribute) || super.isDefaultReadOnly(attribute);
    }

    @Override
    protected FormInputText createStringEditor(Style style, Attribute attribute, boolean readOnly) {
        return super.createStringEditor(style, attribute, readOnly || DeviceResource.isDeviceResource(attribute));
    }

    @Override
    protected FormInputNumber createIntegerEditor(Style style, Attribute attribute, boolean readOnly) {
        return super.createIntegerEditor(style, attribute, readOnly || DeviceResource.isDeviceResource(attribute));
    }

    @Override
    protected FormInputText createFloatEditor(Style style, Attribute attribute, boolean readOnly) {
        return super.createFloatEditor(style, attribute, readOnly || DeviceResource.isDeviceResource(attribute));
    }

    @Override
    protected FormCheckBox createBooleanEditor(Style style, Attribute attribute, boolean readOnly) {
        return super.createBooleanEditor(style, attribute, readOnly || DeviceResource.isDeviceResource(attribute));
    }
}
