var Tasks = function () {


    return {

        //main function to initiate the module
        initDashboardWidget: function () {
			jQuery('input.liChild').change(function() {
				if (jQuery(this).is(':checked')) { 
					jQuery(this).parents('li').addClass("task-done"); 
				} else { 
					jQuery(this).parents('li').removeClass("task-done"); 
				}
			}); 
        }

    };

}();