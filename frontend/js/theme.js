$(function(){
	// menu logica
	$('.mainNav nav ul > li').hover(
		function() {
			$(this).find('.submenu').stop().fadeIn(150);
			$(this).find('> a').addClass('open');
		},
		function() {
			$(this).find('.submenu').stop().hide();			
			$(this).find('> a').removeClass('open');			
		}
	);
	$('form select').selectpicker({
		style: 'btn-primary'
	});	
	$('form input').iCheck({
		checkboxClass: 'icheckbox_square-blue',
		radioClass: 'iradio_square'
	});	
});