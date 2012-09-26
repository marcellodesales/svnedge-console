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
package com.collabnet.svnedge.domain

/**
 * Networking stats configuration
 */
class MonitoringConfiguration {

    String netInterface
    String ipAddress
    boolean networkEnabled
    boolean repoDiskEnabled
    Frequency frequency
    int hour
    int minute
    
    String cronExpression() {
        return "0 ${minute} ${hour} * * ?"
    }
    
    long periodInMillis() {
        long ONE_MIN = 60L * 1000L
        long ONE_HOUR = 60L * ONE_MIN
        switch (frequency) {
            case Frequency.HALF_HOUR:
                return 30 * ONE_MIN
            case Frequency.ONE_HOUR:
                return ONE_HOUR
            case Frequency.TWO_HOUR:
                return 2 * ONE_HOUR
            case Frequency.FOUR_HOUR:
                return 4 * ONE_HOUR
            default:
                throw new RuntimeException("Frequency not appropriate for fixed period")
        }
    }
    
    static constraints = {
        ipAddress(nullable: true, blank: true)
        netInterface(nullable: true, blank: true)
    }
    
    static MonitoringConfiguration getConfig() {
        return MonitoringConfiguration.get(1)
    }
    
    enum Frequency { HALF_HOUR, ONE_HOUR, TWO_HOUR, FOUR_HOUR, DAILY }
}

