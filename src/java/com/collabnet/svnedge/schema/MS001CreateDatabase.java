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
package com.collabnet.svnedge.schema;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class MS001CreateDatabase implements MigrationScript {
    private Logger log = Logger.getLogger(getClass());

    public boolean migrate(SqlUtil db) throws SQLException  {
        DatabaseMetaData dbmd = db.getConnection().getMetaData();
        if (!dbmd.getTables(null, null, "SERVER", null).next()) {
            log.info("Creating database");
            for (String sql : db.loadSql("MS001_initial_schema.sql")) {
                db.executeUpdate(sql);
            }
        } else {
            log.debug("Using existing database");
        }
        return false;
    }

    public int[] getVersion() {
        return null;
    }
}
