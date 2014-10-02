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
	
});