((React, molgenis) => {
	"use strict";
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.ComponentBase = ComponentBase;
	molgenis.ui.BootstrapPanel = BootstrapPanel;
	
	class ComponentBase extends React.Component {
		constructor(props) {
			super(props);
			this.restClient = new molgenis.RestClient();
		}
		 
		shouldComponentUpdate(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
		}
		
		getAsync(uri, queryBuilder) {
			return this.restClient.getAsync(uri, queryBuilder.build());
		}
	}
	
	class BootstrapPanel extends ComponentBase {
		render() {
			return 	<div className="panel">
						<div className="panel-heading">
   							<h4 className="panel-title">{this.props.title}</h4>
   						</div>
   						<div className="panel-body">
   							{this.props.children}
   						</div>
   					</div>
		}
	}

	BootstrapPanel.propTypes = {title : React.PropTypes.string.isRequired};

})(React, molgenis);	