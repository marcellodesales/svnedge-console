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
package com.collabnet.svnedge.console.services

import java.security.SecureRandom

class SecurityService {

    boolean transactional = true
    SecureRandom randomNumGen = new SecureRandom()

    String generatePassword(int minPassLength, int maxPassLength) {
        int variation = maxPassLength - minPassLength
        if (variation < 0) {
            throw new IllegalArgumentException("Maximum password length " +
                "must be greater than or equal to the minimum length.")
        }
        int passLength = minPassLength
        if (variation > 0) {
            passLength += randomNumGen.nextInt(variation)
        }
        StringBuilder sb = new StringBuilder(passLength)
        for (int i = 0; i < passLength; i++) {
            // printable ascii characters (or those between '!' and '~')
            sb.append((char) ((int)'!' + randomNumGen.nextInt(93)))
        }
        sb.toString()
    }
}
