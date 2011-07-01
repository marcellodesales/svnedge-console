/*
* CollabNet Subversion Edge
* Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.console

/**
 * A command bean to hold all the parameters for an svnadmin dump with filtering
 */
public class DumpBean {
    static final String FILENAME_PATTERN = ~/[^<>\/\?\\;:'"`!@#%&\$\*\+)(\|\s]+/.toString()
    
    String filename
    String revisionRange
    boolean incremental
    boolean deltas
    boolean filter
    String includePath
    String excludePath
    boolean dropEmptyRevs
    boolean renumberRevs
    boolean preserveRevprops
    boolean skipMissingMergeSources

    Integer getLowerRevision() {
        Integer result = null
        if (revisionRange?.trim()) {
            String range = revisionRange.trim()
            int colonPos = range.indexOf(':')
            if (colonPos > 0) {
                result = range.substring(0, colonPos) as Integer
            } else if (colonPos < 0) {
                result = range as Integer
            } else {
                result = 0
            }
        }
        return result
    }    

    Integer getUpperRevision() {
        Integer result = null
        if (revisionRange?.trim()) {
            String range = revisionRange.trim()
            int colonPos = range.indexOf(':')
            if (colonPos >= 0) {
                result = range.substring(colonPos + 1) as Integer
            }
        }
        return result
    }    

    List<String> getIncludePathPrefixes() {
        return parsePaths(includePath)
    }
    
    List<String> getExcludePathPrefixes() {
        return parsePaths(excludePath)
    }
    
    private List<String> parsePaths(String paths) {
        List<String> result = null
        if (paths?.trim()) {
            String prefixes = paths.trim()
            List matches = prefixes.findAll(~/\b((?:\S|\\ )+)\b/, { match, path -> 
                return path.replace("\\", "").trim() })
            result = matches
        }
        return result
    }
    
    static constraints = {   
        
        filename(blank: false, nullable: false, matches: FILENAME_PATTERN)

        revisionRange(blank: true, nullable: true, matches: "\\d+(?::\\d+)?",
                      validator: { val, obj ->
                          Integer upperRev = obj.getUpperRevision()
                          if (upperRev && upperRev < obj.getLowerRevision()) {
                              return ['lowerLimitExceedsUpperLimit']
                          }
                      })
    }
}

