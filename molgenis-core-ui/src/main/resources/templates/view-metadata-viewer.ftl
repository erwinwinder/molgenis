<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header />

<div class="row">
	<div class="col-md-1"></div>
	<div class="col-md-10" id="MetaDataViewerHolder"></div>
	<div class="col-md-1"></div>
</div>

<script type="text/jsx">
	React.render(molgenis.ui.MetaDataViewerFactory(), document.getElementById('MetaDataViewerHolder'));
</script>

<@footer/>