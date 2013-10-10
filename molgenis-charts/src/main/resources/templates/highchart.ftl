 $('#container').highcharts({
 	series: [{
    	data: [
    		<#list chart.data.data as p>
			[${p.xvalue},${p.yvalue}],
			</#list>
		]
    }]
});