$(function() {
	$('#submit-button').on('click', function(e) {
		e.preventDefault();
		
		showSpinner(function() {
			$('form').submit();
		});
		
	});
	
});