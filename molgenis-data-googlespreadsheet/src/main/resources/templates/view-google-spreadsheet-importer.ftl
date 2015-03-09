<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=[]>
<@header css js/>

<div class="row">
	<div class="col-md-10 col-md-offset-1 well">
		<legend>Import google EMX spreadsheet</legend>
		
		<div class="row">
			<div class="col-md-6">
				<form role="form"  method="post" action="")>
						<div class="row">
							<div class="col-md-12">
								<i>Spreadsheet key:</i>
								<span>
									<input type="text" name="spreadsheetKey" value="1k432kG-QkpTjlA1C0qOjasVzSIBgCoyOWRBYOdUWZ7A" required style="width: 400px"/>
								</span>
								
								<span class="pull-right">
									<input  type="submit" value="Import" class="btn btn-primary"/>
								</span>
							</div>
						</div>
				</form>
			</div>
		</div>
	</div>
</div>

<@footer/>