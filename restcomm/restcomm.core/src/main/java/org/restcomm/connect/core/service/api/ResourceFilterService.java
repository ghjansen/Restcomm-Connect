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

import org.restcomm.connect.dao.entities.Account;

public interface ResourceFilterService {

    /**
     * Evaluates if the informed {@link Account} is still under the <p/>
     * quota established for outbound voice calls, inside a given <p/>
     * period of time, according with the resource filter configuration.
     * @param account
     * @return true if the account informed still under quota <p/>
     * for outbound voice calls (i.e. sum of outbound voice calls in <p/>
     * a period is less than the limit); false if the account informed <p/>
     * is over quota for outbound voice calls (i.e. sum of outbound <p/>
     * voice calls in a period is greater or equals to the limit).
     */
    boolean isOutboundVoiceCallUnderQuota(Account account);

}
