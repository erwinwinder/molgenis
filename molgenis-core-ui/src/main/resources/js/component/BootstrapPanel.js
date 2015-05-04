(function(React) {
    "use strict";
    
    var h4 = React.DOM.h4, div = React.DOM.div;
    
    var BootstrapPanel = React.createClass({
    	displayName: 'BootstrapPanel',
		propTypes: {
			title : React.PropTypes.string.isRequired
		},
    	render: function() {
    		return div({className: 'panel'},
    				div({className: 'panel-heading'},
    					h4({className: 'panel-title'}, this.props.title)
    				),
    				div({className: 'panel-body'}, this.props.children)
    			);
    	}
    });
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {BootstrapPanel: React.createFactory(BootstrapPanel)});
}(React));	