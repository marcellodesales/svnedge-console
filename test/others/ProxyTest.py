import urllib2

httpTarget = "http://www.collab.net"
httpsTargetTrusted = "https://ctf.open.collab.net/sf/sfmain/do/home"
httpsTargetUntrusted = "https://www.collab.net"

proxyHost = "cu182.cloud.sp.collab.net"
proxyPort = "80"
proxyUser = "proxyuser"
proxyPwd = "proxypass"

def main():
    print "Testing proxy: %s\n" % (getProxyUrl(),)
    testProxy(httpTarget)
    testProxy(httpsTargetTrusted)
    testProxy(httpsTargetUntrusted)

def getProxyUrl():
    proxyUrl = "http://%s:%s@%s:%s" % (proxyUser, proxyPwd, proxyHost, proxyPort)
    return proxyUrl

def getProxyProtocol(url):
    if url.startswith("https"): 
        return "https"
    else: 
        return "http" 
    
def testProxy(url):
    req = urllib2.Request(url)
    
    # build a new opener that uses a proxy requiring authorization
    proxy_support = urllib2.ProxyHandler({getProxyProtocol(url) : getProxyUrl()})
    opener = urllib2.build_opener(proxy_support, urllib2.HTTPHandler)
    # install it
    urllib2.install_opener(opener)    

    try:
        print "Testing proxy to target: %s ..." % (url, )
        response = urllib2.urlopen(req)
        if response.read():
            print "Proxy connection was successful\n" 
        
    except IOError, e:
        if hasattr(e, 'reason'):
            print 'Failed to reach a server.'
            print 'Reason: \n', e.reason
        elif hasattr(e, 'code'):
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: \n', e.code
    
    
if __name__ == "__main__":
    main()    



  