/**
Core script to handle the entire layout and base functions
**/
var App = function () {

    // IE mode
    var isRTL = false;
    var isIE8 = false;
    var isIE9 = false;
    var isIE10 = false;

    var sidebarWidth = 225;
    var sidebarCollapsedWidth = 35;

    var responsiveHandlers = [];

    // theme layout color set
    var layoutColorCodes = {
        'blue': '#4b8df8',
        'red': '#e02222',
        'green': '#35aa47',
        'purple': '#852b99',
        'grey': '#555555',
        'light-grey': '#fafafa',
        'yellow': '#ffb848'
    };

    // last popep popover
    var lastPopedPopover;

    var handleInit = function() {

        if (jQuery('body').css('direction') === 'rtl') {
            isRTL = true;
        }

        isIE8 = !! navigator.userAgent.match(/MSIE 8.0/);
        isIE9 = !! navigator.userAgent.match(/MSIE 9.0/);
        isIE10 = !! navigator.userAgent.match(/MSIE 10.0/);
        
        if (isIE10) {
            jQuery('html').addClass('ie10'); // detect IE10 version
        }
    }

    var handleDesktopTabletContents = function () {
        // loops all page elements with "responsive" class and applies classes for tablet mode
        // For Metronic  1280px or less set as tablet mode to display the content properly
        if (jQuery(window).width() <= 1280 || jQuery('body').hasClass('page-boxed')) {
            jQuery(".responsive").each(function () {
                var forTablet = jQuery(this).attr('data-tablet');
                var forDesktop = jQuery(this).attr('data-desktop');
                if (forTablet) {
                    jQuery(this).removeClass(forDesktop);
                    jQuery(this).addClass(forTablet);
                }
            });
        }

        // loops all page elements with "responsive" class and applied classes for desktop mode
        // For Metronic  higher 1280px set as desktop mode to display the content properly
        if (jQuery(window).width() > 1280 && jQuery('body').hasClass('page-boxed') === false) {
            jQuery(".responsive").each(function () {
                var forTablet = jQuery(this).attr('data-tablet');
                var forDesktop = jQuery(this).attr('data-desktop');
                if (forTablet) {
                    jQuery(this).removeClass(forTablet);
                    jQuery(this).addClass(forDesktop);
                }
            });
        }
    }

    var handleSidebarState = function () {
        // remove sidebar toggler if window width smaller than 900(for table and phone mode)
        if (jQuery(window).width() < 980) {
            jQuery('body').removeClass("page-sidebar-closed");
        }
    }

    var runResponsiveHandlers = function () {
        // reinitialize other subscribed elements
        for (var i in responsiveHandlers) {
            var each = responsiveHandlers[i];
            each.call();
        }
    }

    var handleResponsive = function () {
        handleTooltips();
        handleSidebarState();
        handleDesktopTabletContents();
        handleSidebarAndContentHeight();
        handleChoosenSelect();
        handleFixedSidebar();
        runResponsiveHandlers();
    }

    var handleResponsiveOnInit = function () {
        handleSidebarState();
        handleDesktopTabletContents();
        handleSidebarAndContentHeight();
    }

    var handleResponsiveOnResize = function () {
        var resize;
        if (isIE8) {
            var currheight;
            jQuery(window).resize(function() {
                if(currheight == document.documentElement.clientHeight) {
                    return; //quite event since only body resized not window.
                }                
                if (resize) {
                    clearTimeout(resize);
                }   
                resize = setTimeout(function() {
                    handleResponsive();    
                }, 50); // wait 50ms until window resize finishes.                
                currheight = document.documentElement.clientHeight; // store last body client height
            });
        } else {
            jQuery(window).resize(function() {
                if (resize) {
                    clearTimeout(resize);
                }   
                resize = setTimeout(function() {
                    handleResponsive();    
                }, 50); // wait 50ms until window resize finishes.
            });
        }   
    }

    //* BEGIN:CORE HANDLERS *//
    // this function handles responsive layout on screen size resize or mobile device rotate.
  
    var handleSidebarAndContentHeight = function () {
        var content = jQuery('.page-content');
        var sidebar = jQuery('.page-sidebar');
        var body = jQuery('body');
        var height;

        if (body.hasClass("page-footer-fixed") === true && body.hasClass("page-sidebar-fixed") === false) {
            var available_height = jQuery(window).height() - jQuery('.footer').height();
            if (content.height() <  available_height) {
                content.attr('style', 'min-height:' + available_height + 'px !important');
            }
        } else {
            if (body.hasClass('page-sidebar-fixed')) {
                height = _calculateFixedSidebarViewportHeight();
            } else {
                height = sidebar.height() + 20;
            }
            if (height >= content.height()) {
                content.attr('style', 'min-height:' + height + 'px !important');
            } 
        }          
    }

    var handleSidebarMenu = function () {
        jQuery('.page-sidebar').on('click', 'li > a', function (e) {
                if (jQuery(this).next().hasClass('sub-menu') == false) {
                    if (jQuery('.btn-navbar').hasClass('collapsed') == false) {
                        jQuery('.btn-navbar').click();
                    }
                    return;
                }

                var parent = jQuery(this).parent().parent();
                var the = jQuery(this);

                parent.children('li.open').children('a').children('.arrow').removeClass('open');
                parent.children('li.open').children('.sub-menu').slideUp(200);
                parent.children('li.open').removeClass('open');

                var sub = jQuery(this).next();
                var slideOffeset = -200;
                var slideSpeed = 200;

                if (sub.is(":visible")) {
                    jQuery('.arrow', jQuery(this)).removeClass("open");
                    jQuery(this).parent().removeClass("open");
                    sub.slideUp(slideSpeed, function () {
                        if (jQuery('body').hasClass('page-sidebar-fixed') == false && jQuery('body').hasClass('page-sidebar-closed') == false) {
                            App.scrollTo(the, slideOffeset);
                        }                       
                        handleSidebarAndContentHeight();
                    });
                } else {
                    jQuery('.arrow', jQuery(this)).addClass("open");
                    jQuery(this).parent().addClass("open");
                    sub.slideDown(slideSpeed, function () {
                        if (jQuery('body').hasClass('page-sidebar-fixed') == false && jQuery('body').hasClass('page-sidebar-closed') == false) {
                            App.scrollTo(the, slideOffeset);
                        }
                        handleSidebarAndContentHeight();
                    });
                }

                e.preventDefault();
            });

        // handle ajax links
        jQuery('.page-sidebar').on('click', ' li > a.ajaxify', function (e) {
                e.preventDefault();
                App.scrollTop();

                var url = jQuery(this).attr("href");
                var menuContainer = jQuery('.page-sidebar ul');
                var pageContent = jQuery('.page-content');
                var pageContentBody = jQuery('.page-content .page-content-body');

                menuContainer.children('li.active').removeClass('active');
                menuContainer.children('arrow.open').removeClass('open');

                jQuery(this).parents('li').each(function () {
                        jQuery(this).addClass('active');
                        jQuery(this).children('a > span.arrow').addClass('open');
                    });
                jQuery(this).parents('li').addClass('active');

                App.blockUI(pageContent, false);

                jQuery.ajax({
                    type: "GET",
                    cache: false,
                    url: url,
                    dataType: "html",
                    success: function(res) 
                    {
                        App.unblockUI(pageContent);
                        pageContentBody.html(res);
                        App.fixContentHeight(); // fix content height
                        App.initUniform(); // initialize uniform elements
                    },
                    error: function(xhr, ajaxOptions, thrownError)
                    {
                        pageContentBody.html('<h4>Could not load the requested content.</h4>');
                        App.unblockUI(pageContent);
                    },
                    async: false
                });
        });
    }

    var _calculateFixedSidebarViewportHeight = function () {
        var sidebarHeight = jQuery(window).height() - jQuery('.header').height() + 1;
        if (jQuery('body').hasClass("page-footer-fixed")) {
            sidebarHeight = sidebarHeight - jQuery('.footer').height();
        }

        return sidebarHeight; 
    }

    var handleFixedSidebar = function () {
        var menu = jQuery('.page-sidebar-menu');

        if (menu.parent('.slimScrollDiv').size() === 1) { // destroy existing instance before updating the height
            menu.slimScroll({
                destroy: true
            });
            menu.removeAttr('style');
            jQuery('.page-sidebar').removeAttr('style');            
        }

        if (jQuery('.page-sidebar-fixed').size() === 0) {
            handleSidebarAndContentHeight();
            return;
        }

        if (jQuery(window).width() >= 980) {
            var sidebarHeight = _calculateFixedSidebarViewportHeight();

            menu.slimScroll({
                size: '7px',
                color: '#a1b2bd',
                opacity: .3,
                position: isRTL ? 'left' : (jQuery('.page-sidebar-on-right').size() === 1 ? 'left' : 'right'),
                height: sidebarHeight,
                allowPageScroll: false,
                disableFadeOut: false
            });
            handleSidebarAndContentHeight();
        }
    }

    var handleFixedSidebarHoverable = function () {
        if (jQuery('body').hasClass('page-sidebar-fixed') === false) {
            return;
        }

        jQuery('.page-sidebar').off('mouseenter').on('mouseenter', function () {
            var body = jQuery('body');

            if ((body.hasClass('page-sidebar-closed') === false || body.hasClass('page-sidebar-fixed') === false) || jQuery(this).hasClass('page-sidebar-hovering')) {
                return;
            }

            body.removeClass('page-sidebar-closed').addClass('page-sidebar-hover-on');
            jQuery(this).addClass('page-sidebar-hovering');                
            jQuery(this).animate({
                width: sidebarWidth
            }, 400, '', function () {
                jQuery(this).removeClass('page-sidebar-hovering');
            });
        });

        jQuery('.page-sidebar').off('mouseleave').on('mouseleave', function () {
            var body = jQuery('body');

            if ((body.hasClass('page-sidebar-hover-on') === false || body.hasClass('page-sidebar-fixed') === false) || jQuery(this).hasClass('page-sidebar-hovering')) {
                return;
            }

            jQuery(this).addClass('page-sidebar-hovering');
            jQuery(this).animate({
                width: sidebarCollapsedWidth
            }, 400, '', function () {
                jQuery('body').addClass('page-sidebar-closed').removeClass('page-sidebar-hover-on');
                jQuery(this).removeClass('page-sidebar-hovering');
            });
        });
    }

    var handleSidebarToggler = function () {
        // handle sidebar show/hide
        jQuery('.page-sidebar').on('click', '.sidebar-toggler', function (e) {            
            var body = jQuery('body');
            var sidebar = jQuery('.page-sidebar');

            if ((body.hasClass("page-sidebar-hover-on") && body.hasClass('page-sidebar-fixed')) || sidebar.hasClass('page-sidebar-hovering')) {
                body.removeClass('page-sidebar-hover-on');
                sidebar.css('width', '').hide().show();
                e.stopPropagation();
                runResponsiveHandlers();
                return;
            }

            jQuery(".sidebar-search", sidebar).removeClass("open");

            if (body.hasClass("page-sidebar-closed")) {
                body.removeClass("page-sidebar-closed");
                if (body.hasClass('page-sidebar-fixed')) {
                    sidebar.css('width', '');
                }
            } else {
                body.addClass("page-sidebar-closed");
            }
            runResponsiveHandlers();
        });

        // handle the search bar close
        jQuery('.page-sidebar').on('click', '.sidebar-search .remove', function (e) {
            e.preventDefault();
            jQuery('.sidebar-search').removeClass("open");
        });

        // handle the search query submit on enter press
        jQuery('.page-sidebar').on('keypress', '.sidebar-search input', function (e) {
            if (e.which == 13) {
                window.location.href = "extra_search.html";
                return false; //<---- Add this line
            }
        });

        // handle the search submit
        jQuery('.sidebar-search .submit').on('click', function (e) {
            e.preventDefault();
          
                if (jQuery('body').hasClass("page-sidebar-closed")) {
                    if (jQuery('.sidebar-search').hasClass('open') == false) {
                        if (jQuery('.page-sidebar-fixed').size() === 1) {
                            jQuery('.page-sidebar .sidebar-toggler').click(); //trigger sidebar toggle button
                        }
                        jQuery('.sidebar-search').addClass("open");
                    } else {
                        window.location.href = "extra_search.html";
                    }
                } else {
                    window.location.href = "extra_search.html";
            }
        });
    }

    var handleHorizontalMenu = function () {
        //handle hor menu search form toggler click
        jQuery('.header').on('click', '.hor-menu .hor-menu-search-form-toggler', function (e) {
                if (jQuery(this).hasClass('hide')) {
                    jQuery(this).removeClass('hide');
                    jQuery('.header .hor-menu .search-form').hide();
                } else {
                    jQuery(this).addClass('hide');
                    jQuery('.header .hor-menu .search-form').show();
                }
                e.preventDefault();
            });

        //handle hor menu search button click
        jQuery('.header').on('click', '.hor-menu .search-form .btn', function (e) {
                window.location.href = "extra_search.html";
                e.preventDefault();
            });

        //handle hor menu search form on enter press
        jQuery('.header').on('keypress', '.hor-menu .search-form input', function (e) {
                if (e.which == 13) {
                    window.location.href = "extra_search.html";
                    return false;
                }
            });
    }

    var handleGoTop = function () {
        /* set variables locally for increased performance */
        jQuery('.footer').on('click', '.go-top', function (e) {
            App.scrollTo();
            e.preventDefault();
        });
    }

    var handlePortletTools = function () {
        jQuery('body').on('click', '.portlet > .portlet-title > .tools > a.remove', function (e) {
            e.preventDefault();
            jQuery(this).closest(".portlet").remove();
        });

        jQuery('body').on('click', '.portlet > .portlet-title > .tools > a.reload', function (e) {
            e.preventDefault();
            var el = jQuery(this).closest(".portlet").children(".portlet-body");
            App.blockUI(el);
            window.setTimeout(function () {
                App.unblockUI(el);
            }, 1000);
        });

        jQuery('body').on('click', '.portlet > .portlet-title > .tools > .collapse, .portlet .portlet-title > .tools > .expand', function (e) {
            e.preventDefault();
            var el = jQuery(this).closest(".portlet").children(".portlet-body");
            if (jQuery(this).hasClass("collapse")) {
                jQuery(this).removeClass("collapse").addClass("expand");
                el.slideUp(200);
            } else {
                jQuery(this).removeClass("expand").addClass("collapse");
                el.slideDown(200);
            }
        });
    }

    var handleUniform = function () {
        if (!jQuery().uniform) {
            return;
        }
        var test = jQuery("input[type=checkbox]:not(.toggle), input[type=radio]:not(.toggle, .star)");
        if (test.size() > 0) {
            test.each(function () {
                if (jQuery(this).parents(".checker").size() == 0) {
                    jQuery(this).show();
                    jQuery(this).uniform();
                }
            });
        }
    }

    var handleAccordions = function () {
        jQuery(".accordion").collapse().height('auto');
        var lastClicked;
        //add scrollable class name if you need scrollable panes
        jQuery('body').on('click', '.accordion.scrollable .accordion-toggle', function () {
            lastClicked = jQuery(this);
        }); //move to faq section

        jQuery('body').on('shown', '.accordion.scrollable', function () {
            jQuery('html,body').animate({
                scrollTop: lastClicked.offset().top - 150
            }, 'slow');
        });
    }

    var handleTabs = function () {

        // function to fix left/right tab contents
        var fixTabHeight = function(tab) {
            jQuery(tab).each(function() {
                var content = jQuery(jQuery(jQuery(this).attr("href")));
                var tab = jQuery(this).parent().parent();
                if (tab.height() > content.height()) {
                    content.css('min-height', tab.height());    
                } 
            });            
        }

        // fix tab content on tab shown
        jQuery('body').on('shown', '.nav.nav-tabs.tabs-left a[data-toggle="tab"], .nav.nav-tabs.tabs-right a[data-toggle="tab"]', function(){
            fixTabHeight(jQuery(this)); 
        });

        jQuery('body').on('shown', '.nav.nav-tabs', function(){
            handleSidebarAndContentHeight();
        });

        //fix tab contents for left/right tabs
        fixTabHeight('.nav.nav-tabs.tabs-left > li.active > a[data-toggle="tab"], .nav.nav-tabs.tabs-right > li.active > a[data-toggle="tab"]');

        //activate tab if tab id provided in the URL
        if(location.hash) {
            var tabid = location.hash.substr(1);
            jQuery('a[href="#'+tabid+'"]').click();
        }
    }

    var handleScrollers = function () {
        jQuery('.scroller').each(function () {
                var height;
                if (jQuery(this).attr("data-height")) {
                    height = jQuery(this).attr("data-height");
                } else {
                    height = jQuery(this).css('height');
                }
                jQuery(this).slimScroll({
                        size: '7px',
                        color: '#a1b2bd',
                        position: isRTL ? 'left' : 'right',
                        height:  height,
                        alwaysVisible: (jQuery(this).attr("data-always-visible") == "1" ? true : false),
                        railVisible: (jQuery(this).attr("data-rail-visible") == "1" ? true : false),
                        disableFadeOut: true
                    });
            });
    }

    var handleTooltips = function () {
        if (App.isTouchDevice()) { // if touch device, some tooltips can be skipped in order to not conflict with click events
            jQuery('.tooltips:not(.no-tooltip-on-touch-device)').tooltip();
        } else {
            jQuery('.tooltips').tooltip();
        }
    }

    var handleDropdowns = function () {
        jQuery('body').on('click', '.dropdown-menu.hold-on-click', function(e){
            e.stopPropagation();
        })
    }

    var handleModal = function () {
        // this function adds .modal-open class to body element for select2 and chosen dropdown hacks
        if (jQuery().modalmanager) {
            return; // skip if Extended Modal plugin is used
        }

        jQuery('body').on('shown', '.modal', function(e){
            jQuery('body').addClass('modal-open');
        });

        jQuery('body').on('hidden', '.modal', function(e){
            if (jQuery('.modal').size() === 0) {
                jQuery('body').removeClass('modal-open');
            }
        });
    }

    var handlePopovers = function () {
        jQuery('.popovers').popover();

        // close last poped popover

        jQuery(document).on('click.popover.data-api',function(e) {
            if(lastPopedPopover){
                lastPopedPopover.popover('hide');
            } 
        });
    }

    var handleChoosenSelect = function () {
        if (!jQuery().chosen) {
            return;
        }

        jQuery(".chosen").each(function () {
            jQuery(this).chosen({
                allow_single_deselect: jQuery(this).attr("data-with-deselect") == "1" ? true : false
            });
        });
    }

    var handleFancybox = function () {
        if (!jQuery.fancybox) {
            return;
        }

        if (jQuery(".fancybox-button").size() > 0) {
            jQuery(".fancybox-button").fancybox({
                groupAttr: 'data-rel',
                prevEffect: 'none',
                nextEffect: 'none',
                closeBtn: true,
                helpers: {
                    title: {
                        type: 'inside'
                    }
                }
            });
        }
    }

    var handleTheme = function () {

        var panel = jQuery('.color-panel');

        if (jQuery('body').hasClass('page-boxed') == false) {
            jQuery('.layout-option', panel).val("fluid");
        }
        
        jQuery('.sidebar-option', panel).val("default");
        jQuery('.header-option', panel).val("fixed");
        jQuery('.footer-option', panel).val("default"); 

        //handle theme layout
        var resetLayout = function () {
            jQuery("body").
                removeClass("page-boxed").
                removeClass("page-footer-fixed").
                removeClass("page-sidebar-fixed").
                removeClass("page-header-fixed");

            jQuery('.header > .navbar-inner > .container').removeClass("container").addClass("container-fluid");

            if (jQuery('.page-container').parent(".container").size() === 1) {
                jQuery('.page-container').insertAfter('.header');
            } 

            if (jQuery('.footer > .container').size() === 1) {                        
                jQuery('.footer').html(jQuery('.footer > .container').html());                        
            } else if (jQuery('.footer').parent(".container").size() === 1) {                        
                jQuery('.footer').insertAfter('.page-container');
            }

            jQuery('body > .container').remove(); 
        }

        var lastSelectedLayout = '';

        var setLayout = function () {

            var layoutOption = jQuery('.layout-option', panel).val();
            var sidebarOption = jQuery('.sidebar-option', panel).val();
            var headerOption = jQuery('.header-option', panel).val();
            var footerOption = jQuery('.footer-option', panel).val(); 

            if (sidebarOption == "fixed" && headerOption == "default") {
                alert('Default Header with Fixed Sidebar option is not supported. Proceed with Default Header with Default Sidebar.');
                jQuery('.sidebar-option', panel).val("default");
                sidebarOption = 'default';
            }

            resetLayout(); // reset layout to default state

            if (layoutOption === "boxed") {
                jQuery("body").addClass("page-boxed");

                // set header
                jQuery('.header > .navbar-inner > .container-fluid').removeClass("container-fluid").addClass("container");
                var cont = jQuery('.header').after('<div class="container"></div>');

                // set content
                jQuery('.page-container').appendTo('body > .container');

                // set footer
                if (footerOption === 'fixed' || sidebarOption === 'default') {
                    jQuery('.footer').html('<div class="container">'+jQuery('.footer').html()+'</div>');
                } else {
                    jQuery('.footer').appendTo('body > .container');
                }            
            }

            if (lastSelectedLayout != layoutOption) {
                //layout changed, run responsive handler:
                runResponsiveHandlers();
            }
            lastSelectedLayout = layoutOption;

            //header
            if (headerOption === 'fixed') {
                jQuery("body").addClass("page-header-fixed");
                jQuery(".header").removeClass("navbar-static-top").addClass("navbar-fixed-top");
            } else {
                jQuery("body").removeClass("page-header-fixed");
                jQuery(".header").removeClass("navbar-fixed-top").addClass("navbar-static-top");
            }

            //sidebar
            if (sidebarOption === 'fixed') {
                jQuery("body").addClass("page-sidebar-fixed");
            } else {
                jQuery("body").removeClass("page-sidebar-fixed");
            }

            //footer 
            if (footerOption === 'fixed') {
                jQuery("body").addClass("page-footer-fixed");
            } else {
                jQuery("body").removeClass("page-footer-fixed");
            }

            handleSidebarAndContentHeight(); // fix content height            
            handleFixedSidebar(); // reinitialize fixed sidebar
            handleFixedSidebarHoverable(); // reinitialize fixed sidebar hover effect
        }

        // handle theme colors
        var setColor = function (color) {
            jQuery('#style_color').attr("href", "assets/css/themes/" + color + ".css");
            jQuery.cookie('style_color', color);                
        }

        jQuery('.icon-color', panel).click(function () {
            jQuery('.color-mode').show();
            jQuery('.icon-color-close').show();
        });

        jQuery('.icon-color-close', panel).click(function () {
            jQuery('.color-mode').hide();
            jQuery('.icon-color-close').hide();
        });

        jQuery('li', panel).click(function () {
            var color = jQuery(this).attr("data-style");
            setColor(color);
            jQuery('.inline li', panel).removeClass("current");
            jQuery(this).addClass("current");
        });

        jQuery('.layout-option, .header-option, .sidebar-option, .footer-option', panel).change(setLayout);
    }

    var handleFixInputPlaceholderForIE = function () {
        //fix html5 placeholder attribute for ie7 & ie8
        if (isIE8 || isIE9) { // ie7&ie8
            // this is html5 placeholder fix for inputs, inputs with placeholder-no-fix class will be skipped(e.g: we need this for password fields)
            jQuery('input[placeholder]:not(.placeholder-no-fix), textarea[placeholder]:not(.placeholder-no-fix)').each(function () {

                var input = jQuery(this);

                if(input.val()=='' && input.attr("placeholder") != '') {
                    input.addClass("placeholder").val(input.attr('placeholder'));
                }

                input.focus(function () {
                    if (input.val() == input.attr('placeholder')) {
                        input.val('');
                    }
                });

                input.blur(function () {                         
                    if (input.val() == '' || input.val() == input.attr('placeholder')) {
                        input.val(input.attr('placeholder'));
                    }
                });
            });
        }
    }

    var handleFullScreenMode = function() {
        // mozfullscreenerror event handler
       
        // toggle full screen
        function toggleFullScreen() {
          if (!document.fullscreenElement &&    // alternative standard method
              !document.mozFullScreenElement && !document.webkitFullscreenElement) {  // current working methods
            if (document.documentElement.requestFullscreen) {
              document.documentElement.requestFullscreen();
            } else if (document.documentElement.mozRequestFullScreen) {
              document.documentElement.mozRequestFullScreen();
            } else if (document.documentElement.webkitRequestFullscreen) {
              document.documentElement.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
            }
          } else {
            if (document.cancelFullScreen) {
              document.cancelFullScreen();
            } else if (document.mozCancelFullScreen) {
              document.mozCancelFullScreen();
            } else if (document.webkitCancelFullScreen) {
              document.webkitCancelFullScreen();
            }
          }
        }

        jQuery('#trigger_fullscreen').click(function() {
            toggleFullScreen();
        });
    }

    //* END:CORE HANDLERS *//

    return {

        //main function to initiate template pages
        init: function () {

            //IMPORTANT!!!: Do not modify the core handlers call order.

            //core handlers
            handleInit();
            handleResponsiveOnResize(); // set and handle responsive    
            handleUniform();        
            handleScrollers(); // handles slim scrolling contents 
            handleResponsiveOnInit(); // handler responsive elements on page load

            //layout handlers
            handleFixedSidebar(); // handles fixed sidebar menu
            handleFixedSidebarHoverable(); // handles fixed sidebar on hover effect 
            handleSidebarMenu(); // handles main menu
            handleHorizontalMenu(); // handles horizontal menu
            handleSidebarToggler(); // handles sidebar hide/show            
            handleFixInputPlaceholderForIE(); // fixes/enables html5 placeholder attribute for IE9, IE8
            handleGoTop(); //handles scroll to top functionality in the footer
            handleTheme(); // handles style customer tool

            //ui component handlers
            handlePortletTools(); // handles portlet action bar functionality(refresh, configure, toggle, remove)
            handleDropdowns(); // handle dropdowns
            handleTabs(); // handle tabs
            handleTooltips(); // handle bootstrap tooltips
            handlePopovers(); // handles bootstrap popovers
            handleAccordions(); //handles accordions
            handleChoosenSelect(); // handles bootstrap chosen dropdowns     
            handleModal();

            App.addResponsiveHandler(handleChoosenSelect); // reinitiate chosen dropdown on main content resize. disable this line if you don't really use chosen dropdowns.
            handleFullScreenMode() // handles full screen
        },

        fixContentHeight: function () {
            handleSidebarAndContentHeight();
        },

        setLastPopedPopover: function (el) {
            lastPopedPopover = el;
        },

        addResponsiveHandler: function (func) {
            responsiveHandlers.push(func);
        },

        // useful function to make equal height for contacts stand side by side
        setEqualHeight: function (els) {
            var tallestEl = 0;
            els = jQuery(els);
            els.each(function () {
                    var currentHeight = jQuery(this).height();
                    if (currentHeight > tallestEl) {
                        tallestColumn = currentHeight;
                    }
                });
            els.height(tallestEl);
        },

        // wrapper function to scroll to an element
        scrollTo: function (el, offeset) {
            pos = el ? el.offset().top : 0;
            jQuery('html,body').animate({
                    scrollTop: pos + (offeset ? offeset : 0)
                }, 'slow');
        },

        scrollTop: function () {
            App.scrollTo();
        },

        // wrapper function to  block element(indicate loading)
        blockUI: function (el, centerY) {
            var el = jQuery(el); 
            el.block({
                    message: '<img src="./assets/img/ajax-loading.gif" align="">',
                    centerY: centerY != undefined ? centerY : true,
                    css: {
                        top: '10%',
                        border: 'none',
                        padding: '2px',
                        backgroundColor: 'none'
                    },
                    overlayCSS: {
                        backgroundColor: '#000',
                        opacity: 0.05,
                        cursor: 'wait'
                    }
                });
        },

        // wrapper function to  un-block element(finish loading)
        unblockUI: function (el) {
            jQuery(el).unblock({
                    onUnblock: function () {
                        jQuery(el).removeAttr("style");
                    }
                });
        },

        // initializes uniform elements
        initUniform: function (els) {

            if (els) {
                jQuery(els).each(function () {
                        if (jQuery(this).parents(".checker").size() == 0) {
                            jQuery(this).show();
                            jQuery(this).uniform();
                        }
                    });
            } else {
                handleUniform();
            }

        },

        updateUniform : function(els) {
            jQuery.uniform.update(els);
        },

        // initializes choosen dropdowns
        initChosenSelect: function (els) {
            jQuery(els).chosen({
                    allow_single_deselect: true
                });
        },

        initFancybox: function () {
            handleFancybox();
        },

        getActualVal: function (el) {
            var el = jQuery(el);
            if (el.val() === el.attr("placeholder")) {
                return "";
            }

            return el.val();
        },

        getURLParameter: function (paramName) {
            var searchString = window.location.search.substring(1),
                i, val, params = searchString.split("&");

            for (i = 0; i < params.length; i++) {
                val = params[i].split("=");
                if (val[0] == paramName) {
                    return unescape(val[1]);
                }
            }
            return null;
        },

        // check for device touch support
        isTouchDevice: function () {
            try {
                document.createEvent("TouchEvent");
                return true;
            } catch (e) {
                return false;
            }
        },

        isIE8: function () {
            return isIE8;
        },

        isRTL: function () {
            return isRTL;
        },

        getLayoutColorCode: function (name) {
            if (layoutColorCodes[name]) {
                return layoutColorCodes[name];
            } else {
                return '';
            }
        }

    };

}();