/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2019, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.core.service.api;

import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Account;

public interface ResourceFilterService {

    /**
     * Evaluates if the informed {@link Sid} is still under the <p/>
     * quota established for outbound voice calls, inside a given <p/>
     * period of time, according with the resource filter configuration.
     * @param accountId
     * @return true if the resource filter is active and the account <p/>
     * informed still under quota for outbound voice calls <p/>
     * (i.e. sum of outbound voice calls in a period is less than the limit), <p/>
     * or if the resource filter is inactive;
     * false if the resource filter is active and the account informed <p/>
     * is over quota for outbound voice calls (i.e. sum of outbound <p/>
     * voice calls in a period is greater or equals to the limit), or <p/>
     * if the resource filter is active but failed while evaluating outbound <p/>
     * voice calls.
     */
    boolean isOutboundVoiceCallUnderQuota(Sid accountId);

}
