<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['transmart.js']>

<@header css js/>

<h2>Import study from transMART</h2>

<form name="transmart-form" method="POST">
	<select name="study">
	<#list studies as study>
		<option>${study}</option>
	</#list>
	</select>
	
	<input type="button" id="submit-button" value="Import study" />
</form>

<@footer/>