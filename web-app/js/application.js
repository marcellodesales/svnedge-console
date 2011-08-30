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

var Ajax;
if (Ajax && (Ajax != null)) {
	Ajax.Responders.register({
	  onCreate: function() {
        if($('spinner') && Ajax.activeRequestCount>0)
          Effect.Appear('spinner',{duration:0.5,queue:'end'});
	  },
	  onComplete: function() {
        if($('spinner') && Ajax.activeRequestCount==0)
          Effect.Fade('spinner',{duration:0.5,queue:'end'});
	  }
	});
}

/**
 * A class for streaming a log file into a div or other element
 * @param logFileName the logfile to stream or tail
 * @param initialOffset the offset at which to begin streaming (eg, length of already displayed content)
 * @param elementToUpdate the container for the log content
 * @param divElementToScroll the div element to scroll with the updates (could be same as element to update)
 * @param errorMsg text to display if the incremental update reports an error)
 */
function LogStreamer(logFileName, initialOffset, elementToUpdate, divElementToScroll, errorMsg) {

    this.logData = { "log" : {"fileName": logFileName, "startIndex": 0, "endIndex": initialOffset}}
    this.contentElement = elementToUpdate
    this.scrollingElement = divElementToScroll
    this.errorMsg = errorMsg
    this.fetchUpdates = function(logStreamer) {

        new Ajax.Request('/csvn/log/tail', {
            logStreamer: logStreamer,
            method:'get',
            requestHeaders: {Accept: 'text/json'},
            parameters: {fileName: logStreamer.logData.log.fileName, startIndex: logStreamer.logData.log.endIndex },
            onSuccess: function(transport){
              logStreamer.logData = transport.responseText.evalJSON(true);
              appendText = ""
              if (logStreamer.logData.log.error) {
                  appendText = (logStreamer.errorMsg) ? logStreamer.errorMsg : "\n\n** " + logStreamer.logData.log.error + " **"
                  logStreamer.stop()
              }
              else {
                  appendText = logStreamer.logData.log.content
              }
              if (Prototype.Browser.IE) {
                var newContent = "<PRE>" + logStreamer.contentElement.innerText + "\n" + appendText + "</PRE>"
                logStreamer.contentElement.update(newContent);
              }
              else {
                var newContent = logStreamer.contentElement.innerHTML + appendText
                logStreamer.contentElement.update(newContent) ;
              }
              logStreamer.scrollingElement.scrollTop = logStreamer.scrollingElement.scrollHeight;
            }
         })
    }
    this.periodicUpdater = null
    this.start = function() {
        var fetchUpdates = this.fetchUpdates.curry(this)
        this.periodicUpdater = new PeriodicalExecuter(fetchUpdates, 1)
    }
    this.stop = function() { if (this.periodicUpdater) this.periodicUpdater.stop() }
}

/**
 * A class for validating that a given login username is available in CollabNet cloud services via ajax
 * @param usernameElement the input field to validate (element)
 * @param messageElement the message field in which to indicate result (element)
 */
function CloudLoginAvailabilityChecker(usernameElement, messageElement) {



    this.usernameElement = usernameElement
    this.messageElement = messageElement
    this.delayCheckTimer = null
    this.onSuccess = null
    this.onFailure = null
    this.loginAvailable = false
    this.doAjaxRequest = function(checker) {
        checker.messageElement.innerHTML = usernameAvailableMessages.checking
        checker.ajaxInstance = new Ajax.Request('/csvn/setupCloudServices/checkLoginAvailability', {
                    method:'get',
                    requestHeaders: {Accept: 'text/json'},
                    parameters: {username: checker.usernameElement.value },
                    onSuccess: function(transport) {
                        var responseJson = transport.responseText.evalJSON(true);
                        if (responseJson.loginAvailable == 'true') {
                            checker.messageElement.innerHTML = usernameAvailableMessages.available
                            checker.loginAvailable = true
                            if (checker.onSuccess != null) {
                                checker.onSuccess()
                            }
                        }
                        else {
                            checker.messageElement.innerHTML = usernameAvailableMessages.notAvailable
                            checker.loginAvailable = false
                            if (checker.onFailure != null) {
                                checker.onFailure()
                            }
                        }
                    }
                })
    }
    this.keypressHandler = function() {
        clearTimeout(this.delayCheckTimer)
        var checkLoginAvailability = this.doAjaxRequest.curry(this)
        this.delayCheckTimer = setTimeout(checkLoginAvailability, 1000)
    }

    this.messageElement.innerHTML = usernameAvailableMessages.prompt
}
