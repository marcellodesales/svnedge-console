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
class AuthProxyServiceTest extends grails.util.WebTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001AuthProxyServiceListNewDelete

    def testAuthProxyServiceListNewDelete() {
            invoke      'authProxyService'
            verifyText  'Home'

            verifyListSize 0

            clickLink   'New AuthProxyService'
            verifyText  'Create AuthProxyService'
            clickButton 'Create'
            verifyText  'Show AuthProxyService', description:'Detail page'
            clickLink   'List', description:'Back to list view'

            verifyListSize 1

            group(description:'edit the one element') {
                showFirstElementDetails()
                clickButton 'Edit'
                verifyText  'Edit AuthProxyService'
                clickButton 'Update'
                verifyText  'Show AuthProxyService'
                clickLink   'List', description:'Back to list view'
            }

            verifyListSize 1

            group(description:'delete the only element') {
                showFirstElementDetails()
                clickButton 'Delete'
                verifyXPath xpath:  "//div[@class='message']",
                            text:   /.*AuthProxyService.*deleted.*/,
                            regex:  true
            }

            verifyListSize 0
    }

    String ROW_COUNT_XPATH = "count(//div[@class='list']//tbody/tr)"

    def verifyListSize(int size) {
        ant.group(description:"verify AuthProxyService list view with $size row(s)") {
            verifyText  'AuthProxyService List'
            verifyXPath xpath:      ROW_COUNT_XPATH,
                        text:       size,
                        description:"$size row(s) of data expected"
        }
    }

    def showFirstElementDetails() {
        clickLink   '1', description:'go to detail view'
    }
}