<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['joint.min.css']>
<#assign js=['lodash.js', 'backbone-min.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.layout.DirectedGraph.min.js']>

<@header css js/>

<div class="row">
    <div class="col-md-12">
        <a href="${context_url}" class="btn btn-default btn-md pull-left"><span class="glyphicon glyphicon-chevron-left"></span> Back workflows</a>
    </div>
</div>

<div class="row" style="margin-top: 10px;">
	<div class="col-md-12">
		<div class="well">
			<h3>${workflow.name}</h3>
		</div>
	</div>
</div>

<div class="row" style="margin-top: 10px;">
	<div class="col-md-12">
		<div id="paper" style="width: 1200px; height:600px;"></div>
	</div>
</div>

<script>
	<#include "view-steps.ftl">
</script>

<@footer />