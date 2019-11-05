import java.nio.file.Paths

// .+with (\d+) threads and (\d+) Mb .+\n.*WRITE.+95=([0-9\.]+).+throughputPerThread=([0-9\.]+).+
rawData = [:]
currHead = []
Paths.get("/Users/maxim/test-results/c5n.2xlarge_GCM.txt").readLines().forEach { line ->
    def heading = line =~ /.+with (\d+) threads and (\d+) Kb .+/
    if (heading.matches()) {
        currHead = [heading.group(1), heading.group(2)]
    }

    def dataEntry = line =~ /.+ - ([A-Z]{2,}) .+95=([0-9.]+).+throughputPerThread=([0-9.]+).+/
    if (dataEntry.matches()) {
        def op = dataEntry.group(1)
        if (!(op in rawData)) {
            rawData[op] = [:]
        }

        def nThreads = currHead[0]
        if (!(nThreads in rawData[op])) {
            rawData[op][nThreads] = [:]
        }

        def sz = currHead[1]
        if (!(sz in rawData[op][nThreads])) {
            rawData[op][nThreads][sz] = [:]
        }

        def itnum = rawData[op][nThreads][sz].size()
        rawData[op][nThreads][sz][itnum] = [dataEntry.group(2), dataEntry.group(3)]
    }
}

rawData.forEach { op, values ->
    println("$op")
    println("Threads\t100kb\t1mb\t10mb")
    values.forEach { nThreads, bySizes ->
        printMap = [:]
        bySizes.forEach { sz, it ->
            it.forEach { itnum, percentileThroughPutPerThread ->
                if (!(itnum in printMap)) {
                    printMap[itnum] = "$nThreads"
                }
                printMap[itnum] = printMap[itnum] + "\t${Double.parseDouble(percentileThroughPutPerThread[1]) * Integer.parseInt(nThreads)}"
            }
        }
        printMap.forEach { key, val -> println(val) }
    }
}
