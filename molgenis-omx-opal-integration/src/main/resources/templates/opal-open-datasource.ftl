<#import "/spring.ftl" as spring />

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
			
				$('#openDatasourceForm').submit(function() {
					$('#submitButton').attr("disabled", "disabled");
					parent.showSpinner(); 
					return true;
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<form id="openDatasourceForm" class="form-horizontal" action='/plugin/opal/open-datasource' method="POST">
    				<fieldset>
    					<div id="legend">
    						<legend class="">Open datasource</legend>
    					</div>
    					
    					<#if errorMessage??>
    						<div class="control-group">
    							<div class="controls">
    								<div class="validationerror">${errorMessage}</div>
    							</div>
    						</div>
    					</#if>
    					
    					<div class="control-group">
    						<@spring.bind "openDatasourceForm.name" />
							<label class="control-label" for="name">Name</label>
   					 		<div class="controls">
    							<input type="text" id="name" name="name" placeholder="" value="${openDatasourceForm.name!}" class="input-xlarge" <#if spring.status.error>style="border-color:red"</#if>>
    							<#if spring.status.error>
    								<span class="validationerror">${spring.status.errorMessage}</span>
    							</#if>
    						</div>
    					</div>
    					
    					<div class="control-group">
    						<@spring.bind "openDatasourceForm.url" />
							<label class="control-label" for="url">Opal server url</label>
   					 		<div class="controls">
    							<input type="text" id="url" name="url" placeholder="" value="${openDatasourceForm.url!}" class="input-xlarge" <#if spring.status.error>style="border-color:red"</#if>>
    							<#if spring.status.error>
    								<span class="validationerror">${spring.status.errorMessage}</span>
    							</#if>
    						</div>
    					</div>
    					
    					<div class="control-group">
    						<@spring.bind "openDatasourceForm.username" />
							<label class="control-label" for="username">Username</label>
   					 		<div class="controls">
    							<input type="text" id="username" name="username" placeholder="" value="${openDatasourceForm.username!}" class="input-xlarge" <#if spring.status.error>style="border-color:red"</#if>>
    							<#if spring.status.error>
    								<span class="validationerror">${spring.status.errorMessage}</span>
    							</#if>
    						</div>
    					</div>
    					
    					<div class="control-group">
    						<@spring.bind "openDatasourceForm.password" />
							<label class="control-label" for="password">Password</label>
   					 		<div class="controls">
    							<input type="password" id="password" name="password" placeholder="" value="${openDatasourceForm.password!}" class="input-xlarge" <#if spring.status.error>style="border-color:red"</#if>>
    							<#if spring.status.error>
    								<span class="validationerror">${spring.status.errorMessage}</span>
    							</#if>
    						</div>
    					</div>
    					
    					<div class="control-group">
    						<@spring.bind "openDatasourceForm.remoteName" />
							<label class="control-label" for="remoteName">Remote name</label>
   					 		<div class="controls">
    							<input type="text" id="remoteName" name="remoteName" placeholder="" value="${openDatasourceForm.remoteName!}" class="input-xlarge" <#if spring.status.error>style="border-color:red"</#if>>
    							<#if spring.status.error>
    								<span class="validationerror">${spring.status.errorMessage}</span>
    							</#if>
    						</div>
    					</div>
    					
    					<div class="control-group">
    						<div class="controls">
    							<button id="submitButton" class="btn">Open Datasource</button>
    						</div>
    					</div>
    					
    				</fieldset>
    			</form>
			</div>
		</div>
	</body>
</html>