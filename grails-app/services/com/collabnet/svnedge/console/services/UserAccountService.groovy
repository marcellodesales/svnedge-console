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

import com.collabnet.svnedge.console.security.User
import com.collabnet.svnedge.console.security.Role

/**
 * This class provides User and Role management services and bootstraps the security context
 */
class UserAccountService extends AbstractSvnEdgeService {

    def lifecycleService
    def authenticateService
    def csvnAuthenticationProvider

    // ensures the existence of essential Roles and Users
    def bootStrap = {env->

        // create required security roles if needed
        Role roleAdmin = Role.findByAuthority("ROLE_ADMIN") ?:
            new Role(authority: "ROLE_ADMIN", 
                description: getMessage("role.ROLE_ADMIN"))

        Role roleAdminSystem = Role.findByAuthority("ROLE_ADMIN_SYSTEM") ?:
            new Role(authority: "ROLE_ADMIN_SYSTEM", 
                description: getMessage("role.ROLE_ADMIN_SYSTEM"))

        Role roleAdminRepo = Role.findByAuthority("ROLE_ADMIN_REPO") ?:
            new Role(authority: "ROLE_ADMIN_REPO", 
                description: getMessage("role.ROLE_ADMIN_REPO"))

        Role roleAdminUsers = Role.findByAuthority("ROLE_ADMIN_USERS") ?:
            new Role(authority: "ROLE_ADMIN_USERS", 
                description: getMessage("role.ROLE_ADMIN_USERS"))

        Role roleUser = Role.findByAuthority("ROLE_USER") ?:
            new Role(authority: "ROLE_USER", 
                description: getMessage("role.ROLE_USER"))

        // passwod "admin" used for all test users
        def password = authenticateService.encodePassword("admin")

        // only create test users for dev and test target configs
        switch (env) {

            case "development":
            case "test":

                // create test users
                log.info("Creating test users for all each role in this " +
                    "environment: ${env}")

                User superadmin = User.findByUsername("admin") ?:
                    saveNewSuperUser("admin", password)

                User adminSystem = User.findByUsername("adminSystem") ?: 
                    saveNewSuperUser("adminSystem", password)

                User adminRepo = User.findByUsername("adminRepo") ?:
                    new User(username: "adminRepo",
                            realUserName: "Repo Administrator", passwd: password,
                            description: "repository admin user", enabled: true,
                            email: "adminRepo@example.com").save(flush: true)

                User adminUsers = User.findByUsername("adminUsers") ?:
                    new User(username: "adminUsers",
                            realUserName: "Users Administrator", passwd: password,
                            description: "security admin user", enabled: true,
                            email: "adminUsers@example.com").save(flush: true)

                User normalUser = User.findByUsername("user") ?:
                    new User(username: "user",
                            realUserName: "Regular User", passwd: password,
                            description: "regular user", enabled: true,
                            email: "user@example.com").save(flush: true)

                User normalDots = User.findByUsername("user.new") ?:
                    new User(username: "user.new",
                            realUserName: "Regular User Dot", passwd: password,
                            description: "regular user with dot", enabled: true,
                            email: "user.new@example.com").save(flush: true)


                roleAdminSystem.addToPeople(adminSystem)
                roleAdminRepo.addToPeople(adminRepo)
                roleAdminUsers.addToPeople(adminUsers)


                roleUser.addToPeople(adminSystem)
                roleUser.addToPeople(adminRepo)
                roleUser.addToPeople(adminUsers)
                roleUser.addToPeople(normalUser)
                roleUser.addToPeople(normalDots)

                // Allow admin account access to svn

                lifecycleService.setSvnAuth(adminSystem, "admin")
                lifecycleService.setSvnAuth(adminRepo, "admin")
                lifecycleService.setSvnAuth(adminUsers, "admin")
                lifecycleService.setSvnAuth(normalUser, "admin")
                lifecycleService.setSvnAuth(normalDots, "admin")

            default:

                User superadmin = User.findByUsername("admin") 
                if (!superadmin) {
                    log.warn("Creating 'admin' super user since not found. " +
                        "Be sure to change password.")
                    superadmin = saveNewSuperUser("admin", password)
                }
            
                if (!superadmin.authorities?.contains(roleUser)) {
                    roleUser.addToPeople(superadmin)
                }
                if (!superadmin.authorities?.contains(roleAdmin)) {
                    roleAdmin.addToPeople(superadmin)
                }
                lifecycleService.setSvnAuth(superadmin, "admin")


                try {
                    roleAdmin.save(flush: true)
                    roleUser.save(flush: true)
                    roleAdminSystem.save(flush: true)
                    roleAdminRepo.save(flush: true)
                    roleAdminUsers.save(flush: true)
                }
                catch (Exception e) {
                    log.warn("Could not create roles", e)
                }

                break

        }


    }

    /**
     * Test if the User object derives from LDAP authentication
     * @param u
     * @return boolean is the user an LDAP user
     */
    public boolean isLdapUser(User u) {
        return csvnAuthenticationProvider.isLdapUser(u)
    }

    private User saveNewSuperUser(userid, password) {
        new User(username: userid, realUserName: "Super Administrator", enabled: true,
                 passwd: password, description: "admin user", email: "admin@example.com")
            .save(flush: true)
    }
}
