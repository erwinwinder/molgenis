<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['molgenis-form.css']>
<#assign js=[]>

<@header css js/>

<div class="container-fluid">
	<div class="row">
		<div class="col-md-12">
			<legend>Workflows</legend>
			<table class="table table-condensed table-bordered">
				<thead>
					<tr>
						<th>Name</th>
						<th>Description</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<#if workflows?has_content>
						<#list workflows as workflow>
							<tr>
								<td>${workflow.name}</td>
								<td>${workflow.description!}</td>
								<td><a href="workflow/${workflow.name?url('UTF-8')}">view</a></td>
							</tr>
						</#list>
					</#if>
				</tbody>
			</table>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<form class="form-inline" role="form" action="workflow/add" method="POST">
				<input type="text" name="workflowFolder" class="form-control" placeholder="Enter workflow folder" />
				<input type="submit" value="import">
			</form>
		</div>
	</div>
</div>

<@footer/>