<!DOCTYPE html>
<html>
	<head>
		<title>Opal import plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-data.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript">
			$(function() {	
				parent.hideSpinner(); 
				
				$('form').submit(function() {
					$('.btn').attr("disabled", "disabled");
					parent.showSpinner(); 
					return true;
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<a href="/plugin/opal/import/logout" class="btn pull-right" style="margin-top: 10px">Logout</a>
    				
    		<#if successMessage??>
    			<div style="color:green">${successMessage}</div>
    		</#if>
    				
    		<#if errorMessage??>
    			<div style="color:red">${errorMessage}</div>
    		</#if>
    			
			<div class="row-fluid">		
				<form id="importTablesForm" class="form-horizontal" action='/plugin/opal/import-tables' method="POST">
					<div id="legend">
    					<legend>Tables in datasource '${datasource.name}'</legend>
    				</div>
    				
    				<table class="table table-striped">  
        				<thead>  
          					<tr>  
            					<th>Name</th>  
            					<th>Entity Type</th>  
            					<th>Import</th>
          					</tr>  
        				</thead>  
        				<tbody>  
          					<#list tables as table>
          						<tr>
          							<td>${table.name}</td>
          							<td>${table.entityType}</td>
          							<td><input type="checkbox" name="tables" value="${table.name}" /></td>
          						</tr>
          					</#list>
        				</tbody>  
      				</table>  
      				<button id="submitButton" class="btn">Import tables from Opal</button>
      			</form>
			</div>
			
			<br />
			<br />
			<div class="row-fluid">	
				<form id="exportDataSetsForm" class="form-horizontal" action='/plugin/opal/export-datasets' method="POST">
					<div id="legend">
    					<legend>Datasets in Molgenis</legend>
    				</div>
    				
    				<table class="table table-striped">  
        				<thead>  
          					<tr>  
            					<th>Name</th>  
            					<th>Export</th>
          					</tr>  
        				</thead>  
        				<tbody>  
          					<#list dataSets as dataSet>
          						<tr>
          							<td>${dataSet.name}</td>
          							<td><input type="checkbox" name="dataSets" value="${dataSet.id?c}" /></td>
          						</tr>
          					</#list>
        				</tbody>  
      				</table>  
      				<button id="submitButton" class="btn">Export datasets to Opal</button>
      			</form>
			</div>
			
		</div>
	</body>
</html>