((_, React, molgenis) => {
    "use strict";
    molgenis.ui = molgenis.ui || {};
    molgenis.ui.MetaDataViewerFactory = React.createFactory(MetaDataViewer);
    var BootstrapPanel = molgenis.ui.BootstrapPanel;
    var ComponentBase = molgenis.ui.ComponentBase;
    
    class MetaDataViewer extends ComponentBase {
    	constructor(props) {
    		super(props);
    		this.state = {selectedPackage: null};
    	}
    	
    	render() {
    		return 	<div>
    					<PackageViewer onPackageChange={(p) => this.setState({selectedPackage:p})} />
    					<EntityViewer pack={this.state.selectedPackage} />
    				</div>
    	}
	}
    
    class QueryBuilder {
    	constructor() {
    		this.rules = [];
    		this.expand = [];
    	}
    	
    	eq(attrName, value) {
    		this.rules.push({field: attrName, operator:'EQUALS', value: value})
    		return this;
    	}
    	
    	exp(attrName) {
    		this.expand.push(attrName);
    		return this;
    	}
    	
    	build() {
    		var q = {q: this.rules};
    		if (this.expand.length > 0) {
    			_.extend(q, {expand: this.expand})
    		}
    		
    		return q;
    	}
    }
    
    class EntityViewer extends ComponentBase {
    	
    	constructor(props) {
    		super(props);
    		this.state = {entities: null};
    	}
    	
    	componentWillReceiveProps(nextProps) {
			if (nextProps.pack !== null) {
				this.setState({
					entities: this._showPackageEntities(nextProps.pack)
				});
			}
		}
    	
    	_showPackageEntities(p) {
    		this.getAsync('/api/v1/entities', new QueryBuilder().eq('package', p.fullName).exp('attributes')).done((data) => {
    			this.setState({
    				entities: data,
    			});
    		});
    	}
    	
    	render() {
    		var entities = this.state.entities;
    		
    		return 	<BootstrapPanel title="Entities">
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
    							{entities ? entities.items.map((e, i) => {
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
    				</BootstrapPanel>
    	}
    }
    
    EntityViewer.propTypes = {pack: React.PropTypes.object};
    EntityViewer.defaultProps = {pack: null};
    
    class PackageViewer extends ComponentBase {
    	constructor(props) {
    		super(props);
    		this.state = {
    				selectedPackage: null,
    				packageData: null
    			};
    	}
    	 
    	componentDidMount() {
    		this.getAsync('/api/v1/packages', new QueryBuilder().exp('parent')).done((data) => {
    			this.setState({
					packageData: data,
					selectedPackage: data.items.length > 0 ? data.items[0] : null
				});
    			if (this.props.onPackageChange && (data.items.length > 0)) {
    				this.props.onPackageChange(data.items[0]);
    			}
    		});
    	}
    	
    	_onRowClick(p) {
			this.setState({selectedPackage: p});
			if (this.props.onPackageChange) {
				this.props.onPackageChange(p);
			}
		}
		
    	render() {
    		var packageData = this.state.packageData;
    		var selectedPackage = this.state.selectedPackage;
        	
    		return <BootstrapPanel title="Packages">
    					<table className="table table-condensed table-hover">
    						<thead>
    							<tr><th>Name</th><th>Simple name</th><th>Description</th><th>Parent</th></tr>
    						</thead>
    						<tbody>
    							{packageData ? packageData.items.map((p, i) => {
									return  <tr key={i} 
												onClick={this._onRowClick.bind(this, p)} 
												className={selectedPackage.fullName == p.fullName ? 'info' : ''}>
												<td key="1">{p.fullName}</td>
												<td key="2">{p.name}</td>
												<td key="3">{p.description}</td>
												<td key="4">{p.parent ? p.parent.fullName : ''}</td>
											</tr>}) : ''}
    						</tbody>
    					</table>
    				</BootstrapPanel>
    	}
    }
    
    PackageViewer.propTypes = {onPackageChange: React.PropTypes.func};
     
})(_,React, molgenis);	