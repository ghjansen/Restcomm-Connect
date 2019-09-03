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
package org.restcomm.connect.testsuite.telephony;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.restcomm.connect.testsuite.NetworkPortAssigner;
import org.restcomm.connect.testsuite.WebArchiveUtil;

import javax.sip.address.SipURI;
import javax.sip.message.Response;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.cafesip.sipunit.SipAssert.assertLastOperationSuccess;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class OutboundVoiceCallFilterTest {
    //environment resources
    private final static Logger logger = Logger.getLogger(OutboundVoiceCallFilterTest.class.getName());
    @ArquillianResource
    URL deploymentUrl;
    private static int restcommPort = 5080;
    private static String restcommContact = "127.0.0.1:" + restcommPort;
    private static String dialRestcomm = "sip:0001@" + restcommContact;
    private static int mockPort = NetworkPortAssigner.retrieveNextPortByFile();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(mockPort);
    private static final byte[] bytes = new byte[]{118, 61, 48, 13, 10, 111, 61, 117, 115, 101, 114, 49, 32, 53, 51, 54, 53,
            53, 55, 54, 53, 32, 50, 51, 53, 51, 54, 56, 55, 54, 51, 55, 32, 73, 78, 32, 73, 80, 52, 32, 49, 50, 55, 46, 48, 46,
            48, 46, 49, 13, 10, 115, 61, 45, 13, 10, 99, 61, 73, 78, 32, 73, 80, 52, 32, 49, 50, 55, 46, 48, 46, 48, 46, 49,
            13, 10, 116, 61, 48, 32, 48, 13, 10, 109, 61, 97, 117, 100, 105, 111, 32, 54, 48, 48, 48, 32, 82, 84, 80, 47, 65,
            86, 80, 32, 48, 13, 10, 97, 61, 114, 116, 112, 109, 97, 112, 58, 48, 32, 80, 67, 77, 85, 47, 56, 48, 48, 48, 13, 10};
    private static final String body = new String(bytes);
    //bob resources
    private static SipStackTool tool1;
    private SipStack bobSipStack;
    private SipPhone bobPhone;
    private static String bobPort = String.valueOf(NetworkPortAssigner.retrieveNextPortByFile());
    private String bobContact = "sip:bob@127.0.0.1:" + bobPort;
    //alice resources
    private static SipStackTool tool2;
    private SipStack aliceSipStack;
    private SipPhone alicePhone;
    private static String alicePort = String.valueOf(NetworkPortAssigner.retrieveNextPortByFile());
    private String aliceContact = "sip:alice@127.0.0.1:" + alicePort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        tool1 = new SipStackTool("OutboundTool1");
        tool2 = new SipStackTool("OutboundTool2");
    }

    @Before
    public void before() throws Exception {
        //bob resources setup
        bobSipStack = tool1.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", bobPort, restcommContact);
        bobPhone = bobSipStack.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, restcommPort, bobContact);
        //alice resources setup
        aliceSipStack = tool2.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", alicePort, restcommContact);
        alicePhone = aliceSipStack.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, restcommPort, aliceContact);
    }

    @After
    public void after() throws Exception {
        if(bobPhone != null) bobPhone.dispose();
        if(bobSipStack != null) bobSipStack.dispose();
        if(alicePhone != null) alicePhone.dispose();
        if(aliceSipStack != null) aliceSipStack.dispose();
    }

    @Deployment(name = "OutboundVoiceCallFilterTest", managed = true, testable = false)
    public static WebArchive createWebArchiveNoGw() {
        logger.info("Packing Test App");
        reconfigurePorts();

        Map<String,String> replacements = new HashMap();
        replacements.put("8090", String.valueOf(mockPort));
        replacements.put("5080", String.valueOf(restcommPort));
        replacements.put("5090", String.valueOf(bobPort));
        replacements.put("5091", String.valueOf(alicePort));

        return WebArchiveUtil.createWebArchiveNoGw("restcomm-outboundFilter.xml", "restcomm.script_OutboundFilter", replacements);
    }

    public static void reconfigurePorts() {
        if (System.getProperty("arquillian_sip_port") != null) {
            restcommPort = Integer.valueOf(System.getProperty("arquillian_sip_port"));
            restcommContact = "127.0.0.1:" + restcommPort;
            dialRestcomm = "sip:0001@" + restcommContact;
        }
    }

    private String dialClientRcml = "<Response><Dial timeLimit=\"10\" timeout=\"10\"><Client>alice</Client></Dial></Response>";
    @Test
    public void testOutboudVoiceCallFilter() throws ParseException, InterruptedException {
        stubFor(get(urlPathEqualTo("/0001"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(dialClientRcml)));

        //register alice
        SipURI uri = aliceSipStack.getAddressFactory().createSipURI(null, restcommContact);
        assertTrue(alicePhone.register(uri, "alice", "1234", aliceContact, 3600, 3600));
        //prepare alice for receiving calls
        SipCall aliceCall = alicePhone.createSipCall();
        aliceCall.listenForIncomingCall();

        //bob calls 0001 app
        final SipCall bobCall = bobPhone.createSipCall();
        bobCall.initiateOutgoingCall(bobContact, dialRestcomm, null, body, "application", "sdp", null, null);
        assertLastOperationSuccess(bobCall);
        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        int response = bobCall.getLastReceivedResponse().getStatusCode();
        assertTrue(response == Response.TRYING || response == Response.RINGING);
        if (response == Response.TRYING) {
            assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
            assertEquals(Response.RINGING, bobCall.getLastReceivedResponse().getStatusCode());
        }
        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        assertEquals(Response.OK, bobCall.getLastReceivedResponse().getStatusCode());
        bobCall.sendInviteOkAck();
        assertTrue(!(bobCall.getLastReceivedResponse().getStatusCode() >= 400));

        //alice receives bob's call
        assertTrue(aliceCall.waitForIncomingCall(30 * 1000));
        assertTrue(aliceCall.sendIncomingCallResponse(Response.RINGING, "Ringing-Alice", 3600));
        String receivedBody = new String(aliceCall.getLastReceivedRequest().getRawContent());
        assertTrue(aliceCall.sendIncomingCallResponse(Response.OK, "OK-Alice", 3600, receivedBody, "application", "sdp", null,
                null));
        assertTrue(aliceCall.waitForAck(50 * 1000));
        Thread.sleep(3000);

        //bob hangup
        bobCall.disconnect();
        aliceCall.listenForDisconnect();
        assertTrue(aliceCall.waitForDisconnect(30 * 1000));
        assertTrue(aliceCall.respondToDisconnect());

        //bob calls again
        bobCall.initiateOutgoingCall(bobContact, dialRestcomm, null, body, "application", "sdp", null, null);
        assertLastOperationSuccess(bobCall);
        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        response = bobCall.getLastReceivedResponse().getStatusCode();
        assertTrue(response == Response.TRYING || response == Response.RINGING);
        if (response == Response.TRYING) {
            assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
            assertEquals(Response.RINGING, bobCall.getLastReceivedResponse().getStatusCode());
        }
        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        assertEquals(Response.OK, bobCall.getLastReceivedResponse().getStatusCode());
        bobCall.sendInviteOkAck();
        assertTrue(!(bobCall.getLastReceivedResponse().getStatusCode() >= 400));

        //but as bob already used outbound quota, alice will not receive any call
        assertFalse(aliceCall.waitForIncomingCall(30 * 1000));

        bobCall.disconnect();
    }
}
