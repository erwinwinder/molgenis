$(function() {
	var PAPER_WIDTH = 1200;
	var PAPER_HEIGHT = 600;
	var RECT_WIDTH = 200;
	var RECT_HEIGHT = 15;
	
	var graph = new joint.dia.Graph;
	var paper = new joint.dia.Paper({
		el: $('#paper'),
		width: PAPER_WIDTH,
		height: PAPER_HEIGHT,
		model: graph
	});
	
	var rects = {
	<#list workflow.steps as step>
		'${step.identifier?js_string}': new joint.shapes.basic.Rect({
			size: { 
				width: RECT_WIDTH, 
				height: RECT_HEIGHT
			},
			attrs: {
        		rect: {
            		fill: {
                		type: 'linearGradient',
                		stops: [
                    		{ offset: '0%', color: '#45484d' },
                    		{ offset: '100%', color: '#000000' }
                		],
                		attrs: { x1: '0%', y1: '0%', x2: '0%', y2: '100%' }
            		}
        		},
        		text: {
            		text: '${step.name?js_string}',
           	 		fill: '#fefefe',
            		'font-size': 10,
            		'font-weight': 'bold', 
            		'font-variant': 'small-caps'
        		}
    		}
		}),
	</#list>
	}

	_.each(rects, function(rect) { graph.addCell(rect); });

	<#list workflow.steps as step>
		<#if step.previousSteps?has_content>
			<#list step.previousSteps as prevStep>
				 graph.addCell(new joint.dia.Link({
    				source: { id: rects['${prevStep.identifier?js_string}'].id },
    				target: { id: rects['${step.identifier?js_string}'].id},
    				attrs: {
        				'.connection': {
            			stroke: '#333333',
            			'stroke-width': 3
        			},
        			'.marker-target': {
            			fill: '#333333',
            			d: 'M 10 0 L 0 5 L 10 10 z'
        			}
    			}}));
			</#list>
		</#if>
	</#list>
	
	joint.layout.DirectedGraph.layout(graph, { setLinkVertices: false });
});	

