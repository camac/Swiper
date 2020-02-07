<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2013 Cameron Gregor Licensed under the Apache License, Version 
	2.0 (the "License"); you may not use this file except in compliance with 
	the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
	OR CONDITIONS OF ANY KIND, either express or implied. See the License for 
	the specific language governing permissions and limitations under the License -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- Indent the result tree for more consistency when doing a diff - and 
		it looks much nicer :) -->
	<!-- <xsl:output indent="no"/> -->
	<!-- Strip whitespace so that when we remove elements it does not leave 
		ugly blank gaps -->
	<xsl:strip-space elements="*" />

<!-- 	<xsl:template -->
<!-- 		match="//faces-config-extension/composite-component/composite-extension/designer-extension/render-markup/text()"> -->
<!-- 		&lt;?xml version="1.0" encoding="UTF-8"?&gt;&#xd; -->
<!-- 		&lt;xp:view -->
<!-- 		xmlns:xp="http://www.ibm.com/xsp/core"&gt;&#xd; -->
<!-- 		Disable Design -->
<!-- 		Pane&#xd; -->
<!-- 		&lt;/xp:view&gt; -->
<!-- 	</xsl:template> -->


	<!-- For any node not specified in one of the above templates, simply copy 
		it to the result tree. This template is also named so it can be called by 
		call-template -->
	<xsl:template match="node() | @*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*" />
		</xsl:copy>
	</xsl:template>


	<xsl:template match="faces-config/composite-component/composite-extension/designer-extension[not(render-markup)]">
		<xsl:copy>
    		
    		<xsl:copy-of select="@*" />
			
			<xsl:copy-of select="node()"/>
			
    		<render-markup>&lt;?xml version="1.0" encoding="UTF-8"?&gt;&#xd;
&lt;xp:view xmlns:xp="http://www.ibm.com/xsp/core"&gt;&#xd;
Disable Design Pane&#xd;
&lt;/xp:view&gt;</render-markup>

  		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>