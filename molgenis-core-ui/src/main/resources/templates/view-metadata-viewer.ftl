<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header js=['component/BootstrapPanel.js', 'component/MetaDataViewer.js'] />

<div class="row">
	<div class="col-md-1"></div>
	<div class="col-md-10" id="MetaDataViewer"></div>
	<div class="col-md-1"></div>
</div>

<script>
    React.render(molgenis.ui.MetaDataViewer({}), document.getElementById('MetaDataViewer'));
</script>

<@footer/>