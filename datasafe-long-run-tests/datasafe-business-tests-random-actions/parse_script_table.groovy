import groovy.transform.Field

import java.nio.file.Paths

// script expects file name as argument
rawData = [:]
currHead = []
Paths.get(this.args[0]).readLines().forEach { line ->
    def heading = line =~ /.+with (\d+) threads and (\d+) Kb .+/
    if (heading.matches()) {
        currHead = [heading.group(1), heading.group(2)]
    }

    def dataEntry = line =~ /.+ - ([A-Z]{2,}) .+50=([0-9.]+), 99=([0-9.]+), 90=([0-9.]+), 75=([0-9.]+), 95=([0-9.]+).+throughputPerThread=([0-9.]+).+/
    if (dataEntry.matches()) {
        def op = dataEntry.group(1)
        if (!(op in rawData)) {
            rawData[op] = [:]
        }

        def sz = currHead[1]
        if (!(sz in rawData[op])) {
            rawData[op][sz] = [:]
        }

        def nThreads = currHead[0]
        if (!(nThreads in rawData[op][sz][nThreads])) {
            rawData[op][sz][nThreads] = [:]
        }

        def itnum = rawData[op][sz][nThreads].size()
        rawData[op][sz][nThreads][itnum] = [dataEntry.group(2), dataEntry.group(3), dataEntry.group(4), dataEntry.group(5), dataEntry.group(6), dataEntry.group(7)]
    }
}

@Field final String ANSI_RESET = "\u001B[0m"
@Field final String ANSI_RED = "\u001B[31m"
@Field final String ANSI_YELLOW = "\u001B[33m"

rawData.forEach { op, byOpValues ->
    byOpValues.forEach { sz, bySizeValues ->
        println(ANSI_YELLOW + "${op}" + ANSI_RESET + " operation performance with " + ANSI_YELLOW + "${sz}" + ANSI_RESET +" KB objects")
        printSeparator()
        curStr = ["Threads", "Throughput", "Throughput", "p50", "p99", "p90", "p75", "p95"]
        printString(curStr)
        curStr = ["", "ops", "MB/s", "", "", "", "", ""]
        printString(curStr)
        printSeparator()
        bySizeValues.forEach { nThreads, byThreadValues ->
            byThreadValues.forEach { itnum, val ->
                def opsPerSec = Double.parseDouble(val[5]) * Integer.parseInt(nThreads)
                String thr_ops = opsPerSec.round(2).toString()
                String thr_kb = ANSI_RED + (opsPerSec * Integer.parseInt(sz) / 1024).round(2).toString() + ANSI_RESET
                for (int i=0; i < val.size(); i++) {
                    if (val[i].isDouble() && !val[i].isInteger()) {
                        val[i] = Double.parseDouble(val[i]).round(2).toString()
                    }
                }
                curStr = [nThreads, thr_ops, thr_kb, val[0], val[1], val[2], val[3], val[4]]
                printString(curStr)
                printSeparator()
            }
        }
        println()
    }
}

def printSeparator() {
    println(("+"+"-".multiply(10)).multiply(8)+"+")
}

def printString(List<String> params) {
    params.each { print '|' + it.center(it.contains(ANSI_RESET) ? 19 : 10) }
    println("|")
}
