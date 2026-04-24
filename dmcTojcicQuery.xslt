<xsl:stylesheet version="1.0" 
			xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
            xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
			xmlns="http://visionplus.firstdata.com/XMLschema">
 <xsl:output omit-xml-declaration="yes" method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
 <!-- <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>-->
 <xsl:strip-space elements="*"/>
  <xsl:template match="SOAP-ENV:Envelope/SOAP-ENV:Body/DMC_ROOT">
    <xsl:apply-templates select="DMC_MSGOUT"/>
  </xsl:template>
  <xsl:template match="SVC_RTN_AREA"/>
  <xsl:template match="FIELD_CODE_AREA">
   <soap-env:Envelope xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="http://webService.jcic.pro.com">
	  <soap-env:Header/>
	   <soap-env:Body>
			<ns:queryJCIC>
				<ns:systemName>
				  <xsl:value-of select="fc_5001"/>
				</ns:systemName>
				<ns:timeStr>
				  <xsl:value-of select="fc_5002"/>
				</ns:timeStr>
				<ns:macValue>
				  <xsl:value-of select="fc_5003"/>
				</ns:macValue>
				<ns:querykey1>
				  <xsl:value-of select="fc_5004"/>
				</ns:querykey1>
				<ns:querykey2>
				  <xsl:value-of select="fc_5005"/>
				</ns:querykey2>
				<ns:txtId>
				  <xsl:value-of select="fc_5006"/>
				</ns:txtId>
				<ns:inquiryReason>
				  <xsl:value-of select="fc_5007"/>
				</ns:inquiryReason>
				<ns:sendJCIC>
				  <xsl:value-of select="fc_5008"/>
				</ns:sendJCIC>
				<ns:queryId>
				  <xsl:value-of select="fc_5009"/>
				</ns:queryId>
				<ns:queryName>
				  <xsl:value-of select="fc_5010"/>
				</ns:queryName>
				<ns:chargeBranchCode>
				  <xsl:value-of select="fc_5011"/>
				</ns:chargeBranchCode>
				<ns:rspType>
				  <xsl:value-of select="fc_5012"/>
				</ns:rspType>
				<ns:queryDepCode>
				  <xsl:value-of select="fc_5013"/>
				</ns:queryDepCode>
				<ns:queryDepName>
				  <xsl:value-of select="fc_5014"/>
				</ns:queryDepName>
				<ns:txSerialNo>
				  <xsl:value-of select="fc_5015"/>
				</ns:txSerialNo>
			</ns:queryJCIC>
	   </soap-env:Body>
	</soap-env:Envelope>
  </xsl:template>
</xsl:stylesheet>