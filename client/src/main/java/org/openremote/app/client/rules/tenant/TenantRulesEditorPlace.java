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
package org.openremote.app.client.rules.tenant;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import org.openremote.app.client.rules.RulesEditorPlace;

public class TenantRulesEditorPlace extends RulesEditorPlace {

    final protected String realm;

    public TenantRulesEditorPlace(String realm) {
        super();
        this.realm = realm;
    }

    public TenantRulesEditorPlace(String realm, String rulesetId) {
        super(rulesetId);
        this.realm = realm;
    }

    public TenantRulesEditorPlace(String realm, Long rulesetId) {
        super(rulesetId);
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    @Prefix("tenantRulesEditor")
    public static class Tokenizer implements PlaceTokenizer<TenantRulesEditorPlace> {

        @Override
        public TenantRulesEditorPlace getPlace(String token) {
            if (token == null) {
                throw new IllegalArgumentException("Invalid empty token");
            }
            String[] fields = token.split(":");
            if (fields.length == 1) {
                return new TenantRulesEditorPlace(fields[0]);
            } else if (fields.length == 2) {
                return new TenantRulesEditorPlace(fields[0], fields[1]);
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }

        @Override
        public String getToken(TenantRulesEditorPlace place) {
            if (place.getRealm() == null) {
                return "";
            }
            return place.getRealm() + ":" + (place.getRulesetId() != null ? place.getRulesetId() : "");
        }
    }
}
