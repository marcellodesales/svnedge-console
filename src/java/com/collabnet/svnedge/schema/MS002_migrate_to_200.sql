// CollabNet Subversion Edge
// Copyright (C) 2011, CollabNet Inc. All rights reserved.
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
// 
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

drop table MASTER if exists;
drop table REPLICA_CONFIG if exists;
drop table REPLICA_ERROR_TRACE if exists;
drop table REPLICA_ERROR if exists;
        
// remove some stats entries that were dropped in later versions
set REFERENTIAL_INTEGRITY False;
delete from STAT_ACTION where GROUP_ID in (select ID from STAT_GROUP where NAME in ('UserCache', 'Latency'));
set REFERENTIAL_INTEGRITY True;
delete from STAT_VALUE where STATISTIC_ID in (select ID from STATISTIC inner join STAT_GROUP on (STATISTIC.GROUP_ID=STAT_GROUP.ID) where STAT_GROUP.NAME in ('UserCache', 'Latency'));
delete from STATISTIC where GROUP_ID in (select ID from STAT_GROUP where NAME in ('UserCache', 'Latency'));
delete from STAT_GROUP where NAME in ('UserCache', 'Latency');
// previous delete from STAT_GROUP should take care of constraints/fk
delete from UNIT where NAME in ('Users', 'Milliseconds');

delete from CATEGORY where NAME = 'Cache';

CREATE MEMORY TABLE REPLICA_CONFIGURATION (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, 
  VERSION BIGINT NOT NULL, 
  ACCEPTED_CERT_FINGER_PRINT VARCHAR(255), 
  APPROVAL_STATE VARCHAR(255) NOT NULL, 
  COMMAND_POLL_RATE INTEGER NOT NULL, 
  DESCRIPTION VARCHAR(255) NOT NULL, 
  MAX_LONG_RUNNING_CMDS INTEGER NOT NULL, 
  MAX_SHORT_RUNNING_CMDS INTEGER NOT NULL, 
  NAME VARCHAR(255) NOT NULL, 
  SVN_MASTER_URL VARCHAR(255), 
  SYSTEM_ID VARCHAR(255) NOT NULL
);

CREATE MEMORY TABLE SCHEMA_VERSION (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, 
  VERSION BIGINT NOT NULL, 
  DATE_CREATED TIMESTAMP NOT NULL, 
  DESCRIPTION VARCHAR(255) NOT NULL, 
  MAJOR INTEGER NOT NULL, 
  MINOR INTEGER NOT NULL, 
  REVISION INTEGER NOT NULL
);
