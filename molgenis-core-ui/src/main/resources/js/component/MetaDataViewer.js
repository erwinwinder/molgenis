(function(_, React, molgenis) {
    "use strict";

    var table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, td = React.DOM.td, th = React.DOM.th;
    var h4 = React.DOM.h4, div = React.DOM.div;
    var api = new molgenis.RestClient();
    
    var dispatcher = {
    	_onPackageSelectListeners: [],
    	_onEntitySelectListeners:[],
    	registerOnPackageSelectListener: function(listener) {
    		this._onPackageSelectListeners.push(listener);
    	},
    	onPackageSelect: function(p) {
    		_.each(this._onPackageSelectListeners, function(listener) {
    			listener({pack: p});
    		});
    	},
    	registerOnEntitySelectListener: function(listener) {
    		this._onEntitySelectListeners.push(listener);
    	},
    	onEntitySelect: function(e) {
    		_.each(this._onEntitySelectListeners, function(listener) {
    			listener({entity: e});
    		});
    	}
    };
	
    var MetaDataViewer = React.createClass({
    	render: function() {
    		return div({}, molgenis.ui.PackageViewer({}), molgenis.ui.EntitiesViewer({}), molgenis.ui.AttributesViewer({}));
    	}
    });
    
    var EntitiesViewer = React.createClass({
    	componentWillMount: function() {
    		dispatcher.registerOnPackageSelectListener(this._showPackageEntities);
    	},
    	getInitialState: function() {
			return {
				entities: null,
				selectedEntity: null
			};
		},
		_onRowClick: function(e) {
			this.setState({selectedEntity: e});
			dispatcher.onEntitySelect(e);
		},
    	_showPackageEntities: function(e) {
    		var q = {
    			q:[{field:'package', operator:'EQUALS', value: e.pack.fullName }],
    			expand: ['attributes']
    		}
    		
    		api.getAsync('/api/v1/entities', q).done(function(data) {
    			this.setState({
    				entities: data,
    				selectedEntity: data.items.length > 0 ? data.items[0] : null
    			});
    			
    			dispatcher.onEntitySelect(this.state.selectedEntity);
    		}.bind(this));
    	},
    	render: function() {
    		var rows = [];
    		var entities = this.state.entities;
    		
    		if (entities !== null) {
    			for (var i = 0; i < entities.items.length; i++) {
    				var e = entities.items[i];
    				var cells = [];
    				cells.push(td({key: '1'}, e.fullName));
    				cells.push(td({key: '2'}, e.simpleName));
    				cells.push(td({key: '3'}, e.backend));
    				cells.push(td({key: '4'}, e.idAttribute));
    				cells.push(td({key: '5'}, e.labelAttribute));
    				cells.push(td({key: '6'}, 'e.abstract'));
    				cells.push(td({key: '7'}, e.label));
    				cells.push(td({key: '8'}, 'e.extends'));
    				cells.push(td({key: '9'}, e.description));
    				
    				var props = {
        				key: '' + i, 
        				onClick: this._onRowClick.bind(this, e)
        			}
        				
        			if (this.state.selectedEntity.fullName === e.fullName) {
        				props.className = 'info';
        			} 
    				
    				rows.push(tr(props, cells));
    			}
    		}
    		
    		var entitiesTable = table({className: 'table table-condensed table-hover'},
					thead({},
						tr({},
							th({}, 'Name'), th({}, 'Simple name'), th({}, 'Backend'), th({}, 'Id Attribute'), th({}, 'Label Attribute')
							, th({}, 'Abstract'), th({}, 'Label'), th({}, 'Extends'), th({}, 'Description')
						)
					),
					tbody({}, rows)
				);

    		return molgenis.ui.BootstrapPanel({title: 'Entities'}, entitiesTable);
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
    			
    			if (this.state.selectedPackage !== null) {
    				dispatcher.onPackageSelect(this.state.selectedPackage);
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
			dispatcher.onPackageSelect(p);
		},
    	render: function() {
    		var rows = [];
    		var packageData = this.state.packageData;
    		
    		if (packageData !== null) {
    			for (var i = 0; i < packageData.items.length; i++) {
    				var p = packageData.items[i];
    				var cells = [];
    				cells.push(td({key: '1'}, p.fullName));
    				cells.push(td({key: '2'}, p.name));
    				cells.push(td({key: '3'}, p.description));
    				
    				if (p.parent) {
    					cells.push(td({key: '4'}, p.parent.fullName));
    				} else {
    					cells.push(td({key: '4'}));
    				}
    				
    				var props = {
    					key: '' + i, 
    					onClick: this._onRowClick.bind(this, p)
    				}
    				
    				if (this.state.selectedPackage.fullName === p.fullName) {
    					props.className = 'info';
    				} 
    				
    				rows.push(tr(props, cells));
    			}
    		}
    		
    		var packagesTable = table({className: 'table table-condensed table-hover'},
    								thead({},
    									tr({},
    										th({}, 'Name'), th({}, 'Simple name'), th({}, 'Description'), th({}, 'Parent')
    									)
    								),
    								tbody({}, rows)
    							);
    		
    		return molgenis.ui.BootstrapPanel({title: 'Packages'}, packagesTable);
    	}
    });
    
    var AttributesViewer = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
    	getInitialState: function() {
			return {attributes: null};
		},
    	componentWillMount: function() {
    		dispatcher.registerOnEntitySelectListener(this._showEntityAttributes);
    	},
    	render: function() {
    		var rows = [];
    		var attributes = this.state.attributes;
    		
    		if (attributes !== null) {
    			for (var i = 0; i < attributes.length; i++) {
    				var attr = attributes[i];
    				var cells = [];
    				cells.push(td({key: '1'}, attr.name));
    				cells.push(td({key: '2'}, attr.dataType));
    				cells.push(td({key: '3'}, attr.nillable + ''));
    				cells.push(td({key: '4'}, attr.description));
    				
    				rows.push(tr({key: '' + i}, cells));
    			}
    		}

    		var attributesTable = table({className: 'table table-condensed table-hover'},
    								thead({},
    									tr({},
    										th({}, 'Name'), th({}, 'Data Type'), th({}, 'Nillable'), th({}, 'Description')
    									)
    								),
    								tbody({}, rows)
    							);
    		
    		return molgenis.ui.BootstrapPanel({title: 'Attributes'}, attributesTable);
    	},
    	_showEntityAttributes: function(e) {
    		this.setState({attributes: e.entity !== null ? e.entity.attributes.items : []});
    	}
    });
    
    // export components
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {EntitiesViewer: React.createFactory(EntitiesViewer)});
    _.extend(molgenis.ui, {PackageViewer: React.createFactory(PackageViewer)});
    _.extend(molgenis.ui, {AttributesViewer: React.createFactory(AttributesViewer)});
    _.extend(molgenis.ui, {MetaDataViewer: React.createFactory(MetaDataViewer)});
    
}(_, React, molgenis));	