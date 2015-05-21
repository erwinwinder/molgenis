(function(React, molgenis) {
    "use strict";
    
    var BootstrapPanel = React.createClass({
    	displayName: 'BootstrapPanel',
		propTypes: {
			title : React.PropTypes.string.isRequired
		},
    	render: function() {
    		return 	<div className="panel">
    			   		<div className="panel-heading">
    			   			<h4 className="panel-title">{this.props.title}</h4>
    			   		</div>
    			   		<div className="panel-body">
    			   			{this.props.children}
    			   		</div>
    				</div>
    	}
    });
    
    // export component
    molgenis.ui = molgenis.ui || {};
    molgenis.ui.BootstrapPanel = BootstrapPanel;
    
}(React, molgenis));	