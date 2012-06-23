# -*-python-*-
#
# Copyright (C) 1999-2012 The ViewCVS Group. All Rights Reserved.
#
# By using this file, you agree to the terms and conditions set forth in
# the LICENSE.html file which can be found at the top level of the ViewVC
# distribution or at http://viewvc.org/license-1.html.
#
# For more information, visit http://viewvc.org/
#
# -----------------------------------------------------------------------
#
# viewvc: View CVS/SVN repositories via a web browser
#
# -----------------------------------------------------------------------
#
# This is a teeny stub to launch the main ViewVC app. It checks the load
# average, then loads the (precompiled) viewvc.py file and runs it.
#
# -----------------------------------------------------------------------
#

#########################################################################
#
# INSTALL-TIME CONFIGURATION
#
# These values will be set during the installation process. During
# development, they will remain None.
#
import urllib2

def index(req, java_session = None):
  #########################################################################
  #
  # Adjust sys.path to include our library directory
  #

  import sys
  import os

  env = req.subprocess_env.copy()
  SVN_LIBRARY_DIR = None
  LIBRARY_DIR = None
  CONF_PATHNAME = None
  CSVN_HOME_DIR = env["CSVN_HOME"]
  if CSVN_HOME_DIR:
    SVN_LIBRARY_DIR = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "lib", "svn-python"))
    LIBRARY_DIR     = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "lib", "viewvc"))
    CONF_PATHNAME   = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "data", "conf", "viewvc.conf"))

    sys.path.insert(0, LIBRARY_DIR)
    sys.path.insert(0, SVN_LIBRARY_DIR)

  import sapi
  import imp

  # Import real ViewVC module
  fp, pathname, description = imp.find_module('viewvc', [LIBRARY_DIR])
  try:
    viewvc = imp.load_module('viewvc', fp, pathname, description)
  finally:
    if fp:
      fp.close()

  server = sapi.ModPythonServer(req)
  cfg = viewvc.load_config(CONF_PATHNAME, server)

  if java_session and cfg.general.csvn_servermode == 'MANAGED':
    cfg.general.header_html = _get_sf_header(env, java_session)
  else:
    cfg.general.header_html = ''
    
  try:
    viewvc.main(server, cfg)
  finally:
    server.close()

def _get_sf_header(env, java_session):
  return_to_url = env['CTF_RETURN_TO_URL']
  ctf_url = env['CTF_BASE_URL']
  
  """ Calls the topInclude url to get the header contents of CTF. """
  top_include_url = '%s/sfmain/do/topInclude/%s;jsessionid=%s?base=%s&returnTo=%s&helpTopicId=26' % (ctf_url, env['CTF_PROJECT_PATH'], java_session, ctf_url[0:ctf_url.rfind('/')], return_to_url)

  return urllib2.urlopen(top_include_url).read().strip()

# _get_sf_header()


