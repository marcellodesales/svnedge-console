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
package com.collabnet.svnedge.console.ui

import org.codehaus.groovy.grails.plugins.springsecurity.Secured

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.Unit
import com.collabnet.svnedge.replica.event.UserCacheEvent

import java.text.SimpleDateFormat

import jofc2.model.Chart
import jofc2.model.elements.LineChart
import jofc2.model.elements.Legend
import jofc2.model.elements.PieChart
import jofc2.model.axis.Label
import jofc2.model.axis.XAxis
import jofc2.model.axis.YAxis

import org.codehaus.groovy.grails.plugins.springsecurity.Secured

@Secured(['ROLE_USER'])
class StatisticsController {
    private static int NUM_XLABELS = 10
    private static int NUM_YLABELS = 10

    def operatingSystemService
    def userCacheStatisticsService
    def networkStatisticsService
    def latencyStatisticsService
    def fileSystemStatisticsService
    def svnStatisticsService

    def colors = ["#3333FF", "#FF0033", "#33CC33", "#CC99FF"]

    def getTimespans() {
        return [[index: 0, 
                title: message(code: "statistics.graph.timespan.lastHour"),
                seconds: 60*60, pattern: "HH:mm"],
        [index: 1, title: message(code: "statistics.graph.timespan.lastDay"),
            seconds: 60*60*24, pattern: "HH:mm"],
        [index: 2, title: message(code: "statistics.graph.timespan.lastWeek"),
            seconds: 60*60*24*7, 
            pattern: message(code: "default.dateTime.format.dayMonth")],
        [index: 3, title: message(code: "statistics.graph.timespan.lastMonth"),
            seconds: 60*60*24*30, 
            pattern: message(code: "default.dateTime.format.dayMonth")]]
    }

    def graphList() {
        return [
        [statgroup: "UserCache", 
            graphName: message(code: 
                "statistics.graph.leftNav.usersCache.chart"), 
            graphData: "USER_CACHE_PIE_CHART"],
        [statgroup: "UserCache", 
            graphName: message(code: 
                "statistics.graph.leftNav.usersCache.chart"), 
            graphData: "USER_CACHE_LINE_CHART"],
        [statgroup: "NetworkThroughput", 
            graphName: message(code: 
                "statistics.graph.leftNav.throughput.chart"),
            graphData: "BYTE_RATE_CHART"],
        [statgroup: "Latency", 
            graphName: message(code: 
                "statistics.graph.leftNav.master.latency.chart"),
            graphData: "LATENCY_CHART"],
        [statgroup: "FileSystem", 
            graphName: message(code: 
                "statistics.graph.leftNav.diskSpace.chart"),
            graphData: "DISKSPACE_CHART"]
        /*[statgroup: "SvnRepoHits",
            graphName: "SVN hits by Repository",
            graphData: "SVN_HITS_BY_REPO"]*/]
    }

    @Secured(['ROLE_USER'])
    def index = {
        def statGroups = StatGroup.list(sort: "name")
        def statGroupNames = statGroups.collect{ it.getTitle() }
        def isReplica = Server.getServer().replica
        def initialGraph = null
        def statData = Category.list().collect { category ->
            def statgroups = category.getGroups();
            if (!isReplica) {
                statgroups = statgroups.findAll { allGrp ->
                    !allGrp.isReplica 
                }
            }
            statgroups = statgroups.collect { statgroup ->
                def graphs = graphList().findAll{ gl -> 
                    gl.statgroup == statgroup.name
                }
                if (!initialGraph && graphs.size() > 0) {
                    initialGraph = graphs[0].graphData
                }
                def title = ""
                def n = statgroup.getName()
                if (n.equals(networkStatisticsService.STATGROUP_NAME)) {
                    title = message(code: "statistics.graph.leftNav.throughput")
                } else 
                if (n.equals(fileSystemStatisticsService.STATGROUP_NAME)) {
                    title = message(code: "statistics.graph.leftNav.diskSpace")
                }
                [statgroup: title, graphs: graphs]
            }
            [category: message(code: 
                "statistics.graph.leftNav.category." + 
                category.getName().toLowerCase()), statgroups: statgroups]
        }

        def timespanSelect = this.getTimespans().inject([:]) { map, ts ->
            map[ts.index] = ts.title 
            map
        }
        return [timespanSelect: timespanSelect,
                statData: statData,
                initialGraph: initialGraph ? 
                              initialGraph : graphList()[0].graphData]
    }

    def getTimespan = {
        def index = 0
        if (params && params['timespan']) {
            index = Integer.parseInt(params['timespan'])
        }
        def timespans = this.getTimespans()
        timespans[index]
    }

    def USER_CACHE_PIE_CHART = {
        render getUserCachePieChart()
    }

    def getUserCachePieChart = { 
        def ts = getTimespan()
        def cacheTypes = UserCacheEvent.CACHE_TYPES.collect { it.type }
        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()
        def getTotal = { cacheType, eventType ->
            userCacheStatisticsService.getTotal(cacheType, eventType, start, 
                                                now)
        }
        def totalHits = cacheTypes.collect { getTotal(it, UserCacheEvent.HIT)}
            .sum()
        def totalMisses = cacheTypes.collect { getTotal(it, 
                                                        UserCacheEvent.MISS)}
            .sum()
        PieChart pc = new PieChart().setAnimate(true).setStartAngle(35)
            .setBorder(2).addSlice(totalHits, 
                message(code: "statistics.graph.usersCache.slice.hits"))
            .addSlice(totalMisses, 
                message(code: "statistics.graph.usersCache.slice.misses"))
            .setTooltip("#val# of #total#<br>#percent#")
        pc.setGradientFill(true)
        Chart c = new Chart(message(code: "statistics.graph.userCache.title") +
            " " + ts.title)
            .addElements(pc)
        c.setBackgroundColour("#FFFFFF")
        c
    }

    def USER_CACHE_LINE_CHART = {
        render getUserCacheLineChart()
    }

    def getUserCacheLineChart = {
        Chart c
        def ts = getTimespan()
        def lines = []
        UserCacheEvent.CACHE_TYPES.eachWithIndex{ cacheType, index ->
            def colorIndex = index % colors.size()
            lines << [ cacheType: cacheType.type, title: cacheType.name + 
                    " Cache", color: colors[colorIndex]]
        }

        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()
        
        def hitRateMaps = lines.collect {
            userCacheStatisticsService.getHitRates(it.cacheType, start, now)
        }

        def timeValues = hitRateMaps.inject(new TreeSet()) { treeset, map ->
            treeset.addAll(map.keySet())
            treeset
        }

        def lcValues = hitRateMaps.collect { map ->
            timeValues.collect {
                def val = map.get(it)
                if (val == 0) {
                    0
                } else if (!val) {
                    null
                } else if (val.equals(Double.NaN)) {
                    null
                } else {
                    val * 100
                }
            }
        }

        def minValue = 0
        def maxValue = 100

        if (!timeValues) {
            c = new Chart(message(code: "statistics.graph.userCache.title") +
                " " + ts.title + " : " + message(
                    code: "statistics.graph.noDataYet"))
        } else {

            def lineCharts = []
            lcValues.eachWithIndex { lcValue, index ->
                def lc = new LineChart().addValues(lcValue)
                lc.setColour(lines[index].color)
                lc.setDotSize(3)
                lc.setTooltip(lines[index].title + "<br>#x_label#<br>#val# %")
                lc.setText(lines[index].title)
                lineCharts << lc
            }
            c = new Chart(message(code: "statistics.graph.userCache.hit.title")
                + " " + ts.title)
            .addElements(lineCharts)

            addXAxis(c, timeValues, ts.pattern)

            c.setLegend(new Legend())
        }

        addYAxis(c, minValue, maxValue, false)
        
        c.setBackgroundColour("#FFFFFF")
        log.debug("c = " + c)
        c
    }

    def BYTE_RATE_CHART = {
        render getByteRateChart()
    }

    def getByteRateChart = { 
        Chart c
        def ts = getTimespan()
        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()

        def lines = [[type: networkStatisticsService.getThroughputInRateName(),
                color: colors[0], 
                title: message(code: "statistics.graph.throughput.in")],
            [type: networkStatisticsService.getThroughputOutRateName(), 
                color: colors[1], 
                title: message(code: "statistics.graph.throughput.out")]]

        def rates = networkStatisticsService.getThroughputRates(start, now)
        if (!rates) {
            c = new Chart(message(code: "statistics.graph.throughput.title") +
                " " + ts.title + " : " + 
                message(code: "statistics.graph.noDataYet"))
        } else {
            def minValue = 0
            def maxValue = 0

            def valueSets = lines.collect {
                def values = rates.keySet().collect { key ->
                                   rates.get(key)[it.type]
                }
                maxValue = Math.max(maxValue, values.max()?: 0)
                values
            }

            def magPrefix = getByteMagPrefix(maxValue)
            def unitPrefix = magPrefix[1]
            def mag = magPrefix[0]
            def scaledSets = valueSets
            if (mag) {
                scaledSets = valueSets.collect { vs ->
                    vs.collect { value ->
                        networkStatisticsService.applyMag(value, mag, 2)
                    }

                }
                maxValue = networkStatisticsService.applyMag(maxValue, mag, 2)
            }

            def lineCharts = [] 
            lines.eachWithIndex { line, index ->
                def lineChart = new LineChart()
                    .addValues(scaledSets[index])
                lineChart.setColour(line.color)
                lineChart.setDotSize(3)
                lineChart.setTooltip(line.title + "<br>#x_label#<br>#val# " +
                    unitPrefix + message(code:
                        "general.measurement.bytesPerSecond.short"))
                lineChart.setText(line.title)
                lineCharts << lineChart
            } 

            c = new Chart(message(code: "statistics.graph.throughput.title") +
                " " + ts.title).addElements(lineCharts)
            
            addXAxis(c, rates.keySet(), ts.pattern)
            
            c.setLegend(new Legend())
            
            addYAxis(c, minValue, maxValue)                                 
        }

        c.setBackgroundColour("#FFFFFF")
        log.debug("c = " + c)
        c  
    }

    def LATENCY_CHART = {
        render getLatencyChart()
    }

    def getLatencyChart = {
        Chart c
        def ts = getTimespan()
        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()
 
        def line = [type: "Latency", color: colors[0], 
            title: message(code: "statistics.graph.latency.title")]
        def chartData = latencyStatisticsService
            .getChartValues(start.getTime(), now.getTime())

        if (!chartData) {
            c = new Chart(message(code:"statistics.graph.latency.replica.title")
                + " " + ts.title + " : " + message(
                    code: "statistics.graph.noDataYet"))
        } else {
            c = new Chart(message(code:"statistics.graph.latency.replica.title")
                + " " + ts.title)
            def minValue = 0
            def maxValue = 0
            def latValues = chartData.keySet().collect {
                def val = chartData[it][line['type']]
                maxValue = Math.max(maxValue, val ?: 0)
                val
            }
            def lc = new LineChart().addValues(latValues)
            lc.setColour(line.color)
            lc.setDotSize(3)
            lc.setTooltip(line.title + "<br>#x_label#<br>#val# " + 
                message(code:"general.measurement.milliseconds.short"))
            lc.setText(line.title)
            c.addElements(lc)

            addXAxis(c, chartData.keySet(), ts.pattern)
            
            c.setLegend(new Legend())
            
            addYAxis(c, minValue, maxValue)            
        }

        c.setBackgroundColour("#FFFFFF")
        c
    }

    def DISKSPACE_CHART = {
        render getDiskspaceChart()
    }

    def getDiskspaceChart = {

        // if a repo.id is provided, render diskspace usage
        // for that repo; otherise, show usage for all repos (repo parent dir)
        def repo = Repository.get(params.repoId)
        Chart c
        def ts = getTimespan()
        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()
        def lines = []
        def chartTitle
        if (!repo) {
            lines = [[type: "sysUsed", color: colors[0],
                    title: message(code: "statistics.graph.space.root.title")],
                    [type: "repoUsed", color: colors[1],
                    title: message(code: "statistics.graph.space.repos.title")],
                    [type: "repoFree", color: colors[2],
                    title: message(
                        code: "statistics.graph.space.vol.title")]]
            chartTitle = message(code: "statistics.graph.space.title") + " " +
                ts.title
        }
        else {
            lines = [[type: "repoUsed", color: colors[0],
                    title: message(code: "statistics.graph.repo.size")]]
            chartTitle = message(code: "statistics.graph.repo.over") + ts.title
        }
        
        def chartValues = (repo) ?
                fileSystemStatisticsService.getChartValues(start.getTime(), now.getTime(), repo) :
                fileSystemStatisticsService.getChartValues(start.getTime(), now.getTime())
        
        if (!chartValues) {
            c = new Chart(chartTitle + " : " +
                message(code: "statistics.graph.noDataYet"))
        } else {
            def minValue = 0
            def maxValue = 0

            def valueSets = lines.collect {
                def values = chartValues.keySet().collect { key ->
                    chartValues.get(key)[it.type]
                }
                maxValue = Math.max(maxValue, values.max()?: 0)
                values
            }

            def magPrefix = getByteMagPrefix(maxValue)
            def unitPrefix = magPrefix[1]
            def mag = magPrefix[0]
            def scaledSets = valueSets
            if (mag) {
                scaledSets = valueSets.collect { vs ->
                    vs.collect { value ->
                        networkStatisticsService.applyMag(value, mag, 2)
                    }

                }
                maxValue = networkStatisticsService.applyMag(maxValue, mag, 2)
            }

            def lineCharts = [] 
            lines.eachWithIndex { line, index ->
                def lineChart = new LineChart()
                    .addValues(scaledSets[index])
                lineChart.setColour(line.color)
                lineChart.setDotSize(3)
                lineChart.setTooltip(line.title + "<br>#x_label#<br>#val# " +
                                         unitPrefix + "B")
                lineChart.setText(line.title)
                lineCharts << lineChart
            } 

            c = new Chart(chartTitle)
                .addElements(lineCharts)
            
            addXAxis(c, chartValues.keySet(), ts.pattern)
            
            c.setLegend(new Legend())
            
            addYAxis(c, minValue, maxValue)                                
        }

        c.setBackgroundColour("#FFFFFF")
        c
    }

    def SVN_HITS_BY_REPO = {
        render getSvnHitsByRepoChart()
    }

    def getSvnHitsByRepoChart = {
        Chart c
        def ts = getTimespan()
        def start = new Date(System.currentTimeMillis() 
                             - 1000 * Long.valueOf(ts.seconds))
        def now = new Date()
 
        def lines = []
        Repository.list(sort:"name", order:"asc").eachWithIndex { repo, i ->
            def colorIndex = i % colors.size()
            lines << [type: repo.getName(), color: colors[colorIndex],
                title: repo.getName()]
        }

        def chartData = svnStatisticsService
            .getChartValues(start.getTime(), now.getTime())
        

        if (!chartData) {
            c = new Chart(message(code: "statistics.graph.space.repos") + " " +
                ts.title + " : " + message(code: "statistics.graph.noDataYet"))
        } else {
            c = new Chart(message(code: "statistics.graph.space.repos") +
                ts.title)
            def minValue = 0
            def maxValue = 0

            def valueSets = lines.collect {
                def values = chartData.keySet().collect { key ->
                                   chartData.get(key)[it.type]
                }
                maxValue = Math.max(maxValue, values.max()?: 0)
                values
            }

            def lineCharts = [] 
            lines.eachWithIndex { line, index ->
                def lineChart = new LineChart()
                    .addValues(valueSets[index])
                lineChart.setColour(line.color)
                lineChart.setDotSize(3)
                lineChart.setTooltip(line.title + "<br>#x_label#<br>#val#")
                lineChart.setText(line.title)
                lineCharts << lineChart
            } 

            c = c.addElements(lineCharts)

            addXAxis(c, chartData.keySet(), ts.pattern)
            
            c.setLegend(new Legend())
            
            addYAxis(c, minValue, maxValue)            
        }

        c.setBackgroundColour("#FFFFFF")
        c
    }

    /**
     * Returns a number rounded up to the next even value of the same 
     * magnitude.  For example, 1 -> 1, 153 -> 200, 5423 -> 6000.
     */
    def roundToUpperMagnitude(number) {
        def rounded = 0
        def neg = false
        if (number < 0) {
            neg = true
            number = -number
        }

        for(int mag=0; ; mag++) {
            if (number < 10**mag) {
                rounded = ((number / 10**(mag - 1)).intValue() + 1) \
                          * 10**(mag - 1)
                break
            }
        }

        if (neg) {
            rounded = -rounded
        }
        return rounded
    }

    // return the magnitude and prefix for a byte value
    public static getByteMagPrefix(number) {
       def prefixes = ['', 'K', 'M', 'G', 'T', 'P', 'E']
       def mag = prefixes.size() - 1
       for (int i = 0; i < prefixes.size(); i++) {
           if (number < 1024**(i + 1)) {
               mag = i
               break
           }
       }
       
       return [mag, prefixes[mag]]
   }

   def addXAxis = { chart, timeValues, pattern ->
       def xaxis = new XAxis()
       def labels = timeValues.collect {
           def date = new Date(it)
           SimpleDateFormat formatter = new SimpleDateFormat(pattern)
           formatter.format(date)
       }
       xaxis.addLabels(labels)
       def labelSize = labels.size() - 1
       if (labelSize <= 0) { labelSize = 1 }
       def labelStep = ((labelSize) / NUM_XLABELS).intValue()
       if (labelStep <= 0) { labelStep = 1 }
       xaxis.getLabels().setRotation(Label.Rotation.VERTICAL)
       xaxis.getLabels().setSteps(labelStep)
       xaxis.setRange(0, labelSize, labelStep)
       chart.setXAxis(xaxis)
   }

   def addYAxis = { chart, minValue, maxValue, round=true ->
       def yaxis = new YAxis()
       if (!maxValue) {
           maxValue = 1
       }
       if (round) {
           maxValue = roundToUpperMagnitude(maxValue)
       }
       yaxis.setMax(maxValue)
       yaxis.setMin(minValue)
       def ysteps = ((maxValue - minValue) / NUM_YLABELS).intValue()
       if (!ysteps) { ysteps = 1 }
       yaxis.setSteps(ysteps)
       chart.setYAxis(yaxis)
   }
}
