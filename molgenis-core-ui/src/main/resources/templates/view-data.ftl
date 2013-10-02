<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header ['molgenis-form.css'] />

<div class="well">
	<b>Entities</b>
	<ul class="unstyled">
	<#list entityNames as entityName>
		<li><a href="${context_url}/${entityName}">${entityName}</a><br /></li>
	</#list>
	</ul>
</div>

<#if entityMetaData??>
<table class="table table-bordered table-hover table-striped" style="width: 300px">
	<tbody>
		<tr>
			<td>Writable</td>
			<td>${writable?string('Yes', 'No')}</td>
		</tr>
		<tr>
			<td>Updatable</td>
			<td>${updatable?string('Yes', 'No')}</td>
		</tr>
		<tr>
			<td>Queryable</td>
			<td>${queryable?string('Yes', 'No')}</td>
		</tr>
	</tbody>
</table>

<table class="table table-bordered table-hover table-striped">
	<thead>
		<tr>
		<#list entityMetaData.attributes as attribute>
			<th>${attribute.name}</th>		
		</#list>
		</tr>
	</thead>
	<tbody>
	<#list entities as entity>
		<tr>
		<#list entityMetaData.attributes as attribute>
			<td>${entity.getDisplayValue(attribute.name)}</td>
		</#list>
		</tr>
	</#list>
	</tbody>
</table>
</#if>

<@footer/>