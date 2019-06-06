<?xml version="1.0"?>
<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>


    <xsl:output method="text" version="1.0" encoding="UTF-8"/>
    <xsl:param name="lowerlimit">85</xsl:param>
    <xsl:param name="type">LINE</xsl:param>
    <xsl:param name="count" select="count(/report/group/package/class)"/>
    <xsl:param name="summissed" select="sum(//report/group/package/class/counter[@type='LINE']/@missed)"/>
    <xsl:param name="sumcovered" select="sum(//report/group/package/class/counter[@type='LINE']/@covered)"/>
    <xsl:param name="totallines" select="$summissed + $sumcovered"/>
    <xsl:param name="quote" select="100 * $sumcovered div $totallines"/>


    <xsl:template match="//report">
        <xsl:if test=" $quote &lt; $lowerlimit">
            <xsl:result-document href="target/XSL-ERROR.txt" method="text">
                The total code coverate quote is
                <xsl:value-of select="$quote"/> % and thus below the expected minimum of $lowerlimit %
            </xsl:result-document>
            <xsl:message terminate="yes">
                Error: $quote is not above $lowerlimit
            </xsl:message>
        </xsl:if>
        <xsl:if test=" string($quote) = 'NaN' ">
            <xsl:result-document href="target/XSL-ERROR.txt" method="text">
                Stylesheet did not calculate a quote. Propably jacoco did not run or did not create a jacoco.xml
            </xsl:result-document>
            <xsl:message terminate="yes">
                Error: $quote is not above $lowerlimit
            </xsl:message>
        </xsl:if>
        <xsl:result-document href="target/XSL-EVALUATION-RESULT.txt" method="text">
            The total code coverate quote is
            <xsl:value-of select="$quote"/> % and thus above the expected minimum of $lowerlimit %
        </xsl:result-document>
    </xsl:template>
    <!--
        <xsl:template match="//report/group">
            <xsl:variable name="summissed" select="sum(./package/class/counter[@type='LINE']/@missed)"/>
            <xsl:variable name="sumcovered" select="sum(./package/class/counter[@type='LINE']/@covered)"/>
            <xsl:variable name="totallines" select="$summissed + $sumcovered" />
            <xsl:variable name="quote" select="100 * $sumcovered div $totallines" />
            package	 <xsl:value-of select="./@name" /> , <xsl:value-of select="$totallines" /> , <xsl:value-of select="$sumcovered" /> , <xsl:value-of select="$quote" />
        </xsl:template>
    -->

    <!--
        <xsl:template match="//report/group/package/class/counter[@type='LINE']">
            <xsl:variable name="name" select="../@name" />
            <xsl:variable name="missed" select="@missed" />
            <xsl:variable name="covered" select="@covered" />
        </xsl:template>
    -->

</xsl:stylesheet>
