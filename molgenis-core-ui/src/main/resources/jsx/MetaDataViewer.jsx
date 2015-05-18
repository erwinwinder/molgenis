(function(_, React, molgenis) {
    "use strict";
    var api = new molgenis.RestClient();
    
    var MetaDataViewer = React.createClass({
    	render: function() {
    		return <div><PackageViewer /></div>
    	}
    });
   
    var PackageViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	componentDidMount: function() {
    		api.getAsync('/api/v1/packages', {'expand': ['parent']}).done(function(data) {
    			this.setState({
					packageData: data,
					selectedPackage: data.items.length > 0 ? data.items[0] : null
				});
    		}.bind(this));
    	},
    	getInitialState: function() {
			return {
				selectedPackage: null,
				packageData: null
			};
		},
		_onRowClick: function(p) {
			this.setState({selectedPackage: p});
		},
    	render: function() {
    		var packageData = this.state.packageData;
    		var selectedPackage = this.state.selectedPackage;
        	var self = this;
        	
    		return <molgenis.ui.BootstrapPanel title="Packages">
    					<table className="table table-condensed table-hover">
    						<thead>
    							<tr><th>Name</th><th>Simple name</th><th>Description</th><th>Parent</th></tr>
    						</thead>
    						<tbody>
    							{packageData ? packageData.items.map(function(p, i) {
									return  <tr key={i} 
												onClick={self._onRowClick.bind(self, p)} 
												className={selectedPackage.fullName == p.fullName ? 'info' : ''}>
												<td key="1">{p.fullName}</td>
												<td key="2">{p.name}</td>
												<td key="3">{p.description}</td>
												<td key="4">{p.parent ? p.parent.fullName : ""}</td>
											</tr>}) : ''}
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

    
 
	

