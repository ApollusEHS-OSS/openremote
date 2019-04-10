/*
 * Copyright 2017, OpenRemote Inc.
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
package org.openremote.model.util;

import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.AssetModelProvider;
import org.openremote.model.attribute.AttributeDescriptor;
import org.openremote.model.attribute.AttributeValueDescriptor;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.attribute.MetaItemDescriptor;

import java.util.Optional;

import static org.openremote.model.util.TextUtil.isNullOrEmpty;

/**
 * Explore the asset model of this project and access the {@link AssetModelProvider}s.
 * <p>
 * In a given runtime something should take responsibility for populating the descriptors at start up when running
 * within the manager then the AssetModelService will take care of this; code running outside of the manager should
 * use the {@link org.openremote.model.asset.AssetModelResource} endpoint to get this information and set it once
 * at start up.
 */
public class AssetModelUtil {

    protected static AssetDescriptor[] assetDescriptors = new AssetDescriptor[0];
    protected static AttributeDescriptor[] attributeDescriptors = new AttributeDescriptor[0];
    protected static AttributeValueDescriptor[] attributeValueDescriptors = new AttributeValueDescriptor[0];
    protected static MetaItemDescriptor[] metaItemDescriptors = new MetaItemDescriptor[0];

    private AssetModelUtil() {}

    /**
     * This should be called by something in the VM at startup to populate the available {@link AssetDescriptor}s.
     */
    public static void setAssetDescriptors(AssetDescriptor[] assetDescriptors) {
        AssetModelUtil.assetDescriptors = assetDescriptors;
    }

    /**
     * This should be called by something in the VM at startup to populate the available {@link AttributeDescriptor}s.
     */
    public static void setAttributeDescriptors(AttributeDescriptor[] attributeDescriptors) {
        AssetModelUtil.attributeDescriptors = attributeDescriptors;
    }

    /**
     * This should be called by something in the VM at startup to populate the available {@link AttributeValueDescriptor}s.
     */
    public static void setAttributeValueDescriptors(AttributeValueDescriptor[] attributeValueDescriptors) {
        AssetModelUtil.attributeValueDescriptors = attributeValueDescriptors;
    }

    /**
     * This should be called by something in the VM at startup to populate the available {@link MetaItemDescriptor}s.
     */
    public static void setMetaItemDescriptors(MetaItemDescriptor[] metaItemDescriptors) {
        AssetModelUtil.metaItemDescriptors = metaItemDescriptors;
    }

    public static AssetDescriptor[] getAssetDescriptors() {
        return assetDescriptors;
    }

    public static AttributeDescriptor[] getAttributeDescriptors() {
        return attributeDescriptors;
    }

    public static AttributeValueDescriptor[] getAttributeValueDescriptors() {
        return attributeValueDescriptors;
    }

    public static MetaItemDescriptor[] getMetaItemDescriptors() {
        return metaItemDescriptors;
    }

    public static Optional<MetaItemDescriptor> getMetaItemDescriptor(String urn) {
        if (isNullOrEmpty(urn))
            return Optional.empty();
        for (MetaItemDescriptor metaItemDescriptor : metaItemDescriptors) {
            if (metaItemDescriptor.getUrn().equals(urn))
                return Optional.of(metaItemDescriptor);
        }
        return Optional.empty();
    }

    public static Optional<AssetDescriptor> getAssetDescriptor(String urn) {
        if (urn == null)
            return Optional.empty();

        for (AssetDescriptor assetType : assetDescriptors) {
            if (urn.equals(assetType.getType()))
                return Optional.of(assetType);
        }
        return Optional.empty();
    }

    public static Optional<AttributeDescriptor> getAttributeDescriptor(String name) {
        if (name == null)
            return Optional.empty();

        for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
            if (name.equals(attributeDescriptor.getName()))
                return Optional.of(attributeDescriptor);
        }
        return Optional.empty();
    }

    public static Optional<AttributeValueDescriptor> getAttributeValueDescriptor(String name) {
        if (name == null)
            return Optional.empty();

        for (AttributeValueDescriptor attributeValueDescriptor : attributeValueDescriptors) {
            if (name.equals(attributeValueDescriptor.getName()))
                return Optional.of(attributeValueDescriptor);
        }
        return Optional.empty();
    }

    public static boolean isMetaItemRestrictedRead(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName().orElse(null))
            .map(meta -> meta.getAccess().restrictedRead)
            .orElse(false);
    }

    public static boolean isMetaItemRestrictedWrite(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName().orElse(null))
            .map(meta -> meta.getAccess().restrictedWrite)
            .orElse(false);
    }

    public static boolean isMetaItemPublicRead(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName().orElse(null))
            .map(meta -> meta.getAccess().publicRead)
            .orElse(false);
    }
//
//    public static AssetDescriptor[] getAssetTypesSorted() {
//        List<AssetDescriptor> list = new ArrayList<>(Arrays.asList(assetDescriptors));
//
//        list.sort(Comparator.comparing(AssetDescriptor::getName));
//        if (list.contains(CUSTOM)) {
//            // CUSTOM should be first
//            list.remove(CUSTOM);
//            list.add(0, CUSTOM);
//        }
//
//        return list.toArray(new AssetDescriptor[list.size()]);
//    }
}
