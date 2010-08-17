/*
 * CollabNet Subversion Edge
 * Copyright (C) 2010, CollabNet Inc. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.collabnet.svnedge.teamforge;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.BasicClientConfig;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

/**
 * Helper class for SOAP clients.
 *
 * @author Chary Aasuri <amrchary@vasoftware.com>
 * @version $Revision$ $Date$
 */
public class SoapClientHelper {

    /**
     * Service URL
     */
    private URL mServiceUrl;

    /**
     * Service name
     */
    private String mServiceName;

    /**
     * Service handle
     */
    private Service mService;
    private static final Integer DEFAULT_TIMEOUT = new Integer(Integer.MAX_VALUE);

    // private SfGlobalOptions mGlobalOptions;

    /**
     * Constructor with information on the remote SOAP service URL.
     *
     * @param serviceUrl SOAP service URL.
     * @throws MalformedURLException Thrown when the specified URL is malfored.
     */
    public SoapClientHelper(String serviceUrl)
	    throws MalformedURLException {
	mServiceUrl = new URL(serviceUrl);
	StringTokenizer urlTokens = new StringTokenizer(serviceUrl, "/");
	String urlToken = null;
	while (urlTokens.hasMoreTokens()) {
	    urlToken = urlTokens.nextToken();
	}
	mServiceName = urlToken;
	if (mServiceName == null) {
	    throw new MalformedURLException(serviceUrl);
	}

	// mGlobalOptions = SfGlobalOptionsManager.getOptions();

	EngineConfiguration config = new BasicClientConfig();

	// Uncomment below to allow SSL connections to untrusted servers
	/*AxisProperties.setProperty("org.apache.axis.components.net.SecureSocketFactory",
		"org.apache.axis.components.net.SunFakeTrustSocketFactory");*/

	mService = new Service(config);
    }

    /**
     * Invokes a service method with the specified parameters.
     *
     * @param methodName Service method name.
     * @param params     Service method parameters.
     * @return Return value from the SOAP service call.
     * @throws ServiceException See org.apache.axis.client.Service#createCall.
     * @throws RemoteException  See org.apache.axis.client.Call#invoke
     */
    public Object invoke(String methodName, Object params[])
	    throws ServiceException, RemoteException {
	return invoke(methodName, params, DEFAULT_TIMEOUT);
    }

    /**
     * Invokes a service method with the specified parameters.
     *
     * @param methodName Service method name.
     * @param params     Service method parameters.
     * @param timeout    how long before we timeout this connection.
     * @return Return value from the SOAP service call.
     * @throws ServiceException See org.apache.axis.client.Service#createCall.
     * @throws RemoteException  See org.apache.axis.client.Call#invoke
     */
    public Object invoke(String methodName, Object params[], Integer timeout)
	    throws ServiceException, RemoteException {
    Call call = (Call) mService.createCall();

    call.setTimeout(timeout);
    call.setTargetEndpointAddress(mServiceUrl);
    call.setOperationName(new QName(mServiceName, methodName));

        // Uncomment code below to get full stacktrace of what happens during Axis call
        //try {
    return call.invoke(params);
        //} catch (RemoteException e) {
        //    System. out.println("======== Exception in Axis call ========");
        //    e.printStackTrace();
        //    throw e;
        //}
    }

    /**
     * Check that specified server is alive
     * This assumes server exports Version.getVersion() method (true for Apache Axis)
     *
     * @param serverUrl <i>Server</i> URL (just protocol and hostname, no servuce part)
     * @return true if server appears to be ok
     */
    public static boolean isAlive(String serverUrl) {
	try {
	    SoapClientHelper soap = new SoapClientHelper(serverUrl + "/sf-soap/services/Version");
	    soap.invoke("getVersion", null);
	    return true;
	} catch (ServiceException e) {
	    // problem assumed
	} catch (RemoteException e) {
	    // problem assumed
	} catch (MalformedURLException e) {
	    // problem assumed
	}
	return false;
    }
}
