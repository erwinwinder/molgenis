<html>
	<head>
		<script src="/js/jquery-1.8.3.min.js"></script>
		<script src="/js/highcharts-3.0.6/highcharts.js"></script>
		<script src="/js/highcharts-3.0.6/highcharts-more.js"></script>
		
		<script type="text/javascript">
	  		$(function() {
				$.getScript("http://localhost:8080/charts/line?entity=eelde&x=${x}&y=${y}");
			});	
		</script>
	</head>
	<body>
		<div id="container" ></div>
	</body>
</html>