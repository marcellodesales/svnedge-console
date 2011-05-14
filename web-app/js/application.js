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
 */
function LogStreamer(logFileName, initialOffset, elementToUpdate, divElementToScroll) {

    this.logData = { "log" : {"fileName": logFileName, "startIndex": 0, "endIndex": initialOffset}}
    this.contentElement = elementToUpdate
    this.scrollingElement = divElementToScroll
    this.fetchUpdates = function(logStreamer) {

        new Ajax.Request('/csvn/log/tail', {
            logStreamer: logStreamer,
            method:'get',
            requestHeaders: {Accept: 'text/json'},
            parameters: {fileName: logStreamer.logData.log.fileName, startIndex: logStreamer.logData.log.endIndex },
            onSuccess: function(transport){
              logStreamer.logData = transport.responseText.evalJSON(true);
              if (Prototype.Browser.IE) {
                var newContent = "<PRE>" + logStreamer.contentElement.innerText + "\n" +  logStreamer.logData.log.content + "</PRE>"
                logStreamer.contentElement.update(newContent);
              }
              else {
                var newContent = logStreamer.contentElement.innerHTML + logStreamer.logData.log.content
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



