<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

    <!--
        This transform will result in two separate empty output elements.
    -->
    
    <xsl:template match="firstValue|secondValue">
        <xsl:copy/>
    </xsl:template>
     
</xsl:stylesheet>