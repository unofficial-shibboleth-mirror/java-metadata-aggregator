<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template match="secondValue">
        <secondValue><xsl:attribute name="value"><xsl:value-of select=".+1"/></xsl:attribute></secondValue>
    </xsl:template>
    
</xsl:stylesheet>
