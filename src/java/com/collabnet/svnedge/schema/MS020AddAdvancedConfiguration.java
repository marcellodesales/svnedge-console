/*
 * CollabNet Subversion Edge
 * Copyright (C) 2013, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.schema;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class MS020AddAdvancedConfiguration implements MigrationScript {
    private Logger log = Logger.getLogger(getClass());

    public boolean migrate(SqlUtil db) throws SQLException {
        
        String createTable = "CREATE MEMORY TABLE ADVANCED_CONFIGURATION(" +
                "ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) " +
                "  NOT NULL PRIMARY KEY, " +
                "VERSION BIGINT NOT NULL, " +
                "AUTO_VERSIONING BOOLEAN default false not null, " +
                "LIST_PARENT_PATH BOOLEAN default true not null, " +
                "COMPRESSION_LEVEL INTEGER default 5 not null, " +
                "HOOKS_ENV VARCHAR(255), " +
                "USE_UTF8 BOOLEAN default false not null, " +
                "PATH_AUTHZ BOOLEAN default true not null, " +
                "STRICT_AUTHZ BOOLEAN default false not null, " +
                "IN_MEMORY_CACHE_SIZE INTEGER default 16 not null, " +
                "CACHE_FULL_TEXTS BOOLEAN default false not null, " +
                "CACHE_TEXT_DELTAS BOOLEAN default false not null, " +
                "CACHE_REV_PROPS BOOLEAN default false not null," +
                "ALLOW_BULK_UPDATES BOOLEAN default true not null," +
                "PREFER_BULK_UPDATES BOOLEAN default false not null," +
                "SVN_REALM VARCHAR(255), " +
                "ACCESS_LOG_FORMAT VARCHAR(255), " +
                "SVN_LOG_FORMAT VARCHAR(255)" +
                ")";
        db.executeUpdateSql(createTable);
        return false;
    }

    public int[] getVersion() {
        return new int[] {4,0,3};
    }
}