<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!--
        This transform will result in two separate empty output elements.  Each of these
        will have two InfoStatus, an ErrorStatus and a WarningStatus attached to them,
        as well as any previously attached.
    -->
    
    <xsl:template match="/*">
        <xsl:message terminate="no">
            <xsl:text>[INFO] </xsl:text>
            <xsl:value-of select="name()"/>
        </xsl:message>
    </xsl:template>
     
    <!-- Recurse down through all elements by default. -->
    <xsl:template match="*">
        <xsl:apply-templates select="node()|@*"/>
    </xsl:template>
    
    <!-- Discard text blocks, comments and attributes by default. -->
    <xsl:template match="text()|comment()|@*">
        <!-- do nothing -->
    </xsl:template>
    
</xsl:stylesheet>
