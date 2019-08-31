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
package org.restcomm.connect.core.service.call;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.core.service.api.ResourceFilterService;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.entities.Account;
import org.restcomm.connect.dao.entities.CallDetailRecordFilter;

import java.text.ParseException;

public class ResourceFilterImpl implements ResourceFilterService {

    private static Logger logger = Logger.getLogger(ResourceFilterImpl.class);
    private final DaoManager daoManager;
    private Integer outboundVoiceCallsQuota = null;
    private Integer outboundVoiceCallsPeriod = null;
    private boolean active = false;

    public ResourceFilterImpl(final DaoManager daoManager, final Configuration configuration) {
        this.daoManager = daoManager;
        loadOutboundVoiceCallConfiguration(configuration);
    }

    private void loadOutboundVoiceCallConfiguration(final Configuration configuration){
        String configQuota = configuration.subset("runtime-settings")
                .subset("resourceFilter")
                .getString("outboundVoiceCallsQuota", "");
        String configPeriod = configuration.subset("runtime-settings")
                .subset("resourceFilter")
                .getString("outboundVoiceCallsPeriod", "");
        if(!configQuota.isEmpty() && !configPeriod.isEmpty()){
            Integer quota = Integer.getInteger(configQuota);
            Integer period = Integer.getInteger(configPeriod);
            if(quota >= 0 && period > 0){
                outboundVoiceCallsQuota = quota;
                outboundVoiceCallsPeriod = period;
                active = true;
            }
        }
    }

    @Override
    public boolean isOutboundVoiceCallUnderQuota(final Sid accountId) {
        if(active){
            try {
                CallDetailRecordFilter cdrf = new CallDetailRecordFilter(String.valueOf(accountId),null,null,null,null,null,null,null,null,null,null,null,"outbound-api",outboundVoiceCallsPeriod);
                Integer totalAccount = daoManager.getCallDetailRecordsDao().getTotalCallDetailRecords(cdrf);
                if(totalAccount < outboundVoiceCallsQuota){
                    return true;
                } else {
                    return false;
                }
            } catch (ParseException e) {
                logger.error("Resource Filter failed while evaluating outbound voice calls quota. " +
                        "Over quota assumed as default", e);
                return false;
            }
        } else {
            return true;
        }
    }
}
