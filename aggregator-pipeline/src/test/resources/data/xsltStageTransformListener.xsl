<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

    <!--
        This transform will result in two separate empty output elements.  Each of these
        will have two InfoStatus, an ErrorStatus and a WarningStatus attached to them,
        as well as any previously attached.
    -->
    
    <xsl:template match="firstValue|secondValue">
        <xsl:copy/>
        <xsl:if test="name() = 'firstValue'">
            <xsl:message terminate="no">[WARN]first value</xsl:message>
        </xsl:if>
        <xsl:if test="name() = 'secondValue'">
            <xsl:message terminate="no">[ERROR]       error value</xsl:message>
            <xsl:message terminate="no">[INFO] second value</xsl:message>
            <xsl:message terminate="no">[INFO] second value second message</xsl:message>
        </xsl:if>
    </xsl:template>
     
</xsl:stylesheet>
