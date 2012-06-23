#!/usr/bin/env python
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

LIBRARY_DIR = None
SVN_LIBRARY_DIR = None
CSVN_HOME_DIR = None
CONF_PATHNAME = None
INTEGRATION_DIR = None

#########################################################################
#
# Adjust sys.path to include our library directory
#

import sys
import os

CSVN_HOME_DIR = os.getenv("CSVN_HOME")
if CSVN_HOME_DIR:
  SVN_LIBRARY_DIR = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "lib", "svn-python"))
  LIBRARY_DIR = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                  "lib", "viewvc"))
  CONF_PATHNAME   = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "data", "conf", "viewvc.conf"))
  INTEGRATION_DIR = os.path.abspath(os.path.join(CSVN_HOME_DIR,
                                      "lib", "integration"))

if LIBRARY_DIR:
  sys.path.insert(0, LIBRARY_DIR)
  sys.path.insert(0, SVN_LIBRARY_DIR)
else:
  sys.path.insert(0, os.path.abspath(os.path.join(sys.argv[0],
                                                  "../../../lib")))

### TeamForge customization:  Also add the 'integration' dir to sys.path.
if INTEGRATION_DIR and os.path.exists(INTEGRATION_DIR):
  sys.path.append(INTEGRATION_DIR)

#########################################################################

### add code for checking the load average

#########################################################################

# go do the work
import sapi
import viewvc

server = sapi.CgiServer()
cfg = viewvc.load_config(CONF_PATHNAME, server)
viewvc.main(server, cfg)
