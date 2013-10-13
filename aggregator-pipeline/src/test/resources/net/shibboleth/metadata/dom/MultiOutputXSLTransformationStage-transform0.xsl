<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!--
        This transform will result in no output.
    -->
    
    <!-- Throw everything away text blocks and attributes unchanged. -->
    <xsl:template match="*|text()|@*">
        <!-- do nothing -->
    </xsl:template>
    
</xsl:stylesheet>
