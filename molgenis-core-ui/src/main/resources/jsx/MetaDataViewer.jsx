(function(_, React, molgenis) {
    "use strict";
    var api = new molgenis.RestClient();
    
    var MetaDataViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	getInitialState: function() {
			return {
				selectedPackage: null,
			};
		},
		_onPackageChange: function(p) {
			this.setState({selectedPackage: p})
		},
    	render: function() {
    		return 	<div>
    					<PackageViewer onPackageChange={this._onPackageChange} />
    					<EntitiesViewer pack={this.state.selectedPackage} />
    				</div>
    	}
    });
   
    var EntitiesViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	getInitialState: function() {
			return {
				entities: null,
			};
		},
		propTypes: {
			pack: React.PropTypes.object
		},
		getDefaultProps: function() {
			return {
				pack: null
			};
		},
		componentWillReceiveProps : function(nextProps) {
			if (nextProps.pack !== null) {
				this.setState({
					entities: this._showPackageEntities(nextProps.pack)
				});
			}
		},
		_showPackageEntities: function(p) {
			var q = {
    			q:[{field:'package', operator:'EQUALS', value: p.fullName }],
    			expand: ['attributes']
    		}
    		
    		api.getAsync('/api/v1/entities', q).done(function(data) {
    			this.setState({
    				entities: data,
    			});
    			
    		}.bind(this));
    	},
    	render: function() {
    		var entities = this.state.entities;
    		
    		return 	<molgenis.ui.BootstrapPanel title="Entities">
    					<table className="table table-condensed table-hover">
    						<thead>
    							<tr>
    								<th>Name</th>
    								<th>Simple name</th>
    								<th>Backend</th>
    								<th>Id Attribute</th>
    								<th>Label Attribute</th>
    								<th>Abstract</th>
    								<th>Label</th>
    								<th>Extends</th>
    								<th>Description</th>
    							</tr>
    						</thead>
    						<tbody>
    							{entities ? entities.items.map(function(e, i) {
    								return  <tr key={i} >
    											<td key="1">{e.fullName}</td>
    											<td key="2">{e.simpleName}</td>
    											<td key="3">{e.backend}</td>
    											<td key="4">{e.idAttribute}</td>
    											<td key="5">{e.labelAttribute}</td>
    											<td key="6">{e.abstract}</td>
    											<td key="7">{e.label}</td>
    											<td key="8">{e.extends}</td>
    											<td key="9">{e.description}</td>
    										</tr>}) : ''}
    						</tbody>
    					</table>
    				</molgenis.ui.BootstrapPanel>
    		}
    });
    
    var PackageViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	propTypes: {
			onPackageChange: React.PropTypes.func
		},
		componentDidMount: function() {
    		api.getAsync('/api/v1/packages', {'expand': ['parent']}).done(function(data) {
    			this.setState({
					packageData: data,
					selectedPackage: data.items.length > 0 ? data.items[0] : null
				});
    			if (this.props.onPackageChange && (data.items.length > 0)) {
    				this.props.onPackageChange(data.items[0]);
    			}
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
			if (this.props.onPackageChange) {
				this.props.onPackageChange(p);
			}
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
   
}(_, React, molgenis));	

    
 
	

