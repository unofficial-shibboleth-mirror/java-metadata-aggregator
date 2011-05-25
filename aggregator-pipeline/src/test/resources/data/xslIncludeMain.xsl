<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    
    <!-- type of fruit to use when transforming firstValue; defaults to bananas -->
    <xsl:param name="fruit">bananas</xsl:param>
    
    <xsl:template match="firstValue">
        <firstValue><xsl:value-of select="."/><xsl:text > </xsl:text><xsl:value-of select="$fruit"/></firstValue>
    </xsl:template>
    
    <xsl:include href="xslIncluded.xsl"/>
    
    <!--By default, copy text blocks and attributes unchanged.-->
    <xsl:template match="text()|@*">
        <xsl:copy/>
    </xsl:template>
    
    <!--By default, copy all elements from the input to the output, along with their attributes and contents.-->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
