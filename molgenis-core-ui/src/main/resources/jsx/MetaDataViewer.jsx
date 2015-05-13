(function(_, React, molgenis) {
    "use strict";
   // var api = new molgenis.RestClient();
    
    var MetaDataViewer = React.createClass({
    	render: function() {
    		return <div><PackageViewer /></div>
    	}
    });
   
    
    var PackageViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	render: function() {
    		return <molgenis.ui.BootstrapPanel title="Packages">
    					<table className="table table-condensed table-hover">
    						<thead>
    							<tr><th>Name</th><th>Simple name</th><th>Description</th><th>Parent</th></tr>
    						</thead>
    						<tbody>
    						</tbody>
    					</table>
    				</molgenis.ui.BootstrapPanel>
    	}
    });
    
    // export components
    molgenis.ui = molgenis.ui || {};
    molgenis.ui.MetaDataViewerFactory = React.createFactory(MetaDataViewer);
    
 //   molgenis.ui.MetaDataViewer = React.createFactory(MetaDataViewer);
   // _.extend(molgenis.ui, {PackageViewer: React.createFactory(PackageViewer)});
    //  _.extend(molgenis.ui, {MetaDataViewer: React.createFactory(MetaDataViewer)});
   
}(_, React, molgenis));	

    
 
	

