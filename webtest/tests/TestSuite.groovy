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
  // Suite for all webtests in this application.

import grails.util.WebTest

class TestSuite extends WebTest {

    static void main(args) {
   		new TestSuite().runTests(args)
    }

    /**
        Scan trough all test files and call their suite method.
    */
    void suite() {
    	def scanner = ant.fileScanner {
           	fileset(dir: WEBTEST_DIR, includes:'**/*Test.groovy')
        }
        for (file in scanner) {
            def test = getClass().classLoader.parseClass(file).newInstance()
            test.ant = ant
            test.suite()
        }
    }

/*
    You can alternatively define your suite manually by calling a MyTest.groovy like so
    instead of the suite impl. above:

        new MyTest(ant:ant).suite()
        new MyOtherTest(ant:ant).suite()

    This gives you more fine-grained control over the sequence of test execution.
*/

}