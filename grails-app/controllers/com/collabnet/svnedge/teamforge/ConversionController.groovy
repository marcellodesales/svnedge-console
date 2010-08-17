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
package com.collabnet.svnedge.teamforge

/**
 * Implements no-ops for the expected soap APIs when creating an 
 * integration server
 */
class ConversionController {
    def serverConfService

    private static final def repoPathRegex2 = ~/<ns1\:arg2 xsi\:type="soapenc\:string" xmlns\:soapenc="http\:\/\/schemas\.xmlsoap\.org\/soap\/encoding\/">(.*)<\/ns1\:arg2>/
    private static final def repoPathRegex1 = ~/<ns1\:arg1 xsi\:type="soapenc\:string" xmlns\:soapenc="http\:\/\/schemas\.xmlsoap\.org\/soap\/encoding\/">(.*)<\/ns1\:arg1>/

    def index = {
        def body = request.reader.text
        response.setContentType("Content-Type: text/xml; charset=utf-8")


        def method = body.find(~/<soapenv\:Body>\s*<ns1:(\w+) /, 
            { match, method -> method })
        if (method) {
            // FIXME! This started getting complext trying to fake all
            // the methods needed to remove an integration server with
            // active repositories which is useful for testing, but the
            // only one which works is createRepository
            if (method.indexOf("Repository") > 0) {
                def repoPathRegex = method.startsWith("create") ?
                    repoPathRegex2 : repoPathRegex1
                def path = body.find(repoPathRegex, 
                    { match, repoPath -> repoPath })
                response.writer << REPO_RESPONSE_PREFIX
                response.writer << method
                response.writer << REPO_RESPONSE_2
                response.writer << method
                response.writer << REPO_RESPONSE_3
                response.writer << path
                response.writer << REPO_RESPONSE_4
                response.writer << method
                response.writer << REPO_RESPONSE_5
                response.writer << method
                response.writer << REPO_RESPONSE_SUFFIX

            } else {
                response.writer << RESPONSE_PREFIX
                response.writer << method
                response.writer << RESPONSE_SUFFIX
            }
        } else {
            println body
            response.writer << "Could not find command"
        }
        response.writer.flush()
    }

    private static final String REPO_RESPONSE_PREFIX =
        """<?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:"""

    private static final String REPO_RESPONSE_2 =
        """Response soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"><"""

    private static final String REPO_RESPONSE_3 =
"""Return xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">"""

    private static final String REPO_RESPONSE_4 =
        """</"""

    private static final String REPO_RESPONSE_5 =
        """Return></ns1:"""

    private static final String REPO_RESPONSE_SUFFIX =
        """Response></soapenv:Body></soapenv:Envelope>"""

/*
<soapenv:Body>
  <ns1:createRepository soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon">
   <ns1:arg0 xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">1wwCLdQ1Td2b3ZmSZEfCMTCinToWnH1oQyYZ+A==</ns1:arg0>
   <ns1:arg1 xsi:nil="true"/>
   <ns1:arg2 xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">/svnroot/r1_1273782062878</ns1:arg2>
   <ns1:arg3 xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">exsy1056</ns1:arg3>
   <ns1:arg4 xsi:nil="true"/>
  </ns1:createRepository>
 </soapenv:Body>


<ns1:createRepositoryResponse soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"><createRepositoryReturn xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">/svnroot/r1_1273782062878</createRepositoryReturn></ns1:createRepositoryResponse>
/svnroot/r1_1273782062878

    private static final String VERIFY_RESPONSE = 
        """<?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:verifyExternalSystemResponse soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"/></soapenv:Body></soapenv:Envelope>"""

    private static final String INITIALIZE_RESPONSE = 
        """<?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:initializeExternalSystemResponse soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"/></soapenv:Body></soapenv:Envelope>"""

    private static final String DELETE_RESPONSE = 
        """<?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:deleteExternalSystemResponse soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"/></soapenv:Body></soapenv:Envelope>"""

*/

    // Can handle "verify", "initialize", and "delete" (ExternalSystem)
    private static final String RESPONSE_PREFIX = 
        """<?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><ns1:"""
    private static final String RESPONSE_SUFFIX = 
"""Response soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="SubversionScmServerDaemon"/></soapenv:Body></soapenv:Envelope>"""

}
